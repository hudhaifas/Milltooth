package jo.mill.caller;

/**
 * Sender thread that send out signal and data to a bluetooth connection.
 * <p>Description: Sender is a Runnable implementation that send signal and data (String)
 *  to connected DataInputStream. Each EndPoint has it own sender thread.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * @author Hudhaifa Shatnawi
 * @version 1.0
 */
import java.io.*;

public class Sender
        implements Runnable {
    // end point that this sender sends data to
    public EndPoint endpt;

    private boolean done = false;

    public Sender() {
    }

    /**
     * set 'done' flag to true, which will exit the while loop
     */
    public void stop() {
        done = true;
    }

    public void run() {
        try {
            DataOutputStream dataout = endpt.con.openDataOutputStream();
            while( !done ) {
                // check to see if there are any message to send.
                // if not, then wait for 5 second
                if ( ! endpt.peekString()  ) {
                    synchronized ( this ) {
                        this.wait( 5000 );
                    }
                }

                // wake up and get next string
                CallPacket s = endpt.getString();

                if ( s != null ) {
                    // if there is a message to send, send it now
                    if ( Milltooth.call ) {

                    } else {
                        dataout.writeInt( s.signal );
                        dataout.writeUTF( s.msg );
                        dataout.flush();
                    }
                }

                if ( s != null && s.signal == MillLayer.SIGNAL_TERMINATE ) {
                    // if the message is a TERMINATE signal, then break the run loop as well
                    stop();
                }

            } // while !done

            // close the output stream
            dataout.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

}