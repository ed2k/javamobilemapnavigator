/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.abstractionlayer;


import de.joergjahnke.common.io.FileUtils;
import de.joergjahnke.common.lang.ParallelExecutor;
import de.joergjahnke.jdesktopsearch.IndexStatusMessage;
import de.joergjahnke.jdesktopsearch.SearchResult;
import de.joergjahnke.jdesktopsearch.parser.DocumentParser;
import de.joergjahnke.jdesktopsearch.parser.HTMLDocumentParser;
import de.joergjahnke.jdesktopsearch.parser.MSWordDocumentParser;
import de.joergjahnke.jdesktopsearch.parser.OpenOfficeDocumentParser;
import de.joergjahnke.jdesktopsearch.parser.PlainTextDocumentParser;
import de.joergjahnke.jdesktopsearch.parser.RTFDocumentParser;
import de.joergjahnke.jdesktopsearch.parser.XMLDocumentParser;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import de.joergjahnke.jdesktopsearch.parser.LucenePDFDocument;
//import org.pdfbox.searchengine.lucene.LucenePDFDocument;


/**
 * Class to manage the index.
 * Offers methods to create a new index, add files/directories to the index,
 * update the index and search the index.
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public abstract class AbstractIndexManager extends Observable {
    /**
     * debugging is switched on?
     */
    public final static boolean DEBUG = false;
    /**
     * notification of the Observable that will trigger the dialog to be made visible
     */
    public final static String EVENT_SHOW = "show";
    /**
     * notification of the Observable that will trigger the dialog to be hidden
     */
    public final static String EVENT_HIDE = "hide";
    
    
    // determine number of CPUs to have an optimal number of threads for indexing
    private static int threadCount = 1;
    static {
        try {
            threadCount = Integer.parseInt( System.getenv( "NUMBER_OF_PROCESSORS" ) );
            System.out.print( "Detected " + System.getenv( "NUMBER_OF_PROCESSORS" ) + " CPU cores, " );
        } catch( Exception e ) {}
        System.out.println( "using " + threadCount + " threads when indexing." );
    }
    // document parsers
    private final static DocumentParser[] parsers = {
        new RTFDocumentParser(), new HTMLDocumentParser(), new XMLDocumentParser(), new MSWordDocumentParser(), new OpenOfficeDocumentParser()
    };
    // maps file extensions to document parsers
    private final static Map<String,DocumentParser> parserMap = new Hashtable<String,DocumentParser>();
    static {
        for( final DocumentParser parser : parsers ) {
            final List<String> supportedFileTypes = parser.getSupportedFileTypes();
            
            for( final String fileType : supportedFileTypes ) {
                parserMap.put( fileType, parser );
            }
        }
    }
    
    
    // supported file extensions and their analyzer
    private Map<String,String> fileTypes = new HashMap<String,String>();
    // file extensions where only the general properties are indexed
    private Set<String> propertiesFileTypes = new HashSet<String>(); 
    // properties of this AbstractIndexManager
    private final Properties properties;
    // currently used writer
    private AbstractIndexWriter writer = null;
    /**
     * index name
     */
    protected final String indexName;
    /**
     * index hidden files
     */
    protected final boolean doIndexHidden;
    /**
     * patterns of files to exclude
     */
    protected final String[] exclusions;
    /**
     * indexing is currently running?
     */
    protected volatile boolean isIndexing = false;
    /**
     * queue for the files to index
     */
    private volatile BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>( threadCount * 2 + 10 );
    
    
    /**
     * Creates a new instance of AbstractIndexManager 
     */
    public AbstractIndexManager( final Properties properties ) {
        this.properties = properties;
        
        // extract file names
        this.indexName = this.properties.getProperty( "index_name" );
        
        // read supported file types
        StringTokenizer tokenizer = new StringTokenizer( properties.getProperty( "file_types" ), "," );
        
        while( tokenizer.hasMoreTokens() ) {
            final String type = tokenizer.nextToken();
            
            this.fileTypes.put( type, properties.getProperty( type ) );
        }
        
        // read file types where the file properties are indexed
        tokenizer = new StringTokenizer( properties.getProperty( "properties_only_file_types" ), "," );
        
        while( tokenizer.hasMoreTokens() ) {
            final String type = tokenizer.nextToken();
            
            this.propertiesFileTypes.add( type );
        }
        
        // index hidden files & folder?
        this.doIndexHidden = Boolean.toString( true ).equals( properties.getProperty( "do_index_hidden" ) );
        // get file exclusion list
        this.exclusions = null == properties.getProperty( "excluded_files" ) ? new String[ 0 ] : properties.getProperty( "excluded_files" ).split( "\\," );
        
        // installa shutdown hook which stops any running indexing operations
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                public void run() {
                    try {
                        writer.close();
                    } catch( Exception e ) {
                        // we couldn't close the writer, but there's nothing else we can do now
                    }
                }
            }
        );
    }

    
    /**
     * Check whether an index exists
     *
     * @return  true if an index exists, otherwise false
     */
    public boolean exists() {
        try {
            return !getRootDirectories().isEmpty();
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check whether we are currently indexing files
     *
     * @return  true if indexing is being done, otherwise false
     */
    public boolean isIndexing() {
        return this.isIndexing;
    }
    
    /**
     * Get index properties as printable string
     *
     * @return string with total number of indexed files and number of files per type
     */
    public String getProperties() {
        final int FILETYPES_PER_ROW = 6;
        
        // show number of indexed files
        final StringBuffer propertiesText = new StringBuffer( "No. of indexed files: " );
        
        try {
            propertiesText.append( getNumberOfIndexedFiles() );
        } catch( IOException e ) {}
        propertiesText.append( "\n\n" );
        
        // show number of indexed files per file type
        propertiesText.append( "No. of indexed files per type:\n" );
        
        try {
            final Map<String,Integer> fileCounts = new TreeMap<String,Integer>( getNumberOfIndexedFilesPerType() );
            int rowcount = 0;

            for( String filetype : fileCounts.keySet() ) {
                propertiesText.append( " " );
                propertiesText.append( filetype );
                propertiesText.append( ": " );
                propertiesText.append( fileCounts.get( filetype ) );
                ++rowcount;
                if( 0 == rowcount % FILETYPES_PER_ROW ) {
                    propertiesText.append( "\n" );
                } else {
                    propertiesText.append( "   " );
                }
            }
            if( 0 != rowcount % FILETYPES_PER_ROW ) propertiesText.append( "\n" );
            propertiesText.append( "\n" );
        } catch( IOException e ) {}
        
        // show indexed directories
        propertiesText.append( "Indexed directories:\n" );
        
        final Collection<String> directoryNames = new TreeSet<String>( getRootDirectories() );
        
        for( String directoryName : directoryNames ) {
            propertiesText.append( directoryName );
            propertiesText.append( "\n" );
        }
        
        return propertiesText.toString();
    }
    

    /**
     * Create a new index
     */
    public void clear() throws IOException {
        // create new index database
        this.writer = getIndexWriter( true );
        this.writer.close();
        this.writer = null;
        
        setChanged();
        notifyObservers( new IndexStatusMessage( "New index created" ) );
    }
    
    /**
     * Update all indexed directories
     */
    public void updateAll() throws IOException, ClassNotFoundException {
        // notify observers about the start of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( IndexStatusMessage.EVENT_LONG_OPERATION_STARTED ) );
        
        // iterate over all root directories
        for( String rootDir : getRootDirectories() ) {
            // update index
            update( new File( rootDir ) );
        }
        
        // notify observers about the end of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( "Index was updated successfully", 0, IndexStatusMessage.EVENT_LONG_OPERATION_ENDED ) );
    }
    
    /**
     * Add new directory to the existing index
     *
     * @param   directory   directory to index (recursively)
     */
    public void add( final File directory ) throws IOException, ClassNotFoundException {
        // notify observers about the start of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( IndexStatusMessage.EVENT_LONG_OPERATION_STARTED ) );
        
        // add new root directory
        addRootDirectory( directory.getAbsolutePath() );
         // update index
        update( directory );
        
        // notify observers about the end of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( "The directory '" + directory + "' was added to the index", 0, IndexStatusMessage.EVENT_LONG_OPERATION_ENDED ) );
    }
    
    /**
     * Optimize the index for better performance
     */
    public void optimize() throws IOException {
        // inform observers about optimization of the index
        setChanged();
        notifyObservers( new IndexStatusMessage( "Optimizing index", 0, IndexStatusMessage.EVENT_LONG_OPERATION_STARTED ) );
        
        // do optimization
        this.writer = getIndexWriter( false );
        try { this.writer.optimize(); } catch( IOException e ) {}
        this.writer.close();
        this.writer = null;
        
        // notify observers about the end of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( "Index optimized", 0, IndexStatusMessage.EVENT_LONG_OPERATION_ENDED ) );
    }

    /**
     * Unlock a write lock existing for the index
     *
     * @return  true if a lock was removed, false if no lock existed
     */
    public boolean unlock() throws IOException {
        boolean result = false;
        
        try {
            final AbstractIndexWriter writer = getIndexWriter( false );
            
            writer.close();
        } catch( IOException e ) {
            // we obtained a write lock?
        	System.out.println(e.getMessage());
            if( e.getMessage().indexOf( "Lock@" ) >= 0 && e.getMessage().endsWith( "write.lock" ) ) {
                final String lockFileName = e.getMessage().substring( e.getMessage().indexOf( "Lock@" ) + 5 );
                
                new File( lockFileName ).delete();
            
                result = true;
            }
        }
        
        setChanged();
        notifyObservers( new IndexStatusMessage( result ? "Existing locks on the index were removed" : "No locks to remove" ) );
        
        return result;
    }
    
    /**
     * Search for a given search-string
     *
     * @param   searchText  text to search
     * @param   searchPath  path to search in, set to null to search in all paths
     * @param   maxRows maximum number of documents to retrieve, 0 for an unlimited search
     * @return  map containing a collection of documents (accessed via the key "Documents") and a Query object (access via the key "Query")
     */
    public SearchResult search( final String searchText, final String searchPath, final int maxRows, final Set<String> searchFields ) throws IOException {
        // search in these fields
        final String[] fields = new String[ searchFields.size() ];
        
        searchFields.toArray( fields );
        
        try {
            // create query for the above fields
            final Query query = getSearchQuery( fields, searchText, searchPath );
            // contains found documents
            final Collection<Document> documents = getSearchResults( query, searchPath, maxRows );
            
            return new SearchResult( query, documents );
        } catch( ParseException e ) {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }
    
    /**
     * Stop indexing
     */
    public void stopIndexing() {
        // notify all indexing threads that indexing should now be stopped
        this.isIndexing = false;
        
        // notify observers about the end of the indexing process
        setChanged();
        notifyObservers( new IndexStatusMessage( "Index interrupted", 0, IndexStatusMessage.EVENT_LONG_OPERATION_ENDED ) );
    }
    
    
    /**
     * Update the existing index
     *
     * @param   directory   directory to index (recursively)
     */
    protected synchronized void update( final File directory ) throws IOException {
        assert( !this.isIndexing );
        
        try {
            // process these files and write them to the index
            this.writer = getIndexWriter( false );

            this.isIndexing = true;

            // start some threads which extract searchable data from the indexable files
            final Runnable indexer = new Runnable() {
                public void run() {
                    while( isIndexing || !fileQueue.isEmpty()) {
                        final String path = fileQueue.poll();
                        if( null == path ) {
                            try { Thread.sleep( 100 ); 
                            } catch( InterruptedException e ) {}
                        } else {

                        	final File file = new File( path );

                            try {
                                // get document data
                                Document doc = createDocument( file );
                                writer.addDocument( doc );
                                addDocument( path, file.lastModified() );

                            } catch( IOException e ) {
                                // file could not be added to the index
                                System.err.println( "Could not add document '" + file + "' to index! The error message was: " + e.getMessage() );
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("out of while");
                }
            };
            
            final ParallelExecutor executor = new ParallelExecutor( indexer, AbstractIndexManager.threadCount );
            final Thread indexersThread = new Thread( executor );

            indexersThread.setPriority( Thread.MIN_PRIORITY );
            indexersThread.start();
            // collect all indexable files
            doIndexing( writer, directory );
            // after file list push into fileQue then do real file parsing, so that queue is setup right
            this.isIndexing = false;
            // TODO, need to find a way to kill hanging thread (ex. when PDF is too big)
            // wait for indexers to finish
            indexersThread.join();
            this.writer.close();
            this.writer = null;
        } catch( Throwable t ) {
            // notify any observers about the problem
            setChanged();
            notifyObservers( new IndexStatusMessage( t.getMessage() ) );

            this.isIndexing = false;
            
            // close the writer
            if( null != this.writer ) {
                this.writer.close();
                this.writer = null;
            }
            
            // propagate IOExceptions
            if( t instanceof IOException ) throw (IOException)t;
        }
    }

    /**
     * Check whether the given file is of an indexable file type
     *
     * @param   path    full filename of the file to check
     * @return  true if the file is of a file type to be indexed, false otherwise
     */
    private boolean isIndexableFileType( final String path ) {
        final String ext = FileUtils.getExtension( path );
        return this.fileTypes.containsKey( ext ) || this.propertiesFileTypes.contains( ext );
    }
                
    /**
     * Write index information to a given AbstractIndexWriter
     * 
     * @param   writer  AbstractIndexWriter to update
     * @param   file    file or directory to index. Subdirectories are also indexed
     */
    private void doIndexing( final AbstractIndexWriter writer, final File file ) throws IOException {
        if( !this.isIndexing ) return;
        
        // do not try to index files that cannot be read or that must not be indexed
        final String path = file.getAbsolutePath();
        boolean isExcluded = false;
        
        for( String exclusion : this.exclusions ) {
            if( path.matches( exclusion ) ) {
                isExcluded = true;
                break;
            }
        }
                
        if( ( ! file.isHidden() || this.doIndexHidden ) && ! isExcluded && file.canRead() ) {
            if( file.isDirectory() ) {
                // get all files inside the directory
                final String[] filesArray = file.list();

                // an IO error could occur
                if( filesArray != null ) {
                    if( !this.isIndexing ) return;
                    
                    final Set<String> files = new HashSet<String>( Arrays.asList( filesArray ) );
                
                    // remove all files in current index which do no longer exist (but not root directories)
                    if( ! getRootDirectories().contains( path ) ) {
                        // get all files from the current path that are stored in the index ...
                        final Set<String> existingFiles = listFiles( path );
                        
                        // ... remove the files now found in that path ...
                        existingFiles.removeAll( files );
                        
                        // ... and delete those files that are left
                        try {
                            for( String filename : existingFiles ) {
                                if( DEBUG ) 
                                	System.err.println( this.getClass().getName() + ": Removing file " + new File( file, filename ).getAbsolutePath() );
                                writer.deleteDocument( new File( file, filename ) );
                            }
                        } catch( ConcurrentModificationException e ) {
                            // TODO: ideally removing deleted files should be started again
                        }
                    }
                    
                    // collect all files and sub-directories
                    for( String filename : files ) {
                        doIndexing( writer, new File( file, filename ) );
                    }
                }
            } else {
            	//TODO, should be able to index all files with properties so no need to check isIndexableFileType
            	// comment out for now, to remove it later.
                // add the file to the result list, if it is a supported type
                //if( isIndexableFileType( path ) ) {
                    // inform observers about the current file
                    setChanged();
                    notifyObservers( new IndexStatusMessage( path ) );
                    
                    // check if this file has already been indexed
                    final Long lastModified = getDocumentFileDate( path );

                    if( ! this.isIndexing ) return;

                    // only update index if last modification date differs from that one already in the index
                    if( null == lastModified || lastModified.longValue() != file.lastModified() ) {
                        // first delete old content
                        if( null != lastModified ) {
                            writer.deleteDocument( file );
                            removeDocument( path );
                        }
                        // document data can be extracted?
                        final String ext = FileUtils.getExtension( path );

                        if( this.fileTypes.containsKey( ext ) ) {
                            // then add file to queue of indexable files
                            try {
                                this.fileQueue.put( path );
                            } catch( InterruptedException e ) {
                                // we were interrupted and stop waiting for the queue
                            	if(DEBUG)System.err.println("In which condition are we interrupted? "+e.getMessage());
                            }
                        } else {
                            // otherwise only record the document properties
                            final Document doc = createPropertiesDocument( file );
                            writer.addDocument( doc );
                            addDocument( path, file.lastModified() );
                        }
                    }

                    // inform about file having been processed
                    setChanged();
                    notifyObservers( new IndexStatusMessage( null, 1 ) );
                //}
            }
        }
    }

    String transform(String s){
    	byte[] bytes = s.getBytes();

    	for (int i=0;i<bytes.length;i++){
    		byte b = bytes[i];
    		if (b<128 && b>122) bytes[i] = 32;
    		else if ( b<48) bytes[i] = 32;
    		else if(b >57 && b<65) bytes[i] = 32;
    		else if(b>90 && b<97) bytes[i]=32;
    	}
    	String n = new String(bytes);
    	return n;
    }    
    /**
     * Create indexable document object from a file which only contains the general file properties
     *
     * @param   file    file to index
     * @return  document with properties "path" and "modified" set
     */
    private Document createPropertiesDocument( final File file ) {
        // make a new, empty document
        final Document doc = new Document();
        
        // Add the path of the file as a field named "path".  Use a Text field, so
        // that the index stores the path, and so that the path is searchable
        doc.add( new Field( "path", file.getPath(), Field.Store.YES, Field.Index.ANALYZED ) );

        // Add the last modified date of the file a field named "modified".  Use a
        // Keyword field, so that it's searchable, but so that no attempt is made
        // to tokenize the field into words.
        doc.add( new Field( "modified", 
        		DateTools.timeToString( file.lastModified(),DateTools.Resolution.HOUR ), 
        		Field.Store.YES, Field.Index.NOT_ANALYZED ) );
        doc.add(new Field("keywords", transform(file.getAbsolutePath()), Field.Store.YES, Field.Index.ANALYZED));
        return doc;
    }

    /**
     * Create indexable document object from a file
     *
     * @param   file    file to index
     * @return  document with properties "path", "modified" and "contents" set
     */
    private Document createDocument( final File file ) {
        // create document that already contains the file properties
        Document doc = createPropertiesDocument( file );
        // put a limit on the file we indexed to avoid out of memory
        // TODO, make the limit configurable
        if (file.length() > 5000000) return doc;
        final String extension = FileUtils.getExtension( file.getPath() ).toLowerCase();

        try {

            // index documents that are in the standard parser table
            if( AbstractIndexManager.parserMap.containsKey( extension ) ) {
                // read file into buffer
                final DocumentParser parser = AbstractIndexManager.parserMap.get( extension ).newInstance();

                parser.parse( file );

                // Add the tag-stripped contents as a Reader-valued Text field so it will
                // get tokenized and indexed.
                doc.add( new Field( "contents", parser.getContent(), Field.Store.YES, Field.Index.ANALYZED ) );

                // add title, author, description and keywords if they exist
                if( null != parser.getTitle() ) {
                    doc.add( new Field( "title", parser.getTitle(), Field.Store.YES, Field.Index.ANALYZED ) );
                }
                if( null != parser.getAuthor() ) {
                    doc.add( new Field( "author", parser.getAuthor(), Field.Store.YES, Field.Index.ANALYZED ) );
                }
                if( null != parser.getDescription() ) {
                    doc.add( new Field( "description", parser.getDescription(), Field.Store.YES, Field.Index.ANALYZED ) );
                }
                if( null != parser.getKeywords() ) {
                    for( String keyword : parser.getKeywords() ) {
                        doc.add( new Field( "keywords", keyword, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
                    }
                }
            // index PDF documents
            } else if( ".pdf".equals( extension ) ) {
                doc = LucenePDFDocument.getDocument( file );
                //TODO doc object is re-created, so we need to add the file properties
                doc.add(new Field("keywords", transform(file.getAbsolutePath()), Field.Store.YES, Field.Index.ANALYZED));
            // index plain text files
            } else {
                final DocumentParser parser = new PlainTextDocumentParser();
                parser.parse( file );
                doc.add( new Field( "contents", parser.getContent(), Field.Store.YES, Field.Index.ANALYZED ) );
            }
        } catch( Exception e ) {
            // data could not be extracted
            System.err.println( "Could not extract data from document '" + file + "'! Error: " + e.getMessage() );
            doc.add(new Field("keywords", "parseError "+e.getMessage(), Field.Store.YES, Field.Index.ANALYZED));
            if( DEBUG ) e.printStackTrace();
        }     

        return doc;
    }
    
    static long usedMemory ()
    {
        return s_runtime.totalMemory () - s_runtime.freeMemory ();
    }
    
    private static final Runtime s_runtime = Runtime.getRuntime ();   
    // abstract methods to be implemented by subclasses
    
    /**
     * List indexed files in a given directory (non recursively)
     *
     * @param   path    path of directory to list
     * @return  set of indexed files in the given directory
     */
    public abstract Set<String> listFiles( String path );
    
    /**
     * Get the date when a file in the index was last modified
     *
     * @param   filename    file to check
     * @return  null if the file is not in the index, otherwise its last modification date
     */
    public abstract Long getDocumentFileDate( final String filename );
    
    /**
     * Get names of root directories for the index
     *
     * @return  set of directory names
     */
    public abstract Set<String> getRootDirectories();
    
    /**
     * Add a new root directory to the index
     *
     * @param   filename    absolute root directory name
     */
    public abstract void addRootDirectory( String filename ) throws IOException;

    
    /**
     * Get number of indexed files
     */
    protected abstract int getNumberOfIndexedFiles() throws IOException;
    
    /**
     * Get the number of indexed files per file type
     *
     * @return  map containing all file types and their corresponding count of files
     */
    protected abstract Map<String,Integer> getNumberOfIndexedFilesPerType() throws IOException;
    
    
    /**
     * Create a query object from the search-text and the given fields
     *
     * @param   fields  fields to search
     * @param   searchText  text containing search conditions
     * @param   searchPath  additional path for restrict results
     * @return  query object representing the given parameters
     */
    protected abstract Query getSearchQuery( final String[] fields, final String searchText, final String searchPath ) throws ParseException;

    /**
     * Retrieve search results
     *
     * @param   query   Lucene query with search parameters
     * @param   searchPath  path to search in, set to null to search in all paths
     * @param   maxRows maximum number of results
     * @return  documents matching the search
     */
    protected abstract Collection<Document> getSearchResults( final Query query, final String searchPath, final int maxRows ) throws IOException;
    
    /**
     * Get AbstractIndexWriter to write documents to the index
     * 
     * @param   create  true to create a new index, false if the old index should be used
     * @return  AbstractIndexWriter, must be closed after usage
     */
    protected abstract AbstractIndexWriter getIndexWriter( final boolean create ) throws IOException;

    

    /**
     * Sub-classes may overwrite this method to do special processing when a document is added
     */
    protected void addDocument( String filename, Long lastModified ) throws IOException {
    }
    
    /**
     * Sub-classes may overwrite this method to do special processing when a document is removed
     */
    protected void removeDocument( String filename ) throws IOException {
    }
}

