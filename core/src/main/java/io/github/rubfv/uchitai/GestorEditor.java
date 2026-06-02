package io.github.rubfv.uchitai;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class GestorEditor extends Gestor {
	private final float RANGO_JUNTO = 0.09f;

	GestorEditor(Nivel level, CancionesCargadas canciones) {
		super(level, canciones);
	}

    public void procesarTeclaPresionada(int keycode) { 
		TreeMap<Float, List<Nota>> notasCargadas = nivelAct.getNotas();
		ArrayList<Nota> nota = new ArrayList<>();
		Entry<Float, List<Nota>> e = null;
		float mayorTiempo = -1989;

		System.out.println("NOTA: " + (char)(keycode + 36));
		
		//Obtener el último tiempo
		if (!notasCargadas.isEmpty()) {
			e = notasCargadas.lowerEntry(tiempoAct);
			//Revisa que sí exista una cochinada de estas
			if (e != null) {
				mayorTiempo = e.getKey();
			}
			//busca tiempos superiores, por si acaso
			else {
				e = notasCargadas.ceilingEntry(tiempoAct);
				if (e != null) {
    				mayorTiempo = e.getKey();
				}
			}
		}
		
		//Se junta al grupo de notas más cercana
		if (tiempoAct -RANGO_JUNTO <= mayorTiempo || mayorTiempo >= tiempoAct + RANGO_JUNTO) {
			nivelAct.getNotas().get(mayorTiempo).add(new NotaSimple(keycode, mayorTiempo));  
		}
		//Se genera un nuevo grupo de notas
		else {
			if (nivelAct.getNotas().get(tiempoAct) == null) {
    			nota.add(new NotaSimple(keycode, tiempoAct));
    			nivelAct.getNotas().put(tiempoAct, nota);
			}
			else {
    			nivelAct.getNotas().get(tiempoAct).add(new NotaSimple(keycode, tiempoAct));  
			}
		}
    }
    
    @Override
    public boolean guardar() {
		String ruta = rutaCancion + "/" + nombreCancion + ".dat";
		FileHandle archivo = Gdx.files.local(ruta);
		
		//Escribir el objeto a memoria
    		try (ObjectOutputStream flujoObjeto = new ObjectOutputStream(archivo.write(false))) {
    			flujoObjeto.writeObject(nivelAct);
    			System.out.println("Se guerdo correctamente el mapa");
    		}
    		catch (IOException e) {
    			System.out.println("Error con guardar el mapa del nivel, lol");
    			e.printStackTrace();
    			return false;
        }
    		return true;
    }
    
    @Override
    public void iniciar() {
        if (musicaActual != null) {
            tiempoAct = 0;
            musicaActual.stop();
            musicaActual.play();
            musicaActual.setVolume(1);
            juegoEstado = true;
        }
    }

    public boolean actualizar() { 
    		tiempoAct = musicaActual.getPosition();
    		
    		return false;
    	}
}
