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
    protected Texture txtTitulo, txtKanji;
    protected Sprite sprTitulo, sprKanji;
    protected BitmapFont texto;
    
    DibujadoTitulo(DibujadoGeneral dib) {
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
		// 		Si la nueva pantalla es selección, no se borran
		//	los elementos ya cargados, se van a utilizar.
		if (!(nuevo instanceof DibujadoSeleccion)) {
	        txtTitulo.dispose();
	        txtKanji.dispose();
	        texto.dispose();
		}
    }
    
    @Override
    public void dibujar() {
		GlyphLayout renderTexto = new GlyphLayout();
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
        		0, (Coord.RESOL_Y - 500) / 2,
			Coord.RESOL_X, 70
		);
        
        figurasPantalla.end();
        
        /*------ DIBUJADO DE SPRITES -----*/
        dibujadoPantalla.begin();
        
        /*--- Dibujar título ---*/
        //Animación brinquitos
        sprTitulo.setScale(0.3f + 0.03f * animBrincos);
        sprKanji.setScale(0.3f + 0.005f * animBrincos);

        //Movimiento relativo al ratón
        sprTitulo.setPosition((Coord.RESOL_X - sprTitulo.getWidth()) / 2f, (Coord.RESOL_Y - sprTitulo.getHeight()) / 2f);
        sprTitulo.setX(sprTitulo.getX() + (Coord.RESOL_X / 2 - mouse.x) * 0.025f);
        sprTitulo.setY(sprTitulo.getY() + (Coord.RESOL_Y / 2 - mouse.y) * 0.025f);

        sprKanji.setPosition((Coord.RESOL_X - sprKanji.getWidth()) / 2f, (Coord.RESOL_Y - sprKanji.getHeight()) / 2f);
        sprKanji.setX(sprKanji.getX() + (Coord.RESOL_X / 2 - mouse.x) * 0.0125f);
        sprKanji.setY(sprKanji.getY() + (Coord.RESOL_Y / 2 - mouse.y) * 0.0125f);
		
        //Dibujado de cada elemento
        sprTitulo.draw(dibujadoPantalla);
        sprKanji.draw(dibujadoPantalla);
        
        /*--- Texto inicio de juego ---*/
        texto.getData().scaleX = 0.8f + animBrincos * .0125f;
        texto.getData().scaleY = 1f + animBrincos * .0125f;
        
        //Texto de sombra
		renderTexto.setText(
			texto, "Presiona una tecla para iniciar",
			new Color(0, 0, 0, 0.45f), 500,
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
			new Color(1, 0.9f, 0.95f, 1), 500,
			Align.center, true
		);
		texto.draw(
			dibujadoPantalla, renderTexto,
			(Coord.RESOL_X - renderTexto.width / texto.getScaleX()) / 2,
			(Coord.RESOL_Y  + renderTexto.height - sprTitulo.getHeight() / 3) / 2
		);

		dibujadoPantalla.end();
    }
}
