/*
 * PlayReceiveBuffer.java
 *
 * Created on June 6, 2006, 2:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

/**
 *
 * @author jehad2
 */
public class PlayReceiveBuffer
        implements Buffer {
    
    /** Creates a new instance of PlayReceiveBuffer */
    public PlayReceiveBuffer() {
        buffer = new char[ 100 ];
        received = 0;
        played = 0;
        
    } // END: PlayReceiveBuffer constructor
    
    public synchronized void set( char[] value ) {
        ready = value.length;
        
        while ( ( received + ready ) >= ( played ) && loop ) {
            try {
                System.err.println( "<--Receiver tries to write." );
                displayState( "<--Receiver", "waits; Buffer full." );
                wait();
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
            
        } // END: while
        
        int i, j;
        for ( i = received, j = 0; j < value.length; i++, j++ ) {
            if ( i == buffer.length ) {
                i = 0;
                loop = !loop;
                
            } // END: if
            
            buffer[ i ] = value[ j ];
            
        } // END: for
        
        recCount += value.length;
        displayState( "<--Receiver", "writes buffer[ " + recCount + " : " + received + " >> " + ( received + value.length ) + " ] = " + new String( value ) );
        received = i;//( received == buffer.length - 1 ) ? 0 : ( received + value.length );
        notify();
        
    } // END: set
    
    public synchronized char[] get(){
        while ( ( played + ready ) >= received && !loop )  {
            try {
                System.err.println( "-->Player tries to read." );
                displayState( "-->Player", "waits; Buffer empty." );
                wait();
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
            
        } // END: while
        
        char[] toReturn = new char[ ready ];
        int i, j;
        for ( i = played, j = 0; j < ready; i++, j++ ) {
            if ( i == buffer.length ) {
                i = 0;
                loop = !loop;
                
            } // END: if
            
            toReturn[ j ] = buffer[ i ];
            
        } // END: for
        
        plaCount += toReturn.length;
        //Millcaller.played += new String( toReturn );
        displayState( "-->Player", "reads buffer[ " + plaCount + " : " + played + " >> " + ( played + toReturn.length ) + " ] = " + new String( toReturn ) );
        notify();
        played = i;//( played == buffer.length - 1 ) ? 0 : ( played + toReturn.length );
        return toReturn;
        
    } // END: get
    
    public void displayState( String auth, String s ){
        System.err.println( auth + "\t: " + s + "\n" );
        
    } // END: displayState
    
    private char[] buffer;
    private int received, played, recCount, plaCount;
    private int ready;
    private boolean loop;
    
} // END: class PlayReceiveBuffer