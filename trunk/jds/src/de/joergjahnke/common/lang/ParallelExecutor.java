/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation. For the full
 * license text, see http://www.gnu.org/licenses/lgpl.html.
 */
package de.joergjahnke.common.lang;


/**
 * Executes a program using a set of parallel threads
 *
 * @author Jörg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class ParallelExecutor implements Runnable {
    // program to run
    private final Runnable program;
    // number of threads to use
    private final int numThreads;


    /**
     * Runs a given block of code multiple time with a set of parallel threads
     *
     * @param   program code to execute
     * @param   numThreads  threads to use
     */
    public ParallelExecutor( final Runnable program, final int numThreads ) {
        this.program = program;
        this.numThreads = numThreads;
    }


    public void run() {
        // start program multiple times
        final Thread[] threads = new Thread[ numThreads ];

        for( int i = 0 ; i < numThreads ; ++i ) {
            threads[ i ] = new Thread( program );
            threads[ i ].start();
        }

        // wait for last thread to finish
        for( final Thread thread : threads ) {
            try {
                thread.join();
            } catch( InterruptedException e ) {
                // we were interrupted, that's OK, we just stop
            }
        }
    }
}
