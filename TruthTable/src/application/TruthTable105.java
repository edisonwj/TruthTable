package application;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
* This application generates truth tables for the specified logical formulas.
* Formula operators are generally evaluated right to left without precedence.
*
* @author William Edison
* @version 1.05 August 2020
*
* Formulas may be typed into the text field, or a text file of formulas
* may be loaded using the Main Menu Load Data option. Formulas input
* using either the input field or from a text file are accumulated in the
* scrollable Combo Box area at the bottom of the screen and can be selected
* for processing.
*
* Results may be saved as an image file in various formats (bmp, jpg, png, gif)
* using the Main Menu Save Scene option.
*
* Results may be shown for:
* 	- all columns (View All option),
* 	- all operators (View Operators option),
* 	- or just the propositional variables and final evaluation (View Final option).
*
* The following character representations for logical operators are supported:
* 	- AND - Conjunction
*		p & q
*		p * q
*		(p)(q)
*
*	- OR - Disjunction
*		p | q
*		p + q
*	- Implication
*		p > q
*	- Backward Implication
*		p < q
*	- NAND
*		p ^ q
*	- NOR
*		p v q
*	- XNOR - Equivalence
*		p = q
*	- XOR
*		p : q
*	- NOT - Negation
*		!p
*		~p
*
*/

public class TruthTable105 extends Application {

    private final double sceneWidth = 1400;
    private final double sceneHeight = 700;
    private Stage stage;
    private Scene scene;
    private BorderPane border;
    private GridPane grid;
    private ScrollPane scrollPane;
    private ComboBox<String> comboBox;
    private TextField inputField;
    private ObservableList<String> data = FXCollections.observableArrayList();
    private Formula fm;
    private boolean allView = true;
    private boolean operatorsView = false;
    private boolean tautology = true;
    private boolean contradiction = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        border = new BorderPane();
        border.setTop(addMenuBar());
        border.setBottom(addVBox());
        scrollPane = new ScrollPane();
        border.setCenter(scrollPane);

        scene = new Scene(border, sceneWidth, sceneHeight);
    	stage.setTitle("Truth Table 1.05");
    	stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates a text input field and a combo box input area.
     *
     * @return VBox box
     */
    private VBox addVBox() {
        inputField = new TextField();
        inputField.setText("Enter Formula");
        inputField.setStyle("-fx-text-inner-color: black;");
        inputField.setFont(new Font("ARIAL", 14));

        inputField.setOnAction(e -> {
        							  tautology = true;
        							  contradiction = true;
	    							  scrollPane.setContent(null);
        							  String fmInput = inputField.getText();
        							  data.add(fmInput);
        							  fm = new Formula(fmInput);
        							  if (fm.getWellFormed()) {
	        							  buildTable(fm);
	        						      scrollPane.setContent(grid);
	        						      if (tautology) {
	        						    	  inputField.setText(fmInput + "  Tautology");
	        						      }
	        						      else if (contradiction ){
	        						    	  inputField.setText(fmInput + "  Contradiction");
	        						      }
	        						      else {
	        						    	  inputField.setText(fmInput + "  Neither Tautology nor Contradiction");
	        						      }
        							  }
        							  else {
        								  String message = fm.getErrorMessage();
        								  inputField.setText(fmInput + " - Formula not well formed - " + message);
        							  }
        							});

    	comboBox = new ComboBox<>();
    	comboBox.itemsProperty().setValue(data);
        comboBox.setOnAction(e -> {
			  						tautology = true;
			  						contradiction = true;
    							    scrollPane.setContent(null);
        							String fmInput = comboBox.getValue();
        							inputField.setText(fmInput);
        							fm = new Formula(fmInput);
      							    if (fm.getWellFormed()) {
        							    buildTable(fm);
        						        scrollPane.setContent(grid);
        						        if (tautology) {
        						        	inputField.setText(fmInput + "  Tautology");
	        						    }
	        						    else if (contradiction ){
	        						    	inputField.setText(fmInput + "  Contradiction");
	        						    }
	        						    else {
	        						    	inputField.setText(fmInput + "  Neither Tautology nor Contradiction");
	        						    }
    							    }
    							    else {
	      								String message = fm.getErrorMessage();
	      								inputField.setText(fmInput + " - Formula not well formed - " + message);
    							    }
        						  });

        VBox box = new VBox(inputField, comboBox);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: lightblue");
        return box;
    }

    /**
     * Creates a Menu Bar containing Main, View, and Help menus.
     *
     * @return MenuBar menuBar
     */
    private MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: lightblue");
        Menu mainMenu = new Menu("Main");

        MenuItem loadDataItem = new MenuItem("Load Data");
        loadDataItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	FileChooser fileChooser = new FileChooser();
            	fileChooser.setTitle("Load Data File");
               	String currentDirectory = findDefaultDirectory();
            	fileChooser.setInitialDirectory(new File(currentDirectory));
            	fileChooser.getExtensionFilters().addAll(
            	         new FileChooser.ExtensionFilter("Data Files", "*.txt"));
            	File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                	System.out.println("Load file: " + file.toString());
                	readData(file);
                }
            }
        });

        Menu saveSceneMenu = new Menu("Save Scene");
        MenuItem bmpItem = new MenuItem("bmp");
        MenuItem jpgItem = new MenuItem("jpg");
        MenuItem pngItem = new MenuItem("png");
        MenuItem gifItem = new MenuItem("gif");
        saveSceneMenu.getItems().addAll(bmpItem, jpgItem, pngItem, gifItem);

        bmpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	        	saveScene(scene, "bmp");
	        }
	    });

        jpgItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	        	saveScene(scene, "jpg");
	        }
	    });

        pngItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	        	saveScene(scene, "png");
	        }
	    });

        gifItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	        	saveScene(scene, "gif");
	        }
	    });

        mainMenu.getItems().addAll(loadDataItem, saveSceneMenu);

        Menu viewMenu = new Menu("View");
        MenuItem allItem = new MenuItem("All");
        MenuItem operatorsItem = new MenuItem("Operators");
        MenuItem finalItem = new MenuItem("Final");

        viewMenu.getItems().addAll(allItem, operatorsItem, finalItem);

        allItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	            	allView = true;
	            	operatorsView = false;
            	if (fm != null) {
					buildTable(fm);
				    scrollPane.setContent(grid);
				    border.setCenter(scrollPane);
            	}
	        }
	    });

        operatorsItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	            	allView = false;
	            	operatorsView = true;
	            if (fm != null) {
					buildTable(fm);
				    scrollPane.setContent(grid);
				    border.setCenter(scrollPane);
            	}
	        }
	    });

        finalItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
	            	allView = false;
	            	operatorsView = false;
	            if (fm != null) {
					buildTable(fm);
				    scrollPane.setContent(grid);
				    border.setCenter(scrollPane);
            	}
            }
	    });

        Menu helpMenu = new Menu("Help");
        MenuItem helpItem = new MenuItem("Show Help Text");
        helpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	scrollPane.setContent(addHelpBox());
            }
        });
        helpMenu.getItems().addAll(helpItem);

        menuBar.getMenus().addAll(mainMenu, viewMenu, helpMenu);

        return menuBar;
    }

    /**
     * Finds the default file directory for the selection dialogue.
     *
     * @return String currentDirectory
     */
    private String findDefaultDirectory() {
    	String currentDirectory = null;
    	String userDir = System.getProperty("user.dir");
    	currentDirectory = userDir + "\\src\\Resources";
    	File dir = new File(currentDirectory);
		if (!dir.exists()) {
			currentDirectory = userDir + "\\Resources";
			dir = new File(currentDirectory);
			if (!dir.exists()) {
				currentDirectory = userDir;
			}
    	}
    	return currentDirectory;
    }

    /**
     * Reads a text file of logic formulas into a combo box, allowing for selected evaluation.
     *
     * @param File file path/name of the text file to be read
     */
    private void readData(File file) {

    	int dataSize = data.size();
    	try {
			String lineString;
			int lineNumber = 0;
			BufferedReader in = new BufferedReader(new FileReader(file));

			while ((lineString = in.readLine()) != null) {
				lineNumber++;
				lineString = lineString.trim();
				System.out.println("line# " + lineNumber + " " + lineString);
				StringTokenizer st = new StringTokenizer(lineString, ",; ");
				if (st.nextToken().equals("//")) {
					continue;
				}
				data.add(lineString);
			}
			in.close();
		}
		catch (IOException e) {
			System.out.println("File error: " + e);
		}
    	tautology = true;
    	contradiction = true;
    	String fmInput = data.get(dataSize);
		inputField.setText(fmInput);
		Formula fm = new Formula(fmInput);
		if (fm.getWellFormed()) {
			buildTable(fm);
	        scrollPane.setContent(grid);
	        border.setCenter(scrollPane);
	        if (tautology) {
	        	inputField.setText(fmInput + "  Tautology");
		    }
		    else if (contradiction ){
		    	inputField.setText(fmInput + "  Contradiction");
		    }
			else {
			   	inputField.setText(fmInput + "  Neither Tautology nor Contradiction");
			}
		}
	    else {
			String message = fm.getErrorMessage();
			inputField.setText(fmInput + " - Formula not well formed - " + message);
	    }
	}

    /**
     * Creates an image file of the truth table.
     *
     * @param Scene		scene containing the truth table.
     * @param String	format for the image file: bmp, jpg, png, gif
     */
	private void saveScene(Scene scene, String format) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Scene " + format);
       	String currentDirectory = findDefaultDirectory();
        fileChooser.setInitialDirectory(new File(currentDirectory));
        fileChooser.getExtensionFilters().addAll(
   	         new FileChooser.ExtensionFilter("Image File", "*." + format));
        File file = fileChooser.showSaveDialog(stage);
        System.out.println("file= " + file);
        if (file != null) {
        	WritableImage writableImage=scene.snapshot(null);
        	if (format == "png" || format == "gif") {
            	try {
            		ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), format, file);
            	} catch (IOException ex) {
            		System.out.println(ex.getMessage());
            	}
        	}
        	else if (format == "bmp" || format == "jpg") {
	        	BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);  // Get buffered image.
	        	BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE); // Remove alpha-channel from buffered image.
	        	Graphics2D graphics = imageRGB.createGraphics();
	        	graphics.drawImage(image, 0, 0, null);
	        	try {
	        		ImageIO.write(imageRGB, format, file);
            	} catch (IOException ex) {
            		System.out.println(ex.getMessage());
            	}
	        	graphics.dispose();
        	}
        }
	}

	/**
	 * Shows help text.
	 */
	private VBox addHelpBox() {
		Label helpText = new Label(
				" * This application generates truth tables for specified logical formulas.\n" +
				" * Formula operators are generally evaluated right to left without precedence.\n" +
				" *\n" +
				" * Formulas may be typed into the text field, or a text file of formulas\n" +
				" * may be loaded using the Main Menu Load Data option. Formulas input\n" +
				" * using either the input field or from a text file are accumulated in the\n" +
				" * scrollable Combo Box area at the bottom of the screen and can be selected\n" +
				" * for processing.\n" +
				" *\n" +
				" * Results may be saved as an image file in various formats (bmp, jpg, png, gif)\n" +
				" * using the Main Menu Save Scene option.\n" +
				" *\n" +
				" * Results may be shown for:\n" +
				" * 	- all columns (View All option),\n" +
				" * 	- all operators (View Operators option),\n" +
				" * 	- or just the propositional variables and final evaluation (View Final option).\n" +
				" *\n" +
				" * The following character representations for logical operators are supported:\n" +
				" * 	- AND - Conjunction\n" +
				" *		p & q\n" +
				" *		p * q\n" +
				" *		(p)(q)\n" +
				" *	- OR - Disjunction\n" +
				" *		p | q\n" +
				" *		p + q\n" +
				" *	- Implication\n" +
				" *		p > q\n" +
				" *	- Backward Implication\n" +
				" *		p < q\n" +
				" *	- NAND\n" +
				" *		p ^ q\n" +
				" *	- NOR\n" +
				" *		p v q\n" +
				" *	- XNOR - Equivalence\n" +
				" *		p = q\n" +
				" *	- XOR\n" +
				" *		p : q\n" +
				" *	- NOT - Negation\n" +
				" *		!p\n" +
				" *		~p\n");

        VBox helpBox = new VBox(helpText);
		return helpBox;
	}


	/**
	 * Builds truth table for display.
	 *
	 * @param Formula fm to be evaluated.
	 */

    private void buildTable(Formula fm) {
    	grid = new GridPane();
        grid.setStyle("-fx-background-color: lightgray");
        grid.setGridLinesVisible(true);

        int numPvars = fm.getpNum();						// Number of propositional variables
    	int numOps = fm.getoNum();							// Number of logical operators
    	int nvals = (int) Math.pow(2, numPvars);			// Number of valuations
    	boolean[] v = new boolean[numPvars];				// Truth assignments to variables for a specific valuation
        boolean[] vr = new boolean[numPvars+numOps+1];		// Evaluations for each logical operator
        int d, q, r = 0;

    	String f2 = fm.getFormula2();
    	int f2Size = f2.length();
    	Text[] p = new Text[numPvars + 1 + f2Size + 2];		// Row of text assignments for the grid
    	String trueString = " true ";
    	String falseString = " false ";

    	// Set variable name titles
    	for (int i = 0; i < numPvars; i++) {
        	p[i] = new Text(" " + fm.getpName(i) + " ");
            p[i].setFont(new Font("ARIAL",14));
        	grid.add(p[i], i, 0);
    	}
    	p[numPvars] = new Text("  ");
    	grid.add(p[numPvars], numPvars, 0);

    	// Set other titles
    	if (allView | operatorsView) {
			String nums = "0123456789";
			for (int i = 0; i < f2Size; i++) {
	    		int ti = numPvars + 1 + i;
	    		char c = f2.charAt(i);
	    		if (nums.indexOf(c) >= 0) {
		    		String s = "" + c;
    				while (i+1 < f2.length()) {
        				c = f2.charAt(i+1);
        				if (nums.indexOf(c) >= 0) {
        					s = s + c;
        					i++;
        				}
        				else {
        					break;
        				}
    				}
					int ix = Integer.parseInt(s);
					p[ti] = new Text(" " + fm.getpName(ix) + " ");
	    		}
	    		else {
	    			p[ti] = new Text("   " + c + " ");
	    		}
	            p[ti].setFont(new Font("ARIAL",14));
	        	grid.add(p[ti], ti, 0);
	    	}
	    	p[numPvars + 1 + f2Size] = new Text("  ");
	    	grid.add(p[numPvars + 1 + f2Size], numPvars + 1 + f2Size, 0);
    	}

    	// Set full formula title
        p[numPvars + 1 + f2Size + 1] = new Text(" " + fm.getFormula1() + " ");
        p[numPvars + 1 + f2Size + 1].setFont(new Font("ARIAL",14));
    	grid.add(p[numPvars + 1 + f2Size + 1], numPvars + 1 + f2Size + 1, 0);

    	// Set constant values
       	if (allView) {
	    	ArrayList<Integer> fList = fm.getConstantF();				// Set True constant values
	    	for (int i = 0; i < fList.size(); i++) {
	    		for (int j = 0; j < nvals; j++) {
		    		Text falseText = new Text(falseString);
		    		falseText.setFont(new Font("Arial", 14));
		    		grid.add(falseText,  numPvars + 1 + fList.get(i), j+1);
	    		}
	    	}

	    	ArrayList<Integer> tList = fm.getConstantT();				// Set False constant values
	    	for (int i = 0; i < tList.size(); i++) {
	    		for (int j = 0; j < nvals; j++) {
		    		Text trueText = new Text(trueString);
		    		trueText.setFont(new Font("Arial", 14));
		    		grid.add(trueText,  numPvars + 1 + tList.get(i), j+1);
	    		}
	    	}
    	}

    	//Cycle through all possible truth values
    	ArrayList<ArrayList<Integer>> propList = fm.getPropList();		// Get lists of all propositional variable locations
//    	System.out.println("nvals= " + nvals);
    	for (int i = nvals - 1; i >= 0; i--) {
    		if (i > 0 && i%1000 == 0)
    			System.out.println("i= " + i);
    		//Set variable assignment
        	d = (int) Math.pow(2, numPvars-1);
        	r = i;
        	for (int j = 0; j < numPvars; j++) {
        		ArrayList<Integer> pList = propList.get(j);				// Get list of locations for propositional variable j
        		q = r/d;
        		r = i%d;
         		String value = "";
        		if (q >= 1) {
        			v[j] = true;
        			value = trueString;
        		}
        		else {
        			v[j] = false;
        			value =  falseString;
        		}
        		Text truthText = new Text(value);						// Insert blank column
        		truthText.setFont(new Font("Arial", 14));
        		grid.add(truthText,  j,  nvals-i);

        		if (allView) {
	        		for (int k = 0; k < pList.size(); k++) {			// Insert propositional variable assignment values
	        			truthText = new Text(value);
	        			truthText.setFont(new Font("Arial", 14));
	        			grid.add(truthText,  numPvars + 1 + pList.get(k),  nvals-i);
	        		}
        		}

        		d = (int) Math.pow(2, numPvars-j-2);
        	}

        	//Evaluate formula for each valuation
        	vr = fm.evalFormula(v);
        	HashMap<Integer, Integer> hmr = fm.getHmr();
        	int vj = 0;
        	if (allView | operatorsView) {
	         	for (int j = 0; j < vr.length-1; j++) {					// Insert intermediate expression valuation
	         		vj = numPvars + 1 + hmr.get(j);
	  	    		if (vr[j]) {
		    	        Text trueText = new Text(trueString);
		    	        trueText.setFont(new Font("ARIAL",14));
		    			grid.add(trueText, vj, nvals-i);
		    		}
		    		else {
		    	        Text falseText = new Text(falseString);
		    	        falseText.setFont(new Font("ARIAL",14));
		    			grid.add(falseText, vj, nvals-i);
		    		}
	         	}
        	}

 			vj = numPvars + 1 + f2Size + 1;  							// Insert final formula evaluation
	  		if (vr[vr.length-1]) {
		        Text trueText = new Text(trueString);
		        trueText.setFont(new Font("ARIAL",14));
				grid.add(trueText, vj, nvals-i);
				contradiction = false;
			}
			else {
		        Text falseText = new Text(falseString);
		        falseText.setFont(new Font("ARIAL",14));
				grid.add(falseText, vj, nvals-i);
				tautology = false;
	        }
       	}
    }

    private class Formula
    /**
     * This class instantiates a logic formula parses it and provides methods for evaluating the formula
     * given a truth value assignment.
     */
    {
    	private String f1 = new String();						// Input string
    	private String f2 = new String();						// Parsed string - formatted for evaluation
    	private ArrayList<String> pSet = new ArrayList<>();		// Propositional variables in formula f1
    	private ArrayList<String> oSet = new ArrayList<>(); 	// Operators in formula f1 and f2
    	private ArrayList<Integer> oxSet = new ArrayList<>();	// Indices of operators in f2
    	private ArrayList<ArrayList<Integer>> propList = new ArrayList<ArrayList<Integer>>(); // Indices in f2 for each propositional variable
    	private ArrayList<Integer> constantT = new ArrayList<>();	// Indices in f2 of T occurrences
    	private ArrayList<Integer> constantF = new ArrayList<>();	// Indices in f2 of F occurrences
    	private int pNum = 0;									// Number of distinct propositional variables in f1 and f2
    	private int oNum = 0;									// Number of distinct operators in f1 and f2
    	private boolean wellFormed = true;						// Flag indicating the formula is well formed
    	private String errorMessage = "";						// General error message
    	private String parenMessage = "";						// Unbalanced parenthesis message
    	private String unknownOperatorMessage = "";				// Unknown operator message
    	private String missingOperatorMessage = "";				// Missing operator message
    	private String errorOperatorMessage = "";				// Multiple operators in sequence
    	private String parenMessageText = "unbalanced parentheses;";
    	private String unknownOperatorMessageText = "unknown operator character(s): ";
    	private String missingOperatorMessageText = "multiple variables or constants in sequence;";
    	private String errorOperatorMessageText = "ill-formed operator sequence;";
        private HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();		// Map from f2 index to operator index
        private HashMap<Integer, Integer> hmr = new HashMap<Integer, Integer>();	// Map from operator index to f2 index

    	public Formula (String s) {
    		this.f1 = s.trim();
    		if (f1.length() > 0)
    			parseFormula();
    		else
    			wellFormed = false;
    	}
		/**
		 * Returns the number of propositional variables.
		 * @return int pNum
		 */
        public int getpNum() {
        	return pNum;
        }

        /**
         * Returns the number of logical operator occurrences
         * @return int oNum
         */
        public int getoNum() {
        	return oNum;
        }

        /**
         * Returns the original input formula text.
         * @return String f1 - input formula
         */
        public String getFormula1() {
        	return f1;
        }

        /**
         * Returns the parsed formula text, formatted for evaluation
         * @return String f2 - parsed formula
         */
        public String getFormula2() {
        	return f2;
        }

        /**
         * Returns the i-th unique propositional variable encountered during parsing
         * @param Integer i
         * @return String propositional variable name
         */
        public String getpName(int i) {
        	return pSet.get(i);
        }

        /**
         * Returns the i-th logical operator occurrence in the formula
         * @param Integer i
         * @return String operator
         */
        public String getoName(int i) {
        	return oSet.get(i);
        }

        /**
         * Returns a mapping from the operator array index to the location of the operator in f2
         * @return HashMap<Integer, Integer> hmr
         */
        public HashMap<Integer, Integer> getHmr() {
        	return hmr;
        }

        /**
         * Returns a list of lists of the location occurrences for each propositional variable in f2
         * @return ArrayList<ArrayLists<Integer> propList
         */
        public ArrayList<ArrayList<Integer>> getPropList() {
        	return propList;
        }

        /**
         * Returns a list of the location occurrences in f2 of the constant f/F.
         * @return ArrayList<Integer> constantF
         */
        public ArrayList<Integer> getConstantF() {
        	return constantF;
        }

        /**
         * Returns a list of the location occurrences in f2 of the constant t/T.
         * @return ArrayList<Integer> constantT
         */
        public ArrayList<Integer> getConstantT() {
        	return constantT;
        }

        /**
         * Returns a boolean flag indicating whether the formula is well formed or not.
         * @return boolean wellFormed
         */
        public boolean getWellFormed() {
        	return wellFormed;
        }

        /**
         * Returns an error message describing why the formula is not well formed.
         * @return String errorMessage
         */
        public String getErrorMessage() {
        	return errorMessage;
        }

/**
 * Parses the formula in String f1 resulting in String f2, setting other information regarding the formula.
 *
 *     	ArrayList<String> pSet					-Propositional variables in formula f1
 *   	ArrayList<String> oSet					-Operators in formula f1 and f2
 *   	ArrayList<Integer> oxSet				-Indices of each operator occurrence in f2
 *   	ArrayList<ArrayList<Integer>> propList	-Indices in f2 for each propositional variable occurrence
 *   	ArrayList<Integer> constantT			-Indices in f2 of T occurrences
 *   	ArrayList<Integer> constantF			-Indices in f2 of F occurrences
 *   	int pNum								-Number of distinct propositional variables in f1 and f2
 *   	int oNum								-Number of distinct operators in f1 and f2
 *   	boolean wellFormed						-Flag indicating the formula is well formed
 *   	String errorMessage						-Error message for ill formed formula
 *   	HashMap<Integer, Integer> hm			-Map from f2 index to operator index
 *      HashMap<Integer, Integer> hmr			-Map from operator index to f2 index
 */
        private void parseFormula() {
			String alphanums =	"abcdeghijklmnopqrsuwxyz" +
					"ABCDEGHIJKLMNOPQRSUWXYZ" +								// f, F, t, T are used as constants
					"0123456789";
			String ops = "~!^v&*+=|:<>";									// logical operator symbols
																			// v is used to represent the NOR operator,
																			// corresponding to ^ for NAND
			String s = "";
			String badOperator = "";
			boolean propVariable = false;
			boolean operatorOccurence = false;
			int f2x = 0;
			int osx = 0;
			int parenCount = 0;
			char c;
        	for (int i = 0; i < f1.length(); i++) {
        		c = f1.charAt(i);
        		if (c == ' ') {
        			continue;													// ignore spaces
        		}
        		else if (c == 'f' | c == 'F') {									// process propositional constants f, F
       				if (propVariable) {
       					wellFormed = false;
       					missingOperatorMessage = missingOperatorMessageText;
       				}
       				propVariable = true;
        			operatorOccurence = false;
        			constantF.add(f2.length());
        			f2 = f2 + c;
        		}
        		else if (c == 't' | c == 'T') {									// process propositional constants t, T
       				if (propVariable) {
       					wellFormed = false;
       					missingOperatorMessage = missingOperatorMessageText;
       				}
   					propVariable = true;
        			operatorOccurence = false;
        			constantT.add(f2.length());
        			f2 = f2 + c;
        		}
        		else if (alphanums.indexOf(c) >= 0) {							// process propositional variables
       				if (propVariable) {
       					wellFormed = false;
       					missingOperatorMessage = missingOperatorMessageText;
       				}
       				propVariable = true;
        			operatorOccurence = false;
    				s =  "" + c;
    				f2x = f2.length();
    				while (i+1 < f1.length()) {
        				c = f1.charAt(i+1);
        				if (alphanums.indexOf(c) >= 0) {
        					s = s + c;
        					i++;
        				}
        				else {
        					break;
        				}
    				}
    				if (!pSet.contains(s)) {
    					f2 = f2 + pNum;
    					pSet.add(s);
    					propList.add(new ArrayList<Integer>());
    					propList.get(pNum).add(f2x);
    					pNum++;

    				}
    				else {
    					f2 = f2 + pSet.indexOf(s);
    					propList.get(pSet.indexOf(s)).add(f2x);
         				}
     			}
        		else if (c == ')' && i+1 < f1.length() && f1.charAt(i+1) == '(') {	// if successive ')(' insert conjunction
        			parenCount--;
        			f2 = f2 + c;
    				f2x = f2.length();
    				f2 = f2 + '&';
    				s = "" + '&';
    				osx = oSet.size();
					oSet.add(s);
    				oxSet.add(f2x);
					oNum++;
					hm.put(f2x, osx);
					hmr.put(osx,  f2x);
					propVariable = false;
        			if (operatorOccurence) {
        				errorOperatorMessage = errorOperatorMessageText;
        				wellFormed = false;
        			}
        			else {
        				operatorOccurence = true;
        			}
    			}
    			else if (c == '(') {											// process left paren
    				parenCount++;
    				f2 = f2 + c;
    			}
    			else if (c == ')') {											// process right paren
    				parenCount--;
    				f2 = f2 + c;
    			}
    			else if (ops.indexOf(c) >= 0) {									// process logical operators
        			if (c != '~' && c != '!' && (operatorOccurence ||!propVariable)) {
        				errorOperatorMessage = errorOperatorMessageText;
        				wellFormed = false;
        			}
       				operatorOccurence = true;
    				propVariable = false;
    				f2x = f2.length();
    				f2 = f2 + c;
    				s = "" + c;
    				osx = oSet.size();
					oSet.add(s);
    				oxSet.add(f2x);
					oNum++;
					hm.put(f2x,  osx);
					hmr.put(osx,  f2x);
    			}
        		else {
        			badOperator = badOperator + c;
        			f2 = f2 + c;
        		}
        	}

        	System.out.println("++++++++++++++++++++++++++++++!");
        	System.out.println("f1= " + f1);
        	System.out.println("f2= " + f2);

//        	System.out.println("pNum= " + pNum);
//        	System.out.println("pSet= " + pSet);

//        	System.out.println("oNum= " + oNum);
//        	System.out.println("oSet= " + oSet);
//        	System.out.println("oxSet= " + oxSet);

//        	System.out.println("hm= " + hm);
//        	System.out.println("hmr" + hmr);

			if (operatorOccurence) {
				errorOperatorMessage = errorOperatorMessageText;
				wellFormed = false;
			}

        	if (parenCount != 0) {
        		parenMessage = parenMessageText;
        		wellFormed = false;
        	}

        	if (badOperator != "") {
        		unknownOperatorMessage = unknownOperatorMessageText + badOperator +";";
        		wellFormed = false;
        	}

        	if (!wellFormed) {
        		errorMessage = parenMessage + " " + unknownOperatorMessage + " " + missingOperatorMessage + " " + errorOperatorMessage;
        	}
        }

        /**
         * Evaluates logic formula given a truth value assignment for the propositional variables.
         *
         * @param 	boolean[] v		Array of boolean truth values for the propositional variables.
         * @return	boolean[] vSet	Array of evaluation results for each operator occurrence in the formula
         * 							and the final value of the formula.
         */
        public boolean[] evalFormula(boolean[] v) {
        	String nums = "0123456789";
    		boolean[] vSet = new boolean[oSet.size()+1];	// Truth values for each operator occurrence and the final formula value
        	Stack<Boolean> vStack = new Stack<>();			// Stack of propositional variable truth values
        	Stack<Character> oStack = new Stack<>();		// Stack of operators, including parentheses
        	Stack<Integer> oxStack = new Stack<>();			// Stack of corresponding locations of the operator in f2

        	for (int i = 0; i < f2.length(); i++) {
        		char c1 = f2.charAt(i);
        		String s = "";

        		switch (c1) {

        		case '0':
        		case '1':
        		case '2':
        		case '3':
        		case '4':
        		case '5':
        		case '6':
        		case '7':
        		case '8':
        		case '9':
        			// Process numbered propositional variable, possibly more than one digit
        			s = "" + c1;
        			while (i+1 < f2.length()) {
        				c1 = f2.charAt(i+1);
        				if (nums.indexOf(c1) >= 0) {
        					s = s + c1;
        					i++;
        				}
        				else {
        					break;
        				}
    				}

        			// Replace instances of '(x)' where x is numeric with just 'x'
        			if (!oStack.isEmpty() && oStack.peek() == '(' && f2.charAt(i+1) == ')') {
        				oStack.pop();
        				oxStack.pop();
        				i++;
        			}
        			
        			// Stack variable value
        			vStack.push(v[Integer.parseInt(s)]);

        			// Process preceeding negations,if any
        			while (!oStack.isEmpty() && (oStack.peek() == '~' || oStack.peek() == '!')) {
        				boolean val = vStack.pop();
        				oStack.pop();
        				int ox = oxStack.pop();
	        			int oxv = hm.get(ox);
        				vStack.push(!val);
        				vSet[oxv] = !val;
        			}

        			break;

        		case '(':
        			oStack.push(c1);
        			oxStack.push(i);
        			break;

        		case ')':
        			int ic = 0;
        			while (!oStack.isEmpty() && oStack.peek() != '(') {
        				ic += 1;
	        			boolean v2 = vStack.pop();
	        			boolean v1 = vStack.pop();
	        			char op = oStack.pop();
	        			int ox = oxStack.pop();
	        			int oxv = hm.get(ox);
	        			boolean v3 = evalTerm(op, v1, v2);
	        			vStack.push(v3);
        				vSet[oxv] = v3;
    				}
        			// Remove left paren
        			oStack.pop();
        			oxStack.pop();
        			
        			// Process negations, if any
        			while (!oStack.isEmpty() && (oStack.peek() == '~' || oStack.peek() == '!')) {
        				boolean val = vStack.pop();
						oStack.pop();
						int ox = oxStack.pop();
						int oxv = hm.get(ox);
						vStack.push(!val);
	   					vSet[oxv] = !val;
					}

        			break;

        		case '&':
        		case '*':
        		case '+':
        		case '|':
        		case '>':
        		case '<':
        		case '^':
        		case 'v':
        		case '=':
        		case ':':
        		case '~':
        		case '!':
        			oStack.push(c1);
        			oxStack.push(i);
        			
        			break;

        		case 't':
        		case 'T':
        			vStack.push(true);      			
        			// Process negations, if any
        			while (!oStack.isEmpty() && (oStack.peek() == '~' || oStack.peek() == '!')) {
        				boolean val = vStack.pop();
						oStack.pop();
						int ox = oxStack.pop();
						int oxv = hm.get(ox);
						vStack.push(!val);
	   					vSet[oxv] = !val;
					}
        			
        			break;

        		case 'f':
        		case 'F':
        			vStack.push(false);
        			// Process negations, if any
        			while (!oStack.isEmpty() && (oStack.peek() == '~' || oStack.peek() == '!')) {
        				boolean val = vStack.pop();
						oStack.pop();
						int ox = oxStack.pop();
						int oxv = hm.get(ox);
						vStack.push(!val);
	   					vSet[oxv] = !val;
        			}
        			
        			break;
        		}
        	}

        	while (!oStack.empty()) {
    			boolean v1, v2, v3;
        		char op = oStack.pop();
    			int ox = oxStack.pop();
        		int oxv = hm.get(ox);
    			if (op == '~' || op == '!') {
        			v1 = vStack.pop();
        			v3 = !v1;
        			vSet[oxv] = !v1;
    			}
    			else {
    				v2 = vStack.pop();
    				v1 = vStack.pop();
    				v3 = evalTerm(op, v1, v2);
       				vSet[oxv] = v3;
    				if (!oStack.empty() && oStack.peek() == '(') {
    					oStack.pop();
    					oxStack.pop();
    				}
    			}
    			vStack.push(v3);
        	}
        	vSet[oSet.size()] = vStack.pop();
        	return vSet;
        }

        /**
         * Evaluates an expression of a logic formula.
         *
         * @param char op		Logical binary operator
         * @param boolean v1	Truth value of first operand
         * @param boolean v2	Truth value of second operand
         * @return boolean v3	Truth value of the expression
         */
        public boolean evalTerm(char op, boolean v1, boolean v2) {
		    boolean v3 = true;
		    switch (op) {
				case '&':
				case '*':
					v3 = v1 && v2;
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case '|':
				case '+':
					v3 = v1 || v2;
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case '>':
					v3 = !v1 || v2;
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case '<':
					v3 = v1 || !v2;
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case '^':
					v3 = !(v1 && v2);
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case 'v':
					v3 = !(v1 || v2);
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case '=':
					v3 = (v1 && v2) || (!v1 && !v2);
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
				case ':':
					v3 = (v1 && !v2) || (!v1 && v2);
//					System.out.println("op= " + op + " v1= " + v1 + " v2= " + v2 + " v3= " + v3);
					break;
		    }
		    return v3;
        }
    }
}
