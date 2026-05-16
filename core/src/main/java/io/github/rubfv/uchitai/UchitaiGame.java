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
		if (pantallaAct != orgPantallaAct) {
			DibujadoGeneral nuevo = null;
			
			//Cargar nueva pantalla
			switch (pantallaAct) {
			case TITULO:		nuevo = new DibujadoTitulo(dibujado);		break;
			case SELECCION:	nuevo = new DibujadoSeleccion(dibujado, canciones);		break;
			}
			//Reestructurar
			if (nuevo != null) {
				dibujado.descargar(nuevo);
				dibujado = nuevo;
			}
			
			orgPantallaAct = pantallaAct;
		}
		dibujado.dibujar();
    }

    //Esto sirve para liberar la memoria de las imágenes que ya no se necesitan
    @Override
    public void dispose() {
    		DibujadoGeneral.dispose();
    		
    		canciones.dispose();
        dibujado.descargar(null);
    }
}
