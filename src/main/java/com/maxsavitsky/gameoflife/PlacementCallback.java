package com.maxsavitsky.gameoflife;

import java.util.ArrayList;

public interface PlacementCallback {

	void onPlacementReady(ArrayList<MainController.LiveCell> cells);

}
