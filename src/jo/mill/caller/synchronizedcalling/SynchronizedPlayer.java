/*
 * SynchronizedPlayer.java
 *
 * Created on June 6, 2006, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

import java.util.Random;
import jo.mill.caller.Milltooth;

/**
 *
 * @author jehad2
 */
public class SynchronizedPlayer
        extends Thread {
    
    /** Creates a new instance of SynchronizedPlayer */
    public SynchronizedPlayer( Buffer shared ) {
        sharedLocation = shared;
        
    } // END: SynchronizedPlayer constructor
    
    public static void stoped() {
        done = true;
        System.err.println( "Player stoped" );
        
    } // END: stoped
    
    public static void started() {
        done = false;
        
    } // END: started
    
    public void run() {
        while ( !done ) {
            try {
                Random r = new Random();
                Thread.sleep( r.nextInt() % 50 );
                played += new String( sharedLocation.get() );
                //Milltooth.instance.calling.setTitle( played );
                //Milltooth.instance.alert( played, null );
                //Calling.played += new String( sharedLocation.get() );
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
            
        } // END: while
        
        System.err.println( "Player read values totaling: " + played + "\nTerminating Player.\n" );
        
    } // END: run
    
    private Buffer sharedLocation;
    private static boolean done;
    private String played = "";
    
} // END: class SynchronizedPlayer