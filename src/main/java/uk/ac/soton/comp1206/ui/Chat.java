package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

public class Chat extends VBox {

  ScrollPane scroller;
  VBox chat;
  Double width;
  Double height;
  Communicator communicator;
  Boolean scrollToBottom = false;

  /**
   * The constructor of the ui element representing the chat
   *
   * @param width        The width of the chat element
   * @param height       The height of the chat element
   * @param communicator The communicator that the chat element would use to pass messages to and
   *                     receive messages from
   */
  public Chat(double width, double height, Communicator communicator) {
    this.width = width;
    this.height = height;
    this.communicator = communicator;
    build();
  }

  /**
   * Builds the chat ui element
   */
  public void build() {
        /*
        Builds the chat elements vbox and scroller are used to store the messages
         */
    chat = new VBox();
    chat.setSpacing(20);
    chat.setPadding(new Insets(10));
    chat.getStyleClass().add("chat-pane");

    scroller = new ScrollPane();
    scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.getStyleClass().add("chat-pane");
    scroller.setContent(chat);
    scroller.setFitToWidth(true);
    scroller.setMinWidth(width);
    scroller.setMaxWidth(width);
    scroller.setFitToHeight(true);
    scroller.setPrefHeight(height);

    //Creates a text field that the user can type their message into.
    TextField userMessage = new TextField("Message");
    userMessage.setMinWidth(scroller.getWidth());
    userMessage.setOnAction(e -> {
      sendMessage(userMessage);
    });

    getChildren().addAll(scroller, userMessage);


  }

  /**
   * Sends the message contained in the textfield passed into the method to the server
   *
   * @param userMessage The textfield containing the message the user has typed
   */
  private void sendMessage(TextField userMessage) {
    communicator.send("MSG " + userMessage.getText());
    userMessage.clear();
  }

  /**
   * The method responsible for handling a message received from the server. It is responsible for
   * creating the text that holds the message and the name of the user that sent it
   *
   * @param communication A string containing the message received from the server
   */
  public void handleMessage(String communication) {
    String tempString = communication.replace("MSG", "");
    String[] tempArray = tempString.split(":");
    Text message = new Text("<" + tempArray[0] + "> " + tempArray[1]);
    message.setWrappingWidth(scroller.getWidth());
    message.getStyleClass().add("messages");
    Platform.runLater(() -> {
      chat.getChildren().add(message);
    });
    if (scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) {
      scrollToBottom = true;
    }
  }

  /**
   * The method responsible for returning the messages text box to the bottom
   */
  public void jumpToBottom() {
      if (!scrollToBottom) {
          return;
      }
    scroller.setVvalue(1.0f);
    scrollToBottom = false;
  }

  /**
   * A method that prints out any error messages personal to the played into the chat
   * @param communication The error message sent by the server
   */
  public void handleError(String communication){
    Text message = new Text(communication);
    message.getStyleClass().add("messages");
    Platform.runLater(() -> {
      chat.getChildren().add(message);
    });
    if (scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.9f) {
      scrollToBottom = true;
    }
  }
}
