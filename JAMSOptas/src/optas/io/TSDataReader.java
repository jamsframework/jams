/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package optas.io;

import jams.JAMS;
import jams.data.*;
import jams.workspace.stores.*;
import java.util.*;
import java.io.*;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.io.JAMSTableDataStore;
import java.util.ArrayList;
import optas.hydro.data.DataSet.MismatchException;
import optas.hydro.data.TimeSerie;

/**
 *
 * @author S. Kralisch
 */
public class TSDataReader{
    public static final String SEPARATOR = "\t";
        
    public File dataFileName;

    
        Attribute.Calendar startTime = null;
            Attribute.Calendar endTime = null;

    private JAMSTableDataStore store;
    private TimeSerie t;

    String[] name = null;
    String tres = null;

    int headerLineCount = 0;
    
    public TSDataReader(File data){
        this.dataFileName = data;
        init();
    }

    public ArrayList<Object> getNames(){        
        ArrayList<Object> attr = new ArrayList<Object>();
        for (int i=0;i<name.length;i++){
            attr.add(name[i]);
        }
        return attr;
    }

    public void init() {
        //handle the j2k metadata descriptions        
        String dataName = null;        
        String start = null;
        String end = null;
        double lowBound, uppBound, missData = 0;
        
        
        double[] id = null;
        double[] statx = null;
        double[] staty = null;
        double[] statelev = null;
                      
        String line = "#";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataFileName));
            
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
            startTime = parseJ2KTime(start);
            endTime = parseJ2KTime(end);
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public TimeSerie getData(int column){

        store = new GenericDataReader(this.dataFileName.getAbsolutePath(), false, headerLineCount+1);

        ArrayList<Double> doubleArray = new ArrayList<Double>();

        while(store.hasNext()){
            JAMSTableDataArray tableData = store.getNext();            
            try{
                doubleArray.add(Double.parseDouble(tableData.getValues()[column+2]));
            }catch(NumberFormatException nfe){
                nfe.printStackTrace();
            }catch(ArrayIndexOutOfBoundsException aioobe){
                doubleArray.add(JAMS.getMissingDataValue());
            }
        }

        double data[] = new double[doubleArray.size()];
        for (int i=0;i<doubleArray.size();i++){
            data[i] = doubleArray.get(i);
        }

        Attribute.TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
        interval.setStart(startTime);
        interval.setEnd(endTime);
        if (tres.compareTo("d")==0){
            interval.setTimeUnit(Calendar.DAY_OF_YEAR);
            interval.setTimeUnitCount(1);
        }
        if (tres.compareTo("h")==0){
            interval.setTimeUnit(Calendar.HOUR);
            interval.setTimeUnitCount(1);
        }

        try{
            t = new TimeSerie(data, interval, "observation", null);
        }catch(MismatchException m){
            m.printStackTrace();
        }
        store.close();
        return t;


    }

    private static Attribute.Calendar parseJ2KTime(String timeString) {
        
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
        
        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        cal.setValue(timeArray[2]+"-"+timeArray[1]+"-"+timeArray[0]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }    
}
