package io.github.rubfv.uchitai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.rubfv.uchitai.DibujadoSeleccion.Animacion;
import io.github.rubfv.uchitai.DibujadoSeleccion.Transicion;

public class DibujadoJuego extends DibujadoGeneral {
	protected final int TAM_COLORES = 6;
	protected final float TAM_TECLA = 0.6f;
	protected final float TIEMPO_TRANS = .8f;
	
    protected Texture txtHudTeclas, txtBarras, txtFondoCombo;
    protected Sprite sprHudTeclas, sprFondoTeclas, sprBarras, sprFondoCombo;
    protected BitmapFont texto;
    protected CancionesCargadas canciones;
    
    protected Sprite portadaAct;
    
    protected Color[] coloresNotas;
    
    DibujadoJuego(DibujadoGeneral dib) {
    		this.canciones = canciones;
    		coloresNotas = new Color[TAM_COLORES];					//Color en RGB
    		coloresNotas[0] = new Color(.7695f, .2617f, .7695f, 1);	//200 -  66 - 200	/Rosa
    		coloresNotas[1] = new Color(.2823f, .4705f, .8784f, 1);	// 72 - 120 - 224	/Azul rey (?
    		coloresNotas[2] = new Color(.3019f, .8666f, .8650f, 1);	// 77 - 221 - 173	/Cyan
    		coloresNotas[3] = new Color(.4352f, .8666f, .2392f, 1);	//111 - 222 -  63	/Verde
    		coloresNotas[4] = new Color(.9372f, .8666f, .2392f, 1);	//239 - 221 -  61	/Amarillo
    		coloresNotas[5] = new Color(.9176f, .5098f, .2195f, 1);	//234 - 130 -  56	/Naranja
    		cargar(dib);
    }

	@Override
    public void cargar(DibujadoGeneral viejo) {
		//Carga las nuevas imágenes
		texto = new BitmapFont(Gdx.files.internal("Fuente/Ashkar_grande.fnt"));
		txtHudTeclas = new Texture(Gdx.files.internal("Hud/teclas_gameplay.png"));
		txtBarras = new Texture(Gdx.files.internal("Hud/hud_barras.png"));
		txtFondoCombo = new Texture(Gdx.files.internal("Hud/hud_fondo_combo.png"));
		
		sprHudTeclas = new Sprite(txtHudTeclas);
		sprFondoTeclas = new Sprite(txtHudTeclas, 1800, 400, 200, 200);
		sprBarras = new Sprite(txtBarras);
		sprFondoCombo = new Sprite(txtFondoCombo);
		
		sprFondoTeclas.setSize(200, 200);
		sprFondoTeclas.setScale(TAM_TECLA);
		sprFondoTeclas.setColor(new Color(.5f, .5f, .5f, 0.35f));
		
		sprHudTeclas.setColor(new Color(.5f, .5f, .5f, 0.35f));
		
		sprFondoCombo.setOrigin(150, 150);

        texto.getData().scaleX = TAM_TXT.x;
	}
	
	@Override
    public void descargar(DibujadoGeneral nuevo) {		
		//Limpieza de solo lo necesario
		txtHudTeclas.dispose();
		txtBarras.dispose();
		txtFondoCombo.dispose();

	    texto.dispose();
    	}
	
	@Override
    public void dibujar() {
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
		GestorJuego gestor = (GestorJuego)juego.getGestorPartida();
		InputGeneral input = (InputGeneral)Gdx.input.getInputProcessor();
		GlyphLayout renderTexto = new GlyphLayout();
		float animBrincos;
        frames = Gdx.graphics.getFrameId();

        animBrincos = (float)(frames % bmp) / bmp;
		
		dibujadoPantalla.begin();
        
		//Fondo de la canción
        sprFondo.setPosition(
        		(Coord.RESOL_X - sprFondo.getWidth()) / 2, 
        		(Coord.RESOL_Y - sprFondo.getHeight()) / 2
        	);
        sprFondo.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        	figurasPantalla.begin();

        figurasPantalla.set(ShapeType.Filled);
        figurasPantalla.setColor(new Color(0f, 0f, 0f, 0.65f));
        figurasPantalla.rect(0, 0, Coord.RESOL_X, Coord.RESOL_Y);
        
//        figurasPantalla.setColor(Color.BLACK);
//        figurasPantalla.rect(Coord.RESOL_X / 2 -1, 0, 2, Coord.RESOL_Y);
            
        figurasPantalla.end();
        
        //Desactiva el blend
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        /*---------- Dibujado fondo teclas en QWERTY ----------*/
        dibujadoPantalla.begin();
        for (int y = 0; y < 3; y++) {
        		Coord espacio = new Coord(
        			(Coord.RESOL_X - 200f * TAM_TECLA) / 2 - (1100f * TAM_TECLA),
        			Coord.RESOL_Y / 16 + 230f * TAM_TECLA * (2 - y)
        		);
        		//Ajuste para cantidad de teclas en teclado QWERTY
        		int limX = 0;
        		switch (y) {
        		case 0:	limX = 10;	break;
        		case 1:	limX = 9;	break;
        		case 2:	limX = 7;	break;
        		}
        		espacio.x += 66f * y;
        		
	        	//Primera fila de teclas
	        	for (int x = 0; x < limX; x++, espacio.x += 230 * TAM_TECLA) {
	        		boolean presionado = input.teclaPresionada(Nota.getKeyboardKeycode(x, y) - Input.Keys.A);
	        		int keycode = Nota.getKeyboardKeycode(x, y);
	        		int posX = 0, posY = 0;

	        		posX = keycode + 36;
	        		posX -= 'A';
	        		
	        		//Obtiene la posición de la eltra dentro de la imagen
	        		while (posX >= 10) {
	        			posX -= 10;
	        			posY++;
	        		}

	        		//Color lindo
	        		if (presionado) {
	        			sprFondoTeclas.setColor(Color.WHITE);
	        			sprHudTeclas.setColor(Color.WHITE);
	        			sprFondoTeclas.setScale(TAM_TECLA * 1.1f);
	        			sprHudTeclas.setScale(TAM_TECLA * 1.1f);
	        		}
	        		//No se presiona
	        		else {
	        			sprFondoTeclas.setColor(new Color(.5f, .5f, .5f, 0.2f));
	        			sprHudTeclas.setColor(new Color(.5f, .5f, .5f, 0.2f));
	        			sprFondoTeclas.setScale(TAM_TECLA);
	        			sprHudTeclas.setScale(TAM_TECLA);
	        		}
	        		
	        		sprFondoTeclas.setPosition(espacio.x, espacio.y);
	        		sprFondoTeclas.draw(dibujadoPantalla);
	        		
	        		//Mostrar letra de la tecla
	        		sprHudTeclas.setRegion(posX * 200, posY * 200, 200, 200);
	        		sprHudTeclas.setSize(200,  200);
	        		sprHudTeclas.setOrigin(100, 100);
	        		sprHudTeclas.setPosition(espacio.x, espacio.y);
	        		sprHudTeclas.setScale(TAM_TECLA);
	        		sprHudTeclas.draw(dibujadoPantalla);
	        	}
        }
        /*---------- DIBUJADO NOTAS A PRESIONAR EN PANTALLA ------------*/
        for (int y = 0; y < 3; y++) {
	    		Coord espacio = new Coord(
	    			(Coord.RESOL_X - 200f * TAM_TECLA) / 2 - (1100f * TAM_TECLA),
	    			Coord.RESOL_Y / 16 + 230f * TAM_TECLA * (2 - y)
	    		);
	    		//Ajuste para cantidad de teclas en teclado QWERTY
	    		int limX = 0;
	    		switch (y) {
	    		case 0:	limX = 10;	break;
	    		case 1:	limX = 9;	break;
	    		case 2:	limX = 7;	break;
	    		}
	    		espacio.x += 66f * y;
	    		
	        	//Primera fila de teclas
	        	for (int x = 0; x < limX; x++, espacio.x += 230 * TAM_TECLA) {
	        		int keycode = Nota.getKeyboardKeycode(x, y);
	        		int posX = 0, posY = 0;
	
	        		posX = keycode + 36;
	        		posX -= 'A';
	        		
	        		//Obtiene la posición de la eltra dentro de la imagen
	        		while (posX >= 10) {
	        			posX -= 10;
	        			posY++;
	        		}
	        		
	        		for (Nota n: gestor.getNotasActivas()) {
	        			if (n.getTecla() == keycode) {
	        				float relacionAp = n.getRelacionAparecer(gestor.getTiempoAct());
	        				float tamPresionado = relacionAp * TAM_TECLA * 1.8f;
	        				float trans = 1;
	        				Color c = new Color(coloresNotas[n.getId() % TAM_COLORES]);
	        				if (tamPresionado < 0) tamPresionado = 0;
	        				
	        				//Animación aparición
	        				if (relacionAp > TIEMPO_TRANS) {
	        					trans = 1f - (relacionAp - TIEMPO_TRANS) / (1f - TIEMPO_TRANS);
	        				}
	        				//Animación falla
	        				if (relacionAp < -0.1f) {
	        					float ajuste = -(relacionAp + 0.1f) / (Gestor.getMargenError() - .1f);
	        					c.lerp(Color.RED, ajuste);
	        					
	        					//Desaparecer tecla
	        					if (ajuste > .3f) {
	        						trans = 1f - (ajuste - 0.3f) / 0.7f;
	        						if (trans < 0) trans = 0;
	        					}
	        				}
	        				
		        			sprFondoTeclas.setColor(c);
        					sprFondoTeclas.setAlpha(trans);
		        			sprFondoTeclas.setScale(TAM_TECLA);
		    	        		sprFondoTeclas.setPosition(espacio.x, espacio.y);
		    	        		sprFondoTeclas.draw(dibujadoPantalla);
		    	        		//Mostrar letra de la tecla
		    	        		sprHudTeclas.setRegion(posX * 200, posY * 200, 200, 200);
		    	        		sprHudTeclas.setSize(200,  200);
		    	        		sprHudTeclas.setOrigin(100, 100);
		    	        		sprHudTeclas.setPosition(espacio.x, espacio.y);
		    	        		sprHudTeclas.setColor(c);
	        				sprHudTeclas.setAlpha(trans);
		    	        		sprHudTeclas.setScale(TAM_TECLA);
		    	        		sprHudTeclas.draw(dibujadoPantalla);
	
		    	        		sprHudTeclas.setRegion(1600, 400, 200, 200);
		    	        		sprHudTeclas.setSize(200,  200);
		    	        		sprHudTeclas.setOrigin(100, 100);
		    	        		sprHudTeclas.setPosition(espacio.x, espacio.y);
		    	        		sprHudTeclas.setColor(c);
	        				sprHudTeclas.setAlpha(trans * .65f);
		    	        		sprHudTeclas.setScale(TAM_TECLA + tamPresionado);
		    	        		sprHudTeclas.draw(dibujadoPantalla);
	        			}
	        		}
	        	}
        }

        //------ Fondo combo juegongo ------
        //Datos iniciales de Barras
        
        //Barra tiempo de juego
        sprBarras.setRegion(0, 0, 1280, 460);
        sprBarras.setSize(Coord.RESOL_Y, 460);
        sprBarras.setOrigin(0, 230);
        sprBarras.setRotation(90);
        
        //Fondo tiempo nivelelel
        sprBarras.setScale(1f, 0.125f);
        sprBarras.setPosition(Coord.RESOL_X - 33.75f, -230);
        sprBarras.setColor(Color.BLACK);
        sprBarras.draw(dibujadoPantalla);
        //Barra Tiempo nivel
        sprBarras.setScale(1f * gestor.getRelacionTiempo(), 0.125f);
        sprBarras.setPosition(Coord.RESOL_X - 28.75f, -230);
        sprBarras.setColor(Color.WHITE);
        sprBarras.draw(dibujadoPantalla);
        
        //Iniciar barra de vida
        sprBarras.setRegion(0, 0, 1280, 460);
        sprBarras.setSize(1280, 460);
        sprBarras.setOrigin(0, 230);
        sprBarras.setRotation(0);
        
        //Fondo oscuro de vida
        sprBarras.setColor(Color.BLACK);
        sprBarras.setScale(0.5f, 0.145f);
        sprBarras.setPosition(335, Coord.RESOL_Y - 350);
        sprBarras.draw(dibujadoPantalla);
        //Barra vida ahora sí
        sprBarras.setColor(new Color(.7968f, .2617f, .7695f, 1));
        sprBarras.setScale(0.5f * (1f - ((GestorJuego)gestor).getRelacionVida()), 0.125f);
        sprBarras.draw(dibujadoPantalla);
        
        //Datos iniciales de Esquinita de vida
        sprBarras.setRegion(0, 460, 330, 460);
        sprBarras.setSize(460, 460);
        sprBarras.setOrigin(0, 230);
        sprBarras.setFlip(true, false);

        //Fondo Oscuro de esquinita de vida
        sprBarras.setColor(Color.BLACK);
        sprBarras.setScale(0.145f, 0.145f);
        sprBarras.setPosition(335 + 640, Coord.RESOL_Y - 350);
        sprBarras.draw(dibujadoPantalla);
        //Esquinita barra vida
        sprBarras.setColor(new Color(.7968f, .2617f, .7695f, 1));
        sprBarras.setScale(0.125f, 0.125f);
        sprBarras.setPosition(335 + 640 * (1f - ((GestorJuego)gestor).getRelacionVida()), Coord.RESOL_Y - 350);
        sprBarras.draw(dibujadoPantalla);
        
        //Fondo circular de combo
        sprFondoCombo.setPosition(85, Coord.RESOL_Y - 320);
        sprFondoCombo.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        sprFondoCombo.setRotation((frames / 4) % 360);
        sprFondoCombo.draw(dibujadoPantalla);
        
        //texto combo juego
        //Animación brincos ritmo
        texto.getData().scaleY = TAM_TXT.y;
        texto.getData().scaleX = TAM_TXT.x;
        renderTexto.setText(
    			texto, ""+ gestor.getCombo(),
    			new Color(0, 0, 0, 1f), 300,
    			Align.center, false
        	);
			texto.draw(
    			dibujadoPantalla, renderTexto,
    			85, Coord.RESOL_Y - 135
    		);

        //Dibujar sombra texto puntaje
        texto.getData().scaleY = TAM_TXT.y * .7f;
        texto.getData().scaleX = TAM_TXT.x * .7f;
        renderTexto.setText(
    			texto, ""+ gestor.getPuntaje(),
    			new Color(0, 0, 0, .35f), 500,
    			Align.left, false
        	);
			texto.draw(
    			dibujadoPantalla, renderTexto,
    			26, 66
    		);
        //Dibujar borde texto puntaje
		for (int k = 0; k < 4; k++) {
	        renderTexto.setText(
	    			texto, ""+ gestor.getPuntaje(),
	    			Color.WHITE, 500,
	    			Align.left, false
	        	);
	    		texto.draw(
	    			dibujadoPantalla, renderTexto,
	    			(k / 2) * 6 * texto.getData().scaleX + 20 - 3 *texto.getData().scaleX,
	    			(k % 2) * 6 * texto.getData().scaleY + 72 - 3 * texto.getData().scaleY
	    		);
		}
		//Dibujar texto puntaje
        renderTexto.setText(
    			texto, ""+ gestor.getPuntaje(),
    			new Color(0, 0, 0, 1f), 500,
    			Align.left, false
        	);
			texto.draw(
    			dibujadoPantalla, renderTexto,
    			20, 72
    		);
        
        dibujadoPantalla.end();
	}
}