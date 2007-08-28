/*
 * GenericDataWriter.java
 *
 * Created on 6. Oktober 2005, 01:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.tspaces;

import java.io.*;
import java.util.*;
import org.unijena.jams.data.*;
import org.unijena.jams.runtime.RuntimeException;
import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class GenericDataWriter {
    
    private String fileName;
    private Writer writer;
    private StringWriter stringWriter;
    private ArrayList<String> header = new ArrayList<String>();
    private ArrayList<String> comments = new ArrayList<String>();
    private ArrayList<Object> data;
    private boolean headerClosed = false;
    private boolean useStringWriter = false;
    
    public GenericDataWriter() {
    }
    
    public GenericDataWriter(String fileName) {
        setFileName(fileName);
    }
     public GenericDataWriter(String fileName, boolean stringWriter) {
        if (stringWriter)
          useStringWriter = true;
        setFileName(fileName);
        
    }
    private void openFile() {
       
            if (useStringWriter){
               writer = new StringWriter(); 
            }
            else{
              try {  
                 writer = new BufferedWriter(new FileWriter(fileName));
              }   
              catch (IOException ioe) {
                 JAMS.handle(ioe);
              }
            }  
    }
    
    public void flush(){
        try {
            writer.flush();
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
        openFile();
    }
    
    public void addColumn(String name) {
        if (!headerClosed)
            header.add(name);
    }
    
    public void addComment(String comment) {
        if (!headerClosed)
            comments.add("# "+comment);
    }
    
    public void writeHeader() {
        
        Iterator<String> i;
        String s = "";
        
        i = comments.iterator();
        while (i.hasNext()) {
            s += i.next()+"\n";
        }
        
//        String s = "#JAMS output file\n#\n";
        //s += "#";
        i = header.iterator();
        if (i.hasNext())
            s += i.next();
        while (i.hasNext()) {
            s += "\t"+i.next();
        }
        try {
            writer.write(s);
        //    writer.newLine();
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
        headerClosed = true;
        data = new ArrayList<Object>(header.size());
    }
    
    public void addData(Object o) {
        data.add(o);
    }
    
    public void writeData() throws RuntimeException {
        
        String s = "";
        
        if (data.size() != header.size()) {
            throw new RuntimeException("Wrong number of output columns!");
        } else {
            Iterator<Object> i = data.iterator();
            if (i.hasNext())
                s = i.next().toString();
            while (i.hasNext()) {
                s += "\t"+i.next().toString();
            }
            try {
                writer.write(s);
              //  writer.newLine();
            } catch (IOException ioe) {
                JAMS.handle(ioe);
            }
            data.clear();
        }
    }
    
    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
    }
    
    public static void main(String[] args) throws Exception {
        GenericDataWriter w = new GenericDataWriter("e:/java/jams/application/thornthwaite/data/test.out");
        w.addColumn("Time");
        w.addColumn("wert");
        w.writeHeader();
        
        w.addData(new JAMSCalendar());
        w.addData("a");
        w.writeData();
        w.addData(new JAMSCalendar());
        w.addData("a");
        w.writeData();
        
        w.close();
    }
}
