package io.github.rubfv.uchitai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Align;

public class DibujadoEdicion extends DibujadoGeneral {
	protected final float TAM_TECLA = 0.6f;
	
    protected Texture txtHudTeclas, txtBarras, txtFondoCombo;
    protected Sprite sprHudTeclas, sprFondoTeclas, sprBarras, sprFondoCombo;
    protected BitmapFont texto;
    protected CancionesCargadas canciones;
    
    protected Sprite portadaAct;
    
    DibujadoEdicion (DibujadoGeneral dib) {
    		this.canciones = canciones;
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
		Gestor gestor = juego.getGestorPartida();
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
            
        figurasPantalla.end();
        
        //Desactiva el blend
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        //Dibujado fondo teclas en QWERTY
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
	        	for (int x = 0; x < limX; x++) {
	        		boolean presionado = input.teclaPresionada(Nota.getKeyboardKeycode(x, y) - Input.Keys.A);
	        		int posX = 0, posY = 0;

	        		//Color lindo
	        		if (presionado) {
	        			sprFondoTeclas.setColor(Color.WHITE);
	        			sprHudTeclas.setColor(Color.WHITE);
	        			sprFondoTeclas.setScale(TAM_TECLA * 1.1f);
	        			sprHudTeclas.setScale(TAM_TECLA * 1.1f);
	        		}
	        		//No se presiona
	        		else {
	        			sprFondoTeclas.setColor(new Color(.5f, .5f, .5f, 0.35f));
	        			sprHudTeclas.setColor(new Color(.5f, .5f, .5f, 0.35f));
	        			sprFondoTeclas.setScale(TAM_TECLA);
	        			sprHudTeclas.setScale(TAM_TECLA);
	        		}
	        		
	        		sprFondoTeclas.setPosition(espacio.x, espacio.y);
	        		sprFondoTeclas.draw(dibujadoPantalla);

	        		posX = Nota.getKeyboardKeycode(x, y) + 36;
	        		posX -= 'A';
	        		
	        		//Obtiene la posición de la eltra dentro de la imagen
	        		while (posX >= 10) {
	        			posX -= 10;
	        			posY++;
	        		}
	        		
	        		//Mostrar letra de la tecla
	        		sprHudTeclas.setRegion(posX * 200, posY * 200, 200, 200);
	        		sprHudTeclas.setSize(200,  200);
	        		sprHudTeclas.setOrigin(100, 100);
	        		sprHudTeclas.setPosition(espacio.x, espacio.y);
	        		sprHudTeclas.setScale(TAM_TECLA);
	        		sprHudTeclas.draw(dibujadoPantalla);
	        		
	        		espacio.x += 230 * TAM_TECLA;
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
        
        //Iniciar barra de vida sin usar porque aquí no hay errores, solo felicidad :D
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
        sprBarras.setColor(new Color(.2941f, .7843f, 1f, 1));
        sprBarras.setScale(0.5f, 0.125f);
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
        //Esquinita barra vida Sin usar porque este es el editor
        sprBarras.setColor(new Color(.2941f, .7843f, 1f, 1));
        sprBarras.setScale(0.125f, 0.125f);
        sprBarras.setPosition(335 + 640, Coord.RESOL_Y - 350);
        sprBarras.draw(dibujadoPantalla);
        
        //Fondo circular de combo
        sprFondoCombo.setPosition(85, Coord.RESOL_Y - 320);
        sprFondoCombo.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        sprFondoCombo.setRotation((frames / 4) % 360);
        sprFondoCombo.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
	}
}
