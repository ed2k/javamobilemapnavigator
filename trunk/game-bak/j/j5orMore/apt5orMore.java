import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;

public class apt5orMore extends Applet {
   Button a;
   int level=0;
   j5orMore  assoc;


  boolean isStandalone = false;

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }


  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
      setLayout(new BorderLayout());
      Panel p = new Panel();
      p.add(a = new Button("New Window"));
      add(p);
      assoc = new j5orMore();
      assoc.show();
      assoc.setSize(400,500);
  }
   // This action only for buttons in this frame
   public boolean action(Event evt, Object obj){
      if (evt.target.equals(a)){
	  assoc.dispose();
      }
      return super.action(evt, obj); 
   }
  //Get Applet information
  public String getAppletInfo() {
    return "Applet Information";
  }

  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }


  
 


}

