package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import uk.ac.soton.comp1206.event.ScoreIncreaseListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  ScoreIncreaseListener scoreIncreaseListener;

  int baseMult=1;

  Multimedia music = new Multimedia();

  ObservableList<Pair<String, Pair<Integer,Integer>>> finalScores=null;

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;

  protected GamePiece currentPiece;

  protected GamePiece followingPiece;

  private NextPieceListener listener;

  private LineClearedListener LClistener;

  private Timer gameTimer;

  private GameLoopListener gameLoopListener;

  private GameEndListener gameEndListener;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");
    followingPiece = spawnPiece();
    nextPiece();
    startTimer();
  }

  /**
   * The method called to start the game timer. The timers starts the gameLoop() method with the
   * delay given to it by getTimerDelay()
   */

  public void startTimer() {
    logger.info("Timer started in the game");
    gameTimer = new Timer();
    gameTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        gameLoop();
      }
    }, getTimerDelay());
    gameLoopListener.reportDelay(getTimerDelay());
  }

  /**
   * The method used to reset the timer. The previous timer is cancelled and purged and then the
   * startTimer method is then called to reset the timer with the new delay
   */
  public void resetTimer() {
    logger.info("Timer reset in the game");
    killTimer();
    startTimer();
  }

  /**
   * A method to purge and kill the game timer
   */
  public void killTimer(){
    gameTimer.cancel();
    gameTimer.purge();
  }

  /**
   * The gameLoop method gets the next piece,resets the multiplier back to 1 and decreases the
   * lives. The gameLoop is used to time the round to move the player on if they have waited too
   * long on one piece For this reason it resets the multiplier and decreases lives. It also resets
   * the timer.
   */
  public void gameLoop() {
    logger.info("Game loop run");
    nextPiece();
    Platform.runLater(() -> setMultiplier(1));
    Platform.runLater(() -> setLives());

    resetTimer();
  }

  /**
   * Setter for the gameEndListener
   *
   * @param gameEndListener The gameEndListener for the current gameEndListener
   */
  public void setGameEndListener(GameEndListener gameEndListener) {
    this.gameEndListener = gameEndListener;
  }

  /**
   * Setter for the gameLoopListener
   *
   * @param gameLoopListener The gameLoopListener for the current GameLoopListener
   */
  public void setGameLoopListener(GameLoopListener gameLoopListener) {
    this.gameLoopListener = gameLoopListener;
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    if (grid.playPiece(currentPiece, x, y)) {
      music.playAudio("/sounds/place.wav");
      nextPiece();
      afterPiece();
      resetTimer();
    }else{
      music.playAudio("/sounds/fail.wav");
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
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
   * Creates a new gamePiece of a random type and a random rotation
   *
   * @return Returns the random gamePiece that is created
   */
  public GamePiece spawnPiece() {
    logger.info("New piece created");
    return GamePiece.createPiece((int) (Math.random() * 15), (int) (Math.random() * 4));
  }

  /**
   * The nextPiece method crates a new piece and replaces the current piece with the following piece
   * which is the replaced by thew newly created piece. The following piece is displayed on the 3rd
   * board while the current piece is displayed on 2nd board. These newly shifted pieces are then
   * passed to the nextPiece listener
   */
  public void nextPiece() {
    logger.info("Next piece shuffled down the queue");
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    listener.nextPiece(currentPiece, followingPiece);
  }

  /**
   * The method that runs after the piece is played. The method is responsible for clearing any rows
   * filled when the piece was placed. To do this it uses multiple for loops. The first of these
   * checks if a row/column has the 0 value, representing a blank square. If the row/column has a 0
   * it is not full and so it is taken from the arrays containing the lines which are full. After
   * the next for loop checks if the col/row arrays posses any full lines. If the line is full the
   * for loop resets all the values in that row back to 0, adds the blocks to an array containing
   * the recently cleared blocks and finally increments the int value storing the no of blocks /
   * lines cleared appropriately. After this is done the method passes the arrayList containing the
   * cleared blocks to the LC (line cleared) listener and passes the no of blocks and lines cleared
   * to the score method
   */
  public void afterPiece() {
    logger.info("Checking if any rows or columns have been filled in the last turn");
    var rowArray = new int[]{1, 1, 1, 1, 1};
    var colArray = new int[]{1, 1, 1, 1, 1};
    int linesCleared = 0;
    int boxesCleared = 0;
    ArrayList<GameBlockCoordinate> blocksCleared = new ArrayList<>();
    //Checks if a row or column is full or not
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {

        if (grid.get(x, y) == 0) {
          rowArray[y] = 0;
          colArray[x] = 0;
        }


      }
    }
    //For each full line rests the blocks and increased the lines and boxes cleared variables
    for (int k = 0; k < rowArray.length; k++) {
      if (rowArray[k] == 1) {
        linesCleared++;
        for (int l = 0; l < cols; l++) {
          if (grid.get(l, k) != 0) {
            blocksCleared.add(new GameBlockCoordinate(l, k));
            grid.set(l, k, 0);
            boxesCleared++;
          }
        }
      }
      if (colArray[k] == 1) {
        linesCleared++;
        for (int l = 0; l < rows; l++) {
          if (grid.get(k, l) != 0) {
            blocksCleared.add(new GameBlockCoordinate(k, l));
            grid.set(k, l, 0);
            boxesCleared++;
          }
        }
      }
    }
    LClistener.LineCleared(blocksCleared);
    if(linesCleared>0){
      music.playAudio("/sounds/clear.wav");
    }
    //Calls the scores method with the values of line and boxes cleared
    score(linesCleared, boxesCleared);
  }

  public IntegerProperty getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score.set(score);
  }

  public IntegerProperty getLevel() {
    return level;
  }

  /**
   * Simple setter that also played a the level noise when used
   * @param level
   */
  public void setLevel(int level) {
    if(this.level.get()<level) {
      music.playAudio("/sounds/level.wav");
    }

    this.level.set(level);

  }

  public IntegerProperty getLives() {
    return lives;
  }

  public IntegerProperty getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * Not just a simple set method. The set lives method decreases the lives value by one and checks
   * if the value after this decrement is 0. If that is so the gameEndListener's gameOver method is
   * called with this game value.
   */

  public void setLives() {
    lives.set(lives.getValue() - 1);
    music.playAudio("/sounds/lifelose.wav");
    logger.info("Lives set to: "+lives.get());
    if (lives.getValue() == 0) {
      endGame();
    }
  }

  /**
   * A method to end the game. Kills the gameLoop timer and calls the gameover method in the
   * gameEndListner
   */
  public void endGame(){
    killTimer();
    gameEndListener.gameOver(this);
  }

  protected IntegerProperty score = new SimpleIntegerProperty(0);
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  protected IntegerProperty lives = new SimpleIntegerProperty(3);
  protected IntegerProperty multiplier = new SimpleIntegerProperty(1);

  /**
   * The score method is used to increase the current score of the game and possibly the multiplier.
   * It does this by taking the lines and blocks cleared this turn and increasing the score by the
   * specific amount and increasing the multiplier by one if a line was cleared. If no lines are
   * cleared then the multiplier value is reset back to 1
   *
   * @param lines  The number of lines cleared this turn. If it is greater than 0, the multiplier is
   *               increases by 1. If none are cleared then it is reset back to 1
   * @param blocks The number of blocks cleared. The score of the game is incremented by the no of
   *               blocks*no of lines*10*multiplier value.
   */

  public void score(int lines, int blocks) {
    setScore((getScore().getValue()) + (lines * blocks * 10 * (getMultiplier().getValue())));
    if (lines >= 1) {
      setMultiplier(getMultiplier().getValue() + 1);
    } else {
      setMultiplier(baseMult);
    }
    setLevel(Math.floorDiv(getScore().getValue(), 1000));
  }

  public void setListener(NextPieceListener listener) {
    this.listener = listener;
  }

  /**
   * Rotates the current piece of the game the parameter number of times.
   *
   * @param times The number of times the current block should be rotated clockwise. Basically 1 for
   *              clockwise, 3 for anti-clcckwise
   * @return Returns the game pieces that has just been rotated in its post rotated state
   */
  public GamePiece rotateCurrentPiece(int times) {
    logger.info("Current piece rotated "+times+" times");
    currentPiece.rotate(times);
    return currentPiece;
  }

  /**
   * Swaps the current piece value and the following piece value
   *
   * @return Returns an array of GamePieces with the currentPiece first and the followingPiece
   * second
   */
  public GamePiece[] swapCurrentPiece() {
    logger.info("Current piece and following piece are swapped ");
    var temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
    return new GamePiece[]{currentPiece, followingPiece};
  }

  public void setLClistener(LineClearedListener LClistener) {
    this.LClistener = LClistener;
  }

  /**
   * A method to get the correct timer delay for the user's level
   *
   * @return An int value representing the millisecond delay for the timer.
   */

  public int getTimerDelay() {
    int delay = 12500 - (500 * level.getValue());
    if (delay < 2500) {
     delay=2500;
    }
    logger.info("New game timer delay got. Value : "+delay);
    return delay;
  }

  public void setFinalScores(ObservableList finalScores){
    this.finalScores=finalScores;
  }

  public ObservableList<Pair<String, Pair<Integer, Integer>>> getFinalScores() {
    return finalScores;
  }

  /**
   * Sets the base mult value to the parameter and sets the current mult to the new base mult
   * @param baseMult THe new base mult value (normally 1 and maybe 3 if poweredup)
   */
  public void setBaseMult(int baseMult) {
    this.baseMult = baseMult;
    setMultiplier(baseMult);
  }

  public void setScoreIncreaseListener(ScoreIncreaseListener scoreIncreaseListener){
    this.scoreIncreaseListener=scoreIncreaseListener;
  }
  public void handlePowerUps(int ability){};

  public void killPowTimer(){};
}

