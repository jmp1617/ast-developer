package augusta;

import javafx.scene.control.Button;

/**
 * button type made for the ide environment
 * Created by Jacob Potter on 11/21/2016.
 */
public class IDEButton extends Button{
    private boolean isTabber;
    private boolean isUnTabber;
    private String text;

    public IDEButton (String text, boolean isTabber, boolean isUnTabber){
        super(text);
        this.text = text;
        this.isTabber = isTabber;
        this.isUnTabber = isUnTabber;
    }

    /**
     * get if it is a tabber
     * @return isTabber
     */
    public boolean isTabber(){
        return isTabber;
    }

    /**
     * get if it is an untabber
     * @return inUnTabber
     */
    public boolean isUnTabber(){
        return isUnTabber;
    }

    /**
     * get the name
     * @return text
     */
    public String getName() {
        return text;
    }
}
