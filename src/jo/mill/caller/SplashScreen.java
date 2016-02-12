/*
 * @(#)SplashScreen.java
 *
 * Created on June 19, 2006, 3:47 PM
 */

package jo.mill.caller;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 *
 * @author  Hudhaifa Shatnmawi
 * @version
 */
public class SplashScreen
        extends Canvas
        implements Runnable {
    
    /**
     * constructor
     */
    public SplashScreen() {
        this.setFullScreenMode( true );
        width = getWidth();
        height = getHeight();
        currentX = - 26 ;
        
    } // END: SplashScreen constructor
    
    public void start() {
        System.err.println( " Inited: SplashScreen thread" );
        // Starting self
        isPlay = true;
        Thread t = new Thread( this );
        t.start();
        
    } // END: start
    
    public void stop() {
        isPlay = false;
        
    } // END: stop
    
    public void run() {
        try { splashScreen = Image.createImage( "/icons/splashScreen.png" ); } catch ( IOException ex ) { splashScreen = null; } // END: try..catch
        try { init  = Image.createImage( "/icons/init.png" );  } catch ( IOException e )  { init = null; } // END: try..catch
        while ( isPlay == true ) {
            if ( currentX < 84 ) {
                currentX = currentX + 2;
                
            } else {
                currentX = - 26;
                
            } // END: if..else
            
            repaint();
            // This will make the thread stop for a short period of time every time the bar has moved. It is to controll the bars speed. Try setting 'delay' to 0 and see what happens.
            try { Thread.sleep( 50 ); } catch ( InterruptedException ie ) {} // END: try..catch
            
        } // END: while
        
    } // END: run
    
    /**
     * paint
     */
    public void paint( Graphics g ) {
        if ( splashScreen != null ) {
            int w = ( getWidth() - splashScreen.getWidth() ) / 2;
            g.drawImage( splashScreen, w, 0, Graphics.TOP | Graphics.LEFT );
            
        } // END: if
        
        // Middle-box ( progress bar )
        g.setColor( 0x627293 );
        g.fillRect( width / 2 - 44, height / 2 - 5, 86, 11 );
        
        g.setColor( 0xffffff );
        g.fillRect( width / 2 - 43, height / 2 - 4, 84, 9 );
        
        /*
         * The moving bar, a set of wierd IF statements to make sure the
         * moving bar doesn't cross over any borders and runs fine
         */
        g.setColor( 0xB0B8C9 );
        
        // If currentX + 26 is above 84 the bar will run over the right border, this means we must reduce it's size
        if ( ( currentX + 26 ) > 84 ) {
            int width2 = ( currentX +26 ) - 84;
            width2 = 26 - width2;
            
            g.fillRect( width / 2 - 44 + currentX,height / 2 - 3,width2,7 );
            
        } else if ( currentX < 1 ) { // If currentX is below 0 we must reduce our barsize otherwise it will be over the left border
            int width3 = 26 + currentX;
            
            if ( ( width / 2 - 44 + currentX + width3 ) < ( width / 2 - 44 ) ) {
                // If statement is true it means that it isn't even vissible, do nothing
            } else {
                // The bar is visible, but runs over the left border.
                // Therefor we calculate how much over the border it is,
                // and adds it to X and removes it from the width.
                
                int leftOver = ( width / 2 - 43 ) - ( width / 2 - 44 + currentX );
                g.fillRect( width / 2 - 43 + currentX + leftOver,height / 2 - 3,width3 - leftOver,7 );
                
            } // END: inner if..else
            
        } else {
            // The bar is running freely crossing no borders
            g.fillRect( width / 2 - 44 + currentX,height / 2 - 3,26,7 );
            
        } // END: outer if..else
        
        g.drawImage( init, width / 2 - 44, height / 2 + 6, Graphics.TOP | Graphics.LEFT );
        
    } // END: paint
    
    // Variables declaration
    protected Image splashScreen;
    protected Image init;
    protected boolean isPlay;           // Loading runs when isPlay is true
    protected int width;                // To hold screen width
    protected int height;               // To hold screen height
    protected int currentX, currentY;   // To hold current position of the 'SplashScreen' text
    // End of variables declaration
    
    // Static variables declaration
    // End of static variables declaration
    
    // Final variables declaration - do not modify
    // End of final variables declaration
    
} // END: class SplashScreen
