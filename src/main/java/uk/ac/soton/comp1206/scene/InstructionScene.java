package uk.ac.soton.comp1206.scene;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionScene extends BaseScene {
  private static final Logger logger = LogManager.getLogger(InstructionScene.class);
  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  StackPane instrPane;
  BorderPane instructionPane;
  public Grid grid;

  public InstructionScene(GameWindow gameWindow) {
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

    instrPane = new StackPane();
    instrPane.setMaxWidth(gameWindow.getWidth());
    instrPane.setMaxHeight(gameWindow.getHeight());
    instrPane.getStyleClass().add("instruction-background");
    root.getChildren().add(instrPane);

    instructionPane = new BorderPane();
    instrPane.getChildren().add(instructionPane);

    buildText();
    buildPBStore();
  }

  /**
   * Builds the text of the scene
   */
  public void buildText(){
    Text instructionTitle = new Text("Instructions");
    instructionTitle.getStyleClass().add("title");
    BorderPane.setAlignment(instructionTitle, Pos.CENTER);
    instructionPane.setTop(instructionTitle);
    //Adds the bullet point the character to the String value bullet using the ascii character
    int bulletPoint = 0x2022;
    String bullet = new String(Character.toChars(bulletPoint));
    VBox instructionText = new VBox();
    instructionText.setMaxWidth(instructionPane.getWidth() / 2);
    //A series of text boxes containing the instructions of the game with bullet points before them.
    Text instruction1 = new Text(bullet
        + "To play the game [lace the piece shown on the 3x3 grid on the righthand side of the challenge screen into the 5x5 grid. This can be done by clicking the grid\n");
    instruction1.getStyleClass().add("instructions");
    instruction1.setWrappingWidth(gameWindow.getWidth() / 2);
    Text instruction2 = new Text(bullet
        + "Then continue to do so with the next peices aiming to ceate a ful row or column in the 5x5 grid\n");
    instruction2.getStyleClass().add("instructions");
    instruction2.setWrappingWidth(gameWindow.getWidth() / 2);
    Text instruction3 = new Text(bullet
        + "Once a row or a colum is completed it will be cleared and and you will gain poimts\n");
    instruction3.getStyleClass().add("instructions");
    instruction3.setWrappingWidth(gameWindow.getWidth() / 2);
    Text instruction4 = new Text(bullet
        + "Continue to do this until you lose lives by the time running out at the bottom of the screen\n");
    instruction4.getStyleClass().add("instructions");
    instruction4.setWrappingWidth(gameWindow.getWidth() / 2);
    instructionText.getChildren().addAll(instruction1, instruction2, instruction3, instruction4);
    instructionPane.setLeft(instructionText);
  }

  /**
   * Builds the grid pane storing the gamepieces and dynamically fills it
   */
  public void buildPBStore(){
        /*
    A series of for loops creating the 15 different gamepiece of the game to display out to the user and placing them into a gridpane
     */
    GridPane pbStore = new GridPane();
    int counter = 0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 3; j++) {
        PieceBoard imagePB = new PieceBoard(3, 3, gameWindow.getHeight() / 7,
            gameWindow.getHeight() / 7);
        GamePiece gp = GamePiece.createPiece(counter);
        imagePB.setPiece(gp);
        counter++;

        imagePB.setPadding(new Insets(10));
        pbStore.add(imagePB, j, i);
      }
    }
    instructionPane.setRight(pbStore);
  }
}
