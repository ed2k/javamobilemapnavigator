/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch.abstractionlayer;


import de.joergjahnke.common.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.Token;


/**
 * AbstractIndexWriter implementation for SQL Server which stores the index inside a database
 * 
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class SQLServerIndexWriter implements AbstractIndexWriter {
    // average expected number of characters per word
    private final static int AVG_WORDLENGTH = 8;
    // use lucene tokenizer?
    private final boolean USE_LUCENE_TOKENIZER = true;
    
    
    private Connection con;
    
    
    /**
     * Creates a new instance of SQLServerIndexWriter
     */
    public SQLServerIndexWriter( final SQLServerIndexManager sqlIndexManager, final boolean create ) throws IOException {
        try {
            this.con = sqlIndexManager.getConnection();
        } catch( Exception e ) {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
        
        if( create ) {
            // drop all database tables and create them anew
            try {
                Statement stmt = this.con.createStatement();
                
                try { stmt.execute( "DROP TABLE Token_" ); } catch( Exception e ) {}
                try { stmt.execute( "DROP TABLE Field_" ); } catch( Exception e ) {}
                try { stmt.execute( "DROP TABLE RootDir_" ); } catch( Exception e ) {}
                try { stmt.execute( "DROP TABLE File_" ); } catch( Exception e ) {}
                stmt.close();
                stmt = null;
            } catch( SQLException e ) {}
            
            // create new database tables
            try {
                Statement stmt = this.con.createStatement();
                
                stmt.execute( "CREATE TABLE File_ ( ID int NOT NULL, Name varchar( 255 ) NOT NULL, AddedOn datetime DEFAULT GETDATE(), CONSTRAINT PK_File PRIMARY KEY ( ID ) )" );
                stmt.execute( "CREATE UNIQUE INDEX IDX_File_Name ON File_ ( Name )" );
                stmt.execute( "CREATE TABLE RootDir_ ( Name varchar( 255 ) NOT NULL, AddedOn datetime DEFAULT GETDATE(), CONSTRAINT PK_RootDir PRIMARY KEY ( Name ) )" );
                stmt.execute( "CREATE TABLE Field_ ( ID int NOT NULL, FileID int NOT NULL, Name varchar( 255 ) NOT NULL, Value varchar( 255 ), FullValue image, IsIndexable bit, IsCompressed bit, CONSTRAINT PK_Field PRIMARY KEY ( ID ), CONSTRAINT FK_Field_FileID FOREIGN KEY ( FileID ) REFERENCES File_ ( ID ) )" );
                stmt.execute( "CREATE INDEX IDX_Field_Name ON Field_ ( Name )" );
                stmt.execute( "CREATE INDEX IDX_Field_FileID ON Field_ ( FileID )" );
                stmt.execute( "CREATE TABLE Token_ ( FieldID int NOT NULL, Word_ varchar( 255 ) NOT NULL, Occurrences int NOT NULL, CONSTRAINT FK_Token_FieldID FOREIGN KEY ( FieldID ) REFERENCES Field_ ( ID ) )" );
                stmt.execute( "CREATE INDEX IDX_Token_Word ON Token_ ( Word_ )" );
                stmt.execute( "CREATE UNIQUE CLUSTERED INDEX IDX_Token_FieldIDWord ON Token_ ( FieldID, Word_ )" );
                stmt.close();
                stmt = null;
            } catch( SQLException e ) {
                e.printStackTrace();
                throw new IOException( e.getMessage() );
            }
        }
    }

    
    public synchronized void addDocument( final Document doc ) throws IOException {
        SQLException ex = null;
        
        try {
            this.con.setAutoCommit( false );
            
            // get ID for the new file
            Statement stmt = con.createStatement();
            int fileId = 1;
            ResultSet rs = stmt.executeQuery( "SELECT MAX( ID ) AS \"MaxID\" FROM File_" );
            
            if( null != rs && rs.next() ) {
                fileId = rs.getInt( "MaxID" ) + 1;
            }
            
            rs.close();
            rs = null;
            
            // insert new file record
            PreparedStatement insertFileStmt = con.prepareStatement( "INSERT INTO File_ ( ID, Name ) VALUES ( ?, ? )" );
            
            insertFileStmt.setInt( 1, fileId );
            insertFileStmt.setString( 2, doc.getField( "path" ).stringValue() );
            int inserted = insertFileStmt.executeUpdate();
            
            // prepare SQL statements for the different data to insert
            PreparedStatement insertFieldStmt = con.prepareStatement( "INSERT INTO Field_ ( ID, FileID, Name, Value, FullValue, IsIndexable, IsCompressed ) VALUES ( ?, ?, ?, ?, ?, ?, ? )" );
            PreparedStatement insertTokenStmt = con.prepareStatement( "INSERT INTO Token_ ( FieldID, Word_, Occurrences ) VALUES ( ?, ?, ? )" );

            // get ID for the next field
            int fieldId = 1;

            rs = stmt.executeQuery( "SELECT MAX( ID ) AS \"MaxID\" FROM Field_" );
            if( null != rs && rs.next() ) {
                fieldId = rs.getInt( "MaxID" ) + 1;
            }

            rs.close();
            rs = null;

            // insert all elements into the database
            for( Enumeration elements = doc.fields() ; elements.hasMoreElements() ; ) {
                final Field field = (Field)elements.nextElement();

                // don't save the value if not marked for storage
                String value = field.stringValue();
                
                if( ! field.isStored() ) {
                    value = null;
                }
                
                // compress data if it does not fit into the 255 char value field and
                // if this field is also tokenized so that a search can still take place
                byte[] fullValue = null == value || value.length() <= 255 ? null : value.getBytes();
                boolean isCompressed = false;
                
                if( null != fullValue && field.isTokenized() ) {
                    // compress data
                    final ByteArrayOutputStream newFullValue = new ByteArrayOutputStream();
                    final byte[] buf = new byte[ 4000 ];
                    final Deflater compresser = new Deflater();

                    compresser.setInput( fullValue );
                    compresser.finish();

                    while( ! compresser.finished() ) {
                        final int read = compresser.deflate( buf );

                        newFullValue.write( buf, 0, read );
                    }
                    compresser.end();

                    // only use compressed version if it is shorter than the original one
                    if( newFullValue.size() < fullValue.length ) {
                        fullValue = newFullValue.toByteArray();
                        isCompressed = true;
                    }

                }
            
                // insert new file record
                insertFieldStmt.setInt( 1, fieldId );
                insertFieldStmt.setInt( 2, fileId );
                insertFieldStmt.setString( 3, field.name() );
                insertFieldStmt.setString( 4, isCompressed || null == value ? null : StringUtils.left( value, 255 ) );
                if( isCompressed || ( null != fullValue && fullValue.length > 255 ) ) {
                    insertFieldStmt.setBytes( 5, fullValue );
                } else {
                    insertFieldStmt.setObject( 5, null );
                }
                insertFieldStmt.setBoolean( 6, field.isIndexed() );
                insertFieldStmt.setBoolean( 7, isCompressed );
                inserted = insertFieldStmt.executeUpdate();
            
                // tokenize string if necessary and store all tokens
                if( null != field.stringValue() ) {
                    final Map<String,Integer> tokenMap = new HashMap<String,Integer>( field.isTokenized() ? field.stringValue().length() / this.AVG_WORDLENGTH : 1 );

                    if( field.isTokenized() ) {
                        if( ! this.USE_LUCENE_TOKENIZER ) {
                            for( StringTokenizer tokenizer = new StringTokenizer( field.stringValue(), " |&.:?,;!()[]/\t\n\r\f\240" ) ; tokenizer.hasMoreTokens() ; ) {
                                final String token = tokenizer.nextToken().toLowerCase();

                                if( tokenMap.containsKey( token ) ) {
                                    tokenMap.put( token, tokenMap.get( token ) + 1 );
                                } else {
                                    tokenMap.put( token, 1 );
                                }
                            }
                        } else {
                            final Tokenizer tokenizer = new StandardTokenizer( new StringReader( field.stringValue() ) );
                            Token token = null;

                            while( ( token = tokenizer.next() ) != null ) {
                                final String tokenText = token.termText().toLowerCase();

                                if( tokenMap.containsKey( tokenText ) ) {
                                    tokenMap.put( tokenText, tokenMap.get( tokenText ) + 1 );
                                } else {
                                    tokenMap.put( tokenText, 1 );
                                }
                            }
                        }
                    } else {
                        tokenMap.put( field.stringValue().toLowerCase(), 1 );
                    }

                    // store tokens in database
                    for( String token : tokenMap.keySet() ) {
                        insertTokenStmt.setInt( 1, fieldId );
                        insertTokenStmt.setString( 2, token.substring( 0, Math.min( token.length(), 255 ) ) );
                        insertTokenStmt.setInt( 3, tokenMap.get( token ) );
                        try {
                            insertTokenStmt.executeUpdate();
                        } catch( SQLException e ) {
                            // it might happen that the sql server rejects the row as he assumes two entries which in
                            // Java are different are for him unique. We ignore this.
                        }
                    }
                }
                
                ++fieldId;
            }
            
            // end the transaction
            this.con.commit();
            
            // cleanup
            insertTokenStmt.close();
            insertTokenStmt = null;
            insertFieldStmt.close();
            insertFieldStmt = null;
            insertFileStmt.close();
            insertFileStmt = null;
            stmt.close();
            stmt = null;
        } catch( SQLException e ) {
            e.printStackTrace();
            ex = e;
            try {
                this.con.rollback();
            } catch( SQLException e2 ) {}
        } finally {
            try {
                this.con.setAutoCommit( true );
            } catch( SQLException e2 ) {}
        }
        
        // an error occurred
        if( null != ex ) throw new IOException( ex.getMessage() );
    }
    
    public synchronized void deleteDocument( final File file ) throws IOException {
        SQLException ex = null;
        
        try {
            // get file-ID
            PreparedStatement findFileStmt = con.prepareStatement( "SELECT ID AS \"ID\" FROM File_ WHERE Name = ?" );
            
            findFileStmt.setString( 1, file.getAbsolutePath() );
            
            ResultSet rs = findFileStmt.executeQuery();
            
            if( null != rs && rs.next() ) {
                int fileId = rs.getInt( "ID" );

                rs.close();
                rs = null;

                // delete entries from dependent tables and the file table
                PreparedStatement deleteTokensStmt = con.prepareStatement( "DELETE FROM Token_ WHERE FieldID IN ( SELECT ID FROM Field_ WHERE FileID = ? )" );

                deleteTokensStmt.setInt( 1, fileId );
                deleteTokensStmt.executeUpdate();

                PreparedStatement deleteFieldsStmt = con.prepareStatement( "DELETE FROM Field_ WHERE FileID = ?" );

                deleteFieldsStmt.setInt( 1, fileId );
                deleteFieldsStmt.executeUpdate();

                PreparedStatement deleteFileStmt = con.prepareStatement( "DELETE FROM File_ WHERE ID = ?" );

                deleteFileStmt.setInt( 1, fileId );
                deleteFileStmt.executeUpdate();

                // cleanup
                deleteFileStmt.close();
                deleteFileStmt = null;
                deleteFieldsStmt.close();
                deleteFieldsStmt = null;
                deleteTokensStmt.close();
                deleteTokensStmt = null;
            }
            
            findFileStmt.close();
            findFileStmt = null;
        } catch( SQLException e ) {
            e.printStackTrace();
            ex = e;
            try {
                this.con.rollback();
            } catch( SQLException e2 ) {}
        } finally {
            try {
                this.con.setAutoCommit( true );
            } catch( SQLException e2 ) {}
        }
        
        if( null != ex ) throw new IOException( ex.getMessage() );
    }
    
    /**
     * Close the database connection
     */
    public final void close() throws IOException {
        try {
            this.con.close();
            this.con = null;
        } catch( SQLException e ) {
            throw new IOException( e.getMessage() );
        }
    }
    
    public final void optimize() throws IOException {
        // not implemented
    }
}
