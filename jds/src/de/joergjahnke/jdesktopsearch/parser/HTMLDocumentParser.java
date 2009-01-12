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
import java.util.StringTokenizer;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;


/**
 * HTML parser
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class HTMLDocumentParser extends AbstractDocumentParser {
    // holds the document title
    private String title = null;
    
    
    /**
     * Create new parser
     */
    public HTMLDocumentParser() {
    }
    
    
    public String getTitle() {
        return this.title;
    }
    
    
    public void parse( final InputStream in ) throws IOException {
        // reset fields
        this.content = new StringBuffer();
        
        // parse HTML
        final BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        
        final HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback () {
            // tags whose text should not be added as it is not visible
            private final List<HTML.Tag> nonVisibleTags = Arrays.asList( new HTML.Tag[]{ HTML.Tag.SCRIPT, HTML.Tag.APPLET, HTML.Tag.COMMENT, HTML.Tag.FORM, HTML.Tag.FRAME, HTML.Tag.FRAMESET, HTML.Tag.HEAD, HTML.Tag.IMG, HTML.Tag.MAP, HTML.Tag.NOFRAMES, HTML.Tag.META, HTML.Tag.OBJECT, HTML.Tag.STYLE } );
                    
            
            // visible content follows?
            private HTML.Tag currentTag = null;
            
            
            // store current tag name
            public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos ) {
                this.currentTag = t;
            }
            
            // add text to content if visible or to title
            public void handleText( char[] data, int pos ) {
                if( ! this.nonVisibleTags.contains( this.currentTag ) ) {
                    final StringTokenizer tokenizer = new StringTokenizer( new String( data ) );

                    while( tokenizer.hasMoreTokens() ) {
                        content.append( tokenizer.nextToken() );
                        content.append( ' ' );
                    }
                }
                
                if( this.currentTag.equals( HTML.Tag.TITLE ) ) {
                    title = new String( data );
                }
            }
        };
        new ParserDelegator().parse( reader, callback, true );
        
        reader.close();
    }
    
    
    public DocumentParser newInstance() {
        return new HTMLDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".html", ".htm" };
        
        return Arrays.asList( types );
    }
}
