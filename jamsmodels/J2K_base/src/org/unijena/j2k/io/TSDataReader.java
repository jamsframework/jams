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

import org.unijena.j2k.statistics.*;
import jams.data.*;
import jams.model.*;
import jams.workspace.stores.*;
import java.util.*;
import java.io.*;
import jams.JAMS;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.io.JAMSTableDataConverter;
import jams.io.JAMSTableDataStore;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch
 */
public class TSDataReader extends JAMSComponent {
    public static final String SEPARATOR = "\t";
    
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
            description = "Regression coefficients"
            )
            public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calculate regression coefficients? If not, regCoeff array stays empty!"
            )
            public Attribute.Boolean skipRegression;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of numerical station ids",
            defaultValue=""
            )
            public Attribute.DoubleArray statId;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "names of the stations",
            defaultValue=""
            )
            public Attribute.StringArray statNames;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "temporal resolution",
            defaultValue=""
            )
            public Attribute.String tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "missing data value"
            )
            public Attribute.Double missDataValue;

    private JAMSTableDataStore store;
    private JAMSTableDataArray da;
    
    @Override
    public void init() {
        getModel().getRuntime().println(" start init " + dataFileName.getValue() + ".. ", JAMS.VERBOSE);
        
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String dataName = null;
        String tres = null;
        String start = null;
        String end = null;
        double lowBound, uppBound, missData = 0;
        
        String[] name = null;
        double[] id = null;
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
            StringTokenizer strTok = new StringTokenizer(line,SEPARATOR);
            String token = strTok.nextToken();
            while (!token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVAL)) {
                if(token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS)){
                    line = reader.readLine();
                    headerLineCount++;
                    strTok = new StringTokenizer(line,SEPARATOR);
                    dataName = strTok.nextToken();
                    lowBound = Double.parseDouble(strTok.nextToken());
                    uppBound = Double.parseDouble(strTok.nextToken());
                    line = reader.readLine();
                    strTok = new StringTokenizer(line,SEPARATOR);
                    token = strTok.nextToken();
                    headerLineCount++;
                }else if(token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASETATTRIBS)){
                    int i = 0;
                    line = reader.readLine();
                    while(i < 4){
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t ");
                        String desc = strTok.nextToken();
                        if(desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_MISSINGDATAVAL)){
                           missData = Double.parseDouble(strTok.nextToken()); 
                        }else if(desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASTART)){
                           start = strTok.nextToken(); //date part
                           if(strTok.hasMoreTokens())  //potential time part
                               start = start + " " + strTok.nextToken();
                        }else if(desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAEND)){
                           end = strTok.nextToken();   //date part
                           if(strTok.hasMoreTokens())  //potential time part
                               end = end + " " + strTok.nextToken();
                        }else if(desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_TEMP_RES)){
                           tres = strTok.nextToken(); 
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line,SEPARATOR);
                        token = strTok.nextToken();
                    }   
                }else if(token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_STATATTRIBVAL)){
                    int i = 0;
                    line = reader.readLine();
                    while(i < 6){
                       headerLineCount++;
                       strTok = new StringTokenizer(line,SEPARATOR);
                       String desc = strTok.nextToken();
                       int nstat = strTok.countTokens();
                       
                       if(desc.equalsIgnoreCase("name")){
                           name = new String[nstat];
                           for(int j = 0; j < nstat; j++)
                               name[j] = strTok.nextToken();
                       }else if(desc.equalsIgnoreCase("id")){
                           id = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               id[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.equalsIgnoreCase("elevation")){
                           statelev = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               statelev[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.equalsIgnoreCase("x")){
                           statx = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               statx[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.equalsIgnoreCase("y")){
                           staty = new double[nstat];
                           for(int j = 0; j < nstat; j++)
                               staty[j] = Double.parseDouble(strTok.nextToken());
                       }else if(desc.equalsIgnoreCase("datacolumn")){
                           //do nothing for the moment just counting
                           headerLineCount++;
                           headerLineCount++;
                       }
                       i++;
                       line = reader.readLine();
                       strTok = new StringTokenizer(line,SEPARATOR);
                       token = strTok.nextToken();
                    }
                }   
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        store = new GenericDataReader(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),dataFileName.getValue()), false, headerLineCount+1);
        

        Attribute.Calendar startTime = parseJ2KTime(start);
        Attribute.Calendar endTime = parseJ2KTime(end);
        
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
        statId.setValue(id);
        statNames.setValue(name);
        tempRes.setValue(tres);
        missDataValue.setValue(missData);
        
        //calc offset if start date of time series and temporal context do not match
        if(timeInterval != null){
            int timeUnit = timeInterval.getTimeUnit();
            Attribute.Calendar tiStart = timeInterval.getStart();
            Attribute.Calendar date = getModel().getRuntime().getDataFactory().createCalendar();
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
    
    @Override
    public void run() {
        double value[] = JAMSTableDataConverter.toDouble(store.getNext(), startColumn.getValue());
        for (int i=0;i<value.length;i++){
            if (value[i] == missDataValue.getValue()){
                value[i] = JAMS.getMissingDataValue();
            }
        }
        dataArray.setValue(value);
        
         if (!skipRegression.getValue()) {
            regCoeff.setValue(Regression.calcLinReg(elevation.getValue(), dataArray.getValue()));
        }                 
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
    
    @Override
    public void cleanup() {
        store.close();
    }
}
