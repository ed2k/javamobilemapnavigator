/*************************************************************************/
package bs.earth.google;
/*************************************************************************/
import bs.earth.EarthBean;
import bs.earth.EarthHandler;
import bs.earth.EarthUtil;
import bs.earth.EarthUtil.DataSet;
import bs.earth.EarthUtil.Zoom;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedHashMap;
/*************************************************************************/
public class GoogleEarthHandler implements EarthHandler {
  //-----------------------------------------------------------------------
  private DataSet dataSet         = DataSet.HYBRID;
  private Zoom    zoom            = Zoom.ZOOM_01;
  //-----------------------------------------------------------------------
  public GoogleEarthHandler(DataSet dataSet,Zoom zoom) throws Exception {
    this.dataSet = dataSet;
    this.zoom    = zoom;
  }
  //-----------------------------------------------------------------------
  public EarthBean getTileInfo(double longitude, double latitude) throws Exception {
    EarthBean eb = new EarthBean();
    int    xPos      = EarthUtil.getX(longitude,zoom);
    int    yPos      = EarthUtil.getY(latitude,zoom);
    
    int    minX      = ((int)(xPos/256))*256;
    int    minY      = ((int)(yPos/256))*256;
    int    maxX      = minX+256;
    int    maxY      = minY+256;
    int    posX      = xPos % 256;
    int    posY      = yPos % 256;
    Rectangle2D rectLL     = EarthUtil.getTileLonLatCoordinates(minX,minY,maxX,maxY,zoom);
    Point2D     posXYtile  = new Point2D.Double(posX,posY);
    
    LinkedHashMap images = new LinkedHashMap();
    if(dataSet==DataSet.ROAD){
      images.put("GE_"+dataSet+"_z"+getGoogleZoom()+"_x"+xPos/256+"_y"+yPos/256+".png",new URL("http://mt.google.com/mt?n=404&v=w2.39&x="+xPos/256+"&y="+yPos/256+"&zoom="+getGoogleZoom()));
      eb.setImages(images);
    }else if(dataSet==DataSet.AERIAL){
      String satRef = getSatelliteRef(minY/256,minX/256,getGoogleZoom());
      images.put("GE_"+dataSet+"_z"+getGoogleZoom()+"_t"+satRef+".jpeg",new URL("http://kh.google.com/kh?n=404&v=14&t="+satRef));
      eb.setImages(images);
    }else{
      String satRef = getSatelliteRef(minY/256,minX/256,getGoogleZoom());
      images.put("GE_"+"a"+"_z"+getGoogleZoom()+"_t"+satRef+".jpeg",new URL("http://kh.google.com/kh?n=404&v=14&t="+satRef));
      images.put("GE_"+dataSet+"_z"+getGoogleZoom()+"_x"+xPos/256+"_y"+yPos/256+".png",new URL("http://mt.google.com/mt?n=404&v=w2t.40&x="+xPos/256+"&y="+yPos/256+"&zoom="+getGoogleZoom()));
      eb.setImages(images);
    }
    eb.setTileCoordinates(rectLL);
    eb.setCoordinatePosition(posXYtile);
    
    long nextXpos = xPos+256;
    long nextYpos = yPos+256;
    double nextLon = EarthUtil.getLongitude(nextXpos,zoom);
    double nextLat = EarthUtil.getLatitude(nextYpos,zoom);
    Point2D     nextLL  = new Point2D.Double(nextLon,nextLat);
    eb.setNextCoordinate(nextLL);
    return eb;
  }
  //-----------------------------------------------------------------------
  public static String getSatelliteRef(int tileY ,int tileX , int zoom) {
    int invZoom  = 19 - zoom;
    int stepSize = 1 << (19 - zoom);
    int currentX = 0;
    int currentY = 0;

    StringBuffer satString = new StringBuffer(zoom);
    satString.append("t");

    for (int i = 0; i < invZoom; i++) {
       stepSize >>= 1;

       if ((currentY + stepSize) > tileY) {
          if ((currentX + stepSize) > tileX) {
             satString.append('q');
          }
          else {
             currentX += stepSize;
             satString.append('r');
          }
       }
       else {
          currentY += stepSize;

          if ((currentX + stepSize) > tileX) {
             satString.append('t');
          }
          else {
             currentX += stepSize;
             satString.append('s');
          }
       }
    }

    return satString.toString();
  }
  //-----------------------------------------------------------------------
  public DataSet getDataSet(){
    return dataSet;
  }
  //-----------------------------------------------------------------------
  public Zoom getZoom(){
    return zoom;
  }
  //-----------------------------------------------------------------------
  public String getImageExtension(){
    if(dataSet == DataSet.ROAD)
      return "png";
    else
      return "jpeg";
  }  
  //-----------------------------------------------------------------------
  private int getGoogleZoom(){
    return 19 - zoom.getIntValue();
  }
}
/*************************************************************************/
