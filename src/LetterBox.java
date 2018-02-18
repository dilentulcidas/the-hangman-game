import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

// Represents each block of the LetterBox to be guessed via GUI
public class LetterBox extends StackPane {
  
  /////// variables
  
    // the background of the LetterBox GUI
    private Rectangle background = new Rectangle(40, 60);
    
    // the LetterBox itself
    private Text text;
    
    // font
    private static final Font DEFAULT_FONT = new Font("Candara", 33);
 
  /////// methods

    public LetterBox(char LetterBox) {
        background.setFill(LetterBox == ' ' ? Color.GREEN : Color.WHITE);
        background.setStroke(Color.BLACK);

        text = new Text(String.valueOf(LetterBox).toUpperCase());
        text.setFont(DEFAULT_FONT);
        text.setVisible(false);
        setAlignment(Pos.CENTER);
        getChildren().addAll(background, text);
    }

    public void show() {
        RotateTransition rt = new RotateTransition(Duration.seconds(1), background);
        rt.setAxis(Rotate.Y_AXIS);
        rt.setToAngle(180);
        rt.setOnFinished(event -> text.setVisible(true));
        rt.play();
    }

    public boolean isEqualTo(char other) {
        return text.getText().equals(String.valueOf(other).toUpperCase());
    }
    
    public char getChar() {
      return text.getText().charAt(0);
    }
    
    public boolean isLetterShown(){
      
      boolean isVisible = false;
      if (text.isVisible() == true){
        isVisible = true;
      }
      else
        isVisible = false;
      
      return isVisible;
    }
}
