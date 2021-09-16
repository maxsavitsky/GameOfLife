module com.maxsavitsky.gameoflife {
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;

	opens com.maxsavitsky.gameoflife to javafx.fxml;
	exports com.maxsavitsky.gameoflife;
}