package com.maxsavitsky.gameoflife.controller;

import com.maxsavitsky.gameoflife.GlobalSettings;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditorController {

	private static Callback callback;

	public static void setCallback(Callback sCallback) {
		EditorController.callback = sCallback;
	}

	private static ArrayList<MainController.LiveCell> startCells;

	public static void setStartCells(List<MainController.LiveCell> startCells) {
		EditorController.startCells = new ArrayList<>(startCells);
	}

	private int cellSize;

	private GraphicsContext gc;

	@FXML
	protected Canvas canvas;

	@FXML
	protected Button okButton;

	private final ArrayList<MainController.LiveCell> cells = new ArrayList<>();

	@FXML
	protected void initialize(){
		cellSize = (int) Math.round(canvas.getWidth() / GlobalSettings.getCellsCount());
		okButton.setOnAction(event -> {
			callback.onPlacementReady(cells);
			((Stage) canvas.getScene().getWindow()).close();
		});

		gc = canvas.getGraphicsContext2D();

		for(var c : startCells){
			cells.add(c);
			drawCell(c.x(), c.y());
		}

		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int i = x / cellSize;
			int j = y / cellSize;
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

	private void drawCell(int i, int j) {
		int x = i * cellSize;
		int y = j * cellSize;
		gc.setFill(Color.BLACK);
		gc.fillRect(x, y, cellSize, cellSize);
	}

	private void clearCell(int i, int j) {
		int x = i * cellSize;
		int y = j * cellSize;
		gc.setFill(Color.WHITE);
		gc.fillRect(x, y, cellSize, cellSize);
	}

	public interface Callback {

		void onPlacementReady(ArrayList<MainController.LiveCell> cells);

	}
}
