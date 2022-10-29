package uk.ac.soton.comp1206.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class ControlsScene extends BaseScene{
  private static final Logger logger = LogManager.getLogger(ControlsScene.class);
  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ControlsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {

  }

  /**
   * Builds the scene
   */
  @Override
  public void build() {
    logger.info("Building " +this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var controlPane = new StackPane();
    controlPane.setMaxWidth(gameWindow.getWidth());
    controlPane.setMaxHeight(gameWindow.getHeight());
    controlPane.getStyleClass().add("instruction-background");
    root.getChildren().add(controlPane);

    var instructionPane = new BorderPane();
    controlPane.getChildren().add(instructionPane);

    var content =
        new ImageView((new Image(this.getClass().getResource("/images/Instructions.png").toExternalForm())));
    content.setPreserveRatio(true);
    content.setFitWidth(gameWindow.getWidth());
    instructionPane.setCenter(content);


  }
}
