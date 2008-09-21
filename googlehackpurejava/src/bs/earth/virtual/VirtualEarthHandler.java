/*************************************************************************/
package bs.earth.virtual;
/*************************************************************************/
import bs.earth.EarthBean;
import bs.earth.EarthHandler;
import bs.earth.EarthUtil;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import bs.earth.EarthUtil.DataSet;
import bs.earth.EarthUtil.Zoom;
import java.util.LinkedHashMap;
/*************************************************************************/
public class VirtualEarthHandler implements EarthHandler {
  //-----------------------------------------------------------------------
  private DataSet dataSet         = DataSet.HYBRID;
  private Zoom    zoom            = Zoom.ZOOM_01;
  //-----------------------------------------------------------------------
  public VirtualEarthHandler(DataSet dataSet,Zoom zoom) {
    this.dataSet = dataSet;
    this.zoom    = zoom;
  }
  //-----------------------------------------------------------------------
  public EarthBean getTileInfo(double longitude, double latitude) throws Exception {
    EarthBean veb = new EarthBean();
    long   xPos      = EarthUtil.getX(longitude,zoom);
    long   yPos      = EarthUtil.getY(latitude,zoom);
    
    long   minX      = ((long)(xPos/256))*256;
    long   minY      = ((long)(yPos/256))*256;
    long   maxX      = minX+256;
    long   maxY      = minY+256;
    long   posX      = xPos % 256;
    long   posY      = yPos % 256;
    Rectangle2D rectLL     = EarthUtil.getTileLonLatCoordinates(minX,minY,maxX,maxY,zoom);
    Point2D     posXYtile  = new Point2D.Double(posX,posY);
    
    String        quadKey   = tileToQuadKey(xPos/256,yPos/256,zoom.getIntValue());
    String        url       = "http://"+dataSet+quadKey.charAt(0)+".ortho.tiles.virtualearth.net/tiles/"+dataSet+quadKey+"."+getImageExtension()+"?g="+quadKey.length();
    LinkedHashMap images    = new LinkedHashMap();
    images.put("VE_"+dataSet+"_z"+zoom.getIntValue()+"_t"+dataSet+quadKey+"."+getImageExtension(),new URL("http://"+dataSet+quadKey.charAt(0)+".ortho.tiles.virtualearth.net/tiles/"+dataSet+quadKey+"."+getImageExtension()+"?g="+quadKey.length()));
    veb.setImages(images);
    veb.setTileCoordinates(rectLL);
    veb.setCoordinatePosition(posXYtile);
    
    long nextXpos = xPos+256;
    long nextYpos = yPos+256;
    double nextLon = EarthUtil.getLongitude(nextXpos,zoom);
    double nextLat = EarthUtil.getLatitude(nextYpos,zoom);
    Point2D     nextLL  = new Point2D.Double(nextLon,nextLat);
    veb.setNextCoordinate(nextLL);
    
    return veb;
  }
  //-----------------------------------------------------------------------
  private String tileToQuadKey(long tx,long ty,int zl) { 
    String quad = "";
    for (int i = zl; i > 0; i--){
      int mask = 1 << (i - 1);
      int cell = 0;
      if ((tx & mask) != 0)
        cell++;
      if ((ty & mask) != 0)
        cell += 2;
      quad += cell;
    }
    return quad;
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
}
/*************************************************************************/
