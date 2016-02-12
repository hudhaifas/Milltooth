package jo.mill.caller;

import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Screen;
import javax.microedition.midlet.MIDlet;
import jo.mill.caller.synchronizedcalling.PlayReceiveBuffer;
import jo.mill.caller.synchronizedcalling.SendRecordBuffer;
import jo.mill.caller.synchronizedcalling.SynchronizedPlayer;
import jo.mill.caller.synchronizedcalling.SynchronizedReceiver;
import jo.mill.caller.synchronizedcalling.SynchronizedRecorder;
import jo.mill.caller.synchronizedcalling.SynchronizedSender;

/**
 * Main MIDlet class that execute Milltooth application.
 * <p>Description: </p>
 * <p>Copyright: Copyright ( c ) 2003</p>
 *
 * @author Hudhaifa Shatnawi
 * @version 1.0
 */
public class Milltooth
        extends MIDlet
        implements BTListener, CommandListener {
    
    /** Constructor */
    public Milltooth() {
        instance = this;
        firstTime = true;
        splashScreen = new SplashScreen();
        splashScreen.start();
        
    } // END: Milltooth constructor
    
    /** Handle starting the MIDlet */
    public void startApp() {
        if ( firstTime ) {
            try {
                // initialize the GUI component, and prompt for name input
                catchedDevices = new CatchedDevices();
                ringing = new Ringing( this );
                // user enters virtual chat room.
                // create and initialize Bluetooth network layer
                btnet = new MillLayer();
                // initialize the network layer. This will start the local Milltooth server
                btnet.init( this );
                // search for existing Milltooth nodes
                btnet.query();
                display = Display.getDisplay( this );
                display.setCurrent( splashScreen );
                
                catchedDevices.showDevices();
                // switch screen to message screen
                Thread.sleep( 17000 );
                splashScreen.stop();
                splashScreen = null;
                System.gc();
                display.setCurrent( catchedDevices );
                
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                
            } // END: try..catch
            firstTime = false;
            
        } // END: if
        // obtain reference to Display singleton
        
    } // END: startApp
    
    /** Handle pausing the MIDlet */
    public void pauseApp() {
    } // END: pauseApp
    
    /** Handle destroying the MIDlet */
    public void destroyApp( boolean unconditional ) {
    } // END: destroyApp
    
    /** Quit the MIDlet */
    public void quitApp() {
        instance = null;
        display = null;
        ringing = null;
        devices = null;
        catchedDevices = null;
        btnet = null;
        output = null;
        played = null;
        System.gc();
        destroyApp( true );
        notifyDestroyed();
        
    } // END: quitApp
    
    /**
     * Handle event/activity from Bluetooth Network layer.
     * This class is an implementation of BTListener; therefore, it handles
     * all the bluetooth network event that received by MillLayer.
     * The list of possible event are defined in BTListener.EVENT_XXX.
     *
     * @param action event type. see MillLayer constants
     * @param param1 parameter 1 is usually the remote EndPoint that trigger the action
     * @param param2 parameter 2 is usually the argument of the action
     */
    public void handleAction( String event, Object param1, Object param2 ) {
        if ( event.equals( BTListener.EVENT_JOIN ) ) {
            // a new user has join the chat room
            EndPoint endpt = ( EndPoint ) param1;
            String msg = endpt.remoteName;
            CallPacket packet = new CallPacket( MillLayer.SIGNAL_HANDSHAKE, endpt.remoteName, msg );
            
            // display the join message on screen
            catchedDevices.msgs.addElement( packet );
            catchedDevices.setMessage();
            devices.addElement( endpt.remoteDev );
            catchedDevices.showDevices();
            
        } else if ( event.equals( BTListener.EVENT_SENT ) ) {
            // nothing to do
            
        } else if ( event.equals( BTListener.EVENT_RECEIVED ) ) {
            // a new message has received from a remote user
            EndPoint endpt = ( EndPoint ) param1;
            CallPacket msg = ( CallPacket ) param2;
            if ( msg.msg.equals( "ANSWERED" ) ) {
                startCall();
                ringing.callingCommand();
                display.setCurrent( ringing );
                
            } else if ( msg.msg.equals( "REJECTED" ) ) {
                ringing.stopRinging();
                display.setCurrent( catchedDevices );
                
            } else if ( msg.msg.equals( "ENDCALL" ) ) {
                ringing.stopRinging();
                ringing.ringingCommand();
                display.setCurrent( catchedDevices );
                endCall();
                
            } else if ( msg.msg.equals( "CALLING" ) ) {
                display.setCurrent( ringing );
                // beep
                ringing.setName( msg.sender );
                ringing.start();
                
            } else {
                //synchronized ( receiver ) {
                //receiver.temp = msg.msg.toCharArray();
                ringing.msg = new String( msg.msg.toCharArray() );
                ringing.receive = true;
                ringing.repaint();
                System.out.println( "--> Received: " + msg.msg );
                
                //} // END: synchronized
                
            } // END: if..else
            
        } else if ( event.equals( BTListener.EVENT_LEAVE ) ) {
            // a user has leave the chat room
            EndPoint endpt = ( EndPoint ) param1;
            String msg = endpt.remoteName;
            CallPacket packet = new CallPacket( MillLayer.SIGNAL_TERMINATE, endpt.remoteName, msg );
            // display the leave message on screen
            catchedDevices.msgs.addElement( packet );
            catchedDevices.setMessage();
            devices.removeElement( endpt.remoteDev );
            catchedDevices.showDevices();
            
        } // END: if..else
        catchedDevices.showDevices();
        
    } // END: handleAction
    
    /**
     * Handle user action from Milltooth application.
     *
     * @param c GUI command
     * @param d GUI display object
     */
    public void commandAction( Command c, Displayable d ) {
        if ( c.getLabel().equals( "Call" ) ) {
            display.setCurrent( ringing );
            ringing.setName( "" );
            ringing.callingCommand();
            ringing.start();
            // send + message to all connected Milltooth remote EndPoints
            btnet.sendString( "CALLING" );
            
        } else if ( c.getLabel().equals( "Exit" ) ) {
            // disconnect from the virtual chat room.
            // this will send out TERMINATE signal to all connected
            // remote EndPoints, wait for the TERMINATE_ACK signal, and
            // disconnect all connections.
            btnet.disconnect();
            quitApp();
            
        } else if ( c.getLabel().equals( "About Milltooth" ) ) {
            Alert alert = new Alert( "About", "Milltooth By Hudhaifa Shatnawi (c) 2006.\nfor more information\nVisit www.hudhaifa.mine.nu.", null, AlertType.INFO );
            alert.setTimeout( Alert.FOREVER );
            display.setCurrent( alert, catchedDevices );
            
        } else if ( c.getLabel().equals( "Answer" ) ) {
            ringing.stopRinging();
            startCall();
            btnet.sendString( "ANSWERED" );
            ringing.callingCommand();
            
        } else if ( c.getLabel().equals( "Reject" ) ) {
            ringing.stopRinging();
            btnet.sendString( "REJECTED" );
            display.setCurrent( catchedDevices );
            
        } else if ( c.getLabel().equals( "End call" ) ) {
            btnet.sendString( "ENDCALL" );
            ringing.ringingCommand();
            ringing.stopRinging();
            display.setCurrent( catchedDevices );
            endCall();
            
        } // END: if..else
        catchedDevices.showDevices();
        
    } // END: commandAction
    
    public static void gui_log( String source, String s ) {
        CallPacket packet = new CallPacket( MillLayer.SIGNAL_MESSAGE, source,  s );
        instance.catchedDevices.msgs.addElement( packet );
        instance.catchedDevices.setMessage();
        
    } // END: gui_log
    
    public void alert( String m, Screen nextScreen ) {
        Alert alert = new Alert( "Milltooth", m, null, AlertType.ALARM );
        alert.setTimeout( Alert.FOREVER );
        display.setCurrent( alert, nextScreen );
        
    } // END: alert
    
    public void startCall() {
        output = "";
        played = "";
        
        SendRecordBuffer sharedRecorded = new SendRecordBuffer();
        recorder = new SynchronizedRecorder( sharedRecorded );
        sender = new SynchronizedSender( sharedRecorded );
        
        recorder.start();
        sender.start();
        
        //PlayReceiveBuffer sharedReceived = new PlayReceiveBuffer();
        //receiver = new SynchronizedReceiver( sharedReceived );
        //player = new SynchronizedPlayer( sharedReceived );
        
        //receiver.start();
        //player.start();
        
    } // END: calling
    
    public void endCall() {
        recorder.stoped();
        sender.stoped();
        //receiver.stoped();
        //player.stoped();
        
        sharedRecorded = null;
        recorder = null;
        sender = null;
        
        //sharedReceived = null;
        receiver = null;
        player = null;
        
        // finalize non-used objects
        System.gc();
        
    } // END: endCall
    
    // Variables declaration
    protected CatchedDevices catchedDevices;
    protected SplashScreen splashScreen;
    protected boolean firstTime;
    protected SendRecordBuffer sharedRecorded;
    protected SynchronizedRecorder recorder;
    protected SynchronizedSender sender;
    protected PlayReceiveBuffer sharedReceived;
    protected SynchronizedReceiver receiver;
    protected SynchronizedPlayer player;
    public Ringing ringing;
    public MillLayer btnet;
    // End of variables declaration
    
    // Static variables declaration
    protected static Display display;
    protected static Vector devices = new Vector();
    protected static String output;
    protected static String played;
    public static Milltooth instance;
    public static boolean call;
    // End of static variables declaration
    
} // END: class Millcaller