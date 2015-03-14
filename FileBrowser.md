
```
/*
 */
package example.gmaps;

import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


/**
 */
public class FileBrowser extends MIDlet implements CommandListener {
    
    private ImageCanvas map;
    
    private Command in = new Command("in", Command.EXIT, 1);
    private Command out = new Command("out", Command.OK, 1);
    private Command creatOK = new Command("OK", Command.OK, 1);
    private Command prop = new Command("Properties", Command.ITEM, 2);
    private Command back = new Command("Back", Command.BACK, 2);
    private Command exit = new Command("Exit", Command.EXIT, 3);
    private TextField   nameInput;  // Input field for new file name
    private ChoiceGroup typeInput;  // Input fiels for file type (regular/dir)
    

    public FileBrowser() {
    }
    
    public void startApp() {
        try {
            showImage("");
        } catch (SecurityException e) {
            Alert alert = new Alert("Error",
                "You are not authorized to access the restricted API",
                null, AlertType.ERROR);
            alert.setTimeout(Alert.FOREVER);
            Form form = new Form("Cannot access FileConnection");
            form.append(new StringItem(null,
                "You cannot run this MIDlet with the current permissions. "
                + "Sign the MIDlet suite, or run it in a different security domain"));
            form.addCommand(exit);
            form.setCommandListener(this);
            Display.getDisplay(this).setCurrent(alert, form);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean cond) {
        notifyDestroyed();
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == in) {
           map.in();
        } else if (c == out) {
           map.out();
        } else if (c == back) {
            
        } else if (c == exit) {
            destroyApp(false);
        }
    }
    
    
    
    void showImage(String fileName) {
        try {
            ImageCanvas map = new ImageCanvas();
	    map.addCommand(exit);
            map.setCommandListener(this);
	    
            Display.getDisplay(this).setCurrent(map);
        } catch (Exception e) {
            Alert alert = new Alert("Error!",
            "Can not access file "
            + " in directory "
            + "\nException: " + e.getMessage(),
            null,
            AlertType.ERROR);
            alert.setTimeout(Alert.FOREVER);
            Display.getDisplay(this).setCurrent(alert);
        }
        
    }
    
	    

    
}

```