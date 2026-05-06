package io.github.rubfv.uchitai;

public class Coord {
	public static final int RESOL_X = 1920, RESOL_Y = 1080;
	public static final float RATIO = 16 / 9;
	public float x, y;
	
	Coord() {
		x = y = 0;
	}
	
	Coord(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
