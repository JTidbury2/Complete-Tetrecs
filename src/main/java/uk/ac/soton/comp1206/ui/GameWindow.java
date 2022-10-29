package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.*;

/**
 * The GameWindow is the single window for the game where everything takes place. To move between
 * screens in the game, we simply change the scene.
 * <p>
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes.
 * You can add more methods here to add more screens to the game.
 */
public class GameWindow {

  private static final Logger logger = LogManager.getLogger(GameWindow.class);

  private final int width;
  private final int height;

  private final Stage stage;

  private BaseScene currentScene;
  private Scene scene;

  final Communicator communicator;

  /**
   * Create a new GameWindow attached to the given stage with the specified width and height
   *
   * @param stage  stage
   * @param width  width
   * @param height height
   */
  public GameWindow(Stage stage, int width, int height) {
    this.width = width;
    this.height = height;

    this.stage = stage;

    //Setup window
    setupStage();

    //Setup resources
    setupResources();

    //Setup default scene
    setupDefaultScene();

    //Setup communicator
    communicator = new Communicator("ws://tserver21012003.herokuapp.com");

    //Go to menu
    startMenu();
  }

  /**
   * Setup the font and any other resources we need
   */
  private void setupResources() {
    logger.info("Loading resources");

    //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
    Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 32);
    Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"), 32);
    Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"), 32);
  }

  /**
   * Display the main menu
   */
  public void startMenu() {
    MenuScene menu = new MenuScene(this);
    loadScene(menu);
    menu.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {

          System.exit(0);
        }
      }
    });
  }

  /**
   * Starts the menu scene
   */
  public void startControls(){
    ControlsScene controlsScene = new ControlsScene(this);
    loadScene(controlsScene);
    controlsScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          startMenu();
        }
      }
    });
  }

  /**
   * Display the single player challenge
   */
  public void startChallenge() {
    ChallengeScene challengeScene = new ChallengeScene(this);
    loadScene(challengeScene);
    //Add an end game listener to the challengeScene that stops the music and starts the scores scene
    challengeScene.setGameEndListener((scoreValue) -> {
      challengeScene.stopMusic();
      startScores(scoreValue);
    });
    //Adds an event handler to the scene which exits the scene that if the ESC key is pressed or passes the event onto the handle key method
    challengeScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          challengeScene.stopMusic();
          challengeScene.killTimer();
          startMenu();
        } else {
          challengeScene.handleKey(keyEvent);
        }
      }
    });
  }

  /**
   * Display the single player challenge with powers ups
   */
  public void startPowerUp() {
    PowerUpScene powerUpScene = new PowerUpScene(this);
    loadScene(powerUpScene);
    //Add an end game listener to the challengeScene that stops the music and starts the scores scene
    powerUpScene.setGameEndListener((scoreValue) -> {
      powerUpScene.stopMusic();
      startScores(scoreValue);
    });
    //Adds an event handler to the scene which exits the scene that if the ESC key is pressed or passes the event onto the handle key method
    powerUpScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          powerUpScene.stopMusic();
          powerUpScene.killTimer();
          powerUpScene.killPowTimer();
          startMenu();
        } else {
          powerUpScene.handleKey(keyEvent);
        }
      }
    });
  }

  /**
   * Start up the multiplayer game scene
   */
  public void startMultiplayerGame(){
    MultiplayerScene multiplayerScene = new MultiplayerScene(this,communicator);
    loadScene(multiplayerScene);
    multiplayerScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          multiplayerScene.stopMusic();
          multiplayerScene.killTimer();
          communicator.send("DIE");
          startMenu();
        } else {
          multiplayerScene.handleKey(keyEvent);
        }
      }
    });
    multiplayerScene.setGameEndListener((scoreValue) -> {
      multiplayerScene.stopMusic();
      startScores(scoreValue);
    });
  }

  /**
   * The start scores method is responsible for loading in the scores scene. IT creates the new
   * scene, loads the scores adds the score the new scene and adds an eventHandler that closes the
   * scene down if the ESC key is pressed
   *
   * @param finalGame The final game status with the scores. Needed to add to leader boards
   */
  public void startScores(Game finalGame) {
    ScoresScene scoresScene = new ScoresScene(this, finalGame, communicator);
    loadScene(scoresScene);
    scoresScene.addLoadedLocalScores();
    scoresScene.getScene().setOnKeyPressed((keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        startMenu();
      }
    }));
  }

  /**
   * Starts the instructions scene. Creates a new scene and adds an event handler to the scene that
   * leaves if the ESC key is pressed
   */
  public void startInstructions() {
    InstructionScene instructionScene = new InstructionScene(this);
    loadScene((instructionScene));
    instructionScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          startMenu();
        }
      }
    });
  }

  /**
   * Starts the lobby scene. Adds an event listner to the scene that exits the scene if the ESC key
   * is pressed As wel as closing the scene the ESC key kills the timer in the scene and starts the
   * menu scene
   */
  public void startLobby() {
    LobbyScene lobbyScene = new LobbyScene(this, communicator);
    loadScene(lobbyScene);
    lobbyScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          lobbyScene.killTimer();
          startMenu();
        }
      }
    });
  }

  /**
   * Starts the chat scene of the multiplayer.
   *
   * @param chatName The name of the chat that is to be the title of the scene
   * @param host     A boolean value representing if the user is the host of the chat that is
   *                 started
   */
  public void startChat(String chatName, boolean host) {
    ChatScene chatScene = new ChatScene(this, communicator, chatName, host);
    loadScene(chatScene);
        /*
         Adds a pulse listener that causes the chatscenes chat to jump to the bottom(ei
        autoscroll)
         */
    chatScene.getScene().addPostLayoutPulseListener(() -> chatScene.getChat().jumpToBottom());
        /*
        Adds an event listener that exits the chat is the ESC is pressed. THe timer and server
        connection is also killed
         */
    chatScene.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
          chatScene.killTimer();
          communicator.send("PART");
          startLobby();
        }
      }
    });
  }

  /**
   * Setup the default settings for the stage itself (the window), such as the title and minimum
   * width and height.
   */
  public void setupStage() {
    stage.setTitle("TetrECS");
    stage.setMinWidth(width);
    stage.setMinHeight(height + 20);
    stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
  }

  /**
   * Load a given scene which extends BaseScene and switch over.
   *
   * @param newScene new scene to load
   */
  public void loadScene(BaseScene newScene) {
    //Cleanup remains of the previous scene
    cleanup();

    //Create the new scene and set it up
    newScene.build();
    currentScene = newScene;
    scene = newScene.setScene();
    stage.setScene(scene);

    //Initialise the scene when ready
    Platform.runLater(() -> currentScene.initialise());
  }

  /**
   * Setup the default scene (an empty black scene) when no scene is loaded
   */
  public void setupDefaultScene() {
    this.scene = new Scene(new Pane(), width, height, Color.BLACK);
    stage.setScene(this.scene);
  }

  /**
   * When switching scenes, perform any cleanup needed, such as removing previous listeners
   */
  public void cleanup() {
    logger.info("Clearing up previous scene");
    communicator.clearListeners();
  }

  /**
   * Get the current scene being displayed
   *
   * @return scene
   */
  public Scene getScene() {
    return scene;
  }

  /**
   * Get the width of the Game Window
   *
   * @return width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Get the height of the Game Window
   *
   * @return height
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Get the communicator
   *
   * @return communicator
   */
  public Communicator getCommunicator() {
    return communicator;
  }
}
