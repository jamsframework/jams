/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.jams.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

/**
 *
 * @author Christian Fischer
 */
public class SerializableBufferedWriter implements Serializable{

    transient private BufferedWriter writer = null;
    public SerializableBufferedWriter(Writer s){
        writer = new BufferedWriter(s);
    }
    
    public void close() throws IOException{
        if (writer!=null)
            writer.close();
    }
    public void flush() throws IOException{
        if (writer!=null)
            writer.flush();
    }
    
    public void	newLine() throws IOException{
        if (writer!=null)
            writer.newLine();
    }
             
    public void write(char[] cbuf, int off, int len) throws IOException{
       if (writer!=null)
            writer.write(cbuf,off,len);
    }

    public void write(int c) throws IOException{
       if (writer!=null)
            writer.write(c);
    }
 
    public void write(String s, int off, int len) throws IOException{
        if (writer!=null)
            writer.write(s, off, len);
    }   
    
    public void	write(String str)throws IOException{
        if (writer!=null)
            writer.write(str);
    }
  
}
