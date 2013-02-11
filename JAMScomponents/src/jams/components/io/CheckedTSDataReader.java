/*
 * CheckedTSDataReader.java
 * Created on 1. Juni 2007, 14:12
 *
 * This file is part of JAMS
 * Copyright (C) 2007 S. Kralisch and P. Krause
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

package jams.components.io;

import jams.data.*;
import jams.model.*;
import java.util.*;
import java.io.*;
import jams.JAMS;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.io.JAMSTableDataConverter;
import jams.io.JAMSTableDataStore;

/**
 *
 * @author S. Kralisch
 */
public class CheckedTSDataReader extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file name"
            )
            public JAMSString dataFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Column of first data value"
            )
            public JAMSInteger startColumn;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Time interval of current temporal context"
            )
            public JAMSTimeInterval timeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Time interval of current temporal context"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of data values for current time step"
            )
            public JAMSDoubleArray dataArray;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "data set descriptor"
            )
            public JAMSString dataSetName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station elevations"
            )
            public JAMSDoubleArray elevation;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's x coordinate"
            )
            public JAMSDoubleArray xCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array of station's y coordinate"
            )
            public JAMSDoubleArray yCoord;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Regression coefficients"
            )
            public JAMSDoubleArray regCoeff;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Calculate regression coefficients? If not, regCoeff array stays empty!"
            )
            public JAMSBoolean skipRegression;
    
    private JAMSTableDataStore store;
    private JAMSTableDataArray da;
    private int timeUnit;
    private Attribute.Calendar tsTime, currentTime;
    private double[] noDataValues = new double[0];
    
    public void init() {
        
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String dataName = null;
        String tres = null;
        String start = null;
        String end = null;
        double lowBound, uppBound, missData;
        
        String[] name, id;
        double[] statx = null;
        double[] staty = null;
        double[] statelev = null;
        
        
        String fileName = getModel().getWorkspaceDirectory().getPath()+"/"+dataFileName.getValue();
        String line = "#";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            
            //skip comment lines
            while(line.charAt(0) == '#'){
                line = reader.readLine();
                headerLineCount++;
            }
            
            //metadata tags
            StringTokenizer strTok = new StringTokenizer(line, "\t");
            String token = strTok.nextToken();
            while(token.compareTo("@dataVal") != 0){
                if(token.compareTo("@dataValueAttribs") == 0){
                    line = reader.readLine();
                    headerLineCount++;
                    strTok = new StringTokenizer(line, "\t");
                    dataName = strTok.nextToken();
                    lowBound = Double.parseDouble(strTok.nextToken());
                    uppBound = Double.parseDouble(strTok.nextToken());
                    line = reader.readLine();
                    strTok = new StringTokenizer(line, "\t");
                    token = strTok.nextToken();
                    headerLineCount++;
                }else if(token.compareTo("@dataSetAttribs") == 0){
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
                    while(i < 6){
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t");
                        String desc = strTok.nextToken();
                        int nstat = strTok.countTokens();
                        
                        if(desc.compareTo("name") == 0){
                            name = new String[nstat];
                            for(int j = 0; j < nstat; j++)
                                name[j] = strTok.nextToken();
                        }else if(desc.compareTo("ID") == 0){
                            id = new String[nstat];
                            for(int j = 0; j < nstat; j++)
                                id[j] = strTok.nextToken();
                        }else if(desc.compareTo("elevation") == 0){
                            statelev = new double[nstat];
                            for(int j = 0; j < nstat; j++)
                                statelev[j] = Double.parseDouble(strTok.nextToken());
                        }else if(desc.compareTo("x") == 0){
                            statx = new double[nstat];
                            for(int j = 0; j < nstat; j++)
                                statx[j] = Double.parseDouble(strTok.nextToken());
                        }else if(desc.compareTo("y") == 0){
                            staty = new double[nstat];
                            for(int j = 0; j < nstat; j++)
                                staty[j] = Double.parseDouble(strTok.nextToken());
                        }else if(desc.compareTo("dataColumn")==0){
                            //do nothing for the moment just counting
                            headerLineCount++;
                            headerLineCount++;
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line, "\t");
                        token = strTok.nextToken();
                    }
                }
            }
            reader.close();
            
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            getModel().getRuntime().handle(ioe);
        }
        
        store = new GenericDataReader(getModel().getWorkspaceDirectory().getPath()+"/"+dataFileName.getValue(), true, headerLineCount+1);
        
        timeUnit = timeInterval.getTimeUnit();
        
        Attribute.Calendar startTime = parseTime(start);
        Attribute.Calendar endTime = parseTime(end);
        
        //these are the stations with fixed attribute sets --> must be extended
        dataSetName.setValue(dataName);
        //elevation.setValue(JAMSTableDataConverter.toDouble(da, 2));
        elevation.setValue(statelev);
        
        //xCoord.setValue(JAMSTableDataConverter.toDouble(da, 2));
        xCoord.setValue(statx);
        //yCoord.setValue(JAMSTableDataConverter.toDouble(da, 2));
        yCoord.setValue(staty);
        
        currentTime = timeInterval.getStart().clone();
        tsTime = getModel().getRuntime().getDataFactory().createCalendar();
        tsTime.setValue("0-01-01 00:00");
        
        getModel().getRuntime().println(dataSetName.getValue() + " data file initalised ... ", JAMS.VERBOSE);
    }
    
    public void run() {
        
        int diff;
        
        //while ((diff = time.compareTo(tsTime, timeUnit)) < 1) {
        while ((diff = time.compareTo(tsTime, timeUnit)) > 0) {
            da = store.getNext();
            tsTime = da.getTime();
        }
        
        
        if (diff == 0) {
            dataArray.setValue(JAMSTableDataConverter.toDouble(da, startColumn.getValue()));
            //getModel().getRuntime().println(tsTime.toString() + " : " + dataArray);
        } else {
            dataArray.setValue(noDataValues);
        }
        
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
        cal.setValue(timeArray[2]+"-"+timeArray[1]+"-"+timeArray[0]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
    
    public void cleanup() {
        store.close();
    }
}
