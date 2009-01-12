/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Parser for OpenOffice documents
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class OpenOfficeDocumentParser extends AbstractDocumentParser {
    // xml parser
    private XMLDocumentParser xmlParser = null;

    
    
    /**
     * Create new parser
     */
    public OpenOfficeDocumentParser() {
    }

    
    public String getTitle() {
        return this.xmlParser.getTitle();
    }
    
    public String getContent() {
        return this.xmlParser.getContent();
    }

    public String getAuthor() {
        return this.xmlParser.getAuthor();
    }

    public String[] getKeywords() {
        return this.xmlParser.getKeywords();
    }

    public String getDescription() {
        return this.xmlParser.getDescription();
    }

    
    public void parse( final File file ) throws IOException {
        // reset parser
        this.xmlParser = new OpenOfficeXMLDocumentParser() {
            // Do not clear the element map on each call of the parser
            protected void clearElementMap() {}
        };
        
        // access zipped content
        final ZipFile zipFile = new ZipFile( file, ZipFile.OPEN_READ );
        // read content from content.xml
        ZipEntry entry = zipFile.getEntry( "content.xml" );
        InputStream is = zipFile.getInputStream( entry );

        xmlParser.parse( new BufferedInputStream( is ) );
        is.close();

        // read information from meta.xml
        entry = zipFile.getEntry( "meta.xml" );
        is = zipFile.getInputStream( entry );
        xmlParser.parse( is );

        is.close();

        zipFile.close();
    }
    
    public void parse( InputStream is ) throws IOException {
        throw new RuntimeException( "parse( InputStream ) not supported in " + this.getClass().getName() + "!" );
    }
    
    
    public DocumentParser newInstance() {
        return new OpenOfficeDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".sxw", ".odt", ".sxc", ".ods", ".sxi", ".odp" };
        
        return Arrays.asList( types );
    }
}
