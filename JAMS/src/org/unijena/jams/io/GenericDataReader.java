/*
 * GenericDataReader.java
 *
 * Created on 04. October 2005, 01:49
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

package org.unijena.jams.io;

import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */

public class GenericDataReader implements JAMSTableDataStore {
    
    BufferedReader reader;
    String fileName;
    boolean timeParse;
    String nextString = "";
    boolean active = false;
    private String[] metadata;
    private JAMSTableDataArray current = null;
    
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
            reader = new BufferedReader(new FileReader(fileName));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
    }
    
    private void parseMetadata() {
        String hold = "";
        
        update();
        while (nextString.startsWith("#")) {
            active = false;
            hold = nextString;
            update();
        }
        StringTokenizer st = new StringTokenizer(hold.substring(1), "\t");
        
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
        
        StringTokenizer st = new StringTokenizer(nextString, "\t");
        
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
    
    /*
    private void parseMetadata() {
     
        String hold = "";
     
        update();
        active = false;
        update();
        while (!nextString.endsWith("=============")) {
            active = false;
            hold = nextString;
            update();
        }
     
        StringTokenizer st = new StringTokenizer(hold);
     
        //throw away the time column
        st.nextToken();
     
        int n = st.countTokens();
        String[] metadata = new String[n];
        for (int i = 0; i < n; i++) {
            metadata[i] = st.nextToken();
        }
        this.metadata=metadata;
        active = false;
    }
     */
    
    private void update() {
        if (!active) {
            try {
                nextString = reader.readLine();
            } catch (IOException ioex) {
                JAMS.handle(ioex);
            }
            active = true;
        }
    }
    
    public JAMSTableDataArray getCurrent() {
        return current;
    }
    
    public JAMSTableDataArray getNext() {
        
        JAMSCalendar time;
        
        update();
        active = false;
        
        StringTokenizer st = new StringTokenizer(nextString, "\t");
        
        if (timeParse) {
            String timeString = st.nextToken();
            timeString = timeString + " " + st.nextToken();
            time = JAMSTableDataConverter.parseTime(timeString);
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
    
    public boolean hasNext() {
        update();
        if (nextString != null)
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
            JAMS.handle(ioe);
        }
    }
}
