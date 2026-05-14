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
				juego.getCanciones().antCancion();
			}
			if (keycode == Input.Keys.RIGHT) {
				juego.getCanciones().sigCancion();
			}
			break;
		}
		
		if (keycode == Input.Keys.ESCAPE) {
			Gdx.app.exit();
		}
		
		//para conocer las canciones actualmente cargadas
		if (keycode == Input.Keys.ALT_LEFT) {
			for (int i = 0; i < juego.getCanciones().size(); i++) {
				System.out.println(juego.getCanciones().nombreCancion(i));
			}
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
		Coord scale = new Coord(), ajuste = new Coord();
		scale.y = ((float)Coord.RESOL_Y / (float)Gdx.graphics.getHeight());
		scale.x = ((float)Coord.RESOL_X / (float)Gdx.graphics.getWidth());
		
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
		
		ajuste.x = (Gdx.graphics.getWidth() * scale.y - Gdx.graphics.getWidth() * scale.x) / 2;
		ajuste.y = (Gdx.graphics.getHeight() * scale.x - Gdx.graphics.getHeight() * scale.y) / 2;
		
		//Ajuste de resolución porque es un dolor de cocos
		if (ajuste.x > 0) {
			screenX *= scale.y;
			screenX -= ajuste.x;
		}
		else {
			screenX *= scale.x;
		}
		if (ajuste.y > 0) {
			screenY *= scale.x;
			screenY -= ajuste.y;
		}
		else {
			screenY *= scale.y;
		}
		screenY = Coord.RESOL_Y - screenY;
		
		switch (button) {
		case Input.Buttons.LEFT:
			switch(juego.getPantallaAct()) {
			case TITULO:
				juego.setPantallaAct(PANTALLA.SELECCION);
				return true;
			case SELECCION:
				//Detectar botón anterior canción
				if (screenX >= 180 && screenX < Coord.RESOL_X / 2 - 255  &&
					screenY >= 30 && screenY < 180) {
					System.out.println(true);
					juego.getCanciones().antCancion();
				}
				else if (screenX >= Coord.RESOL_X / 2 + 255 && screenX < Coord.RESOL_X - 180 &&
						screenY >= 30 && screenY < 180) {
					System.out.println(true);
					juego.getCanciones().sigCancion();
				}
				else {
					System.out.println("MOUSE: " + screenX + "\t" + screenY);
					System.out.println("SCRR: " + Gdx.graphics.getWidth() + "\t" + Gdx.graphics.getHeight());
					System.out.println("SCAL: " + scale.x + "\t" + scale.y);
				}
				return true;
			}
			break;
		}

        return false;
    }
}
