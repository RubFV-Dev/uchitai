package io.github.rubfv.uchitai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class DibujadoTitulo extends DibujadoGeneral {
	protected class Transicion {
		private static final int MAX_ANIM_TRANS = 40;
		private float animTrans;
		private boolean entrada;
		
		Transicion() {
			animTrans = -1;
			entrada = false;
		}

		Transicion(boolean entrada) {
			animTrans = -1;
			this.entrada = entrada;
		}
		
		public void animar() {
			if (animTrans == -1) {
				animTrans = MAX_ANIM_TRANS;
			}
			else {
				animTrans /= 1.125;		//Mover
				
				//Detener animación
				if (animTrans <= 1) {
					animTrans = 0;
				}
			}
		}
		
		public float relacionAnim() {
			if (animTrans == -1) {
				return 0;
			}
			return (float) (MAX_ANIM_TRANS - animTrans) / MAX_ANIM_TRANS;
		}
		
		public boolean completado() {
			return animTrans == 0;
		}
	}
	
    protected Texture txtTitulo, txtKanji;
    protected Sprite sprTitulo, sprKanji;
    protected BitmapFont texto;
    protected Transicion trans;
    
    DibujadoTitulo(DibujadoGeneral dib) {
    		trans = new Transicion();
    		cargar(dib);
    }
    
    @Override
    public void cargar(DibujadoGeneral viejo) {
		//Carga las nuevas imágenes
	    	if (!(viejo instanceof DibujadoSeleccion)) {
	    		txtTitulo = new Texture("hud/titulo.png");
	    		txtKanji = new Texture("hud/kanji.png");
	        	texto = new BitmapFont(Gdx.files.internal("Fuente/Ashkar.fnt"));
	    	}
	    	//Copia las imágenes existentes
	    	else {
	    		DibujadoSeleccion seleccion = (DibujadoSeleccion)viejo;
	    		
	    		txtTitulo = seleccion.txtTitulo;
	    		txtKanji = seleccion.txtKanji;
	    		texto = seleccion.texto;
	    	}
	    	
        	sprTitulo = new Sprite(txtTitulo);
        	sprKanji = new Sprite(txtKanji);
			
		//Inicializar datos wtf
		sprTitulo.setScale(0.3f);
		sprKanji.setScale(0.3f);
		texto.getData().scaleX = 0.8f;
			
	    figurasPantalla.setAutoShapeType(true);
			
		bmp = 110 / 6;
    }
    
    @Override
    public void descargar(DibujadoGeneral nuevo) {
		// 	  Si la nueva pantalla es selección, no se borran
		//	los elementos ya cargados, se van a utilizar.
		if (!(nuevo instanceof DibujadoSeleccion)) {
	        txtTitulo.dispose();
	        txtKanji.dispose();
		}
        texto.dispose();
    }
    
    @Override
    public void dibujar() {
		GlyphLayout renderTexto = new GlyphLayout();
		float animBrincos;
		float animTransicion = trans.relacionAnim();
		Coord posTitulo = new Coord(), posKanji = new Coord();
		
        ScreenUtils.clear(.10f, .09f, .11f, 1);
        frames = Gdx.graphics.getFrameId();

        animBrincos = (float)(frames % bmp) / bmp;
        
        /*------ Dibujado Fondo -----*/
        dibujadoPantalla.begin();
        
        sprFondo.setPosition(
        		(Coord.RESOL_X - sprFondo.getWidth()) / 2+ mouse.x / Coord.RESOL_X * 10 - 5, 
        		(Coord.RESOL_Y - sprFondo.getHeight()) / 2  + mouse.y / Coord.RESOL_Y * 10 - 5
        	);
        sprFondo.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        /*------ DIBUJADO DE FIGURAS -----*/
        figurasPantalla.begin();

        //Fondo título
        figurasPantalla.set(ShapeType.Filled);
        figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
        
        if (animTransicion == 0) {
        		figurasPantalla.rect(
        			0, 290,
        			Coord.RESOL_X, 70
    			);
        }
        else {
        		figurasPantalla.rect(
        			0, 290 - animTransicion * 260,
        			Coord.RESOL_X, 70 + animTransicion * 80
    			);
        	}
        
        figurasPantalla.end();
        
        /*------ DIBUJADO DE SPRITES -----*/
        dibujadoPantalla.begin();
        
        /*--- Dibujar título ---*/
        //Animación brinquitos
        sprTitulo.setScale(0.3f + 0.03f * animBrincos);
        sprKanji.setScale(0.3f + 0.005f * animBrincos);

        //Movimiento relativo al ratón
        posTitulo.x = (Coord.RESOL_X - sprTitulo.getWidth()) / 2f;
        posTitulo.y = (Coord.RESOL_Y - sprTitulo.getHeight()) / 2f;
        posTitulo.x += (Coord.RESOL_X / 2 - mouse.x) * 0.025f;
        posTitulo.y += (Coord.RESOL_Y / 2 - mouse.y) * 0.025f;
        
        //Kanji
        posKanji.x = (Coord.RESOL_X - sprKanji.getWidth()) / 2f;
        posKanji.y = (Coord.RESOL_Y - sprKanji.getHeight()) / 2f;
        posKanji.x += (Coord.RESOL_X / 2 - mouse.x) * 0.0125f;
        posKanji.y += (Coord.RESOL_Y / 2 - mouse.y) * 0.0125f;

        //Animar la transición a pantalla de selección
        if (animTransicion != 0) {
        		Coord posTituloEx = new Coord(), posKanjiEx = new Coord();
        		//Cambio de tamaño
            sprTitulo.setScale(
            		(0.3f + 0.03f * animBrincos) * (1 - animTransicion) + 
            		(0.25f + 0.015f * animBrincos) * animTransicion
            	);
            sprKanji.setScale(
            		(0.3f + 0.005f * animBrincos) * (1 - animTransicion) + 
            		(0.25f + 0.0025f * animBrincos) * animTransicion
            	);
	    
            //Cambio de posición; extra
            posTituloEx.x = -sprTitulo.getWidth() * sprTitulo.getScaleX() * 2 / 3;
            posTituloEx.y = (Coord.RESOL_Y - sprTitulo.getHeight()) / 3f;
            posKanjiEx.x = -sprKanji.getWidth() * sprKanji.getScaleX() * 2 / 3;
            posKanjiEx.y = (Coord.RESOL_Y - sprKanji.getHeight()) / 3f;
            
            //Ajuste de multiplicador para que se vea fifiris nais
            posTitulo.multipleCociente(1f - animTransicion);
            posKanji.multipleCociente(1f - animTransicion);
            posTituloEx.multipleCociente(animTransicion);
            posKanjiEx.multipleCociente(animTransicion);

            //Dibujado
            sprTitulo.setPosition(posTitulo.x + posTituloEx.x, posTitulo.y + posTituloEx.y);
            sprKanji.setPosition(posKanji.x + posKanjiEx.x, posKanji.y + posKanjiEx.y);
        }
        else {
            sprTitulo.setPosition(posTitulo.x, posTitulo.y);
            sprKanji.setPosition(posKanji.x, posKanji.y);
        }
        
        //Dibujado de cada elemento
        sprTitulo.draw(dibujadoPantalla);
        sprKanji.draw(dibujadoPantalla);
        
        /*--- Texto inicio de juego ---*/
        if (animTransicion == 0) {
            texto.getData().scaleX = 0.8f + animBrincos * .0125f;
            texto.getData().scaleY = 1f + animBrincos * .0125f;
        }
        else {
            texto.getData().scaleX = 0.8f + animBrincos * .0125f;
            texto.getData().scaleX *= (1.0 - animTransicion);
            texto.getData().scaleY = 1f + animBrincos * .0125f;
            texto.getData().scaleY *= (1.0 - animTransicion);
        }
        
        //Texto de sombra
		renderTexto.setText(
			texto, "Presiona una tecla para iniciar",
			new Color(0, 0, 0, 0.45f * (1.0f - animTransicion)), 500,
			Align.center, true
		);
		texto.draw(
			dibujadoPantalla, renderTexto,
			(Coord.RESOL_X - renderTexto.width / texto.getScaleX()) / 2 + 5,
			(Coord.RESOL_Y  + renderTexto.height - sprTitulo.getHeight() / 3) / 2 - 5
		);
		
		//Texto real
		renderTexto.setText(
			texto, "Presiona una tecla para iniciar",
			new Color(1, 0.9f, 0.95f, (1.0f - animTransicion)), 500,
			Align.center, true
		);
		texto.draw(
			dibujadoPantalla, renderTexto,
			(Coord.RESOL_X - renderTexto.width / texto.getScaleX()) / 2,
			(Coord.RESOL_Y + renderTexto.height - sprTitulo.getHeight() / 3) / 2
		);

		dibujadoPantalla.end();
    }
    
    @Override
    public boolean transCompletada() {
    		trans.animar();
		return trans.completado();
    }
    
    @Override
    public boolean estaBloqueado() {
    		return trans.relacionAnim() != 0;
    }
}
