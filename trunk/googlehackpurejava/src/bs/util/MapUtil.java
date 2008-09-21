/*************************************************************************/
package bs.util;
/*************************************************************************/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
/*************************************************************************/
public class MapUtil {
  //-----------------------------------------------------------------------
  public  static final int           COORD_KIND_LATTITUDE = 1;
  public  static final int           COORD_KIND_LONGITUDE = 2;
  private static final DecimalFormatSymbols dfs                  = new DecimalFormatSymbols(Locale.ENGLISH); 
  private static final DecimalFormat        format6dec           = new DecimalFormat("#0.000000",dfs);
  //-----------------------------------------------------------------------
  private MapUtil() {
  }
  //-----------------------------------------------------------------------
  public static void createCalibrationFile(File file,double east, double west, double north, double south, long width, long height) throws Exception {
    StringBuffer sbMap = new StringBuffer();
    
    sbMap.append("OziExplorer Map Data File Version 2.2\r\n");
    sbMap.append(file.getName().substring(0,file.getName().length()-4)+".png" + "\r\n");
    sbMap.append(file.getName().substring(0,file.getName().length()-4)+".png" + "\r\n");
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
    
    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
    bw.write(sbMap.toString()); 
    bw.flush();
    bw.close();
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
