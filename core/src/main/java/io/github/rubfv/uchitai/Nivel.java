package io.github.rubfv.uchitai;
import java.io.Serializable;
import java.util.*;

//Clase Para manejar el Nivel (esta se pretende guardar en un archivo binario)

public class Nivel implements Serializable {
    private  String nombreCancion;
    private String archivoAudio;                    //dirección del .mp3(Propenso a eliminarse)
    //Exclusivo para la hora de guardar, de esta forma siempre se estara all en orden
    private TreeMap<Float,List<Nota>> notas;		//Clave(tiempo s) valor lista de teclas de ese tiempo

    // Todo CONSTRUCTOR (RUBÉN)
    Nivel() {
    		notas = new TreeMap<>();
    }
    
    

    public String getNombreCancion() {
        return nombreCancion;
    }

    public TreeMap<Float,List<Nota>> getNotas() {
        return notas;
    }

    public String getArchivoAudio() {
        return archivoAudio;
    }

    //encargado de realizar la conversion a la estructura de juego
    public HashMap<Integer, Queue<Nota>> estructuraJuego() {
            HashMap<Integer,Queue<Nota>> notasJuego=new HashMap<>();
        //conversión de la estructura de las teclas
        for(List<Nota> listaNotas : notas.values()){
            for(Nota nota: listaNotas){
                Integer tecla=nota.getTecla();    //la tecla que sera nuestra nueva clave

                if(!notasJuego.containsKey(tecla)) {
                    notasJuego.put(tecla, new LinkedList<>());
                }
                notasJuego.get(tecla).add(nota);
            }
        }
        return notasJuego;
    }
}
