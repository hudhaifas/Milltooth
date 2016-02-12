package jo.mill.caller;

import java.util.Vector;
import javax.bluetooth.RemoteDevice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

/**
 * A screen to display current messages in the BlueChat virtual chat room.
 * <p>Description: This is a canvas screen to display the current messages in
 * virtual chat room. Only the latest messages are displayed. If there are  more
 * messages than those can fit into one screen, old messages are roll off from
 * the upper edge. User is not able to scroll back to see old messages, however,
 * the old messages is still available in msgs Vector until a clear command
 * is invoked. When a clear command is invoked, all message will be removed
 * from msgs vector. </p>
 * <p>Copyright: Copyright ( c ) 2003</p>
 * @author Hudhaifa Shatnawi
 * @version 1.0
 */
public class CatchedDevices
        extends List {
    
    public CatchedDevices() {
        super( "Catched Devices", List.IMPLICIT );
        addCommand( new Command( "Discover", Command.SCREEN, 1 ) );
        addCommand( new Command( "Call", Command.SCREEN, 2 ) );
        addCommand( new Command( "About Milltooth", Command.SCREEN, 3 ) );
        addCommand( new Command( "Exit", Command.SCREEN, 4 ) );
        setCommandListener( Milltooth.instance );
        
    } // END: CatchedDevices constructor
    
    public void setMessage() {
        super.deleteAll();
        for ( int i= 0; i < msgs.size(); i++ ) {
            CallPacket p = ( CallPacket ) msgs.elementAt( i );
            String s = p.sender + ": " + p.msg;
            append( s, null );
            
        } // END: for
        
    } // END: setMsg
    
    public void showDevices() {
        // clear the screen
        super.deleteAll();
        
        // for each devices on the list, show the friend name
        if ( Milltooth.devices.size() > 0 ) {
            for ( int i = 0; i < Milltooth.devices.size(); i++ ) {
                try {
                    RemoteDevice device = ( RemoteDevice ) Milltooth.devices.elementAt( i );
                    // we put false, meaning don't contact the remote device for the name
                    String name = device.getFriendlyName( false );
                    append( name, null );
                    
                } catch ( Exception e ) {
                    e.printStackTrace();
                    
                } // END: try..catch
                
            } // END: for
            
        } else {
            // no device on list, tell user to press command
            append( "[Press Discover Devices]", null );
            
        } // END: if..else
        
    } // END: showDevices
    
    Vector msgs = new Vector();
    
} // END: class MessageUI