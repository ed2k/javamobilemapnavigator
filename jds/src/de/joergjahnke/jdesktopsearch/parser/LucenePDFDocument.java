package de.joergjahnke.jdesktopsearch.parser;

/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
//package org.pdfbox.searchengine.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

import java.net.URL;
import java.net.URLConnection;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.util.PDFTextStripper;

/**
 * This class is used to create a document for the lucene search engine.
 * This should easily plug into the IndexHTML or IndexFiles that comes with
 * the lucene project.  This class will populate the following fields.
 * <table>
 *      <tr>
 *          <th>Lucene Field Name</th>
 *          <th>Description</th>
 *      </tr>
 *      <tr>
 *          <td>path</td>
 *          <td>File system path if loaded from a file</td>
 *      </tr>
 *      <tr>
 *          <td>url</td>
 *          <td>URL to PDF document</td>
 *      </tr>
 *      <tr>
 *          <td>contents</td>
 *          <td>Entire contents of PDF document, indexed but not stored</td>
 *      </tr>
 *      <tr>
 *          <td>summary</td>
 *          <td>First 500 characters of content</td>
 *      </tr>
 *      <tr>
 *          <td>modified</td>
 *          <td>The modified date/time according to the url or path</td>
 *      </tr>
 *      <tr>
 *          <td>uid</td>
 *          <td>A unique identifier for the Lucene document.</td>
 *      </tr>
 *      <tr>
 *          <td>CreationDate</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>Creator</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>Keywords</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>ModificationDate</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>Producer</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>Subject</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 *      <tr>
 *          <td>Trapped</td>
 *          <td>From PDF meta-data if available</td>
 *      </tr>
 * </table>
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.22 $
 */


public class LucenePDFDocument
{
    private static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

    // given caveat of increased search times when using
    //MICROSECOND, only use SECOND by default
    private DateTools.Resolution dateTimeResolution = DateTools.Resolution.SECOND;
    
    private PDFTextStripper stripper = null;
    
    /**
     * Constructor.
     */
    public LucenePDFDocument()
    {
    }
    
    /**
     * Set the text stripper that will be used during extraction.
     * 
     * @param aStripper The new pdf text stripper.
     */
    public void setTextStripper( PDFTextStripper aStripper )
    {
        stripper = aStripper;
    }
    
    /**
     * Get the Lucene data time resolution.
     * 
     * @return current date/time resolution
     */
    public DateTools.Resolution getDateTimeResolution()
    {
        return dateTimeResolution;
    }
    
    /**
     * Set the Lucene data time resolution.
     * 
     * @param resolution set new date/time resolution
     */
    public void setDateTimeResolution( DateTools.Resolution resolution )
    {
        dateTimeResolution = resolution;
    }
    
    //
    // compatibility methods for lucene-1.9+
    //
    private String timeToString( long time )
    {
        return DateTools.timeToString( time, dateTimeResolution );
    }
    
    private void addKeywordField( Document document, String name, String value )
    {
        if ( value != null ) 
        {
            document.add( new Field( name, value, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
        }
    }
    
    private void addTextField( Document document, String name, Reader value )
    {
        if ( value != null ) 
        {
            document.add( new Field( name, value ) );
        }
    }
    
    private void addTextField( Document document, String name, String value )
    {
        if ( value != null ) 
        {
            document.add( new Field( name, value, Field.Store.YES, Field.Index.ANALYZED ) );
        }
    }
    
    private void addTextField( Document document, String name, Date value )
    {
        if ( value != null ) 
        {
            addTextField( document, name, DateTools.dateToString( value, dateTimeResolution ) );
        }
    }
    
    private void addTextField( Document document, String name, Calendar value )
    {
        if ( value != null ) 
        {
            addTextField( document, name, value.getTime() );
        }
    }
    
    private static void addUnindexedField( Document document, String name, String value )
    {
        if ( value != null ) 
        {
            document.add( new Field( name, value, Field.Store.YES, Field.Index.NO ) );
        }
    }
    
    private void addUnstoredKeywordField( Document document, String name, String value )
    {
        if ( value != null ) 
        {
            document.add( new Field( name, value, Field.Store.NO, Field.Index.NOT_ANALYZED ) );
        }
    }
    
    /**
     * Convert the PDF stream to a lucene document.
     * 
     * @param is The input stream.
     * @return The input stream converted to a lucene document.
     * @throws IOException If there is an error converting the PDF.
     */
    public Document convertDocument( InputStream is ) throws IOException
    {
        Document document = new Document();
        addContent( document, is, "<inputstream>" );
        return document;
        
    }
    
    /**
     * This will take a reference to a PDF document and create a lucene document.
     * 
     * @param file A reference to a PDF document.
     * @return The converted lucene document.
     * 
     * @throws IOException If there is an exception while converting the document.
     */
    public Document convertDocument( File file ) throws IOException
    {
        Document document = new Document();

        // Add the url as a field named "url".  Use an UnIndexed field, so
        // that the url is just stored with the document, but is not searchable.
        addUnindexedField( document, "path", file.getPath() );
        addUnindexedField( document, "url", file.getPath().replace(FILE_SEPARATOR, '/') );

        // Add the last modified date of the file a field named "modified".  Use a
        // Keyword field, so that it's searchable, but so that no attempt is made
        // to tokenize the field into words.
        addKeywordField( document, "modified", timeToString( file.lastModified() ) );

        String uid = file.getPath().replace(FILE_SEPARATOR,'\u0000')
                     + "\u0000" 
                     + timeToString( file.lastModified() );

        // Add the uid as a field, so that index can be incrementally maintained.
        // This field is not stored with document, it is indexed, but it is not
        // tokenized prior to indexing.
        addUnstoredKeywordField( document, "uid", uid );

        FileInputStream input = null;
        try
        {   
        	if(file.length()<99999999){
            input = new FileInputStream( file );
            addContent( document, input, file.getPath() );
        	} else {
        		// experiment, to avoid file is too big out of memory problem.
                input = new FileInputStream( file );
        		addContent2( document, input, file.getPath() );
        	}
        }
        finally
        {
            if( input != null )
            {
                input.close();
            }
        }


        // return the document

        return document;
    }
    
    /**
     * Convert the document from a PDF to a lucene document.
     * 
     * @param url A url to a PDF document.
     * @return The PDF converted to a lucene document.
     * @throws IOException If there is an error while converting the document.
     */
    public Document convertDocument( URL url ) throws IOException
    {
        Document document = new Document();
        URLConnection connection = url.openConnection();
        connection.connect();
        // Add the url as a field named "url".  Use an UnIndexed field, so
        // that the url is just stored with the document, but is not searchable.
        addUnindexedField( document, "url", url.toExternalForm() );

        // Add the last modified date of the file a field named "modified".  Use a
        // Keyword field, so that it's searchable, but so that no attempt is made
        // to tokenize the field into words.
        addKeywordField( document, "modified", timeToString(connection.getLastModified() ) );

        String uid = url.toExternalForm().replace(FILE_SEPARATOR, '\u0000')
                     + "\u0000" 
                     + timeToString( connection.getLastModified() );

        // Add the uid as a field, so that index can be incrementally maintained.
        // This field is not stored with document, it is indexed, but it is not
        // tokenized prior to indexing.
        addUnstoredKeywordField( document, "uid", uid );

        InputStream input = null;
        try
        {
            input = connection.getInputStream();
            addContent( document, input,url.toExternalForm() );
        }
        finally
        {
            if( input != null )
            {
                input.close();
            }
        }

        // return the document
        return document;
    }
    
    /**
     * This will get a lucene document from a PDF file.
     *
     * @param is The stream to read the PDF from.
     *
     * @return The lucene document.
     *
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument( InputStream is ) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument( is );
    }

    /**
     * This will get a lucene document from a PDF file.
     *
     * @param file The file to get the document for.
     *
     * @return The lucene document.
     *
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument( File file ) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument( file );
    }

    /**
     * This will get a lucene document from a PDF file.
     *
     * @param url The file to get the document for.
     *
     * @return The lucene document.
     *
     * @throws IOException If there is an error parsing or indexing the document.
     */
    public static Document getDocument( URL url ) throws IOException
    {
        LucenePDFDocument converter = new LucenePDFDocument();
        return converter.convertDocument( url );
    }
    private void addContent2( Document document, InputStream is, String documentLocation ) throws IOException
    {
        PDDocument pdfDocument = null;
        try
        {
            pdfDocument = PDDocument.load( is );

            if( pdfDocument.isEncrypted() )
            {
                //Just try using the default password and move on
                pdfDocument.decrypt( "" );
            }

            //create a writer where to append the text content.
            //StringWriter writer = new StringWriter();
            if( stripper == null )
            {
                stripper = new PDFTextStripper();
            }
            else
            {
                stripper.resetEngine();
            }
            // stripper.writeText( pdfDocument, writer );
            

            //String contents = writer.getBuffer().toString();

            //StringReader reader = new StringReader( contents );

            // Add the tag-stripped contents as a Reader-valued Text field so it will
            // get tokenized and indexed.
            addTextField( document, "contents", "reader" );
            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            if( info != null ) 
            {
                addTextField( document, "Author", info.getAuthor() );
                addTextField( document, "CreationDate", info.getCreationDate() );
                addTextField( document, "Creator", info.getCreator() );
                addTextField( document, "Keywords", info.getKeywords() );
                addTextField( document, "ModificationDate", info.getModificationDate() );
                addTextField( document, "Producer", info.getProducer() );
                addTextField( document, "Subject", info.getSubject() );
                addTextField( document, "Title", info.getTitle() );
                addTextField( document, "Trapped", info.getTrapped() );
            }

            addUnindexedField( document, "summary", "summary" );
        }
        catch( CryptographyException e )
        {
            throw new IOException( "Error decrypting document(" + documentLocation + "): " + e );
        }
        catch( InvalidPasswordException e )
        {
        	//they didn't suppply a password and the default of "" was wrong.
            throw new IOException( 
                "Error: The document(" + documentLocation + 
                ") is encrypted and will not be indexed." );
        }
        finally
        {
            if( pdfDocument != null )
            {
                pdfDocument.close();
            }
        }
    	
    }
    /**
     * This will add the contents to the lucene document.
     *
     * @param document The document to add the contents to.
     * @param is The stream to get the contents from.
     * @param documentLocation The location of the document, used just for debug messages.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private void addContent( Document document, InputStream is, String documentLocation ) throws IOException
    {
        PDDocument pdfDocument = null;
        try
        {
            pdfDocument = PDDocument.load( is );

            if( pdfDocument.isEncrypted() )
            {
                //Just try using the default password and move on
                pdfDocument.decrypt( "" );
            }

            //create a writer where to append the text content.
            StringWriter writer = new StringWriter();
            if( stripper == null )
            {
                stripper = new PDFTextStripper();
            }
            else
            {
                stripper.resetEngine();
            }
             stripper.writeText( pdfDocument, writer );
            
            // Note: the buffer to string operation is costless;
            // the char array value of the writer buffer and the content string
            // is shared as long as the buffer content is not modified, which will
            // not occur here.
            String contents = writer.getBuffer().toString();

            StringReader reader = new StringReader( contents );

            // Add the tag-stripped contents as a Reader-valued Text field so it will
            // get tokenized and indexed.
            addTextField( document, "contents", reader );
            //reader=null;writer=null;
            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            if( info != null ) 
            {
                addTextField( document, "Author", info.getAuthor() );
                addTextField( document, "CreationDate", info.getCreationDate() );
                addTextField( document, "Creator", info.getCreator() );
                addTextField( document, "Keywords", info.getKeywords() );
                addTextField( document, "ModificationDate", info.getModificationDate() );
                addTextField( document, "Producer", info.getProducer() );
                addTextField( document, "Subject", info.getSubject() );
                addTextField( document, "Title", info.getTitle() );
                addTextField( document, "Trapped", info.getTrapped() );
            }
            int summarySize = Math.min( contents.length(), 500 );
            String summary = contents.substring( 0, summarySize );
            // Add the summary as an UnIndexed field, so that it is stored and returned
            // with hit documents for display.
            addUnindexedField( document, "summary", summary );
        }
        catch( CryptographyException e )
        {
            throw new IOException( "Error decrypting document(" + documentLocation + "): " + e );
        }
        catch( InvalidPasswordException e )
        {
        	//they didn't suppply a password and the default of "" was wrong.
            throw new IOException( 
                "Error: The document(" + documentLocation + 
                ") is encrypted and will not be indexed." );
        }
        finally
        {
            if( pdfDocument != null )
            {
                pdfDocument.close();
            }
        }
    }

    /**
     * This will test creating a document.
     *
     * usage: java pdfparser.searchengine.lucene.LucenePDFDocument &lt;pdf-document&gt;
     *
     * @param args command line arguments.
     *
     * @throws IOException If there is an error.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 1 )
        {
            String us = LucenePDFDocument.class.getName();
            System.err.println( "usage: java " + us + " <pdf-document>" );
            //System.exit( 1 );
        }
        String a="C:\\Users\\lmcyisu\\Desktop\\AUPRP\\ET155\\ET1551-BlockFunctions.pdf";
        System.out.println( "Document=" + getDocument( new File( a ) ) );
    }
    /* Given binary/text mixed string (i.e a binary file) return only readable part (ASCII).
     * This function will eliminate any non-English code.
     * TODO As the last parser if we want to handle all files, need to add more parsers for each file type
     */
    public String getReaderable(String s){
    	byte[] bytes = s.getBytes();
    	int idx  = 0; int temp = 0;
    	for (int i=0;i<bytes.length;i++){
    		if (bytes[i]>31 && bytes[i]<127){
    			if ((32 != temp) || (32 == temp && 32 != bytes[i])) {
    				bytes[idx]=bytes[i];	
    				idx++;
    				temp = bytes[i];
    			}
    		}
    	}
    	String n = new String(bytes,0,idx);
    	return n;
    }

}
