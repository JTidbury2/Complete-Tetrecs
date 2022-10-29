package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockChangedListener;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.ArrayList;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming
 * block. It also be linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in
 * it, which should take place in the Grid.
 */
public class GameBoard extends GridPane {

  private static final Logger logger = LogManager.getLogger(GameBoard.class);

  /**
   * Number of columns in the board
   */
  private final int cols;

  /**
   * Number of rows in the board
   */
  private final int rows;

  /**
   * The visual width of the board - has to be specified due to being a Canvas
   */
  private final double width;

  /**
   * The visual height of the board - has to be specified due to being a Canvas
   */
  private final double height;

  /**
   * The grid this GameBoard represents
   */
  final Grid grid;

  /**
   * The blocks inside the grid
   */
  protected GameBlock[][] blocks;

  /**
   * The listener to call when a specific block is clicked
   */
  private BlockClickedListener blockClickedListener;

  private boolean selectable;

  private boolean hoverEnabled;

  private int[] selectedBlock;

  private BlockChangedListener blockChangedListener;


  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid   linked grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height, boolean selectable) {
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;
    this.selectable = selectable;

    //Build the GameBoard
    build();
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height, boolean selectable) {
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);
    this.selectable = selectable;

    //Build the GameBoard
    build();
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Build the GameBoard by creating a block at every x and y column and row
   */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    /*
    Build the series of gameBlocks and adds them to the game boards
    Adds a on mouse entered listner to the blocks that calls if a mouse hovers over them, making
    them get painted as selected.
    Also add a mouse left listner to remove the yellow box if the mouse has moved
     */
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        GameBlock tempBlock = createBlock(x, y);
        if (selectable) {

          tempBlock.setOnMouseEntered(mouseEvent -> {
            blockChangedListener.blockChanged(selectedBlock,new int[]{tempBlock.getX(), tempBlock.getY()});
            if (hoverEnabled) {
              tempBlock.paintSelected();
              changeBlock(new int[]{tempBlock.getX(), tempBlock.getY()});
            }
          });

          tempBlock.setOnMouseExited(mouseEvent -> {
            if (hoverEnabled) {
              tempBlock.mouseLeft();
            }
          });
        }
      }
    }
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    //Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

    //Add to the GridPane
    add(block, x, y);

    //Add to our block directory
    blocks[x][y] = block;

    //Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    //Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked((e) -> blockClicked(e, block));

    return block;
  }

  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param event mouse event
   * @param block block clicked on
   */
  private void blockClicked(MouseEvent event, GameBlock block) {
    logger.info("Block clicked: {}", block);
    if (event.getButton() == MouseButton.PRIMARY) {
      if (blockClickedListener != null) {
        blockClickedListener.blockClicked(block);
      }
    }
  }

  /**
   * Simple getter to get the block stored at x and y
   * @param x The x co-ord of the block in the grid
   * @param y The y co-ord of the block in the grid
   * @return Returns the gameblock at certain x and y co-ords
   */
  public GameBlock getBlockClicked(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Applies the fadeout animation to the game blocks stored at the co-ords stored in the array
   * @param coordinates The arrayList of coord storing the blocks to fade out
   */
  public void fadeOut(ArrayList<GameBlockCoordinate> coordinates) {
    logger.info("Block fading out, coordinates"+ coordinates);
    for (GameBlockCoordinate coOrd : coordinates) {
      blocks[coOrd.getX()][coOrd.getY()].fadeOut();
    }
  }

  /**
   * Setting the hover effect as enabled on the board
   * @param hoverEnabled Boolean representing if hover is enabled or not
   */
  public void setHoverEnabled(boolean hoverEnabled) {
    this.hoverEnabled = hoverEnabled;
  }

  /**
   * Changes the selected block to the new selected block provided in the array
   * @param newSelectedBlock The int array containg the coord of the new selected block
   */
  private void changeBlock(int[] newSelectedBlock) {
    blockChangedListener.blockChanged(selectedBlock, newSelectedBlock);
    selectedBlock = newSelectedBlock;
  }

  public void setBlockChangedListener(BlockChangedListener blockChangedListener) {
    this.blockChangedListener = blockChangedListener;
  }
  public boolean getHoverEnabled(){
    return hoverEnabled;
  }
}
