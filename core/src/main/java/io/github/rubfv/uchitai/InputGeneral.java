package io.github.rubfv.uchitai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import io.github.rubfv.uchitai.UchitaiGame;

public class InputGeneral extends InputAdapter {
	
	private PANTALLA pantallaAct;
	
	InputGeneral(PANTALLA p){
		super();
		pantallaAct = p;
	}
	
	public void setPantallaAct(PANTALLA pantalla) {
		pantallaAct = pantalla;
	}
	
	//Esta función SIEMPRE se llamará al presionar una tecla
	@Override
	public boolean keyDown(int keycode) {
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
		
		switch (juego.getPantallaAct()) {
		case SELECCION:	
			if (keycode == Input.Keys.LEFT) {
				int indice = juego.getCanciones().getIndiceCancion();
				
				if (indice - 1 >= 0) {
					juego.getCanciones().cargarCancion(indice - 1);
					DibujadoGeneral.cargarFondoCancion(juego.getCanciones().getSprite());
					
					if (juego.getDibujado() instanceof DibujadoSeleccion) {
						DibujadoSeleccion sel = (DibujadoSeleccion)juego.getDibujado();
						
						sel.recargarTexturas();
					}
				}
			}
			if (keycode == Input.Keys.RIGHT) {
				int indice = juego.getCanciones().getIndiceCancion();
				
				if (indice + 1 < juego.getCanciones().size()) {
					juego.getCanciones().cargarCancion(indice + 1);
					DibujadoGeneral.cargarFondoCancion(juego.getCanciones().getSprite());
					
					if (juego.getDibujado() instanceof DibujadoSeleccion) {
						DibujadoSeleccion sel = (DibujadoSeleccion)juego.getDibujado();
						
						sel.recargarTexturas();
					}
				}
			}
			break;
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		
		return false;
	}
	
	@Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
		
		switch (button) {
		case Input.Buttons.LEFT:
			switch(juego.getPantallaAct()) {
			case TITULO:
				juego.setPantallaAct(PANTALLA.SELECCION);
				return true;
			}
			break;
		}

        return false;
    }
}
