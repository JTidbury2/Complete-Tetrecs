package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);
  private boolean selected;


  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.GREY,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.TURQUOISE,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.AQUAMARINE,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE,
  };
  /**
   * Gameboard where played
   */
  private final GameBoard gameBoard;

  private final double width;
  private final double height;

  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  Color oldColor;


  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    logger.info("Building a game block");
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;


    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
    this.getStyleClass().add("gameBlock");

  }


  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
      oldColor = COLOURS[value.get()];
    }
  }

  /**
   * Paint this block selected yellow square around it to indicate selection
   */
  public void paintSelected() {
    var gc = getGraphicsContext2D();

    gc.setStroke(Color.YELLOW);
    gc.setLineWidth(10);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint this canvas empty
   * Done using 4 triangle with different shades and a square to make a 3D effect
   */
  private void paintEmpty() {
    //logger.info("Painting the block empty");
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);
    //Triangles Co-Ordinates
    var TopX = new double[]{0, width, width / 2};
    var TopY = new double[]{0, 0, height / 2};
    //Colour fill
    gc.setFill(Color.GREY.deriveColor(0, 5, 5, 1));
    gc.fillPolygon(TopX, TopY, 3);

    //Triangles Co-Ordinates
    var LeftX = new double[]{0, 0, width / 2};
    var LeftY = new double[]{0, height, height / 2};
    //Colour fill
    gc.setFill(Color.GREY.deriveColor(0, 3, 3, 1));
    gc.fillPolygon(LeftX, LeftY, 3);

    //Triangles Co-Ordinates
    var RightX = new double[]{width, width, width / 2};
    var RightY = new double[]{0, height, height / 2};
    //Colour fill
    gc.setFill(Color.GREY.deriveColor(0, 0.7, 0.7, 1));
    gc.fillPolygon(RightX, RightY, 3);

    //Triangles Co-Ordinates
    var BottomX = new double[]{0, width, width / 2};
    var BottomY = new double[]{height, height, height / 2};
    //Colour fill
    gc.setFill(Color.GREY.deriveColor(0, 0.2, 0.2, 1));
    gc.fillPolygon(BottomX, BottomY, 3);

    gc.setFill(Color.GREY);
    gc.fillRect(width / 10, height / 10, (8 * width / 10), (8 * height / 10));

    //Border
    //gc.setStroke(Color.BLACK);
    //gc.strokeRect(0,0,width,height);
  }

  /**
   * Paint this canvas with the given colour
   * Done using 4 triangle with different shades and a square to make a 3D effect
   * @param colour the colour to paint
   */
  private void paintColor(Color colour) {
    var gc = getGraphicsContext2D();

    //Clear
    //Triangles Co-Ordinates
    var TopX = new double[]{0, width, width / 2};
    var TopY = new double[]{0, 0, height / 2};
    //Colour fill
    gc.setFill(colour.deriveColor(0, 5, 5, 1));
    gc.fillPolygon(TopX, TopY, 3);

    //Triangles Co-Ordinates
    var LeftX = new double[]{0, 0, width / 2};
    var LeftY = new double[]{0, height, height / 2};
    //Colour fill
    gc.setFill(colour.deriveColor(0, 3, 3, 1));
    gc.fillPolygon(LeftX, LeftY, 3);

    //Triangles Co-Ordinates
    var RightX = new double[]{width, width, width / 2};
    var RightY = new double[]{0, height, height / 2};
    //Colour fill
    gc.setFill(colour.deriveColor(0, 0.7, 0.7, 1));
    gc.fillPolygon(RightX, RightY, 3);

    //Triangles Co-Ordinates
    var BottomX = new double[]{0, width, width / 2};
    var BottomY = new double[]{height, height, height / 2};
    //Colour fill
    gc.setFill(colour.deriveColor(0, 0.2, 0.2, 1));
    gc.fillPolygon(BottomX, BottomY, 3);

    gc.setFill(colour);
    gc.fillRect(width / 10, height / 10, (8 * width / 10), (8 * height / 10));

  }

  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing it's colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }


  public void mouseLeft() {
    this.paintColor(COLOURS[value.get()]);
  }

  /**
   * A fade out animation that slowly fade the block out and replaces it with the grey block
   */
  public void fadeOut() {
    logger.info("Running the fade out animation of the blocks");
    AnimationTimer time = new AnimationTimer() {
      double opacity = 1;

      @Override
      public void handle(long l) {
        doHandle();
      }

      public void doHandle() {
        /*
        Flickers between the old colour at a lower opacity and an empty box to get fade effect
         */
        paintEmpty();
        paintColor(oldColor.deriveColor(0, 1, 1, opacity));
        opacity -= 0.025;

        if (opacity <= 0.0) {
          oldColor=COLOURS[value.get()];
          stop();
          paintEmpty();

        }
      }
    };
    time.start();
  }
}
