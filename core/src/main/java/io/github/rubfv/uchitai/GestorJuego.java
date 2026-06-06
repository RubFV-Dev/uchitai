package io.github.rubfv.uchitai;
import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.nio.file.Path;
import java.nio.file.Paths;

//Encargado de manejar la lógica del juego.
public class GestorJuego extends Gestor {
	enum ESTADO{ JUGANDO, GANO, PERDIO }

	public static final int MAX_FALLOS = 15;

    //Mapa (Keycode, cola de tiempos por tecla) Reordenar para separar por teclas
    //de esta forma sera más facil de manejar
    protected TreeMap<Float,List<Nota>> notasTecla;
    protected List<Nota> notasActivas;
    protected int posNota, anteriorKeycode;

    protected int puntaje;
    protected int combo, mjrCombo;                  //El combo es un multiplicador de los puntos
    protected int numFallos;              //contador de fallos

    protected float fallo;                  //contador de fallos (fallos maximos 10)
    protected int mediaAcert;           //contador media aciertos
    protected int acertada;             //contador acertada
    protected int contadorIni;
    protected ESTADO estadoJuego;		//de la enumeración
    //todo progreso aun no se

     GestorJuego(Nivel level, CancionesCargadas canciones){		//El nivel lo recibe, por ende RODRIGO, a la hora de entrar al nivel debe de
    	 	super(level, canciones);
        puntaje=0;
        combo = mjrCombo =0;
        numFallos=0;
        fallo=0;
        mediaAcert=0;
        acertada=0;
        notasTecla = level.getNotas();
        notasActivas = new ArrayList<>();
        contadorIni = 90;
        estadoJuego = ESTADO.JUGANDO;

        Nota.setAparicion(TIEMPO_APARICION);

        System.out.println("LISTA NOTAS GUARDADAS: " + aparicionNotas);
		//Muestra una lista completa de las teclas presionadas
		for (Entry<Float, List<Nota>> inp: notasTecla.entrySet()) {
			Float tiempo = inp.getKey();
			ArrayList<Nota> listaNotas = (ArrayList<Nota>) inp.getValue();
			System.out.print("SEC: " + tiempo + "\tNOTAS: ");

			for (Nota n: listaNotas) {
				System.out.print((char)(n.getTecla() + 36) + " ");
			}
			System.out.println();
		}

		//variables utilizadas para darles id's a las notas (por el dibujado, agruparlas, etc)
		posNota = 0;
		anteriorKeycode = -1;
    }

    public List<Nota> getNotasActivas() {
        return notasActivas;
    }

    // NOTA: Tecnicamente se usa polleo en cada render de las cosas por asi decirlo

    //Metodo que manejara las teclas que se presionen
    @Override
    public void procesarTeclaPresionada(int keycode) {
        //Plan: Se mantendra en la estructura notas Activas solo las notas para ese momento
        if (!juegoEstado) return;

        Nota notaPress = null;
        for(Nota n : notasActivas) {     //se hace la busqueda de la tecla presionada
            if(n.getTecla() == keycode){
                notaPress = n;
                break;
            }
        }

        if (notaPress == null) {
            //notaOlvidada();     //si es null entonces esa no era, restar puntos
        		//Comentado para evitar que pierdas puntos
            return;
        }

        float desfase = Math.abs(notaPress.getTiempoInicio() - tiempoAct);     //todo CONSTRUCTOR EN NOTA

        if(desfase <= MARGEN_ERROR){  //para notas que se presionan correctamente
            incrementarPuntaje(notaPress, desfase);
            // la nota ya termino, la sacamos de la lista
            notasActivas.remove(notaPress);
            acertada++;
        }
        else if(tiempoAct < notaPress.getTiempoInicio() - MARGEN_ERROR){
            // se presiona antes de tiempo
            notaOlvidada();     //para que le de el fallo
            notasActivas.remove(notaPress);     //la quitamos, ya no vale
        }
    }


    //Metodo para poleo
    //Como este es de poleo aqui gestionara el momento para llamar al perder (pendiente fin de juego)
    @Override
    public boolean actualizar() {
        // Plan: Metodo encargado de las teclas que se olvidan osea no se presionan, este sera continuo
        // de cierta forma, lo ideal es que cada que avance el tiempo se llame al metodo
        // este verifica las colas de todas las notas y si no estan dentro del margen entonces las elimina
        if (!juegoEstado) return false;

        //Tiempo inicio
        if (contadorIni > 0) {
        	System.out.println("T: "+ contadorIni);
	        	contadorIni--;
	        	tiempoAct = -(float)contadorIni / 60;
	        	if (contadorIni == 0) {
	        		juegoEstado = true;
	        		reanudarJuego();
	        	}
        }
        //Aquí actualizamos el tiempo ya que esta se ejecuta en cada ciclo
        else {
            tiempoAct = musicaActual.getPosition();
        }

        // Tomamos el submapa de todas las notas cuyo tiempo ya llegó
        SortedMap <Float, List<Nota>> notasAparece = notasTecla.headMap(tiempoAct + aparicionNotas);

        if (!notasAparece.isEmpty()) {    //Si existen para ese tiempo
        		boolean nuevoKeycode = false;
            for (List<Nota> listaNotas : notasAparece.values()) {
                for (Nota n : listaNotas) {
                		n.setId(posNota);
                    notasActivas.add(n);

                    if (!nuevoKeycode && anteriorKeycode != n.getTecla() && anteriorKeycode != -1) {
                    		nuevoKeycode = true;
                    		anteriorKeycode = -1;
                    }
                    if (anteriorKeycode == -1) anteriorKeycode = n.getTecla();
                }
            }
            if (nuevoKeycode) posNota++;
            notasAparece.clear();       //las pasamos a la nueva estructura
        }

        Iterator<Nota> ite = notasActivas.iterator();
        while (ite.hasNext()) {
            Nota notaEnPantalla = ite.next();

            //tiempo límite en el que la nota caduca de la pantalla
            float limiteTiempo = notaEnPantalla.getTiempoInicio() + MARGEN_ERROR;

            if (limiteTiempo < tiempoAct) {
                //si el tiempo actual superó el límite la nota ya se olvido
                notaOlvidada();
                ite.remove();               //Borramos
            }
        }

        //Cambiar a que el juego acabó
        if (tiempoAct > nivelAct.getTiempoFinal() + TIEMPO_APARICION * 5) {
        		estadoJuego = ESTADO.GANO;
        }

        // checar si ya perdio (15 fallos)
        if(fallo >= MAX_FALLOS){
        		//FIN DEL JUEGO
            juegoEstado=false;
            if(musicaActual != null){    //para detener la musica
            		estadoJuego = ESTADO.PERDIO;
                musicaActual.stop();
            }

            // TODO perdio, se guarda puntaje?
            return true;        //true de perdio
        }
        return false;
    }

    public void pausarJuego(){
        if (musicaActual != null && musicaActual.isPlaying()) {
            musicaActual.pause();
            juegoEstado = false;
        }
    }
    public void reanudarJuego(){
        if (musicaActual != null && !musicaActual.isPlaying()) {
            musicaActual.play();
            juegoEstado = true;
        }
    }

    public void incrementarPuntaje(Nota n, float desfase) {  //mas que nada cosas proporcionales
        float multiplicador = 1;
        if (combo >= 50) 		multiplicador = 3f;
        else if (combo >= 30) 	multiplicador = 2.5f;
        else if (combo >= 20) 	multiplicador = 2f;
        else if (combo >= 10) 	multiplicador = 1.5f;
        PUNTERIA msg = PUNTERIA.BAD;

        int puntosB = 0;  //Para generalizar casos

        if (desfase <= MARGEN_ERROR * 0.2f) {     //momento exacto con un rango pequeño para que lo presione
            msg = PUNTERIA.PERFECT;
            puntosB = 300;
            fallo -= 1.5f;

        } else if (desfase <= MARGEN_ERROR * 0.5f) {      //A esta funcion solo entra cuando no hay fallos
        		msg = PUNTERIA.GREAT;
            puntosB = 200;
            fallo -= 1f;

        } else {
        		msg = PUNTERIA.GOOD;
            puntosB = 100;
            fallo -= 0.5f;
        }
        esCombo(true);
        if (fallo < 0) fallo = 0;
        puntaje += (puntosB * multiplicador);

        //Mandar mensaje de puntería al dibujado
        UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
        if (juego.getDibujado() instanceof DibujadoJuego) {
        		DibujadoJuego d = (DibujadoJuego)juego.getDibujado();
        		d.obtenerPunteria(msg, n.getId());
        }
    }

    public void esCombo(boolean acierto) {
        if(acierto){ //si se continua con el combo se suma
            combo++;
            if (combo > mjrCombo) mjrCombo = combo;
            return;
        }
        combo = 0;        //si no reinicia
    }

    public void notaOlvidada() {
    		PUNTERIA msg = PUNTERIA.BAD;
        esCombo(false);
        puntaje -= 50;
        if (puntaje < 0) puntaje = 0;
        numFallos++;    //estadistica
        fallo += 1f;        //vida

        UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
        if (juego.getDibujado() instanceof DibujadoJuego) {
        		DibujadoJuego d = (DibujadoJuego)juego.getDibujado();
        		d.obtenerPunteria(msg, 0);
        }
    }

    public float getRelacionVida() {
    		return fallo / MAX_FALLOS;
    }

    public float getRelacionTiempo() {
    		float r = musicaActual.getPosition() / nivelAct.getTiempoFinal();
    		if (r > 1f) return 1f;
    		return r;
    }

    public int getCombo() {
    		return combo;
    }

    public int getMjrCombo() {
    		return mjrCombo;
    }

    public int getFallos() {
    		return numFallos;
    }

    public int getPuntaje() {
    		return puntaje;
    }

    public int getTotalNotas() {
    		return (acertada + numFallos);
    }

    public float getAsertivo() {
    		float r = (float)acertada / (acertada + numFallos);
    		if (acertada + numFallos == 0) return 100;
    		return r * 100;
    }

    //TODO checar en que momento se manda a llamar
    @Override
    public void guardar() {
    		if (estadoJuego == ESTADO.GANO) {
    			CancionesCargadas can = ((UchitaiGame)Gdx.app.getApplicationListener()).getCanciones();
        		int i = can.getIndiceCancion();
        		InputGeneral inp = (InputGeneral)Gdx.input.getInputProcessor();
        		String nombreJugador = inp.getLevelName();
        		String ArchivoPuntuacion = can.rutaCancion(i) + "/" + can.nombreCancion(i) + " Progreso.txt";
            Path archivo = Paths.get(ArchivoPuntuacion);

            //Para obtener la fecha y hora del registro
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaFormateada = ahora.format(formato);

            //pa que quede cuadradito
            String registro = String.format("%-15s %-25s %-25s",nombreJugador,puntaje,fechaFormateada);

            try {
                Files.write(        //Cosas que hacen mas sencillo el manejo de los archivos
                    archivo,
                    Collections.singletonList(registro),
                    StandardOpenOption.CREATE,           // <- Si no existe, lo CREA
                    StandardOpenOption.APPEND            // <- Si ya existe, lo añade al FINAL (Append)
                );
                System.out.println("Puntaje añadido");

            } catch (IOException e) {
                System.err.println("Error al escribir: " + e.getMessage());
            }
    		}
    }

    public boolean esPerdio() {
    		return estadoJuego == ESTADO.PERDIO;
    }

    public boolean esGano() {
    		return estadoJuego == ESTADO.GANO;
    }
}
