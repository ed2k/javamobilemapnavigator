/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.SAXException;


/**
 * Parser for XML documents.
 * Will extract all PCDATA texts of the XML document and collect them
 * as one document text. Subclasses may distribute the content of the
 * document over other fields like the title or omit some of the content.
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class XMLDocumentParser extends DefaultHandler implements DocumentParser {
    // buffer for each XML element
    protected final StringBuffer elementBuffer = new StringBuffer();
    // field to fill
    private String fieldName = "contents";
    // map containing element texts
    protected final Map<String,StringBuffer> elementContent = new HashMap<String,StringBuffer>();

    
    /**
     * Create a new XMLDocumentParser
     */
    public XMLDocumentParser() {
    }
    
    
    /**
     * Get map containing all extracted data of the document
     *
     * @return  map containing the key "contents" with the corresponding value holding the document text
     *          subclasses may provide additional keys
     */
    public Map<String,StringBuffer> getElementMap() {
        return this.elementContent;
    }
    
    /**
     * Clear element map to start a new document
     */
    protected void clearElementMap() {
        this.elementContent.clear();
    }

    
    /**
     * Get field to fill in lucene document.
     * Default name is "contents"
     *
     * @return  current field name
     */
    protected String getFieldName() {
        return this.fieldName;
    }
    
    /**
     * Set new field to fill in lucene document
     *
     * @param   fieldName   new field name
     */
    protected void setFieldName( String fieldName ) {
        this.fieldName = fieldName;
    }

    
    // overridden methods of the DefaultHandler
    
    // call at element start
    public void startElement( String uri, String localName, String qName, Attributes attributes  ) throws SAXException {
        this.elementBuffer.setLength( 0 );
    }

    // call when cdata found
    public void characters( char[] text, int start, int length ) {
        this.elementBuffer.append( text, start, length );
    }

    // call at element end
    public void endElement( String uri, String localName, String qName ) throws SAXException {
        if( this.elementBuffer.length() > 0 ) {
            if( this.elementContent.containsKey( getFieldName() ) ) {
                final StringBuffer content = this.elementContent.get( getFieldName() );
                
                content.append( ' ' );
                content.append( this.elementBuffer.toString().trim() );
            } else {
                this.elementContent.put( getFieldName(), new StringBuffer( this.elementBuffer.toString().trim() ) );
            }
        }
    }

    
    // implementation of the DocumentParser interface
    
    public String getTitle() {
        final StringBuffer title = this.elementContent.get( "title" );
        
        return null == title ? null : title.toString();
    }
    
    public String getContent() {
        final StringBuffer contents = this.elementContent.get( "contents" );
        
        return null == contents ? null : contents.toString();
    }

    public String getAuthor() {
        final StringBuffer author = this.elementContent.get( "author" );
        
        return null == author ? null : author.toString();
    }

    public String[] getKeywords() {
        final StringBuffer keywords = this.elementContent.get( "keywords" );
        
        return null == keywords ? null : keywords.toString().split( " " );
    }
    
    public String getDescription() {
        final StringBuffer description = this.elementContent.get( "description" );
        
        return null == description ? null : description.toString();
    }
    
    public void parse( File file ) throws IOException {
        final InputStream in = new BufferedInputStream( new FileInputStream( file ) );
        
        parse( in );
        in.close();
    }
    
    public void parse( InputStream is ) throws IOException {
        // start new document
        clearElementMap();
        
        // use SAX parser to extract content of the document
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        
        try {
            final SAXParser parser = spf.newSAXParser();
            final ParserAdapter pa = new ParserAdapter( parser.getParser() );

            pa.setContentHandler( this );
            pa.setEntityResolver( new EntityResolver() {
                /**
                 * Avoid DTD resolution
                 *
                 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
                 */
                public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
                    final byte[] empty = {};

                    if( systemId != null && systemId.toLowerCase().endsWith( ".dtd" ) ) {
                        return new InputSource( new ByteArrayInputStream( empty ) );
                    }
                    return null;
                }
              }
            );

            pa.parse( new InputSource( is ) );
        } catch( Exception e ) {
            throw new IOException( "Could not parse XML document! The error message was: " + e.getMessage() );
        }
    }
    
    
    public DocumentParser newInstance() {
        return new XMLDocumentParser();
    }
    
    public List<String> getSupportedFileTypes() {
        final String[] types = { ".xml" };
        
        return Arrays.asList( types );
    }
}
