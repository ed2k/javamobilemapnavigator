/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;


/**
 * Utility methods for Lucene queries
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class QueryUtils {
    /**
     * Get search terms for a given field from a query
     *
     * @param   query   query to analyze
     * @param   fieldName   fieldName for which terms are searched
     * @return  set of search terms
     */
    public static Set<String> getSearchTerms( final Query query, final String fieldName ) {
        final Set<String> result = new HashSet<String>();

        // analyze the sub-queries of BooleanQuerys
        if( query instanceof BooleanQuery ) {
            final BooleanClause[] clauses = ( (BooleanQuery)query ).getClauses();

            for( BooleanClause clause : clauses ) {
                if( ! clause.isProhibited() ) {
                    result.addAll( getSearchTerms( clause.getQuery(), fieldName ) );
                }
            }
        // extract terms from TermQuerys
        } else if( query instanceof TermQuery ) {
            final Term term = ( (TermQuery)query ).getTerm();
            
            if( term.field().equals( fieldName ) ) {
                result.add( term.text() );
            }
        }
        
        return result;
    }
}
