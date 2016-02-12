/*
 * SynchronizedRecorder.java
 *
 * Created on June 6, 2006, 2:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

import java.util.Random;
import java.util.Timer;

/**
 *
 * @author jehad2
 */
public class SynchronizedRecorder
        extends Thread {
    
    /** Creates a new instance of SynchronizedRecorder */
    public SynchronizedRecorder( Buffer shared ) {
        sharedLocation = shared;
        timer = new Timer();
        done = false;
        
    }
    
    public static void stoped() {
        done = true;
        System.err.println( "Recorder stoped" );
        
    } // END: stoped
    
    public void run() {
        while ( !done ) {
            try {
                final Random r = new Random();
                Thread.sleep( 50 + r.nextInt() % 100 );
//                timer.schedule(
//                        new TimerTask() {
//                    public void run() {
                int temp = count + 10;//( count + ( int ) r.nextInt() % 100 );
                temp = ( temp >= recorded.length() - 1 ? ( recorded.length() - 2 ) : temp );
                toSend = "" + recorded.substring( count, temp );
                count = temp;
                sharedLocation.set( toSend.toCharArray() );
                
                
//                    } // END: run
                
//                }, 300 );
                            
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: trycatch
            
        } // END: while
        
        System.err.println( "Recorder done producing." + "\nTerminating Recorder.\n" );
        
    } // END: run
    
    private Buffer sharedLocation;
    private static boolean done;
    private Timer timer;
    private final String recorded = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private String toSend = "";
    private int count = 0;
    
}