package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.ArrayList;

public interface LineClearedListener {

  int LineCleared(ArrayList<GameBlockCoordinate> coordinates);
}
