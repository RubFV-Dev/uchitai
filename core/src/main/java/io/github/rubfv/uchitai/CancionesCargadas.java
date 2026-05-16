package io.github.rubfv.uchitai;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CancionesCargadas {
    private FileHandle[] canciones;
    private Music cancionActual;
    private int indiceCancionAct;
    private Texture txtFondo;
    private Sprite sprFondo;
    
    public String nombreCancion(int i) {
    		return canciones[i].name();
    }
    
    public String rutaCancion(int i) {
    		return canciones[i].path();
    }
    
    public int size() {
    		return canciones.length;
    }
    
    public Music getCancionActual() {
    		return cancionActual;
    }
    
    public Sprite getSprite() {
    		return sprFondo;
    }

    public int getIndiceCancion() {
    		return indiceCancionAct;
    }
    
    public void cargarListaCanciones() {
	    FileHandle directorio = Gdx.files.internal("../assets/canciones/");
	    ArrayList<FileHandle> carpetas = new ArrayList<>();
	    
	    //Consigue las carpetas dentro del directorio canciones
	    for (FileHandle actual : directorio.list()) {
	    		if (actual.isDirectory()) {
	    			FileHandle cancion = Gdx.files.internal(actual.path() + "/" + actual.name() + ".mp3");
	    			if (cancion.exists()) {
			    		carpetas.add(actual);
			    		System.out.println(actual.name());
	    			}
	    		}
	    }
	    
	    //Se guarda en el array las direcciones para hacerlo más ágil :O
	    canciones = new FileHandle[carpetas.size()];
	    for (int i = 0; i < carpetas.size(); i++) {
	    		canciones[i] = new FileHandle(carpetas.get(i).path());
	    }
    }

    public void antCancion() {
    		if (indiceCancionAct - 1 >= 0) {
    			UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
    			if (juego.getDibujado() instanceof DibujadoSeleccion) {
    				DibujadoSeleccion sel = (DibujadoSeleccion)juego.getDibujado();
    				
    				sel.anim.animDeslizarIzq();
    			}
    			indiceCancionAct--;
    			cargarCancion(indiceCancionAct);
    		}
    }
    
    public void sigCancion() {
    		if (indiceCancionAct + 1 < canciones.length) {
    			UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
    			if (juego.getDibujado() instanceof DibujadoSeleccion) {
    				DibujadoSeleccion sel = (DibujadoSeleccion)juego.getDibujado();
    				
    				sel.anim.animDeslizarDer();
    			}
    			indiceCancionAct++;
    			cargarCancion(indiceCancionAct);
    		}
    }
    
    // -1 para aleatorio
    public void cargarCancion(int i) {
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
	    if (i < 0 || i >= canciones.length) {
		    //Elige una canción al azar
	    		do {
	    		    i = new Random().nextInt(canciones.length);
	    		} while (i == indiceCancionAct);
	    }
	    //Limpiar memory
	    if (cancionActual != null) {
		    cancionActual.dispose();
	    }
	    if (txtFondo != null) {
	    		txtFondo.dispose();
	    }
	    
    		String ruta = canciones[i].path() + "/" + canciones[i].name();
    		//Iniciar canción y poner fondo
    		try {
    			cancionActual = Gdx.audio.newMusic(Gdx.files.internal(ruta + ".mp3"));
    			
    			//cargar el fondo
        		try {
        		    txtFondo = new Texture(Gdx.files.internal(ruta + ".png"));
	    	    }
	    	    catch (Exception noFoto) {
	    	    		// Esto no debería existir, pero lo dejo por flojo
	    	    		Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
	    	    		noFondo.setColor(new Color(0, 0, 0, 0));
	    	    		noFondo.fill();
	    	    		
	    	    		txtFondo = new Texture(noFondo);
	    	    		noFondo.dispose();
	    	    }
        		
			sprFondo = new Sprite(txtFondo);
    		}
    		catch (Exception noSong) {
    			System.out.println("La cancion no existe, buu " + noSong.getMessage());
    			return;
    		}

        cancionActual.setLooping(true);
		cancionActual.play();
		//Configurar imagen de fondo
		//La imagen tiene un ratio menor o igual a 16:9
		if (txtFondo.getWidth() / txtFondo.getHeight() <= Coord.RATIO) {
			sprFondo.setScale((float)(Coord.RESOL_X + 10) / txtFondo.getWidth());
		}
		//La imagen tiene un ratio mayor a 16:9
		else {
			sprFondo.setScale((float)(Coord.RESOL_Y + 10) / txtFondo.getHeight());
		}
		//Centrar
		sprFondo.setPosition(
		(Coord.RESOL_X - sprFondo.getWidth()) / 2, 
		(Coord.RESOL_Y - sprFondo.getHeight()) / 2
		);
		
		indiceCancionAct = i;

		//Cargar en el juego el nuevo índice
		DibujadoGeneral.cargarFondoCancion(sprFondo);
		//Si está en la selección, recarga las portadas cargadas
		if (juego.getDibujado() instanceof DibujadoSeleccion) {
			DibujadoSeleccion sel = (DibujadoSeleccion)juego.getDibujado();
			
			sel.recargarTexturas();
		}
    }
    
    public void dispose() {
    		txtFondo.dispose();
    }
}
