package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.Chat;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

public class MultiplayerScene extends ChallengeScene {
  Communicator communicator;
  Chat chat;
  private SimpleListProperty<Pair<String, Pair<Integer,Integer>>> scoresSL;
  private ArrayList<Pair<String, Pair<Integer,Integer>>> scoresAL = new ArrayList<>();
  ObservableList<Pair<String, Pair<Integer,Integer>>> scoresOL;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow,Communicator communicator) {
    super(gameWindow);
    this.communicator=communicator;
  }

  @Override
  public void setupGame() {
    game=new MultiplayerGame(5,5,communicator);
  }

  @Override
  void blockClicked(GameBlock gameBlock) {
    super.blockClicked(gameBlock);
    communicator.send("SCORE "+game.getScore().getValue().toString());
  }

  @Override
  public void build() {
    super.build();
    buildMultiplayeradditions();
    setUpCommunicator();
    setUpLivesListener();
    communicator.send("SCORES");
  }
  private void setUpLivesListener(){
    game.getLives().addListener((observableValue, number, t1) -> {
      if(t1.intValue()>0){
        communicator.send("LIVES "+t1.intValue());
      }else if(t1.intValue()==0){
        communicator.send("LIVES "+t1.intValue());
        communicator.send("DIE");
      }
    });
  }

  private void setUpCommunicator(){
    communicator.addListener(communication -> {
      if(communication.startsWith("SCORES ")){
        handleScores(communication);
      }else if (communication.startsWith("MSG")){
        chat.handleMessage(communication);
      }
    });
  }
  private void handleScores(String communication) {
    String tempString = communication.replace("SCORES ", "");
    String[] tempArray = tempString.split("\n");
    for (String scoreData : tempArray) {
      String[] dataSplit = scoreData.split(":");

      Pair<String, Pair<Integer, Integer>> tempPair = new Pair<>(dataSplit[0],
          new Pair<Integer, Integer>(Integer.parseInt(dataSplit[1]),
              Integer.parseInt(dataSplit[2])));
      boolean containsBool=false;
      Pair<String, Pair<Integer, Integer>> contains = null;
      for(Pair pair:scoresAL){
        if(pair.getKey().equals(tempPair.getKey())){
          containsBool=true;
          contains=pair;
        }
      }
      if(containsBool==false) {
        scoresAL.add(tempPair);
        sortScore(scoresAL);
        scoresOL.add(scoresAL.indexOf(tempPair), tempPair);
      }
      else if (containsBool == true) {
        if (!tempPair.equals(contains)) {
          scoresAL.remove(contains);
          scoresAL.add(tempPair);
          sortScore(scoresAL);
          scoresOL.remove(contains);
          scoresOL.add(scoresAL.indexOf(tempPair), tempPair);
        }
      }

    }
  }
  public void sortScore(ArrayList<Pair<String,Pair<Integer,Integer>>> list) {
    Collections.sort(list, new Comparator<Pair<String, Pair<Integer, Integer>>>() {
      @Override
      public int compare(Pair<String, Pair<Integer, Integer>> o1,
          Pair<String, Pair<Integer, Integer>> o2) {
        return o2.getValue().getKey().compareTo(o1.getValue().getKey());
      }
    });
  }


  private void setUpTimer(){
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        communicator.send("SCORES");
      }
    }, 10000, 10000);
  }

  public void buildMultiplayeradditions(){
    VBox left = new VBox();
    Leaderboard leaderboard = new Leaderboard("Scores",false);
    chat = new Chat(gameWindow.getWidth()/4,gameWindow.getHeight()/2,communicator);
    //leaderboard.getStyleClass().add("leaderboard");
    leaderboard.setMaxHeight(gameWindow.getHeight()/3);
    leaderboard.setMinHeight(gameWindow.getHeight()/3);
    left.getChildren().addAll(leaderboard,chat);
    mainPane.setLeft(left);

    scoresOL = FXCollections.observableArrayList(scoresAL);
    scoresSL = new SimpleListProperty(scoresOL);
    leaderboard.uiScoresv2.bind(scoresSL);
  }

  @Override
  public void setGEL() {
    game.setGameEndListener((finalGame) -> {
      game.setFinalScores(scoresOL);
      gameEndListener.gameOver(finalGame);
    });
  }
}
