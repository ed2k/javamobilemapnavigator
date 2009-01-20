/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import de.joergjahnke.jdesktopsearch.abstractionlayer.AbstractIndexManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


/**
 * Main window of the JDesktopSearch application
 * TODO, try build the file list from indexdb
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class MainFrame extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * program name
     */
    private final static String APP_NAME = "JDesktopSearch";
    /**
     * program version
     */
    private final static String VERSION = "2.0.0";
    /**
     * URL of the online help page
     */
    private final static String URL_ONLINE_HELP = "http://jdesktopsearch.wiki.sourceforge.net/Online+help";
    
    
    // index manager
    private AbstractIndexManager indexManager = null;
    // properties
    private final IndexProperties properties;
    // do we have a tray icon installed?
    private boolean isTrayIconInstalled = false;
    // copy of the main window
    private final JFrame mainWindow;
    // status bar
    private final IndexingStatusPanel statusbar;
    
    
    /**
     * Creates new form JDesktopSearchFrame
     */
    public MainFrame( final AbstractIndexManager indexManager, final IndexProperties properties ) {
        this.properties = properties;
        this.mainWindow = this;
        
        initComponents();
        
        // create a status bar an register it as observer for the index manager
        this.statusbar = new IndexingStatusPanel();
        add( this.statusbar, BorderLayout.SOUTH );
        
        // install index manager
        setIndexManager( indexManager );
        
        // try to install tray icon
        if( SystemTray.isSupported() ) {
            installTrayIcon();
        }
        
        // set the window icon
        try {
            setIconImage( getToolkit().getImage( getClass().getResource( "/res/JDesktopSearch.gif" ) ) );
        } catch( SecurityException e ) {
            // we can work without the icon having been set
        }
        
        // set preferred size according to settings
        final int width = Integer.parseInt( this.properties.getProperty( "main_window_width", "750" ) );
        
        this.jTabbedPaneSearch.setPreferredSize( new Dimension( width, Integer.parseInt( this.properties.getProperty( "main_window_height", "550" ) ) ) );
        pack();

        // create one default tab
        jMenuItemNewSearchTabActionPerformed( null );

        // center window
        setLocation( (int)( getToolkit().getScreenSize().getWidth() - getWidth() ) / 2, (int)( getToolkit().getScreenSize().getHeight() - getHeight() ) / 2 );
        
        // update menu items
        updateIndexMenuItems();
    }

    
    /**
     * Set the index manager and let it be observed by the status bar
     *
     * @param   indexManager    new indexManager
     */
    private void setIndexManager( final AbstractIndexManager indexManager ) {
        this.indexManager = indexManager;
        this.indexManager.addObserver( this.statusbar );
    }

    /**
     * Install a tray icon
     */
    private void installTrayIcon() {
        // initialize tray icon image
        final Image image = Toolkit.getDefaultToolkit().getImage( getClass().getResource( "/res/JDesktopSearch.gif" ) );

        // tray icon will open a popup menu
        final PopupMenu popup = new PopupMenu();

        // add menu item to restore main window
        final MenuItem restore = new MenuItem( "Restore" );

        restore.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                setVisible( true );
                setState( Frame.NORMAL );
                toFront();
            }
          }
        );
        popup.add( restore );
        popup.addSeparator();

        // add menu item to close the application
        final MenuItem update = new MenuItem( "Update index" );

        update.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                jMenuItemUpdateIndexActionPerformed( evt );
            }
          }
        );
        popup.add( update );
        popup.addSeparator();

        // add menu item to close the application
        final MenuItem close = new MenuItem( "Close" );

        close.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {
                jMenuItemCloseActionPerformed( evt );
            }
          }
        );
        popup.add( close );

        // create tray icon with this menu
        final TrayIcon trayIcon = new TrayIcon( image , APP_NAME + " " + VERSION, popup );
        
        trayIcon.setImageAutoSize( true );
        
        // add this icon to the system tray
        try {
            SystemTray.getSystemTray().add( trayIcon );
            this.isTrayIconInstalled = true;
        } catch( Exception e ) {
            // installing has failed, this is no problem
        }
    }
    
    
    /**
     * Add a new directory to the index.
     * Opens a file dialog to select the directory and adds it to the index.
     *
     * @createNew   true to create a new index, false to add to an existing index
     */
    private void addDirectoryToIndex() {
        final JFileChooser fileDialog = new JFileChooser();

        fileDialog.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
        fileDialog.setMultiSelectionEnabled( true );
        
        if( JFileChooser.APPROVE_OPTION == fileDialog.showOpenDialog( this ) ) {
            DesktopSearchWorker worker = new DesktopSearchWorker() {
                @Override
                protected Object main() throws Exception {
                    // create new index only for the first file/directory if requested
                    for( File file : fileDialog.getSelectedFiles() ) {
                        indexManager.add( file );
                    }
                    
                    return null;
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Update index-related menu items.
     * If an index does not exist some of the menu items are disabled.
     */
    private void updateIndexMenuItems() {
        final boolean indexExists = null != this.indexManager && this.indexManager.exists();
        
        this.jMenuItemNewIndex.setEnabled( true );
        this.jMenuItemUpdateIndex.setEnabled( indexExists );
        this.jMenuItemAddToIndex.setEnabled( indexExists );
        this.jMenuItemOptimize.setEnabled( indexExists );
        this.jMenuItemUnlock.setEnabled( indexExists );
        this.jMenuItemSettings.setEnabled( true );
        this.jMenuItemIndexProperties.setEnabled( indexExists );
    }

    /**
     * During indexing some menu items must not be available
     */
    private void updateMenuItemsForIndexing() {
        this.jMenuItemAddToIndex.setEnabled( false );
        this.jMenuItemNewIndex.setEnabled( false );
        this.jMenuItemUpdateIndex.setEnabled( false );
        this.jMenuItemOptimize.setEnabled( false );
        this.jMenuItemUnlock.setEnabled( false );
        this.jMenuItemSettings.setEnabled( false );
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jTabbedPaneSearch = new javax.swing.JTabbedPane();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNewSearchTab = new javax.swing.JMenuItem();
        jMenuItemCloseSearchTab = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemClose = new javax.swing.JMenuItem();
        jMenuIndex = new javax.swing.JMenu();
        jMenuItemNewIndex = new javax.swing.JMenuItem();
        jMenuItemAddToIndex = new javax.swing.JMenuItem();
        jMenuItemUpdateIndex = new javax.swing.JMenuItem();
        jMenuItemOptimize = new javax.swing.JMenuItem();
        jMenuItemUnlock = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemSettings = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemIndexProperties = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();
        jMenuItemContents = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JDesktopSearch");
        setName("JDesktopSearch");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        jTabbedPaneSearch.setPreferredSize(null);
        getContentPane().add(jTabbedPaneSearch, java.awt.BorderLayout.CENTER);

        jMenuFile.setText("File");
        jMenuItemNewSearchTab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNewSearchTab.setText("New search tab");
        jMenuItemNewSearchTab.setToolTipText("Create a new, empty search tab");
        jMenuItemNewSearchTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewSearchTabActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemNewSearchTab);

        jMenuItemCloseSearchTab.setText("Close search tab");
        jMenuItemCloseSearchTab.setToolTipText("Close currently selected search tab");
        jMenuItemCloseSearchTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseSearchTabActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemCloseSearchTab);

        jMenuFile.add(jSeparator1);

        jMenuItemClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemClose.setText("Exit");
        jMenuItemClose.setToolTipText("Exit program");
        jMenuItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemClose);

        jMenuBar.add(jMenuFile);

        jMenuIndex.setText("Index");
        jMenuItemNewIndex.setText("Create new...");
        jMenuItemNewIndex.setToolTipText("Delete the existing index and create a new one, starting with a directory to index");
        jMenuItemNewIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewIndexActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemNewIndex);

        jMenuItemAddToIndex.setText("Add directory...");
        jMenuItemAddToIndex.setToolTipText("Add new directory with all sub-directories and files to the index");
        jMenuItemAddToIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddToIndexActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemAddToIndex);

        jMenuItemUpdateIndex.setText("Update");
        jMenuItemUpdateIndex.setToolTipText("Update all directories in the index to find new or updated files and remove unused ones");
        jMenuItemUpdateIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUpdateIndexActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemUpdateIndex);

        jMenuItemOptimize.setText("Optimize");
        jMenuItemOptimize.setToolTipText("Optimize index for better performancel");
        jMenuItemOptimize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOptimizeActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemOptimize);

        jMenuItemUnlock.setText("Unlock");
        jMenuItemUnlock.setToolTipText("Removes a write lock existing for the index");
        jMenuItemUnlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUnlockActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemUnlock);

        jMenuIndex.add(jSeparator2);

        jMenuItemSettings.setText("Edit Settings");
        jMenuItemSettings.setToolTipText("Edit index settings [Expert]");
        jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSettingsActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemSettings);

        jMenuIndex.add(jSeparator3);

        jMenuItemIndexProperties.setText("Show Properties");
        jMenuItemIndexProperties.setToolTipText("Show statistics about file-types and directories in the index");
        jMenuItemIndexProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemIndexPropertiesActionPerformed(evt);
            }
        });

        jMenuIndex.add(jMenuItemIndexProperties);

        jMenuBar.add(jMenuIndex);
       
        jMenuHelp.setText("Help");
        javax.swing.JMenuItem b = new javax.swing.JMenuItem();
        b.setText("Stop Indexing");
        b.addActionListener(new java.awt.event.ActionListener(){
        	public void actionPerformed(java.awt.event.ActionEvent evt){
        		if( indexManager.exists() ) {
    	            indexManager.stopIndexing();
    	        }
        	}
        });
        jMenuHelp.add(b);
        
        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemAbout);

        jMenuItemContents.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItemContents.setText("Contents");
        jMenuItemContents.setToolTipText("Display help page");
        jMenuItemContents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemContentsActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemContents);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemUnlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUnlockActionPerformed
        try {
            this.indexManager.unlock();
        } catch( Exception e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( this, "Could not unlock the index. Try removing the lock file manually", "Could not unlock the index", JOptionPane.ERROR_MESSAGE );
        }
        
    }//GEN-LAST:event_jMenuItemUnlockActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        jMenuItemCloseActionPerformed( null );
    }//GEN-LAST:event_formWindowClosing

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        if( !this.isTrayIconInstalled ) {
            setState( Frame.ICONIFIED );
        } else {
            setVisible( false );
        }
    }//GEN-LAST:event_formWindowIconified

    private void jMenuItemContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemContentsActionPerformed
        try {
            java.awt.Desktop.getDesktop().browse( new URI( URL_ONLINE_HELP ) );
        } catch( Throwable t ) {
            JOptionPane.showMessageDialog( this, "Could not start browser to display online help from '" + URL_ONLINE_HELP + "'", "Could not display online-help", JOptionPane.ERROR_MESSAGE );
        }
    }//GEN-LAST:event_jMenuItemContentsActionPerformed

    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        // show a dialog to edit the settings
        final IndexSettingsDialog dialog = new IndexSettingsDialog( this, this.properties );
        
        dialog.setVisible( true );
        if( dialog.wasApproved() ) {
            try {
                // store the new settings
                this.properties.storeToXML();
                // it might be necessary to clear the index
                if( dialog.isOldIndexInvalid() ) {
                    try {
                        this.indexManager.clear();
                        updateIndexMenuItems();
                    } catch( IOException e ) {
                        e.printStackTrace();
                    }
                }
                // assign a new index manager which reloads the new settings
                setIndexManager( Main.createIndexManager( this.properties ) );
            } catch( IOException e ) {
                JOptionPane.showMessageDialog( this, "Could not save settings! The error message was:\n" + e.getMessage(), "Error while saving settings", JOptionPane.ERROR_MESSAGE );
            }
        }
    }//GEN-LAST:event_jMenuItemSettingsActionPerformed

    private void jMenuItemOptimizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOptimizeActionPerformed
        DesktopSearchWorker worker = new DesktopSearchWorker() {
            @Override
            protected Object main() throws Exception {
                indexManager.optimize();

                return null;
            }
        };

        worker.execute();
    }//GEN-LAST:event_jMenuItemOptimizeActionPerformed

    private void jMenuItemCloseSearchTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseSearchTabActionPerformed
        final int index = this.jTabbedPaneSearch.getSelectedIndex();
        
        if( index >= 0 ) {
            this.jTabbedPaneSearch.removeTabAt( index );
        }
    }//GEN-LAST:event_jMenuItemCloseSearchTabActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        JOptionPane.showMessageDialog( this, 
        		"© 2005-2007 by Jörg Jahnke\n\nThis program uses the following libraries:\n-Apache Lucene (http://lucene.apache.org/)\n-textmining (http://www.textmining.org/)\n-PDFBox (http://www.pdfbox.org/)\n-Log4j (http://logging.apache.org/)\n-jTDS (http://jtds.sourceforge.net/)\n\nThis program and its used libraries are distributed without warranties or conditions of any kind.", 
        		"About " + APP_NAME + " " + VERSION, JOptionPane.PLAIN_MESSAGE );
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemIndexPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemIndexPropertiesActionPerformed
        JOptionPane.showMessageDialog( this, this.indexManager.getProperties(), "Index properties", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_jMenuItemIndexPropertiesActionPerformed

    private void jMenuItemAddToIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddToIndexActionPerformed
        addDirectoryToIndex();
    }//GEN-LAST:event_jMenuItemAddToIndexActionPerformed

    private void jMenuItemNewIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewIndexActionPerformed
        if( ! this.indexManager.exists() || JOptionPane.showConfirmDialog( this, "Really discard existing index?", "Delete old index?", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {
            try {
                this.indexManager.clear();
                addDirectoryToIndex();
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jMenuItemNewIndexActionPerformed

    private void jMenuItemUpdateIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUpdateIndexActionPerformed
        DesktopSearchWorker worker = new DesktopSearchWorker() {
            @Override
            protected Object main() throws Exception {
                indexManager.updateAll();

                return null;
            }
        };

        worker.execute();
    }//GEN-LAST:event_jMenuItemUpdateIndexActionPerformed

    private void jMenuItemNewSearchTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewSearchTabActionPerformed
        this.jTabbedPaneSearch.addTab( "New search", new SearchPanel( this.indexManager, this.properties ) );
    }//GEN-LAST:event_jMenuItemNewSearchTabActionPerformed

    private void jMenuItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseActionPerformed
        this.indexManager.stopIndexing();
        System.exit( 0 );
    }//GEN-LAST:event_jMenuItemCloseActionPerformed

        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenu jMenuIndex;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAddToIndex;
    private javax.swing.JMenuItem jMenuItemClose;
    private javax.swing.JMenuItem jMenuItemCloseSearchTab;
    private javax.swing.JMenuItem jMenuItemContents;
    private javax.swing.JMenuItem jMenuItemIndexProperties;
    private javax.swing.JMenuItem jMenuItemNewIndex;
    private javax.swing.JMenuItem jMenuItemNewSearchTab;
    private javax.swing.JMenuItem jMenuItemOptimize;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JMenuItem jMenuItemUnlock;
    private javax.swing.JMenuItem jMenuItemUpdateIndex;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPaneSearch;
    // End of variables declaration//GEN-END:variables


    // inner class for executing long running threads
    
    abstract class DesktopSearchWorker extends SwingWorker<Object,Object> {
        /**
         * Do the main work and show any occurring errors
         */
        @Override
        protected Object doInBackground() {
            Object result = null;
            
            // disable some menu items
            updateMenuItemsForIndexing();
            
            try {
                result = main();
            } catch( Exception e ) {
                // show error message
                JOptionPane.showMessageDialog( mainWindow, e.getMessage(), "An exception has occurred", JOptionPane.ERROR_MESSAGE );
                e.printStackTrace();
                // reset status panel
                statusbar.update( null, new IndexStatusMessage( IndexStatusMessage.EVENT_RESET ) );
            }
            
            // enable some menu items when we are done
            updateIndexMenuItems();
            
            return result;
        }

        /**
         * Do the main work
         */
        protected abstract Object main() throws Exception;
    }
}
