/*************************************************************************/
package bs.util;
/*************************************************************************/
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/*************************************************************************/
public class FileCopy {
  //-----------------------------------------------------------------------
  public static void copy(File source, File dest) throws IOException{
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
  //-----------------------------------------------------------------------
}
/*************************************************************************/
