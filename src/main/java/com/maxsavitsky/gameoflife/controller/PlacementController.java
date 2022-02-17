package com.maxsavitsky.gameoflife.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;

public class PlacementController {

	private static double cellsSize;
	private static Callback callback;

	public static void setCellsSize(double cellsSize) {
		PlacementController.cellsSize = cellsSize;
	}

	public static void setPlacementCallback(Callback sCallback) {
		PlacementController.callback = sCallback;
	}

	@FXML
	protected Canvas canvas;

	@FXML
	protected Button okButton;

	private final ArrayList<MainController.LiveCell> cells = new ArrayList<>();

	@FXML
	protected void initialize(){
		okButton.setOnAction(event -> {
			callback.onPlacementReady(cells);
			((Stage) canvas.getScene().getWindow()).close();
		});

		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int i = (int) (x / cellsSize);
			int j = (int) (y / cellsSize);
			int index = -1;
			for (int k = 0; k < cells.size(); k++) {
				MainController.LiveCell c = cells.get(k);
				if (c.x() == i && c.y() == j) {
					index = k;
					break;
				}
			}
			if(index != -1){
				clearCell(i, j);
				cells.remove(index);
			}else {
				cells.add(new MainController.LiveCell(i, j));
				drawCell(i, j);
			}
		});
	}

	private void drawCell(int i, int j){
		canvas.getGraphicsContext2D().fillRect(i * cellsSize, j * cellsSize, cellsSize, cellsSize);
	}

	private void clearCell(int i, int j){
		canvas.getGraphicsContext2D().clearRect(i * cellsSize, j * cellsSize, cellsSize, cellsSize);
	}

	public interface Callback {

		void onPlacementReady(ArrayList<MainController.LiveCell> cells);

	}
}
