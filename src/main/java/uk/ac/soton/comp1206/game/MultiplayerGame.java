package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.ChallengeScene;

public class MultiplayerGame extends Game{

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  Communicator communicator;
  ArrayList<Integer> gamePieceNumbers = new ArrayList<>();
  int counter =0;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows,Communicator communicator) {
    super(cols, rows);
    this.communicator=communicator;
  }

  /**
   * Override of initialise game. Sets up the communicator listener and send PIECES to the server
   * 5 times to request the first 5 pieces of the online game to create a buffer
   */
  @Override
  public void initialiseGame() {
    logger.info("Initialised game");
    setUpComm();
    for(int i=5;i>0;i--){
      communicator.send("PIECE");
    }

  }

  /**
   * Similar to the standard next piece but removes the first gamePieceNumber from the arraylist,
   * moving up the queue and sending to the server for another piece to maintain the buffer
   */
  @Override
  public void nextPiece() {
    logger.info("Next piece of game is played and the next piece is requested from server");
    super.nextPiece();
    gamePieceNumbers.remove(0);
    communicator.send("PIECE");
  }

  /**
   * Ovverriding largly from the standard game, instead of randomly making a piece gets the
   * number from the queue of int numbers representing the gamepieces so all players get the same
   * peices
   * @return The game piece created from the number in the first place of the queue
   */
  @Override
  public GamePiece spawnPiece() {
    logger.info("New gamePiece created from the game piece number stored in gamePieceNumbers");
    return GamePiece.createPiece((int)(gamePieceNumbers.get(0)), (int) (Math.random() * 4));
  }

  /**
   * A method to setup the communicator listeners to add the piece number to the queue arraylist
   * when the msg is recieved from the server
   */
  private void setUpComm(){
    logger.info("Communicator response setup");
    communicator.addListener((communication -> {
      if(communication.startsWith("PIECE")){
        String tempString = communication.replace("PIECE ","");
        gamePieceNumbers.add(Integer.parseInt(tempString));
        counter++;
        if(counter==4){
          super.initialiseGame();
        }
      }
    }));
  }

  /**
   * Similar to standard afterPiece but also send the server a message containing the values of
   * the players current board using 2 for loops to get the information
   */
  @Override
  public void afterPiece() {
    super.afterPiece();
    String message="BOARD ";
    for(int x=0;x<5;x++){
      for(int y=0;y<5;y++){
        message=message+ getGrid().get(x,y)+" ";
      }
    }
    communicator.send(message);
  }
}
