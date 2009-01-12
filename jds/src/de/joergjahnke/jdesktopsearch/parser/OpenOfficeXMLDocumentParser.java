/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.parser;


import org.xml.sax.SAXException;


/**
 * Special XMLDocumentParser for the XML content of OpenOffice documents
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class OpenOfficeXMLDocumentParser extends XMLDocumentParser {
    /**
     * Creates a new instance of OpenOfficeDocumentHandler
     */
    public OpenOfficeXMLDocumentParser() {
    }


    /**
     * Fill fields in document depending on the localName of the XML element
     */
    public void endElement( final String uri, final String localName, final String qName ) throws SAXException {
        if( this.elementBuffer.length() > 0 ) {
            String name = getFieldName();
            
            if( localName != null && localName.startsWith( "keyword" ) ) {
                name = "keywords";
            } else if( "creator".equals( localName ) ) {
                name = "author";
            } else if( "subject".equals( localName ) || "title".equals( localName ) ) {
                name = "title";
            } else if( "description".equals( localName ) ) {
                name = "description";
            }
            
            if( this.elementContent.containsKey( name ) ) {
                final StringBuffer content = this.elementContent.get( name );
                
                content.append( ' ' );
                content.append( this.elementBuffer.toString().trim() );
            } else {
                this.elementContent.put( name, new StringBuffer( this.elementBuffer.toString().trim() ) );
            }
        }
    }
    
    
    public DocumentParser newInstance() {
        return new OpenOfficeXMLDocumentParser();
    }
}
