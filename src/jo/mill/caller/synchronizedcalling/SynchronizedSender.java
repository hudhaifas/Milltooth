/*
 * SynchronizedSender.java
 *
 * Created on June 6, 2006, 2:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

import java.util.Random;
import jo.mill.caller.Milltooth;

/**
 *
 * @author Hudhaifa Shatnawi
 */
public class SynchronizedSender
        extends Thread {
    
    /** Creates a new instance of SynchronizedSender */
    public SynchronizedSender( Buffer sharedToRead ) {
        sharedToReadLocation = sharedToRead;
        
    } // END: SynchronizedSender constructor
    
    public static void stoped() {
        done = true;
        System.err.println( "Sender stoped" );
        
    } // END: stoped
    
    public static void started() {
        done = false;
        
    } // END: started
    
    public void run() {
        int count = 0;
        while ( !done ) {
            try {
                Random r = new Random();
                Thread.sleep( 50 + r.nextInt() % 100 );
                
                char[] temp = sharedToReadLocation.get();
                count += temp.length;
                Milltooth.instance.btnet.sendString( new String( temp ) );
                //sharedToWriteLocation.set( temp );
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
        } // END: for
        
        System.err.println( "Sender read values totaling: " + count + "\nTerminating Sender.\n" );
        
    } // END: run
    
    private Buffer sharedToReadLocation;
    protected static boolean done;
    
} // END: class SynchronizedSender