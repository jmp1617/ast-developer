package augusta;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.HBox;
import java.util.*;

/**
 * button object to handle events
 * Created by jacob on 11/20/16.
 */
public class ButtonAction extends Button {

    public boolean cancelled = false;

    public ButtonAction(String text, String id, boolean hasOptions, boolean isTabber, boolean isUnTabber) {
        super(text);
        this.setId(id);
        this.setOnAction(e->{ //create the button and shows it then sends command to model

            String options = "";
            String toSend = "";

            Map<String,String> commands = new HashMap<>(); //map commands to smaller size command to be passed to model
            commands.put("forward_but","F");
            commands.put("turn_but","T");
            commands.put("if_crumb_but","C");
            commands.put("if_access_but","A");
            commands.put("while_access_but","W");
            commands.put("repeat_but","R");
            commands.put("eat_crumb_but","E");
            commands.put("drop_but","D");
            commands.put("nop_but","N");
            commands.put("begin_but","B");
            commands.put("end_but","X");
            commands.put("halt_but","H");
            commands.put("else_but","P");

            toSend+=commands.get(id); //adds the shorthand command to the string to add to list of commands

            if (hasOptions){ //if it had options go get them
                options += getOptions(id);
                toSend+=options;//adds the option to the to send
            }

            if(!cancelled) { // if the command hasnt been canceled
                if (isUnTabber) { //dec one tab for end
                    AugustaDeveloper.decTab();
                }

                HBox boxToAdd = addTab(); // add the correct amount of tabs

                if (isTabber) { // add a tab
                    AugustaDeveloper.incTab();
                }
                if(text.equals("END")) { // avoid else
                    if (isUnTabber) { // one more to return to normal tabbing
                        AugustaDeveloper.decTab();
                    }
                }

                String option = "";
                if (hasOptions) {  //This section formats the tiles names correctly
                    if (text.equals("REPEAT")) {
                        option += ": " + options.charAt(0);
                    } else {
                        char first = options.charAt(0);
                        switch (first) {
                            case ('R'):
                                option += ": RIGHT";
                                break;
                            case ('L'):
                                option += ": LEFT";
                                break;
                            case ('O'):
                                option += ": OPEN";
                                break;
                            case ('B'):
                                option += ": BLOCKED";
                                break;
                        }
                        if (options.length() > 1) {
                            char second = options.charAt(1);
                            switch (second) {
                                case ('A'):
                                    option += " AHEAD";
                                    break;
                                case ('B'):
                                    option += " BEHIND";
                                    break;
                                case ('R'):
                                    option += " RIGHT";
                                    break;
                                case ('L'):
                                    option += " LEFT";
                                    break;
                            }
                        }
                    }

                }

                //create the new ide tile for the ide space
                Button toAdd = new IDEButton(text + option, isTabber, isUnTabber);
                toAdd.setId(id + "_ide");
                toAdd.setPrefSize(266, 20);
                boxToAdd.getChildren().add(toAdd);
                AugustaDeveloper.addToIde(boxToAdd);


                AugustaDeveloper.sendModelCommand(toSend); //sends the command to the model
                autoBegin(isTabber, text, isUnTabber); //autobegins
            }
            cancelled = false;
        });
    }

    /**
     * get the options for the parameter
     * @param command type of command
     * @return the options
     */
    public String getOptions(String command){
        String options = "";

        //repeat button command getter
        if(command.equals("repeat_but")) {
            List<String> num  = new ArrayList<>();
            for(int i = 1; i<10; i++){
                num.add(""+i);
            }
            String s = null;

            ChoiceDialog<String> dialog = new ChoiceDialog<>("1", num);
            dialog.setTitle("Augusta Developer");
            dialog.setHeaderText("Repeat Tile");
            dialog.setContentText("Choose number of times to repeat:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                s = result.get();
            }
            else{ // if it is cancelled
                options+="!"; //dummy var
                cancelled = true;
            }

            options+=s;
        }

        //turn but option getter
        if(command.equals("turn_but")){
            String d ;
            String n = null;
            List<String> choices = new ArrayList<>();
            choices.add("Right");
            choices.add("Left");

            ChoiceDialog<String> dialog = new ChoiceDialog<>("Right", choices);
            dialog.setTitle("Augusta Developer");
            dialog.setHeaderText("Turn Tile");
            dialog.setContentText("Choose direction to turn:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                n = result.get();
            }
            else{ //same as above if cancelled
                n = "!";
                cancelled = true;
            }

            if(n.equals("Right")){
                d="R";
            }
            else {
                d="L";
            }
            options+=d;
        }

        //get options for if and while access
        if(command.equals("if_access_but")||command.equals("while_access_but")){
            String d;
            String[] options_i = {"Open","Blocked"};
            String n = null;
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Open", options_i);
            dialog.setTitle("Augusta Developer");
            dialog.setHeaderText("While Tile");
            dialog.setContentText("Choose Access:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                n = result.get();
            }
            else { // if canceled
                n = "!";
                cancelled = true;
            }

            if(n.equals("Open")){
                d="O";
            }
            else {
                d="B";
            }
            if(!cancelled) { //if canceled dont continue
                String[] posibilities = {"Ahead", "Behind", "Right", "Left"};
                String s = null;
                dialog = new ChoiceDialog<>("Ahead", posibilities);
                dialog.setTitle("Augusta Developer");
                dialog.setHeaderText("While Tile");
                dialog.setContentText("Choose Direction:");

                result = dialog.showAndWait();
                if (result.isPresent()) {
                    s = result.get();
                }
                else {
                    s = "!";
                    cancelled = true;
                }

                if (s.equals("Ahead"))
                    d += "A";
                else if (s.equals("Behind"))
                    d += "B";
                else if (s.equals("Right"))
                    d += "R";
                else if (s.equals("Left"))
                    d += "L";
                options += d;
            }
        }
        return options; //return the options
    }

    /**
     * Automatically place a begin for the user
     * @param isTabber if tabber
     * @param text name
     * @param isUnTabber if untabber
     */
    public void autoBegin(boolean isTabber, String text, boolean isUnTabber){
        if(isTabber&&((!text.equals("BEGIN")&&!text.equals("ELSE")))){ //if it is a function that needs a begin, one will autoplace
            if(isUnTabber){ //dec one tab for end
                AugustaDeveloper.decTab();
            }

            HBox boxToAdd = addTab(); //add the tabs

            if(isTabber){ // add a tab
                AugustaDeveloper.incTab();
            }
            if(isUnTabber){ // one more to return to normal tabbing
                AugustaDeveloper.decTab();
            }

            Button toAdd = new IDEButton("BEGIN",isTabber,isUnTabber);
            toAdd.setId("begin_but_ide");
            toAdd.setPrefSize(266,20);
            boxToAdd.getChildren().add(toAdd);
            AugustaDeveloper.sendModelCommand("B");
            AugustaDeveloper.addToIde(boxToAdd);
        }
    }

    /**
     * add the correct amount of tabs
     * @return boxToAdd hbox of spaces
     */
    public HBox addTab(){
        Button spacer;
        HBox boxToAdd = new HBox();
        for (int tab = 0; tab < AugustaDeveloper.getTab(); tab++) { //tab correct ammount
            spacer = new Button(""); //create a button that is just a spacer
            spacer.setId("spacer");
            spacer.setPrefSize(50, 20);
            boxToAdd.getChildren().add(spacer);
        }
        return boxToAdd;
    }

}
