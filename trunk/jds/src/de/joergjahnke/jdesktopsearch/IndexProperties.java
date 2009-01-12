/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Extension of the Properties class with special methods for the properties of the index
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class IndexProperties extends Properties {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * indexer type: lucene
     */
    public final static String INDEXER_TYPE_LUCENE = "lucene";
    /**
     * indexer type: sql server
     */
    public final static String INDEXER_TYPE_SQLSERVER = "sqlserver";
    
    
    // name of file with properties for indexable files
    private final static String PROPERTIES_NAME = "JDesktopSearch.properties.xml";
    // index name
    private final static String INDEX_NAME = "JDesktopSearch.index";
    // name of file to store additional information about indexed files
    private final static String INDEXEDFILES_NAME = "JDesktopSearch.indexedFiles";
    // standard file types
    private final static String FILE_TYPES = ".txt,.csv,.html,.java,.cxx,.hxx,.cpp,.hpp,.c,.h,.pl,.xml,.doc,.sxw,.odt,.sxc,.ods,.sxi,.odp,.pdf,.rtf";
    // file types where only the properties are indexed
    private final static String PROPERTIES_ONLY_FILE_TYPES = ".mp3,.ogg,.gif,.jpg,.tiff,.tif,.jpeg,.mpg,.mpeg,.avi,.xls,.ppt";
    
    
    /** Creates a new instance of IndexProperties */
    public IndexProperties() {
    }

    
    /**
     * Load from XML file with pre-defined name
     */
    public void loadFromXML() throws IOException {
        loadFromXML( new FileInputStream( new File( PROPERTIES_NAME ) ) );
    }
    
    /**
     * Initialize properties for a new index
     */
    public void initialize() {
        setProperty( "index_name", INDEX_NAME );
        setProperty( "indexed_files_name", INDEXEDFILES_NAME );
        setProperty( "file_types", FILE_TYPES );
        setProperty( "properties_only_file_types", PROPERTIES_ONLY_FILE_TYPES );
        setProperty( "indexer_type", INDEXER_TYPE_LUCENE );
        
        try {
            storeToXML();
        } catch( IOException e2 ) {
            System.err.println( "Could not create settings file!" );
        }
    }
    
    /**
     * Save application settings
     */
    public void storeToXML() throws IOException {
        storeToXML( new FileOutputStream( new File( PROPERTIES_NAME ) ), "Properties for JDesktopSearch" );
    }
}
