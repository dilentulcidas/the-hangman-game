import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.*;

public class MiniProject extends Application implements Serializable, EventHandler<ActionEvent>{

  private static final long serialVersionUID = 1L;
	
	List<String> chosenWords1 = new ArrayList<String>();
	List<String> chosenWords2 = new ArrayList<String>();
	List<String> chosenWords3 = new ArrayList<String>();
	List<String> chosenWords4 = new ArrayList<String>();
	
	List<List<String>> group = new ArrayList<List<String>>();
	
	
	// load files
  InputStreamReader file1 = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("files/file1.txt"));
  InputStreamReader file2 = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("files/file2.txt"));
  InputStreamReader file3 = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("files/file3.txt"));
  InputStreamReader file4 = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("files/file4.txt"));
  
  // contains file1,file2,file3,file4 words read respectively
  List<String> readFile1 = new ArrayList<String>();
  List<String> readFile2 = new ArrayList<String>();
  List<String> readFile3 = new ArrayList<String>();
  List<String> readFile4 = new ArrayList<String>();
  
  // load hangman images
  Image hangman0 = new Image(getClass().getResourceAsStream("images/hangman_0_post.png"));
  Image hangman1 = new Image(getClass().getResourceAsStream("images/hangman_1.png"));
  Image hangman2 = new Image(getClass().getResourceAsStream("images/hangman_2.png"));
  Image hangman3 = new Image(getClass().getResourceAsStream("images/hangman_3.png"));
  Image hangman4 = new Image(getClass().getResourceAsStream("images/hangman_4.png"));
  Image hangman5 = new Image(getClass().getResourceAsStream("images/hangman_5.png"));
  Image hangman6 = new Image(getClass().getResourceAsStream("images/hangman_6.png"));
  Image hangmanWon = new Image(getClass().getResourceAsStream("images/hangman_won.png"));
  
  
  // checks whether the user clicked submit or not on the first screen
  boolean hasClickedSubmit = false;

  //javafx stuff

  //scene1
	Button button;
  ToggleGroup grouped;
  RadioButton sequential;
  RadioButton parallel;
  Text text;
   
  //scene2
  Scene scene2;
  ToggleGroup grouped2;
  RadioButton easy;
  RadioButton medium;
  RadioButton hard;
  RadioButton extreme;
  Text text2;
  
  ////////////////
  ////GAME VARIABLES
  ///////////////
  
  // the word which was chosen and to be guessed by the player
  String chosenWord;
  // its respective saved version of the variable
  String saveChosenWord;
  
  // the list containing words of the difficulty chosen
  List<String> chosenDifficulty;
  
  // letters show on the gui, at the beginning they are all hidden
  private ObservableList<Node> letters;
  
  // number of guesses right
  int guessed;
  
  // number of guesses wrong
  int wrong;
  
  // number of failed attempts left
  int tries;
  
  // chooses a random word from a list
  public String chooseWord(List<String> words) {
    return words.get((int)(Math.random()*words.size()));
  }
  
  // the status of the letter buttons, contains information regarding if they were clicked or not
  List<Button> buttons;
  
  // save the current hangman image
  String saveImage = "";
  String saveWrong = "";
  String saveGuessed = "";
  String saveTries = "";
  String saveButtonsChosen = "";
  String saveLettersShown = "";
  List<String> buttonsChosen = new ArrayList<String>();
  List<String> lettersShown = new ArrayList<String>();
	
	public MiniProject(){
	}

	// put all the words in the file in a list
	 public void readFile(InputStreamReader file, List<String> readWords){
	   
	   BufferedReader bf = new BufferedReader(file);
		 
		 try{
		    while (bf.readLine() != null)
		        readWords.add(bf.readLine());
         } catch (Exception e) {
             System.out.println(e.getMessage());
           }
	}
	 
	 // puts the list of words in the mentioned list
	 public void storeWords(List<String> chosenWords, List<String> readWords) {
		    
		    try{
		    for (int i=0; i<50;i++){
			    Collections.shuffle(readWords);
			    chosenWords.add(readWords.get(1));
		    }
		    }
		    catch (IndexOutOfBoundsException e){
		    	System.out.println("Failed to store the words in a list: " + e.getMessage());
		    }
	}
	 
	 public String printList(List<String> chosenWord){
		 return Arrays.toString(chosenWord.toArray());
	 }
	 
	 public String printLists(List<List<String>> chosenWord){
		 return Arrays.toString(chosenWord.toArray());
	 }
	 
	 public void serialize(List<List<String>> group){
	        ObjectOutputStream outputStream = null;
	        try {
	            outputStream = new ObjectOutputStream(new FileOutputStream("serialized.ser"));
	            outputStream.writeObject(group);
	            System.out.println("Binary file generated successfully!");
	            outputStream.flush();
	            outputStream.close();
	            hasClickedSubmit = true;
	        } catch (FileNotFoundException ex) {
	        	System.out.println("File Not Found");
	        } catch (IOException ex) {
	            ex.getMessage();
	        } finally {
	            try {
	                if (outputStream != null) {
	                    outputStream.flush();
	                    outputStream.close();
	                }
	            } catch (IOException ex) {
	                ex.getMessage();
	            }
	        }
	    }

	public void singleThread(){
		 
			readFile(file1, readFile1);
			storeWords(chosenWords1, readFile1);
			//System.out.println("Easy: "+printList(chosenWords1));
			
			readFile(file2, readFile2);
			storeWords(chosenWords2, readFile2);
	    //System.out.println("Medium: "+printList(chosenWords2));
			
			readFile(file3, readFile3);
			storeWords(chosenWords3, readFile3);
			//System.out.println("Hard: "+printList(chosenWords3));
			
			readFile(file4, readFile4);
			storeWords(chosenWords4, readFile4);
			//System.out.println("Extreme: "+printList(chosenWords4));
			
			group.add(chosenWords1);
			group.add(chosenWords2);
			group.add(chosenWords3);
			group.add(chosenWords4);
			//System.out.println("Grouped List: "+printLists(group));
		   
			serialize(group);	
		
	}
	
	public synchronized void assistThread(InputStreamReader file, List<String> chosenWord, List<String> readWords){
		readFile(file, readWords);
		storeWords(chosenWord, readWords);
		group.add(chosenWord);
	}
	
	public void multiThread(){

	Thread t1 = new Thread(new Runnable(){ public void run(){
    assistThread(file1, chosenWords1, readFile1);
	}});
	
  Thread t2 = new Thread(new Runnable(){ public void run(){
    assistThread(file2, chosenWords2, readFile2);
  }});
  
  Thread t3 = new Thread(new Runnable(){ public void run(){
    assistThread(file3, chosenWords3, readFile3);
  }});
  
  Thread t4 = new Thread(new Runnable(){ public void run(){
    assistThread(file4, chosenWords4, readFile4);
  }});

  t1.start();
  t2.start();
  t3.start();
  t4.start();

  
  try {
    t1.join();
    t2.join();
    t3.join();
    t4.join();
    //System.out.println(printLists(group));
    serialize(group);
  } catch (InterruptedException e) {
    // do something if really needed otherwise not needed
  }
	
	}
    
	
	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
	  
	  Application.setUserAgentStylesheet(STYLESHEET_MODENA);
	  stage.setResizable(false);
	  
	  
	  //========================================================================
	  //FIRST SCREEN 
	  
		// set title
		stage.setTitle("Hangman Word Chooser");
	
		
		// set text for choice
		text = new Text("Please choose which mode you wish to choose from:");

		// set radio buttons for each threading mode
	    grouped = new ToggleGroup();
	    sequential = new RadioButton("Sequential");
	    sequential.setToggleGroup(grouped);
	    parallel = new RadioButton("Parallel");
	    parallel.setToggleGroup(grouped);
	    
		// set submit button
		button = new Button();
		button.setText("Submit");
		button.setOnAction(this);
		
		// set next arrowButton button    
		Image img = new Image(getClass().getResourceAsStream("images/arrow.png"));
		ImageView imgView = new ImageView(img);
    imgView.setImage(img);
		
		Button arrowButton = new Button();
		arrowButton.setGraphic(imgView);
		arrowButton.setMaxSize(1, 2);
		
		arrowButton.setOnAction(e -> {
		  // only allows to go to next screen once submit has been clicked
		  // that is, once a serialized has been generated
		  if (hasClickedSubmit == true){
		  stage.setScene(scene2);
	    stage.setTitle("Hangman Difficulty");
		  }
		  else {
		    Alert alert = new Alert(AlertType.INFORMATION);
	      alert.setTitle("Submit to proceed");
	      alert.setHeaderText("You have to submit before proceeding");
	      alert.setContentText("Please select one of the options and submit.");
	      alert.showAndWait();
		  }
		});
	    
	    // make layout
	    HBox top = new HBox();
	    top.getChildren().add(text);
	    top.setAlignment(Pos.CENTER);
	    top.setPadding(new Insets(10, 0, 0, 0));
	    
	    HBox middle = new HBox();
	    middle.getChildren().addAll(sequential,parallel);
	    middle.setAlignment(Pos.CENTER);
	    middle.setSpacing(15);
	    middle.setPadding(new Insets(0,0,0,0));
	    
	    HBox bottom = new HBox();
	    bottom.getChildren().addAll(button,arrowButton);
	    bottom.setAlignment(Pos.CENTER);
	    bottom.setSpacing(15);
	    bottom.setPadding(new Insets(0, 0, 10, 0));
	    
	    
	    BorderPane borderpane = new BorderPane();
	    borderpane.setTop(top);
	    borderpane.setCenter(middle);
	    borderpane.setBottom(bottom);
		
	    // make a scene to show the above
		Scene scene1 = new Scene(borderpane, 380, 120);
		stage.setScene(scene1);
		stage.show();
		
		//==============================================================================================
		// SECOND SCREEN
    
    // set text for choice
    text2 = new Text("Choose the difficulty:");

    // set radio buttons for each difficulty
      grouped2 = new ToggleGroup();
      easy = new RadioButton("Easy");
      easy.setToggleGroup(grouped2);
      medium = new RadioButton("Medium");
      medium.setToggleGroup(grouped2);
      hard = new RadioButton("Hard");
      hard.setToggleGroup(grouped2);
      extreme = new RadioButton("Extreme");
      extreme.setToggleGroup(grouped2);
      
    // set start button
    Button button2 = new Button();
    button2.setText("Start!");
    button2.setOnAction(e -> {
      /////////////////////////BEGIN START BUTTON HANDLING//////////////////////////////////////
      
      // no option selected
    if (grouped2.getSelectedToggle() == null){
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No option selected!");
      alert.setHeaderText("No option selected!");
      alert.setContentText("Please select an option.");
      alert.showAndWait();
    }
    
    // every time you start, you create a new list of words based on the level of difficulty, from which one word is chosen
    List<String> listOfWordsBasedOnDifficulty = new ArrayList<String>();
    
    // if nothing was serialized, that is, nothing is on the lists, then warn the user
      try{
          if (easy.isSelected()){
            listOfWordsBasedOnDifficulty.addAll(chosenWords1);
            chosenWord = chooseWord(listOfWordsBasedOnDifficulty);
            chosenDifficulty = chosenWords1;
          }
          
          if (medium.isSelected()){
            listOfWordsBasedOnDifficulty.addAll(chosenWords2);
            chosenWord = chooseWord(listOfWordsBasedOnDifficulty);
            chosenDifficulty = chosenWords2;
          }
          
          if (hard.isSelected()){
            listOfWordsBasedOnDifficulty.addAll(chosenWords3);
            chosenWord = chooseWord(listOfWordsBasedOnDifficulty);
            chosenDifficulty = chosenWords3;
          }
          
          if (extreme.isSelected()){
            listOfWordsBasedOnDifficulty.addAll(chosenWords4);
            chosenWord = chooseWord(listOfWordsBasedOnDifficulty);
            chosenDifficulty = chosenWords4;
          }
      } catch (Exception exception){
            Alert alert2 = new Alert(AlertType.ERROR);
            alert2.setTitle("Go back");
            alert2.setHeaderText("No word could be chosen!");
            alert2.setContentText("Please go back and choose one of the mentioned options and click submit.");
            alert2.showAndWait();
         }
      
      System.out.println("The chosen word: "+ chosenWord);
      
      // if the word was chosen, then start the game
      if (chosenWord != null){
        
        // setup a new scene for the game appearance
        Scene scene3 = new Scene(createBasicGUI());
        stage.setScene(scene3);
        stage.setTitle("Hangman Game");
        
      }
      ///////////////////////////////END START BUTTON HANDLING///////////////////////////////////////
    });
    
    // set back button
    Image reverseimg = new Image(getClass().getResourceAsStream("images/reversearrow.png"));
    ImageView reverseimgView = new ImageView(reverseimg);
    reverseimgView.setImage(reverseimg);
    
    Button reverseArrowButton = new Button();
    reverseArrowButton.setGraphic(reverseimgView);
    reverseArrowButton.setMaxSize(1, 2);
    
    reverseArrowButton.setOnAction(e -> {
      stage.setScene(scene1);
      stage.setTitle("Hangman Word Chooser");
    });
      
      // make layout
      HBox top1 = new HBox();
      top1.getChildren().add(text2);
      top1.setAlignment(Pos.CENTER);
      top1.setPadding(new Insets(10, 0, 0, 0));
      
      HBox middle1 = new HBox();
      middle1.getChildren().addAll(easy,medium,hard,extreme);
      middle1.setAlignment(Pos.CENTER);
      middle1.setSpacing(15);
      middle1.setPadding(new Insets(0,0,0,0));
      
      HBox bottom1 = new HBox();
      bottom1.getChildren().addAll(reverseArrowButton,button2);
      bottom1.setAlignment(Pos.CENTER);
      bottom1.setSpacing(15);
      bottom1.setPadding(new Insets(0, 0, 10, 0));
      
      BorderPane borderpane1 = new BorderPane();
      borderpane1.setTop(top1);
      borderpane1.setCenter(middle1);
      borderpane1.setBottom(bottom1);
    
      // make a scene to show the above
      scene2 = new Scene(borderpane1, 380, 120);

	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == button){
			if (sequential.isSelected()){
			    long startTime = System.currentTimeMillis();
				 
			    // empty the variables every time it is submitted again
			    chosenWords1 = new ArrayList<String>();
			    chosenWords2 = new ArrayList<String>();
			    chosenWords3 = new ArrayList<String>();
			    chosenWords4 = new ArrayList<String>();

			    group = new ArrayList<List<String>>();
			    
			    // run sequential
			    singleThread();

				long endTime   = System.currentTimeMillis();
				
				long totalTime = endTime - startTime;
				//System.out.println("Sequential ran for : "+totalTime+" milliseconds.");
				
				Alert alert = new Alert(AlertType.NONE);
				alert.setTitle("Sequential Threading done");
				alert.setHeaderText("Sequential Threading done");
				alert.setContentText("Time taken: "+ +totalTime+" milliseconds.");
				alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
				alert.show();
			}
			if (parallel.isSelected()){
			    long startTime = System.currentTimeMillis();
			    
	         // empty the variables every time it is submitted again
	         chosenWords1 = new ArrayList<String>();
	         chosenWords2 = new ArrayList<String>();
	         chosenWords3 = new ArrayList<String>();
	         chosenWords4 = new ArrayList<String>();

	         group = new ArrayList<List<String>>();
				 
	        // run parallel
			    multiThread();

				long endTime   = System.currentTimeMillis();
				
				long totalTime = endTime - startTime;
				//System.out.println("Parallel ran for : "+totalTime+" milliseconds.");
				Alert alert = new Alert(AlertType.NONE);
				alert.setTitle("Parallel Threading done");
				alert.setHeaderText("Parallel Threading done");
				alert.setContentText("Time taken: "+ +totalTime+" milliseconds.");
				alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
				alert.show();
			}
			if (grouped.getSelectedToggle() == null){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("No option selected!");
				alert.setHeaderText("No option selected!");
				alert.setContentText("Please select an option.");
				alert.showAndWait();
			}
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// CREATES JAVAFX GUI AND LOGIC OF THE GAME
	///////////////////////////////////////////////////////////////////////////////////////////////////
  public Parent createBasicGUI() {
    
    //Fonts
    Font labelFont = new Font("Candara Italic", 12.0);
    Font functionButtonsFont = new Font("Candara", 12.0);
    Font letterFont = new Font("Californian FB", 13.0);
    Font statusFont = new Font("Candara", 19.0);
    Font failedAttemptFont = new Font("Courier New Bold", 20.0);

    // create label
    Label label = new Label();
    label.setText("Made by Dilen Tulcidas");
    label.setLayoutX(434.0);
    label.setLayoutY(336.0);
    label.setPrefHeight(17.0);
    label.setPrefWidth(116.0);
    label.setFont(labelFont);
    label.setAlignment(Pos.CENTER);
    
    // create status label, says whether the guess was correct or not
    Label statusLabel = new Label();
    statusLabel.setText(" ");
    statusLabel.setLayoutX(350.0);
    statusLabel.setLayoutY(-4.0);
    statusLabel.setPrefHeight(39.0);
    statusLabel.setPrefWidth(182.0);
    statusLabel.setFont(statusFont);
    statusLabel.setAlignment(Pos.CENTER);
    
    // create number of failed attempts left label
    Label failedAttemptsLabel = new Label();
    failedAttemptsLabel.setText("Failed attempts left: "+6);
    failedAttemptsLabel.setLayoutX(-14.0);
    failedAttemptsLabel.setLayoutY(-3.0);
    failedAttemptsLabel.setPrefHeight(39.0);
    failedAttemptsLabel.setPrefWidth(385.0);
    failedAttemptsLabel.setFont(failedAttemptFont);
    failedAttemptsLabel.setAlignment(Pos.CENTER);
    
    
    // letter buttons
    Button a = new Button("A");a.setLayoutX(372.0);a.setLayoutY(39.0);a.setMnemonicParsing(false);a.prefHeight(13.0);a.prefWidth(0.0);a.setWrapText(true);a.setFont(letterFont);
    Button b = new Button("B");b.setLayoutX(409.0);b.setLayoutY(39.0);b.setMnemonicParsing(false);b.prefHeight(13.0);b.prefWidth(0.0);b.setWrapText(true);b.setFont(letterFont);   
    Button c = new Button("C");c.setLayoutX(447.0);c.setLayoutY(39.0);c.setMnemonicParsing(false);c.prefHeight(13.0);c.prefWidth(0.0);c.setWrapText(true);c.setFont(letterFont);
    Button d = new Button("D");d.setLayoutX(485.0);d.setLayoutY(39.0);d.setMnemonicParsing(false);d.prefHeight(13.0);d.prefWidth(0.0);d.setWrapText(true);d.setFont(letterFont);
    Button e = new Button("E");e.setLayoutX(372.0);e.setLayoutY(80.0);e.setMnemonicParsing(false);e.prefHeight(13.0);e.prefWidth(0.0);e.setWrapText(true);e.setFont(letterFont);
    Button f = new Button("F");f.setLayoutX(409.0);f.setLayoutY(80.0);f.setMnemonicParsing(false);f.prefHeight(13.0);f.prefWidth(0.0);f.setWrapText(true);f.setFont(letterFont);
    Button g = new Button("G");g.setLayoutX(447.0);g.setLayoutY(80.0);g.setMnemonicParsing(false);g.prefHeight(13.0);g.prefWidth(0.0);g.setWrapText(true);g.setFont(letterFont);
    Button h = new Button("H");h.setLayoutX(485.0);h.setLayoutY(80.0);h.setMnemonicParsing(false);h.prefHeight(13.0);h.prefWidth(0.0);h.setWrapText(true);h.setFont(letterFont);
    Button i = new Button("I");i.setLayoutX(372.0);i.setLayoutY(122.0);i.setMnemonicParsing(false);i.prefHeight(13.0);i.prefWidth(0.0);i.setWrapText(true);i.setFont(letterFont);
    Button j = new Button("J");j.setLayoutX(409.0);j.setLayoutY(122.0);j.setMnemonicParsing(false);j.prefHeight(13.0);j.prefWidth(0.0);j.setWrapText(true);j.setFont(letterFont);
    Button k = new Button("K");k.setLayoutX(447.0);k.setLayoutY(122.0);k.setMnemonicParsing(false);k.prefHeight(13.0);k.prefWidth(0.0);k.setWrapText(true);k.setFont(letterFont);
    Button l = new Button("L");l.setLayoutX(485.0);l.setLayoutY(122.0);l.setMnemonicParsing(false);l.prefHeight(13.0);l.prefWidth(0.0);l.setWrapText(true);l.setFont(letterFont);   
    Button m = new Button("M");m.setLayoutX(372.0);m.setLayoutY(164.0);m.setMnemonicParsing(false);m.prefHeight(13.0);m.prefWidth(0.0);m.setWrapText(true);m.setFont(letterFont);
    Button n = new Button("N");n.setLayoutX(409.0);n.setLayoutY(164.0);n.setMnemonicParsing(false);n.prefHeight(13.0);n.prefWidth(0.0);n.setWrapText(true);n.setFont(letterFont);
    Button o = new Button("O");o.setLayoutX(447.0);o.setLayoutY(164.0);o.setMnemonicParsing(false);o.prefHeight(13.0);o.prefWidth(0.0);o.setWrapText(true);o.setFont(letterFont);   
    Button p = new Button("P");p.setLayoutX(485.0);p.setLayoutY(164.0);p.setMnemonicParsing(false);p.prefHeight(13.0);p.prefWidth(0.0);p.setWrapText(true);p.setFont(letterFont); 
    Button q = new Button("Q");q.setLayoutX(372.0);q.setLayoutY(201.0);q.setMnemonicParsing(false);q.prefHeight(13.0);q.prefWidth(0.0);q.setWrapText(true);q.setFont(letterFont);
    Button r = new Button("R");r.setLayoutX(409.0);r.setLayoutY(201.0);r.setMnemonicParsing(false);r.prefHeight(13.0);r.prefWidth(0.0);r.setWrapText(true);r.setFont(letterFont);
    Button s = new Button("S");s.setLayoutX(447.0);s.setLayoutY(201.0);s.setMnemonicParsing(false);s.prefHeight(13.0);s.prefWidth(0.0);s.setWrapText(true);s.setFont(letterFont);
    Button t = new Button("T");t.setLayoutX(485.0);t.setLayoutY(201.0);t.setMnemonicParsing(false);t.prefHeight(13.0);t.prefWidth(0.0);t.setWrapText(true);t.setFont(letterFont); 
    Button u = new Button("U");u.setLayoutX(372.0);u.setLayoutY(243.0);u.setMnemonicParsing(false);u.prefHeight(13.0);u.prefWidth(0.0);u.setWrapText(true);u.setFont(letterFont);
    Button v = new Button("V");v.setLayoutX(409.0);v.setLayoutY(243.0);v.setMnemonicParsing(false);v.prefHeight(13.0);v.prefWidth(0.0);v.setWrapText(true);v.setFont(letterFont);
    Button w = new Button("W");w.setLayoutX(447.0);w.setLayoutY(243.0);w.setMnemonicParsing(false);w.prefHeight(13.0);w.prefWidth(0.0);w.setWrapText(true);w.setFont(letterFont);
    Button x = new Button("X");x.setLayoutX(485.0);x.setLayoutY(243.0);x.setMnemonicParsing(false);x.prefHeight(13.0);x.prefWidth(0.0);x.setWrapText(true);x.setFont(letterFont);
    Button y = new Button("Y");y.setLayoutX(410.0);y.setLayoutY(284.0);y.setMnemonicParsing(false);y.prefHeight(13.0);y.prefWidth(0.0);y.setWrapText(true);y.setFont(letterFont); 
    Button z = new Button("Z");z.setLayoutX(448.0);z.setLayoutY(284.0);z.setMnemonicParsing(false);z.prefHeight(13.0);z.prefWidth(0.0);z.setWrapText(true);z.setFont(letterFont);
    
    // ImageView of hangman status
    ImageView img = new ImageView();
    img.setImage(hangman0);
    img.setFitHeight(243.0);
    img.setFitWidth(173.0);
    img.setLayoutX(96.0);
    img.setLayoutY(12.0);
    img.setPickOnBounds(true);
    img.setPreserveRatio(true);
    
    // function buttons
    Button changeDifficulty = new Button("Change Difficulty");
    changeDifficulty.setLayoutX(262.0);
    changeDifficulty.setLayoutY(326.0);
    changeDifficulty.setFont(functionButtonsFont);
    
    Button saveGame = new Button("Save Game");
    saveGame.setLayoutX(100.0);
    saveGame.setLayoutY(326.0);
    saveGame.setFont(functionButtonsFont);
 
    Button loadGame = new Button("Load Game");
    loadGame.setLayoutX(14.0);
    loadGame.setLayoutY(326.0);
    loadGame.setFont(functionButtonsFont);
    
    Button newGame = new Button("New Game");
    newGame.setLayoutX(182.0);
    newGame.setLayoutY(326.0);
    newGame.setFont(functionButtonsFont);
    
    // LETTERBOXES - Guess word letters representation in the GUI. ex of chosenWord: AMFKDA , at the beginning shows blank in GUI

    FlowPane flowpane = new FlowPane();
    flowpane.setLayoutX(25.0);
    flowpane.setLayoutY(255.0);
    flowpane.setPrefHeight(58.0);
    flowpane.setPrefWidth(307.0);
    flowpane.setAlignment(Pos.CENTER);
    
        // gets all the corresponding letters of the chosen word and makes a GUI LetterBox for each one
   
          HBox rowOfLetters = new HBox();
          rowOfLetters.setAlignment(Pos.CENTER);
          
          for (int index = 0; index < chosenWord.length(); index++){
              rowOfLetters.getChildren().add(new LetterBox(chosenWord.charAt(index)));
          }
          
    flowpane.getChildren().add(rowOfLetters);    
    
    // create the pane/window layout of the game
    Pane pane = new Pane();
    pane.setPrefHeight(352.0);
    pane.setPrefWidth(548.0);
    pane.getChildren().addAll(label, statusLabel, failedAttemptsLabel,
        img,
        a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,
        flowpane,
        changeDifficulty, saveGame, loadGame, newGame);
    
    //////////////////////////////////////////
    //////////GAME EVENT HANDLING AND LOGIC
    /////////////////////////////////////////
    
    // stores all the letters of the GUI to this list variable declared at the beginning
    letters = rowOfLetters.getChildren();
    
    // number of guesses right
    guessed = 0;
    
    // number of guesses wrong
    wrong = 0;
    
    // number of guesses wrong
    tries = 6;
    
    // disable save game button in the beginning, so that later on enables once there is at least one guess
    saveGame.setDisable(true); 
    
    
    /////////////////////
    ////// Set the event handler of all the buttons
    /////////////////////
      
    ///////////////////////
     /////// alphabet letter buttons
        
          buttons = new ArrayList<Button>();
          Button[] arrayOfButtons = new Button[] {a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z};          
          
          // add all the letter buttons to a list
          buttons.addAll(Arrays.asList(arrayOfButtons));
          
          // set the handling for all the buttons in that list (a to z)
          for (Button letterButton : buttons){
            letterButton.setOnAction(event -> {
              
              // System.out.println("Button clicked: " + letterButton.getText());
            
              
            // disables the button when clicked
            letterButton.setDisable(true);
            
            // register the clicked button
           // lettersShown = new ArrayList<String>();
           // buttonsChosen = new ArrayList<String>();
            buttonsChosen.add(letterButton.getText());
            
            // check if the letter clicked belongs to the chosenWord     
            boolean belongs = false;
            String exclamationMarksCorrect = "";
            for (int number = 0; number<guessed; number++)
              exclamationMarksCorrect = exclamationMarksCorrect + "!";
            String exclamationMarksWrong = "";
            for (int number = 0; number<wrong; number++)
              exclamationMarksWrong = exclamationMarksWrong + "!";
            
            
            for (Node node : letters) {
              LetterBox letter = (LetterBox) node;
              if (letter.isEqualTo(letterButton.getText().charAt(0))){
                 belongs = true;
                 // if it belongs then show the respective letter
                 letter.show();
                 guessed++;
                 lettersShown.add(letterButton.getText());
                 statusLabel.setText("Correct Guess"+exclamationMarksCorrect);
                 statusLabel.setTextFill(Color.GREEN);
                }
            }
            
            // enable save game button once a correct guess is made
            if (guessed > 0)
              saveGame.setDisable(false);
            
            // if it does NOT belong
            if (!belongs){
              wrong++;
              statusLabel.setText("Wrong Guess"+exclamationMarksWrong);
              statusLabel.setTextFill(Color.RED);
              tries--;
              failedAttemptsLabel.setText("Failed attempts left: "+tries);
            }
            
            // change image depending on how many wrongs there were
            if (wrong == 1){
              img.setImage(hangman1);
            }
            if (wrong == 2){
              img.setImage(hangman2);
            }
            if (wrong == 3){
              img.setImage(hangman3);
            }
            if (wrong == 4){
              img.setImage(hangman4);
            }
            if (wrong == 5){
              img.setImage(hangman5);
            }
            
            // Status of the game
            
          //  System.out.println("Letters Shown: "+lettersShown + " Buttons Chosen: "+buttonsChosen);
            
            // what happens when the user lost
            if (wrong == 6){
              img.setImage(hangman6);
              
              // alerts the user that he lost
              Alert alert = new Alert(AlertType.INFORMATION);
              alert.setTitle("You lost!");
              alert.setHeaderText("You have lost the game");
              alert.setContentText("The man has died. Try again!");
              alert.showAndWait();
              
              // disable user from saving game
              saveGame.setDisable(true);
              
              // disable the user from clicking any more letter buttons
              for (Button button: buttons){
                button.setDisable(true);
              }
              
              // shows all the letters so that the user can see what the word was
              for (Node node : letters) {
                LetterBox letter = (LetterBox) node;
                letter.show();
              }
            }
            
            // what happens when the user wins
            if (guessed == chosenWord.length()){
              img.setImage(hangmanWon);
              
              // disable the user from clicking any more letter buttons
              for (Button button: buttons){
                button.setDisable(true);
              }
              
              // disable user from saving game
              saveGame.setDisable(true);
              
              Alert alert = new Alert(AlertType.NONE);
              alert.setTitle("You won!");
              alert.setHeaderText("You won the game");
              alert.setContentText("The man has survived. Congratulations!");
              alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
              alert.show();
            }
            
            
          });
          }  
      
      ///////////////////
      /////// Function buttons
          
          
          ////
          //// Change difficulty button
            changeDifficulty.setOnAction(event -> {
              Stage stage= (Stage) changeDifficulty.getScene().getWindow();
              stage.setScene(scene2);
              stage.setTitle("Hangman Difficulty");
            });
            
            
          ////
          //// New game button
            newGame.setOnAction(event -> {
              
            // reset the stats
            guessed = 0;
            wrong = 0;
            tries = 6;      
            saveLettersShown = "";
            saveButtonsChosen = "";
            
            // update failed attempts left label
            failedAttemptsLabel.setText("Failed attempts left: "+6);
            
            // reset status
            statusLabel.setText(" ");
            
            // reset image
            img.setImage(hangman0);
            
            // clear lists
            buttonsChosen.clear();
            lettersShown.clear();
            
            // choose new word randomly
            chosenWord = chooseWord(chosenDifficulty);
            
            // reset the letters GUI
            letters.clear();
            for (int index = 0; index < chosenWord.length(); index++) {
                letters.add(new LetterBox(chosenWord.charAt(index)));
            }
            
            // make all the buttons clickable
            for (Button button: buttons){
              button.setDisable(false);
            }
            
            // disable user from saving game until there is a correct guess
            saveGame.setDisable(true);
            
            System.out.println("The chosen word: "+ chosenWord);
            
            });
            
            
          ////
          //// Load Game button
              loadGame.setOnAction(event -> {
                
                Stage stage= (Stage) loadGame.getScene().getWindow();
                
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Save File", "*.ser")
                );
                File selectedFile = fileChooser.showOpenDialog(stage);

                if (selectedFile != null) {
                  
                  try {
                    FileInputStream fis =  new FileInputStream(selectedFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Object obj = ois.readObject();
                    ois.close();
                    
                    String savedData = obj.toString();
                    List<String> dataList = new ArrayList<String>();
                    
                   // put the data in the list
                    for (String value : savedData.split(" ")) { 
                         if(!value.equals(""))                                
                           dataList.add(value);  
                    }
                    
                    // store the data in different variables
                    String guessedFromSave = dataList.get(0);
                    guessedFromSave.replaceAll("\\s","");
                    
                    String wrongFromSave = dataList.get(1);
                    wrongFromSave.replaceAll("\\s","");
                    
                    String triesFromSave = dataList.get(2);
                    triesFromSave.replaceAll("\\s","");
                    
                    String imageLocationFromSave = dataList.get(3);
                    imageLocationFromSave.replaceAll("\\s","");
                    
                    String chosenWordFromSave = dataList.get(4);
                    chosenWordFromSave.replaceAll("\\s","");
                    
                    String lettersShownFromSave = dataList.get(5);
                    lettersShownFromSave.replaceAll("\\s","");
                    
                    String buttonsChosenFromSave = dataList.get(6);
                    buttonsChosenFromSave.replaceAll("\\s","");
                   
                //   System.out.println("============Save Data===============");
                //   System.out.println(guessedFromSave+" "+wrongFromSave+" "+triesFromSave+" "+imageLocationFromSave+" "+chosenWordFromSave
                //        +" "+lettersShownFromSave+" "+buttonsChosenFromSave);
                    
                    // use the new data to update the game state to the saved one
                    
                      // stats
                      guessed = Integer.valueOf(guessedFromSave);
                      wrong = Integer.valueOf(wrongFromSave);
                      tries = Integer.valueOf(triesFromSave);
                      
                      buttonsChosen.clear();
                      lettersShown.clear();
                      
                      
                      for (int index = 0; index < buttonsChosenFromSave.length(); index++){
                        buttonsChosen.add(Character.toString(buttonsChosenFromSave.charAt(index)));
                      }
                      
                      for (int index = 0; index < lettersShownFromSave.length(); index++){
                        lettersShown.add(Character.toString(lettersShownFromSave.charAt(index)));
                      }
                      
                      
                      // get image
                      Image savedImage = new Image(imageLocationFromSave);
                      img.setImage(savedImage);
                      
                      // change the chosen word
                      chosenWord = chosenWordFromSave;
                      
                      // save the number of tries text
                      failedAttemptsLabel.setText("Failed attempts left: "+tries);
                      
                      // reset status
                      statusLabel.setText(" ");
                      
                      // update letters GUI
                      letters.clear();
                      for (int index = 0; index < chosenWord.length(); index++){
                        rowOfLetters.getChildren().add(new LetterBox(chosenWord.charAt(index)));
                      }
                      
                      for (Node node : letters) {
                        LetterBox letter = (LetterBox) node;
                        for (int index = 0; index < lettersShownFromSave.length(); index++){
                            if (lettersShownFromSave.charAt(index) == letter.getChar()){
                               letter.show();
                            }
                        }
                      }
                      
                      // update buttons visibility
                      
                        // reset so that every button has the same clickability
                          for (Button button: buttons){
                            button.setDisable(false);
                          }
                        
                        // enable user from saving game
                          saveGame.setDisable(false);
                        
                        // disable clickability of the already pressed buttons
                          for (Button button: buttons){
                            for (int index = 0; index < buttonsChosenFromSave.length(); index++){
                              if (buttonsChosenFromSave.charAt(index) == button.getText().charAt(0)){
                                 button.setDisable(true);
                              }
                            }
                          } 

                  } catch (Exception e1) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error - could not open file");
                    alert.setHeaderText("Could not open file!");
                    alert.setContentText("Make sure you selected the correct file and try again.");
                    alert.showAndWait();
                  }
                  
                }
                else{
                  // canceled
                }
                
              });
         
              
          ////
          //// Save Game button
              saveGame.setOnAction(event -> {
                
                Stage stage= (Stage) saveGame.getScene().getWindow();
                
                
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save game");
                fileChooser.setInitialFileName("hangmansave.ser");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Save File (*.ser)", "*.ser")
                );
                File saveFile = fileChooser.showSaveDialog(stage);

                if (saveFile != null) {

                    try {
                      
                      
                      // save the variable values
                      saveGuessed = Integer.toString(guessed);
                      saveWrong = Integer.toString(wrong);
                      saveTries = Integer.toString(tries);
                      
                      // save the current hangman image, get the image file name as a string
                      if (img.getImage() == hangman0){
                        saveImage = "images/hangman_0_post.png";
                      }
                      if (img.getImage() == hangman1){
                        saveImage = "images/hangman_1.png";
                      }
                      if (img.getImage() == hangman2){
                        saveImage = "images/hangman_2.png";
                      }
                      if (img.getImage() == hangman3){
                        saveImage = "images/hangman_3.png";
                      }
                      if (img.getImage() == hangman4){
                        saveImage = "images/hangman_4.png";
                      }
                      if (img.getImage() == hangman5){
                        saveImage = "images/hangman_5.png";
                      }
                      if (img.getImage() == hangman6){
                        saveImage = "images/hangman_6.png";
                      }
                      if (img.getImage() == hangmanWon){
                        saveImage = "images/hangman_won.png";
                      }
                      
                      // store the chosen word
                      saveChosenWord = chosenWord;
                      
                      // store all the letters which were correct
                      saveLettersShown = "";
                      saveButtonsChosen = "";
                      
                      for (String letter : lettersShown){
                        saveLettersShown = saveLettersShown + letter;
                      }
                      
                      // store all the buttons clicked
                      for (String letter : buttonsChosen){
                        saveButtonsChosen = saveButtonsChosen + letter;
                      }
                      
                      //System.out.println("saveLettersShown: "+ saveLettersShown+" saveButtonsChosen: "+saveButtonsChosen);
                      
                      // Write the above variables in a file
                      
                      FileOutputStream fop = new FileOutputStream(saveFile);
                      ObjectOutputStream oos = new ObjectOutputStream(fop);
                      oos.writeObject(saveGuessed+" "+
                                      saveWrong+" "+
                                      saveTries+" "+
                                      saveImage+" "+
                                      saveChosenWord+" "+
                                      saveLettersShown+" "+
                                      saveButtonsChosen+" "
                                     );
                      //System.out.println("File saved!");
                      oos.close();
                      
                      
                    }
                    catch(Exception exception) {
                      Alert alert = new Alert(AlertType.ERROR);
                      alert.setTitle("Error occured while saving");
                      alert.setHeaderText("Could not save!");
                      alert.setContentText("Please try again.");
                      alert.showAndWait();
                    }

                }
                else {
                    // canceled
                }
                
              });
    
    
    // shows the GUI once the method is run
    return pane;
    
  }
  

}
