package io.github.rubfv.uchitai;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.audio.Music;

public abstract class Gestor {
    public static final float MARGEN_ERROR = 0.3f;      //los milisegundos que estan permitidos para desfasarse del presionar
	public static final float TIEMPO_APARICION = 1.5f;
    
    protected Nivel nivelAct;             //como tal all the level
    protected String nombreCancion;
    protected String rutaCancion;
    
    protected Music musicaActual;
    protected float tiempoAct, aparicionNotas;      		//Este es nuestro reloj interno
    protected boolean juegoEstado;          	//pa saber si esta en pausa o no (pendiente de confirmar [Sí, Aranza, nos sirve: att Rodrigo])
    
    Gestor(Nivel level, CancionesCargadas canciones) {
        nivelAct = level;							//pasarle la estructura ya extraida del archivo
        tiempoAct = 0;
        aparicionNotas = TIEMPO_APARICION;
        
        juegoEstado = true;
        musicaActual = canciones.getCancionActual();
        nombreCancion = canciones.nombreCancion(canciones.getIndiceCancion());
        rutaCancion = canciones.rutaCancion(canciones.getIndiceCancion());
    }
    
    public void procesarTeclaPresionada(int keycode) { /**/ }
    
    public boolean actualizar() { return false; }

    //Estos tres van con el tema de la música
    public void iniciar() {
        if (musicaActual != null) {
            tiempoAct = 0;
            musicaActual.stop();
            musicaActual.play();
            musicaActual.setVolume(1);
            juegoEstado = true;
        }
    }
    
    //en GestorJuego puede servir para guardar la puntuación
    //en GestorEdicion sirve para guardar el mapa
    public boolean guardar() {
    		return false;
    }
    
    public float getTiempoAct() {
    		return tiempoAct;
    }

    public float getRelacionTiempo() {
    		return (float)musicaActual.getPosition();
    }
    
    public static float getMargenError() {
    		return MARGEN_ERROR;
    }
}
