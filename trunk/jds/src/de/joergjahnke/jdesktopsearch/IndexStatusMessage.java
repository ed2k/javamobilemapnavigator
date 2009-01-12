/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.jdesktopsearch;


/**
 * Transports the status of an indexing operation
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class IndexStatusMessage {
    /**
     * No special event
     */
    public final static int NO_EVENT = 0;
    /**
     * Event type for starting a long indexing operation like the update of an indexed directory
     */
    public final static int EVENT_LONG_OPERATION_STARTED = 1;
    /**
     * Event type for ending a long indexing operation
     */
    public final static int EVENT_LONG_OPERATION_ENDED = 2;
    /**
     * Event type to reset the status panel
     */
    public final static int EVENT_RESET = 3;
    
    
    /**
     * textual message
     */
    public final String message;
    /**
     * number of files affected
     */
    public final int fileCount;
    /**
     * a special event type e.g. the start of a long operation
     */
    public final int event;
    
    
    /**
     * Creates a new instance of IndexStatusMessage
     *
     * @param   message status message text
     * @param   fileCount   number of modified files
     * @param   event   special event, e.g. EVENT_LONG_OPERATION_STARTED
     */
    public IndexStatusMessage( final String message, final int fileCount, final int event ) {
        this.message = message;
        this.fileCount = fileCount;
        this.event = event;
    }
    
    /**
     * Creates a new instance of IndexStatusMessage
     *
     * @param   message status message text
     * @param   fileCount   number of modified files
     */
    public IndexStatusMessage( final String message, final int fileCount ) {
        this( message, fileCount, NO_EVENT );
    }
    
    /**
     * Creates a new instance of IndexStatusMessage
     *
     * @param   message status message text
     */
    public IndexStatusMessage( final String message ) {
        this( message, 0 );
    }
    
    /**
     * Creates a new instance of IndexStatusMessage
     *
     * @param   event   special event, e.g. EVENT_LONG_OPERATION_STARTED
     */
    public IndexStatusMessage( final int event ) {
        this( null, 0, event );
    }
}
