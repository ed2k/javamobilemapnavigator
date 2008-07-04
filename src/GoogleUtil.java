/*************************************************************************/
//package googlehackpurejava;
/*************************************************************************/
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
/*************************************************************************/
public class GoogleUtil {
  //-----------------------------------------------------------------------
  public  static final int           COORD_KIND_LATTITUDE = 1;
  public  static final int           COORD_KIND_LONGITUDE = 2;
  private static final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH); 
  private static final DecimalFormat format6dec           = new DecimalFormat("#0.000000",dfs);
  //-----------------------------------------------------------------------
  public GoogleUtil() {
  }
  //-----------------------------------------------------------------------
  public static Point2D processCoord(int imWidth, int imHeight, double lonCurrent, double latCurrent, int zoom, String mapName) throws Exception{
    double lngWidth256             = 360.0 / GoogleUtil.getSlik256NaIzbranemZoomu(zoom); // width in degrees longitude of 256 pix image ( ena 256 slika zajame x�)
    double pixWidthDegLon          = lngWidth256/256; // 1 pix zajame x�
    double lonNext                 = lonCurrent + imWidth*pixWidthDegLon;

    double latHeight256Mercator    = 1.0 / GoogleUtil.getSlik256NaIzbranemZoomu(zoom); // height in "normalized" mercator 0,0 top left
    double pixHeightDegLatMercator = latHeight256Mercator/256;
    double latCurrentMercator      = GoogleUtil.getNormalizedMercatorLatitude(latCurrent);
    double latNextMercator         = latCurrentMercator + imHeight*pixHeightDegLatMercator;
    double latNext                 = GoogleUtil.getLatitudeFromMercator(latNextMercator);
      
    double lonCurrWest             = lonCurrent - ((imWidth/2.0)*pixWidthDegLon);
    double lonCurrEast             = lonCurrent + ((imWidth/2.0)*pixWidthDegLon);
    double latCurrNorthMercator    = latCurrentMercator-((imHeight/2.0)*pixHeightDegLatMercator);
    double latCurrSouthMercator    = latCurrentMercator+((imHeight/2.0)*pixHeightDegLatMercator);;
    double latCurrNorth            = GoogleUtil.getLatitudeFromMercator(latCurrNorthMercator);
    double latCurrSouth            = GoogleUtil.getLatitudeFromMercator(latCurrSouthMercator);
    
    String url = "http://maps.google.com/mapdata?latitude_e6="+GoogleUtil.getE6notation(latCurrent)+"&longitude_e6="+GoogleUtil.getE6notation(lonCurrent)+"&zm="+GoogleUtil.getGoogleZoomValue(zoom,imWidth,lonCurrEast,lonCurrWest)+"&w="+imWidth+"&h="+imHeight+"&cc=us&min_priority=1";
    System.out.println("lonCurrent   = "+lonCurrent);
    System.out.println("lonNext      = "+lonNext);
    System.out.println("latCurrent   = "+latCurrent);
    System.out.println("latNext      = "+latNext);
    System.out.println("lonCurrWest  = "+lonCurrWest);
    System.out.println("lonCurrEast  = "+lonCurrEast);
    System.out.println("latCurrNorth = "+latCurrNorth);
    System.out.println("latCurrSouth = "+latCurrSouth);
    System.out.println("url          = "+url);
    
    GoogleDownload gd = new GoogleDownload(mapName,url,lonCurrEast,lonCurrWest,latCurrNorth,latCurrSouth,imWidth,imHeight);
    Point2D.Double next = new Point2D.Double(lonNext,latNext);
    return next;
  }
  //-----------------------------------------------------------------------
  private static double getLatitudeFromMercator(double lat){
    return (180 / Math.PI) * ((2 * Math.atan(Math.exp(Math.PI * (1 - (2 * lat))))) - (Math.PI / 2));
  }
  //-----------------------------------------------------------------------
  private static double getNormalizedMercatorLatitude(double lat){
    return 0.5 - ((Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180))) / Math.PI) / 2.0);
  }
  //-----------------------------------------------------------------------
  private static long getE6notation(double coord){
    double coordE6 = coord*1e6;
    if(coordE6<0)
      coordE6 += Math.pow(2,32);
    return (long)Math.floor(coordE6);
  }
  //-----------------------------------------------------------------------
  private static long getGoogleZoomValue(int zoom,int imWidth,double lonEast,double lonWest) throws Exception {
    double multiplier = 0.0;
    switch(zoom){
      case  1:
      case  2:
      case  3: 
      case 10: multiplier = 100000.0;break;
      case  4: 
      case  5: 
      case  6: 
      case  7: 
      case  8: 
      case  9: multiplier = 75000.0;break;
      default: throw new Exception("Invalid value");
    }
    
    double linkZoomValue = 2.0 * Math.round(multiplier * (lonEast-lonWest));
    return (long)linkZoomValue;
  }
  //-----------------------------------------------------------------------
  private static long getSlik256NaIzbranemZoomu(int zoom) throws Exception {
    return 1 << (18 - zoom);
  }
  //-----------------------------------------------------------------------
  public static String prepareMapString(String fileName,double east, double west, double north, double south, long width, long height){
    StringBuffer sbMap = new StringBuffer();
    
    sbMap.append("OziExplorer Map Data File Version 2.2\r\n");
    sbMap.append(fileName + "\r\n");
    sbMap.append(fileName + "\r\n");
    sbMap.append("1 ,Map Code,\r\n");
    sbMap.append("WGS 84,WGS 84,   0.0000,   0.0000,WGS 84\r\n");
    sbMap.append("Reserved 1\r\n");
    sbMap.append("Reserved 2\r\n");
    sbMap.append("Magnetic Variation,,,E\r\n");
    sbMap.append("Map Projection,Mercator,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No\r\n");
    //sbMap.append("Map Projection,Latitude/Longitude,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No\r\n");
    
    sbMap.append("Point01,xy,    0,    0,in, deg," + getDegMinFormat(north,COORD_KIND_LATTITUDE) + "," + getDegMinFormat(west,COORD_KIND_LONGITUDE) + ", grid,   ,           ,           ,N\r\n");
    sbMap.append("Point02,xy," + (width-1) + ",0,in, deg," + getDegMinFormat(north,COORD_KIND_LATTITUDE) + "," + getDegMinFormat(east,COORD_KIND_LONGITUDE) + ", grid,   ,           ,           ,N\r\n");
    sbMap.append("Point03,xy,    0," + (height-1) + ",in, deg," + getDegMinFormat(south,COORD_KIND_LATTITUDE) + "," + getDegMinFormat(west,COORD_KIND_LONGITUDE) + ", grid,   ,           ,           ,N\r\n");
    sbMap.append("Point04,xy," + (width-1) + "," + (height-1) + ",in, deg," + getDegMinFormat(south,COORD_KIND_LATTITUDE) + "," + getDegMinFormat(east,COORD_KIND_LONGITUDE) + ", grid,   ,           ,           ,N\r\n");
    sbMap.append("Point05,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N\r\n");
    sbMap.append("Point06,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N\r\n");
    sbMap.append("Point07,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N\r\n");
    sbMap.append("Point08,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N\r\n");
    sbMap.append("Point09,xy,     ,     ,in, deg,    ,        ,N,    ,        ,W, grid,   ,           ,           ,N\r\n");
    sbMap.append("Point10,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point11,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point12,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point13,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point14,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point15,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point16,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point17,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point18,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point19,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point20,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point21,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point22,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point23,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point24,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point25,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point26,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point27,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point28,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point29,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Point30,xy,     ,     ,in, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
    sbMap.append("Projection Setup,,,,,,,,,,\r\n");
    sbMap.append("Map Feature = MF ; Map Comment = MC     These follow if they exist\r\n");
    sbMap.append("Track File = TF      These follow if they exist\r\n");
    sbMap.append("Moving Map Parameters = MM?    These follow if they exist\r\n");

    sbMap.append("MM0,Yes\r\n");
    sbMap.append("MMPNUM,4\r\n");
    sbMap.append("MMPXY,1,0,0\r\n");
    sbMap.append("MMPXY,2,"+(width-1)+",0\r\n");
    sbMap.append("MMPXY,3,0,"+(height-1)+"\r\n");
    sbMap.append("MMPXY,4,"+(width-1)+","+(height-1)+"\r\n");
    sbMap.append("MMPLL,1,  "+format6dec.format(west)+","+format6dec.format(north)+"\r\n");
    sbMap.append("MMPLL,2,  "+format6dec.format(east)+","+format6dec.format(north)+"\r\n");
    sbMap.append("MMPLL,3,  "+format6dec.format(west)+","+format6dec.format(south)+"\r\n");
    sbMap.append("MMPLL,4,  "+format6dec.format(east)+","+format6dec.format(south)+"\r\n");
    
    sbMap.append("IWH,Map Image Width/Height," + width + "," + height + "\r\n");
    
    return sbMap.toString();
  }
  //-----------------------------------------------------------------------
  private static String getDegMinFormat(double coord, int COORD_KIND){
    boolean neg  = coord<0.0 ? true:false;
    int     deg   = (int)coord;
    double  min   = (coord-deg)*60;
    
    StringBuffer sbOut = new StringBuffer();
    sbOut.append((int)Math.abs(deg));
    sbOut.append(",");
    sbOut.append(format6dec.format(Math.abs(min)));
    sbOut.append(",");
    if(COORD_KIND==COORD_KIND_LATTITUDE){
      sbOut.append(neg ? "S":"N");
    }else{
      sbOut.append(neg ? "W":"E");
    }
    
    return sbOut.toString();
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
