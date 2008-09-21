/*************************************************************************/
package bs.util;
/*************************************************************************/
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
/*************************************************************************/
public class ImageUtil {
  //-----------------------------------------------------------------------
  private ImageUtil() {
  }
  //-----------------------------------------------------------------------
  public static void merge(String[] images,File outImage, int colorType) throws Exception {    
    BufferedImage imgMerged = null;
    Graphics2D    gMerged   = null;
    int x1=0;
    int y1=0;
    int x2=0;
    int y2=0;
    String lastFileName = images[images.length-1];
    int x = Integer.parseInt(lastFileName.substring(lastFileName.indexOf("_x")+2,lastFileName.indexOf("_y")))+1;
    int y = Integer.parseInt(lastFileName.substring(lastFileName.indexOf("_y")+2,lastFileName.indexOf(".")))+1;

    for(int i=0;i<images.length;i++){
      BufferedImage imgPart   = ImageIO.read(new File(images[i]));
      if(imgMerged==null){
        int WIDTH  = x*imgPart.getWidth();
        int HEIGHT = y*imgPart.getHeight();
        imgMerged  = new BufferedImage(WIDTH,HEIGHT,colorType);
        gMerged    = imgMerged.createGraphics();
      }
      x2=x1+imgPart.getWidth();
      y2=y1+imgPart.getHeight();
      System.out.println("Drawing to "+x1+","+y1+"  "+x2+","+y2);
      gMerged.drawImage(imgPart,x1,y1,x2,y2,0,0,imgPart.getWidth(),imgPart.getHeight(),null);
      x1=x1+imgPart.getWidth();
      if(x1>=imgMerged.getWidth()){
        x1=0;
        y1=y1+imgPart.getHeight();  
      }
    }
    ImageIO.write(imgMerged,"png",outImage); 
    for(int i=0;i<images.length;i++){
      new File(images[i]).delete();
    }
    System.out.println("Merge done!");    
  }
  //-----------------------------------------------------------------------
  public static void download(LinkedHashMap images, String fileName) throws Exception {
    String   ext       = fileName.substring(fileName.indexOf(".")+1);
    Iterator imagesI   = images.keySet().iterator();
    if(images.size()==1){
        String cacheName = (String)imagesI.next();
        URL    url       = (URL)images.get(cacheName);
        downloadImageTry(cacheName,url,fileName);
    }else{
      BufferedImage imgMerged   = null;
      Graphics2D    gMerged     = null;
      String        tmpFileName = null;
      int           counter     = 0;
      while(imagesI.hasNext()){
        String cacheName = (String)imagesI.next();
        URL    url       = (URL)images.get(cacheName);
        tmpFileName = "partialImage_"+ (counter++) + "."+ext;
        downloadImageTry(cacheName,url,tmpFileName);
        
        if(imgMerged==null){
          imgMerged  = ImageIO.read(new File(tmpFileName));
          gMerged    = imgMerged.createGraphics();
        }else{
          BufferedImage imgPart   = ImageIO.read(new File(tmpFileName));
          gMerged.drawImage(imgPart,0,0,imgMerged.getWidth(),imgMerged.getHeight(),0,0,imgPart.getWidth(),imgPart.getHeight(),null);
          
        }
      }

      ImageIO.write(imgMerged,ext,new File(fileName)); 
      for(int i=0;i<counter;i++){
        new File("partialImage_"+i+"."+ext).delete();
      }
    }             
  }
  //-----------------------------------------------------------------------
  private static void downloadImageTry(String cacheName, URL url, String fileName) throws Exception {
      try{
        downloadImage(cacheName,url,fileName);
      }catch(Exception e){
        System.out.println("Don't you die on me now!! I give you one more chance....");
        Thread.currentThread().sleep(1000);
        try{
          downloadImage(cacheName,url,fileName);
        }catch(Exception ex){
          ex.printStackTrace();
          createErrorImage(fileName);
        }
      }
  }
  //-----------------------------------------------------------------------
  private static void downloadImage(String cacheName, URL u, String fileName) throws Exception {
    boolean wasCached = false;
    System.out.println("cacheName="+cacheName);
    System.out.println("u="+u);
    System.out.println("fileName="+fileName);
    
    File cacheDir = new File("cache");
    if(cacheDir.exists()==false)
      cacheDir.mkdirs();
    
    File imgFile = new File(cacheDir,cacheName);
    if(imgFile.exists()==false){
      System.out.println("Img " + imgFile.getName() + " is not cached... download...");
      HttpURLConnection huc = (HttpURLConnection)u.openConnection() ;

      huc.setRequestMethod( "GET" ) ;
      huc.connect(  ) ;

      InputStream is   = huc.getInputStream() ;
      int         code = huc.getResponseCode() ;

      if  ( code == HttpURLConnection.HTTP_OK )   {
        byte[]           buffer       = new byte [ 4096 ] ;
        File             output       = new File(fileName) ;
        FileOutputStream outputStream = new FileOutputStream(output) ;
        System.out.println( "Ready to download " + fileName +" " + huc.getContentLength()  + " bytes" ) ;

        int totBytes,bytes,sumBytes = 0;
        totBytes = huc.getContentLength(  ) ;
        while((bytes=is.read(buffer))>0){
          sumBytes+= bytes;
          outputStream.write(buffer,0,bytes) ;
        }
        outputStream.close(  ) ;
        System.out.println( sumBytes + " of " + totBytes + " " + (  ( float ) sumBytes/ ( float ) totBytes ) *100 + "% done" ) ;
      }
      huc.disconnect(  ) ;

      System.out.println("Caching file " + imgFile.getName());
      FileCopy.copy(new File(fileName),imgFile);
    }else{
      System.out.println("Img " + imgFile.getName() + " is cached... ");
      FileCopy.copy(imgFile,new File(fileName));
    }
  }
  //-----------------------------------------------------------------------
  public static void createErrorImage(String fileName) throws Exception {
    String        ext    = fileName.substring(fileName.indexOf(".")+1);
    BufferedImage img    = new BufferedImage(256,256,BufferedImage.TYPE_BYTE_INDEXED);
    Graphics2D    gImg   = (Graphics2D)img.getGraphics();
    
    gImg.setBackground(Color.WHITE);
    gImg.fillRect(0,0,256,256);

    Font f  =new Font("SansSerif",Font.BOLD,48);
    Map  map = f.getAttributes();
    map.put(TextAttribute.FOREGROUND,Color.BLUE);
    gImg.setFont(f.deriveFont(map));
    gImg.drawString("?",256/2 - 12,256/2);
    ImageIO.write(img,ext,new File(fileName));     
  }
  //-----------------------------------------------------------------------
  public static void splitImage(File fImage, File fDestDir,int tileSize,int colorType) throws Exception{      
    BufferedImage biImage = ImageIO.read(fImage);
    int           width   = biImage.getWidth();
    int           height  = biImage.getHeight();
    int           x1      = 0;
    int           x2      = 0;
    int           y1      = 0;
    int           y2      = 0;
    int           tileSizeX = tileSize>width  ? width  : tileSize;
    int           tileSizeY = tileSize>height ? height : tileSize;
    
    String        fileName= fImage.getName().substring(0,fImage.getName().length()-4);
     
    while(x1<width || y1<height){
      if(x1>=width){
        x1  = 0;
        y1 += tileSizeY;
        if(y1>=height)
          break;
      }
      
      x2=x1+tileSizeX;
      if(x2>width)
        x2=width;
      y2=y1+tileSizeY;
      if(y2>height)
        y2=height;
      
      int imSizeW = x2>width  ?  width-x1:x2-x1;
      int imSizeH = y2>height ?  height-y1:y2-y1;
      
      BufferedImage biTile = null;
      if(colorType==BufferedImage.TYPE_BYTE_INDEXED){
        try{
          biTile = new BufferedImage(imSizeW,imSizeH,colorType,(IndexColorModel)biImage.getColorModel());
        }catch(Exception e){
          biTile = new BufferedImage(imSizeW,imSizeH,colorType);
        }
      }else{
        biTile = new BufferedImage(imSizeW,imSizeH,colorType);
      }
      Graphics2D    gTile  = biTile.createGraphics();
      gTile.drawImage(biImage,0,0,imSizeW,imSizeH,x1,y1,x2,y2,null);
      ImageIO.write(biTile,"png",new File(fDestDir,fileName+"_"+x1+"_"+y1+ ".png"));       
      x1 += tileSizeX;
    }
  }
  //-----------------------------------------------------------------------  
}
/*************************************************************************/
