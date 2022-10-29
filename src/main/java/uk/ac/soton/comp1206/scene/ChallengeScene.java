package uk.ac.soton.comp1206.scene;
//test1

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
  protected Game game;
  protected Multimedia music;
  protected int[] selectedBlock = new int[]{2, 2};
  private PieceBoard board2;
  private GameBoard board;
  private PieceBoard board3;
  protected GameEndListener gameEndListener;
  BorderPane mainPane;
  Text lives;
  Text multiplier;

  protected static final String RED_BAR = "red_bar";
  protected static final String YELLOW_BAR = "yellow_bar";
  protected static final String ORANGE_BAR = "orange_bar";
  protected static final String GREEN_BAR = "green_bar";
  protected static final String BLUE_BAR = "blue_bar";
  protected static final String[] barColorStyleClasses = {RED_BAR, ORANGE_BAR, YELLOW_BAR,
      GREEN_BAR,BLUE_BAR};

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    HBox top = buildTop();
    mainPane = new BorderPane();
    mainPane.setTop(top);

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2,
        true);
    board2 = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);
    board3 = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);
    board.setAlignment(Pos.CENTER);
    mainPane.setCenter(board);
    VBox boards = new VBox();
    board2.setPadding(new Insets(10));
    board3.setPadding(new Insets(10));
    Text current= new Text("Current Piece");
    Text following = new Text("Following Piece");
    current.getStyleClass().add("scorelist");
    following.getStyleClass().add("scorelist");
    boards.getChildren().addAll(current,board2,following, board3);
    mainPane.setRight(boards);

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    music = new Multimedia();
    music.playBackgroundMusicMenu("/music/game.mp3");

    setGameListeners();
    setGEL();

    setUpBoardListeners();
    setUpTimeLine();


  }

  /**
   * Sets the game listeners to swap pieces and fade out pieces
   */
  public void setGameListeners(){
    game.setListener((currentGP, followingGP) -> {
      board2.setPiece(currentGP);
      board3.setPiece((followingGP));
    });

    game.setLClistener((coordinates -> {
      board.fadeOut(coordinates);
      return 0;
    }
    ));
  }

  /**
   * Sets the board mouse listenrs to allow for swapping and rotation upon clicking. Also sets a
   * mouse moved listner to allow for the creation of the hover effect by paiting the block that
   * it is hovered over
   */
  public void setUpBoardListeners(){
    board2.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.PRIMARY) {
        rotatePiece(1);
      }

    });
    board.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.SECONDARY) {
        rotatePiece(1);

      }

    });

    board3.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.PRIMARY) {
        swapPiece();
      }
    });

    board.setOnMouseMoved((mouseEvent) -> {
      if(board.getHoverEnabled()==false) {
        board.getBlock(selectedBlock[0], selectedBlock[1]).paint();
      }
      board.setHoverEnabled(true);
    });
    board.setBlockChangedListener((currentBlock, newBlock) -> {
      selectedBlock = newBlock;
    });
  }

  /**
   * A method to setup the progressBar timeline to a certain size and location. Repsonaible for
   * setting the gameLoop listner to the game which takes in a delya and makes the bar slowly
   * depresess until the time is over
   */
  public void setUpTimeLine(){
    ProgressBar timeLine = new ProgressBar(1);
    timeLine.setPrefSize(gameWindow.getWidth(), 40);
    mainPane.setBottom(timeLine);
    game.setGameLoopListener(delay -> {
      Timeline task = new Timeline(
          new KeyFrame(
              Duration.ZERO,
              new KeyValue(timeLine.progressProperty(), 1)
          ),
          new KeyFrame(Duration.millis(delay), new KeyValue(timeLine.progressProperty(), 0
          ))
      );
      task.playFromStart();

    });
    setUptimelineListner(timeLine);
  }

  /**
   * sets up the timelIne listner that changes the colour of the progress bar based on the
   * proress property of the timelIne parameter
   * @param timeLine The progressBar reresenting the amount of time left to pladce a piece
   */
  public void setUptimelineListner(ProgressBar timeLine){
    timeLine.progressProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValuer,
          Number newValue) {
        double progress = newValue == null ? 0 : newValue.doubleValue();
        if (progress < 0.2) {
          setBarStyleClass(timeLine, RED_BAR);
        } else if (progress < 0.4) {
          setBarStyleClass(timeLine, YELLOW_BAR);
        } else if (progress < 0.6) {
          setBarStyleClass(timeLine, ORANGE_BAR);
        } else {
          setBarStyleClass(timeLine, GREEN_BAR);
        }


      }
    });
  }

  /**
   * A method to set the style class of the progress bar representing the amount fo time the player
   * has left to place the block
   *
   * @param bar           The progress bar that the style class is changed for
   * @param barStyleClass The string representing the name of the bar's style class
   */
  protected void setBarStyleClass(ProgressBar bar, String barStyleClass) {

    bar.getStyleClass().removeAll(barColorStyleClasses);
    bar.getStyleClass().add(barStyleClass);
  }

  /**
   * The buildTop method written below build the top of the game, the status bar containing the
   * current players staus, their score, lives etc. It builds all of these components and then adds
   * them to a HBox which it returns it put on the borderPane of whatever scene needed.
   *
   * @return HBox containing the top components of the scene (scores,lives,ect)
   */
  private HBox buildTop() {
    logger.info("Building the top of the challenge scene containing the scores, lives etc");
    HBox top = new HBox();

    //A series of statements creating text objects and binding them to the appropriate values of the game
    Text Prescore = new Text("  Score:");
    Prescore.getStyleClass().add("score");
    Text score = new Text();
    score.getStyleClass().add("score");
    score.textProperty().bind(game.getScore().asString());

    Text Prelevel = new Text("  Level:");
    Prelevel.getStyleClass().add("level");
    Text level = new Text();
    level.getStyleClass().add("level");
    level.textProperty().bind(game.getLevel().asString());

    Text Prelives = new Text("  Lives:");
    Prelives.getStyleClass().add("lives");
    lives = new Text();
    lives.getStyleClass().add("lives");
    lives.textProperty().bind(game.getLives().asString());

    Text Premultiplier = new Text("  Multiplier:");
    Premultiplier.getStyleClass().add("score");
    multiplier = new Text();
    multiplier.getStyleClass().add("score");
    multiplier.textProperty().bind(game.getMultiplier().asString());

    //Add all of the recently made Text ui elements to the top HBox that the method returns

    top.getChildren()
        .addAll(Prescore, score, Prelevel, level, Prelives, lives, Premultiplier, multiplier);

    Text Prehscore = new Text("  High Score:");
    Prehscore.getStyleClass().add("score");

    //Creating a hscore that stores the highscore stored on the local system. Gets the value from loadHighScore method
    int hscore = loadHighScore();
    Text highscore = new Text(String.valueOf(hscore));
    highscore.getStyleClass().add("score");
    top.getChildren().addAll(Prehscore, highscore);

    /* Adding a listener to the score property that makes the hscore change to the current score
     * if the current score is greater than the stored highscore.*/
    score.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
        if (Integer.parseInt(score.textProperty().getValue()) >= hscore) {
          highscore.textProperty().bind(score.textProperty());
        }
      }
    });
    return top;
  }


  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
  }

  /**
   * Stops the music currently playing in the background
   */
  public void stopMusic() {
    music.stopBackgroundMenu();
  }

  /**
   * Rotates the piece on the 2nd board the parameter amount of times Also plays the rotate noise
   * when it is rotated
   *
   * @param times The amount of times that the piece on the 2nd board is rotated
   */
  public void rotatePiece(int times) {
    logger.info("Pieces rotated in the scene, the second gameBoard, time: "+times);
    board2.setPiece(game.rotateCurrentPiece(times));
    music.playAudio("/sounds/rotate.wav");
  }

  /**
   * Swaps the piece on the 2nd board with the piece on the 3rd board Also plays the transition
   * noise when the peices are swapped
   */
  public void swapPiece() {
    logger.info("Pieces of current pieces and following piece swapped");
    var temp = game.swapCurrentPiece();
    board2.setPiece(temp[0]);
    board3.setPiece(temp[1]);
    music.playAudio("/sounds/transition.wav");
  }

  /**
   * Handles what occurs after a key if pressed on the keyBoard while the game is  playing If a
   * specific key is pressed then the method handles it in a specific way
   *
   * @param event The event caused by the key pressed. Contains the KeyCode for the key pressed
   */
  public void handleKey(KeyEvent event) {
    logger.info("The keyHandlers are set up in the challenge scene");
    if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
      board.getBlock(selectedBlock[0], selectedBlock[1]).paint();
      if (selectedBlock[1] > 0) {
        selectedBlock[1]--;
        board.setHoverEnabled(false);
      }
      board.getBlock(selectedBlock[0], selectedBlock[1]).paintSelected();
    } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
      board.getBlock(selectedBlock[0], selectedBlock[1]).paint();
      if (selectedBlock[0] > 0) {
        selectedBlock[0]--;
        board.setHoverEnabled(false);
      }
      board.getBlock(selectedBlock[0], selectedBlock[1]).paintSelected();
    } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
      board.getBlock(selectedBlock[0], selectedBlock[1]).paint();
      if (selectedBlock[0] < 4) {
        selectedBlock[0]++;
        board.setHoverEnabled(false);
      }
      board.getBlock(selectedBlock[0], selectedBlock[1]).paintSelected();
    } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
      board.getBlock(selectedBlock[0], selectedBlock[1]).paint();
      if (selectedBlock[1] < 4) {
        selectedBlock[1]++;
        board.setHoverEnabled(false);
      }
      board.getBlock(selectedBlock[0], selectedBlock[1]).paintSelected();
    } else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z) {
      rotatePiece(3);
    } else if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C) {
      rotatePiece(1);
    } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
      blockClicked(board.getBlockClicked(selectedBlock[0], selectedBlock[1]));
    } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
      swapPiece();
    }
  }

  public void setGameEndListener(GameEndListener gameEndListener) {
    this.gameEndListener = gameEndListener;
  }

  /**
   * The loadHighScore method looks into the localScores text file ( the one used to score the local
   * scores) and reads the scores, returns the highscore value
   *
   * @return The highscore of the local scores is returned
   */
  public int loadHighScore() {
    logger.info("High score of the local files is loaded");
    String path = new String("localScores.txt");
    File scoresFile = new File(path);
    String scoresString = "";
    int highscore;
    Reader reader;
    if (!scoresFile.exists()) {
      highscore = 0;
      return highscore;
    }

    try {
      reader = new FileReader(scoresFile);
      int temp = 0;
      try {
        while ((temp = reader.read()) != -1) {
          scoresString = scoresString + (char) temp;
        }
      } catch (IOException e) {
        System.out.println(e.getStackTrace());
      }

      String[] scores2Pairs = scoresString.split("\n");
      highscore = Integer.parseInt(scores2Pairs[0].split(",")[1]);
      reader.close();
      return highscore;

    } catch (FileNotFoundException e) {
      System.out.println(e.getStackTrace());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public void setGEL(){
    game.setGameEndListener((finalGame) -> {
      gameEndListener.gameOver(finalGame);
    });
  }

  public void killTimer(){
    game.killTimer();
  }

  public void killPowTimer(){game.killPowTimer();}
}
