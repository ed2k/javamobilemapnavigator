/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.abstractionlayer;


import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.Document;


/**
 * Interface for an index writer which updates files in the index
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public interface AbstractIndexWriter {
    /**
     * Add a document to the index
     *
     * @param   doc document to add
     * @throws  IOException if the document cannot be added
     */
    public void addDocument( Document doc ) throws IOException;
    
    /**
     * Delete a document from the index
     *
     * @param   file    file to remove
     * @throws  IOException if the document cannot be deleted
     */
    public void deleteDocument( File file ) throws IOException;
    
    /**
     * Optimize the index.
     * This is an optional operation.
     *
     * @throws  IOException if the index cannot be optimized
     */
    public void optimize() throws IOException;
    
    /**
     * Close the index.
     * This is an optional operation
     *
     * @throws  IOException if the index cannot be closed
     */
    public void close() throws IOException;
}
