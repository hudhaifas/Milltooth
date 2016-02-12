/*
 * @(#)Ringing.java
 *
 * Created on June 19, 2006, 12:41 PM
 */

package jo.mill.caller;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;
import javax.microedition.media.control.VolumeControl;

/**
 *
 * @author  Hudhaifa Shatnmawi
 */
public class Ringing
        extends Canvas {
    
    /**
     * constructor
     */
    public Ringing( Milltooth mill ) {
        this( mill, "" );
        //this.setFullScreenMode( true );
        
    } // END: Ringing constructor
    
    /**
     * constructor
     */
    public Ringing( Milltooth mill, String remoteName ) {
        this.remoteName = remoteName;
        this.mill = mill;
        this.setTitle( "" );
        addCommand( answerCommand );
        addCommand( rejectCommand );
        setCommandListener( mill );
        setupLogo();
        
    } // END: Ringing constructor
    
    public void setupLogo() {
        try {
            logo = Image.createImage( "/icons/Millicon.png" );
            
        } catch (IOException ex) {
            logo = null;
            System.out.println( "can not load Millicon.png" );
            
        } // END: try..catch
        
        try {
            label = Image.createImage( "/icons/label.png" );
            
        } catch (IOException ex) {
            label = logo;
            System.out.println( "can not load label.png" );
            
        } // END: try..catch
        
    } // END: setupLogo
    
    public void start() {
        if ( remoteName != "" )
            startRinging();
        
        ringing = true;
        repaint();
        
    } // END: start
    
    /**
     * paint
     * @param
     */
    public void paint( Graphics g ) {
        if ( ringing ) {
            g.setColor( 0xFFFFFF );
            g.fillRect( 0, 0, getWidth(), getHeight() );
            
            if ( label != null ) {
                int w = ( getWidth() - label.getWidth() ) / 2;
                g.drawImage( label, w, 0, Graphics.TOP | Graphics.LEFT );
                
            } // END: if
            
            g.setColor( 0xAAAAAA );
            g.fillRect( 10, getHeight() - 70, getWidth() - 10, 65 );
            
            g.setColor( 0xEEEEEE );
            g.fillRect( 5, getHeight() - 75, getWidth() - 10, 65 );
            
            g.setColor( 0xEE8822 );
            g.drawRect( 5, getHeight() - 75, getWidth() - 10, 65 );
            
            delay = 1500;
            if ( !clear ) {
                g.setColor( 0xFF0000 );
                g.setFont( Font.getFont( Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE ) );
                g.drawString( remoteName, 20, getHeight() - 45, Graphics.BOTTOM | Graphics.LEFT );
                g.drawString( "Calling...", 20, getHeight() - 30, Graphics.BOTTOM | Graphics.LEFT );
                delay = 500;
                
            } // END: inner if
            clear = !clear;
            
            if ( logo != null ) {
                g.drawImage( logo, getWidth() - 45, getHeight() - 55, Graphics.TOP | Graphics.HCENTER );
                
            } // END: inner if
            try { Thread.sleep( delay ); } catch (InterruptedException ex) {} // END: try..catch
            repaint();
            
        } // END: outer if
        
        if ( receive ) {
            g.setFont( Font.getFont( Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL ) );
            g.drawString( msg, 20, y, Graphics.BOTTOM | Graphics.LEFT );
            y += 20;
        }
        
    } // END: paint
    
    /**
     * Called when a key is pressed.
     * @param
     */
    protected  void keyPressed( int keyCode ) {
    } // END: keyPressed
    
    private void startRinging() {
        Milltooth.display.vibrate( 2000 );
        byte d = 8;
        byte C4 = ToneControl.C4;
        byte D4 = ToneControl.C4 + 2; // a whole step
        byte E4 = ToneControl.C4 + 4; // a major third
        byte G4 = ToneControl.C4 + 7; // a fifth
        byte rest = ToneControl.SILENCE; // eighth-note rest
        
        byte[] mySequence = new byte[] {
            ToneControl.VERSION, 1,
            ToneControl.TEMPO, 30,
            ToneControl.BLOCK_START, 0,
            E4, d, D4, d, C4, d, D4, d, E4, d, E4, d, E4, d, rest, d,
            ToneControl.BLOCK_END, 0,
            ToneControl.PLAY_BLOCK, 0,
            D4, d, D4, d, D4, d, rest, d, E4, d, G4, d, G4, d, rest, d, //play "B" section
            ToneControl.PLAY_BLOCK, 0,                  // content of "A" section
            D4, d, D4, d, E4, d, D4, d, C4, d, rest, d  // play "C" section
        };
        
        try {
            if ( tonePlayer == null ) {
                tonePlayer = Manager.createPlayer( Manager.TONE_DEVICE_LOCATOR );
                tonePlayer.setLoopCount( 5 );
                tonePlayer.realize();
                ToneControl tc = ( ToneControl )tonePlayer.getControl( "javax.microedition.media.control.ToneControl" );
                tc.setSequence( mySequence );
                VolumeControl vc = ( VolumeControl ) tonePlayer.getControl( "javax.microedition.media.control.VolumeControl" );
                vc.setLevel( 100 );
                
            } // END: if
            
            if ( tonePlayer != null && !stopSound ) {
                tonePlayer.start();
                
            } // END: if
            
        } catch ( Exception ex ) {
            // ex.printStackTrace();
            if ( tonePlayer != null ) {
                tonePlayer.close();
                tonePlayer = null;
                
            } // END: if
            
        } // END: try..catch
        //try { Thread.sleep( 1000 ); } catch (InterruptedException ex) {} // END: try..catch
        //mill.endCall();
        
    } // END: startRinging
    
    public void stopRinging() {
        if ( tonePlayer != null ) {
            tonePlayer.close();
            tonePlayer = null;
            
        } // END: if
        ringing = false;
        
    } // END: stopRinging
    
    public void callingCommand() {
        removeCommand( answerCommand );
        removeCommand( rejectCommand );
        addCommand( endCommand );
        
    } // END: callingCommand
    
    public void ringingCommand() {
        removeCommand( endCommand );
        addCommand( answerCommand );
        addCommand( rejectCommand );
        
    } // END: normalCommand
    
    public void setName( String remoteName ) {
        this.remoteName = remoteName;
        
    } // END: setName
    
    // Variables declaration
    protected Milltooth mill;
    protected Player tonePlayer;
    protected boolean stopSound;
    protected boolean ringing;
    protected boolean clear;
    protected long delay;
    protected String remoteName;
    protected Image logo;
    protected Image label;
    public String msg;
    public boolean receive;
    public int y = 20;
    // End of variables declaration
    
    // Static variables declaration
    // End of static variables declaration
    
    // Final variables declaration - do not modify
    protected final Command answerCommand = new Command( "Answer", Command.OK, 0 );
    protected final Command rejectCommand = new Command( "Reject", Command.EXIT,01 );
    protected final Command endCommand = new Command( "End call", Command.CANCEL, 0 );
    // End of final variables declaration
    
} // END: class Ringing