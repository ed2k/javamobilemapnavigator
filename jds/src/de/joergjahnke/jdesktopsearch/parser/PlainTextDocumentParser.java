/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


/**
 * Parser for plain text documents
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class PlainTextDocumentParser extends AbstractDocumentParser {
    /**
     * Create new parser
     */
    public PlainTextDocumentParser() {
    }

    
    public void parse( final InputStream is ) throws IOException {
        // reset fields
        this.content = new StringBuffer();

        // read all lines from file
        final BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        String line = null;
        
        while( ( line = reader.readLine() ) != null ) {
	        this.content.append( line );
	        this.content.append( '\n' );
        }

        reader.close();
    }
    
    public DocumentParser newInstance() {
        return new PlainTextDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".txt" };
        
        return Arrays.asList( types );
    }
}
