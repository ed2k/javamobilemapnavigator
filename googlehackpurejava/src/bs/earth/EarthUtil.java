/*************************************************************************/
package bs.earth;

import java.awt.geom.Rectangle2D;
/*************************************************************************/
public class EarthUtil {
  //-----------------------------------------------------------------------
  public static double  EARTH_RADIUS    = 6378137;
  public static double  EARTH_CIRCUM    = EARTH_RADIUS * 2.0 * Math.PI;
  public static double  EARTH_HALF_CIRC = EARTH_CIRCUM / 2.0;
  //-----------------------------------------------------------------------
  public EarthUtil() {
  }
  //-----------------------------------------------------------------------
  public static Rectangle2D.Double getTileLonLatCoordinates(long minX,long minY,long maxX,long maxY,Zoom zoom){
    double minXLL = getLongitude(minX,zoom);
    double minYLL = getLatitude(minY,zoom);
    double maxXLL = getLongitude(maxX,zoom);
    double maxYLL = getLatitude(maxY,zoom);
    
    Rectangle2D.Double rect = new Rectangle2D.Double(minXLL,minYLL,maxXLL-minXLL,maxYLL-minYLL);
    return rect;
  }
  //-----------------------------------------------------------------------
  public static  double radToDeg(double d) {
    return d / Math.PI * 180.0;
  }
  //-----------------------------------------------------------------------
  public static  double degToRad(double d) {
    return d * Math.PI / 180.0;
  }
  //-----------------------------------------------------------------------
  public static  int getY(double latitude, Zoom zoom){
    double arc     = EarthUtil.EARTH_CIRCUM / ((1 << zoom.getIntValue()) * 256);
    double sinLat  = Math.sin(degToRad(latitude));
    double metersY = EarthUtil.EARTH_RADIUS / 2 * Math.log((1 + sinLat) / (1 - sinLat));
    double y       = Math.round((EarthUtil.EARTH_HALF_CIRC - metersY) / arc);
    return (int)y;
  }
  //-----------------------------------------------------------------------
  public static  int getX(double longitude, Zoom zoom){
    double arc     = EarthUtil.EARTH_CIRCUM / ((1 << zoom.getIntValue()) * 256);
    double metersX = EarthUtil.EARTH_RADIUS * degToRad(longitude);
    double x       = Math.round((EarthUtil.EARTH_HALF_CIRC + metersX) / arc);
    return (int)x;
  }
  //-----------------------------------------------------------------------
  public static  double getLatitude(long y, Zoom zoom) {
    double arc     = EarthUtil.EARTH_CIRCUM / ((1 << zoom.getIntValue()) * 256);
    double metersY = EarthUtil.EARTH_HALF_CIRC - (y * arc);
    double a       = Math.exp(metersY * 2 / EarthUtil.EARTH_RADIUS);
    double result  = radToDeg(Math.asin((a - 1) / (a + 1)));
    return result;
  }
  //-----------------------------------------------------------------------
  public static double getLongitude(long x, Zoom zoom) {
    double arc     = EarthUtil.EARTH_CIRCUM / ((1 << zoom.getIntValue()) * 256);
    double metersX = (x * arc) - EarthUtil.EARTH_HALF_CIRC;
    double result  = radToDeg(metersX / EarthUtil.EARTH_RADIUS);
    return result;
  }
  //-----------------------------------------------------------------------
  public static  double getMetersPerPixel(Zoom zoom){    
    double tilesX = Math.sqrt(getNumberOfTiles(zoom));
    double mpp    = Math.round((EarthUtil.EARTH_CIRCUM/(tilesX*256))*100.0)/100.0;
    return mpp;
  }
  //-----------------------------------------------------------------------
  public static  long getNumberOfTiles(Zoom zoom){
    return (long)Math.pow((1 << (zoom.getIntValue())),2);
  }
  //-----------------------------------------------------------------------
  public enum Zoom {
    ZOOM_01,ZOOM_02,ZOOM_03,ZOOM_04,ZOOM_05,ZOOM_06,ZOOM_07,ZOOM_08,ZOOM_09,ZOOM_10,ZOOM_11,ZOOM_12,ZOOM_13,ZOOM_14,ZOOM_15,ZOOM_16,ZOOM_17,ZOOM_18,ZOOM_19;
    public String toString(){
      return this.name().substring("ZOOM_".length());
    }
    public int getIntValue(){
      return Integer.parseInt(this.name().substring("ZOOM_".length()));
    }
  };
  //-----------------------------------------------------------------------
  public enum DataSet {
    ROAD,AERIAL,HYBRID;
    public String toString(){
      if(this == DataSet.ROAD)
        return "r";
      else if (this == DataSet.HYBRID)
        return "h";
      else if (this == DataSet.AERIAL)
        return "a";
      else
        return "";
    }
  };
  //-----------------------------------------------------------------------
  public enum MapSource {
    VIRTUALEARTH,GOOGLEEARTH    
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
