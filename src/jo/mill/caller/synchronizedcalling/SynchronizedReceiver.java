/*
 * SynchronizedReceiver.java
 *
 * Created on June 6, 2006, 2:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

import java.util.Random;

/**
 *
 * @author jehad2
 */
public class SynchronizedReceiver
        extends Thread {
    
    /** Creates a new instance of SynchronizedReceiver */
    public SynchronizedReceiver( Buffer  sharedToWrite) {
        sharedToWriteLocation = sharedToWrite;
        
    }
    
    public static void stoped() {
        done = true;
        System.err.println( "Receiver stoped" );
        
    } // END: stoped
    
    public static void started() {
        done = false;
        
    } // END: started
    
    public void run() {
        int count = 0;
        while ( !done ) {
            try {
                Random r = new Random();
                Thread.sleep( r.nextInt() % 50 );
                
                temp = new char[ ( r.nextInt() % 100 ) ];
                if ( temp.length != 0 ) {
                    sharedToWriteLocation.set( temp );
                    count += temp.length;
                    
                } // END: if
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
        } // END: for
        
        System.err.println( "Receiver read values totaling: " + count + "\nTerminating Receiver.\n" );
        
    } // END: run
    
    private Buffer sharedToWriteLocation;
    protected static boolean done;
    public char[] temp;
    
}
