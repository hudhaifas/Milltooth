package jo.mill.caller;

/**
 * Reader thread that read in signal and data from a bluetooth connection.
 * <p>Description: Reader is a Runnable implementation that read in signal and data (String)
 *  from connected DataInputStream. Each EndPoint has it own reader thread.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * @author Hudhaifa Shatnawi
 * @version 1.0
 */
import java.io.*;

public class Receiver
        implements Runnable {
    // end point that this reader reads data from
    
    public Receiver() {
    }
    
    /**
     * set 'done' flag to true, which will exit the while loop
     */
    public void stop() {
        done = true;
        
    }
    
    public void run() {
        try {
            DataInputStream datain = endpt.con.openDataInputStream();
            
            while ( !done ) {
                int signal = MillLayer.SIGNAL_TERMINATE;
                try {
                    signal = datain.readInt();
                } catch (EOFException ex) {
                    ex.printStackTrace();
                }
                
                if ( signal == MillLayer.SIGNAL_MESSAGE ) {
                    if ( Milltooth.call ) {
                        
                    } else {
                        String s = datain.readUTF();
                        CallPacket packet = new CallPacket( MillLayer.SIGNAL_MESSAGE, endpt.remoteName, s );
                        
                        // read in a string message. emit RECEIVED event to BTListener implementation
                        endpt.callback.handleAction( BTListener.EVENT_RECEIVED, endpt, packet );
                        
                    } // END: if..else
                    
                    
                } else if ( signal == MillLayer.SIGNAL_HANDSHAKE ) {
                    String s = datain.readUTF();
                    // update the remote user nick name
                    endpt.remoteName = s;
                    
                    // echo acknowledgment and local user friendly name back to remote device
                    endpt.putString( MillLayer.SIGNAL_HANDSHAKE_ACK, endpt.localName );
                    
                    
                    endpt.callback.handleAction( BTListener.EVENT_JOIN, endpt, null );
                    
                } else if ( signal == MillLayer.SIGNAL_TERMINATE ) {
                    // echo acknowledgment and local friendly name back to remote device
                    endpt.putString( MillLayer.SIGNAL_TERMINATE_ACK, "end" );
                    
                    // emit LEAVE event to BTListener implementation
                    endpt.callback.handleAction( BTListener.EVENT_LEAVE, endpt, null );
                    
                    // clean up end point resources and associated connections
                    endpt.btnet.cleanupRemoteEndPoint( endpt );
                    
                    // stop this reader, no need to read any more signal
                    stop();
                    
                } else if ( signal == MillLayer.SIGNAL_HANDSHAKE_ACK ) {
                    // the string data is the remote user nick name
                    String s = datain.readUTF();
                    // update remote user nick name
                    endpt.remoteName = s;
                    
                } else if ( signal == MillLayer.SIGNAL_TERMINATE_ACK ) {
                    
                    System.out.println("read in TERMINATE_ACK from "+endpt.remoteName);
                    // doesn't do anything, just wake up from readInt() so that the thread can stop
                    
                    
                }
                
            } // while !done
            
            datain.close();
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        
    }
    
    public EndPoint endpt;
    private boolean done = false;
    
}