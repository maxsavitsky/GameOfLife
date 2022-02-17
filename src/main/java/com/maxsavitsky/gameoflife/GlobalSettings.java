package com.maxsavitsky.gameoflife;

public class GlobalSettings {

	private static final int CELLS_COUNT = 50;

	public static int getCellsCount() {
		return CELLS_COUNT;
	}

	private static final int STROKE_WIDTH = 10;

	public static int getStrokeWidth() {
		return STROKE_WIDTH;
	}

	private GlobalSettings(){}

}
