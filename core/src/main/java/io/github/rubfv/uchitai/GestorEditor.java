package io.github.rubfv.uchitai;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GestorEditor extends Gestor {
	private final float RANGO_JUNTO = 0.09f;
	private final float MAX_BRINCO = 15;
	int totalNotas;
	float tiempoBrinco;

	GestorEditor(Nivel level, CancionesCargadas canciones) {
		super(level, canciones);
		totalNotas = 0;
		tiempoBrinco = 3;
		
		for (List<Nota> lista: level.getNotas().values()) {
			for (Nota n: lista) {
				totalNotas++;
			}
		}
		
		nivelAct = level;
		Nota.setAparicion(TIEMPO_APARICION);
	}

    public TreeMap<Float,List<Nota>> getNotas() {
        return nivelAct.getNotas();
    }
    
    public SortedMap<Float,List<Nota>> getNotas(float min, float max){
        return nivelAct.getNotas().headMap(min + max);
    }
    
    public int getTotalNotas() {
    		return totalNotas;
    }

    public void procesarTeclaPresionada(int keycode, boolean borrar) { 
		TreeMap<Float, List<Nota>> notasCargadas = nivelAct.getNotas();
		ArrayList<Nota> nota = new ArrayList<>();
		Entry<Float, List<Nota>> e = null;
		float mayorTiempo = -1989, rango;

		System.out.println("NOTA: " + (char)(keycode + 36) + "\t" +borrar);
		
		if (borrar) rango = TIEMPO_APARICION;
		else			rango = RANGO_JUNTO;
		
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
		if (tiempoAct - rango <= mayorTiempo && tiempoAct > mayorTiempo - rango) {
			//Sin repeticiones
			for (Nota a: nivelAct.getNotas().get(mayorTiempo)) {
				if (a.getTecla() == keycode) {
					//Se borra si es que presiona shift
					if (borrar) {
						nivelAct.getNotas().get(mayorTiempo).remove(a);
						totalNotas--;
					}
					return;		//Hay repetición, se acaba el mundo
				}
			}
			if (!borrar) {
				nivelAct.getNotas().get(mayorTiempo).add(new NotaSimple(keycode, mayorTiempo));  
			}
		}
		//Se genera un nuevo grupo de notas
		else if (!borrar) {
			if (nivelAct.getNotas().get(tiempoAct) == null) {
    				nota.add(new NotaSimple(keycode, tiempoAct));
    				nivelAct.getNotas().put(tiempoAct, nota);
			}
			else {
				//Sin repeticiones
				for (Nota a: nivelAct.getNotas().get(tiempoAct)) {
					if (a.getTecla() == keycode) {
						return;		//Hay repetición, se acaba el mundo
					}
				}
    				nivelAct.getNotas().get(tiempoAct).add(new NotaSimple(keycode, tiempoAct));  
			}
		}
		
		if (!borrar) totalNotas++;
    }
    
    @Override
    public void guardar() {
		String ruta = rutaCancion + "/" + nombreCancion;
		FileHandle archivo = Gdx.files.local(ruta + ".dat");
		
		//Escribir el objeto a memoria
		if (totalNotas != 0) {
			//Si hay listas vacías, se borran para no extender la canción sin querer
			try {
				for (Entry<Float, List<Nota>> inp: nivelAct.getNotas().entrySet()) {
					if (inp.getValue().isEmpty()) {
						nivelAct.getNotas().remove(inp.getKey());
					}
				}
			}
			catch(ConcurrentModificationException ex) {
				System.out.println("Error al buscar notas vacias");
				ex.printStackTrace();
			}
	    		try (ObjectOutputStream flujoObjeto = new ObjectOutputStream(archivo.write(false))) {
	    			flujoObjeto.writeObject(nivelAct);
	    			System.out.println("Se guerdo correctamente el mapa");
	    		}
	    		catch (IOException e) {
	    			System.out.println("Error con guardar el mapa del nivel, lol");
	    			e.printStackTrace();
	        }
		}
		//No hay nada que escribir, se borra el original si hay
		else if (archivo.exists()){
			archivo.delete();
		}
    }
    
    public void retroceder() {
    		boolean pausa = !musicaActual.isPlaying();
    		if (pausa) musicaActual.play();
    		if (tiempoAct - tiempoBrinco < 0) {
        		musicaActual.setPosition(0);
    		}
    		else {
        		musicaActual.setPosition(tiempoAct - tiempoBrinco);
    		}
    		if (pausa) musicaActual.pause();
    }

    public void avanzar() {
		boolean pausa = !musicaActual.isPlaying();
		if (pausa) musicaActual.play();
    		musicaActual.setPosition(tiempoAct + tiempoBrinco);
    		if (pausa) musicaActual.pause();
    }
    
    public void aumentarBrinco() {
    		//tiempo preciso
    		if (tiempoBrinco < 1) {
    			tiempoBrinco += .1;
    		}
    		else {
    			tiempoBrinco++;
    		}
    		if (tiempoBrinco > MAX_BRINCO) tiempoBrinco = MAX_BRINCO;
    }
    
    public void reducirBrinco() {
    		//Precisión beba
		if (tiempoBrinco <= 1) {
			tiempoBrinco -= .1;
			if (tiempoBrinco < .1) {
				tiempoBrinco = .1f;
			}
		}
		else {
			tiempoBrinco -= 1;
		}
    }
    
    public float relacionBrinco() {
    		return tiempoBrinco / MAX_BRINCO;
    }
    
    public void ajustarTiempo(float ratio) {
		boolean pausa = !musicaActual.isPlaying();
		if (pausa) musicaActual.play();
    		musicaActual.setPosition((nivelAct.getNotas().lastKey() * ratio));
    		if (pausa) musicaActual.pause();
    }

    public float getRelacionTiempo() {
    		if (!nivelAct.getNotas().isEmpty()) {
        		float r = musicaActual.getPosition() / nivelAct.getNotas().lastKey();
        		if (r > 1f) return 1f;
        		return r;
    		}
    		return 1;
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
