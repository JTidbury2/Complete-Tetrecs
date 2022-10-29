package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.ChallengeScene;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
  private static final Logger logger = LogManager.getLogger(Grid.class);

  /**
   * The number of columns in this grid
   */
  private final int cols;

  /**
   * The number of rows in this grid
   */
  private final int rows;

  /**
   * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
   */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    //Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x     column
   * @param y     row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) throws ArrayIndexOutOfBoundsException {
    return grid[x][y].get();
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * The can play piece method is used to check is the gamePiece provided as a parameter can be
   * played at the co-ordinates provides as the 2nd and 3rd parameters The method does this by
   * checking a 3x3 box around where the user has clicked (the co-ordinates provided) and checking
   * if the values for the colour in this box are 0 for the correct areas (the ones needed to be 0
   * to place the piece, ie the pieces where the piece is not 0.
   *
   * @param gp The gamePiece that the user intends to place
   * @param x  The x-coordinate of the block the user clocked
   * @param y  The y-coordinate of the block the user clocked
   * @return Returns the boolean value symbolising if the piece cn be played
   */

  public boolean canPlayPiece(GamePiece gp, int x, int y) {
    logger.info("Check if piece can be played");

    for (int gpx = 0; gpx < 3; gpx++) {
      for (int gpy = 0; gpy < 3; gpy++) {
        if (gp.getBlocks()[gpx][gpy] != 0) {
          try {
            if (get(x - 1 + gpx, y - 1 + gpy) != 0) {
              return false;
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * The play piece method checks if you can play a piece and if you can it plays the piece that is
   * passed to it
   *
   * @param gp The game piece that is going to be played at the co-ordinates provided
   * @param x  The x-coordinate of the block clicked by the user
   * @param y  The y-coordinate of the bock clicked by the user
   * @return Returns the boolean value containg true if the block was placed or false if not played
   */

  public boolean playPiece(GamePiece gp, int x, int y) {
    logger.info("Piece played at coordinates x="+x+" y="+y);
    boolean checkValue = canPlayPiece(gp, x, y);
    if (checkValue) {
      for (int gpx = 0; gpx < 3; gpx++) {
        for (int gpy = 0; gpy < 3; gpy++) {
          if (gp.getBlocks()[gpx][gpy] != 0) {
            set(x - 1 + gpx, y - 1 + gpy, gp.getValue());
          }
        }
      }
    }
    return checkValue;
  }
}
