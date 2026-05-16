package io.github.rubfv.uchitai;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import io.github.rubfv.uchitai.UchitaiGame;

public class InputGeneral extends InputAdapter {
	
	private PANTALLA pantallaAct;
	private Coord mouse;
	
	InputGeneral(PANTALLA p, Coord m) {
		super();
		pantallaAct = p;
		mouse = m;
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
	public boolean mouseMoved(int screenX, int screenY) {
		ajusteMouse(screenX, screenY);
		return false;
	}
	
	@Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		UchitaiGame juego = (UchitaiGame)Gdx.app.getApplicationListener();
		ajusteMouse(screenX, screenY);
		
		switch (button) {
		case Input.Buttons.LEFT:
			switch(juego.getPantallaAct()) {
			case TITULO:
				juego.setPantallaAct(PANTALLA.SELECCION);
				return true;
			case SELECCION:
				//Detectar selección anterior canción
				if (mouse.dentroDe(180, Coord.RESOL_X / 2 - 255, 30, 180)) {
					System.out.println(true);
					juego.getCanciones().antCancion();
				}
				//Detectar selección aiguiente canción
				else if (mouse.dentroDe(Coord.RESOL_X / 2 + 255, Coord.RESOL_X - 180, 30, 180)) {
					System.out.println(true);
					juego.getCanciones().sigCancion();
				}
				//Botón inicio canción
				else if (mouse.dentroDe(Coord.RESOL_X / 2 - 30, Coord.RESOL_X / 2 + 30, 75, 135)) {
					juego.setPantallaAct(PANTALLA.JUEGO);
				}
				//Botón edición mapa
				else if (mouse.dentroDe(Coord.RESOL_X - 205, Coord.RESOL_X - 95, 200, 300)) {
					juego.setPantallaAct(PANTALLA.EDICION);
				}
				//Botón canción aleatoria
				else if (mouse.dentroDe(95, 205, 200, 300)) {
					DibujadoSeleccion dibujado = (DibujadoSeleccion)juego.getDibujado();
					Random r = new Random();
					int indiceOrg = juego.getCanciones().getIndiceCancion();
					int indiceAct = -1;
					
					//Nuevo índice aleatorio
					do {
						indiceAct = r.nextInt(juego.getCanciones().size());
					} while (indiceAct == indiceOrg);
					
					//Ajuste a la animación de cambio de canción
					//El nuevo índice está más a la izquierda
					if (indiceOrg > indiceAct) {
						for (int i = indiceOrg - indiceAct; i > 0; i--) {
							dibujado.anim.animDeslizarIzq();
						}
					}
					//Está pa la derecha
					else {
						for (int i = indiceAct - indiceOrg; i > 0; i--) {
							dibujado.anim.animDeslizarDer();
						}
					}
					
					//Cargar la nueva canción aleatoria
					juego.getCanciones().cargarCancion(indiceAct);
				}
				else {
					System.out.println("MOUSE: " + mouse.x + "\t" + mouse.y);
					System.out.println("SCRR: " + Gdx.graphics.getWidth() + "\t" + Gdx.graphics.getHeight());
				}
				return true;
			}
			break;
		}

        return false;
    }
	
    public void ajusteMouse(int screenX, int screenY) {
		Coord scale = new Coord(), ajuste = new Coord();
		
		scale.y = ((float)Coord.RESOL_Y / (float)Gdx.graphics.getHeight());
		scale.x = ((float)Coord.RESOL_X / (float)Gdx.graphics.getWidth());
		
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
		mouse.setCoord(screenX, screenY);
    }
}
