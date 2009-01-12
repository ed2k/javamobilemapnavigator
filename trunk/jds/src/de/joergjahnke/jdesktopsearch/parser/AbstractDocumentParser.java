/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Abstract implmentation of the DocumentParser interface
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public abstract class AbstractDocumentParser implements DocumentParser {
    /**
     * contains the visible text of the document
     */
    protected StringBuffer content = new StringBuffer();

    
    /**
     * Create new parser
     */
    protected AbstractDocumentParser() {
    }
    
    
    public String getContent() {
        return this.content.toString();
    }
    
    public String getTitle() {
        return null;
    }

    public String getAuthor() {
        return null;
    }

    public String[] getKeywords() {
        return null;
    }
    
    public String getDescription() {
        return null;
    }
    
    public void parse( final File file ) throws IOException {
        final InputStream in = new BufferedInputStream( new FileInputStream( file ) );
        
        parse( in );
        in.close();
    }
}
