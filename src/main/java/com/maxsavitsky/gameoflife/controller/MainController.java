package com.maxsavitsky.gameoflife.controller;

import com.maxsavitsky.gameoflife.GameOfLifeApplication;
import com.maxsavitsky.gameoflife.GlobalSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainController {

	public record LiveCell(int x, int y) {
		public String convertToString(){
			return x + ":" + y;
		}
	}

	public enum ActionType {
		REMOVE_CELL,
		ADD_CELL
	}

	private record Action(ActionType action, LiveCell cell) {
	}

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
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

		Platform.runLater(()->{
			gc.setFill(Color.BLACK);
			gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
		});

		placeButton.setOnMouseClicked(event -> {
			stop();
			EditorController.setCallback(cells -> {
				startCells = cells;
				reset();
			});
			EditorController.setStartCells(liveCells);
			FXMLLoader loader = new FXMLLoader(GameOfLifeApplication.class.getResource("placement-view.fxml"));
			loader.setResources(ResourceBundle.getBundle("com.maxsavitsky.gameoflife.constants", Locale.getDefault()));
			Stage placementStage = new Stage();
			try {
				Scene scene = new Scene(loader.load());
				placementStage.setScene(scene);
				placementStage.setResizable(false);
				placementStage.setTitle("Editor");
				placementStage.show();
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

	@FXML
	protected void importConf(){
		stop();
		FileChooser fileChooser = new FileChooser();
		fileChooser
				.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
		File file = fileChooser.showOpenDialog(stage);
		if(file != null){
			importFromFile(file);
		}
	}

	private void importFromFile(File file){
		HashSet<LiveCell> cells = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String l = reader.readLine();
			if(l == null || !isInteger(l)){
				displayError("Parse error: Expected cells count on line 1", null);
				return;
			}
			int n = Integer.parseInt(l);
			for(int i = 0; i < n; i++){
				l = reader.readLine();
				final String expectedCoordinateParseError = "Parse error: Expected cell coordinate on line " + (i + 2);
				if(l == null){
					displayError(expectedCoordinateParseError, null);
					return;
				}
				LiveCell cell = parseCell(l);
				if(cell == null){
					displayError(expectedCoordinateParseError, null);
					return;
				}
				cells.add(cell);
			}
		} catch (IOException e) {
			e.printStackTrace();
			displayError("Parse error: Failed to open file", e.getMessage());
			return;
		}

		startCells = new ArrayList<>(cells);
		init();
	}

	private boolean isInteger(String s){
		Pattern pattern = Pattern.compile("^[0-9]+$");
		return pattern.matcher(s).matches();
	}

	private LiveCell parseCell(String s){
		String[] parts = s.split(":");
		if(parts.length != 2){
			return null;
		}
		int x;
		int y;
		try{
			x = Integer.parseInt(parts[0]);
			y = Integer.parseInt(parts[1]);
		}catch (NumberFormatException e){
			return null;
		}
		return new LiveCell(x, y);
	}

	@FXML
	private void exportConf(){
		stop();
		FileChooser fileChooser = new FileChooser();
		fileChooser
				.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
		File file = fileChooser.showSaveDialog(stage);
		if(file != null)
			exportToFile(file);
	}

	private void exportToFile(File file){
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))){
			writer.write(String.valueOf(liveCells.size()));
			writer.newLine();
			for(var c : liveCells){
				writer.write(c.convertToString());
				writer.newLine();
			}
		}catch (IOException e){
			e.printStackTrace();
			displayError("Error: Failed to open file", e.getMessage());
			return;
		}

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText("Successfully");
		alert.show();
	}

	private void displayError(String message, String subtext){
		Alert alert = new Alert(Alert.AlertType.ERROR);

		alert.setHeaderText(message);
		if(subtext != null)
			alert.setContentText(subtext);

		alert.show();
	}

}