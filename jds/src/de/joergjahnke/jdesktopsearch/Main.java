/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import de.joergjahnke.jdesktopsearch.abstractionlayer.AbstractIndexManager;
import de.joergjahnke.jdesktopsearch.abstractionlayer.LuceneIndexManager;
import de.joergjahnke.jdesktopsearch.abstractionlayer.SQLServerIndexManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.UIManager;


/**
 * Main class to start the JDesktopSearch application
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class Main {
    /**
     * Load index properties, create index manager and start either main window or execute command line options, depending on startup parameters
     *
     * @param args the command line arguments
     */
    public static void main( final String args[] ) {
        // load properties from file
        final IndexProperties properties = new IndexProperties();
        
        try {
            properties.loadFromXML();
        } catch( FileNotFoundException e ) {
            // create new file if it was not found
            properties.initialize();
        } catch( IOException e ) {
            // quit if file was incorrect
            e.printStackTrace();
            System.exit( 1 );
        }
        
        // load last index
        final AbstractIndexManager indexManager = createIndexManager( properties );

        // start command-line version?
        if( args.length > 0 ) {
            try {
                new CommandLineUtilities( indexManager ).execute( args );
            } catch( Exception e ) {
                e.printStackTrace();
            } finally {
                System.exit( 0 );
            }
        } else {
            // no, start GUI
            java.awt.EventQueue.invokeLater( new Runnable() {
                public void run() {
                    // set system look & feel
                    try {
                        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                    } catch( Exception e ) {
                        // no problem, use preset look & feel
                    }
                    // show main window
                    new MainFrame( indexManager, properties ).setVisible( true );
                }
              }
            );
        }
    }


    /**
     * Create index manager according to properties
     */
    public static AbstractIndexManager createIndexManager( final Properties properties ) {
        final String indexerType = properties.getProperty( "indexer_type" );

        if( IndexProperties.INDEXER_TYPE_LUCENE.equals( indexerType ) ) {
            return new LuceneIndexManager( properties );
        } else if( IndexProperties.INDEXER_TYPE_SQLSERVER.equals( indexerType ) ) {
            return new SQLServerIndexManager( properties );
        } else {
            throw new RuntimeException( "Indexer type '" + indexerType + "' not defined." );
        }
    }
}
