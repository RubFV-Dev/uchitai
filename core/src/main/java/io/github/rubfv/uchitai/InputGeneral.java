package io.github.rubfv.uchitai;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;

import static io.github.rubfv.uchitai.PANTALLA.JUEGO;

public class InputGeneral extends InputAdapter {
    protected final int CANT_TECLAS = 26;
    private PANTALLA pantallaAct;
    private Coord mouse;
    protected boolean[] teclas;
    private String LevelName;
    private FileHandle SongFile;
    private FileHandle BackgroundFile;
    private boolean escribiendo;

    InputGeneral(PANTALLA p, Coord m) {
        super();
        teclas = new boolean[CANT_TECLAS];
        for (int i = 0; i < CANT_TECLAS; i++) {
            teclas[i] = false;
        }
        pantallaAct = p;
        mouse = m;
        LevelName = "";
        escribiendo = false;
        
        SongFile = null;
        BackgroundFile = null;
    }

    public void setPantallaAct(PANTALLA pantalla) {
        pantallaAct = pantalla;
    }

    // Esta función SIEMPRE se llamará al presionar una tecla
    @Override
    public boolean keyDown(int keycode) {
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();

        // Mostrar que se tecleaba
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            teclas[keycode - Input.Keys.A] = true;
        }

        if (!escribiendo) {
            if (!juego.getDibujado().estaBloqueado()) {
                switch (juego.getPantallaAct()) {
                    case TITULO:
                        // Salir del juego
                        if (keycode == Input.Keys.ESCAPE) {
                            Gdx.app.exit();
                            return false;
                        }

                        // Iniciar juego
                        if (keycode < Input.Keys.F1 || keycode > Input.Keys.F12) {
                            juego.setPantallaAct(PANTALLA.SELECCION);
                        }
                        break;
                    case SELECCION:
                        // Entrada de datos normales
                        switch (keycode) {
                            case Input.Keys.LEFT:
                                juego.getCanciones().antCancion();
                                break;

                            case Input.Keys.RIGHT:
                                juego.getCanciones().sigCancion();
                                break;

                            case Input.Keys.SPACE:
                                if (!((DibujadoSeleccion) juego.getDibujado()).getAniadir()) {
                                    juego.setPantallaAct(JUEGO);
                                }
                                else if (!LevelName.isEmpty()) {
	                                	if (CancionesCargadas.subirCancion(getLevelName(), SongFile, BackgroundFile)) {
	                            			juego.getCanciones().setIndiceCancion(juego.getCanciones().getIndiceCancion() + 1);
	                                     limpiarArchivos();
	                            		}
                                }
                                break;

                            case Input.Keys.SHIFT_LEFT:
                            case Input.Keys.SHIFT_RIGHT:
                                cancionAleatoria();
                                break;

                            // Regresar al menú anterior
                            case Input.Keys.ESCAPE:
                                juego.setPantallaAct(PANTALLA.TITULO);
                                break;
                        }
                        break;

                    case JUEGO:
                        switch (keycode) {
                            default:
                                if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                                    if (juego.getGestorPartida() != null) {
                                        juego.getGestorPartida().procesarTeclaPresionada(keycode);
                                    }
                                }
                                break;
                            case Input.Keys.ENTER:
                                if (juego.getGestorPartida() != null) {
                                    GestorJuego gestor = (GestorJuego) juego.getGestorPartida();
                                    if (gestor.esGano() || gestor.esPerdio()) {
                                        juego.setPantallaAct(PANTALLA.SELECCION);
                                    }
                                }
                                break;

                            // Regresar al menú anterior
                            case Input.Keys.ESCAPE:
                                juego.setPantallaAct(PANTALLA.SELECCION);
                                break;
                        }
                        break;

                    case EDICION:
                        switch (keycode) {
                            default:
                                if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                                    if (juego.getGestorPartida() != null) {
                                        ((GestorEditor)juego.getGestorPartida()).procesarTeclaPresionada(keycode, Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT));
                                    }
                                }
                                break;

                            // Regresar y guardar
                            case Input.Keys.ESCAPE:
                                juego.setPantallaAct(PANTALLA.SELECCION);
                                break;
                            case Input.Keys.SPACE:
                            		if (juego.getCanciones().getCancionActual().isPlaying()) {
                                		juego.getCanciones().getCancionActual().pause();
                            		}
                            		else {
                                		juego.getCanciones().getCancionActual().play();
                            		}
                            		break;
                            		
                            	//Retroceder
                            case Input.Keys.LEFT:
                            		((GestorEditor)(juego.getGestorPartida())).retroceder();
                            		break;
                            	
                            	//Avanzar
                            case Input.Keys.RIGHT:
                            		((GestorEditor)(juego.getGestorPartida())).avanzar();
                            		break;
                            		

                            	//Más brinco
                            case Input.Keys.UP:
                            		((GestorEditor)(juego.getGestorPartida())).aumentarBrinco();
                            		break;
                            	
                            	//Menos brinco
                            case Input.Keys.DOWN:
                            		((GestorEditor)(juego.getGestorPartida())).reducirBrinco();
                            		break;
                        }
                        break;
                }
            }
        }
        // Se está escribiendo en el text area
        else {
            // Input
            if (LevelName.length() < 12) {
                // Letras y su madre
                if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                    int extra = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                            Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ? 36 : 68;
                    LevelName += (char) (keycode + extra);
                }
                // Números
                if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                    LevelName += (char) (keycode + 40);
                }
                // Espacio
                else if (keycode == Input.Keys.SPACE) {
                    LevelName += ' ';
                }
            }
            // Borrar
            if (keycode == Input.Keys.BACKSPACE) {
                if (!LevelName.isEmpty()) {
                    String niu;
                    niu = LevelName.substring(0, LevelName.length() - 1);
                    LevelName = niu;
                }
            }
            // Salir con enter
            if (keycode == Input.Keys.ENTER) {
                escribiendo = false;
            }
        }

        // Pantalla completa siempre
        if (keycode == Input.Keys.F5) {
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(1280, 720);
            }
        }

        // para conocer las canciones actualmente cargadas
        if (keycode == Input.Keys.ALT_LEFT) {
            for (int i = 0; i < juego.getCanciones().size(); i++) {
                System.out.println(juego.getCanciones().nombreCancion(i));
            }
        }

        // De aqui si se presiona cualquier tecla del teclado basico y se esta en la
        // pamntalla juego
        // entonces se manda a la clase que gestiona el juego el keyCode de la tecla
        // presionada
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();

        // Mostrar que se tecleaba
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            teclas[keycode - Input.Keys.A] = false;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        ajusteMouse(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        ajusteMouse(screenX, screenY);

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();
        ajusteMouse(screenX, screenY);

        if (!juego.getDibujado().estaBloqueado()) {
            switch (button) {
                case Input.Buttons.LEFT:
                    escribiendo = false;
                    switch (juego.getPantallaAct()) {
                        case TITULO:
                            juego.setPantallaAct(PANTALLA.SELECCION);
                            return true;
                        case SELECCION:
                            // Detectar selección anterior canción
                            if (mouse.dentroDe(180, Coord.RESOL_X / 2 - 255, 30, 180)) {
                                juego.getCanciones().antCancion();
                            }
                            // Detectar selección aiguiente canción
                            else if (mouse.dentroDe(Coord.RESOL_X / 2 + 255, Coord.RESOL_X - 180, 30, 180)) {
                                juego.getCanciones().sigCancion();
                            }
                            // Botón canción aleatoria
                            else if (mouse.dentroDe(95, 205, 200, 300)) {
                                cancionAleatoria();
                            }
                            // Si no se está añadiendo
                            else if (!((DibujadoSeleccion) juego.getDibujado()).getAniadir()) {
                                // Botón inicio canción
                                if (mouse.dentroDe(Coord.RESOL_X / 2 - 30, Coord.RESOL_X / 2 + 30, 75, 135)) {
                                    juego.setPantallaAct(JUEGO);
                                }
                                // Botón edición mapa
                                else if (mouse.dentroDe(Coord.RESOL_X - 205, Coord.RESOL_X - 95, 200, 300)) {
                                    CancionesCargadas canciones = juego.getCanciones();
                                    int i = canciones.getIndiceCancion();
                                    String ruta = canciones.rutaCancion(i) + "/" + canciones.nombreCancion(i) + ".dat";

                                    if (Gdx.files.local(ruta).exists())
                                        juego.setPantallaAct(PANTALLA.EDICION);
                                }
                            }
                            // Se está añadiendo una canción
                            else {
                                Coord c = new Coord(Coord.RESOL_X / 2 - Coord.RESOL_X / 6 - 75,
                                        (Coord.RESOL_Y - 400f) / 2 + 150);
                                // Añadir canción
                                if (mouse.dentroDe(c.x, c.x + 150, c.y, c.y + 150)) {
                                    juego.gestorArch.PedirArchivo("audio/mpeg", archivo -> {
                                        System.out.println("Archivo mp3 " + archivo + " seleccionado correctamente");
                                        SongFile = archivo;
                                    });
                                }

                                c.x = Coord.RESOL_X / 2 + Coord.RESOL_X / 6 - 75;
                                // Añadir foto
                                if (mouse.dentroDe(c.x, c.x + 150, c.y, c.y + 150)) {
                                    juego.gestorArch.PedirArchivo("image/png", archivo -> {
                                        System.out.println("Archivo png " + archivo + " seleccionado correctamente");
                                        BackgroundFile = archivo;
                                    });
                                }

                                // Añadir texto
                                c.x = Coord.RESOL_X / 2 - 200;
                                if (mouse.dentroDe(c.x, c.x + 400, c.y - 100, c.y - 50)) {
                                    escribiendo = true;
                                }

                                // Guardar TODITO
                                if (mouse.dentroDe(Coord.RESOL_X / 2 - 30, Coord.RESOL_X / 2 + 30, 75, 135) && !LevelName.isEmpty()) {
                                		if (CancionesCargadas.subirCancion(getLevelName(), SongFile, BackgroundFile)) {
                                			juego.getCanciones().setIndiceCancion(juego.getCanciones().getIndiceCancion() + 1);
                                         limpiarArchivos();
                                		}
                                }
                            }
                            return true;

                        case JUEGO:
                            if (mouse.dentroDe(Coord.RESOL_X / 2 - 200, Coord.RESOL_X / 2 + 200,
                                    Coord.RESOL_Y - 950, Coord.RESOL_Y - 900) && ((GestorJuego)juego.getGestorPartida()).esGano()) {
                                escribiendo = true;
                            }
                            break;
                            
                        case EDICION:
                        		if (mouse.dentroDe(Coord.RESOL_X - 57.5f, Coord.RESOL_X, 0, Coord.RESOL_Y)) {
                        			((GestorEditor)juego.getGestorPartida()).ajustarTiempo(mouse.y / Coord.RESOL_Y);
                            }
                        		break;
                    }
                    break;
            }
        }

        return false;
    }

    public void ajusteMouse(int screenX, int screenY) {
        Coord scale = new Coord(), ajuste = new Coord();

        scale.y = ((float) Coord.RESOL_Y / (float) Gdx.graphics.getHeight());
        scale.x = ((float) Coord.RESOL_X / (float) Gdx.graphics.getWidth());

        ajuste.x = (Gdx.graphics.getWidth() * scale.y - Gdx.graphics.getWidth() * scale.x) / 2;
        ajuste.y = (Gdx.graphics.getHeight() * scale.x - Gdx.graphics.getHeight() * scale.y) / 2;

        // Ajuste de resolución porque es un dolor de cocos
        if (ajuste.x > 0) {
            screenX *= scale.y;
            screenX -= ajuste.x;
        } else {
            screenX *= scale.x;
        }
        if (ajuste.y > 0) {
            screenY *= scale.x;
            screenY -= ajuste.y;
        } else {
            screenY *= scale.y;
        }
        screenY = Coord.RESOL_Y - screenY;

        if (screenX > Coord.RESOL_X)
            screenX = Coord.RESOL_X;
        if (screenX < 0)
            screenX = 0;
        if (screenY > Coord.RESOL_Y)
            screenY = Coord.RESOL_Y;
        if (screenY < 0)
            screenY = 0;

        mouse.setCoord(screenX, screenY);
    }

    private void cancionAleatoria() {
        UchitaiGame juego = (UchitaiGame) Gdx.app.getApplicationListener();
        DibujadoSeleccion dibujado = (DibujadoSeleccion) juego.getDibujado();
        Random r = new Random();
        int indiceOrg = juego.getCanciones().getIndiceCancion();
        int indiceAct = -1;

        if (juego.getCanciones().size() > 1) {
            // Nuevo índice aleatorio
            do {
                indiceAct = r.nextInt(juego.getCanciones().size() - 1);
            } while (indiceAct == indiceOrg);

            // Ajuste a la animación de cambio de canción
            // El nuevo índice está más a la izquierda
            if (indiceOrg > indiceAct) {
                for (int i = indiceOrg - indiceAct; i > 0; i--) {
                    dibujado.anim.animDeslizarIzq();
                }
            }
            // Está pa la derecha
            else {
                for (int i = indiceAct - indiceOrg; i > 0; i--) {
                    dibujado.anim.animDeslizarDer();
                }
            }

            // Cargar la nueva canción aleatoria
            juego.getCanciones().cargarCancion(indiceAct);
        }

        // Recargar texturas
        if (juego.getDibujado() instanceof DibujadoSeleccion) {
            DibujadoSeleccion d = (DibujadoSeleccion) juego.getDibujado();
            d.recargarTexturas();

            // Quitar pantalla añadir
            if (indiceOrg == juego.getCanciones().size() - 1) {
                d.setAniadir(false);
                escribiendo = false;
                LevelName = "";
            }
        }
    }

    public boolean teclaPresionada(int indice) {
        return teclas[indice];
    }

    public String getLevelName() {
        return LevelName;
    }

    public boolean estaEscribiendo() {
        return escribiendo;
    }
    
    public boolean existeSong() {
    		return SongFile != null;
    }

    public boolean existeBackground() {
    		return BackgroundFile != null;
    }
    
    public void limpiarArchivos() {
    		BackgroundFile = null;
        SongFile = null;
        LevelName = "";
    }
}
