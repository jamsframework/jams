/*
 * GenericDataReader.java
 *
 * Created on 04. October 2005, 01:49
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

import java.util.*;
import java.io.*;
import jams.tools.JAMSTools;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */

public class GenericDataReader implements JAMSTableDataStore, Serializable {
    
    BufferedReader reader;
    String fileName;
    boolean timeParse;
    String nextString = "";
    boolean active = false;
    private String[] metadata;
    private JAMSTableDataArray current = null;
    private String delimiters = "\t"; 
    private String endString = "#end";
    private String metadataID = "#";
    
    public GenericDataReader(String fileName, boolean timeParse, int startMeta, int startData) {
        
        this.fileName = fileName;
        this.timeParse = timeParse;
        createReader();
        
        //parse data depending on file format / parsing strategy
        if ((startMeta > 0) && (startData > startMeta))
            parseMetadata(startMeta, startData);
        else
            parseMetadata();
    }
    
    public GenericDataReader(String fileName, boolean timeParse, int startData){
        this.fileName = fileName;
        this.timeParse = timeParse;
        createReader();
        for(int i = 0; i < startData; i++){
            active = false;
            update();
        }
    }
    
    public GenericDataReader(String fileName, boolean timeParse) {
        this(fileName, timeParse, 0, 0);
    }
    
    private void createReader() {
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }
    
    public void parseMetadata() {
        String hold = "";
        
        update();
        while (nextString.startsWith(metadataID)) {
            active = false;
            hold = nextString;
            update();
        }
        StringTokenizer st = new StringTokenizer(hold.substring(1), delimiters);
        
        //throw away the time column
        if (timeParse)
            st.nextToken();
        
        int n = st.countTokens();
        String[] metadata = new String[n];
        for (int i = 0; i < n; i++) {
            metadata[i] = st.nextToken();
        }
        this.metadata=metadata;
    }
    
    private void parseMetadata(int startMeta, int startData) {
        
        for (int i = 0; i < startMeta; i++) {
            active = false;
            update();
        }
        
        StringTokenizer st = new StringTokenizer(nextString, delimiters);
        
        //throw away the time column
        if (timeParse)
            st.nextToken();
        
        int n = st.countTokens();
        String[] metadata = new String[n];
        for (int i = 0; i < n; i++) {
            metadata[i] = st.nextToken();
        }
        this.metadata=metadata;
        
        for (int i = startMeta; i < startData; i++) {
            active = false;
            update();
        }
    }
    
    private void update() {
        if (!active) {
            try {
                nextString = reader.readLine();
            } catch (IOException ioex) {
                JAMSTools.handle(ioex);
            }
            active = true;
        }
    }
    
    public JAMSTableDataArray getCurrent() {
        return current;
    }
    
    public JAMSTableDataArray getNext() {
        
        Attribute.Calendar time;
        
        update();
        active = false;
        
        StringTokenizer st = new StringTokenizer(nextString, delimiters);
        
        if (timeParse) {
            String timeString = st.nextToken();
            try{
                time = JAMSTableDataConverter.parseTime(timeString);
            }catch(Throwable t){
                timeString = timeString + " " + st.nextToken();
                time = JAMSTableDataConverter.parseTime(timeString);
            }            
        } else {
            time = null;
        }
        
        int n = st.countTokens();
        String[] values = new String[n];
        for (int i = 0; i < n; i++) {
            values[i] = st.nextToken();
        }
        
        this.current = new JAMSTableDataArray(time, values);
        return current;
    }
    
    public ArrayList<JAMSTableDataArray> getAll(){
        ArrayList<JAMSTableDataArray> array = new ArrayList<JAMSTableDataArray>();        
        while (hasNext()) {
            array.add(getNext());
        }
        return array;
    }
    
    
    public boolean hasNext() {
        update();
        if (nextString != null && !nextString.startsWith(endString))
            return true;
        else
            return false;
    }
    
    public String[] getMetadata() {
        return metadata;
    }
    
    public void close() {
        try {
            reader.close();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    /**
     * @return the delimiters
     */
    public String getDelimiters() {
        return delimiters;
    }

    /**
     * @param delimiters the delimiters to set
     */
    public void setDelimiters(String delimiters) {
        this.delimiters = delimiters;
    }

    /**
     * @return the endString
     */
    public String getEndString() {
        return endString;
    }

    /**
     * @param endString the endString to set
     */
    public void setEndString(String endString) {
        this.endString = endString;
    }

    /**
     * @return the metadataID
     */
    public String getMetadataID() {
        return metadataID;
    }

    /**
     * @param metadataID the metadataID to set
     */
    public void setMetadataID(String metadataID) {
        this.metadataID = metadataID;
    }
}
