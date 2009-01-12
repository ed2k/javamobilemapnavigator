/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import java.util.Collection;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;


/**
 * Data structure for the result of a search.
 * Contains result document plus the generated query.
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class SearchResult {
    // generated query
    private final Query query;
    // retrieved documents
    private final Collection<Document> documents;
    
    
    /**
     * Creates a new instance of SearchResult
     *
     * @param   query   generated query object
     * @param   documents   retrieved result documents
     */
    public SearchResult( final Query query, final Collection<Document> documents ) {
        this.query = query;
        this.documents = documents;
    }

    
    /**
     * Get the generated query
     */
    public final Query getQuery() {
        return this.query;
    }
    
    /**
     * Get the retrieved result documents
     */
    public final Collection<Document> getDocuments() {
        return this.documents;
    }
}
