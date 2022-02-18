package com.maxsavitsky.gameoflife.controller;

import com.maxsavitsky.gameoflife.GameOfLifeApplication;
import com.maxsavitsky.gameoflife.GlobalSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {

	public record LiveCell(int x, int y) {
	}

	public enum ActionType {
		REMOVE_CELL,
		ADD_CELL
	}

	private record Action(ActionType action, LiveCell cell) {
	}

	@FXML
	private Canvas canvas;
	private GraphicsContext gc;

	@FXML
	private Button placeButton;

	@FXML
	private Label statusLabel;

	private ArrayList<LiveCell> startCells = new ArrayList<>();

	private static final int CELL_SIZE = GlobalSettings.getCellSize();

	private int rowCount;
	private int colCount;

	private ArrayList<LiveCell> liveCells = new ArrayList<>();
	private HashMap<Integer, LiveCell> map;

	private Timer timer;

	private int generation = 1;

	@FXML
	public void initialize() {
		rowCount = (int)(canvas.getHeight() / CELL_SIZE);
		colCount = (int)(canvas.getWidth() / CELL_SIZE);

		map = new HashMap<>((rowCount + 1) * (colCount + 1));

		gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(GlobalSettings.getStrokeWidth());

		placeButton.setOnMouseClicked(event -> {
			stop();
			EditorController.setCallback(cells -> {
				startCells = cells;
				reset();
			});
			EditorController.setStartCells(liveCells);
			FXMLLoader loader = new FXMLLoader(GameOfLifeApplication.class.getResource("placement-view.fxml"));
			Stage stage = new Stage();
			try {
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);
				stage.setResizable(false);
				stage.setTitle("Editor");
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
		for (var c : liveCells) {
			map.put(getIdForCell(c.x(), c.y()), c);
		}

		drawCells();

		generation = 1;

		statusLabel.setText(
				"Generation: %d%nCount: %d"
						.formatted(generation, liveCells.size())
		);
	}

	private final int[] dx = {1, 0, -1, 0, 1, -1, 1, -1};
	private final int[] dy = {0, 1, 0, -1, 1, 1, -1, -1};

	private void fetch() {
		HashSet<Action> pending = getPendingActions();
		if(pending.isEmpty()){
			stop();
			return;
		}
		for (var a : pending) {
			var c = a.cell;
			if (a.action() == ActionType.REMOVE_CELL) {
				liveCells.remove(c);
				map.remove(getIdForCell(c.x(), c.y()));
				clearCell(c.x(), c.y());
			} else {
				liveCells.add(c);
				map.put(getIdForCell(c.x(), c.y()), c);
				drawCell(c.x(), c.y());
			}
		}

		generation++;

		Platform.runLater(()->statusLabel.setText(
				"Generation: %d%nCount: %d"
						.formatted(generation, liveCells.size())
		));
	}

	private HashSet<Action> getPendingActions(){
		HashSet<Action> pending = new HashSet<>();
		for (var c : liveCells) {
			int n = calculateNeighboursCount(c.x(), c.y());
			if (n <= 1 || n >= 4) {
				pending.add(new Action(ActionType.REMOVE_CELL, c));
			}
			for (int i = 0; i < 8; i++) {
				int nx = c.x() + dx[i];
				int ny = c.y() + dy[i];
				if (nx >= 0 && nx < colCount
						&& ny >= 0 && ny < rowCount
						&& !map.containsKey(getIdForCell(nx, ny))) {
					n = calculateNeighboursCount(nx, ny);
					if (n == 3)
						pending.add(new Action(ActionType.ADD_CELL, new LiveCell(nx, ny)));
				}
			}
		}
		return pending;
	}

	private int calculateNeighboursCount(int i, int j) {
		int cnt = 0;
		for (int k = 0; k < 8; k++) {
			int nx = i + dx[k];
			int ny = j + dy[k];
			if (nx >= 0 && nx < colCount
					&& ny >= 0 && ny < rowCount
					&& map.containsKey(getIdForCell(nx, ny))) {
				cnt++;
			}
		}
		return cnt;
	}

	private int getIdForCell(int i, int j) {
		return i * colCount + j;
	}

	private void drawCells() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		for (var c : liveCells) {
			drawCell(c.x(), c.y());
		}
	}

	private void drawCell(int i, int j) {
		int x = i * CELL_SIZE;
		int y = j * CELL_SIZE;
		gc.setFill(Color.BLACK);
		gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
	}

	private void clearCell(int i, int j) {
		int x = i * CELL_SIZE;
		int y = j * CELL_SIZE;
		gc.setFill(Color.WHITE);
		gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
	}

	@FXML
	protected void start() {
		stop();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				fetch();
			}
		}, 200, 200);
	}

	@FXML
	protected void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@FXML
	protected void reset() {
		stop();
		init();
	}

	@FXML
	protected void clear(){
		stop();
		startCells = new ArrayList<>();
		init();
	}

}