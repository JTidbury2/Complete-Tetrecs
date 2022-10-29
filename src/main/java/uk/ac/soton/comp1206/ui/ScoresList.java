package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.concurrent.TimeUnit;

public class ScoresList extends VBox {

  //The simplelistproperty that contains the scores on the screen
  public SimpleListProperty<Pair<String, Integer>> uiScores = new SimpleListProperty();
   VBox scores;
  protected ScrollPane scroller;
  public boolean changeAble = true;
  private String name;

  /**
   * The constructor for the scores list, the custom ui element that shows the scores on the screen
   * It extends the vbox to allow stuff to be added to it
   */
  public ScoresList(String name) {
    this.name=name;
    setPrefWidth(200);
    setSpacing(20);
    setPadding(new Insets(10, 10, 10, 10));
    setAlignment(Pos.TOP_CENTER);
    getStyleClass().add("ScoresList");
    // Add a change listener to the list containing scores to call revlealScores when the list is changed

    build();
  }
  public void addChangeListener(){
    uiScores.addListener(new ListChangeListener<Pair<String, Integer>>() {
      @Override
      public void onChanged(Change<? extends Pair<String, Integer>> change) {
        revealScores(change);

      }
    });
  }

  /**
   * Builds the ui score element
   */
  public void build() {

    Text title = new Text(name);
    title.getStyleClass().add("title");
    getChildren().add(title);
    scores = new VBox();

    scores.setSpacing(20);
    scores.setPadding(new Insets(10));
    scores.getStyleClass().add("scroller-pane");


    scroller = new ScrollPane();
    scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.getStyleClass().add("scroller-pane");
    scroller.setContent(scores);
    scroller.setFitToWidth(true);
    getChildren().add(scroller);

    addChangeListener();
  }

  /**
   * The method that reveals any changes to the list of scores to the ui display of the scores
   *
   * @param change The change to the list
   */
  public void revealScores(ListChangeListener.Change<? extends Pair<String, Integer>> change) {

    //If statement to check if anything has actually been added
    if (change.next()) {
      /*A for loop that goes through every paur in the changes's added sublist and adds a new
      text element into the correct spot on the leader board for it
       */
      for (Pair<String, Integer> pair : change.getAddedSubList()) {
        Integer tempIndex = uiScores.indexOf(pair);
        var tempText = new Text((String) pair.getKey() + " " + pair.getValue().toString());
        tempText.getStyleClass().add("scorelist");
        tempText.setWrappingWidth(scroller.getWidth());

        var tempFade = new FadeTransition(Duration.millis(3000), tempText);
        tempFade.setFromValue(0);
        tempFade.setToValue(1);
        scores.getChildren().add(tempIndex, tempText);
        tempFade.play();
      }


    }
  }

}
