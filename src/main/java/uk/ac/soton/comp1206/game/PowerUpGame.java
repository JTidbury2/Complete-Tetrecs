package uk.ac.soton.comp1206.game;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.ScoreIncreaseListener;

public class PowerUpGame extends Game {

  private static final Logger logger = LogManager.getLogger(PowerUpGame.class);
  boolean powerupTimer=false;
  boolean clearBoard;
  Timer multTimer;
  Timer powerTimer;
  int powerValue=0;
  Timer timerTimer;




  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public PowerUpGame(int cols, int rows) {
    super(cols, rows);
  }

  /**
   * A method that fills the empty board places with blocks and then calls the after piece method
   * to clear the board. Toggles the clear board value so that clearing the board does not result
   * in any point gain, just a cleared baord
   */
  public void clearBoard() {
    for(int x=0; x<getCols();x++){
      for(int y = 0; y<getRows();y++){
        if(grid.get(x,y)==0){
          grid.playPiece(GamePiece.createPiece(3),x,y);

        }
      }
    }
    clearBoard=true;
    afterPiece();
    clearBoard=false;
  }

  /**
   * Also starts the powTimer which is responsible for reducing the powervalue of the game ever
   * second so that the powerups are hard to get
   */
  @Override
  public void initialiseGame() {
    super.initialiseGame();
    startPowTimer();
  }

  /**
   * Overrding by making it so the score of the game is only increased if the clearBoard value is
   * false (to counteract the bomb). Also calculates the increase in score and send it to the
   * handleincrease method
   * @param lines  The number of lines cleared this turn. If it is greater than 0, the multiplier is
   *               increases by 1. If none are cleared then it is reset back to 1
   * @param blocks The number of blocks cleared. The score of the game is incremented by the no of
   */
  @Override
  public void score(int lines, int blocks) {
    if(!clearBoard) {
      int increase=getScore().get();
      super.score(lines, blocks);
      increase=getScore().get()-increase;
      if(increase>0) {

        handleIncrease(increase);
      }
    }
  }

  /**
   * Adds the increase/5 to the powervalue and then sends the new power value to the powerUpScene
   * via a listner
   * @param increase
   */
  public void handleIncrease(int increase){
    powerValue=powerValue+(increase/5);

    scoreIncreaseListener.scoreIncreased(powerValue);

  }

  /**
   * Starts the power times that calls reducePowValue ever second
   */
  public void startPowTimer(){
    powerTimer=new Timer();
    powerTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        reducePowValue();
      }
    },0,1000);
  }

  /**
   * A method to cancel and purge the powerTimer
   */
  public void killPowTimer(){
    powerTimer.cancel();
    powerTimer.purge();
  }

  /**
   * A method that either reduces the powervalue by 1,or sets the value to 0 if the current
   * powervalue is between 0 and 100 or not
   */
  public void reducePowValue(){

    if(powerValue>=100) {
      powerValue=0;
      Platform.runLater(()->scoreIncreaseListener.scoreIncreased(0));
    }else if(powerValue>0) {
      powerValue-=1;

    }
    if(powerValue<0){
      powerValue=0;
    }
  }

  /**
   * A method that skips 2 pieces in the games
   */
  public void skipPieces(){
    nextPiece();
    nextPiece();
  }

  /**
   * The method that increase the life of the player and plays a sound
   */
  public void increaseLife(){
    lives.set(lives.getValue() + 1);
    music.playAudio("/sounds/lifegain.wav");
    logger.info("Lives set to: "+lives.get());
  }

  /**
   * A methiod that sets the base mult to 3 and creates a timer that calls reset mult in 10 seconds
   */
  public void increaseMult(){
    setBaseMult(3);
    multTimer= new Timer();
    multTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        resetMult();
      }
    },10000);


  }

  /**
   * Resets the base mult to 1 and cancals and purges the multTimer
   */
  public void resetMult(){
    setBaseMult(1);

    multTimer.cancel();
    multTimer.purge();
  }

  /**
   * Overriding the getTimerDelay to make it so if powerUpTimer is true they  get 15sec not the
   * njumber usually claculated
   * @return An int resembling the millis value used for the gameloop timer delay
   */
  @Override
  public int getTimerDelay() {
    if(powerupTimer){
      return 15000;
    }else {
      return super.getTimerDelay();
    }
  }

  /**
   * Sets the powerUpTimer value to true and schedueles a time to call resetTimerPower after 30
   * seconds
   */
  public void timePower(){
    powerupTimer=true;
    timerTimer = new Timer();
    timerTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        resetTimerPower();
      }
    },30000);
  }

  /**
   * Resets powerUpTimer to false an cancles and purges timerTimer
   */
  public void resetTimerPower(){
    powerupTimer=false;
    timerTimer.cancel();
    timerTimer.purge();

  }


  /**
   * A simple method calling each of the different ability methods depeding on what value is used
   * as the parameter
   * @param ability A int value represeting the ability gained by the power up
   */
  @Override
  public void handlePowerUps(int ability) {
    if(ability==0){
      increaseLife();
    }else if(ability==1){
      clearBoard();
    }else if(ability==2){
      increaseMult();
    }else if(ability==3){
      skipPieces();
    }else if(ability==4){
      timePower();
    }
  }

  /**
   * same as super but also kills the powTImer
   */
  @Override
  public void endGame() {
    super.endGame();
    killPowTimer();
  }
}
