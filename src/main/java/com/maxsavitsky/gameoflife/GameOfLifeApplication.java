package com.maxsavitsky.gameoflife;

import com.maxsavitsky.gameoflife.controller.MainController;
import com.maxsavteam.props.ProjectProps;
import com.maxsavteam.props.PropsException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class GameOfLifeApplication extends javafx.application.Application {
	@Override
	public void start(Stage stage) throws IOException, PropsException {
		FXMLLoader fxmlLoader = new FXMLLoader(GameOfLifeApplication.class.getResource("main-view.fxml"));
		ResourceBundle resourceBundle = loadConfig();
		fxmlLoader.setResources(resourceBundle);
		Scene scene = new Scene(fxmlLoader.load());
		((MainController) fxmlLoader.getController()).setStage(stage);
		stage.setResizable(false);
		stage.setTitle("Game of Life by Max Savitsky");
		stage.setScene(scene);
		stage.show();
	}

	private ResourceBundle loadConfig() throws IOException, PropsException {
		try (InputStream is = getClass().getResourceAsStream("props.properties")) {
			if(is == null)
				throw new IOException("Unable to open props resource");
			Properties props = ProjectProps.load(is);
			GlobalSettings.setCellSize(Integer.parseInt(props.getProperty("cellSize")));

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			props.store(bos, null);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ResourceBundle resourceBundle = new PropertyResourceBundle(bis);
			GlobalSettings.setPropsResourceBundle(resourceBundle);
			return resourceBundle;
		}
	}

	public static void main(String[] args) {
		launch();
	}
}