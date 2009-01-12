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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;


/**
 * RTF parser
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class RTFDocumentParser extends AbstractDocumentParser {
    /**
     * Create new parser
     */
    public RTFDocumentParser() {
    }

    
    public void parse( final InputStream is ) throws IOException {
        // reset fields
        this.content = new StringBuffer();
        
        // use RTFEditorKit to extract the text
        final DefaultStyledDocument styledDoc = new DefaultStyledDocument();
        
        try {
            new RTFEditorKit().read( is, styledDoc, 0 );
            content.append( styledDoc.getText( 0, styledDoc.getLength() ) );
        } catch( BadLocationException e ) {
            throw new IOException( "Could not extract RTF content from document! The error message was: " + e.getMessage() );
        }
    }
    
    
    public DocumentParser newInstance() {
        return new RTFDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".rtf" };
        
        return Arrays.asList( types );
    }
}
