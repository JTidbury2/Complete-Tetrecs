package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

public class Leaderboard extends ScoresList{
  private SimpleListProperty<Pair<String, Pair<Integer,Integer>>> mimicUiScores = new SimpleListProperty<>();
    public SimpleListProperty<Pair<String, Pair<Integer,Integer>>> uiScoresv2;
    boolean finalScores;

  /**
   * The constructor for the scores list, the custom ui element that shows the scores on the screen It
   * extends the vbox to allow stuff to be added to it
   *
   * @param name
   */
  public Leaderboard(String name,boolean finalScores) {
    super(name);
    this.finalScores=finalScores;
  }


  @Override
    public void addChangeListener() {
      uiScoresv2=new SimpleListProperty<>();
      uiScoresv2.addListener(new ListChangeListener<Pair<String,Pair<Integer,Integer>>>() {
        @Override
        public void onChanged(Change<? extends Pair<String,Pair<Integer,Integer>>> change) {
          System.out.println("Change noticed");
          revealMultScores(change);

        }
      });
    }

  /**
   * A the reveal scores method notices changes to the list of players and scores and adjusts
   * thier position in the table appropriatley. If they have run out of lives or let they will be
   * strike throughed .
   * @param change The change to the list of players and their scores and lives
   */
  public void revealMultScores(Change<? extends Pair<String,Pair<Integer,Integer>>> change) {
      System.out.println("TEST reveal check");
      while(change.next()){
        if (change.wasAdded()) {
          for(Pair<String,Pair<Integer,Integer>> pair :change.getAddedSubList()){

            System.out.println("Change was added");
            Text tempText = null;
            if(pair.getValue().getValue()!=0) {
              tempText = new Text(
                  (String) pair.getKey() + " " + pair.getValue().getKey().toString() + " "
                      + pair.getValue().getValue().toString());
              tempText.getStyleClass().add("scorelist");
            }else if (pair.getValue().getValue()==0){
              tempText = new Text(
                  (String) pair.getKey() + " " + pair.getValue().getKey().toString() + " "
                      + "DEAD");
              tempText.getStyleClass().add("scorelist");
              tempText.setStrikethrough(true);
            }
            int index = uiScoresv2.indexOf(pair);
            Text finalTempText = tempText;
            finalTempText.setWrappingWidth(scroller.getWidth());

            if(finalScores) {

              var tempFade = new FadeTransition(Duration.millis(3000), finalTempText);
              tempFade.setFromValue(0);
              tempFade.setToValue(1);

              tempFade.play();
            }
            Platform.runLater(()->scores.getChildren().add(index, finalTempText));

          }
        }else if (change.wasRemoved()) {
          System.out.println("Change was removed befor for loop");
        for(Pair<String,Pair<Integer,Integer>> pair :change.getRemoved()){
          System.out.println("Change was removed");
          System.out.println(change.getRemoved());
          int index =change.getTo();
          System.out.println(index);
          if(index != -1) {
            System.out.println("test if actually removed");
            Platform.runLater(() -> scores.getChildren().remove(index));

          }
        }
      }
      }
    }
}
