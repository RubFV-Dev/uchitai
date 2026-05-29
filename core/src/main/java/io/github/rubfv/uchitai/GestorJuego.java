package io.github.rubfv.uchitai;
import com.badlogic.gdx.audio.Music;
import java.util.*;


//Encargado de manejar la lógica del juego.
public class GestorJuego {

	protected final int MAX_FALLOS = 15;
    protected final float MARGEN_ERROR = 0.1f;      //los milisegundos que estan permitidos para desfasarse del presionar

    protected Nivel nivelAct;             //como tal all the level
    //Mapa (Keycode, cola de tiempos por tecla) Reordenar para separar por teclas
    //de esta forma sera más facil de manejar
    protected TreeMap<Float,List<Nota>> notasTecla;
    protected List<Nota> notasActivas;

    protected Music musicaActual;
    protected float tiempoAct;          //A emplear el calculo de la musica Perdon no me acuerdo, Este es nuestro reloj interno
    protected boolean juegoEstado;          //pa saber si esta en pausa o no (pendiente de confirmar)
    protected int puntaje;
    protected int combo;                  //El combo es un multiplicador de los puntos
    protected int numFallos;              //contador de fallos

    protected int fallo;                  //contador de fallos (fallos maximos 14)
    protected int medioFallo;           //contador medio fallo
    protected int acertada;             //contador acertada
    //todo progreso aun no se

     GestorJuego(Nivel level, Music musica){		//El nivel lo recibe, por ende RODRIGO, a la hora de entrar al nivel debe de
        nivelAct=level;							//pasarle la estructura ya extraida del archivo
        tiempoAct=0;
        juegoEstado=true;
        puntaje=0;
        combo=0;
        numFallos=0;
        fallo=0;
        medioFallo=0;
        acertada=0;
        notasTecla = level.getNotas();
        notasActivas = new ArrayList<>();
        musicaActual = musica;
    }

    public List<Nota> getNotasActivas() {
        return notasActivas;
    }

    // NOTA: Tecnicamente se usa polleo en cada render de las cosas por asi decirlo

    //Metodo que manejara las teclas que se presionen
    public void procesarTeclaPresionada(int keycode){
        //Plan: Se mantendra en la estructura notas Activas solo las notas para ese momento
        if(!juegoEstado) return;

        Nota notaPress =null;
        for(Nota n : notasActivas){     //se hace la busqueda de la tecla presionada
            if(n.getTecla() == keycode){
                notaPress=n;
                break;
            }
        }

        if (notaPress==null){
            notaOlvidada();     //si es null entonces esa no era, restar puntos
            return;
        }

        float desfase= Math.abs(notaPress.getTiempoInicio()- tiempoAct);     //todo CONSTRUCTOR EN NOTA

        if(desfase <= MARGEN_ERROR){  //para notas que se presionan correctamente
            incrementarPuntaje(desfase);
            // la nota ya termino, la sacamos de la lista
            notasActivas.remove(notaPress);
            acertada++;
        }
        else if(tiempoAct < notaPress.getTiempoInicio()- MARGEN_ERROR){
            // se presiona antes de tiempo
            notaOlvidada();     //para que le de el fallo
            notasActivas.remove(notaPress);     //la quitamos, ya no vale
        }

    }


    //Metodo para poleo
    //Como este es de poleo aqui gestionara el momento para llamar al perder (pendiente fin de juego)
    public boolean limpieza(){
        // Plan: Metodo encargado de las teclas que se olvidan osea no se presionan, este sera continuo
        // de cierta forma, lo ideal es que cada que avance el tiempo se llame al metodo
        // este verifica las colas de todas las notas y si no estan dentro del margen entonces las elimina
        if(!juegoEstado) return false;

        tiempoAct = getRelacionTiempo();      //Aquí actualizamos el tiempo ya que esta se ejecuta en cada ciclo
        // Tomamos el submapa de todas las notas cuyo tiempo ya llegó
        SortedMap <Float, List<Nota>> notasAparece = notasTecla.headMap(tiempoAct+0.001f);

        if(!notasAparece.isEmpty()){    //Si existen para ese tiempo
            for (List<Nota> listaNotas : notasAparece.values()) {
                for (Nota n : listaNotas) {
                    if (!notasActivas.contains(n)) {
                        notasActivas.add(n);
                    }
                }
            }
            notasAparece.clear();       //las pasmos a la nueva estructura
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


        // checar si ya perdio (15 fallos)
        if(fallo >= MAX_FALLOS){
            juegoEstado=false;
            if(musicaActual !=null){    //para detener la musica
                musicaActual.stop();
            }
            return true;        //true de perdio
        }
        return false;
    }

    //Estas tres van con el tema de la música
    public void iniciarJuego(){
        if (musicaActual != null) {
            tiempoAct = 0;
            musicaActual.stop();
            musicaActual.play();
            musicaActual.setVolume(1);
            juegoEstado = true;
        }
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

    public void incrementarPuntaje(float desfase) {  //mas que nada cosas proporcionales
        int multiplicador = 1;
        if (combo > 30) multiplicador = 5;
        else if (combo > 20) multiplicador = 4;
        else if (combo > 15) multiplicador = 3;
        else if (combo > 9) multiplicador = 2;

        int puntosB = 0;  //Para generalizar casos

        if (desfase <= 0.03f) {     //momento exacto con un rango pequeño para que lo presione
            System.out.println("PERFECT!!!");
            puntosB = 300;
            if (fallo > 0) fallo = 0;

        } else if (desfase <= 0.07f) {      //A esta funcion solo entra cuando no hay fallos
            System.out.println("GREAT!!");
            puntosB = 100;
            if (fallo > 0) fallo = 0;

        } else {
            System.out.println("GOOD");
            puntosB = 35;
        }
        esCombo(true);
        puntaje += (puntosB*multiplicador);
    }

    public void esCombo(boolean acerto){
        if(acerto){ //si se continua con el combo se suma
            combo++;
            return;
        }
        combo=0;        //si no reinicia
    }

    public void notaOlvidada() {
        System.out.println("BAD");
        esCombo(false);
        puntaje -= 50;
        if (puntaje < 0) puntaje = 0;
        numFallos++;    //estadistica
        fallo++;        //vida
    }

    public float getRelacionVida() {
    		return (float)fallo / MAX_FALLOS;
    }

    public float getRelacionTiempo() {
    		return (float)musicaActual.getPosition();
    }
}
