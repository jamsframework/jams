/*
 * SerializableBufferedWriter.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.io;

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
