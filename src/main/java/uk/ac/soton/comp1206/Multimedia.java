package uk.ac.soton.comp1206;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;

/**
 * A class to play the music and soundaffects
 */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  MediaPlayer audio;
  MediaPlayer music;
  Media audioMedia;
  Media musicMedia;
  static String musicPath;
  static boolean playNoise = false;
  static boolean playMusic = false;
  static SimpleBooleanProperty musicListenable = new SimpleBooleanProperty(false);

  public Multimedia(){
    musicListenable.addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean,
          Boolean t1) {
        if(t1==true){
          playBackgroundMusicMenu(musicPath);
        }else if(t1==false){
          stopBackgroundMenu();
        }
      }
    });
  }




  /**
   * The method to play a sound affect from the path provided in the string parameter
   *
   * @param path The path of the audio file that the method should play
   */
  public void playAudio(String path) {
    logger.info("Noise played: "+path);
    if (playNoise) {
      audioMedia = new Media(getClass().getResource(path).toExternalForm());
      audio = new MediaPlayer(audioMedia);
      audio.play();
    }
  }

  /**
   * A method to play music in the background of a scene. Once the music file has ended the method
   * is set to replay it
   *
   * @param path The path of the audio file to be played in the background as a string
   */
  public void playBackgroundMusicMenu(String path) {
    musicPath=path;
    if (playMusic) {

      musicMedia = new Media(getClass().getResource(path).toExternalForm());
      music = new MediaPlayer(musicMedia);
      music.setOnEndOfMedia(new Runnable() {
        @Override
        public void run() {
          music.seek(Duration.ZERO);
          music.play();
        }
      });
      music.play();
    }
  }

  /**
   * A method to stop the background music played by this multimedia class
   */
  public void stopBackgroundMenu() {
    if (this.music!=null) {
      music.stop();
      music=null;
    }
  }

  public static void setPlayNoise(boolean value){
    Multimedia.playNoise=value;
  }

  public static void setPlayMusic(boolean playMusic) {
    Multimedia.playMusic = playMusic;
    Multimedia.musicListenable.set(playMusic);
  }
  public static boolean getPlayNoise(){
    return Multimedia.playNoise;
  }

  public static boolean getPlayMusic(){
    return Multimedia.playMusic;
  }
}
