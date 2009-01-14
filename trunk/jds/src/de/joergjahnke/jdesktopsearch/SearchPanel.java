/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


import de.joergjahnke.common.io.FileUtils;
import de.joergjahnke.common.util.StringUtils;
import de.joergjahnke.jdesktopsearch.abstractionlayer.AbstractIndexManager;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;


/**
 * Panel to display the search form
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class SearchPanel extends javax.swing.JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// maximum number of characters to show as document title
    private final int MAX_TITLE_LENGTH = 15;
    
    
    // debugging?
    private final static boolean DEBUG = false;
    // maximum text length to display, actual displayed text length can be up to twice this value
    private final static int MAX_LENGTH = 200;
    // use highlighter to display results?
    private final static boolean IS_USE_HIGHLIGHTER = true;
    // html tags for term highlighting
    private final String MARK_START = "<b><font color='purple'>";
    private final String MARK_END = "</font></b>";

                    
    // AbstractIndexManager to contact for search operations
    private final AbstractIndexManager indexManager;
    
    
    /** Creates new form SearchPanel */
    public SearchPanel( AbstractIndexManager indexManager, final Properties properties ) {
        this.indexManager = indexManager;
        
        initComponents();
        this.jTextFieldSearchText.grabFocus();
        
        // add hyperlink listener which opens the documents
        this.jEditorPaneResults.addHyperlinkListener( new HyperlinkListener() {
            public void hyperlinkUpdate( HyperlinkEvent evt ) {
                if( evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                    final URL url = evt.getURL();
                    
                    // URL is a special: url?
                    if( evt.getDescription().startsWith( "special://" ) ) {
                        String command = evt.getDescription().split( "special://" )[ 1 ];
                        
                        if( "searchAgain".equals( command ) ) {
                            startSearch( 0 );
                        } else {
                            throw new RuntimeException( "Unknown command: " + command );
                        }
                    } else {
                        // process a document URL and load the document with the file handler application
                        try {
                            // try desktop-integration first
                            Desktop.getDesktop().open( new File( url.toURI() ) );
                        } catch( Throwable t ) {
                            // if not working start registered file-handler
                            final Runtime runtime= Runtime.getRuntime();

                            try {
                                final String extension = FileUtils.getExtension( url.toString() ).toLowerCase();
                                String handler = properties.getProperty( "file_handler_default" );

                                for( Iterator iter = properties.keySet().iterator() ; iter.hasNext() ; ) {
                                    final String key = iter.next().toString();

                                    if( key.startsWith( "file_handler" ) && key.indexOf( extension ) > 0 ) {
                                        handler = properties.getProperty( key );
                                    }
                                }
                                runtime.exec( handler + " \"" + new File( new URI( url.toString() ) ).getAbsolutePath() + "\"" );
                            } catch( Exception e ) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
          }
        );
    }

    /**
     * Get search text which depends on search text field and the selected operator
     *
     * @return  search text to parse
     */
    private String getSearchText() {
        String searchText = this.jTextFieldSearchText.getText();
        
        // this text is the new title
        ( (JTabbedPane)getParent() ).setTitleAt( ( (JTabbedPane)getParent() ).indexOfComponent( this ), searchText.length() > MAX_TITLE_LENGTH ? searchText.substring( 0, MAX_TITLE_LENGTH - 3 ) + "..." : searchText );

        // need to parse search text to insert AND or OR operators?
        if( this.jRadioButtonAND.isSelected() || this.jRadioButtonOR.isSelected() ) {
            // use StringTokenizer to parse the text
            final StringTokenizer tokenizer = new StringTokenizer( searchText, " \t\"", true );
            final StringBuffer newSearchText = new StringBuffer();
            boolean isQuote = false;
            boolean needOperator = false;

            while( tokenizer.hasMoreTokens() ) {
                final String token = tokenizer.nextToken();

                // delimiter found?
                if( " ".equals( token ) || "\t".equals( token ) ) {
                    // in case of a quotation just append the token, otherwise have operator inserted next
                    if( isQuote ) {
                        newSearchText.append( token );
                    } else {
                        needOperator = true;
                    }
                // handle "normal" text
                } else {
                    // insert operator if required
                    if( needOperator ) {
                        if( this.jRadioButtonAND.isSelected() ) {
                            newSearchText.append( " AND " );
                        } else if( this.jRadioButtonOR.isSelected() ) {
                            newSearchText.append( " OR " );
                        }
                        needOperator = false;
                    }
                    // remember quotations as they must not be split
                    if( "\"".equals( token ) ) {
                        isQuote = ! isQuote;
                        newSearchText.append( token );
                    } else {
                        // append the search token
                        newSearchText.append( token );
                    }
                }
            }

            searchText = newSearchText.toString();
        }
        
        return searchText;
    }

    /**
     * Highlight search terms in a given StringBuffer
     *
     * @param   value   string where to highlight terms
     * @param   searchTerms set of strings which are the search terms
     * @param   currentLength   current length of highlighted text
     * @return  String where the search terms are highlighted using HTML font attributes
     */
    private String highlightSearchTerms( final String value, final Set<String> searchTerms, int currentLength ) {
        final String valueLC = value.toLowerCase();
        boolean isStarted = currentLength > 0;

        for( String searchTerm : searchTerms ) {
            if( isStarted ) break;
            isStarted = valueLC.indexOf( searchTerm.toLowerCase() ) >= 0;
        }

        // highlight search terms
        StringBuffer result = new StringBuffer( StringUtils.xmlEncode( value ) );

        for( String searchTerm : searchTerms ) {
            int index = 0;
            final String resultLC = result.toString().toLowerCase();

            while( ( index = resultLC.indexOf( searchTerm.toLowerCase(), index ) ) >= 0 ) {
                StringBuffer newResult = new StringBuffer( result.substring( 0, index ) );

                newResult.append( this.MARK_START );
                newResult.append( result.substring( index, index + searchTerm.length() ) );
                newResult.append( this.MARK_END );
                newResult.append( result.substring( index + searchTerm.length(), result.length() ) );
                result = newResult;
                index += searchTerm.length() + this.MARK_START.length() + this.MARK_END.length();
            }
            //contents = new StringBuffer( contents.toString().replaceAll( searchTerm, "<b><font color='purple'>" + searchTerm + "</font></b>" ) );
        }

        // trim string if necessary
        currentLength += result.toString().replaceAll( MARK_START, "" ).replaceAll( MARK_END, "" ).length();
        
        if( currentLength > MAX_LENGTH ) {
            // search first occurrence of a search term within the content strings
            int startIndex = -1;
            final String resultLC = result.toString().toLowerCase();

            for( String searchTerm : searchTerms ) {
                startIndex = startIndex >= 0 ? Math.min( startIndex, resultLC.indexOf( searchTerm.toLowerCase() ) ) : resultLC.indexOf( searchTerm.toLowerCase() );
            }
            startIndex = Math.max( 0, resultLC.indexOf( ' ', Math.max( 0, startIndex - MAX_LENGTH / 6 ) ) );

            // set end index
            int endIndex = Math.min( result.length(), resultLC.indexOf( ' ', startIndex + MAX_LENGTH * 5 / 6 ) );

            if( -1 == endIndex ) endIndex = Math.min( result.length(), startIndex + MAX_LENGTH );

            result = new StringBuffer( result.substring( startIndex, endIndex ) );
        }

        return result.toString();
    }
    
    /**
     * Get HTML representation of search results
     *
     * @param   documents   found result documents
     * @param   query   query which was executed and which contains the search terms
     * @param   maxResults  maximum number of results to display, 0 for unlimited search
     * @return  StringBuffer containing HTML to display
     */
    private StringBuffer getSearchResultHTML( final Collection<Document> documents, final Query query, final int maxResults ) {
        // create result string with HTML content
        final StringBuffer result = new StringBuffer( "<html><body>" );

        result.append( "<i>Searching for &quot;" + query.toString() + "&quot;</i><br><br>" );

        // get query strings to mark in result
        final Set<String> searchTerms = QueryUtils.getSearchTerms( query, "contents" );

        // add hits to result string
        int found = 0;

        // prepare highlighter
        final Formatter formatter = new SimpleHTMLFormatter( MARK_START, MARK_END );
        final Highlighter highlighter;

        if( IS_USE_HIGHLIGHTER ) {
        	//QueryScorer q = new QueryScorer(query);
            highlighter = new Highlighter( formatter, new QueryScorer( query ) );
        }
        
        for( Document doc : documents ) {
            final String path = doc.get( "path" );

            // add title if one exists
            String[] values = doc.getValues( "title" );

            if( null != values ) {
                result.append( "<b>" );

                for( int j = 0 ; j < values.length ; ++j ) {
                    result.append( values[ j ] );
                    if( j != values.length - 1 ) {
                        result.append( ' ' );
                    }
                }

                result.append( "</b><br>" );
            }

            // add part of the document
            values = doc.getValues( "contents" );
            if( null != values ) {
                final StringBuffer contents = new StringBuffer();
                for( int j = 0 ; j < values.length && contents.length() < MAX_LENGTH ; ++j ) {
                    final String value = values[ j ];
                    String newContents = null;

                    if( IS_USE_HIGHLIGHTER ) {
                        final TokenStream tokenStream = new StandardAnalyzer().tokenStream( "contents", new StringReader( value ) );

                        try {
                            // get 3 best fragments and seperate with a "..."
                            newContents = highlighter.getBestFragments( tokenStream, value, 3, "<br>...<br>" );
                        } catch( IOException e ) {
                        	System.err.println("why here "+e.getMessage());
                        }
                    } else {
                        newContents = highlightSearchTerms( value, searchTerms, contents.toString().replaceAll( MARK_START, "" ).replaceAll( MARK_END, "" ).length() );
                    }

                    if( null != newContents && newContents.length() > 0 ) {
                        contents.append( newContents );
                        contents.append( ' ' );
                    }
                }

                if( contents.length() > 0 ) {
                    result.append( contents.toString() );
                    result.append( "<br>" );
                }
            } else {
            	System.err.println("why no contents");
            }

            // add url to document
            result.append( "<a href='" + new File( path ).toURI() + "'>" + path + "</a><br><br>" );
            ++found;
        }

        result.append( "<br>" + found + " documents found." );

        // show link to get more results if the maximum result number was reached
        if( maxResults > 0 && documents.size() >= maxResults ) {
            result.append( "<br><br>The maximum of " + maxResults + " documents to display was reached. More results might be available. Click <a href='special://searchAgain'>here</a> to search again without a result number limitation. Please note that such a search might take a considerable amount of time depending on the number of documents found." );
        }

        result.append( "</body></html>" );
            
        return result;
    }
    
    /**
     * Start search
     * Parameters will be retrieved from form fields
     *
     * @param   maxResults  maximum number of results to display, 0 for unlimited search
     */
    public void startSearch( final int maxResults ) {
        // get search text and path
        final String searchText = getSearchText();
        final String searchPath = "".equals( this.jTextFieldPath.getText() ) ? null : this.jTextFieldPath.getText();

        // clear old results
        this.jEditorPaneResults.setText( "" );

        try {
            if( DEBUG ) System.err.println("Starting search at " + new java.util.Date() );

            // determine fields to search in
            final Set<String> searchFields = new HashSet<String>();

            if( this.jCheckBoxContents.isSelected() ) searchFields.add( "contents" );
            if( this.jCheckBoxDescription.isSelected() ) searchFields.add( "description" );
            if( this.jCheckBoxKeywords.isSelected() ) searchFields.add( "keywords" );
            if( this.jCheckBoxPath.isSelected() ) searchFields.add( "path" );
            if( this.jCheckBoxTitle.isSelected() ) searchFields.add( "title" );

            // start search
            final SearchResult searchResult = this.indexManager.search( searchText, searchPath, maxResults, searchFields );

            // create HTML page with highlighted search terms
            if( DEBUG ) System.err.println( searchResult.getDocuments().size() + " entries to process" );
            if( DEBUG ) System.err.println( "Starting highlighting at " + new java.util.Date() );

            final Collection<Document> documents = searchResult.getDocuments();
            final Query query = searchResult.getQuery();
            final StringBuffer result = getSearchResultHTML( documents, query, maxResults );

            if( DEBUG ) System.err.println("Highlighting finished at " + new java.util.Date() );

            // display result string
            this.jEditorPaneResults.setText( result.toString() );
            this.jEditorPaneResults.setCaretPosition( 0 );
            if( DEBUG ) System.err.println( this.getClass().getName() + ": EditorPanel filled at " + new java.util.Date() );
        } catch( Throwable t ) {
            if( t instanceof OutOfMemoryError ) {
                JOptionPane.showMessageDialog( this, "The search engine has run out of memory!\nTry to restrict your search or to increase the memory allowance for the application using e.g. 'java -Xmx256m'.\nThe error message was: " + t.getMessage(), "An error has occurred", JOptionPane.ERROR_MESSAGE );
            } else {
                JOptionPane.showMessageDialog( this, "An exception has occurred!\nThe error message was: " + t.getMessage(), "An exception has occurred", JOptionPane.ERROR_MESSAGE );
            }
            t.printStackTrace();
        } finally {
            System.gc();
            if( DEBUG ) System.err.println( this.getClass().getName() + ": Garbage collection finished at " + new java.util.Date() );
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelSearchFor = new javax.swing.JPanel();
        jLabelSearch = new javax.swing.JLabel();
        jTextFieldSearchText = new javax.swing.JTextField();
        jRadioButtonAND = new javax.swing.JRadioButton();
        jRadioButtonOR = new javax.swing.JRadioButton();
        jRadioButtonCustom = new javax.swing.JRadioButton();
        jButtonFind = new javax.swing.JButton();
        jPanelSearchInPath = new javax.swing.JPanel();
        jLabelPath = new javax.swing.JLabel();
        jTextFieldPath = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jPanelSearchMisc = new javax.swing.JPanel();
        jLabelMaxResults = new javax.swing.JLabel();
        jTextFieldMaxResults = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxTitle = new javax.swing.JCheckBox();
        jCheckBoxPath = new javax.swing.JCheckBox();
        jCheckBoxDescription = new javax.swing.JCheckBox();
        jCheckBoxKeywords = new javax.swing.JCheckBox();
        jCheckBoxContents = new javax.swing.JCheckBox();
        jScrollPaneResults = new javax.swing.JScrollPane();
        jEditorPaneResults = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane.setToolTipText("");
        jPanelSearchFor.setToolTipText("Enter search text and start search");
        jLabelSearch.setText("Search for");
        jPanelSearchFor.add(jLabelSearch);

        jTextFieldSearchText.setToolTipText("Enter the search terms here");
        jTextFieldSearchText.setPreferredSize(new java.awt.Dimension(200, 19));
        jTextFieldSearchText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSearchTextActionPerformed(evt);
            }
        });

        jPanelSearchFor.add(jTextFieldSearchText);

        buttonGroup.add(jRadioButtonAND);
        jRadioButtonAND.setSelected(true);
        jRadioButtonAND.setText("AND");
        jRadioButtonAND.setToolTipText("All terms of the search string are required");
        jPanelSearchFor.add(jRadioButtonAND);

        buttonGroup.add(jRadioButtonOR);
        jRadioButtonOR.setText("OR");
        jRadioButtonOR.setToolTipText("At least one of the search terms is required");
        jPanelSearchFor.add(jRadioButtonOR);

        buttonGroup.add(jRadioButtonCustom);
        jRadioButtonCustom.setText("Custom");
        jRadioButtonCustom.setToolTipText("Create a custom search using AND, OR, NOT operators");
        jPanelSearchFor.add(jRadioButtonCustom);

        jButtonFind.setText("Find");
        jButtonFind.setToolTipText("Start search");
        jButtonFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindActionPerformed(evt);
            }
        });
        jPanelSearchFor.add(jButtonFind);
        
        jTabbedPane.addTab("Search Text", null, jPanelSearchFor, "Specify search terms and how to combine them");

        jPanelSearchInPath.setToolTipText("Restrict the search path");
        jLabelPath.setText("in path");
        jPanelSearchInPath.add(jLabelPath);

        jTextFieldPath.setPreferredSize(new java.awt.Dimension(200, 19));
        jPanelSearchInPath.add(jTextFieldPath);

        jButtonBrowse.setText("Browse");
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        jPanelSearchInPath.add(jButtonBrowse);

        jTabbedPane.addTab("Search Path", null, jPanelSearchInPath, "Restrict search path");

        jPanelSearchMisc.setToolTipText("Specify maximum number of results and specify fields to search in");
        jLabelMaxResults.setText("Max. # results");
        jPanelSearchMisc.add(jLabelMaxResults);

        jTextFieldMaxResults.setText("50");
        jTextFieldMaxResults.setPreferredSize(new java.awt.Dimension(50, 19));
        jTextFieldMaxResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMaxResultsActionPerformed(evt);
            }
        });

        jPanelSearchMisc.add(jTextFieldMaxResults);

        jLabel1.setText("Search in");
        jPanelSearchMisc.add(jLabel1);

        jCheckBoxTitle.setSelected(true);
        jCheckBoxTitle.setText("Title");
        jPanelSearchMisc.add(jCheckBoxTitle);

        jCheckBoxPath.setSelected(true);
        jCheckBoxPath.setText("Path");
        jPanelSearchMisc.add(jCheckBoxPath);

        jCheckBoxDescription.setSelected(true);
        jCheckBoxDescription.setText("Description");
        jPanelSearchMisc.add(jCheckBoxDescription);

        jCheckBoxKeywords.setSelected(true);
        jCheckBoxKeywords.setText("Keywords");
        jPanelSearchMisc.add(jCheckBoxKeywords);

        jCheckBoxContents.setSelected(true);
        jCheckBoxContents.setText("Contents");
        jPanelSearchMisc.add(jCheckBoxContents);

        jTabbedPane.addTab("Other Search Parameters", null, jPanelSearchMisc, "Additional miscellaneous search parameters");

        add(jTabbedPane, java.awt.BorderLayout.NORTH);

        jEditorPaneResults.setEditable(false);
        jEditorPaneResults.setContentType("text/html");
        jScrollPaneResults.setViewportView(jEditorPaneResults);

        add(jScrollPaneResults, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jTextFieldMaxResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMaxResultsActionPerformed
        try {
            if( Integer.parseInt( this.jTextFieldMaxResults.getText() ) <= 0 ) {
                throw new RuntimeException();
            }
        } catch( Exception e ) {
            JOptionPane.showMessageDialog( this, "Please enter an integer > 0!", "Incorrect input value", JOptionPane.ERROR_MESSAGE );
        }
    }//GEN-LAST:event_jTextFieldMaxResultsActionPerformed

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        final JFileChooser fileDialog = new JFileChooser();

        fileDialog.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        if( JFileChooser.APPROVE_OPTION == fileDialog.showOpenDialog( this ) ) {
            this.jTextFieldPath.setText( fileDialog.getSelectedFile().getAbsolutePath() );
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jTextFieldSearchTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSearchTextActionPerformed
        jButtonFindActionPerformed( evt );
    }//GEN-LAST:event_jTextFieldSearchTextActionPerformed

    private void jButtonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindActionPerformed
        // an index must exist before a search can be started
        if( ! this.indexManager.exists() ) {
            JOptionPane.showMessageDialog( this, "Create an index before starting a search!", "An index does not exist", JOptionPane.ERROR_MESSAGE );
        } else {
            // determine maximum number of results and start search
            startSearch( Integer.parseInt( this.jTextFieldMaxResults.getText() ) );
        }
    }//GEN-LAST:event_jButtonFindActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JCheckBox jCheckBoxContents;
    private javax.swing.JCheckBox jCheckBoxDescription;
    private javax.swing.JCheckBox jCheckBoxKeywords;
    private javax.swing.JCheckBox jCheckBoxPath;
    private javax.swing.JCheckBox jCheckBoxTitle;
    private javax.swing.JEditorPane jEditorPaneResults;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelMaxResults;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JLabel jLabelSearch;
    private javax.swing.JPanel jPanelSearchFor;
    private javax.swing.JPanel jPanelSearchInPath;
    private javax.swing.JPanel jPanelSearchMisc;
    private javax.swing.JRadioButton jRadioButtonAND;
    private javax.swing.JRadioButton jRadioButtonCustom;
    private javax.swing.JRadioButton jRadioButtonOR;
    private javax.swing.JScrollPane jScrollPaneResults;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTextField jTextFieldMaxResults;
    private javax.swing.JTextField jTextFieldPath;
    private javax.swing.JTextField jTextFieldSearchText;
    // End of variables declaration//GEN-END:variables
    
}
