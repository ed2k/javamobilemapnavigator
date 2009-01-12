/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.common.lang;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;


/**
 * Class loader to load classes from a jar file and also jar files within this jar file
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class JarFileClassLoader extends ClassLoader {
    private final static boolean DEBUG = false;
    
    
    // cache of classes already loaded
    private final Map<String,byte[]> loadedFiles;
    // set of packages already define
    private final Set<Package> packages;
    // path for own classes
    private String ownPath;
    
    
    /**
     * Creates a new instance of JarFileClassLoader.
     * This class loader will check the file classloader.properties for an entry named Extended-Class-Path.
     * This entry must contain a space separated list of jar files which must reside in the root
     * of the application jar file. These jar files are then also check when loading classes via
     * this class loader.
     */
    public JarFileClassLoader( ClassLoader parent ) {
        super( parent );
        
        if( DEBUG ) System.err.println( this.getClass().getName() );
        
        // initialize class file cache and package list
        this.loadedFiles = new Hashtable<String,byte[]>();
        this.packages = new HashSet<Package>();
        
        // remember packages inside this method
        final Set<String> packageNames = new HashSet<String>();
        
        try {
            Properties prop = new Properties();
            
            prop.load( getParent().getResourceAsStream( "JarFileClassLoader.properties" ) );
            
            String ecp = prop.getProperty( "Extended-Class-Path" );
            
            if( DEBUG ) System.err.println( "Str: " + ecp );
            
            if( ecp != null ) {
                StringTokenizer tok = new StringTokenizer( ecp );
                
                while( tok.hasMoreTokens() ) {
                    // read entries inside the jar files
                    String jarFileName = tok.nextToken();
                    JarInputStream jin = new JarInputStream( new BufferedInputStream( getParent().getResourceAsStream( jarFileName ) ) );
                    Manifest manifest = jin.getManifest();
                    JarEntry jarEntry = null;
                    
                    while( ( jarEntry = jin.getNextJarEntry() ) != null ) {
                        // store each entries bytes
                        String name = jarEntry.getName().replace( '\\', '/' );
                        
                        if( ! name.endsWith( "/" ) ) {
                            // load class data into cache
                            long size = jarEntry.getSize();
                            int read = loadClassData( name, jin, (int)size );
                            
                            if( size > 0 && read != size ) {
                                throw new IOException( "Could not load class from file '" + name + "'! Expected size was " + size + " bytes, only " + read + " bytes were read." );
                            }
                        }
                    }
                    
                    jin.close();
                }
            }
            
            // store information about path to own classes
            this.ownPath = prop.getProperty( "Own-Path" );

            // separately load main class
            String mc = prop.getProperty( "Main-Class" );
            
            if( DEBUG ) System.err.println( "MC: '" + mc + "'" );

            if( null != mc ) {
                String filename = mc.replace( '.', '/' ) + ".class";
                BufferedInputStream in = new BufferedInputStream( getParent().getResourceAsStream( filename ) );
                loadClassData( filename, in, -1 );
                in.close();
            }
        } catch( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( "Could not initialize ClassLoader " + this + "!\nThe error message was: " + e.getMessage() );
        }
    }

    
    /**
     * Read class data from stream and store it inside the cache
     */
    private int loadClassData( final String filename, final InputStream in, final int size ) throws IOException {
        int totalread = 0;
        byte[] bytes = null;
        
        if( size > 0 ) {
            bytes = new byte[ size ];

            do {
                int read = in.read( bytes, totalread, bytes.length - totalread );

                if( read > 0 ) totalread += read;
            } while( totalread < size );
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream( os );
            int b;

            while( ( b = in.read() ) >= 0 ) {
                bos.write( b );
                ++totalread;
            }
            bos.flush();
            bos.close();
            
            bytes = os.toByteArray();
        }

        if( DEBUG ) System.err.println( filename + ", expected " + bytes.length + " bytes, read " + totalread + " bytes" );
        this.loadedFiles.put( filename, bytes );
        
        return totalread;
    }
    
    
    /**
     * First check class cache, then use system class loader, then URLClassLoader that includes the additional jar files when loading classes
     */
    protected Class findClass( String name ) throws ClassNotFoundException {
        if( DEBUG ) System.err.println( "Searching class: " + name );
        
        String filename = name.replace( '.', '/' ) + ".class";

        // class is cached?
        if( this.loadedFiles.containsKey( filename ) ) {
            // then return cached data
            byte[] bytes = this.loadedFiles.get( filename );
            
            return defineClass( name, bytes, 0, bytes.length );
        } else {
            // this class should be loaded by the custom class loader?
            if( name.startsWith( this.ownPath ) ) {
                // try to load the class now
                try {
                    BufferedInputStream in = new BufferedInputStream( getParent().getResourceAsStream( filename ) );
                    
                    loadClassData( filename, in, -1 );
                    in.close();
                    if( this.loadedFiles.containsKey( filename ) ) {
                        return findClass( name );
                    }
                } catch( Exception e ) {
                    // file could not be loaded, then class cannot be found
                }
            }
            if( DEBUG ) System.err.println( "Not found: " + name );
            throw new ClassNotFoundException();
        }
    }

    /**
     * First try to load the class using this ClassLoader, then the parent
     */
    public Class loadClass( String name ) throws ClassNotFoundException {
        if( DEBUG ) System.err.println( "loadClass:" + name );
        try {
            return findClass( name );
        } catch( ClassNotFoundException e ) {
            try {
                return super.findClass( name );
            } catch( Exception e2 ) {
                return super.loadClass( name );
            }
        }
    }
    
    /**
     * First check whether the resource can be found inside the cache, otherwise load it using the parent ClassLoader
     */
    public InputStream getResourceAsStream( String name ) {
        if( this.loadedFiles.containsKey( name ) ) {
            return new ByteArrayInputStream( this.loadedFiles.get( name ) );
        } else {
            return super.getResourceAsStream( name );
        }        
    }
}
