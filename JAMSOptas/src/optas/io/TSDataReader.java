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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import optas.gui.wizard.OPTASWizardException;
import optas.data.DataSet.MismatchException;
import optas.data.TimeSerie;

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
    private double missingDataValue = -9999;

    String[] name = null;
    double statx[] = null;
    double staty[] = null;
    String tres = null;

    int headerLineCount = 0;
    
    public TSDataReader(File data) throws IOException{
        this.dataFileName = data;
        init();
    }

    public int getColumnCount(){
        return statx.length;
    }
    
    public ArrayList<Object> getNames(){        
        ArrayList<Object> attr = new ArrayList<Object>();
        for (int i=0;i<name.length;i++){
            attr.add(name[i]);
        }
        return attr;
    }

    private String cleanToken(String token){        
        while (token.endsWith(" ") || token.endsWith("\t")){
            token = token.substring(0, token.length()-1);
        }
        return token;
    }
    
    private void init() throws IOException {
        //handle the j2k metadata descriptions        
        String dataName = null;
        String start = null;
        String end = null;
        double lowBound, uppBound;


        double[] id = null;        
        double[] statelev = null;

        String line = "#";

        BufferedReader reader = new BufferedReader(new FileReader(dataFileName));

        //skip comment lines
        while (line.charAt(0) == '#') {
            line = reader.readLine();
            headerLineCount++;
        }
        boolean dataValueAttribsValid = false, datasetAttribsValid = false, statAttribsValid = false;
        //metadata tags
        StringTokenizer strTok = new StringTokenizer(line, SEPARATOR);
        String token = cleanToken(strTok.nextToken());
        try{
        while (line!=null && token != null && !token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVAL)) {
            if (token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS)) {
                line = reader.readLine();                
                headerLineCount++;
                strTok = new StringTokenizer(line, SEPARATOR);
                dataName = strTok.nextToken();
                lowBound = Double.parseDouble(strTok.nextToken());
                uppBound = Double.parseDouble(strTok.nextToken());
                line = reader.readLine();
                strTok = new StringTokenizer(line, SEPARATOR);
                token = cleanToken(strTok.nextToken());
                dataValueAttribsValid = true;
                headerLineCount++;
            } else if (token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASETATTRIBS)) {
                int i = 0;
                line = reader.readLine();
                while (i < 4) {
                    headerLineCount++;
                    strTok = new StringTokenizer(line, "\t ");
                    String desc = cleanToken(strTok.nextToken());
                    if (desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_MISSINGDATAVAL)) {
                        missingDataValue = Double.parseDouble(cleanToken(strTok.nextToken()));
                    } else if (desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASTART)) {
                        start = cleanToken(strTok.nextToken()); //date part
                        if (strTok.hasMoreTokens()) //potential time part
                        {
                            start = start + " " + cleanToken(strTok.nextToken());
                        }
                    } else if (desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAEND)) {
                        end = cleanToken(strTok.nextToken());   //date part
                        if (strTok.hasMoreTokens()) //potential time part
                        {
                            end = end + " " + cleanToken(strTok.nextToken());
                        }
                    } else if (desc.equalsIgnoreCase(J2KTSDataStore.TAGNAME_TEMP_RES)) {
                        tres = cleanToken(strTok.nextToken());
                    }
                    i++;
                    line = reader.readLine();                    
                    strTok = new StringTokenizer(line, SEPARATOR);
                    token = cleanToken(strTok.nextToken());
                }
                datasetAttribsValid = true;
            } else if (token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_STATATTRIBVAL)) {
                int i = 0;
                line = reader.readLine();
                while (i < 6) {
                    headerLineCount++;
                    strTok = new StringTokenizer(line, SEPARATOR);
                    String desc = cleanToken(strTok.nextToken());
                    int nstat = strTok.countTokens();

                    if (desc.equalsIgnoreCase("name")) {
                        name = new String[nstat];
                        for (int j = 0; j < nstat; j++) {
                            name[j] = cleanToken(strTok.nextToken());
                        }
                    } else if (desc.equalsIgnoreCase("id")) {
                        id = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            id[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    } else if (desc.equalsIgnoreCase("elevation")) {
                        statelev = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            statelev[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    } else if (desc.equalsIgnoreCase("x")) {
                        statx = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            statx[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    } else if (desc.equalsIgnoreCase("lat")) {
                        statx = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            statx[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    }else if (desc.equalsIgnoreCase("y")) {
                        staty = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            staty[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    }else if (desc.equalsIgnoreCase("lon")) {
                        staty = new double[nstat];
                        for (int j = 0; j < nstat; j++) {
                            staty[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    } else if (desc.equalsIgnoreCase("datacolumn")) {
                        //do nothing for the moment just counting
                        headerLineCount++;
                        headerLineCount++;
                    }
                    i++;
                    line = reader.readLine();
                    strTok = new StringTokenizer(line, SEPARATOR);
                    token = cleanToken(strTok.nextToken());
                    statAttribsValid = true;
                }
            } else {
                if (strTok.hasMoreElements()) {
                    token = strTok.nextToken();
                } else {
                    line = reader.readLine();
                }
            }
        }
        }catch(NumberFormatException nfe){
            throw new IOException(JAMS.i18n("not_a_valid_line_in_J2K_datafile "));
        }catch(NoSuchElementException nfe){
            nfe.printStackTrace();
            throw new IOException(JAMS.i18n("not_a_valid_line_in_J2K_datafile "));
        }catch(NullPointerException npe){
            throw new IOException(JAMS.i18n("not_a_valid_J2K_datafile "));
        }finally{
            reader.close();
        }
        if (!dataValueAttribsValid || !datasetAttribsValid || !statAttribsValid) {
            throw new IOException(JAMS.i18n("no_valid_J2K_datafile"));
        }
        startTime = parseJ2KTime(start);
        endTime = parseJ2KTime(end);
    }

    public TimeSerie getData(int column) throws OPTASWizardException{

        store = new GenericDataReader(this.dataFileName.getAbsolutePath(), false, headerLineCount+1);

        ArrayList<Double> doubleArray = new ArrayList<Double>();
        int firstColumn = -1;
        while(store.hasNext()){
            JAMSTableDataArray tableData = store.getNext();            
            try{                   
                if (firstColumn == -1){
                    boolean didWork = false;                    
                    while(!didWork){
                        try{
                            doubleArray.add(Double.parseDouble(tableData.getValues()[firstColumn+column]));
                            didWork = true;
                        }catch(Throwable e){
                            firstColumn++;
                            if (firstColumn >= tableData.getValues().length){
                                throw new OPTASWizardException("J2K input file cannot be read! Invalid format!");
                            }
                        }
                    }
                }else{
                    doubleArray.add(Double.parseDouble(tableData.getValues()[firstColumn+column]));
                }
            }catch(NumberFormatException nfe){
                nfe.printStackTrace();
            }catch(ArrayIndexOutOfBoundsException aioobe){
                doubleArray.add(JAMS.getMissingDataValue());
            }
        }

        double data[] = new double[doubleArray.size()];
        for (int i=0;i<doubleArray.size();i++){
            if (doubleArray.get(i) == this.missingDataValue)
                data[i] = JAMS.getMissingDataValue();
            else{
                data[i] = doubleArray.get(i);
            }
            
        }

        Attribute.TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
        interval.setStart(startTime);
        interval.setEnd(endTime);
        if (tres.compareTo("m")==0){
            interval.setTimeUnit(Calendar.MONTH);
            interval.setTimeUnitCount(1);
        }
        if (tres.compareTo("d")==0){
            interval.setTimeUnit(Calendar.DAY_OF_YEAR);
            interval.setTimeUnitCount(1);
        }
        if (tres.compareTo("h")==0){
            interval.setTimeUnit(Calendar.HOUR_OF_DAY);
            interval.setTimeUnitCount(1);
        }

        try{
            t = new TimeSerie(data, interval, "observation", null);
        }catch(MismatchException m){
            m.printStackTrace();
            throw new OPTASWizardException("J2K input file is not valid!\n" + m.toString());            
        }
        store.close();
        return t;
    }

    public double[] getLocation(int i){
        return new double[]{statx[i],staty[i]};
    }
    
    private static Attribute.Calendar parseJ2KTime(String timeString) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        Date d = null;
        try{
            d = sdf1.parse(timeString);
        }catch(ParseException pe){
            try{
                d = sdf2.parse(timeString);
            }catch(ParseException pe2){
                return null;
            }
        }
        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        cal.setTime(d);
        return cal;
        //Array keeping values for year, month, day, hour, minute
        /*String[] timeArray = new String[5];
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
        return cal;*/
    }    
    
}
