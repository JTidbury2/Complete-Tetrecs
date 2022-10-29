package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LobbyScene extends BaseScene {

  Communicator communicator;
  private VBox channels;
  private ScrollPane scroller;
  private Timer timer;
  private boolean host = false;
  VBox createZone;
  ObservableList<String> channelsListOL;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow, Communicator communicator) {
    super(gameWindow);
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
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    //Set up the background root object for the LobbyScene
    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("lobby-background");
    root.getChildren().add(lobbyPane);

    var lobbyBorderPane = new BorderPane();
    lobbyPane.getChildren().add(lobbyBorderPane);

    VBox channelsAndTitle = new VBox();
    Text channelsTitle = new Text("Channels");
    channelsTitle.getStyleClass().add("title");

    /*
     Setting up the channels vbox to store the names of channels that are currently available for the user to join
     A scroller is used to store the VBox to allow the user to scroll between the different names of the channels
     */
    setUpChannelScroller();

    channelsAndTitle.getChildren().addAll(channelsTitle, scroller);
    lobbyBorderPane.setLeft(channelsAndTitle);
    /*
    Setting up the lists needed to store the channels
     */
    ArrayList<String> channelsListAl = new ArrayList<>();
    channelsListOL = FXCollections.observableArrayList((channelsListAl));
    SimpleListProperty<String> channelsListSL = new SimpleListProperty<String>(channelsListOL);
    /*
    Adding another vbox to the right hand side of the scene. This is used to allow the user to create a channel
     */
    setupCreateZone(lobbyBorderPane);
    /*
     Adding a listener to the communicator to allow the program to respond to different messages from
     the server.
     */
    setupComm();
    //Setting up a timer to request the list of channels available ever 10 seconds
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        communicator.send("LIST");
      }
    }, 1000, 1000);
  }

  /**
   * Sets up the communicator of the scene to respond to the many incoming communications
   */
  public void setupComm(){
    communicator.addListener(communication -> {
      System.out.println("Communication recieved was:" + communication + ".");

      if (communication.startsWith("CHANNELS") && communication.length() > 9) {
        System.out.println("Channels if statment part");
        String tempString = communication.replace("CHANNELS ", "");
        System.out.println("tempstring is:" + tempString + ".");
        String[] tempArray = tempString.split("\n");
        System.out.println(tempArray.length);
        for (String test : tempArray) {
          System.out.println("temp array contents" + test);
        }
        for (String chanels : tempArray) {
          if (channelsListOL.contains(chanels) != true) {
            System.out.println("else test");
            channelsListOL.add(chanels);
            System.out.println(channelsListOL);
            revealChanges(chanels);
          }
        }
      } else if (communication.startsWith("JOIN ")) {
        continuedJoinChannel(communication.replace("JOIN ", ""));

      } else if (communication.startsWith("ERROR")) {
        Text errorText = new Text(communication);
        errorText.getStyleClass().add("instructions");
        Platform.runLater(()-> createZone.getChildren().set(2,errorText));
      }

    });
  }

  /**
   * Builds the createzone of the lobby scene with a textfield and a button and error message
   * when needed
   * @param lobbyBorderPane The border pane into which the create zone is added
   */
  public void setupCreateZone(BorderPane lobbyBorderPane){
    createZone = new VBox();
    Button createButton = new Button("Create Channel");
    createButton.getStyleClass().add("menuItem");
    createZone.getChildren().add(createButton);
    lobbyBorderPane.setRight(createZone);
    final boolean[] buttonMade = {true};

    createButton.setOnAction(e -> {
      if (buttonMade[0]) {
        TextField createText = new TextField("Channel Name");
        createText.setOnAction(actionEvent -> {
          createChannel(createText.getText());
        });
        Text blank=new Text("");
        createZone.getChildren().addAll(createText,blank);
        buttonMade[0] = false;
      }

    });
  }

  /**
   * Sets up the fields channel and scroller
   */
  public void setUpChannelScroller(){
    channels = new VBox();
    channels.setSpacing(20);
    channels.setPadding(new Insets(10));
    channels.getStyleClass().add("scroller-pane");

    scroller = new ScrollPane();
    scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.getStyleClass().add("scroller-pane");
    scroller.setContent(channels);
    scroller.setFitToWidth(true);
  }

  /**
   * A method that reveals any additions to the list of channels available by creating a new button
   *
   * @param change Change is a string containg the name of the channel that has just been added
   */
  private void revealChanges(String change) {
    System.out.println("Reveal check");
    Button channelButton = new Button(change);
    System.out.println("Before sytle check");
    channelButton.getStyleClass().add("menuItem");
    System.out.println("After sytle check");
    channelButton.setOnAction(e -> {
      initialJoinChannel(channelButton.getText());
    });
    System.out.println("After button creation check");
    //Add animation
    Platform.runLater(() -> {
      channels.getChildren().add(channelButton);
    });


  }

  /**
   * A method to send the join message to the server to the channel by the name of the channel
   * specified
   *
   * @param channel The name of the channel that the join message is sent to
   */
  private void initialJoinChannel(String channel) {
    communicator.send("JOIN " + channel);
  }

  /**
   * The method that continues the joining process (only run after the join message has been
   * confirmed and recieved) It is responsible for killing the timer requesting the channels ( to
   * stop the channels messages coming) It also starts the chatScene after the timer has been
   * killed
   *
   * @param channel
   */
  private void continuedJoinChannel(String channel) {
    System.out.println("CountinuedJoinChannel check");
    killTimer();
    Platform.runLater(() -> {
      gameWindow.startChat(channel, host);
    });
  }

  /**
   * Send a message to the server to create a new channel and make the user tha created it the host
   *
   * @param name The name parameter is a string that contains the name of the channel that the user
   *             want to create
   */
  private void createChannel(String name) {
    host = true;
    communicator.send("CREATE " + name);
  }

  /**
   * The method to kill a timer and stop it requesting the list of channels available to the user.
   * It purges and cancels the timer.
   */
  public void killTimer() {
    timer.cancel();
    timer.purge();
  }


}
