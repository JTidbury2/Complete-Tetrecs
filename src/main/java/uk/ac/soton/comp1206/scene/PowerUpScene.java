package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import uk.ac.soton.comp1206.game.PowerUpGame;
import uk.ac.soton.comp1206.ui.GameWindow;

public class PowerUpScene extends ChallengeScene{

  Image[] images;
  ImageView powerImages;
  HBox powerStore;
  int power=0;
  EventHandler powerHandler;
  Timeline task;
  boolean extraTime=false;
  Timer timerTimer;



  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public PowerUpScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void build() {
    super.build();
    buildPowerupAdditions();
    buildImages();

  }

  public void setUpSIL(ProgressBar powerUpBar){
    game.setScoreIncreaseListener((powerValue -> {

      double proress = powerValue/100;

      if(powerValue!=0) {
        if(task!=null) {
          task.stop();
        }
        task = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(powerUpBar.progressProperty(), proress)
            ),
            new KeyFrame(Duration.millis(1000 * powerValue),
                new KeyValue(powerUpBar.progressProperty(), 0
                ))
        );
        task.playFromStart();
      }else if(powerValue==0){
          task.stop();
          powerUpBar.setProgress(0);
      }
      if(powerValue>=100){


        powerUpAnimation();
      }
    }));
  }

  public void buildImages(){
    Image heartImage = new Image(this.getClass().getResource("/images/heart.png").toExternalForm());
    Image bombImage = new Image(this.getClass().getResource("/images/bomb.png").toExternalForm());
    Image multiplierImage = new Image(this.getClass().getResource("/images/multiplier.png").toExternalForm());
    Image swapImage = new Image(this.getClass().getResource("/images/swap.png").toExternalForm());
    Image timerImage = new Image(this.getClass().getResource("/images/timer.png").toExternalForm());
    images= new Image[]{heartImage, bombImage, multiplierImage, swapImage, timerImage};
  }

  public void powerUpAnimation(){

    int startPower = (int) (Math.random()*5);

    AnimationTimer time = new AnimationTimer() {
      int swaps=15;
      @Override
      public void handle(long l) {
        doHandle();
      }

      public void doHandle(){
        power =(swaps+startPower)% images.length;
        powerImages.setImage(images[power]);

        powerImages.setFitWidth(powerStore.getWidth());
        powerImages.setFitHeight(powerStore.getHeight());
        swaps=swaps-1;
        if(swaps<=0){

          stop();
          buildClickable(power);
        }
      }
    };
    time.start();
  }

  public void buildClickable(int ability){
    powerHandler = new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        game.handlePowerUps(ability);
        powersUIHandle(ability);
        removeClickabale();
      }
    };

    powerImages.setOnMouseClicked(powerHandler);

  }
  
  public void powersUIHandle(int ability){
    if (ability == 0) {
      shakeTop(lives);
    }else if(ability ==2){
      shakeTop(multiplier);
    }else if(ability ==4){
      timePower();
    }
  }

  public void timePower(){
    extraTime=true;
    timerTimer = new Timer();
    timerTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        resetTimerPower();
      }
    },30000);
  }

  public void resetTimerPower(){
    extraTime=false;
    timerTimer.cancel();
    timerTimer.purge();

  }



  public void shakeTop(Node node){
    RotateTransition rotate = new RotateTransition();
    rotate.setAxis(Rotate.Z_AXIS);
    rotate.setByAngle(360);
    rotate.setCycleCount(1);
    rotate.setDuration(Duration.millis(2000));
    rotate.setAutoReverse(true);
    rotate.setNode(node);
    rotate.play();
  }

  public void removeClickabale(){
    powerImages.setImage(null);
    power=5;
  }



  public void buildPowerupAdditions(){
    VBox leftSide = new VBox();

    Text powerTitle = new Text("Power Ups");
    powerTitle.getStyleClass().add("title");


    ProgressBar powerUpBar = new ProgressBar(0);
    setUpSIL(powerUpBar);
    powerUpBar.setPrefSize(gameWindow.getWidth()/5,30);

    powerStore = new HBox();
    powerStore.setMaxSize(gameWindow.getWidth()/12,gameWindow.getWidth()/12);
    powerStore.setMinSize(gameWindow.getWidth()/12,gameWindow.getWidth()/12);
    powerStore.getStyleClass().add("power-background");

    powerImages=new ImageView();
    powerImages.setFitWidth(powerStore.getWidth());
    powerStore.getChildren().add(powerImages);

    leftSide.getChildren().addAll(powerTitle,powerUpBar,powerStore);
    leftSide.setAlignment(Pos.CENTER);

    mainPane.setLeft(leftSide);
  }

  @Override
  public void setupGame() {
    game=new PowerUpGame(5,5);
  }

  @Override
  public void setUptimelineListner(ProgressBar timeLine) {
    timeLine.progressProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValuer,
          Number newValue) {
        if(!extraTime) {
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
        }else if(extraTime){
          setBarStyleClass(timeLine, BLUE_BAR);
        }


      }
    });
  }
}
