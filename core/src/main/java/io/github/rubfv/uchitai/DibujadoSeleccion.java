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
    		private final int MAX_FRAMES_MOV = 40;
    		private float framesMov;
    		private Sprite sprFondoAnt;
    		private CancionesCargadas canciones;
    		
    		public Animacion(CancionesCargadas c) {
    			framesMov = 0;
    			sprFondoAnt = null;
    			canciones = c;
    		}
    		
    		public void animar() {
    			framesMov /= 1.125f;		//Mover
    			
    			//Detener animación
    			if (framesMov >= -1 && framesMov < 0 ||
    				framesMov > 0 && framesMov <= 1) {
    				framesMov = 0;
    				sprFondoAnt = null;
    			}
    		}
    		
    		public float relacionAnim() {
    			return (float) framesMov / MAX_FRAMES_MOV;
    		}
    		
    		public void animDeslizarDer() {
			framesMov += MAX_FRAMES_MOV;
			ajusteSprite();
    		}
    		
    		public void animDeslizarIzq() {
			framesMov -= MAX_FRAMES_MOV;
			ajusteSprite();
    		}
    		
    		public Sprite getSprAnt() {
    			return sprFondoAnt;
    		}
    		
    		private void ajusteSprite() {
    			Texture origen = txtPortadas[canciones.getIndiceCancion()];
			sprFondoAnt = new Sprite(origen);
    			
    			//Configurar imagen de fondo
    			//La imagen tiene un ratio menor o igual a 16:9
    			if (origen.getWidth() / origen.getHeight() <= Coord.RATIO) {
    				sprFondoAnt.setScale((float)Coord.RESOL_X / origen.getWidth());
    			}
    			//La imagen tiene un ratio mayor a 16:9
    			else {
    				sprFondoAnt.setScale((float)Coord.RESOL_Y / origen.getHeight());
    			}
    			//Centrar
    			sprFondoAnt.setPosition(
    			(Coord.RESOL_X - sprFondo.getWidth()) / 2, 
    			(Coord.RESOL_Y - sprFondo.getHeight()) / 2
    			);
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
    		anim = new Animacion(canciones);
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
		float relacionAnimAj = Math.abs(anim.relacionAnim()) > 1 ? 1 : Math.abs(anim.relacionAnim());
		
        ScreenUtils.clear(.10f, .09f, .11f, 1);
        frames = Gdx.graphics.getFrameId();
        mouse.x = Gdx.input.getX();
        mouse.y = Coord.RESOL_Y - Gdx.input.getY();

        animBrincos = (float)(frames % bmp) / bmp;
        
        /*------ Dibujado Fondo -----*/
        dibujadoPantalla.begin();

        if (anim.getSprAnt() != null) {
        		float anima = anim.relacionAnim() > 0 ? -1.0f: 1.0f;
    			anim.getSprAnt().setPosition(
    				(Coord.RESOL_X - anim.getSprAnt().getWidth()) / 2 + Coord.RESOL_X * (anima + anim.relacionAnim()), 
    				(Coord.RESOL_Y - anim.getSprAnt().getHeight()) / 2
    			);
            anim.getSprAnt().draw(dibujadoPantalla);
        }
        sprFondo.setPosition(
        		(Coord.RESOL_X - sprFondo.getWidth()) / 2 + Coord.RESOL_X * (anim.relacionAnim()), 
        		(Coord.RESOL_Y - sprFondo.getHeight()) / 2
        	);
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
        		int posBarraX = (i - MAX_PORTADAS / 2) * 350;
        		float posTextY = 0;
        		
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
        		
        		if (j == indice) {
        			posTextY = 75 - 75 * relacionAnimAj;
        		}
        		if (animacion > 0 && j == indice - 1) {
        			posTextY = 75 * relacionAnimAj;
        		}
        		else if (animacion < 0 && j == indice + 1) {
        			posTextY = 75 * relacionAnimAj;
        		}
        		
        		for (int k = 0; k < 4; k++) {
            		renderTexto.setText(
            			texto, canciones.nombreCancion(j),
            			new Color(0, 0, 0, 1f), Coord.RESOL_X,
            			Align.center, false
            		);
            		texto.draw(
            			dibujadoPantalla, renderTexto,
            			posBarraX + (k / 2) * 4 - 2 + animacion,
            			115 + (k % 2) * 4 + 2 + posTextY
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
        			posBarraX + animacion,
        			120 + posTextY
        		);
        		
        		dibujadoPantalla.end();
        }
        
        dibujadoPantalla.begin();

		//Botón Izquierda
		sprBotones.setRegion(0, 150, 150, 150);
		sprBotones.setSize(300,  300);
		sprBotones.setOrigin(150 + Coord.RESOL_X / 2, 150);
		sprBotones.setPosition(-Coord.RESOL_X / 4 + 150 + animacion * .25f, -45);
		sprBotones.setScale(0.5f + 0.015f * animBrincos);
		sprBotones.draw(dibujadoPantalla);

		//Botón Derecha
		sprBotones.setRegion(150, 150, 150, 150);
		sprBotones.setSize(300,  300);
		sprBotones.setOrigin(150 - Coord.RESOL_X / 2, 150);
		sprBotones.setPosition(Coord.RESOL_X + 150f / 4 + animacion * .25f, -45);
		sprBotones.setScale(0.5f + 0.015f * animBrincos);
		sprBotones.draw(dibujadoPantalla);
		
		//Botón inicio
		sprBotones.setRegion(0, 0, 150, 150);
		sprBotones.setSize(300,  300);
		sprBotones.setOrigin(150, 150);
		sprBotones.setPosition(Coord.RESOL_X / 2 - 150, 105 - 150);
		sprBotones.setScale(0.25f + 0.015f * animBrincos);
		sprBotones.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        anim.animar();
	}
}
