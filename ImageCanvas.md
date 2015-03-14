
```
package example.gmaps;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/*

*/

// This class displays a selected image centered in the screen
class ImageCanvas
  extends Canvas
{
  //private final ImageViewerMIDlet midlet;
  private static final int CHUNK_SIZE = 10240;
  private Image imgBR = null;
  private Image imgBL = null;
  private Image imgUR = null;
  private Image imgUL = null;
  private Image imgNone = null;
  private int imageW = 256;
  private int imageH = 256;
  private int imageX = 4841;
  private int imageY = 5860;
  private int posX = 0; // relative to 2x2 imageWxH
  private int posY = 0;
  private int screenW = 240;
  private int screenH = 320;
  private int posZ = 3;
  private int delta = 30;
  

  ImageCanvas() {
     imgNone = loadImage(-1,-1,-1);
     imageW = imgNone.getWidth();
     imageH = imgNone.getHeight();
     loadTiles();
        screenW = getWidth();
        screenH = getHeight();
  }
  protected void loadTiles(){
	int z=posZ; int x=imageX; int y=imageY;
 
    	imgUL = loadImage(x,y,z);
	imgUR = loadImage(x,y+1,z);
	imgBL = loadImage(x+1,y,z);
	imgBR = loadImage(x+1,y+1,z);
  }

  private Image loadImage(int x,int y, int z)  {
    String d = "c:/"; d="file://localhost/";
    d="/gmaps/";
    String imgName = d+"x"+x+"y"+y+"z"+z+".png";
    if (z==-1) imgName =d+"none.png";
         
    try
    {
      if(imgName.startsWith("/") ) { // resource file
        return Image.createImage(imgName);
      }

      FileConnection fileConn =
        (FileConnection)Connector.open(imgName, Connector.READ);
      // load the image data in memory
      // Read data in CHUNK_SIZE chunks
      InputStream fis = fileConn.openInputStream();
      long overallSize = fileConn.fileSize();

      int length = 0;
      byte[] imageData = new byte[0];
      while (length < overallSize)
      {
        byte[] data = new byte[CHUNK_SIZE];
        int readAmount = fis.read(data, 0, CHUNK_SIZE);
        byte[] newImageData = new byte[imageData.length + CHUNK_SIZE];
        System.arraycopy(imageData, 0, newImageData, 0, length);
        System.arraycopy(data, 0, newImageData, length, readAmount);
        imageData = newImageData;
        length += readAmount;
      }

      fis.close();
      fileConn.close();
      return Image.createImage(imageData, 0, length);
    }

    catch (IOException e)
    {
      //midlet.showError(e);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      //midlet.showError(e);
    }
    return imgNone;
  }
  public int mod(int a, int b){ return a - b*(int)(a/b); }

  protected void paint(Graphics g)
  {
    // Set background color to black
    g.setColor(0x00000000);
    g.fillRect(0, 0, screenW, screenH);

    //find the four images to draw
    int ulW = imageW - posX;
    int ulH = imageH - posY;    

    if (imgUL != null) {
      if( (ulW >= screenW) && (ulH >= screenH)) {
 	// upper left
        g.drawRegion(imgUL,
          posX,posY,screenW,screenH,
          Sprite.TRANS_NONE ,0,0,0);
      } else if( ulW >= screenW ) {
 	// upper left
        g.drawRegion( imgUL,
          posX,posY,screenW,ulH,
          Sprite.TRANS_NONE ,0,0,0);
	 // bottom left
        g.drawRegion(imgBL,
          posX,0,screenW,screenH-ulH,
          Sprite.TRANS_NONE ,0,ulH,0);
      } else if (ulH >= screenH) {
 	// upper left
        g.drawRegion( imgUL,
          posX,posY,ulW,screenH,
          Sprite.TRANS_NONE ,0,0,0);
	// upper right
        g.drawRegion( imgUR,
          0,posY,screenW-ulW,screenH,
        Sprite.TRANS_NONE ,ulW,0,0);
      } else {
 	// upper left
        g.drawRegion( imgUL,
          posX,posY,ulW,ulH,
          Sprite.TRANS_NONE ,0,0,0);
	 // bottom left
        g.drawRegion( imgBL,
          posX,0,ulW,screenH-ulH,
          Sprite.TRANS_NONE ,0,ulH,0);
	// upper right
        g.drawRegion( imgUR,
          0,posY,screenW-ulW,ulH,
        Sprite.TRANS_NONE ,ulW,0,0);
	// bottom right
        g.drawRegion( imgBR,
          0,0,screenW-ulW,screenH-ulH,
          Sprite.TRANS_NONE ,ulW,ulH,0);
      }
      
      g.drawString(imageX+" "+imageY+" "+posX+" "+posY,
        screenW / 2, screenH / 2,
        Graphics.HCENTER | Graphics.BASELINE);
    }    else    {
      // If no image is available display a message
      g.setColor(0x00FFFFFF);
      g.drawString("No image",
        screenW / 2,
        screenH / 2,
        Graphics.HCENTER | Graphics.BASELINE);
    }
  }

  public void in() {
    if (posZ <= 2) return;
    posZ --;
    imageX = imageX*2;
    imageY = imageY*2; 
    // assert (posX, posY) inside UL image
    int halfw = imageW / 2;
    int halfh = imageH / 2;
    if ((posX > halfw) && (posY > halfh) ) {
      imageX++;
      imageY++; 
    } else if (posX > halfw) {
      imageX++;
    } else if (posY > halfh) {
      imageY++; 
    } 
    //TODO ? move posX, posY 
    loadTiles();
  }
  public void out() {
    if (posZ >= 6) return;
    posZ ++;
    imageX = imageX/2;
    imageY = imageY/2; 
    // assert (posX, posY) inside UL image
    int halfw = imageW / 2;
    int halfh = imageH / 2;
    //TODO ? move posX, posY 
    loadTiles();

  }

  protected void down() { 
     posY += delta;
     if((posY) <= imageH) return;
     imageY++;
     imgUR = imgBR;
     imgUL = imgBL;
     imgBL = loadImage(imageX,imageY+1,posZ);
     imgBR = loadImage(imageX+1,imageY+1,posZ);
     posY -= imageH;    
  }
  protected void up() {  
     posY -= delta;
     if(posY >= 0) return;
     imageY--;
     imgBR = imgUR;
     imgBL = imgUL;
     imgUL = loadImage(imageX,imageY,posZ);
     imgUR = loadImage(imageX+1,imageY,posZ);
     posY += imageH;
  }
  protected void right() {  
     posX += delta;
     if((posX) <= imageW ) return;
     imageX++;
     imgUL = imgUR;
     imgBL = imgBR; 
     imgUR = loadImage(imageX+1,imageY,posZ);
     imgBR = loadImage(imageX+1,imageY+1,posZ);
     posX -= imageW;
  }
  protected void left() { 
     posX -= delta;
     if(posX >= 0) return;
     imageX--;
     imgUR = imgUL; 
     imgBR = imgBL; 
     imgUL = loadImage(imageX,imageY,posZ);
     imgBL = loadImage(imageX,imageY+1,posZ);
     posX += imageW;
  }

  protected void pointerPressed(int x, int y) { in(); }
  protected void pointerDragged(int x, int y) { out(); }
  protected void keyPressed(int keyCode) {
    // Exit with any key
    //midlet.displayFileBrowser();

        int action = getGameAction(keyCode);

    int w = getWidth();
    int h = getHeight();
	int delta = w/2;
        switch (action) {
        case RIGHT: right();
            break;
        case LEFT: left();
            break;
        case UP:  up();
            break;
        case DOWN: down();
            break;
        case FIRE: in();    break;
        case GAME_A: out(); break;
        case GAME_B: left(); break;
        case GAME_C: right(); break;
        case GAME_D: up(); break;
        }


    int right = imageW - w; right = 2*imageW;
    int bottom = imageH - h; bottom = 2*imageH;
	if (posX < 0)posX = 0;
	if (posX > right) posX = right;
	if (posY <0 )posY = 0;
        if (posY > bottom) posY = bottom;
	repaint();
  }

    /**
     * Handle key repeat events as regular key events.
     * @param keyCode of the key repeated
     */
    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

  
}

```