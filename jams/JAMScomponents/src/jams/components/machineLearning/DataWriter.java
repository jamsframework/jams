/*
 * DataWriter.java
 *
 * Created on 5. Juli 2007, 12:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;
import java.io.*;
import jams.io.SerializableBufferedWriter;

/**
 *
 * @author Christian(web)
 */
public class DataWriter {
    
    /** Creates a new instance of DataWriter */
    public DataWriter() {
    }
    
    public void write(double data[][],String file) {
	SerializableBufferedWriter writer = null;
	try {
	    writer = new SerializableBufferedWriter(new FileWriter(file,true));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<data.length;i++) {
	    String line = "";
	    for (int j=0;j<data[i].length;j++) {
		line += data[i][j] + "\t";
	    }
	    line += "\n";
	    try {
		writer.write(line);		    
		writer.flush();
	    }
	    catch(Exception e) {
		System.out.println("could not write, because: " + e.toString());
	    }
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    System.out.println("GP - Error" + e.toString());
	}
    }
}
