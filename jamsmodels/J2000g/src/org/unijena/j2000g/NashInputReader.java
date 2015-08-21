/*
 * NashInputReader.java
 * Created on 22. May 2008, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2000g;

import jams.model.*;
import jams.data.*;
import jams.io.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Peter Krause
 * 
 */
public class NashInputReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String workspace;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE
            )
            public Attribute.Double sq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the tmin input"
            )
            public Attribute.Double ssq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the tmean input"
            )
            public Attribute.Double bq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE
            )
            public Attribute.Double obsRunoff;
    
    
    private JAMSTableDataStore store;
    
    public void init(){
        String fName = null;
        try {
            fName = workspace.getValue() + java.io.File.separatorChar + fileName.getValue();
            BufferedReader reader = new BufferedReader(new FileReader(fName));
            
            //skipping comments
            String line = reader.readLine();
            if(line.charAt(0) == '#')
                line = reader.readLine();
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        store = new GenericDataReader(fName, true, 1, 2);
        
    }
    
    public void run(){
        if(store.hasNext()){
            JAMSTableDataArray da = store.getNext();
            double[] vals = JAMSTableDataConverter.toDouble(da);
            this.sq.setValue(vals[0]);
            this.ssq.setValue(vals[1]);
            this.bq.setValue(vals[2]);
            this.obsRunoff.setValue(vals[3]);
        }
    }
    
    public void cleanup(){
        store.close();
    }
    
    private Attribute.Calendar parseTime(String timeString) {
        
        //Array keeping values for year, month, day, hour, minute
        String[] timeArray = new String[5];
        timeArray[0] = "1";
        timeArray[1] = "1";
        timeArray[2] = "0";
        timeArray[3] = "0";
        timeArray[4] = "0";
        
        StringTokenizer st = new StringTokenizer(timeString, ".-/ :");
        int n = st.countTokens();
        
        for (int i = 0; i < n; i++) {
            timeArray[i] = st.nextToken();
        }
        
        Attribute.Calendar cal = getModel().getRuntime().getDataFactory().createCalendar();
        cal.setValue(timeArray[0]+"-"+timeArray[1]+"-"+timeArray[2]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
    
}
