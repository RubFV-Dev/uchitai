package io.github.rubfv.uchitai;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;             //Muchas librerias no se usan
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
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/*
 * 		Esta clase funciona para el manejo del dibujado del juego
 * 	procuraré que la gran mayoría del código esté comentado, sobretodo
 * 	cuando se necesite explicar qué fregados hice. ;)
*/
/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class UchitaiGame extends ApplicationAdapter {

    public GestorArchivos gestorArch;
	private PANTALLA pantallaAct, orgPantallaAct;

    private InputGeneral input;
    private DibujadoGeneral dibujado;
    private CancionesCargadas canciones;
    private Coord mouse;
    private Gestor gestor;

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

    public UchitaiGame(NativeFileChooser fileChooser) {
        this.gestorArch = new GestorArchivos(fileChooser);
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
			if (pantallaAct == PANTALLA.JUEGO && orgPantallaAct == PANTALLA.SELECCION) {
				int i = canciones.getIndiceCancion();

				//Está en el botón de añadir
				if (i == canciones.size() - 1) {
					pantallaAct = PANTALLA.SELECCION;
					if (dibujado instanceof DibujadoSeleccion) {
						DibujadoSeleccion d = (DibujadoSeleccion)dibujado;

						d.setAniadir(true);
					}
				}
				//Cargar los datos
				else {
					String ruta = canciones.rutaCancion(i) + "/" + canciones.nombreCancion(i) + ".dat";

					if (!Gdx.files.local(ruta).exists()) pantallaAct = PANTALLA.EDICION;
				}

			}
			if (dibujado.transCompletada()) {
				Nivel nivelCargado;
				DibujadoGeneral nuevo = null;

				if (gestor != null) {
					gestor.guardar();
				}

				//Cargar nueva pantalla
				switch (pantallaAct) {
				case TITULO:
					nuevo = new DibujadoTitulo(dibujado);
					gestor = null;
					break;
				case SELECCION:
					nuevo = new DibujadoSeleccion(dibujado, canciones);
					gestor = null;
					break;
                case JUEGO:
                    nivelCargado = cargarNivelDesdeDisco(canciones.getIndiceCancion());
                    gestor = new GestorJuego(nivelCargado, canciones);
                    gestor.iniciar();
                    // Todo TEMA DE DIBUJADO
                    nuevo = new DibujadoJuego(dibujado);
                    break;
                case EDICION:
                    nivelCargado = cargarNivelDesdeDisco(canciones.getIndiceCancion());
                    gestor = new GestorEditor(nivelCargado, canciones);
                    gestor.iniciar();
                    // Todo TEMA DE DIBUJADO
                    nuevo = new DibujadoEdicion(dibujado);
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

        if ((pantallaAct == PANTALLA.JUEGO || pantallaAct == PANTALLA.EDICION) && gestor != null) {   //Para el reloj del juego
            boolean juegoFin = gestor.actualizar();
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
            }
            catch (Exception e) {
                System.out.println("Error al leer el archivo del mapa: " + e.getMessage());
            }
        }

		System.out.println("No existe un mapa para este nivel");

        // Si no existe el mapa entonces se manda algo vacío
        return new Nivel();
    }

    public Gestor getGestorPartida(){  //pa saber si hay un gestor(por que no existe en un inicio)
        return gestor;
    }
}
