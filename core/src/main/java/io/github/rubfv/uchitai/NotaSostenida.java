package io.github.rubfv.uchitai;



//Aquellas teclas que duran una x cantidad de milisegundos presionadas
public class NotaSostenida extends Nota{
    private long tiempoFinal;       //Momento cuando se debe soltar la tecla
    private boolean inicioAcertado;


    // Todo CONSTRUCTOR (RUBÉN)


    public long getTiempoFinal() {
        return tiempoFinal;
    }

    @Override
    public boolean isSostenida(){
        return true;
    }

    public void setInicioAcertado(boolean inicioAcertado) {
        this.inicioAcertado = inicioAcertado;
    }

    public boolean isInicioAcertado() {
        return inicioAcertado;
    }
}
