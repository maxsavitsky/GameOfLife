package com.maxsavitsky.gameoflife;

import com.maxsavitsky.gameoflife.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class GameOfLifeApplication extends javafx.application.Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(GameOfLifeApplication.class.getResource("main-view.fxml"));
		fxmlLoader.setResources(ResourceBundle.getBundle("com.maxsavitsky.gameoflife.constants", Locale.getDefault()));
		Scene scene = new Scene(fxmlLoader.load());
		((MainController) fxmlLoader.getController()).setStage(stage);
		stage.setResizable(false);
		stage.setTitle("Game of Life by Max Savitsky");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}