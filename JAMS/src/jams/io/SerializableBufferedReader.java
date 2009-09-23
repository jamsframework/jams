/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.Reader;
import jams.JAMSConstants;

/**
 *
 * @author Christian Fischer
 */
public class SerializableBufferedReader implements Serializable {

    transient private BufferedReader reader = null;

    public SerializableBufferedReader(Reader s) {
        reader = new BufferedReader(s);
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
    
    public void read(char[] cbuf, int off, int len) throws IOException {
        if (reader != null) {
            reader.read(cbuf, off, len);
        }
    }

    public int read() throws IOException {
        if (reader != null) {
            return reader.read();
        }
        throw new IOException(JAMSConstants.resources.getString("reader_not_reader!"));
    }
    
    public String readLine() throws IOException {
        if (reader != null) {
            return reader.readLine();
        }
        throw new IOException(JAMSConstants.resources.getString("reader_not_reader!"));
    }    
}
