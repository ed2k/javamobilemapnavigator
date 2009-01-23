/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import de.joergjahnke.jdesktopsearch.abstractionlayer.AbstractIndexManager;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import org.apache.lucene.document.Document;


/**
 * Utilities for executing the JDesktopSearch application from the command line
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class CommandLineUtilities implements Observer {
    // index manager to use
    private final AbstractIndexManager indexManager;
    // files observed
    private volatile int filesObserved;
    
    
    /** Creates a new instance of CommandLineUtilities */
    public CommandLineUtilities( AbstractIndexManager indexManager ) {
        this.indexManager = indexManager;
    }
    

    /**
     * Execute command as defined in command arguments
     *
     * @param   args    arguments to execute
     *                  can be either of<ul>
     *                  <li>"search <searchterms>" to do a search in the index and get all files with the given terms
     *                  <li>"properties" to get the index properties
     *                  <li>"update" to update all directories in the index
     *                  <li>"new" to delete the existing index and start a new one
     *                  <li>"add <directory>" to add a new directory to the existing index
     *                  </ul>
     */
    public void execute( final String[] args ) throws IOException, ClassNotFoundException {
        // do a search?
        if( "search".equals( args[ 0 ] ) && args.length >= 2 ) {
            // collect search text
            final StringBuffer searchText = new StringBuffer( args[ 1 ] );

            for( int i = 2 ; i < args.length ; ++i ) {
                searchText.append( ' ' );
                searchText.append( args[ i ] );
            }

            // do search
            System.out.println( "Searching index for '" + searchText + "'..." );
            executeSearch( searchText.toString() );
        } else if( "properties".equals( args[ 0 ] ) && args.length == 1 ) {
            System.out.println( indexManager.getProperties() );
        } else if( "new".equals( args[ 0 ] ) && args.length == 1 ) {
            System.out.println( "Deleting old index..." );
            this.indexManager.clear();
            System.out.println( "Done." );
        } else if( "add".equals( args[ 0 ] ) && args.length == 2 ) {
            System.out.println( "Adding directory " + args[ 1 ] + " to index..." );
            this.indexManager.addObserver( this );
            this.indexManager.add( new File(args[ 1 ]) );
            
            this.indexManager.deleteObserver( this );
        } else if( "update".equals( args[ 0 ] ) && args.length == 1 ) {
            System.out.println( "Updating existing index..." );
            this.indexManager.addObserver( this );
            this.indexManager.updateAll();
            this.indexManager.deleteObserver( this );
        } else if( "optimize".equals( args[ 0 ] ) && args.length == 1 ) {
            System.out.println( "Optimizing existing index..." );
            this.indexManager.optimize();
            System.out.println( "Done." );
        } else {
            System.out.println( "Usage: java -jar jdesktopsearch.jar [search <searchtext> | properties] | new | add <directory> | optimize" );
        }
    }

    /**
     * Display search results on the command line
     *
     * @param   searchText  search terms
     */
    public void executeSearch( final String searchText ) throws IOException {
        final Set<String> searchFields = new HashSet<String>();

        searchFields.add( "contents" );
        searchFields.add( "description" );
        searchFields.add( "keywords" );
        searchFields.add( "path" );
        searchFields.add( "title" );

        final SearchResult searchResult = this.indexManager.search( searchText.toString(), null, 0, searchFields );
        final Collection<Document> documents = searchResult.getDocuments();

        System.out.println( "Search string was found in the following files:" );

        for( Document doc : documents ) {
            System.out.println( doc.get( "path" ) );
        }
    }
    
    
    // implementation of the Observer interface
   
    /**
     * Update progress bar and action label
     */
    public void update( final Observable observable, final Object arg ) {
        assert( arg instanceof IndexStatusMessage );
        
        final IndexStatusMessage statusMessage = (IndexStatusMessage)arg;

        if( statusMessage.fileCount > 0 ) {
            this.filesObserved += statusMessage.fileCount;
            if( this.filesObserved % 100 == 0 ) {
                System.out.print( "." );
            }
        }
        // check for special events
        switch( statusMessage.event ) {
            // a long operation has started?
            case IndexStatusMessage.EVENT_LONG_OPERATION_STARTED:
                // reset counter
                this.filesObserved = 0;
                break;
            // a long operation has ended or we need to reset?
            case IndexStatusMessage.EVENT_RESET:
            case IndexStatusMessage.EVENT_LONG_OPERATION_ENDED:
                System.out.println();
                System.out.println( "Done." );
                break;
        }
    }
}

