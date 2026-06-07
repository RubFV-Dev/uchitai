package io.github.rubfv.uchitai;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CancionesCargadas {
    private static FileHandle[] canciones;
    private Music cancionActual;
    private int indiceCancionAct;
    private Texture txtFondo;
    private Sprite sprFondo;
    static public FileHandle dirCanciones = Gdx.files.local("../Canciones/");

    public String nombreCancion(int i) {
        if (i >= 0 && i < canciones.length) {
            if (canciones[i] != null) {
                return canciones[i].name();
            } else {
                return "ERR";
            }
        } else {
            return "ANIADIR";
        }
    }

    public String rutaCancion(int i) {
        if (i >= 0 && i < canciones.length) {
            return canciones[i].path();
        } else {
            return "ANIADIR";
        }
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
    
    public void setIndiceCancion(int i) {
    		indiceCancionAct = i;
    }
    
    CancionesCargadas() {
    		cancionActual = null;
    		indiceCancionAct = -1;
    		txtFondo = null;
    		sprFondo = null;
    }

    public static void cargarListaCanciones() {
        if (!dirCanciones.exists()) {
            dirCanciones.mkdirs();
        }
        ArrayList<FileHandle> carpetas = new ArrayList<>();
        System.out.println(dirCanciones.path());

        // Consigue las carpetas dentro del directorio canciones
        for (FileHandle actual : dirCanciones.list()) {
            if (actual.isDirectory()) {
                FileHandle cancion = Gdx.files.local(actual.path() + "/" + actual.name() + ".mp3");
                System.out.println(actual.path() + "/" + actual.name() + ".mp3");
                if (cancion.exists()) {
                    carpetas.add(actual);
                    System.out.println(actual.name());
                }
            }
        }

        // Se guarda en el array las direcciones para hacerlo más ágil :O
        canciones = new FileHandle[carpetas.size() + 1];
        for (int i = 0; i < carpetas.size(); i++) {
            canciones[i] = new FileHandle(carpetas.get(i).path());
        }
        canciones[carpetas.size()] = null;
    }

    public void antCancion() {
        if (indiceCancionAct - 1 >= 0) {
            UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();
            if (juego.getDibujado() instanceof DibujadoSeleccion) {
                DibujadoSeleccion sel = (DibujadoSeleccion) juego.getDibujado();

                sel.anim.animDeslizarIzq();
                indiceCancionAct--;
                sel.setAniadir(false);
                cargarCancion(indiceCancionAct);
                sel.recargarTexturas();
            }
        }
    }

    public void sigCancion() {
        if (indiceCancionAct + 1 < canciones.length) {
            UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();
            if (juego.getDibujado() instanceof DibujadoSeleccion) {
                DibujadoSeleccion sel = (DibujadoSeleccion) juego.getDibujado();

                sel.anim.animDeslizarDer();
                indiceCancionAct++;
                sel.setAniadir(false);
                cargarCancion(indiceCancionAct);
                sel.recargarTexturas();
            }
        }
    }

    // -1 para aleatorio
    public void cargarCancion(int i) {
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();

        int realNumCanciones = canciones.length - 1;
        if (i < 0 || i > realNumCanciones) {
            if (realNumCanciones > 1) {
                do {
                    i = com.badlogic.gdx.math.MathUtils.random(realNumCanciones - 1);
                } while (i == indiceCancionAct);
            } else if (realNumCanciones == 1) {
                i = 0;
            } else {
                i = -1;
            }
        }

        if (cancionActual != null) {
            cancionActual.dispose();
            cancionActual = null;
        }
        if (txtFondo != null) {
            txtFondo.dispose();
        }

        // Si existe el directorio o no es añadir canción
        if (i != -1 && canciones[i] != null) {
            String ruta = canciones[i].path() + "/" + canciones[i].name();
            // Iniciar canción y poner fondo
            try {
            		boolean portada = false;
                cancionActual = Gdx.audio.newMusic(Gdx.files.local(ruta + ".mp3"));

                // cargar el fondo
                if (Gdx.files.local(ruta + ".png").exists()) {
	                	txtFondo = new Texture(Gdx.files.local(ruta + ".png"));
	    				portada = true;
				}
				//un jpeg
				else if (!portada && Gdx.files.local(ruta + ".jpeg").exists()) {
					txtFondo = new Texture(Gdx.files.local(ruta + ".jpeg"));
	    				portada = true;
				}
				//el primo malvado de png
				else if (!portada && Gdx.files.local(ruta + ".jpg").exists()) {
					txtFondo = new Texture(Gdx.files.local(ruta + ".jpg"));
	    				portada = true;
				}
				//Ya ni pedo, ahí muere
				else {
		    	    		// Esto no debería existir, pero lo dejo por flojo
		    	    		Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
		    	    		noFondo.setColor(new Color(0, 0, 0, 0));
		    	    		noFondo.fill();
	
		    	    		txtFondo = new Texture(noFondo);
		    	    		noFondo.dispose();
				}

                sprFondo = new Sprite(txtFondo);
            } catch (Exception noSong) {
                System.out.println("La cancion no existe, buu " + noSong.getMessage());
                return;
            }

            cancionActual.setLooping(true);
            cancionActual.play();
        }
        // Añadir canción
        else {
            Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
            noFondo.setColor(new Color(0.3984f, 0.13085f, 0.38475f, 1));
            noFondo.fill();

            txtFondo = new Texture(noFondo);
            sprFondo = new Sprite(txtFondo);
            noFondo.dispose();
        }

        // Configurar imagen de fondo
        // La imagen tiene un ratio menor o igual a 16:9
        if (txtFondo.getWidth() / txtFondo.getHeight() <= Coord.RATIO) {
            sprFondo.setScale((float) (Coord.RESOL_X + 10) / txtFondo.getWidth());
        }
        // La imagen tiene un ratio mayor a 16:9
        else {
            sprFondo.setScale((float) (Coord.RESOL_Y + 10) / txtFondo.getHeight());
        }
        // Centrar
        sprFondo.setPosition(
                (Coord.RESOL_X - sprFondo.getWidth()) / 2,
                (Coord.RESOL_Y - sprFondo.getHeight()) / 2);

        // Cargar en el juego el nuevo índice
        DibujadoGeneral.cargarFondoCancion(sprFondo);
        // Si está en la selección, recarga las portadas cargadas
        if (juego.getDibujado() instanceof DibujadoSeleccion) {
            DibujadoSeleccion sel = (DibujadoSeleccion) juego.getDibujado();

            sel.recargarTexturas();
        }

        if (i != -1) {
            indiceCancionAct = i;
        } else {
            indiceCancionAct = 0;
        }
    }

    public void dispose() {
        if (cancionActual != null) {
            cancionActual.dispose();
        }
        txtFondo.dispose();
    }

    static public boolean subirCancion(String newName, FileHandle song, FileHandle png){
        if (Objects.equals(newName, "") || song == null || png == null) {
            return false;
        }

        FileHandle newDir = dirCanciones.child(newName);
        if (newDir.isDirectory() || !song.exists() || !png.exists() || !song.extension().equals(".mp3") ||
        		!png.extension().equals(".png") || !png.extension().equals(".jpg") || !png.extension().equals(".jpeg")){
            return false;
        }
        newDir.mkdirs();
        String newNameSong = newName + "." + song.extension();
        String newNamePng = newName + "." + png.extension();

        FileHandle newSong = newDir.child(newNameSong);
        FileHandle newPng = newDir.child(newNamePng);
        song.moveTo(newSong);
        png.moveTo(newPng);
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();
        cargarListaCanciones();
        DibujadoSeleccion sel = (DibujadoSeleccion) juego.getDibujado();
        sel.recargarTexturas();
        
        return true;
    }
}
