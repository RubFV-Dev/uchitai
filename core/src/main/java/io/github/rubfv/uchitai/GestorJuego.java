package io.github.rubfv.uchitai;
import com.badlogic.gdx.audio.Music;
import java.util.*;


//Encargado de manejar la lógica del juego.
public class GestorJuego {
	
	protected final int MAX_FALLOS = 15;
	
    protected Nivel nivelAct;             //como tal all the level
    //Mapa (Keycode, cola de tiempos por tecla) Reordenar para separar por teclas
    //de esta forma sera más facil de manejar
    protected HashMap<Integer,Queue<Nota>> notasTecla;
    protected HashMap<Integer,NotaSostenida> sostenidasProceso;   //para tener un lugar para guarda las notas que
                                                    // sabemos que estan sostenidas en ese momento
    protected Music musicaActual;
    protected float tiempoAct;          //A emplear el calculo de la musica Perdon no me acuerdo, Este es nuestro reloj interno
    protected boolean juegoEstado;          //pa saber si esta en pausa o no (pendiente de confirmar)
    protected int puntaje;
    protected int combo;                  //El combo es un multiplicador de los puntos
    protected int numFallos;              //contador de fallos
    protected float margenError= 0.1f;      //los milisegundos que estan permitidos para desfasarse del presionar
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
        notasTecla=level.estructuraJuego();
        sostenidasProceso=new HashMap<>();
        musicaActual = musica;
    }

    // NOTA: Tecnicamente se usa polleo en cada render de las cosas por asi decirlo

    //Metodo que manejara las teclas que se presionen
    public void procesarTeclaPresionada(int keycode){
        //Plan: Al presionar la tecla en tiempo de juego se llama a este metodo mandandole el keycode
        // de la tecla, posteriormente el metodo buscara la clave en el mapa y verificara si al inicio
        // de la cola de esa tecla esta el tiempo en que se esta presionando, de ahi se manda a llamar
        // a los encargados del puntaje.
        if(!juegoEstado) return;

        // Primer verificación: Si esa nota debe presionarse en este nivel
        // o esa tecla ya no tiene más momentos en esa cancion
        if(!notasTecla.containsKey(keycode) || notasTecla.get(keycode).isEmpty()){
            //Ps esa no era
            notaOlvidada();
            return;
        }

        // Ya que comprobamos que la tecla si puede ser presionada pasamos a checar si si debia ser presionada
        Nota notaProx =notasTecla.get(keycode).peek();

        float tempoAct= tiempoAct;
        float desfase= Math.abs(notaProx.getTiempoInicio()- tempoAct);     //todo CONSTRUCTOR EN NOTA

        if(desfase <= margenError){  //para notas que se presionan correctamente
            if(notaProx.isSostenida()){
                NotaSostenida notitaSos = (NotaSostenida) notaProx;
                notitaSos.setInicioAcertado(true);
                sostenidasProceso.put(keycode,notitaSos);
                incrementarPuntaje(desfase,true);
            }
            else{
                incrementarPuntaje(desfase,false);
                // la nota ya termino, la sacamos de la cola
                notasTecla.get(keycode).poll();
                acertada++;
            }
        }
        else if(tiempoAct < notaProx.getTiempoInicio()-margenError){
            // se presiona antes de tiempo
            notaOlvidada();     //para que le de el fallo
        }

    }

    public void procesarTeclaArriba (int keycode){
         if(!juegoEstado) return;

        NotaSostenida notaProx= sostenidasProceso.remove(keycode);
         //checar si es una de las notas que deberian de ser sostenidas
        if(notaProx==null){
            //Ps no nos interesa
            return;
        }

        float tempoAct = tiempoAct; // para no modificar el temporizador
        if(notaProx.isInicioAcertado()){    //si inicio cuando era debido
            float desfase= Math.abs(tempoAct-notaProx.getTiempoFinal());
            if(desfase<=margenError){
                incrementarPuntaje(desfase, true);  //para que le de la otra mitad de puntos
                acertada++;

            }
            else {
                esCombo(false);
                medioFallo++;       //estadistica
                fallo++;            //vida
                puntaje-=25;    //para no quitarle los 50 ya que fue un error parcial
                if (puntaje < 0) puntaje = 0;
            }
            //verificacion por si acaso
            if(!notasTecla.isEmpty() && notasTecla !=null) {
                notasTecla.get(keycode).poll();     // así se libera de su cola
            }
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

        //recorremos cada tecla y checamos el frente de su cola
        for(Queue<Nota> cola : notasTecla.values()){
            if(!cola.isEmpty() && (cola.peek().getTiempoInicio() + margenError ) < tiempoAct){
                // Pues ya se paso, por ende ya no entra
                Nota notaOlvidada= cola.peek();
                //si es sostenida la sacamos del set
                if(notaOlvidada.isSostenida()){
                    sostenidasProceso.remove(notaOlvidada.getTecla());
                }
                notaOlvidada();

                cola.poll();    // Se saca
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

    public void incrementarPuntaje(float desfase, boolean isInicioSos) {  //mas que nada cosas proporcionales
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
        if (isInicioSos) {    //cuando solo es el inicio de la sostenida dar la mitad, lo otro sera ya que acabe
            puntosB/=2;
        }
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
