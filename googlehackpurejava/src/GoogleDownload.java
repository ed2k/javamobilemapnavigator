/*************************************************************************/
//package googlehackpurejava;
/*************************************************************************/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/*************************************************************************/
public class GoogleDownload {
  //-----------------------------------------------------------------------
  public GoogleDownload(String mapName,String pictureUrl, double east, double west, double north, double south,long width, long height) {
    
    String saveDir   = "./";
    String mapString = GoogleUtil.prepareMapString(mapName+".gif",east,west,north,south,width,height); 
    
    download(pictureUrl,saveDir+mapName+".gif");
    mapSave(mapString,saveDir+mapName+".map");
  }
  //-----------------------------------------------------------------------
  private void download(String url, String fileName){
    try {
      URL               u   = new URL(url) ;
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
    }catch(IOException e){
      e.printStackTrace();
      System.out.println( "Exception\n" + e ) ;
    }
  }
  //-----------------------------------------------------------------------
  private void mapSave(String mapString, String fileName){
    try {
      File           output  = new File(fileName);
      FileWriter     fWriter = new FileWriter(output);
      BufferedWriter writer  = new BufferedWriter(fWriter);
      writer.write(mapString);
      writer.close();
      System.out.println( "Wrote map file to " + fileName) ;
    } catch (IOException ex) {
      ex.printStackTrace();
      System.out.println( "Exception\n" + ex ) ;
     }
  }
  //-----------------------------------------------------------------------
}
/*************************************************************************/
