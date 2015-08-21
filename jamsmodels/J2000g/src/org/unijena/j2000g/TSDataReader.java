/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
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

import org.unijena.j2k.statistics.*;
import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.util.*;
import java.io.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class TSDataReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Data file name"
            )
            public Attribute.String dataFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Data file directory name"
            )
            public Attribute.String dirName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Column of first data value"
            )
            public Attribute.Integer startColumn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time interval of current temporal context"
            )
            public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of data values for current time step",
            defaultValue=""
            )
            public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "data set descriptor",
            defaultValue=""
            )
            public Attribute.String dataSetName;
    
   private JAMSTableDataStore store;
   private JAMSTableDataArray da;
    
    public void init() {
        
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String dataName = null;
        String tres = null;
        String start = null;
        String end = null;
        String[] name, id;
        
        
        String fileName = dirName.getValue()+"/"+dataFileName.getValue();
        String line = "#";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            
            //skip comment lines
            while(line.charAt(0) == '#'){
                line = reader.readLine();
                headerLineCount++;
            }
            StringTokenizer tok = new StringTokenizer(line,"\t");
            start = tok.nextToken();
           
            
            //get end date
            String lastLine = null;
            while(line.charAt(0) != '#'){
                lastLine = line;
                line = reader.readLine();
            }
            tok = new StringTokenizer(lastLine,"\t");
            end = tok.nextToken();
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        store = new GenericDataReader(dirName.getValue()+"/"+dataFileName.getValue(), false, headerLineCount);
        

        Attribute.Calendar startTime = parseJ2KTime(start);
        Attribute.Calendar endTime = parseJ2KTime(end);
        
        if(timeInterval != null){
            //check if the time series start and end date match the temporal context's time interval
            if ((timeInterval.getStart().before(startTime) || timeInterval.getEnd().after(endTime))) {
                getModel().getRuntime().sendHalt("TSData start and end time of " + this.dataFileName.getValue() + " do not match current temporal context!");
            }
        }
                
        //these are the stations with fixed attribute sets --> must be extended
        dataSetName.setValue(dataName);
        //calc offset if start date of time series and temporal context do not match
        if(timeInterval != null){
            int timeUnit = timeInterval.getTimeUnit();
            Attribute.Calendar tiStart = timeInterval.getStart();
            Attribute.Calendar date = getModel().getRuntime().getDataFactory().createCalendar();
            if(timeUnit == Attribute.Calendar.DAY_OF_YEAR)
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                else if(timeUnit == Attribute.Calendar.HOUR_OF_DAY)
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), tiStart.get(Calendar.HOUR_OF_DAY), tiStart.get(Calendar.MINUTE), 0);
                else if(timeUnit == Attribute.Calendar.MONTH)
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), 1, 0, 0, 0);
            
            while (startTime.before(date) && store.hasNext()) {
                da = store.getNext();
                if (timeUnit == Attribute.Calendar.DAY_OF_YEAR) {
                    startTime.add(Attribute.Calendar.DATE, 1);
                } else if (timeUnit == Attribute.Calendar.HOUR_OF_DAY) {
                    startTime.add(Attribute.Calendar.HOUR_OF_DAY, 1);
                } else if (timeUnit == Attribute.Calendar.MONTH) {
                    startTime.add(Attribute.Calendar.MONTH, 1);
                }

            }
        }
        
        getModel().getRuntime().println(dataSetName.getValue() + " data file initalised ... ", JAMS.VERBOSE);
    }
    
    public void run() {
        
        dataArray.setValue(JAMSTableDataConverter.toDouble(store.getNext(), startColumn.getValue()));
    }
    
    private Attribute.Calendar parseJ2KTime(String timeString) {
        
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
        cal.setValue(timeArray[2]+"-"+timeArray[1]+"-"+timeArray[0]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
    
    public void cleanup() {
        store.close();
    }
}
