/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Interface for a generic document parser that extracts title,
 * author, description, keywords and the text of a document
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public interface DocumentParser {
    /**
     * Return visible text of the document
     *
     * @return  document text
     */
    public String getContent();

    /**
     * Return document title
     *
     * @return  document title, null if the title could not be extracted
     */
    public String getTitle();

    /**
     * Return author of the document
     *
     * @return  author of the document, null if the author could not be extracted
     */
    public String getAuthor();

    /**
     * Return keywords for this document
     *
     * @return  document keywords, null if the keywords could not be extracted
     */
    public String[] getKeywords();

    /**
     * Return document description
     *
     * @return  document description, null if the description could not be extracted
     */
    public String getDescription();

    /**
     * Parse HTML file and extract its text and title
     *
     * @param   file    file to parse
     */
    public void parse( final File file ) throws IOException;

    /**
     * Parse stream with HTML content and extract its text and title
     *
     * @param   is    stream with text to parse
     */
    public void parse( final InputStream is ) throws IOException;
    
    /**
     * Create a new instance of the same parser
     *
     * @return  DocumentParser instance of the same type
     */
    public DocumentParser newInstance();
    public void cleanup();
    /**
     * Get the file types that this parser may parse
     *
     * @return  list of file extensions of the supported file types
     */
    public List<String> getSupportedFileTypes();
}
