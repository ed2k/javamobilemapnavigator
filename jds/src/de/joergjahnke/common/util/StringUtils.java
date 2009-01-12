/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.common.util;


/**
 * String functions.
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class StringUtils {
    /**
     * Get the left part of a string.
     *
     * @param s     string to get the part of
     * @param n     number of characters
     * @return  left <n> characters out of <s>
     */
    public static String left( String s, int n ) {
        return s.substring( 0, Math.max( Math.min( n, s.length() ), 0 ) );
    }

    /**
     * Get the right part of a string.
     *
     * @param s     string to get the part of
     * @param n     number of characters
     * @return  right <n> characters out of <s>
     */
    public static String right( String s, int n ) {
        return s.substring( Math.max( s.length() - n, 0 ) );
    }

    /**
     * Create a string by repeating another string sequence.
     * E.g. repeat( "ab", 3 ) would result in "ababab" .
     *
     * @param   s   string sequence to repeat
     * @param   n   number of repetitions
     * @return  new string which contains "s" "n" times
     */
    public static String repeat( String s, int n ) {
        StringBuffer result = new StringBuffer();

        while( n-- > 0 ) result.append( s );

        return result.toString();
    }


    /**
     * Encode string to XML.
     * This will encode the following raw characters:
     * <ul>
     * <li>&quot; = &amp;quot;</li>
     * <li>&gt; = &amp;gt;</li>
     * <li>&lt; = &amp;lt;</li>
     * <li>&amp; = &amp;amp;</li>
     * </ul>
     *
     * @param   s   string to encode
     * @return  encoded string
     */
    public static String xmlEncode( String s ) {
        return s.replaceAll( "&", "&amp;" ).replaceAll( "\"", "&quot;" ).replaceAll( ">", "&gt;" ).replaceAll( "<", "&lt;" ).replaceAll( "'", "&apos;" );
    }

    /**
     * Decode string from HTML.
     * Replacements from htmlEncode are reversed.
     *
     * @param   s   string to decode
     * @return  decoded string
     * @see de.joergjahnke.common.util.StringUtils#xmlEncode
     */
    public static String xmlDecode( String s ) {
        return s.replaceAll( "&apos", "'" ).replaceAll( "&lt;", "<" ).replaceAll( "&gt;", ">" ).replaceAll( "&quot;", "\"" ).replaceAll( "&amp;", "&" );
    }
    
    /**
     * Join substrings into one string.
     *
     * @param   strings     strings to join
     * @param   delimiter   delimiter between joined strings
     * @return  string containing all substrings delimited by the given delimiter
     */
    public static String join( Object[] strings, String delimiter ) {
        StringBuffer result = new StringBuffer();

        for( int i = 0 ; i < strings.length ; ++i ) {
            result.append( result.length() <= 0 ? "" : delimiter );
            result.append( strings[ i ] );
        }

        return result.toString();
    }
}
