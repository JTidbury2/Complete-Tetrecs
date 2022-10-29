package uk.ac.soton.comp1206.scene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  ImageView title;
  Multimedia music;
  boolean powerUps= false;

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);


    var mainPane = new BorderPane();
    mainPane.setMinHeight(gameWindow.getHeight());
    mainPane.setMinWidth(gameWindow.getWidth());
    menuPane.getChildren().add(mainPane);

    setupTitle(mainPane);
    BorderPane.setAlignment(title, Pos.CENTER);

    rotatePieces();


    //For now, let us just add a button that starts the game. I'm sure you'll do
    VBox menu = new VBox();
    menu.setPrefWidth(mainPane.getWidth());
    menu.setPrefHeight(mainPane.getHeight());

    setupButton(menu);


    menu.setMinWidth(gameWindow.getWidth()/2);

    mainPane.setCenter(menu);
    setupCheckBox(mainPane);

    music = new Multimedia();
    music.playBackgroundMusicMenu("/music/menu.mp3");

  }

  /**
   * Sets up the numerous checkbox ui elemtns in the menu
   * @param mainPane THe broderpane in whcih to place them
   */
  public void setupCheckBox(BorderPane mainPane){
    HBox noiseControls = new HBox();
    CheckBox noiseBox = new CheckBox("SFX");
    noiseBox.getStyleClass().add("menuItem");
    noiseBox.setSelected(Multimedia.getPlayNoise());
    noiseBox.setOnAction(actionEvent -> {
      if(noiseBox.isSelected()){
        Multimedia.setPlayNoise(true);
      }else{
        Multimedia.setPlayNoise(false);
      }
    });
    CheckBox musicBox = new CheckBox("Music");
    musicBox.getStyleClass().add("menuItem");
    musicBox.setSelected(Multimedia.getPlayMusic());
    musicBox.setOnAction(actionEvent -> {
      if(musicBox.isSelected()){
        Multimedia.setPlayMusic(true);
      }else{
        Multimedia.setPlayMusic(false);
      }
    });

    mainPane.setBottom(noiseControls);

    CheckBox powerBox = new CheckBox("Powers Ups");
    powerBox.getStyleClass().add("menuItem");
    powerBox.setOnAction(actionEvent -> {
      if(powerBox.isSelected()){
        powerUps=true;
      }else{
        powerUps=false;
      }
    });

    noiseControls.getChildren().addAll(noiseBox,musicBox,powerBox);
    noiseControls.setAlignment(Pos.BOTTOM_RIGHT);
  }

  /**
   * Sets up the title of the scene from an image view
   * @param mainPane The borderpane in whcih to place the title
   */
  public void setupTitle(BorderPane mainPane){
    title =
        new ImageView((new Image(this.getClass().getResource("/images/TetrECS.png").toExternalForm())));
    title.setPreserveRatio(true);
    title.setFitWidth(3*gameWindow.getWidth()/4);
    HBox TitleContainer = new HBox();
    TitleContainer.setAlignment(Pos.CENTER);
    TitleContainer.getChildren().add(title);
    TitleContainer.setPadding(new Insets(50));



    mainPane.setTop(TitleContainer);
  }

  /**
   * Adds buttons and their functionalities to the menu of the scene
   * @param menu The VBox where the buttons are stored
   */
  public void setupButton(VBox menu){
    var singleButton = new Button("Play");
    singleButton.getStyleClass().add("menuItem");


    var instructionButton = new Button("Instructions");
    instructionButton.getStyleClass().add("menuItem");


    var multiplayerButton = new Button("MultipLayer");
    multiplayerButton.getStyleClass().add("menuItem");

    var controlsButton = new Button("Controls");
    controlsButton.getStyleClass().add("menuItem");

    var exitButton = new Button("Exit");
    exitButton.getStyleClass().add("menuItem");
    menu.getChildren().addAll(singleButton,multiplayerButton,instructionButton,controlsButton,exitButton);

    //Bind the button action to the startGame method in the menu
    singleButton.setOnAction((event) -> {
      music.stopBackgroundMenu();
      startGame(event);
    });

    instructionButton.setOnAction((event) -> {
      music.stopBackgroundMenu();
      showInstructions(event);
    });

    multiplayerButton.setOnAction(actionEvent -> {
      music.stopBackgroundMenu();
      loadLobby();
    });

    exitButton.setOnAction(actionEvent -> {
      System.exit(0);
    });

    controlsButton.setOnAction(actionEvent -> {
      music.stopBackgroundMenu();
      loadControls();
    });
  }

  private void loadControls(){
    logger.info("Starting the controls scene from the menu");
    gameWindow.startControls();
  }

  private void loadLobby() {
    logger.info("Starting the lobby scene from the menu");
    gameWindow.startLobby();
  }

  private void showInstructions(ActionEvent event) {
    logger.info("Staring the Instructions from the menu");
    gameWindow.startInstructions();
  }

  /**
   * Initialise the menu
   */
  @Override
  public void initialise() {

  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    logger.info("Starting the game from the menu");
    music.stopBackgroundMenu();
    if(powerUps){
      gameWindow.startPowerUp();
    }else {
      gameWindow.startChallenge();
    }
  }

  /**
   * Adds an animation the to the title node that rotates it
   */
  private void rotatePieces() {
    logger.info("Adding rotate animation to the menu title");
    RotateTransition rotate = new RotateTransition();
    rotate.setAxis(Rotate.Z_AXIS);
    rotate.setFromAngle(-7.5);
    rotate.setByAngle(15);
    rotate.setCycleCount(Animation.INDEFINITE);
    rotate.setDuration(Duration.millis(2000));
    rotate.setAutoReverse(true);
    rotate.setNode(title);
    rotate.play();
  }


}
