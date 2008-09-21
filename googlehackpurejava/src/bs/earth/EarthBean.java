/*************************************************************************/
package bs.earth;
/*************************************************************************/
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.LinkedHashMap;
/*************************************************************************/
public class EarthBean {
  //-----------------------------------------------------------------------
  private Rectangle2D   tileCoordinates;
  private Point2D       coordinatePosition;
  private Point2D       nextCoordinate;
  private LinkedHashMap images;
  //-----------------------------------------------------------------------
  public EarthBean() {
  }
  //-----------------------------------------------------------------------
  public Rectangle2D getTileCoordinates() {
    return this.tileCoordinates;
  }
  public void setTileCoordinates(Rectangle2D tileCoordinates) {
    this.tileCoordinates = tileCoordinates;
  }
  //-----------------------------------------------------------------------
  public LinkedHashMap getImages() {
    return this.images;
  }
  public void setImages(LinkedHashMap images) {
    this.images = images;
  }
  //-----------------------------------------------------------------------
  public Point2D getCoordinatePosition() {
    return this.coordinatePosition;
  }
  public void setCoordinatePosition(Point2D coordinatePosition) {
    this.coordinatePosition = coordinatePosition;
  }
  //-----------------------------------------------------------------------
  public Point2D getNextCoordinate() {
    return this.nextCoordinate;
  }
  public void setNextCoordinate(Point2D nextCoordinate) {
    this.nextCoordinate = nextCoordinate;
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
