/*
 * SendRecordBuffer.java
 *
 * Created on June 6, 2006, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jo.mill.caller.synchronizedcalling;

/**
 *
 * @author jehad2
 */
public class SendRecordBuffer
        implements Buffer {
    /** Creates a new instance of SendRecordBuffer */
    public SendRecordBuffer() {
        buffer = new char[ 50 ];
        recorded = 0;
        sent = 0;
        loop = false;
        
    } // END: PlayReceiveBuffer constructor
    
    public synchronized void set( char[] value ) {
        ready = value.length;
        
        while ( ( recorded + ready ) >= ( sent ) && loop ) {
            try {
                System.err.println( "<--Recorder tries to write." );
                displayState( "<--Recorder", "waits; Buffer full." );
                wait();
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
            
        } // END: while
        
        int i, j;
        for ( i = recorded, j = 0; j < value.length; i++, j++ ) {
            if ( i == buffer.length ) {
                i = 0;
                loop = !loop;
                
            } // END: if
            
            buffer[ i ] = value[ j ];
            
        } // END: for
        
        recCount += value.length;
        displayState( "<--Recorder", "writes buffer[ " + recCount + " : " + recorded + " >> " + ( recorded + value.length ) + " ] = " + new String( value ) );
        recorded = i;//( recorded == buffer.length - 1 ) ? 0 : ( recorded + value.length );
        notify();
        
    } // END: set
    
    public synchronized char[] get(){
        while ( ( sent + ready ) >= recorded && !loop )  {
            try {
                System.err.println( "-->Sender tries to read." );
                displayState( "-->Sender", "waits; Buffer empty." );
                wait();
                
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
                
            } // END: try..catch
            
        } // END: while
        
        char[] toReturn = new char[ ready ];
        int i, j;
        for ( i = sent, j = 0; j < ready; i++, j++ ) {
            if ( i == buffer.length ) {
                i = 0;
                loop = !loop;
                
            } // END: if
            
            toReturn[ j ] = buffer[ i ];
            
        } // END: for
        
        senCount += toReturn.length;
        displayState( "-->Sender", "reads buffer[ " + senCount + " : " + sent + " >> " + ( sent + toReturn.length ) + " ] = " + new String( toReturn ) );
        notify();
        sent = i;//( sent == buffer.length - 1 ) ? 0 : ( sent + toReturn.length );
        return toReturn;
        
    } // END: get
    
    public void displayState( String auth, String s ){
        System.err.println( auth + "\t: " + s + "\n" );
        
    } // END: displayState
    
    private char[] buffer;
    private int recorded, sent, recCount, senCount;
    private int ready;
    private boolean loop;
    
}