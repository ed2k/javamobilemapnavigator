/*************************************************************************/
//package googlehackpurejava;
/*************************************************************************/
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import javax.imageio.ImageIO;
/*************************************************************************/
public class ImageUtil {
  //-----------------------------------------------------------------------
  public ImageUtil() {
  }
  //-----------------------------------------------------------------------
  public void splitImage(File fImage, File fDestDir,int tileSize) throws Exception{      
    BufferedImage biImage = ImageIO.read(fImage);
    int           width   = biImage.getWidth();
    int           height  = biImage.getHeight();
    int           x1      = 0;
    int           x2      = 0;
    int           y1      = 0;
    int           y2      = 0;
    String        fileName= fImage.getName().substring(0,fImage.getName().length()-4);
     
    while(x1<width || y1<height){
      if(x1>=width){
        x1  = 0;
        y1 += tileSize;
        if(y1>=height)
          break;
      }
      x2=x1+tileSize;
      y2=y1+tileSize;
      
      int imSizeW = x2>width  ?  width-x1:x2-x1;
      int imSizeH = y2>height ?  height-y1:y2-y1;

      BufferedImage biTile = new BufferedImage(imSizeW,imSizeH,BufferedImage.TYPE_BYTE_INDEXED,(IndexColorModel)biImage.getColorModel());
      Graphics2D    gTile  = biTile.createGraphics();
      gTile.drawImage(biImage,0,0,tileSize,tileSize,x1,y1,x2,y2,null);
      ImageIO.write(biTile,"png",new File(fDestDir,fileName+"_"+x1+"_"+y1+ ".png"));       
      x1 += tileSize;
    }
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
