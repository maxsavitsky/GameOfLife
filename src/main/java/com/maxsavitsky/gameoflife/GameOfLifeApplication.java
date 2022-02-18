package com.maxsavitsky.gameoflife;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOfLifeApplication extends javafx.application.Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(GameOfLifeApplication.class.getResource("main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setResizable(false);
		stage.setTitle("Game of Life by Max Savitsky");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}