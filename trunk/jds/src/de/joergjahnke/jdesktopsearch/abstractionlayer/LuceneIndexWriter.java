/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.abstractionlayer;


import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;


/**
 * Implementation of the IndexWriter interface for Lucene indexes
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class LuceneIndexWriter implements AbstractIndexWriter {
    private final IndexWriter indexWriter;
    private final IndexReader indexReader;
    
    
    /**
     * Creates a new instance of LuceneIndexWriter
     */
    public LuceneIndexWriter( final String name, final boolean create ) throws IOException {
        this.indexWriter = new IndexWriter( name, new StandardAnalyzer(), create, IndexWriter.MaxFieldLength.LIMITED );
        this.indexReader = IndexReader.open( name );
        System.out.println(indexWriter.getRAMBufferSizeMB());
    }
    
    
    public final void addDocument( final Document doc ) throws IOException {
        this.indexWriter.addDocument( doc );
    }
    
    public void deleteDocument( final File file ) throws IOException {
        this.indexReader.deleteDocuments( new Term( "path", file.getAbsolutePath() ) );
    }
    
    public final void close() throws IOException {
        this.indexReader.close();
        this.indexWriter.close();
    }
    
    public final void optimize() throws IOException {
        this.indexWriter.optimize();
    }
}
