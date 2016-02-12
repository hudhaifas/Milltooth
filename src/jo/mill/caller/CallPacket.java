package jo.mill.caller;

/**
 * A holder object for BlueChat network packet data.
 * <p>Description: CallPacket can represent severl type of message, which is defined
 * by NetLayer.SIGNAL_XXX enumeration. The common type is SIGNAL_MESSAGE, which
 * hold an user entered message to sent across the virtual chat room.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * 
 * @author Hudhaifa Shatnawi
 * @version 1.0
 */

public class CallPacket {

  public CallPacket() {
  } // END: CallPacket constructor

  public CallPacket( int signal, String msg ) {
    this.signal = signal;
    this.msg = msg;

  } // END: CallPacket constructor

  public CallPacket( int signal, String sender, String msg ) {
    this.signal = signal;
    this.sender = sender;
    this.msg = msg;

  } // END: CallPacket constructor
  
  // signal, must be one of NetLayer.SIGNAL_XXX
  public int signal;
  // indicate the nick name of the sender
  public String sender;
  // the message content
  public String msg;

} // END: class CallPacket