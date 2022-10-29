package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.Chat;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.UnaryOperator;

public class ChatScene extends BaseScene {

  Multimedia music = new Multimedia();
  private static final Logger logger = LogManager.getLogger(ChatScene.class);
  Communicator communicator;
  String chatName;
  ArrayList<String> usersString = new ArrayList<>();
  ArrayList<Pair<SimpleStringProperty, Text>> users = new ArrayList<>();
  Chat chat;
  private Timer timer;
  public boolean host = false;

  public ChatScene(GameWindow gameWindow, Communicator communicator, String chatName,
      boolean host) {
    super(gameWindow);
    this.communicator = communicator;
    this.chatName = chatName;
    this.host = host;
  }

  @Override
  public void initialise() {

  }

  /**
   * Builds the chat scene's layout
   */
  @Override
  public void build() {
    logger.info("Building:" + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var chatPane = new StackPane();
    chatPane.setMaxWidth(gameWindow.getWidth());
    chatPane.setMaxHeight(gameWindow.getHeight());
    chatPane.getStyleClass().add("lobby-background");
    root.getChildren().add(chatPane);

    var chatBorderPane = new BorderPane();
    chatPane.getChildren().add(chatBorderPane);

    Text chatTitle = new Text("Chat: " + chatName);
    chatTitle.getStyleClass().add("title");
    BorderPane.setAlignment(chatTitle, Pos.CENTER);
    chatBorderPane.setTop(chatTitle);

    chat = new Chat(gameWindow.getWidth() / 2, gameWindow.getHeight(), communicator);
    chatBorderPane.setLeft(chat);

    VBox rightHandSide = new VBox();

    TextField nickField = new TextField("New Nickname");
    Button nickButton = new Button("ChangeNickName");
    nickButton.setOnAction(e -> {
      changeName(nickField.getText());
      nickField.setText("New Nickname");
    });
    nickButton.getStyleClass().add("menuItem");

    Text usersTitle = new Text("Current Users:");
    usersTitle.getStyleClass().add("title");

    VBox names = new VBox();

    rightHandSide.getChildren().addAll(nickField, nickButton, usersTitle, names);
    chatBorderPane.setRight(rightHandSide);

    if (host) {
      hostSetup(rightHandSide);
    }

        /*
        Adds a communicator listener to the communicator to check what message has been received form the server
        and act accordingly if need be.
         */
    setupComm(names,rightHandSide);

    setupTimer();
  }

  /**
   * Sets up the communicator listner ti handle all the incoming messages
   * @param names The VBox conatining all the possible names of people on the server
   * @param rightHandSide The VBox containg all of the RHS content of the chat scene
   */
  public void setupComm(VBox names, VBox rightHandSide){
    communicator.addListener(communication -> {
      if (communication.startsWith("MSG")) {
        music.playAudio("/sounds/message.wav");
        chat.handleMessage(communication);
      } else if (communication.startsWith("USERS ")) {
        handleUser(communication,names);
      } else if (communication.startsWith("NICK ") && communication.contains(":")) {
        nickHandler(communication);
      } else if (communication.startsWith("HOST")) {
        hostSetup(rightHandSide);
      } else if (communication.startsWith("START")) {
        killTimer();
        Platform.runLater(()->gameWindow.startMultiplayerGame());
      } else if (communication.startsWith("DIE")) {
        removeName(communication.replace("DIE ", ""), names);
      }else if(communication.startsWith("ERROR")){
        chat.handleError(communication);
      }

    });
  }

  /**
   * A method specialising in handling the change of nickname messages. Changes the vlaues of the
   * old name to the new name in the names vbox
   * @param communication The message containg the information about the changed nickname
   */
  public void nickHandler(String communication){
    String tempString = communication.replace("NICK ", "");
    String[] tempArray = tempString.split(":");
    usersString.remove(tempArray[0]);
    usersString.add(tempArray[1]);

    for (Pair<SimpleStringProperty, Text> userName : users) {
      if (userName.getKey().getValue().equals(tempArray[0])) {
        Platform.runLater(() -> userName.getKey().setValue(tempArray[1]));

      }
    }
  }

  /**
   * Creates a timer that requests the users in the lobby ever second
   */
  public void setupTimer(){
            /*
        The following lines of code set up a timer that is responsible for asking the server for the list of
        users every 1
         seconds.
         */

    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        communicator.send("USERS");
      }
    }, 1000, 1000);
  }

  /**
   * The following method is responsible for removing the name of a user from the list of users in
   * the ui display and from the ArrayList representing the users in the ui display.
   *
   * @param name The name of the user that we want to remove as a string
   * @param vbox The vbox from which we want to remove the user in the ui display
   */
  private void removeName(String name, VBox vbox) {
    logger.info("The name "+name+" is removed from the list of people in the chat");
    usersString.remove(name);
    Pair<SimpleStringProperty, Text> tempSP = null;
    for (Pair<SimpleStringProperty, Text> userName : users) {
      if (userName.getKey().getValue().equals(name)) {
        tempSP = userName;
      }
    }
    System.out.println("TESTTTT for remove name" + tempSP);
    users.remove(tempSP);
    Pair<SimpleStringProperty, Text> finalTempSP = tempSP;

    Platform.runLater(() -> vbox.getChildren().remove(finalTempSP.getValue()));


  }

  /**
   * An additional setup that is run if the user becomes or is the host. Adds an additional button
   * to the scene that is responsible for starting the game in multpilayer
   *
   * @param vbox The vbox that the start game button is added to
   */
  private void hostSetup(VBox vbox) {
    logger.info("Host setup is completed");
    System.out.println("Checl if HOST WORKSSSSSSSSSSSSSSSSSSSSSS");
    Button startButton = new Button("Start Game");
    startButton.getStyleClass().add("menuItem");
    Platform.runLater(() -> vbox.getChildren().add(0, startButton));
    startButton.setOnAction(actionEvent -> {
      communicator.send("START");
    });
  }

  /**
   * Change the name of the user
   *
   * @param name The name that the user want to be changed to
   */
  private void changeName(String name) {
    logger.info("Name changed to "+name);
    communicator.send("NICK " + name);
  }

  /**
   * Sends a message to the server and clears the textfield containing said message afterwards
   *
   * @param userMessage The textfield containing the string that we want to be sent to the server
   */
  private void sendMessage(TextField userMessage) {
    communicator.send("MSG " + userMessage.getText());
    userMessage.clear();
  }


  /**
   * A method to kill the timer
   */
  public void killTimer() {
    logger.info("Timer killed");
    timer.cancel();
    timer.purge();
  }

  public Chat getChat() {
    return chat;
  }

  /**
   * Handles a new user joining or an old user leaving the server
   * @param communication The message containg the info about a user joining /leaving
   * @param names The VBox conaing all of the current names
   */
  public void handleUser(String communication,VBox names){
    String tempString = communication.replace("USERS ", "");
    String[] tempArray = tempString.split("\n");
    for (String user : tempArray) {
      if (usersString.contains(user) != true) {
        SimpleStringProperty userSP = new SimpleStringProperty(user);
        Text userText = new Text();
        userText.getStyleClass().add("title");
        userText.textProperty().bind(userSP);
        users.add(new Pair<SimpleStringProperty, Text>(userSP, userText));
        usersString.add(userSP.getValue());
        Platform.runLater(() -> names.getChildren().add(userText));
      }
    }
    if (users.size() > tempArray.length) {
      boolean checker;
      ArrayList<String> removes = new ArrayList<>();
      for (Pair<SimpleStringProperty, Text> pair : users) {
        checker = false;
        for (String user : tempArray) {
          if (pair.getKey().getValue().equals(user)) {
            checker = true;
            break;
          }
        }
        if (checker == false) {
          removes.add(pair.getKey().getValue());
        }
      }
      if (removes.size() > 0) {
        for (String name : removes) {
          removeName(name, names);
        }
      }
    }
  }
}

