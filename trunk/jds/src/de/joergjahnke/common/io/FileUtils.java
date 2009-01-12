/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.common.io;

/**
 * File utility methods
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class FileUtils {
    /** Creates a new instance of FileUtils */
    protected FileUtils() {
    }
    
    
    /**
     * Extract the file extension from a filename
     *
     * @param   filename    filename whose extension to determine
     * @return  file extension or an empty string if the filename does not have an extension
     */
    public static String getExtension( String filename ) {
        return filename.indexOf( '.' ) >= 0 ? filename.substring( filename.lastIndexOf( '.' ) ) : "";
    }
}
