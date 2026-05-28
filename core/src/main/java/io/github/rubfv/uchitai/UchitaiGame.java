package io.github.rubfv.uchitai;

import java.util.*;
import java.io.*;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

/*
 * 		Esta clase funciona para el manejo del dibujado del juego
 * 	procuraré que la gran mayoría del código esté comentado, sobretodo
 * 	cuando se necesite explicar qué fregados hice. ;)
*/
/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class UchitaiGame extends ApplicationAdapter {

	private PANTALLA pantallaAct, orgPantallaAct;

    private InputGeneral input;
    private DibujadoGeneral dibujado;
    private CancionesCargadas canciones;
    private Coord mouse;
    private GestorJuego gestorPartida;

    public PANTALLA getPantallaAct() {
    		return pantallaAct;
    }

    public void setPantallaAct(PANTALLA pantalla) {
    		pantallaAct = pantalla;
    }

    public CancionesCargadas getCanciones() {
    		return canciones;
    }

    public DibujadoGeneral getDibujado() {
    		return dibujado;
    }

    @Override
    public void resize(int width, int height) {
    		int tamHorizont = width;
    		int tamVertical = (int) (width / Coord.RATIO);
    		int tamX, tamY;

    		if (tamVertical > height) {
    			tamHorizont = (int) (height * Coord.RATIO);
    			tamVertical = height;
    		}

    		tamX = (int) (width - tamHorizont) / 2;
    		tamY = (int) (height - tamVertical) / 2;

    		Gdx.gl.glViewport(tamX, tamY, tamHorizont, tamVertical);
    }

    //Aquí se cargan las imágenes
    @Override
    public void create() {
		mouse = new Coord();
    		input = new InputGeneral(pantallaAct, mouse);
    		canciones = new CancionesCargadas();
    		Gdx.input.setInputProcessor(input);
    		DibujadoGeneral.setMouse(mouse);
    		Gdx.graphics.setWindowedMode(1280, 720);

        pantallaAct = PANTALLA.TITULO;
        orgPantallaAct = PANTALLA.TITULO;

        canciones.cargarListaCanciones();
        canciones.cargarCancion(-1);

        DibujadoGeneral.cargarFondoCancion(canciones.getSprite());
        dibujado = new DibujadoTitulo(null);
    }

    //Renderizado de la pantalla, se llamará 60 veces por segundo, es decir, 60FPS
    //Aquí iría la lógica del juego que depende del tiempo
    @Override
    public void render() {
    		//Si cambia de pantalla, se reorganiza la clase dibujado
		if (pantallaAct != orgPantallaAct) {
			if (dibujado.transCompletada()) {
				DibujadoGeneral nuevo = null;

				//Cargar nueva pantalla
				switch (pantallaAct) {
				case TITULO:		nuevo = new DibujadoTitulo(dibujado);		break;
				case SELECCION:	nuevo = new DibujadoSeleccion(dibujado, canciones);		break;
                case JUEGO:
                    Nivel nivelCargado = cargarNivelDesdeDisco(canciones.getIndiceCancion());
                    gestorPartida = new GestorJuego(nivelCargado, canciones.getCancionActual());
                    gestorPartida.iniciarJuego();
                    // Todo TEMA DE DIBUJADO
                    nuevo = new DibujadoJuego(dibujado);
                    break;
				}
				//Reestructurar
				if (nuevo != null) {
					dibujado.descargar(nuevo);
					dibujado = nuevo;
				}

				orgPantallaAct = pantallaAct;
			}
		}
		dibujado.dibujar();

        if (pantallaAct == PANTALLA.JUEGO && gestorPartida != null) {   //Para el reloj del juego
            boolean juegoFin= gestorPartida.limpieza();
            if(juegoFin){//si ya termino
                System.out.println("GAME OVER");

                //TODO cambio de pantalla
            }
        }
    }

    //Esto sirve para liberar la memoria de las imágenes que ya no se necesitan
    @Override
    public void dispose() {
    		DibujadoGeneral.dispose();

    		canciones.dispose();
        dibujado.descargar(null);
    }

    private Nivel cargarNivelDesdeDisco(int indiceCancion) {
        // Carpeta
        String rutaMapa = canciones.rutaCancion(indiceCancion) + "/" + canciones.nombreCancion(indiceCancion) + ".dat";
        FileHandle archivo = Gdx.files.local(rutaMapa);

        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(archivo.read())) {
                return (Nivel) ois.readObject();    //para reconstruir el objeto
            } catch (Exception e) {
                System.out.println("Error al leer el archivo del mapa: " + e.getMessage());
            }
        }

        // Si no existe el mapa entonces se manda algo vacío
        return new Nivel();
    }

    public GestorJuego getGestorPartida(){  //pa saber si hay un gestor(por que no existe en un inicio)
        return gestorPartida;
    }
}
