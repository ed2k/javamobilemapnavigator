/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.textmining.text.extraction.WordExtractor;


/**
 * Parser for MS Word documents
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class MSWordDocumentParser extends AbstractDocumentParser {
    /**
     * Create new parser
     */
    public MSWordDocumentParser() {
    }

    
    public void parse( final InputStream is ) throws IOException {
        // reset fields
        this.content = new StringBuffer();

        // use WordExtractor to extract content
        final WordExtractor wordExtractor = new WordExtractor();

        try {
            this.content.append( wordExtractor.extractText( is ) );
        } catch( Exception e ) {
            throw new IOException( "Could not extract text from Microsoft Word document!\nThe error message was: " + e.getMessage() );
        }
    }
    
    
    public DocumentParser newInstance() {
        return new MSWordDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".doc" };
        
        return Arrays.asList( types );
    }
}
