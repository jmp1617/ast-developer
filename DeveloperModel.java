package augusta;

import augusta.properties.Access;
import augusta.properties.Direction;
import augusta.tree.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * model to take in developer commands
 * Created by Jacob Potter on 11/15/2016.
 */
public class DeveloperModel extends Observable {
    private List<String> commands; //list of commands from view
    private List<String> original_commands; //copy of the original commands
    private List<ProgNode> commands_prog; //prognode representation of the commands after compiling
    private String ERROR_MESSAGE; //if error occors, holds message
    private String filePath; //file path to save
    private int pointer; // should always be zero if everything works

    public DeveloperModel() {
        this.commands = new ArrayList<>();
        this.original_commands = new ArrayList<>();
        this.commands_prog = new ArrayList<>();
        this.pointer = 0;
        this.ERROR_MESSAGE = "";
    }

    /**
     * way for the view to send commands to the model
     * @param cmd command from view
     */
    public void sendCommand(String cmd) {
        if (cmd.equals("remPrev")) { // remove previous command
            original_commands.remove(original_commands.size() - 1);
        } else if (cmd.equals("remAll")) { // remove all
            this.original_commands = new ArrayList<>();
            this.commands = new ArrayList<>();
        } else if (cmd.equals("compile")) { //compile
            checkAndCompile();
        } else if (cmd.equals("save")) { //save
            try { //try the file path
                save();
            }
            catch (IOException e){ //if the file cant be accessed or the directory isnt found
                String[] toSend = new String[2]; // tell the view to display the information
                toSend[0] = "4";
                toSend[1] = e.toString();
                updateValue(toSend);
            }
        } else {   //add to list of commands
            original_commands.add(cmd);
        }
    }


    /**
     * way for the view to send the file path over
     * @param path file path
     */
    public void sendFilePath(String path){
        this.filePath = path;
    }

    /**
     * updates the model and notifies
     * @param newValue abstract value
     */
    private void updateValue(String[] newValue) {
        super.setChanged();
        super.notifyObservers(newValue);
    }

    /**
     * takes a list of string commands and compiles them into prognode lists;
     *
     * @param commands commands in string form
     * @return commands_prog list of prognodes
     */
    private List<ProgNode> compile(List<String> commands, int pointer) {
        List<ProgNode> commands_prog = new ArrayList<>(); // hold the nodes

        while (commands.size() > 0 ) { //while there are still some commands
            String c = commands.get(pointer); //get the first command
            char toTest = c.charAt(0); //get first letter and checks against the below
            if (toTest == 'F') {
                commands_prog.add(new Forward()); // add it and then
                commands.remove(pointer); //remove
            } else if (toTest == 'T') {
                Turn t;
                if (c.charAt(1) == 'R') { //add the node with its option
                    t = new Turn(Direction.RIGHT);
                } else
                    t = new Turn(Direction.LEFT);
                commands_prog.add(t);
                commands.remove(pointer);
            } else if (toTest == 'W') {
                List<ProgNode> wChildren; //get the whiles options
                Object H;
                Object A;
                char h = c.charAt(2);
                char a = c.charAt(1);
                if (a == 'O')
                    A = Access.OPEN;
                else
                    A = Access.BLOCKED;
                if (h == 'A')
                    H = Direction.AHEAD;
                else if (h == 'B')
                    H = Direction.BEHIND;
                else if (h == 'R')
                    H = Direction.RIGHT;
                else
                    H = Direction.LEFT;

                List<String> wCommands = new ArrayList<>(); //commands to recurse
                commands.remove(pointer); //remove w
                commands.remove(pointer); //remove b
                int numBegins = 1;
                int numEnds = 0;
                String cmd;
                while (pointer < commands.size() && numBegins != numEnds) { //while there are commands and there is still commands to go
                    cmd = commands.get(pointer);
                    if (cmd.equals("B")) {
                        numBegins++;
                    }
                    if (cmd.equals("X")) {
                        numEnds++;
                    }
                    if (numBegins != numEnds) { //if not done
                        wCommands.add(cmd); //add and remove
                        commands.remove(pointer);
                    }
                }


                wChildren = compile(wCommands, 0); //recurse to get the children commands
                While w = new While((augusta.properties.Access) A, (augusta.properties.Direction) H, wChildren);
                commands_prog.add(w); //add the while node

            } else if (toTest == 'C') { // if it is an if crumb
                List<ProgNode> if_nodes ; //to hold the then nodes
                List<ProgNode> else_nodes ; //to hold the else nodes
                List<String> if_commands = new ArrayList<>(); //commands for then
                List<String> else_commands = new ArrayList<>(); //commands for else

                commands.remove(pointer); //remove if
                commands.remove(pointer); //remove begin

                int numBegins = 1;
                int numEnds = 0;
                int numElse = 0;
                String cmd;
                boolean whileBegin = false;
                while (pointer < commands.size() && numBegins != numElse) { //same as the while only until else
                    cmd = commands.get(pointer);
                    if(cmd.charAt(0)=='W'|| cmd.charAt(0)=='R'){ // if it is a while or a repeat
                        whileBegin = true;
                    }
                    if (cmd.equals("B")&&!whileBegin) { //only if it comes after a if so that it runs correctly
                        numBegins++;                    // and gets the correct commands
                    }
                    if (cmd.equals("B")){
                        whileBegin = false;
                    }
                    if (cmd.equals("P")) {
                        numElse++;
                    }
                    if (numBegins != numElse) {
                        if_commands.add(cmd); //add
                        commands.remove(pointer); //remove
                    }
                }
                if_nodes = compile(if_commands,pointer); // compile the if commands

                numElse = 1;
                commands.remove(pointer); //remove else
                while (pointer < commands.size() && numElse != numEnds) {  // do the same for the else
                    cmd = commands.get(pointer);
                    if(cmd.charAt(0)=='W'|| cmd.charAt(0)=='R'){
                        whileBegin = true;
                    }
                    if (cmd.equals("P")) {
                        numElse++;
                    }
                    if (cmd.equals("X")&&!whileBegin) { // only if from if
                        numEnds++;
                    }
                    if (cmd.equals("X")){
                        whileBegin = false;
                    }
                    if (numElse != numEnds) {
                        else_commands.add(cmd);
                        commands.remove(pointer);
                    }
                }
                else_nodes = compile(else_commands, pointer); // compile the else commands

                commands_prog.add(new IfCrumb(if_nodes, else_nodes)); // add the if
            } else if (toTest == 'A') { // same as above for if access only with parameters
                Object H;
                Object A;
                char h = c.charAt(2);
                char a = c.charAt(1);
                if (a == 'O')
                    A = Access.OPEN;
                else
                    A = Access.BLOCKED;
                if (h == 'A')
                    H = Direction.AHEAD;
                else if (h == 'B')
                    H = Direction.BEHIND;
                else if (h == 'R')
                    H = Direction.RIGHT;
                else
                    H = Direction.LEFT;

                List<ProgNode> if_nodes;
                List<ProgNode> else_nodes;
                List<String> if_commands = new ArrayList<>();
                List<String> else_commands = new ArrayList<>();

                commands.remove(pointer); //remove if
                commands.remove(pointer); //remove begin

                int numBegins = 1;
                int numEnds = 0;
                int numElse = 0;
                String cmd;
                boolean whileBegin = false;
                while (pointer < commands.size() && numBegins != numElse) {
                    cmd = commands.get(pointer);
                    if(cmd.charAt(0)=='W'|| cmd.charAt(0)=='R'){
                        whileBegin = true;
                    }
                    if (cmd.equals("B")&&!whileBegin) { //only if it comes after a if
                        numBegins++;
                    }
                    if (cmd.equals("B")){
                        whileBegin = false;
                    }
                    if (cmd.equals("P")) {
                        numElse++;
                    }
                    if (numBegins != numElse) {
                        if_commands.add(cmd);
                        commands.remove(pointer);
                    }
                }
                if_nodes = compile(if_commands,pointer);

                numElse = 1;
                commands.remove(pointer); //remove else

                while (pointer < commands.size() && numElse != numEnds) {
                    cmd = commands.get(pointer);
                    if(cmd.charAt(0)=='W' || cmd.charAt(0)=='R'){
                        whileBegin = true;
                    }
                    if (cmd.equals("P")) {
                        numElse++;
                    }
                    if (cmd.equals("X")&&!whileBegin) { // only if from if
                        numEnds++;
                    }
                    if (cmd.equals("X")){
                        whileBegin = false;
                    }
                    if (numElse != numEnds) {
                        else_commands.add(cmd);
                        commands.remove(pointer);
                    }
                }
                else_nodes = compile(else_commands, pointer);

                commands_prog.add(new IfBlocks((augusta.properties.Access) A, (augusta.properties.Direction) H, if_nodes, else_nodes));

            } else if (toTest == 'R') { // if repear command
                int torep = ((int) c.charAt(1)) - 48; //convert to correct int
                List<String> repCommands = new ArrayList<>(); //commands for the inside of the rep
                List<ProgNode> rChildren;

                commands.remove(pointer); //remove r
                commands.remove(pointer); //remove b

                int numBegins = 1;
                int numEnds = 0;
                String cmd;
                while (pointer < commands.size() && numBegins != numEnds) { //same as the while
                    cmd = commands.get(pointer);
                    if (cmd.equals("B")) {
                        numBegins++;
                    }
                    if (cmd.equals("X")) {
                        numEnds++;
                    }
                    if (numBegins != numEnds) {
                        repCommands.add(cmd);
                        commands.remove(pointer);
                    }
                }

                rChildren = compile(repCommands,  0); //recurse to get the children commands
                commands_prog.add(new Repeat(torep, rChildren)); //add the repeat

            } else if (toTest == 'N') { //add the rest of the types of commands and then remove
                commands_prog.add(new DoNothing());
                commands.remove(pointer);

            } else if (toTest == 'H') {
                commands_prog.add(new Halt());
                commands.remove(pointer);

            } else if (toTest == 'D') {
                commands_prog.add(new Drop());
                commands.remove(pointer);

            } else if (toTest == 'E') {
                commands_prog.add(new Eat());
                commands.remove(pointer);
            } else { // if its a stray x p or b
                commands.remove(pointer);
            }
        }
        return commands_prog; //return the compiled commands
    }

    /**
     * compiles the commands and then saves
     * @throws IOException
     */
    private void save() throws IOException{
        boolean safe = checkAndCompile(); //compile
        if(safe) { //if it compiled successfully
            String[] toDev = new String[2]; //prepares to talk to the controller
            toDev[0] = "2";
            String filepath;
            updateValue(toDev); // talk to controller view
            filepath = this.filePath; //grab the set file path

            if(filepath!=null) { // if not cancelled
                if (filepath.length() > 0) { //if the fiile path is greateer than 0
                    ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filepath, false)); //create a stream
                    for (ProgNode p : commands_prog) {
                        stream.writeObject(p);
                    }
                    stream.close();
                    toDev[0] = "3";
                    toDev[1] = this.filePath;
                    updateValue(toDev); //tell the view
                }
            }
        }
    }


    /**
     * a method to check the string commands for errors before compiling
     * @param commands string commands
     * @return safe if it is safe
     */
    private boolean checkForErrors(List<String> commands) {
        boolean safe = true;

        //check for matching end ifs
        int numBegin = 0;
        int numEnd = 0;
        int numIfs = 0;
        int numElse = 0;
        for (String c : commands) {
            if (c.equals("B")) {
                numBegin++;
            } else if (c.equals("X")) {
                numEnd++;
            }
            else if (c.equals("C")||c.charAt(0)=='A'){ // if it is an if statement
                numIfs++;
            }
            else  if (c.equals("P")){
                numElse++;
            }
        }
        if (numBegin != numEnd) { // if there is a mismatch
            safe = false;
            ERROR_MESSAGE = " ERROR: Missing end or begin";
        }
        if (numIfs != numElse){
            safe = false;
            ERROR_MESSAGE = " ERROR: Missing structure command";
        }
        //check for empty statement
        if (commands.size() > 1) {
            for (int i = 1; i < commands.size(); i++) {
                String first = commands.get(i - 1);
                String second = commands.get(i);
                if (first.equals("B") && second.equals("X")) { // check all of this
                    safe = false;
                    ERROR_MESSAGE = " ERROR: Empty Statement";
                }
                if (first.equals("B") && second.equals("P")) {
                    safe = false;
                    ERROR_MESSAGE = " ERROR: Empty Statement";
                }
                if (first.equals("P") && second.equals("X")) {
                    safe = false;
                    ERROR_MESSAGE = " ERROR: Empty Statement";
                }
            }
        }
        //check for empty development environment
        if (commands.size() == 0) {
            safe = false;
            ERROR_MESSAGE = " ERROR: No commands entered";
        }
        //check for if without children / structure issues
        List<String> brackets = new ArrayList<>();
        for (String c : commands){
            if (c.equals("B")) {
                brackets.add("B");
            }
            else if (c.equals("X")) {
                brackets.add("X");
            }
            else  if (c.equals("P")){
                brackets.add("P");
            }
        }
        if (brackets.size() > 1) { // if there is more than one b p or x
            for (int i = 1; i < brackets.size(); i++) {
                String first = brackets.get(i - 1);
                String second = brackets.get(i);
                if(numElse!=numIfs) {
                    if (first.equals("X") && second.equals("P")) {// check to see if there is improper placement of else
                        safe = false;
                        ERROR_MESSAGE = " ERROR: improper else placement";
                    }
                }

            }
        }
        return safe;
    }

    /**
     * check and then compile
     * @return safeif compiled successfully
     */
    private boolean checkAndCompile() {
        String[] pass = new String[2];
        for (String c : original_commands) { //copy
            commands.add(c);
        }
        boolean safe = checkForErrors(original_commands); //check for errors
        if (safe) {
            pass[0] = "0";
            updateValue(pass); //0 is good
            this.commands_prog = compile(commands, pointer); //compile if safe
        } else {
            pass[0] = "1";
            pass[1] = ERROR_MESSAGE;

            updateValue(pass); //one is bad tell the view to display error
        }
        return safe;
    }
}
