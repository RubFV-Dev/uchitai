package io.github.rubfv.uchitai;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

abstract public class DibujadoGeneral {
    protected static SpriteBatch dibujadoPantalla = new SpriteBatch();
    protected static ShapeRenderer figurasPantalla = new ShapeRenderer();
    protected static int bmp;
    protected static long frames;
    protected static Coord mouse = new Coord();
    protected static Sprite sprFondo;
    
    //Esta función se encargará de cargar a memoria los recursos necesarios para la pantalla, optimizado
    public void cargar(DibujadoGeneral viejo) {}
    //Descarga los recursos necesarios para la pantalla, se optimiza btw
    public void descargar(DibujadoGeneral nuevo) {}
    //Dibujado de la pantalla, se llama dentro de render() y se llama 60 veces por segundo
    public void dibujar() {}
    
    //Descargar recursos generales de la pantalla
    public static void dispose() {
    		dibujadoPantalla.dispose();
    		figurasPantalla.dispose();
    }
    
    //Fondo coqueto que todos comparten
    public static void cargarFondoCancion(Sprite fondo) {
    		sprFondo = fondo;
    }
}
