package com.maxsavitsky.gameoflife;

import java.util.ResourceBundle;

public class GlobalSettings {

	private static int CELL_SIZE = 14;

	public static int getCellSize() {
		return CELL_SIZE;
	}

	public static void setCellSize(int cellSize) {
		CELL_SIZE = cellSize;
	}

	private static final int STROKE_WIDTH = 1;

	public static int getStrokeWidth() {
		return STROKE_WIDTH;
	}

	private static ResourceBundle propsResourceBundle;

	public static ResourceBundle getPropsResourceBundle() {
		return propsResourceBundle;
	}

	public static void setPropsResourceBundle(ResourceBundle propsResourceBundle) {
		GlobalSettings.propsResourceBundle = propsResourceBundle;
	}

	private GlobalSettings(){}

}
