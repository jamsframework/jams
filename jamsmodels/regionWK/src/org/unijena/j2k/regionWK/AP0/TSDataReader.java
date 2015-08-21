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

package org.unijena.j2k.regionWK.AP0;

import org.unijena.j2k.statistics.*;
import jams.model.*;
import jams.io.*;
import java.util.*;
import java.io.*;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.Attribute.Boolean;
import jams.data.Attribute.Calendar;
import jams.data.JAMSDataFactory;
import jams.data.Attribute.DoubleArray;
import jams.data.Attribute.Integer;
import jams.data.Attribute.String;
import jams.data.Attribute.StringArray;
import jams.data.Attribute.TimeInterval;
import jams.tools.JAMSTools;

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
            description = "data set descriptor"
            )
            public Attribute.String dataSetName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station elevations"
            )
            public Attribute.DoubleArray elevation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's x coordinate"
            )
            public Attribute.DoubleArray xCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's y coordinate"
            )
            public Attribute.DoubleArray yCoord;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "station location"
            )
            public Attribute.StringArray corrFac;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Regression coefficients"
            )
            public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calculate regression coefficients? If not, regCoeff array stays empty!"
            )
            public Attribute.Boolean skipRegression;
    
    
    private JAMSTableDataStore store;
    private JAMSTableDataArray da;
    
    public void init() {
        
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String dataName = null;
        String tres = null;
        String start = null;
        String end = null;
        double lowBound, uppBound, missData;

        String[] rainFac = null;
        String[] name, id;
        double[] statx = null;
        double[] staty = null;
        double[] statelev = null;
                      
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
            while(token.toLowerCase().compareTo("@dataval") != 0){
                if(token.toLowerCase().compareTo("@datavalueattribs") == 0){
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
                }else if(token.toLowerCase().compareTo("@datasetattribs") == 0){
                    int i = 0;
                    line = reader.readLine();
                    while(i < 4){
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t ");
                        String desc = strTok.nextToken();
                        if(desc.toLowerCase().compareTo("missingdataval") == 0){
                           missData = Double.parseDouble(strTok.nextToken()); 
                        }else if(desc.toLowerCase().compareTo("datastart") == 0){
                           start = strTok.nextToken(); //date part
                           if(strTok.hasMoreTokens())  //potential time part
                               start = start + " " + strTok.nextToken();
                        }else if(desc.toLowerCase().compareTo("dataend") == 0){
                           end = strTok.nextToken();   //date part
                           if(strTok.hasMoreTokens())  //potential time part
                               end = end + " " + strTok.nextToken();
                        }else if(desc.toLowerCase().compareTo("tres") == 0){
                           tres = strTok.nextToken(); 
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line, "\t");
                        token = strTok.nextToken();
                    }   
                }else if(token.toLowerCase().compareTo("@statattribval") == 0){
                    
                    line = reader.readLine();
                     while(token.toLowerCase().compareTo("@dataval") != 0){
                       headerLineCount++;
                       strTok = new StringTokenizer(line, "\t");
                       String desc = strTok.nextToken();
                       int nstat = strTok.countTokens();
                       
                       if(desc.toLowerCase().compareTo("name") == 0){
                           name = new String[nstat];
                           for(int j = 0; j < nstat; j++)
                               name[j] = strTok.nextToken();
                       }else if(desc.toLowerCase().compareTo("id") == 0){
                           id = new String[nstat];
                           for(int j = 0; j < nstat; j++)
                               id[j] = strTok.nextToken();
                       }else if(desc.toLowerCase().compareTo("elevation") == 0){
                           statelev = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               statelev[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.toLowerCase().compareTo("x") == 0){
                           statx = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               statx[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.toLowerCase().compareTo("y") == 0){
                           staty = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               staty[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.toLowerCase().compareTo("stattyp") == 0){
                           rainFac = new String[nstat];
                           for(int j = 0; j < nstat; j++)
                               rainFac[j] = strTok.nextToken();
                       }else if(desc.toLowerCase().compareTo("datacolumn")==0){
                           
                           //do nothing for the moment just counting
                          
                           headerLineCount++;
                           headerLineCount++;
                      }
                       
                       line = reader.readLine();
                       strTok = new StringTokenizer(line, "\t");
                       token = strTok.nextToken();
                      }

                }   
                
            }
            //System.out.println("huhu");
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        store = new GenericDataReader(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),dataFileName.getValue()), false, headerLineCount+1);
        

        jams.data.Attribute.Calendar startTime = parseJ2KTime(start);
        jams.data.Attribute.Calendar endTime = parseJ2KTime(end);
        
        if(timeInterval != null){
            //check if the time series start and end date match the temporal context's time interval
            if ((timeInterval.getStart().before(startTime) || timeInterval.getEnd().after(endTime))) {
                getModel().getRuntime().sendHalt("TSData start and end time of " + this.dataFileName.getValue() + 
                        " do not match current temporal context!\n" + 
                        "(" + timeInterval.getStart() + "<->" + timeInterval.getEnd() + 
                        " vs. " + startTime + "<->" + endTime + ")");
            }
        }
                
        //these are the stations with fixed attribute sets --> must be extended
        dataSetName.setValue(dataName);
        //elevation.setValue(JAMSTableDataConverter.toDouble(da, 2));
        elevation.setValue(statelev);

        //xCoord.setValue(JAMSTableDataConverter.toDouble(da, 2));
        xCoord.setValue(statx);
        //yCoord.setValue(JAMSTableDataConverter.toDouble(da, 2));
        yCoord.setValue(staty);
        
        corrFac.setValue(rainFac);
        
        //calc offset if start date of time series and temporal context do not match
        if(timeInterval != null){
            int timeUnit = timeInterval.getTimeUnit();
            Attribute.Calendar tiStart = timeInterval.getStart();
            jams.data.Attribute.Calendar date = JAMSDataFactory.createCalendar();
            if(timeUnit == Attribute.Calendar.DAY_OF_YEAR) {
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            } else if(timeUnit == Attribute.Calendar.HOUR_OF_DAY) {
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), tiStart.get(Calendar.HOUR_OF_DAY), tiStart.get(Calendar.MINUTE), 0);
            } else if(timeUnit == Attribute.Calendar.MONTH) {
                    date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), 1, 0, 0, 0);
            }
            
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
        
        getModel().getRuntime().println(dataSetName.getValue() + " data file initalised ... ", JAMS.VERBOSE);
    }
    
    public void run() {
        
        dataArray.setValue(JAMSTableDataConverter.toDouble(store.getNext(), startColumn.getValue()));
        if (!skipRegression.getValue()) {
            regCoeff.setValue(Regression.calcLinReg(elevation.getValue(), dataArray.getValue()));
        }
    }
    
    private static jams.data.Attribute.Calendar parseJ2KTime(String timeString) {
        
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
        
        jams.data.Attribute.Calendar cal = JAMSDataFactory.createCalendar();
        cal.setValue(timeArray[2]+"-"+timeArray[1]+"-"+timeArray[0]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
    
    public void cleanup() {
        store.close();
    }
}
