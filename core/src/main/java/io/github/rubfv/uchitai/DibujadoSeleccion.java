package io.github.rubfv.uchitai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
	protected class Transicion {
		private final int MAX_ANIM_TRANS = 25;
		private final int MAX_TIEMPO_EX = 120;
		private final float FIN_TRANSICION = 0.0025f;
		private final float VEL_TRANSICION = 1.21875f;
		private float animTrans;
		private int tiempoEx;
		private boolean entrada;
		
		Transicion() {
			animTrans = -1;
			tiempoEx = -1;
			entrada = false;
		}
	
		Transicion(boolean entrada) {
			this.entrada = entrada;
			tiempoEx = -1;
			if (entrada) {
				animTrans = MAX_ANIM_TRANS;
			}
			else {
				animTrans = -1;
			}
		}
		
		public void animar() {
			if (tiempoEx > 0) {
				tiempoEx--;
			}
			if (animTrans == -1) {
				animTrans = MAX_ANIM_TRANS;
			}
			else if (tiempoEx == -1) {
				animTrans /= VEL_TRANSICION;		//Mover
				
				//Detener animación
				if (animTrans <= FIN_TRANSICION) {
					animTrans = 0;
					
					if (!entrada) {						
						tiempoEx = MAX_TIEMPO_EX;
						animTrans = 0;
					}
					
					if (entrada) {
						entrada = false;
						animTrans = -1;
					}
				}
			}
		}
	
		public float relacionAnim() {
			if (animTrans == -1) {
				return 0;
			}
			return (float) (MAX_ANIM_TRANS - animTrans) / MAX_ANIM_TRANS;
		}
		
		public float relacionAnimEspera(int rangoSup, int rangoInf) {
			if (tiempoEx == -1 || tiempoEx > MAX_TIEMPO_EX - rangoInf) return 0;
			if (tiempoEx < MAX_TIEMPO_EX - rangoSup) return 1;
			return (float)(MAX_TIEMPO_EX - rangoInf - tiempoEx) / (rangoSup - rangoInf);
		}
		
		public boolean esEntrada() {
			return entrada;
		}
		
		public boolean completado() {
			return animTrans == 0 && tiempoEx == 0;
		}
	};
    protected class Animacion {
    		private static final float MAX_FRAMES_MOV = 5;
    		private static final int MAX_SCALE_EX = 100;
    		private static final float FIN_ANIMACION = 0.01f;
    		private static final float VEL_ANIMACION = 1.1875f;		//Número entre el que se divirá framesMov
    		private float framesMov;
    		private Sprite sprFondoAnt;
    		private CancionesCargadas canciones;
    		private float scaleEx;
    		
    		public Animacion(CancionesCargadas c) {
    			framesMov = 0;
    			sprFondoAnt = null;
    			canciones = c;
    			scaleEx = 0;
    		}
    		
    		public void animScaleEx() {
    			//Animar la escala
    			if (scaleEx < MAX_SCALE_EX) {
        			scaleEx /= .125f;
    			}
    			
    			//Ajustar la escala
    			if (scaleEx > MAX_SCALE_EX) {
    				scaleEx = MAX_SCALE_EX;
    			}
    			
    			//Iniciar animación de la escala
    			if (scaleEx == 0) {
    				scaleEx = 1;
    			}
    		}
    		
    		public float getScaleEx() {
    			return scaleEx / MAX_SCALE_EX;
    		}
    		
    		public void reiniciarScaleEx() {
    			scaleEx = 0;
    		}
    		
    		public void animar() {
    			framesMov /= VEL_ANIMACION;		//Mover
    			
    			//Detener animación
    			if (framesMov >= -FIN_ANIMACION && framesMov < 0 ||
    				framesMov > 0 && framesMov <= FIN_ANIMACION) {
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
    
    private final Coord TAM_PORTADA_BARRA = new Coord(430, 150);
    private final int MAX_PUNTUACIONES = 5;
    private final int MAX_PORTADAS = 9;		//Cantidad de portadas cargadas en memoria, debe ser impar
    
    protected Texture txtTitulo, txtKanji, txtShaderFondo, txtBotones, txtBarras;
    protected Sprite sprTitulo, sprKanji, sprBotones, sprBarras;
    protected BitmapFont texto;
    protected CancionesCargadas canciones;
    protected ShaderProgram shaderFondo;
    
    private Texture[] txtPortadas;
    private Sprite[] sprPortadas;
    private Sprite portadaAct;
    private boolean tieneMapa;
    private boolean pantallaAniadir;
    private List<String> puntuaciones;
    
    public Animacion anim;
    public Transicion trans;
    
    DibujadoSeleccion(DibujadoGeneral dib, CancionesCargadas canciones) {
    		this.canciones = canciones;
    		pantallaAniadir = false;
    		txtPortadas = new Texture[canciones.size()];
    		sprPortadas = new Sprite[canciones.size()];
    		anim = new Animacion(canciones);
    		trans = new Transicion(true);
    		cargar(dib);
    }

	@Override
    public void cargar(DibujadoGeneral viejo) {
		//Carga las nuevas imágenes
		if (!(viejo instanceof DibujadoTitulo)) {
			txtTitulo = new Texture("Hud/titulo.png");
			txtKanji = new Texture("Hud/kanji.png");
		}
		//Copia las imágenes ya existentes
		else {
			DibujadoTitulo titulo = (DibujadoTitulo)viejo;
			
			txtTitulo = titulo.txtTitulo;
			txtKanji = titulo.txtKanji;
		}
		texto = new BitmapFont(Gdx.files.internal("Fuente/Ashkar.fnt"));
		txtBotones = new Texture("Hud/botones_seleccion.png");
    		shaderFondo = new ShaderProgram(
    			Gdx.files.internal("Hud/default.vert"),
    			Gdx.files.internal("Hud/mascara.frag")
    		);
		txtShaderFondo = new Texture("Hud/mascara_fondo.png");
		txtBarras = new Texture(Gdx.files.internal("Hud/hud_barras.png"));
		
		sprTitulo = new Sprite(txtTitulo);
		sprKanji = new Sprite(txtKanji);
		sprBotones = new Sprite(txtBotones);
		sprBarras = new Sprite(txtBarras);
		
		//Ajustar tamaños de los sprites y texto
	    sprTitulo.setScale(0.25f);
	    sprKanji.setScale(0.25f);
		texto.getData().scaleX = TAM_TXT.x;
			
		//Ajustar posición en pantallaaaaaa
		sprTitulo.setX(-sprTitulo.getWidth() * sprTitulo.getScaleX() * 2 / 3);
	    sprTitulo.setY((Coord.RESOL_Y - sprTitulo.getHeight()) / 3f);
	    sprKanji.setX(-sprKanji.getWidth() * sprKanji.getScaleX() * 2 / 3);
	    sprKanji.setY((Coord.RESOL_Y - sprKanji.getHeight()) / 3f);
	    
	    //reproduce la canción si no se está reproduciendo ya
	    if (!canciones.getCancionActual().isPlaying()) {
	    		canciones.getCancionActual().play();
	    		canciones.getCancionActual().setVolume(1);
	    }
	    
		recargarTexturas();
	}
	
	public void recargarTexturas() {
		int l = canciones.getIndiceCancion();
		//Limpieza original
		for (int i = 0; i < canciones.size(); i++) {
			//Descargar solo las texturas necesarias según el nuevo índice
			if (i <= l - MAX_PORTADAS / 2 && i > l - MAX_PORTADAS / 2) {
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
				if (j != canciones.size() - 1) {
					Coord escala = new Coord(1, 1);
					Coord escSprite = new Coord(1, 1);
					Texture textura;
					Sprite sprite;
					String ruta = canciones.rutaCancion(j) + "/" + canciones.nombreCancion(j);
					System.out.println("CARGADO: " + ruta);

		    			//cargar el fondo
		        		try {
		        			textura = new Texture(Gdx.files.local(ruta + ".png"));
			    	    }
			    	    catch (Exception noF) {
			    	    		try {
			    	    			textura = new Texture(Gdx.files.local(ruta + ".jpeg"));
		        		    }
			    	    		catch (Exception noFoto) {
				    	    		// Esto no debería existir, pero lo dejo por flojo
				    	    		Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
				    	    		noFondo.setColor(new Color(0, 0, 0, 0));
				    	    		noFondo.fill();
				    	    		
				    	    		textura = new Texture(noFondo);
				    	    		noFondo.dispose();
			    	    		}
			    	    }
		        		
		        		txtPortadas[j] = (textura);
		        		//Imagen reescalada para caber en el selector
		        		escala.x = TAM_PORTADA_BARRA.x / textura.getWidth();
		        		escala.y = TAM_PORTADA_BARRA.y / textura.getHeight();
		        		sprite = new Sprite(textura);
		        		//Resol 2:1
		        		sprite.setRegion(0, textura.getHeight() / 4, textura.getWidth(), textura.getHeight() / 2);
		        		sprite.setScale(escala.x, escala.y);
		        		sprPortadas[j] = (sprite);
		        		
		        		//revisa si existe mapa para el nivel
				}
				else {
					Pixmap noFondo = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
					noFondo.setColor(new Color(0.3984f, 0.13085f, 0.38475f, 1));
					noFondo.fill();
					
					txtPortadas[j] = new Texture(noFondo);
					sprPortadas[j] = new Sprite(txtPortadas[j]);
					noFondo.dispose();
				}
			}
		}
		if (l != canciones.size() -1) {
			String ruta = canciones.rutaCancion(l) + "/" + canciones.nombreCancion(l)+ ".dat";
			tieneMapa = Gdx.files.local(ruta).exists();
			puntuaciones = leerPuntajes();
		}
		else {
			tieneMapa = false;
		}
	}

    private List<String> leerPuntajes() {     //Ps por si quieren leer, en caso contrario se elimina esto
		CancionesCargadas can = ((UchitaiGame)Gdx.app.getApplicationListener()).getCanciones();
		int i = can.getIndiceCancion();
		String ruta = can.rutaCancion(i) + "/" + can.nombreCancion(i) + " Progreso.txt";
        Path puntaje = Paths.get(ruta);
        if (Files.exists(puntaje)) {    //si existe el txt
            try {       //Si se usa pues tecnicamente se manda a imrpimir a la pantalla grafica
                List<String> lineas = Files.readAllLines(puntaje);

                System.out.printf("%-65s%n","-------Historial de Puntajes-------");
                System.out.printf("%-15s / %-25s %-25s","Jugador","Puntaje","Fecha y Hora");

                for (String linea : lineas) {
                    System.out.println(linea);
                }
                
                return lineas;

            } catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
            }
        } else {
            System.out.println("El archivo de puntuaciones aún no existe.");
        }

        return null;
    }
	
	@Override
    public void descargar(DibujadoGeneral nuevo) {		
		//Limpieza de solo lo necesario
    		if (!(nuevo instanceof DibujadoTitulo)) {
	        txtTitulo.dispose();
	        txtKanji.dispose();
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

	    texto.dispose();
    		txtBotones.dispose();
    		txtShaderFondo.dispose();
    		txtBarras.dispose();
    		shaderFondo.dispose();
    	}
	
	@Override
    public void dibujar() {
		GlyphLayout renderTexto = new GlyphLayout();
		InputGeneral inp = (InputGeneral)Gdx.input.getInputProcessor();
		boolean cursorEncima = false, transEntrada = false, transSalida = false;
		int indice = canciones.getIndiceCancion();
		float animacion = 350 * anim.relacionAnim();
		float animBrincos, animBoton = 0;
		float relacionAnimAj = Math.abs(anim.relacionAnim()) > 1 ? 1 : Math.abs(anim.relacionAnim());
		Coord transCoord = new Coord();
		float transTransparencia = 1;
		
		//Ajustar entrada
		if (trans.esEntrada()) {
			transEntrada = true;
			transCoord.y = trans.relacionAnim();
			trans.animar();
		}
		//Ajustar salida
		else if (trans.relacionAnim() > 0) {
			transSalida = true;
			transCoord.y = trans.relacionAnim();
			transTransparencia = 1f - trans.relacionAnim();
		}
		
        ScreenUtils.clear(.10f, .09f, .11f, 1);
        frames = Gdx.graphics.getFrameId();

        animBrincos = (float)(frames % bmp) / bmp;
        
        /*------ Dibujado Fondo -----*/
        dibujadoPantalla.begin();

        //Animación fondo cambio de canción
        if (anim.getSprAnt() != null) {
        		//variables encargadas de la animación
        		float anima = anim.relacionAnim() > 0 ? -1.0f: 1.0f;
        		float relacionAnimLim = 0;
        		
        		//Se limita la variable de animación a 1 o -1 para evitar errores gráficos
            if (anim.relacionAnim() > 0) {
            		relacionAnimLim = anim.relacionAnim() > 1.0f ? 1.0f : anim.relacionAnim();
            }
            else if (anim.relacionAnim() < 0){
        			relacionAnimLim = anim.relacionAnim() < -1.0f ? -1.0f : anim.relacionAnim();
            }
        		
            //Ajuste de la imagen de fondo
    			anim.getSprAnt().setPosition(
    				(Coord.RESOL_X - anim.getSprAnt().getWidth()) / 2 + Coord.RESOL_X * (anima + relacionAnimLim), 
    				(Coord.RESOL_Y - anim.getSprAnt().getHeight()) / 2
    			);
            anim.getSprAnt().draw(dibujadoPantalla);
        }
        //Animación del fondo actual
        sprFondo.setPosition(
        		(Coord.RESOL_X - sprFondo.getWidth()) / 2 + Coord.RESOL_X * (anim.relacionAnim()) + mouse.x / Coord.RESOL_X * 10 - 5, 
        		(Coord.RESOL_Y - sprFondo.getHeight()) / 2  + mouse.y / Coord.RESOL_Y * 10 - 5
        	);
        sprFondo.draw(dibujadoPantalla);
        
        dibujadoPantalla.end();
        
        /*------ DIBUJADO DE FIGURAS -----*/
        figurasPantalla.begin();
        
        //Dibujado barra canciones fondo
        if (!transSalida) {
            figurasPantalla.set(ShapeType.Filled);
            figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
            figurasPantalla.rect(
            		0, 30,
            		Coord.RESOL_X, 150
            	);
        }
        //Dibujado transición salida
        else {
            figurasPantalla.set(ShapeType.Filled);
            figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
            figurasPantalla.rect(
            		0, 30 + trans.relacionAnim() * 75,
            		Coord.RESOL_X, 150 * transTransparencia
            	);
            
            //Barra de miniatura
            if (trans.relacionAnimEspera(20, 0) > 0) {
	            	figurasPantalla.set(ShapeType.Filled);
	            figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
	            figurasPantalla.rect(
	            		0, (Coord.RESOL_Y - 300f) / 2,
	            		trans.relacionAnimEspera(20, 0) * (float) Coord.RESOL_X, 300f
	            	);
            }
            //Barra de texto puntuación
            if (trans.relacionAnimEspera(45, 25) > 0 && puntuaciones != null) {
	            	figurasPantalla.set(ShapeType.Filled);
	            	figurasPantalla.setColor(new Color(0.05f, 0f, 0.075f, 0.25f));
	            figurasPantalla.rect(
	            		0, (Coord.RESOL_Y - 300f) / 2 - 200f,
	            		trans.relacionAnimEspera(45, 25) * (float) Coord.RESOL_X, 80f
	            	);
	        }
        }
        
        figurasPantalla.end();
        
        /*------ DIBUJADO DE SPRITES -----*/
        dibujadoPantalla.begin();
        
        /*--- Dibujar título ---*/
        //Animación brinquitos
        sprTitulo.setScale(0.25f + 0.015f * animBrincos);
        sprKanji.setScale(0.25f + 0.0025f * animBrincos);
        
        //Transición a salida
        if (transSalida) {
        		sprTitulo.setAlpha(transTransparencia);
        		sprKanji.setAlpha(transTransparencia);
        }
		
        //Dibujado de cada elemento
        sprTitulo.draw(dibujadoPantalla);
        sprKanji.draw(dibujadoPantalla);
        
        //Barras de puntuaciones
        if (puntuaciones != null) {
        		float animX = trans.relacionAnim() * 800 + Coord.RESOL_X * anim.relacionAnim();
	        for (int i = puntuaciones.size() - 1, j = 0; i >= 0 && j < MAX_PUNTUACIONES; i--, j++) {
	        		//Dibujar barra de puntuación
	            sprBarras.setRegion(0, 0, 1280, 460);
	            sprBarras.setSize(1, 460);
	            sprBarras.setOrigin(0, 230);
	            sprBarras.setRotation(0);
	            sprBarras.setScale(700, 0.15f);
	            //Sombra
	            sprBarras.setColor(new Color(0, 0, 0, 0.35f));
	            sprBarras.setPosition(Coord.RESOL_X - 700 + animX, Coord.RESOL_Y - 408 - 80 * j);
	            sprBarras.draw(dibujadoPantalla);

	            //Barrita
	            sprBarras.setColor(new Color(.875f, .875f, .875f, 1));
	            sprBarras.setPosition(Coord.RESOL_X - 700 + animX, Coord.RESOL_Y - 400 - 80 * j);
	            sprBarras.draw(dibujadoPantalla);
	            
	            //Esquinita
	            sprBarras.setRegion(0, 460, 330, 460);
	            sprBarras.setSize(460, 460);
	            sprBarras.setScale(0.1f, 0.15f);
	            //Sombrarronga
	            sprBarras.setColor(new Color(0, 0, 0, 0.35f));
	            sprBarras.setPosition(Coord.RESOL_X - 746f + animX, Coord.RESOL_Y - 408 - 80 * j);
	            sprBarras.draw(dibujadoPantalla);
	            //Equiñonga
	            sprBarras.setColor(new Color(.875f, .875f, .875f, 1));
	            sprBarras.setPosition(Coord.RESOL_X - 746f + animX, Coord.RESOL_Y - 400 - 80 * j);
	            sprBarras.draw(dibujadoPantalla);
	            
	            //Puntuación sombra
	            renderTexto.setText(
	        			texto, puntuaciones.get(i),
	        			new Color(0, 0, 0, 0.35f), 700,
	        			Align.left, false
	            	);
	        		texto.draw(
	        			dibujadoPantalla, renderTexto,
	        			Coord.RESOL_X - 688 + animX, Coord.RESOL_Y - 157 - 80 * j
	        		);
	        		//Puntuación texto
	        		renderTexto.setText(
	        			texto, puntuaciones.get(i),
	        			Color.BLACK, 700,
	        			Align.left, false
	            	);
	        		texto.draw(
	        			dibujadoPantalla, renderTexto,
	        			Coord.RESOL_X - 690 + animX, Coord.RESOL_Y - 155 - 80 * j
	        		);
	        }
        }

        dibujadoPantalla.end();
        
        //Oscurecer pantalla. Transición a salida
        if (transSalida) {
        		//Activa el blending de la imagen
        		//(real, juro saber qué es esto, ya me he peleado con esta cochinada en Allegro)
        		Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            
	        	figurasPantalla.begin();
	
            figurasPantalla.set(ShapeType.Filled);
            figurasPantalla.setColor(new Color(0f, 0f, 0f, 0.65f));
            figurasPantalla.rect(0, 0, Coord.RESOL_X, Coord.RESOL_Y * trans.relacionAnim());
	            
            figurasPantalla.end();
            
            //Desactiva el blend
            Gdx.gl.glDisable(GL20.GL_BLEND);
            
            //Texto mejor puntuación
            if (trans.relacionAnimEspera(45, 25) > 0 && puntuaciones != null) {
	    			Coord txtOrg = new Coord(texto.getScaleX(), texto.getScaleY());
	    			
	    			texto.getData().scaleX = TAM_TXT.x;
	    			texto.getData().scaleY = TAM_TXT.y;

	    			//----- Última puntiación -----
            		dibujadoPantalla.begin();
	    			renderTexto.setText(
	        			texto, "Última Puntuación: " + puntuaciones.getLast(),
	        			new Color(1f, 1f, 1f, 1f), Coord.RESOL_X,
	        			Align.center, false
	            	);
	        		texto.draw(
	        			dibujadoPantalla, renderTexto,
	        			-Coord.RESOL_X * (1f - trans.relacionAnimEspera(45, 25)),
	        			(Coord.RESOL_Y - 300f) / 2 - 145f
	        		);
            		dibujadoPantalla.end();
	        		
	        		texto.getData().scaleX = txtOrg.x;
	        		texto.getData().scaleY = txtOrg.y;
	        }
        }
        
        //Dibujado canciones visibles
        for (int i = 0, j = canciones.getIndiceCancion() - MAX_PORTADAS / 2; i < MAX_PORTADAS; i++, j++) {
        		if (j >= canciones.size() || j < 0) continue;  
        		int posBarraX = (i - MAX_PORTADAS / 2) * 350;
        		float posTextY = 0;
        		//Animar título de canción
        		if (j == indice) {
        			posTextY = 75 - 75 * relacionAnimAj;
        		}
        		if ((animacion > 0 && j == indice - 1) || (animacion < 0 && j == indice + 1)) {
        			posTextY = 75 * relacionAnimAj;
        		}

        		//Dibujar Detalles canción
        		if (j != canciones.size() - 1) {
        			float opacidadTxt = transTransparencia;
            		//Dibujar fondo canción, si es que está cargada
            		if (sprPortadas[j] != null && txtPortadas[j] != null) {
                		//Activa el shader del fondo con los triangulitos
                		dibujadoPantalla.setShader(shaderFondo);
                		dibujadoPantalla.begin();
                		
                		//Tecnisismos de OpenGL para shaders
                		txtShaderFondo.bind(1);
                		shaderFondo.setUniformi("u_mask", 1);
                		txtPortadas[j].bind(0);

                		//Dibujado del fondo
                		//Dibujado normal
                		if (!transEntrada && !transSalida) {
                			sprPortadas[j].setCenter((i - 1) * 350 - 87.5f + animacion, 105);
                			sprPortadas[j].draw(dibujadoPantalla);
                			
                			texto.getData().scaleY = TAM_TXT.y;
                		}
                		//Transición entrada
                		else if (transEntrada) {
                			Coord escalaOrg = new Coord(sprPortadas[j].getScaleX(), sprPortadas[j].getScaleY());
                			sprPortadas[j].setScale(escalaOrg.x, escalaOrg.y * transCoord.y);		//Animación entrada
                			sprPortadas[j].setCenter((i - 1) * 350 - 87.5f + animacion, 105);
                			sprPortadas[j].draw(dibujadoPantalla);
                			sprPortadas[j].setScale(escalaOrg.x, escalaOrg.y);

                			texto.getData().scaleY = transCoord.y;
                		}
                		//Transición Salida
                		else if (transSalida) {
                			//Se achican de vuelta cualquiera que no sea la seleccionada
                			if (indice != j) {
                    			Coord escalaOrg = new Coord(sprPortadas[j].getScaleX(), sprPortadas[j].getScaleY());
                    			sprPortadas[j].setScale(escalaOrg.x, escalaOrg.y * (1f - transCoord.y));		//Animación entrada
                    			sprPortadas[j].setCenter((i - 1) * 350 - 87.5f + animacion, 105);
                    			sprPortadas[j].draw(dibujadoPantalla);
                    			sprPortadas[j].setScale(escalaOrg.x, escalaOrg.y);
                			}
                			//se agranda y se centra la canción seleccionada
                			else {
                    			Coord escalaOrg = new Coord(sprPortadas[j].getScaleX(), sprPortadas[j].getScaleY());
                    			sprPortadas[j].setScale(escalaOrg.x * transCoord.y * 2, escalaOrg.y * transCoord.y * 2);		//Animación salidaaanga
                    			sprPortadas[j].setCenter((i - 1) * 350 - 87.5f + animacion, 105 * (1f - transCoord.y) + Coord.RESOL_Y / 2 * transCoord.y);
                    			sprPortadas[j].draw(dibujadoPantalla);
                    			
                    			sprPortadas[j].setScale(escalaOrg.x, escalaOrg.y);
                			}
                		}
            			
            			//Quitar el shader porque si no todo vale cola
            			dibujadoPantalla.flush();
            			dibujadoPantalla.end();
                		dibujadoPantalla.setShader(null);
            		}
            		
                //---------- Texto de sombra ----------//
            		dibujadoPantalla.begin();
            		
            		if (transSalida && indice == j) {
            			opacidadTxt = 1;
            			posTextY = (Coord.RESOL_Y / 2 + 120) * transCoord.y;
            			
            			texto.getData().scaleX = TAM_TXT.x + trans.relacionAnim() * TAM_TXT.x;
            			texto.getData().scaleY = TAM_TXT.y + trans.relacionAnim() * TAM_TXT.y;
            		}
            		else {
            			texto.getData().scaleX = TAM_TXT.x;
            			texto.getData().scaleY = TAM_TXT.y;
            		}
            		
            		//Dibujar borde texto
            		for (int k = 0; k < 4; k++) {
                		renderTexto.setText(
                			texto, canciones.nombreCancion(j),
                			new Color(0, 0, 0, opacidadTxt), Coord.RESOL_X,
                			Align.center, false
                		);
                		texto.draw(
                			dibujadoPantalla, renderTexto,
                			posBarraX + (k / 2) * 4 * texto.getData().scaleX - 2 + animacion,
                			115 + (k % 2) * 4 * texto.getData().scaleY + 2 + posTextY
                		);
            		}
            		
            		//Texto real
            		renderTexto.setText(
            			texto, canciones.nombreCancion(j),
            			new Color(1, 0.9f, 0.95f, opacidadTxt), Coord.RESOL_X,
            			Align.center, false
            		);
            		texto.draw(
            			dibujadoPantalla, renderTexto,
            			posBarraX + animacion,
            			120 + posTextY
            		);
            		
            		dibujadoPantalla.end();
        		}
        		//Botón añadir canción
        		else {
        			dibujadoPantalla.begin();
        			
        			//Botón añadir
        			sprBotones.setRegion(300, 150, 150, 150);
        			sprBotones.setSize(150,  150);
        			sprBotones.setOrigin(75, 75);
        			sprBotones.setCenter((i - 1) * 350 - 87.5f + animacion, 105);
        			sprBotones.setScale(0.5f + 0.015f * animBrincos);
        			if (pantallaAniadir) {
        				if (inp.getAuxStr().isEmpty()) {
            				sprBotones.setColor(new Color(.1f, .1f ,.1f, 1));
        				}
        				else {
            				sprBotones.setColor(Color.WHITE);
        				}
        			}
        			if (transSalida) {		//Animación encogerse así bien cura
        				sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
        			}
        			sprBotones.draw(dibujadoPantalla);
    				sprBotones.setColor(Color.WHITE);
        			
        			//Dibujar texto junto a animación
        			if (indice == j || relacionAnimAj != 0) {
        				float opacidad = relacionAnimAj;
        				if (indice == j) opacidad = 1;
        				//Para evitar conflictos con la animación de entrada y salida del texto de "Añadir"
        				if ((indice != canciones.size() - 2 && anim.relacionAnim() < 0) ||
        					(indice != canciones.size() - 1 && anim.relacionAnim() > 0)) {
        					opacidad = 0;
        				}

            			if (transSalida) {		//Animación encogerse así bien cura
            				texto.getData().scaleY = TAM_TXT.y * transTransparencia;
            			}
        				
        				//Dibujado sombra texto
            			for (int k = 0; k < 4; k++) {
                    		renderTexto.setText(
                    			texto, "Añadir",
                    			new Color(0, 0, 0, opacidad), Coord.RESOL_X,
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
                			texto, "Añadir",
                			new Color(1, 0.9f, 0.95f, opacidad), Coord.RESOL_X,
                			Align.center, false
                		);
                		texto.draw(
                			dibujadoPantalla, renderTexto,
                			posBarraX + animacion,
                			120 + posTextY
                		);
                		
                		if (transSalida) {		//Recupera el tamaño originalal
            				texto.getData().scaleY = TAM_TXT.y;
            			}
        			}

            		dibujadoPantalla.end();
        		}
        }
        
        dibujadoPantalla.begin();

        //Transición a transparencia
        if (canciones.size() != 1) {
	    		if (transEntrada) {
	    			sprBotones.setAlpha(trans.relacionAnim());
	    		}
	    		//está en el botón de añadir
	    		else if (indice == canciones.size() - 1 && relacionAnimAj == 0) {
	    			sprBotones.setAlpha(0);
	    		}
	    		//LLega al botón de añadir
	    		else if ((indice == canciones.size() - 1 && anim.relacionAnim() > 0)) {
	    			sprBotones.setAlpha(relacionAnimAj);
	    		}
	    		//Se va del boing de añadir :,(
	    		else if ((indice == canciones.size() - 2 && anim.relacionAnim() < 0)) {
	    			sprBotones.setAlpha(1f - relacionAnimAj);
	    		}
	    		//Siempre mostrar los botongos
	    		else {
	    			sprBotones.setAlpha(1);
	    		}
        }
        else sprBotones.setAlpha(0);
		
		//Botón inicio
        //Si es que la canción actual tiene un mapa
        if (tieneMapa) {
	        	//animación de cuando se le acerca el mouse
	    		if (mouse.dentroDe(Coord.RESOL_X / 2 - 30, Coord.RESOL_X / 2 + 30, 75, 135)) 	{
	    			anim.animScaleEx();
	    			animBoton = anim.getScaleEx() * .125f;
	    			cursorEncima = true;
	    		}
	    		else animBoton = 0;
	    		
	    		sprBotones.setRegion(0, 0, 150, 150);
	    		sprBotones.setSize(150,  150);
	    		sprBotones.setOrigin(75, 75);
	    		sprBotones.setPosition(Coord.RESOL_X / 2 - 75, 30);
	    		sprBotones.setScale(0.5f + 0.03f * animBrincos + animBoton);
	    		if (transSalida) {		//Animación encogerse así bien cura
	    			sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
	    		}
	    		sprBotones.draw(dibujadoPantalla);

	    		sprBotones.setAlpha(1);
	    		
	    		//Animación desvanecido
	    		if (transSalida) {		
	    			sprBotones.setAlpha(transTransparencia);
	    		}
	    		
	    		//Botón Editar
	    		//animación de cuando se le acerca el mouse
	    		if (mouse.dentroDe(Coord.RESOL_X - 205, Coord.RESOL_X - 95, 200, 300)) 	{
	    			anim.animScaleEx();
	    			animBoton = anim.getScaleEx() * .1f;
	    			cursorEncima = true;
	    		}
	    		else animBoton = 0;
	    		sprBotones.setRegion(150, 0, 150, 150);
	    		sprBotones.setSize(150,  150);
	    		sprBotones.setOrigin(75, 75);
	    		sprBotones.setPosition(Coord.RESOL_X - 225, 175);
	    		sprBotones.setScale(0.9f + 0.02f * animBrincos + animBoton);
	    		if (transSalida) {		//Animación encogerse así bien cura
	    			sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
	    		}
	    		sprBotones.draw(dibujadoPantalla);
        }
        else {
        		//Editar
	        	//animación de cuando se le acerca el mouse
	    		if (mouse.dentroDe(Coord.RESOL_X / 2 - 30, Coord.RESOL_X / 2 + 30, 75, 135)) 	{
	    			anim.animScaleEx();
	    			animBoton = anim.getScaleEx() * .125f;
	    			cursorEncima = true;
	    		}
	    		else animBoton = 0;
	    		
	    		sprBotones.setRegion(150, 0, 150, 150);
	    		sprBotones.setSize(150,  150);
	    		sprBotones.setOrigin(75, 75);
	    		sprBotones.setPosition(Coord.RESOL_X / 2 - 75, 30);
	    		sprBotones.setScale(0.7f + 0.03f * animBrincos + animBoton);
	    		if (transSalida) {		//Animación encogerse así bien cura
	    			sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
	    		}
	    		sprBotones.draw(dibujadoPantalla);
	
	    		sprBotones.setAlpha(1);
	    		
	    		//Animación desvanecido
	    		if (transSalida) {		
	    			sprBotones.setAlpha(transTransparencia);
	    		}
        }

		//Si hay más de una rola
		if (canciones.size() != 1) {
			//Transición a transparencia
			if (transEntrada) {
				sprBotones.setAlpha(trans.relacionAnim());
			}
			//Transición a salida
			else if (transSalida) {		
				sprBotones.setAlpha(transTransparencia);
			}
			//Siempre mostrar los botongos, ahora sin trans (clap clap)
			else {
				sprBotones.setAlpha(1);
			}
			//Botón Aleatorio
			//animación de cuando se le acerca el mouse
			if (mouse.dentroDe(95, 205, 200, 300)) {
				anim.animScaleEx();
				animBoton = anim.getScaleEx() * .1f;
				cursorEncima = true;
			}
			else animBoton = 0;
			sprBotones.setRegion(300, 0, 150, 150);
			sprBotones.setSize(150,  150);
			sprBotones.setOrigin(75, 75);
			sprBotones.setPosition(75, 175);
			sprBotones.setScale(0.9f + 0.02f * animBrincos + animBoton);
			sprBotones.draw(dibujadoPantalla);
			
			//Transición entrada de flechongas
			sprBotones.setAlpha(1);

			//Botón Izquierda
			sprBotones.setRegion(0, 150, 150, 150);
			sprBotones.setSize(300,  300);
			sprBotones.setOrigin(150 + Coord.RESOL_X / 2, 150);
			sprBotones.setPosition(-Coord.RESOL_X / 4 + 150 + animacion * .25f, -45);
			if (transEntrada) {
				sprBotones.setX(sprBotones.getX() - 300 * (1f - trans.relacionAnim()));
			}
			sprBotones.setScale(0.5f + 0.015f * animBrincos);
			if (transSalida) {		//Animación encogerse así bien cura
				sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
			}
			sprBotones.draw(dibujadoPantalla);

			//Botón Derecha
			sprBotones.setRegion(150, 150, 150, 150);
			sprBotones.setSize(300,  300);
			sprBotones.setOrigin(150 - Coord.RESOL_X / 2, 150);
			sprBotones.setPosition(Coord.RESOL_X + 150f / 4 + animacion * .25f, -45);
			if (transEntrada) {
				sprBotones.setX(sprBotones.getX() + 300 * (1f - trans.relacionAnim()));
			}
			sprBotones.setScale(0.5f + 0.015f * animBrincos);
			if (transSalida) {		//Animación encogerse así bien cura
				sprBotones.setScale(sprBotones.getScaleX(), sprBotones.getScaleY() * transTransparencia);
			}
			sprBotones.draw(dibujadoPantalla);
		}
		
        dibujadoPantalla.end();
		
		//Pantalla para que rubén añada el importe de archivos
		if (pantallaAniadir) {
			Coord c = new Coord(Coord.RESOL_X / 2 - Coord.RESOL_X / 6 - 75, (Coord.RESOL_Y - 400f) / 2 + 150);
			figurasPantalla.begin();
			
            figurasPantalla.set(ShapeType.Filled);
            figurasPantalla.setColor(new Color(0f, 0f, 0f, 0.65f));
            figurasPantalla.rect(
            		0, (Coord.RESOL_Y - 450f) / 2,
            		Coord.RESOL_X, 450f
            	);

            figurasPantalla.setColor(new Color(0.125f, 0.125f, 0.125f, 0.65f));
            figurasPantalla.rect(
                	(Coord.RESOL_X - 400) / 2, c.y - 100,
                	400, 50
            );
            figurasPantalla.set(ShapeType.Line);
            figurasPantalla.setColor(Color.WHITE);
            //Señalar cajita
            if (inp.estaEscribiendo()) {
                figurasPantalla.rect(
                    	(Coord.RESOL_X - 400) / 2, c.y - 100,
                    	400, 50
                );
            }
            //Animación escribir si no hay texto
            else if (inp.getAuxStr().isEmpty() && frames % 60 > 0 && frames % 60 < 30){
                figurasPantalla.rect(
                    	(Coord.RESOL_X - 400) / 2, c.y - 100,
                    	400, 50
                );
            }
	            
            figurasPantalla.end();
            
            dibujadoPantalla.begin();
            
            //Añadir canción
            if (mouse.dentroDe(c.x, c.x + 150, c.y, c.y + 150)) {
	    			anim.animScaleEx();
	    			animBoton = anim.getScaleEx() * .1f;
	    			cursorEncima = true;
	    		}
	    		else animBoton = 0;
	    		sprBotones.setRegion(300, 150, 150, 150);
	    		sprBotones.setSize(150,  150);
	    		sprBotones.setOrigin(75, 75);
	    		sprBotones.setPosition(c.x, c.y);
	    		sprBotones.setScale(0.7f + 0.02f * animBrincos + animBoton);
	    		sprBotones.draw(dibujadoPantalla);
	    		
	    		//Texto añadir canción pal botón añadir canción
	    		texto.getData().scaleX = TAM_TXT.x * 1.2f;
	    		texto.getData().scaleY = TAM_TXT.y * 1.2f;
        		renderTexto.setText(
        			texto, "Añadir canción",
        			new Color(1, 0.9f, 0.95f, 1), 500,
        			Align.center, false
        		);
        		texto.draw(
        			dibujadoPantalla, renderTexto,
        			c.x - 175,
        			180 + c.y
        		);

	    		c.x = Coord.RESOL_X / 2 + Coord.RESOL_X / 6 - 75;
	    		//Añadir foto
            if (mouse.dentroDe(c.x, c.x + 150, c.y, c.y + 150)) 	{
	    			anim.animScaleEx();
	    			animBoton = anim.getScaleEx() * .1f;
	    			cursorEncima = true;
	    		}
	    		else animBoton = 0;
	    		sprBotones.setRegion(300, 150, 150, 150);
	    		sprBotones.setSize(150,  150);
	    		sprBotones.setOrigin(75, 75);
	    		sprBotones.setPosition(c.x, c.y);
	    		sprBotones.setScale(0.7f + 0.02f * animBrincos + animBoton);
	    		sprBotones.draw(dibujadoPantalla);

        		renderTexto.setText(
        			texto, "Añadir fondo",
        			new Color(1, 0.9f, 0.95f, 1), 500,
        			Align.center, false
        		);
        		texto.draw(
        			dibujadoPantalla, renderTexto,
        			c.x - 175,
        			180 + c.y
        		);
        		
        		//Texto introducido
        		renderTexto.setText(
        			texto, inp.getAuxStr(),
        			new Color(1, 0.9f, 0.95f, 1), 600,
        			Align.center, false
        		);
        		texto.draw(
        			dibujadoPantalla, renderTexto,
        			(Coord.RESOL_X - 600) / 2,
        			c.y - 60
        		);
        		//Pista introducir texto
        		renderTexto.setText(
        			texto, "Nuevo nombre",
        			new Color(1, 0.9f, 0.95f, 1), 600,
        			Align.center, false
        		);
        		texto.draw(
        			dibujadoPantalla, renderTexto,
        			(Coord.RESOL_X - 600) / 2,
        			c.y
        		);

	        dibujadoPantalla.end();
		}

		if (!cursorEncima) anim.reiniciarScaleEx();
        anim.animar();
	}
	
    @Override
    public boolean transCompletada() {
    		trans.animar();
    		if (!trans.esEntrada() && trans.relacionAnimEspera(120, 0) > 0) {
    			canciones.getCancionActual().setVolume(1f - trans.relacionAnimEspera(120, 0));
    		}
		return trans.completado();
    }
    
    @Override
    public boolean estaBloqueado() {
    		return trans.relacionAnim() != 0 && !trans.esEntrada();
    }
    
    public void setAniadir(boolean b) {
    		pantallaAniadir = b;
    		if (!b) {
        		trans = new Transicion(false);
    		}
    }
    
    public boolean getAniadir() {
    		return pantallaAniadir;
    }
}