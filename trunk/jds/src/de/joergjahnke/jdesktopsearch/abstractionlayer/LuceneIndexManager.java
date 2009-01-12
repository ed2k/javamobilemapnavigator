/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.abstractionlayer;


import de.joergjahnke.common.io.FileUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.Hits;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;


/**
 * Lucene implementation of the AbstractIndexManager
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class LuceneIndexManager extends AbstractIndexManager {
    // list of indexed files with their modification date
    private SortedMap<String,Long> indexedFiles = Collections.synchronizedSortedMap( new TreeMap<String,Long>() );
    // list of indexed root directories
    private Set<String> indexedRootDirectories = Collections.synchronizedSet( new HashSet<String>() );
    // name of file containing list of indexed files
    private final String indexedFilesName;
    // has the list of indexed files to be flushed to disk after a change?
    private boolean wasChanged = false;
    
    
    /**
     * Creates a new instance of LuceneIndexManager
     */
    public LuceneIndexManager( final Properties properties ) {
        super( properties );
        this.indexedFilesName = properties.getProperty( "indexed_files_name" );
        
        try {
            loadIndexedFiles();
        } catch( Exception e ) {
            System.err.println( "Could not load indexed files!" );
        }
    }
    

    /**
     * Also clear existing list of indexed files
     */
    public final void clear() throws IOException {
        super.clear();
        this.indexedFiles = Collections.synchronizedSortedMap( new TreeMap<String,Long>() );
        this.indexedRootDirectories = Collections.synchronizedSet( new HashSet<String>() );
        this.wasChanged = true;
        saveIndexedFiles();
    }
    
    /**
     * If indexing is stopped the indexed files need to be saved
     */
    public void stopIndexing() {
        super.stopIndexing();
        try {
            saveIndexedFiles();
        } catch( IOException e ) {
            throw new RuntimeException( "Could not save indexed files! The error message was:\n" + e.getMessage() );
        }
    }

    
    public final int getNumberOfIndexedFiles() {
        return this.indexedFiles.size();
    }
    
    public final Map<String,Integer> getNumberOfIndexedFilesPerType() {
        final Map<String,Integer> result = new HashMap<String,Integer>();
        
        // iterate over all files in the index and count number for each file type
        for( String filename : this.indexedFiles.keySet() ) {
            final String ext = FileUtils.getExtension( filename ).toLowerCase();
            int count = 1;
            
            if( result.containsKey( ext ) ) {
                count = result.get( ext ) + 1;
            }
            result.put( ext, count );
        }
        
        return result;
    }
    
    public final Long getDocumentFileDate( final String file ) {
        return this.indexedFiles.get( file );
    }
    
    
    public final Set<String> getRootDirectories() {
        return this.indexedRootDirectories;
    }
    
    public final void addRootDirectory( final String filename ) {
        // add the directory
        this.indexedRootDirectories.add( filename );
        this.wasChanged = true;
        // save new list of indexed files
        try {
            saveIndexedFiles();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public final Set<String> listFiles( String directory ) {
        final Set<String> result = new HashSet<String>();
        
        if( ! directory.endsWith( File.separator ) ) directory += File.separator;
        
        for( String filename : this.indexedFiles.tailMap( directory ).keySet() ) {
            if( filename.startsWith( directory ) ) {
                // only add files in the given directory but not those in subdirectories
                if( filename.substring( directory.length() ).indexOf( File.separator ) < 0 ) {
                    result.add( filename );
                }
            } else {
                // we can exit when the first files from another directory is found as we have an ordered set
                break;
            }
        }
        
        return result;
    }

    
    /**
     * Save indexed files after updating the index
     */
    protected void update( final File directory ) throws IOException {
        super.update( directory );
        saveIndexedFiles();
    }
    
    protected Query getSearchQuery( final String[] fields, final String searchText, final String searchPath ) throws ParseException {
        // construct query
        //final BooleanQuery query = new BooleanQuery();
        final Analyzer analyzer = new StandardAnalyzer();
        String[] s = new String[fields.length];
        for (int i=0;i<fields.length;i++)s[i] = searchText;
        
        final Query textQuery = MultiFieldQueryParser.parse( s, fields, analyzer );
        // TODO: figure out how to put back the boolean operator or just remove it if it is not needed.
        //QueryParser parser = new QueryParser("contents", analyzer);
        //query.add( textQuery, BooleanClause.Occur.MUST );
        //Query query = parser.parse(searchText);
        // add search path to search text if necessary
        if( null != searchPath ) {
            //query.add( new QueryParser( "path", analyzer ).parse( "\"" + searchPath + "*\")" ), BooleanClause.Occur.MUST );
        }
        
        return textQuery;
    }
    
    protected Collection<Document> getSearchResults( final Query query, final String searchPath, final int maxResults ) throws IOException {
        final Collection<Document> searchResults = new LinkedList<Document>();
        
        // add hits to result string
        TopDocCollector collector = new TopDocCollector(maxResults);

        final Searcher indexSearcher = new IndexSearcher( this.indexName );
        indexSearcher.search( query, collector );
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        for( int i = 0 ; i < hits.length && ( 0 == maxResults || i < maxResults ) ; ++i ) {
        	int docId = hits[i].doc;
            final Document doc = indexSearcher.doc(docId);
            // this is the latest version of the document?
            try {
            	//TODO: figure out why the date never match, comment out for now.
                //if( DateTools.stringToTime( doc.getField( "modified" ).stringValue() ) == getDocumentFileDate( path ) && ( null == searchPath || path.startsWith( searchPath ) ) ) {
                    searchResults.add( doc );
                //}
            } catch( Exception e ) {
                // document has no current modification date, ignore this
            	System.err.println(e.getMessage());
            }
        }

        indexSearcher.close();
        
        return searchResults;
    }
    
    protected AbstractIndexWriter getIndexWriter( final boolean create ) throws IOException {
        return new LuceneIndexWriter( this.indexName, create );
    }
    
    
    protected void addDocument( String filename, Long lastModified ) throws IOException {
        super.addDocument( filename, lastModified );
        
        this.indexedFiles.put( filename, lastModified );
        this.wasChanged = true;
    }
    
    protected void removeDocument( String filename ) throws IOException {
        this.indexedFiles.remove( filename );
        this.wasChanged = true;
    }

    
    /**
     * Documents are not removed from a Lucene index. Therefore the most recent version needs to be checked.
     */
    private boolean isDocumentUpToDate( final Document doc )  throws Exception{
        return DateTools.stringToTime( doc.getField( "modified" ).stringValue() ) == getDocumentFileDate( doc.get( "path" ) );
    }
    
    /**
     * Load list of indexed files.
     * This list stores the date of last modification for each indexed file and is used to avoid new indexing of files already indexed.
     */
    private synchronized void loadIndexedFiles() throws IOException, ClassNotFoundException {
        // load file list
        final ObjectInputStream istream = new ObjectInputStream( new BufferedInputStream( new FileInputStream( new File( this.indexedFilesName ) ) ) );

        this.indexedFiles = Collections.synchronizedSortedMap( new TreeMap<String,Long>( (Map<String,Long>)istream.readObject() ) );
        this.indexedRootDirectories = (Set<String>)istream.readObject();
        istream.close();
        // no changes have been done yet
        this.wasChanged = false;
    }
    
    /**
     * Save list of indexed files
     *
     * @see de.joergjahnke.jdesktopsearch.abstractionlayer.LuceneIndexManager#loadIndexedFiles
     */
    protected synchronized void saveIndexedFiles() throws IOException {
        if( this.wasChanged ) {
            // save changes
            final ObjectOutputStream ostream = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( new File( this.indexedFilesName ) ) ) );

            ostream.writeObject( this.indexedFiles );
            ostream.writeObject( this.indexedRootDirectories );
            ostream.close();
            // all changes were saved
            this.wasChanged = false;
        }
    }
}
