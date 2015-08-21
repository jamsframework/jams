/*
 * Climate.java
 * Created on 30. September 2005, 11:37
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

package org.unijena.abc;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class ABCxDataReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String workspace;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time interval of current temporal context"
            )
            public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the tmin input"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the tmean input"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the tmax input"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.Double rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.Double obsRunoff;
    
    
    private JAMSTableDataStore store;
    
    public void init(){
        String startEntry = null;
        String endEntry = null;
        String fName = null;
        try {
            fName = workspace.getValue() + java.io.File.separatorChar + fileName.getValue();
            BufferedReader reader = new BufferedReader(new FileReader(fName));
            
            //read start and end date
            String line = reader.readLine();
            StringTokenizer tok = new StringTokenizer(line, "\t");
            tok.nextToken();
            startEntry = tok.nextToken();
            
            line = reader.readLine();
            tok = new StringTokenizer(line, "\t");
            tok.nextToken();
            endEntry = tok.nextToken();
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        Attribute.Calendar startTime = parseTime(startEntry);
        Attribute.Calendar endTime = parseTime(endEntry);
        store = new GenericDataReader(fName, true, 1, 4);
        //calc offset if start date of time series and temporal context do not match
        if(timeInterval != null){
            if(timeInterval.getStart().before(startTime)){
                System.out.println("Model start time was before data start time");
                timeInterval.setStart(startTime);
            }
            if(timeInterval.getEnd().after(endTime)){
                System.out.println("Model end time was after data end time");
                timeInterval.setEnd(endTime);
            }
            int timeUnit = timeInterval.getTimeUnit();
            Attribute.Calendar tiStart = timeInterval.getStart();
            Attribute.Calendar date = new Attribute.Calendar(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE),startTime.get(Calendar.SECOND));
            while (startTime.before(date) && store.hasNext()) {
                JAMSTableDataArray da = store.getNext();
                if(timeUnit == Attribute.Calendar.MONTH)
                    startTime.add(Attribute.Calendar.MONTH, 1);
                else if(timeUnit == Attribute.Calendar.YEAR)
                    startTime.add(Attribute.Calendar.YEAR, 1);
            }
        }
    }
    
    public void run(){
        //if(store.hasNext())
        JAMSTableDataArray da = store.getNext();
        double[] vals = JAMSTableDataConverter.toDouble(da);
        this.precip.setValue(vals[0]);
        this.tmin.setValue(vals[1]);
        this.tmean.setValue(vals[2]);
        this.tmax.setValue(vals[3]);
        this.rhum.setValue(vals[4]);
        this.obsRunoff.setValue(vals[5]);
        
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
        
        Attribute.Calendar cal = new Attribute.Calendar();
        cal.setValue(timeArray[0]+"-"+timeArray[1]+"-"+timeArray[2]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
    
}
