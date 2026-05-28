package io.github.rubfv.uchitai;
import java.io.Serializable;

import com.badlogic.gdx.Input;

//Clase molde para las notas en general, nota se refiere a la tecla que se debe de presionar
//Serializable para que al momento de guardar en el archivo binario la clase se pueda convertir en un flujo
//de bytes y no de problemas :)
public abstract class Nota implements Serializable {
    protected Integer tecla;               //Key code de la tecla de acuerdo al Libgdx
    protected float tiempoInicio;      //El tiempo/momento en que debe ser presionada segundos
    protected float aparece;     //milisegundos antes de que se deba teclear(para aparecer la tecla)

    // Todo CONSTRUCTOR (RUBÉN)
    Nota(int keycode) {
    		tecla = keycode;
    		tiempoInicio = 0;
    		aparece = 0.1f;
    }
    Nota() {
		tecla = -1;
		tiempoInicio = 0;
		aparece = 0.1f;
}

    public Integer getTecla() {
        return tecla;
    }

    public float getTiempoInicio() {
        return tiempoInicio;
    }

    public boolean isSostenida(){
        return false;
    }

    public static int getKeyboardKeycode(int x, int y) {
    		switch (y) {
    		case 0:
    			switch (x) {
    			case 0:	return Input.Keys.Q;
    			case 1: return Input.Keys.W;
    			case 2:	return Input.Keys.E;
    			case 3: return Input.Keys.R;
    			case 4:	return Input.Keys.T;
    			case 5: return Input.Keys.Y;
    			case 6:	return Input.Keys.U;
    			case 7: return Input.Keys.I;
    			case 8:	return Input.Keys.O;
    			case 9: return Input.Keys.P;
    			}
    			break;
    		case 1:
    			switch (x) {
    			case 0:	return Input.Keys.A;
    			case 1: return Input.Keys.S;
    			case 2:	return Input.Keys.D;
    			case 3: return Input.Keys.F;
    			case 4:	return Input.Keys.G;
    			case 5: return Input.Keys.H;
    			case 6:	return Input.Keys.J;
    			case 7: return Input.Keys.K;
    			case 8:	return Input.Keys.L;
    			}
    			break;
    		case 2:
    			switch (x) {
    			case 0:	return Input.Keys.Z;
    			case 1: return Input.Keys.X;
    			case 2:	return Input.Keys.C;
    			case 3: return Input.Keys.V;
    			case 4:	return Input.Keys.B;
    			case 5: return Input.Keys.N;
    			case 6:	return Input.Keys.M;
    			}
    			break;
    		}
    		return -1;
    }
}
