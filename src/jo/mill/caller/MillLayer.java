/*
 * @(#)MillLayer.java
 *
 * Created on May 19, 2006, 12:41 PM
 */

package jo.mill.caller;

import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.*;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.ServiceRecord;

/**
 * This is the main class for handling bluetooth connectivity and
 * device/service discovery process. This class does many things, including
 * - search for bluetooth devices ( query() )
 * - create a local Milltooth server and register it with bluetooth ( run() )
 * - search for remote Milltooth services using searchServices()
 * - handle incoming connection request from remote Milltooth
 * - establish connection to remote Milltooth
 *
 * @author Hudhaifa Shatnawi
 */
public class MillLayer
        implements Runnable {
    
    public MillLayer() { /* do nothing */ } // END: MillLayer constructor
    
    public void init( BTListener callback ) {
        try {
            this.callback = callback;
            
            // initialize the JABWT stack
            localDevice = LocalDevice.getLocalDevice();		// Obtain reference to singleton
            localDevice.setDiscoverable( DiscoveryAgent.GIAC );	// Set Discover mode to GIAC
            agent = localDevice.getDiscoveryAgent();		// Obtain reference to singleton
            this.localName = localDevice.getFriendlyName();
            
            /* start bluetooth server socket
             * see run() for implementation of local Milltooth service
             */
            Thread thread = new Thread( this );
            thread.start();
            
        } catch ( BluetoothStateException e ) {
        } catch ( IOException e ) {} // END: try..catch
        
    } // END: init
    
    /**
     * Implement local Milltooth service.
     */
    public void run() {
        // connection to remote device
        StreamConnection streamConnection = null;
        try {
            /* Create a server connection object, using a
             * Serial Port Profile URL syntax and our specific UUID
             * and set the service name to Milltooth
             */
            server =  ( StreamConnectionNotifier ) Connector.open( "btspp://localhost:" + uuid.toString() + ";name=Milltooth" );
            
            // Retrieve the service record template
            ServiceRecord serviceRecord = localDevice.getRecord( server );
            
            /* set ServiceRecrod ServiceAvailability ( 0x0008 ) attribute to indicate our service is available
             * 0xFF indicate fully available status
             * This operation is optional
             */
            serviceRecord.setAttributeValue( 0x0008, new DataElement( DataElement.U_INT_1, 0xFF ) );
            
            /* Set the Major Service Classes flag in Bluetooth stack.
             * We choose Object Transfer Service
             */
            serviceRecord.setDeviceServiceClasses( SERVICE_TELEPHONY );
            
        } catch ( Exception e ) {} // END: try..catch
        
        while( !done ) {
            try {
                /* start accepting client connection.
                 * This method will block until a client
                 * connected
                 */
                streamConnection = server.acceptAndOpen();
                
                // retrieve the remote device object
                RemoteDevice rdev = RemoteDevice.getRemoteDevice( streamConnection );
                /*
                 * check to see if the EndPoint already exist
                 */
                EndPoint endpt = findEndPointByRemoteDevice( rdev );
                if ( endpt == null ) {
                    /* - create a new EndPoint object
                     * - initialize the member variables
                     * - start the data reader and sender threads.
                     */
                    endpt = new EndPoint( this, rdev, streamConnection );
                    
                    Thread t1 = new Thread( endpt.sender );
                    t1.start();
                    
                    Thread t2 = new Thread( endpt.reader );
                    t2.start();
                    
                    // add this EndPoint to the active list
                    endPoints.addElement( endpt );
                    
                } // END: if..else
                
            } catch ( IOException e ) {
                e.printStackTrace();
                // if any exception happen, we assume this connection is
                // failed and close it. closing the connection will cause
                // the reader and sender thread to exit ( because they will got
                // exception as well ).
                if ( streamConnection != null )
                    try { streamConnection.close(); } catch ( IOException e2 ) { /* ignore */ } // END: try..catch
                
            } finally {
                // nothing to do here
            } // END: try..catch..fainally
            
        } // END: while
        
    } // END: run
    
    public void disconnect() {
        // stop server socket, not longer accept client connection
        done = true;
        /* this close will interrupt server.acceptAndOpen()
         * wake it up to exit
         */
        try { server.close(); } catch ( IOException ex ) {} // END: try..catch
        
        /* stop each EndPoint reader and sender threads
         * and send TERMINATE signal to other connected
         * Milltooth peers
         */
        for ( int i=0; i < endPoints.size(); i++ ) {
            EndPoint endpt = ( EndPoint ) endPoints.elementAt( i );
            endpt.putString( MillLayer.SIGNAL_TERMINATE, "end" );
            endpt.sender.stop();
            endpt.reader.stop();
            
        } // END: for
        
    } // END: disconnect
    
    public void query() {
        /* although JSR-82 provides the ability to lookup
         * cached and preknown devices, we intentionally by-pass
         * them and go to discovery mode directly.
         * this allow us to retrieve the latest active Milltooth parties
         */        
        try { agent.startInquiry( DiscoveryAgent.GIAC, new Listener() ); } catch ( BluetoothStateException e ) {} // END: try..catch
        
    } // END: query
    
    public EndPoint findEndPointByRemoteDevice( RemoteDevice rdev ) {
        for ( int i=0; i < endPoints.size(); i++ ) {
            EndPoint endpt = ( EndPoint ) endPoints.elementAt( i );
            if ( endpt.remoteDev.equals( rdev ) )
                return endpt;
            
        } // END: for
        return null; // not found, return null
        
    } // END: findEndPointByRemoteDevice
    
    public EndPoint findEndPointByTransId( int id ) {
        for ( int i=0; i < pendingEndPoints.size(); i++ ) {
            EndPoint endpt = ( EndPoint ) pendingEndPoints.elementAt( i );
            if ( endpt.transId == id )
                return endpt;
            
        } // END: for
        return null; // not found, return null
        
    } // END: findEndPointByTransId
    
    /**
     * Send a string message to all active EndPoints
     * @param s
     */
    public void sendString( String s ) {
        for ( int i=0; i < endPoints.size(); i++ ) {
            EndPoint endpt = ( EndPoint ) endPoints.elementAt( i );
            // put the string on EndPoint, so sender will send the message
            endpt.putString( MillLayer.SIGNAL_MESSAGE, s );
            
        } // END: for
        
    } // END: sendString
    
    /**
     * Clean up the resource for a EndPoint, remove it from the active list.
     * This is triggered by a remote EndPoint leaving the network
     * @param endpt
     */
    public void cleanupRemoteEndPoint( EndPoint endpt ) {
        // set 'done' flag to true to exit the run loop
        endpt.reader.stop();
        endpt.sender.stop();
        
        // remove this end point from the active end point list
        endPoints.removeElement( endpt );
        
    } // END: cleanupRemoteEndPoint
    
    /**
     * Internal discovery listener class for handling device & service discovery events.
     * @author Hudhaifa Shatnawi
     * @version 1.0
     */
    class Listener
            implements DiscoveryListener {
        
        /**
         * A device is discovered.
         * Create a EndPoint for the device discovered and put it on the pending list.
         * A service search will happen when all the qualifying devices are discovered.
         *
         * @param remoteDevice
         * @param deviceClass
         */
        public void deviceDiscovered( RemoteDevice remoteDevice, DeviceClass deviceClass ) {
            /* only device of SERVICE_OBJECT_TRANSFER will be considered as candidate device
             * because in our Milltooth service, we explicitly set the service class to
             * SERVICE_OBJECT_TRANSFER. see the run() method
             */
            try {
                // create a inactive EndPoint and put it on the pending list
                EndPoint endpt = new EndPoint( MillLayer.this, remoteDevice, null );
                pendingEndPoints.addElement( endpt );
                
            } catch ( Exception e ) {} // END: try..catch
            
        } // END: deviceDiscovered
        
        /**
         * device discovery completed.
         * After device inquery completed, we start to search for Milltooth services.
         * We loop through all the pending EndPoints and request agent.searchServices
         * on each of the remote device.
         * @param transId
         */
        public void inquiryCompleted( int transId ) {
            /* wait 100ms and start doing service discovery
             * the choice of 100ms is really just a guess
             */
            timer.schedule( new DoServiceDiscovery(), 100 );
            
        } // END: inquiryCompleted
        
        /**
         * a service is discovered from a remote device.
         * when a Milltooth service is discovered, we establish a connection to
         * this service. This signal joining the existing virtual chat room.
         * @param transId
         * @param svcRec
         */
        public void servicesDiscovered( int transId, ServiceRecord[] svcRec ) {
            try {                
                for ( int i=0; i< svcRec.length; i++ ) {
                    EndPoint endpt = findEndPointByTransId( transId );
                    serviceRecordToEndPoint.put( svcRec[i], endpt );
                    
                } // END: for
                
            } catch ( Exception e ) {} // END: try..catch
            
        } // END: servicesDiscovered
        
        /**
         * service discovery is completed.
         * @param int0
         * @param int1
         */
        public void serviceSearchCompleted( int transID, int respCode ) {
            for ( Enumeration records = serviceRecordToEndPoint.keys(); records.hasMoreElements(); ) {
                try {
                    ServiceRecord rec = ( ServiceRecord ) records.nextElement();
                    
                    /* We make an assumption that the first service is Milltooth. In fact, only one
                     * service record will be found on each device.
                     * Note: we know the found service is Milltooth service because we search on specific UUID,
                     * this UUID is unique to us.
                     */
                    String url  = rec.getConnectionURL( ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false );
                    StreamConnection con = ( StreamConnection ) Connector.open( url );
                    
                    /* retrieve the pending EndPoint and initialize the necessary member variables
                     * to activate the EndPoint. this includes
                     * - initialize connection
                     * - start sender and reader thread
                     */
                    EndPoint endpt = ( EndPoint ) serviceRecordToEndPoint.get( rec );
                    if ( endpt != null ) {
                        endpt.con = con;
                        
                        
                        Thread t1 = new Thread( endpt.sender );
                        t1.start();
                        
                        Thread t2 = new Thread( endpt.reader );
                        t2.start();
                        
                        endPoints.addElement( endpt );
                        
                        /* once a EndPoint established, the Milltooth client is responsible to initiate the
                         * handshake protocol.
                         */
                        endpt.putString( MillLayer.SIGNAL_HANDSHAKE, localName );
                        
                    } // END: if
                    
                } catch ( Exception e ) {} // END: try..catch
                
            } // END: for
            
            /* finished process current batch of service record
             * clear it and service discovery on next device
             */
            serviceRecordToEndPoint.clear();
            
            synchronized ( lock ) {
                /* unlock to proceed to service search on next device
                 * see DoServiceDiscovery.run()
                 */
                lock.notifyAll();
                
            } // END: synchronized
            
        } // END: serviceSearchCompleted
        
    } // inner class Listener
    
    /**
     */
    class DoServiceDiscovery
            extends TimerTask {
        
        public void run() {
            // for each EndPoint, we search for Milltooth service
            for ( int i = 0; i < pendingEndPoints.size(); i++ ) {
                
                EndPoint endpt = ( EndPoint ) pendingEndPoints.elementAt( i );
                
                try {
                    /* searchServices return a transaction id, which we will used to
                     * identify which remote device the service is found in our callback
                     * listener ( class Listener )
                     * 
                     * note: in theory, only one runtine instance of Listener is needed
                     * to handle all discovery callback. however, there is a bug in rococo
                     * simualtor that cause callback fails with one instance of used
                     * so we make a new Listener for every searchServices()
                     */
                    endpt.transId = agent.searchServices(
                            null, // null to indicate retrieve default attributes
                            new UUID[] { uuid },  // Milltooth service UUID SerialPort
                            endpt.remoteDev,
                            new Listener() );
                    
                    /* wait until the above service discovery is completed
                     * because N6600 cannot handle more than one service discovery
                     * request at the same time
                     * see serviceSearchCompleted()
                     */
                    synchronized ( lock ) {
                        try { lock.wait(); } catch ( InterruptedException ex ) {} // END: try..catch
                        
                    } // END: synchronized
                    
                } catch ( BluetoothStateException e ) {} // END: try..catch
                
            } // END: for
            
            /* no more service to discovery. so any pending EndPoints
             * will be ignored and removed
             */
            pendingEndPoints.removeAllElements();
            
            // this message is to inform user that chatting can start
            Milltooth.instance.gui_log( "", "Ready to start calling" );
            
        } // END: run
        
    } // END: inner class DoServiceDiscovery
    
    /** reference to local bluetooth device singleton */
    protected LocalDevice localDevice = null;
    /** reference to local discovery agent singleton */
    protected DiscoveryAgent agent = null;
    /** local Milltooth service server object */
    protected StreamConnectionNotifier server;
    /** reference to BListener implementation. for Milltooth event callback */
    protected BTListener callback = null;
    protected boolean done = false;
    protected String localName = "";
    
    /**
     * list of active EndPoints. all messages will be sent to all
     * active EndPoints 
     */
    protected Vector endPoints = new Vector();
    
    /** 
     * list of pending EndPoints. this is used to keep track of
     * discovered devices waiting for service discovery. When all the near-by
     * Milltooth service has been discovered, this list will be cleared until the
     * next inquiry
     */
    protected static Vector pendingEndPoints = new Vector();
    
    /**
     * map ServiceRecord to EndPoint
     * see DoServiceDiscovery and serviceSearchCompleted
     */
    protected Hashtable serviceRecordToEndPoint = new Hashtable();
    
    /**
     * synchronization lock
     * see DoServiceDiscovery and serviceSearchCompleted
     */
    protected Object lock = new Object();
    
    /**
     * timer to schedule task to do service discovery
     * see inquiryCompleted
     **/
    protected Timer timer = new Timer();
    
    /** 
     * Milltooth specific service UUID
     * note: this UUID must be a string of 32 char
     * do not use the 0x???? constructor because it won't
     * work. not sure if it is a N6600 bug or not
     */
    protected final static UUID uuid = new UUID( "102030405060708090A0B0C0D0E0F010", false );
    
    /** major service class as SERVICE_TELEPHONY */
    protected final static int SERVICE_TELEPHONY = 0x400000;
    
    public final static int SIGNAL_HANDSHAKE = 0;
    public final static int SIGNAL_MESSAGE = 1;
    public final static int SIGNAL_TERMINATE = 3;
    public final static int SIGNAL_HANDSHAKE_ACK = 4;
    public final static int SIGNAL_TERMINATE_ACK = 5;
    
} // END: class NetLayer