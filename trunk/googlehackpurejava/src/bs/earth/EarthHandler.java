/*************************************************************************/
package bs.earth;
/*************************************************************************/
import bs.earth.EarthUtil.DataSet;
import bs.earth.EarthUtil.Zoom;
/*************************************************************************/
public interface EarthHandler {
  //-----------------------------------------------------------------------
  //public super(DataSet dataSet,Zoom zoom);
  public EarthBean getTileInfo(double longitude, double latitude) throws Exception;
  public DataSet getDataSet();
  public Zoom getZoom();
  public String getImageExtension();
  //-----------------------------------------------------------------------
}
/*************************************************************************/