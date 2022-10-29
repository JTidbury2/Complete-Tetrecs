package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard {
  private static final Logger logger = LogManager.getLogger(PieceBoard.class);

  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height, false);
  }


  /**
   * Sets the piece on the pieceboard to the game piece provided and painting the middle piece as
   * selected to indicate where the bloc is placed from
   * @param gp The gamepiece to be put onto the piece boards
   */
  public void setPiece(GamePiece gp) {
    logger.info("Set a new piece on the pieceboard, value: "+gp.getValue());
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        grid.set(i, j, 0);
      }
    }
    grid.playPiece(gp, 1, 1);
    blocks[1][1].paintSelected();
  }

}
