/*
 * Buffer.java
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
public interface Buffer {
    public void set( char[] value );
    public char[] get();
    
} // END: interface Buffer