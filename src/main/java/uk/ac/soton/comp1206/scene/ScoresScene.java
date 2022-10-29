package uk.ac.soton.comp1206.scene;

import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;
import uk.ac.soton.comp1206.ui.ScoresList;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScoresScene extends BaseScene {

  Game finalGame;
  // A series of contstructors that create all of the needed lists to represent the scores
  private SimpleListProperty<Pair<String, Integer>> localScoresSL;
  private ArrayList<Pair<String, Integer>> localScoresAL = new ArrayList<>();
  ObservableList<Pair<String, Integer>> localScoresOL;
  public SimpleListProperty<Pair<String, Integer>> onlineScoresSL;
  private ArrayList<Pair<String, Integer>> onlineScoresAL = new ArrayList<>();
  ObservableList<Pair<String, Integer>> onlineScoresOL;

  String path = new String("localScores.txt");
  File scoresFile = new File(path);
  TextField name;
  ScoresList uiScores;
  Communicator communicator;
  boolean nameSet = true;

  /**
   * Create a new scene scene passing the gameWindow, the finals game state and the communicator in
   *
   * @param gameWindow   The game window hosting the game
   * @param finalGame    The final state of the game just played. Allows for access to stats created
   *                     during the game
   * @param communicator The communicator used send messages to the server
   */
  public ScoresScene(GameWindow gameWindow, Game finalGame, Communicator communicator) {
    super(gameWindow);
    this.finalGame = finalGame;
    this.communicator = communicator;
  }


  @Override
  public void initialise() {

  }

  /**
   * Builds the scene
   */
  @Override
  public void build() {
    //Sets up the base of th scene, allowing a background
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("score-background");
    root.getChildren().add(scorePane);

    var scrPane = new BorderPane();
    scorePane.getChildren().add(scrPane);

    Text scoreTitle = new Text("Scores");
    scoreTitle.getStyleClass().add("title");
    BorderPane.setAlignment(scoreTitle, Pos.TOP_CENTER);
    scrPane.setTop(scoreTitle);

        /*
        Creating the text field and button for a user to submit their name and enter their score onto the various leaderboards
         */
    if(finalGame.getFinalScores()==null) {
      name = new TextField("Enter Name");
      name.setOnAction(e -> {
        addScores();
      });
      Button enterName = new Button("Submit Name");
      enterName.getStyleClass().add("scoreSceneItem");
      enterName.setOnAction(e -> {
        addScores();
      });

      VBox names = new VBox();
      names.getChildren().addAll(name, enterName);
      scrPane.setCenter(names);
    }
    //Create the lists needed to store both the online and local score of the game


    onlineScoresOL = FXCollections.observableArrayList(onlineScoresAL);
    onlineScoresSL = new SimpleListProperty<>(onlineScoresOL);

    if(finalGame.getFinalScores()==null) {
      System.out.println("Test for local scores");
      setupLocalScores(scrPane);
    }else if(finalGame.getFinalScores()!=null){
      System.out.println("Test for multi scores");
      setupMultiplayerScore(scrPane);
    }

    ScoresList onlinescore = new ScoresList("Online Score");
    scrPane.setRight(onlinescore);
    onlinescore.uiScores.bind(onlineScoresSL);

    changeUIScores(false);
    loadOnlineScores();

  }
  private void setupMultiplayerScore(BorderPane scrPane) {
    ArrayList<Pair<String, Pair<Integer, Integer>>> multiScoresAL = new ArrayList<>();
    ObservableList<Pair<String, Pair<Integer,Integer>>> multiScoresOL = FXCollections.observableArrayList(multiScoresAL);
    SimpleListProperty<Pair<String, Pair<Integer,Integer>>> multiScoresSL= new SimpleListProperty<>(multiScoresOL);
    Leaderboard uiScores= new Leaderboard("Multiplayer Scores",true);
    scrPane.setLeft(uiScores);
    uiScores.uiScoresv2.bind(multiScoresSL);
    for(Pair<String,Pair<Integer,Integer>> pair: finalGame.getFinalScores()){
      multiScoresOL.add(pair);
    }

  }
  private void setupLocalScores(BorderPane scrPane){
    localScoresOL = FXCollections.observableArrayList(localScoresAL);
    localScoresSL = new SimpleListProperty(localScoresOL);
    //Load the scores
    loadScores();
    sortScore(localScoresAL);
    uiScores = new ScoresList("Local Scores");
    scrPane.setLeft(uiScores);
    uiScores.uiScores.bind(localScoresSL);
  }

  /**
   * The method needed to add the local scores that have just been loaded in to the arraylist to the
   * observable list
   */
  public void addLoadedLocalScores() {
    for (Pair pair : localScoresAL) {
      localScoresOL.add(pair);
    }
  }

  /**
   * The method needed to add the online scores tht have just been loaded in to the arrayList to the
   * observable list
   */
  public void addLoadedOnlineScores() {
    for (Pair pair : onlineScoresAL) {
      onlineScoresOL.add(pair);
    }
  }

  /**
   * The add score method takes the text value typed in the testField (the one that prompted the
   * user to type in their name) and adds a text with the users name and the score they got tin the
   * most recent game to the correct position on both the online and the local leaderboard if they
   * get a score high enough to earn a position on them. This invloves writing to a local file for
   * the local scores or sending a message to the server if online.
   */

  public void addScores() {
    if (nameSet) {
      Pair<String, Integer> tempPair = new Pair<String, Integer>(name.getText(),
          finalGame.getScore().getValue());
      localScoresAL.add(tempPair);
      sortScore(localScoresAL);
      try {
        writeScore(true, scoresFile);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      localScoresOL.add(localScoresAL.indexOf(tempPair), tempPair);
      if (tempPair.getValue() > onlineScoresAL.get(onlineScoresAL.size() - 1).getValue()) {
        onlineScoresAL.add(tempPair);
        sortScore(onlineScoresAL);
        onlineScoresOL.add(onlineScoresAL.indexOf(tempPair), tempPair);
        communicator.send("HISCORE " + tempPair.getKey() + ":" + tempPair.getValue());
      }
      nameSet = false;
    }
  }

  /**
   * The method to load the scores from the server
   */
  public void loadOnlineScores() {
        /*adding a listener to the communicator that takes the highscores provided and created text objects for each of them
        as well as adding them to the lists containing the online scores.  A bit of string manipultation is needed to get the
        scores and names from the message sent by the communicator but other than that it is relativly simple.
         */
    communicator.addListener(communication -> {
      if (communication.startsWith("HISCORES")) {
        String communicationMK2 = communication.replace("HISCORES ", "");
        String[] scores2Pairs = communicationMK2.split("\n");
        System.out.println(scores2Pairs.toString());
        for (String pair2Be : scores2Pairs) {
          System.out.println(pair2Be);
          var tempArray = pair2Be.split(":");
          tempArray.toString();
          var tempPair = new Pair<String, Integer>(tempArray[0], Integer.parseInt(tempArray[1]));
          tempPair.toString();
          onlineScoresAL.add(tempPair);
        }
        Platform.runLater(() -> addLoadedOnlineScores());
      }
    });
    //Send the HISCORES message to the communicator to the server to request the 10 highest scores stored online.
    //Done after the listener is added so that if a response is sent there is a way to react to it
    communicator.send("HISCORES");
  }

  /**
   * A method to load the scores from the local scores store Involves creating a file reader and
   * then some basic string manipulation to place the scores and their names as pairs to the lists
   * needed to store them
   */
  public void loadScores() {
    String scoresString = new String();
    Reader reader;
    try {
      if (!scoresFile.exists()) {
        scoresFile.createNewFile();
        writeScore(false, scoresFile);
      }
    } catch (IOException e) {
      System.out.println("Error in file writing");
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
      System.out.println(scores2Pairs.toString());
      for (String pair2Be : scores2Pairs) {
        System.out.println(pair2Be);
        var tempArray = pair2Be.split(",");
        tempArray.toString();
        var tempPair = new Pair<String, Integer>(tempArray[0], Integer.parseInt(tempArray[1]));
        tempPair.toString();
        localScoresAL.add(tempPair);
      }

      reader.close();

    } catch (FileNotFoundException e) {
      System.out.println(e.getStackTrace());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * A method to sort the list provided to it into descending order governed by the values stored in
   * the value part of the pairs
   *
   * @param list The list that is to be sorted into descening order
   */
  public void sortScore(ArrayList<Pair<String, Integer>> list) {
    Collections.sort(list, new Comparator<Pair<String, Integer>>() {
      @Override
      public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });


  }

  /**
   * A method to write a score to the local scores file.
   *
   * @param made       The boolean value representing if the file containing the list containing the
   *                   scores has been made already. This allows the program to create a local file
   *                   if it hasnt or access the one already their if it has
   * @param scoresFile The file where the scores are to be written
   * @throws IOException Throws an IOException error if the writer is unable to write to the file
   *                     for whatever reason
   */
  public void writeScore(boolean made, File scoresFile) throws IOException {
    if (made == false) {
      Writer out = new FileWriter(scoresFile, false);
      out.write("Sweat,3000\nBetter,2000\nMeh,1000\nNoob,500");
      out.close();
    } else {
      Writer out = new FileWriter(scoresFile, false);
      String scores = "";
      for (Pair<String, Integer> pair : localScoresAL) {
        scores = scores + pair.getKey() + "," + pair.getValue() + "\n";
      }
      out.write(scores);
      out.close();
    }
  }

  private void changeUIScores(Boolean value) {
    if(finalGame.getFinalScores()==null) {
      uiScores.changeAble = value;
    }
  }
}
