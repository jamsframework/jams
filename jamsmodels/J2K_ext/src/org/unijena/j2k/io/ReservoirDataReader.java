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

package org.unijena.j2k.io;

import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.util.*;
import java.io.*;
import jams.JAMS;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch
 */
public class ReservoirDataReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Data file name"
            )
            public Attribute.String dataFileName;

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
            description = "Array of data values for current time step"
            )
            public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "data field descriptors"
            )
            public Attribute.StringArray dataNames;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "data set descriptor"
            )
            public Attribute.String dataSetName;
    
    

    private JAMSTableDataStore store;
    private JAMSTableDataArray da;
    
    public void init() {
        
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String[] dataNames = null;
        String tres = null;
        String start = null;
        String end = null;
        double lowBound, uppBound, missData;
        
        String name, id;
        double statx = 0;
        double staty = 0;
        double statelev = 0;
                        
        String line = "#";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),dataFileName.getValue())));
            
            //skip comment lines
            while(line.charAt(0) == '#'){
                line = reader.readLine();
                headerLineCount++;
            }
            
            //metadata tags
            StringTokenizer strTok = new StringTokenizer(line, "\t");
            String token = strTok.nextToken();
            while(token.compareTo("@dataVal") != 0){
                if(token.compareTo("@dataSetAttribs") == 0){
                    int i = 0;
                    line = reader.readLine();
                    while(i < 4){
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t");
                        String desc = strTok.nextToken();
                        if(desc.compareTo("missingDataVal") == 0){
                           missData = Double.parseDouble(strTok.nextToken()); 
                        }else if(desc.compareTo("dataStart") == 0){
                           start = strTok.nextToken(); 
                        }else if(desc.compareTo("dataEnd") == 0){
                           end = strTok.nextToken(); 
                        }else if(desc.compareTo("tres") == 0){
                           tres = strTok.nextToken(); 
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line, "\t");
                        token = strTok.nextToken();
                    }   
                }else if(token.compareTo("@statAttribVal") == 0){
                    int i = 0;
                    line = reader.readLine();
                    strTok = new StringTokenizer(line, "\t");
                    String desc = strTok.nextToken();
                    if(desc.compareTo("dataColumn")==0){
                        int nstat = strTok.countTokens();
                        dataNames = new String[nstat];
                        for(int j = 0; j < nstat; j++)
                            dataNames[j] = strTok.nextToken();
                        headerLineCount++;
                        headerLineCount++;
                        headerLineCount++;
                    }
                    i++;
                    line = reader.readLine();
                    strTok = new StringTokenizer(line, "\t");
                    token = strTok.nextToken();
                    
                }   
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        //elevation
        //reservoir.setDouble("elevation", statelev);
        //xCoord
        //reservoir.setDouble("x", statx);
        //yCoord
        //reservoir.setDouble("y", staty);
        //adding the entries as attributes to the reservoir entity
        //for(int i = 0; i < dataSetNames.length; i++)
        //        reservoir.setDouble(dataSetNames[i], 0);
        this.dataNames.setValue(dataNames);
        
        store = new GenericDataReader(getModel().getWorkspaceDirectory().getPath()+"/"+dataFileName.getValue(), false, headerLineCount+1);
        

        Attribute.Calendar startTime = parseJ2KTime(start);
        Attribute.Calendar endTime = parseJ2KTime(end);

        if(timeInterval != null){
            //check if the time series start and end date match the temporal context's time interval
            if ((timeInterval.getStart().before(startTime) || timeInterval.getEnd().after(endTime))) {
                getModel().getRuntime().sendHalt("TSData start and end time of " + this.dataFileName.getValue() + " do not match current temporal context!");
            }
        }
                
        
        
        
        
        //calc offset if start date of time series and temporal context do not match
        if(timeInterval != null){
            int timeUnit = timeInterval.getTimeUnit();
            Attribute.Calendar tiStart = timeInterval.getStart();
            Attribute.Calendar date = getModel().getRuntime().getDataFactory().createCalendar();
            date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND));
            
            while (startTime.before(date) && store.hasNext()) {
                da = store.getNext();
                if(timeUnit == Attribute.Calendar.DAY_OF_YEAR)
                    startTime.add(Attribute.Calendar.DATE, 1);
                else if(timeUnit == Attribute.Calendar.HOUR_OF_DAY)
                    startTime.add(Attribute.Calendar.HOUR_OF_DAY, 1);
                else if(timeUnit == Attribute.Calendar.MONTH)
                    startTime.add(Attribute.Calendar.MONTH, 1);
            }
        }
        
        getModel().getRuntime().println(" reservoir data file initalised ... ", JAMS.VERBOSE);
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
