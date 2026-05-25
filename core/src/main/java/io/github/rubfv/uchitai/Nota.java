package io.github.rubfv.uchitai;
import java.io.Serializable;

//Clase molde para las notas en general, nota se refiere a la tecla que se debe de presionar
//Serializable para que al momento de guardar en el archivo binario la clase se pueda convertir en un flujo
//de bytes y no de problemas :)
public abstract class Nota implements Serializable {
    protected Integer tecla;               //Key code de la tecla de acuerdo al Libgdx
    protected long tiempoInicio;      //El tiempo/momento en que debe ser presionada
    protected int aparece=5000;     //milisegundos antes de que se deba teclear(para aparecer la tecla)

    // Todo CONSTRUCTOR (RUBÉN)


    public Integer getTecla() {
        return tecla;
    }

    public long getTiempoInicio() {
        return tiempoInicio;
    }

    public boolean isSostenida(){
        return false;
    }

}
