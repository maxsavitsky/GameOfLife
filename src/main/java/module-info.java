module com.maxsavitsky.gameoflife {
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;
	requires com.maxsavteam.props;

	opens com.maxsavitsky.gameoflife to javafx.fxml;
	exports com.maxsavitsky.gameoflife;
	exports com.maxsavitsky.gameoflife.controller;
	opens com.maxsavitsky.gameoflife.controller to javafx.fxml;
}