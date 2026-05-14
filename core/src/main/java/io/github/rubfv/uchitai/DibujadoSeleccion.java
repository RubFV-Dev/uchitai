package io.github.rubfv.uchitai;

import java.util.ArrayList;
import java.util.logging.FileHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class DibujadoSeleccion extends DibujadoGeneral {
    public class Animacion{
    		private final int MAX_FRAMES_MOV = 60;
    		private float framesMov;
    		
    		public Animacion() {
    			framesMov = 0;
    		}
    		
    		public void animar() {
    			framesMov /= 1.125f;
    			
    			if (framesMov >= -1 && framesMov < 0 ||
    				framesMov > 0 && framesMov <= 1) {
    				framesMov = 0;
    			}
    		}
    		
    		public float relacionAnim() {
    			return (float) framesMov / MAX_FRAMES_MOV;
    		}
    		
    		public void animDeslizarDer() {
			framesMov += MAX_FRAMES_MOV;
    		}
    		
    		public void animDeslizarIzq() {
			framesMov -= MAX_FRAMES_MOV;
    		}
    };
    
    protected Texture txtTitulo, txtKanji, txtShaderFondo, txtBotones;
    protected Sprite sprTitulo, sprKanji, sprBotones;
    protected BitmapFont texto;
    protected CancionesCargadas canciones;
    protected ShaderProgram shaderFondo;
    
    protected Texture[] txtPortadas;
    protected Sprite[] sprPortadas;
    protected Sprite portadaAct;
    protected final int MAX_PORTADAS = 9;		//Cantidad de portadas cargadas en memoria, debe ser impar
    
    public Animacion anim;
    
    DibujadoSeleccion(DibujadoGeneral dib, CancionesCargadas canciones) {
    		this.canciones = canciones;
    		txtPortadas = new Texture[canciones.size()];
    		sprPortadas = new Sprite[canciones.size()];
    		anim = new Animacion();
    		cargar(dib);
    }

	@Override
    public void cargar(DibujadoGeneral viejo) {
		//Carga las nuevas imágenes
		if (!(viejo instanceof DibujadoTitulo)) {
			txtTitulo = new Texture("hud/titulo.png");
			txtKanji = new Texture("hud/kanji.png");
	    		texto = new BitmapFont(Gdx.files.internal("Fuente/Ashkar.fnt"));
		}
		//Copia las imágenes ya existentes
		else {
			DibujadoTitulo titulo = (DibujadoTitulo)viejo;
			
			txtTitulo = titulo.txtTitulo;
			txtKanji = titulo.txtKanji;
			texto = titulo.texto;
		}
		txtBotones = new Texture("hud/botones_seleccion.png");
    		shaderFondo = new ShaderProgram(
    			Gdx.files.internal("Hud/default.vert"),
    			Gdx.files.internal("Hud/mascara.frag")
    		);
		txtShaderFondo = new Texture("Hud/mascara_fondo.png");
		
		sprTitulo = new Sprite(txtTitulo);
		sprKanji = new Sprite(txtKanji);
		sprBotones = new Sprite(txtBotones);
		
	    sprTitulo.setScale(0.25f);
	    sprKanji.setScale(0.25f);
			
		sprTitulo.setX(-sprTitulo.getWidth() * sprTitulo.getScaleX() * 2 / 3);
	    sprTitulo.setY((Coord.RESOL_Y - sprTitulo.getHeight()) / 3f);
	    sprKanji.setX(-sprKanji.getWidth() * sprKanji.getScaleX() * 2 / 3);
	    sprKanji.setY((Coord.RESOL_Y - sprKanji.getHeight()) / 3f);
	    
		recargarTexturas();
	}
	
	public void recargarTexturas() {
		int l = canciones.getIndiceCancion();
		//Limpieza original
		for (int i = 0; i < canciones.size(); i++) {
			//Descargar solo las texturas necesarias según el nuevo índice
			if (i <= l - MAX_PORTADAS / 2 && i >= l - MAX_PORTADAS / 2) {
				if (txtPortadas[i] != null) {
					txtPortadas[i].dispose();
					txtPortadas[i] = null;
				}
				if (sprPortadas[i] != null) {
					sprPortadas[i] = null;
				}
			}
		}
		
		for (int i = 0, j = canciones.getIndiceCancion() - MAX_PORTADAS / 2; i < MAX_PORTADAS; i++, j++) {
			if (j >= 0 && j < canciones.size() && txtPortadas[j] == null) {
				Coord escala = new Coord(1, 1);
				Coord escSprite = new Coord(1, 1);
				Texture textura;
				Sprite sprite;
				String ruta = canciones.rutaCancion(j) + "/" + canciones.nombreCancion(j) + ".png";
				System.out.println(ruta);

	        		try {
	        		    textura = new Texture(Gdx.files.internal(ruta));
		    	    }
		    	    catch (Exception noFoto) {
		    	    		// Esto no debería existir, pero lo dejo por flojo
		    	    		Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
		    	    		noFondo.setColor(new Color(0, 0, 0, 0));
		    	    		noFondo.fill();
		    	    		
		    	    		textura = new Texture(noFondo);
		    	    		noFondo.dispose();
		    	    }
	        		
	        		txtPortadas[j] = (textura);
	        		//Imagen reescalada para caber en el selector
	        		escala.x = 430f / textura.getWidth();
	        		escala.y = 150f / textura.getHeight();
	        		sprite = new Sprite(textura);
	        		//Resol 2:1
	        		sprite.setRegion(0, textura.getHeight() / 4, textura.getWidth(), textura.getHeight() / 2);
	        		sprite.setScale(escala.x, escala.y);
	        		sprPortadas[j] = (sprite);
			}
		}
		
		System.out.println();
	}
	
	@Override
    public void descargar(DibujadoGeneral nuevo) {		
		//Limpieza de solo lo necesario
    		if (!(nuevo instanceof DibujadoTitulo)) {
	        txtTitulo.dispose();
	        txtKanji.dispose();
	        texto.dispose();
		}

    		//Limpieza de los arreglos de las portadas
    		for (int i = 0; i < canciones.size(); i++) {
    			if (txtPortadas[i] != null) {
    				txtPortadas[i].dispose();
    				txtPortadas[i] = null;
    			}
    			if (sprPortadas[i] != null) {
    				sprPortadas[i] = null;
    			}
    		}
    		
    		txtBotones.dispose();
    		txtShaderFondo.dispose();
    		shaderFondo.dispose();
    	}
	
	@Override
    public void dibujar() {
		GlyphLayout renderTexto = new GlyphLayout();
		int indice = canciones.getIndiceCancion();
		float animacion = 350 * anim.relacionAnim();
		float animBrincos;
		
        ScreenUtils.clear(.10f, .09f, .11f, 1);
        frames = Gdx.graphics.getFrameId();
        mouse.x = Gdx.input.getX();
        mouse.y = Coord.RESOL_Y - Gdx.input.getY();

        animBrincos = (float)(frames % bmp) / bmp;
        
        /*------ Dibujado Fondo -----*/
        dibujadoPantalla.begin();
        
        sprFondo.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        /*------ DIBUJADO DE FIGURAS -----*/
        figurasPantalla.begin();

        //Fondo título
        figurasPantalla.set(ShapeType.Filled);
        figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
        figurasPantalla.rect(
        		0, 30,
			Coord.RESOL_X, 150
		);
        
        figurasPantalla.end();
        
        /*------ DIBUJADO DE SPRITES -----*/
        dibujadoPantalla.begin();
        
        /*--- Dibujar título ---*/
        //Animación brinquitos
        sprTitulo.setScale(0.25f + 0.015f * animBrincos);
        sprKanji.setScale(0.25f + 0.0025f * animBrincos);
		
        //Dibujado de cada elemento
        sprTitulo.draw(dibujadoPantalla);
        sprKanji.draw(dibujadoPantalla);
        
        //Dibujado canciones visibles
        dibujadoPantalla.end();
        for (int i = 0, j = canciones.getIndiceCancion() - MAX_PORTADAS / 2; i < MAX_PORTADAS; i++, j++) {
        		if (j >= canciones.size() || j < 0) continue;  
        		
        		//Dibujar fondo canción
        		//Activa el shader del fondo con los triangulitos
        		dibujadoPantalla.setShader(shaderFondo);
        		dibujadoPantalla.begin();
        		
        		//Tecnisismos de OpenGL para shaders
        		txtShaderFondo.bind(1);
        		shaderFondo.setUniformi("u_mask", 1);
        		txtPortadas[j].bind(0);
        		
        		//Dibujado del fondo
    			sprPortadas[j].setCenter((i - 1) * 350 - 87.5f + animacion, 105);
    			sprPortadas[j].draw(dibujadoPantalla);
    			
    			//Quitar el shader porque si no todo vale cola
    			dibujadoPantalla.flush();
    			dibujadoPantalla.end();
        		dibujadoPantalla.setShader(null);

            //Texto de sombra
        		dibujadoPantalla.begin();
        		for (int k = 0; k < 4; k++) {
            		renderTexto.setText(
            			texto, canciones.nombreCancion(j),
            			new Color(0, 0, 0, 1f), Coord.RESOL_X,
            			Align.center, false
            		);
            		texto.draw(
            			dibujadoPantalla, renderTexto,
            			(i - MAX_PORTADAS / 2) * 350 + (k / 2) * 4 - 2 + animacion,
            			115 + (k % 2) * 4 + 2
            		);
        		}
        		
        		//Texto real
        		renderTexto.setText(
        			texto, canciones.nombreCancion(j),
        			new Color(1, 0.9f, 0.95f, 1), Coord.RESOL_X,
        			Align.center, false
        		);
        		texto.draw(
        			dibujadoPantalla, renderTexto,
        			(i - MAX_PORTADAS / 2) * 350 + animacion,
        			120
        		);
        		
        		dibujadoPantalla.end();
        }
        
        dibujadoPantalla.begin();

		//Botón Izquierda
		sprBotones.setRegion(0, 150, 150, 150);
		sprBotones.setOrigin(150 + Coord.RESOL_X / 2, 150);
		sprBotones.setPosition(-Coord.RESOL_X / 4 + 150 + animacion * .25f, -45);
		sprBotones.setScale(0.5f + 0.015f * animBrincos);
		
		sprBotones.draw(dibujadoPantalla);

		//Botón Derecha
		sprBotones.setRegion(150, 150, 150, 150);
		sprBotones.setOrigin(150 - Coord.RESOL_X / 2, 150);
		sprBotones.setPosition(Coord.RESOL_X + 150f / 4 + animacion * .25f, -45);
		sprBotones.setScale(0.5f + 0.015f * animBrincos);
		sprBotones.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        anim.animar();
	}
}
