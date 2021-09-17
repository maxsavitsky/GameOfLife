package com.maxsavitsky.gameoflife;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

	public static class LiveCell {
		private int x, y;

		public LiveCell(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LiveCell liveCell = (LiveCell) o;
			return x == liveCell.x && y == liveCell.y;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y);
		}
	}

	private static class Action{
		protected final int action;
		protected final LiveCell cell;

		public Action(int action, LiveCell cell) {
			this.action = action;
			this.cell = cell;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Action action1 = (Action) o;
			return action == action1.action && Objects.equals(cell, action1.cell);
		}

		@Override
		public int hashCode() {
			return Objects.hash(action, cell);
		}
	}

	@FXML
	private Canvas canvas;
	private GraphicsContext gc;

	@FXML
	private Button placeButton, startButton;

	private ArrayList<LiveCell> startCells = new ArrayList<>();

	private int cellsCount = 50;
	private double cellSize;
	private final double strokeWidth = 1;

	private ArrayList<LiveCell> liveCells = new ArrayList<>();
	private HashMap<Integer, LiveCell> map = new HashMap<>(cellsCount * cellsCount + cellsCount);

	private Timer timer;

	@FXML
	public void initialize() {
		cellSize = (canvas.getWidth()) / cellsCount;
		gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(strokeWidth);

		placeButton.setOnMouseClicked(event -> {
			stop();
			PlacementController.cellsCount = cellsCount;
			PlacementController.cellsSize = cellSize;
			PlacementController.sPlacementCallback = cells -> {
				startCells = cells;
				reset();
			};
			FXMLLoader loader = new FXMLLoader(Application.class.getResource("placement-view.fxml"));
			Stage stage = new Stage();
			try {
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		init();
	}

	private void init() {
		liveCells = new ArrayList<>(startCells);
		map.clear();
		for(var c : liveCells){
			map.put(getIdForCell(c.getX(), c.getY()), c);
		}

		drawCells();
	}

	private final int[] dx = {1, 0, -1,  0, 1, -1,  1, -1};
	private final int[] dy = {0, 1,  0, -1, 1,  1, -1, -1};

	private void fetch(){
		System.out.println("old size " + liveCells.size());
		HashSet<Action> pending = new HashSet<>();
		for(var c : liveCells){
			int n = calculateNeighboursCount(c.getX(), c.getY());
			if(n <= 1 || n >= 4){
				pending.add(new Action(0, c));
			}
			System.out.println("looking neighbours for " + c.getX() + " " + c.getY());
			for(int i = 0; i < 8; i++){
				int nx = c.getX() + dx[i], ny = c.getY() + dy[i];
				if(nx >= 0 && nx < cellsCount && ny >= 0 && ny < cellsCount) {
					if(!map.containsKey(getIdForCell(nx, ny))){
						n = calculateNeighboursCount(nx, ny);
						System.out.println("neighbour " + nx + " " + ny + " " + n);
						if(n == 3)
							pending.add(new Action(1, new LiveCell(nx, ny)));
					}
				}
			}
		}
		for(var a : pending){
			var c = a.cell;
			if(a.action == 0){
				liveCells.remove(c);
				map.remove(getIdForCell(c.getX(), c.getY()));
				clearCell(c.getX(), c.getY());
			}else{
				liveCells.add(c);
				map.put(getIdForCell(c.getX(), c.getY()), c);
				drawCell(c.getX(), c.getY());
			}
		}
		System.out.println("new size " + liveCells.size());
		//drawCells();
	}

	private int calculateNeighboursCount(int i, int j){
		int cnt = 0;
		for(int k = 0; k < 8; k++){
			int nx = i + dx[k], ny = j + dy[k];
			if(nx >= 0 && nx < cellsCount && ny >= 0 && ny < cellsCount){
				if(map.containsKey(getIdForCell(nx, ny)))
					cnt++;
			}
		}
		return cnt;
	}

	private int getIdForCell(int i, int j){
		return i * cellsCount + j;
	}

	private void drawCells() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		for (var c : liveCells) {
			drawCell(c.getX(), c.getY());
		}
	}

	private void drawCell(int i, int j) {
		double x = i * cellSize;// - i * strokeWidth;
		double y = j * cellSize;// - j * strokeWidth;
		gc.setFill(Color.BLACK);
		gc.fillRect(x, y, cellSize, cellSize);
	}

	private void clearCell(int i, int j){
		double x = i * cellSize;
		double y = j * cellSize;
		gc.clearRect(x, y, cellSize, cellSize);
	}

	@FXML
	protected void start(){
		stop();
		timer = new Timer();
		System.out.println("start");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				fetch();
			}
		}, 200, 200);
	}

	@FXML
	protected void stop(){
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}

	@FXML
	protected void reset(){
		stop();
		init();
	}

}