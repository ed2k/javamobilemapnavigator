/*************************************************************************/
//package googlehackpurejava;
/*************************************************************************/
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
/*************************************************************************/
public class Main {
  //-----------------------------------------------------------------------
  private static final DecimalFormat df6 = new DecimalFormat("000000");
  //-----------------------------------------------------------------------
  public Main() throws Exception {
  }
  //-----------------------------------------------------------------------
  public void getImages(int imWidth, int imHeight, double lonStart, double latStart, double lonEnd, double latEnd, int zoom, String mapName)throws Exception{    
    if(!checkArgs(imWidth,imHeight,lonStart,latStart,lonEnd,latEnd,zoom)){
      throw new Exception("Invalid args. Exiting.");
    }
    
    String  currMapName =       "";
    int     tileNo      =        0;
    double  lonCurrent  = lonStart;
    double  latCurrent  = latStart;
    Point2D next        = null;
    
    while(lonCurrent<lonEnd || latCurrent>latEnd){
      currMapName = mapName+zoom+df6.format(++tileNo);
      if(lonCurrent>lonEnd){
        lonCurrent = lonStart;
        next       = GoogleUtil.processCoord(imWidth, imHeight, lonCurrent, latCurrent, zoom,currMapName);        
        lonCurrent = next.getX();
      }else{
        latCurrent = latCurrent;
        next       = GoogleUtil.processCoord(imWidth, imHeight, lonCurrent, latCurrent, zoom,currMapName);
        lonCurrent = next.getX();
        if(lonCurrent>lonEnd){
          latCurrent = next.getY();
        }
      }
    }
  }
  //-----------------------------------------------------------------------
  public void prepareForTrekBuddy(final String mapName,final int zoom,int tileSize) throws Exception{
    String gifFiles[]     = new File("./").list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if(name.endsWith(".gif") && name.startsWith(mapName+zoom))
          return true;  
        else
          return false;
      }
    });
    
    String rootMapDir = "maps/"+mapName+zoom;
    new File(rootMapDir).mkdirs();
    
    FileWriter fwAtlas = new FileWriter(new File("maps/cr.tba"));
    fwAtlas.write("Atlas 1.0\r\n");
    fwAtlas.close();
    
    String rootOziDir = "ozi";
    new File(rootOziDir).mkdirs();
    for(int i=0;i<gifFiles.length;i++){
      String fileName = gifFiles[i].substring(0,gifFiles[i].length()-4);
      System.out.println("Processing " + gifFiles[i] + " ...");

      File fSet = new File(rootMapDir+"/"+fileName+"/set");
      File fMap = new File(fileName+".map");
      File fGif = new File(fileName+".gif");
      
      fSet.mkdirs();
      new FileCopy().copy(fMap,new File(rootMapDir+"/"+fileName,fileName+".map"));
      new ImageUtil().splitImage(fGif,fSet,tileSize);
      
      String pngFiles[]     = fSet.list(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if(name.endsWith(".png") && name.startsWith(mapName+zoom))
            return true;  
          else
            return false;
        }
      });
      FileWriter fwSet = new FileWriter(new File(rootMapDir+"/"+fileName,fileName+".set"));
      for(int j=0;j<pngFiles.length;j++){
        fwSet.write(pngFiles[j]+"\r\n");
      }
      fwSet.close();
      
      //backup
      fMap.renameTo(new File(rootOziDir,fileName+".map"));
      fGif.renameTo(new File(rootOziDir,fileName+".gif"));
    }
  }
  //-----------------------------------------------------------------------
  private boolean checkArgs(int imWidth, int imHeight, double lonStart, double latStart, double lonEnd, double latEnd,int zoom){
    System.out.println("imWidth="+imWidth+" imHeight="+imHeight+" lonStart="+lonStart+" latStart="+latStart+" lonEnd="+lonEnd+" latEnd="+latEnd+" zoom="+zoom);
    if(imWidth%256!=0 || imWidth<1 || imWidth>2000 || imHeight%256!=0 || imHeight<1 || imHeight>2000){
      System.out.println("Image bounds must be a multiple of 256 and less than 2000 pix.");
      return false;
    }
    if(zoom<1 || zoom>10){
      System.out.println("Zoom must be between 1 and 10.");
      return false;
    }
    if(lonStart>lonEnd){
      System.out.println("End longitude invalid - must be greater than start longitude.");
      return false;
    }
    if(latStart<latEnd){
      System.out.println("End latitude invalid - must be less than start latitude.");
      return false;
    }
    
    return true;
  }
  //-----------------------------------------------------------------------
  //java.exe  -Dhttp.proxyHost=www-proxy.lmc.ericsson.se  -Dhttp.proxyPort=80 -jar GoogleHackPureJava.jar 1792 14.30773333 50.1363111111 14.59328333 49.9937583333 4 praha 512
  // longmont  40.2035 -105.0552 40.1325 3 longmont 512
  // montreal -73.9, -73.3 45.7 45.3
  public static void main(String[] args) throws Exception {
    Main m = new Main();

    int    imSizeW   = 1792;
    double lonStart = -105.1556;
    double latStart = 40.2035;
    double lonEnd   = -105.0552;
    double latEnd   = 40.1325;
    int    zoom     = 3;
    String mapName  = "test";
    
    if(args.length<7){
      System.out.println("Wrong number of input parameters! use default .");
      //return;
    } else {

     imSizeW   = Integer.parseInt(args[0]);
     lonStart = Double.parseDouble(args[1]);
     latStart = Double.parseDouble(args[2]);
     lonEnd   = Double.parseDouble(args[3]);
     latEnd   = Double.parseDouble(args[4]);
     zoom     = Integer.parseInt(args[5]);
     mapName  = args[6];
    }
    int    tileSize = 256;
    if(args.length>=8){
      tileSize = Integer.parseInt(args[7]);
    }
    int imSizeH = (imSizeW*11/17)/256;
    imSizeH = imSizeH * 256;
    //m.getImages(1792,1792,-10      ,60.0     ,20.0     ,30.0     ,7,"europe");
    m.getImages(imSizeW,imSizeH,lonStart,latStart,lonEnd,latEnd,zoom,mapName);
    m.prepareForTrekBuddy(mapName,zoom,tileSize);
  }
  //-----------------------------------------------------------------------
  class FileCopy {
    public void copy(File source, File dest) throws IOException{
      BufferedInputStream  in  = new BufferedInputStream(new FileInputStream(source));
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      
      byte buff[] = new byte[4096];
      int  len    = 0;
      while((len=in.read(buff))>0){
        out.write(buff,0,len);
      }
      
      in.close();
      out.close();
    }
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
