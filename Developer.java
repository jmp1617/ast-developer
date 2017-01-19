import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

/**
 * A developer for augusta robot
 * Created by jacob on 11/2/16.
 */
public class AugustaDeveloper extends Application implements Observer{

    private static DeveloperModel model; //the model for the developer
    private Label robot; //robot label
    private static VBox idespace; //area for tiles that is scrollable
    private static int pointer; //pointer to keep track of current command
    private static int current_tab; //keeps track of the current tab ammount
    private final int NUMCOMNMANDS; // number of possible tiles types
    private final String ascii_robot = //robot
            "/----------------------------\\"+
            "\n*           ( o )            *\n" +
            "*           __|__            *\n" +
            "*          ( o o )           *\n" +
            "*          ( === )           *\n" +
            "*         ___|||___    \\)    *\n" +
            "*      []/__Beep!__\\[] /     *\n" +
            "*      / \\_________/ \\/      *\n" +
            "*     /     /___\\            *\n" +
            "*    (\\    /_____\\           *\n"+
            "|----------------------------|\n"+
            "|      Augusta The Robot     |\n"+
            "\\----------------------------/\n";

    public AugustaDeveloper(){
        super();
        this.NUMCOMNMANDS = 13; //this many commands
        current_tab = 0; //zero out
        pointer = 0;  //statics
    }

    @Override
    public void init() throws Exception{
        model = new DeveloperModel(); //create the model object
        model.addObserver(this); //add this view as an observer
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane mainpane = new BorderPane(); //create the main pane
        VBox left = new VBox(); //create left vbox to hold tiles and buttons
        VBox right = new VBox();    //creates right vbox to hold instructions and info
        ScrollPane center = new ScrollPane(); //pane to make the vbox scrollable
        idespace = new VBox(); //area for tiles to go
        //Labels
        Label tile_header = new Label(
                "/-*-*-*-*/|\\*-*-*-*-\\\n" +
                "*Robot Command Tiles*\n" +
                "\\-*-*-*-*\\|/*-*-*-*-/");
        Label color_code = new Label(
                "/-*-*-*-*-*-*-*/\\*-*-*-*-*-*-*-\\\n" +
                        "*     Cyan: Robot Commands     *\n" +
                        "*     Red: World Commands      " +
                "*\n*   Purple: Control Commands   *\n" +
                        "\\-*-*-*-*-*-*-*\\/*-*-*-*-*-*-*-/");
        robot = new Label(ascii_robot);
        Label inst_head = new Label("|-*-*-Instruction-*-*-|");
        Label inst_tile = new Label(
                "1)Click on the tile    \n" +
                "  that you want to add \n" +
                "  from the tile window \n" +
                "2)If it is a tile that \n" +
                "  has parameters, a    \n" +
                "  pop up will show for \n" +
                "  parameter entry      \n" +
                "3)The clear all button \n" +
                "  will clear the whole \n" +
                "  development area     \n" +
                "4)The clear previous   \n" +
                "  button will clear 1  \n" +
                "  tile at a time       \n" +
                "5)The compile button   \n" +
                "  will check to see if \n" +
                "  the flow is correct  \n" +
                "6)The save button will \n" +
                "  compile the flow and \n" +
                "  save it to a file    \n" +
                "\n  By: Jacob Potter   ");

        //left setup
        left.setSpacing(10);
        left.getChildren().add(tile_header);
        left.setPadding(new Insets(20,20,20,20));
        left.getChildren().add(this.makeTiles());   //creates the tiles
        left.getChildren().add(color_code); //add label for color code
        left.getChildren().add(this.makeOperations());
        //right setup
        right.setSpacing(22);
        right.getChildren().add(robot);
        right.getChildren().add(inst_head);
        right.getChildren().add(inst_tile);
        right.setPadding(new Insets(20,20,20,20));
        //id setting
        tile_header.setId("tile_header");
        color_code.setId("color_code");
        left.setId("left-side");
        right.setId("right-side");
        robot.setId("robot");
        inst_head.setId("dev");
        inst_tile.setId("dev");

        //setup scroll and idespace
        center.setContent(idespace);
        center.setId("ide");
        idespace.setPadding(new Insets(20,20,20,20));
        idespace.setSpacing(1);
        idespace.setId("ide");

        mainpane.setLeft(left);     //set the left
        mainpane.setRight(right);   //set the right
        mainpane.setCenter(center); //set the center

        primaryStage.setTitle("Agusta Development Environment");    //set the title
        Scene mainscene = new Scene(mainpane, 1200, 700);   //create the main scene
        primaryStage.setResizable(false);   //sets not resizable
        primaryStage.setScene(mainscene);   //set the scene
        mainscene.getStylesheets().add(AugustaDeveloper.class.getResource("AugustaDeveloperStyle.css").toExternalForm()); //import the css
        primaryStage.show();    //show app
    }

    /**
     * Static method for use in ButtonAction objects to set a tile
     * @param b the tabs and tiles
     */
    public static void addToIde(HBox b){
        idespace.getChildren().add(b); //add to ide
        pointer++; //increase location
    }

    /**
     * inc tab by 1
     */
    public static void incTab(){ //static method to inc tab
        current_tab++;
    }

    /**
     * dec tab by 1
     */
    public static void decTab(){
        current_tab--;
    }

    /**
     * gets the tab
     * @return current tab ammount
     */
    public static int getTab(){ //static method to return tab
        return current_tab;
    }

    /**
     * takes the command and sends it to the model
     * @param cmd to send to model
     */
    public static void sendModelCommand(String cmd){
        model.sendCommand(cmd); //pass to model
    }

    @Override
    public void stop() throws Exception{
        super.stop();
    }

    /**
     * make 4 operation buttons
     * @return the grid of buttons
     */
    private Pane makeOperations(){
        VBox grid = new VBox(); // a Vbox to hold all of the buttons
        HBox r1 = new HBox(); //top row
        HBox r2 = new HBox(); //bottom row
        grid.setPadding(new Insets(20,20,20,20));
        grid.setSpacing(10);
        r1.setSpacing(10);
        r2.setSpacing(10);
        Button[] bts = new Button[4]; //array to hold all of the buttons
        //id
        grid.setId("oper_grid");
        //button creation
        Button clear_all = new Button("Clear All"); //create the clear all button
        r1.getChildren().add(clear_all); //add it
        clear_all.setOnAction(e->{ //ic clicked
            for (int cell = pointer-1; cell>=0; cell--){ //remove all the tiles
                idespace.getChildren().remove(cell);
            }
            pointer = 0; //zero out everything
            current_tab = 0;
            sendModelCommand("remAll"); //send to model
        });
        bts[0] = clear_all; //add to the array

        Button clear_previous = new Button("Clear Previous"); //clear prev button
        r1.getChildren().add(clear_previous);
        clear_previous.setOnAction(e->{
            if(pointer>0) {
                HBox toRemove = (HBox)idespace.getChildren().remove(--pointer); //remove most recent
                IDEButton b = (IDEButton)toRemove.getChildren().get(toRemove.getChildren().size()-1); //and remove from ide
                if(!b.getName().equals("ELSE: ")) { // if not else -special case because its both inc and dec tab
                    if (b.isTabber()) { //fix tab
                        current_tab--;
                    }
                    if (b.isUnTabber()) {
                        current_tab += 2;
                    }
                }
                sendModelCommand("remPrev"); //send
            }

        });
        bts[1] = clear_previous; //add to the array

        Button save = new Button("Save to File"); //save button
        r2.getChildren().add(save);
        bts[2] = save;
        save.setOnAction(e->{  //if clicked tell the model
            sendModelCommand("save");
        });

        Button compile = new Button("Compile"); //compile button to check before check  and save
        r2.getChildren().add(compile);
        bts[3] = compile;
        compile.setOnAction(e->{ // tell model
            sendModelCommand("compile");
        });

        for (Button b : bts){ // give the buttons id and size
            b.setId("operation_but");
            b.setPrefSize(107,25);
        }
        //add the two rows
        grid.getChildren().add(r1);
        grid.getChildren().add(r2);
        return grid; //return the buttons
    }

    /**
     * creates the button tiles
     * @return v the left pane of buttons
     */
    private Pane makeTiles(){
        VBox v = new VBox(); //temp vbox
        v.setSpacing(2);
        Button[] bts = new Button[NUMCOMNMANDS]; //array to hold all of the buttons
        String[] ids = {"forward_but","turn_but","drop_but","eat_crumb_but","while_access_but","if_crumb_but","if_access_but","repeat_but","nop_but","halt_but","begin_but","else_but","end_but"}; //all of the ids
        String[] names = {"FORWARD","TURN","DROP","EAT_CRUMB","WHILE_ACCESS","IF_CRUMB","IF_BLOCKS","REPEAT","NOP","HALT","BEGIN","ELSE","END"};//all of the names
        String[] has_options = {"TURN","REPEAT","IF_BLOCKS","WHILE_ACCESS"}; //which have options
        String[] should_tab = {"WHILE_ACCESS","REPEAT","BEGIN","ELSE","IF_CRUMB","IF_BLOCKS","ELSE"}; // which should tab
        String[] should_un_tab = {"END","ELSE"}; //should untab current tab ammount
        int counter = 0;
        Button b; //new button

        //Begin button creation
        for (String id : ids) {
            boolean hasOp = false;
            boolean isTabber = false;
            boolean isUnTabber = false;
            for(String tab: should_tab){ //if tabber
                if(tab.equals(names[counter]))
                    isTabber = true;
            }
            for(String untab: should_un_tab){ //if untabber
                if (untab.equals(names[counter]))
                    isUnTabber = true;
            }
            for (String name : has_options) { //if has options
                if (name.equals(names[counter]))
                    hasOp = true;
            }
            b = new ButtonAction(names[counter], id, hasOp, isTabber, isUnTabber); // create the button
            bts[counter] = b; //add to the array
            counter++;
        }

        //add buttons and set properties
        for (Button btn : bts){
            v.getChildren().add(btn);
            btn.setPrefSize(266,20);
        }
        return v; //return the vbox
    }

    /**
     * launches with args
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Launching Agusta Developer");
        Application.launch(args);
    }


    /**
     * sets the robot back to green if compile is successful
     */
    private void setGreen(){
        robot.setId("robot_good");
    }

    /**
     * sets the background to red if it fails
     */
    private void setRed(){
        robot.setId("robot_bad");
    }

    @Override
    public void update(Observable o, Object arg) {
        assert o == model : "Incorrect observable " +o; //o as a calc model /static
        assert arg instanceof String[] : "Incorrect update argument " + arg; //arg as string array
        if (((String[])arg)[0].equals("0")){ // if it is a good structure
            setGreen();

            Alert alert = new Alert(AlertType.INFORMATION); //create good alert
            alert.setTitle("Augusta Developer");
            alert.setHeaderText(null);
            alert.setContentText("Command Structure Good!");

            alert.showAndWait();
        }
        else if(((String[])arg)[0].equals("1")){ //if it fails show why
            setRed();

            Alert alert = new Alert(AlertType.ERROR); //create bad alert
            alert.setTitle("Augusta Developer");
            alert.setHeaderText("Command Structure Bad, Try Again");
            alert.setContentText(((String[])arg)[1]);

            alert.showAndWait();
        }
        else if (((String[])arg)[0].equals("2")){ //dialog to ask for a path to save and give it to the model
            TextInputDialog dialog = new TextInputDialog("Path");
            dialog.setTitle("Augusta Developer");
            dialog.setHeaderText("Enter file path where you want to save the tree");
            dialog.setContentText("File Path:");

            Optional<String> result = dialog.showAndWait();
            // give to model
            result.ifPresent(s -> model.sendFilePath(s));
        }
        else if (((String[])arg)[0].equals("3")){//show the file that is saved
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Augusta Developer");
            alert.setHeaderText(null);
            alert.setContentText("File saved to: "+ ((String[])arg)[1]);

            alert.showAndWait();
        }
        else if (((String[])arg)[0].equals("4")){ // print the file error
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Augusta Developer");
            alert.setHeaderText(null);
            alert.setContentText("Error writing to file: "+ ((String[])arg)[1]);

            alert.showAndWait();
        }
    }
}
