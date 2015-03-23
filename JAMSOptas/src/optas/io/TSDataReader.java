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
    private String missingDataString = "-9999";

    private String projection = "unknown";
    private String x = "x";
    private String y = "y";
    
    String[] name = null;
    double[] id = null;           
    double statx[] = null;
    double staty[] = null;
    double[] statelev = null;
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
    
    public void setElevation(double elev[]){
        for (int i=0;i<elev.length;i++){
            this.statelev[i] = elev[i];
        }
    }

    public int getTimeUnit(){
        if (tres == null)
            return -1;
        if (tres.compareTo("d")==0){
            return 6;
        }else if (tres.compareTo("h")==0){
            return 11;
        }else if (tres.compareTo("m")==0){
            return 2;
        }
        return -1;
    }
    
    public double getMissingDataValue(){
        return this.missingDataValue;
    }

    public String getMissingDataString(){
        return this.missingDataString;
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
            
        String line = "#";

        BufferedReader reader = new BufferedReader(new FileReader(dataFileName));

        //skip comment lines
        while (line.length()==0 || line.charAt(0) == '#') {
            if (line.contains(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS)){
                line = J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS;
                break;
            }
            line = reader.readLine();
            headerLineCount++;            
        }
        boolean dataValueAttribsValid = false, datasetAttribsValid = false, statAttribsValid = false;
        //metadata tags
        StringTokenizer strTok = new StringTokenizer(line, SEPARATOR);
        String token = cleanToken(strTok.nextToken());
        try{
        while (line!=null && token != null && !token.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVAL)) {
            if (token.equals(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS)) {
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
                        missingDataString = strTok.nextToken();
                        missingDataValue = Double.parseDouble(cleanToken(missingDataString));
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
                        x = "x";
                        for (int j = 0; j < nstat; j++) {
                            statx[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    } else if (desc.equalsIgnoreCase("lat")) {
                        statx = new double[nstat];
                        x = "lat";
                        for (int j = 0; j < nstat; j++) {
                            statx[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    }else if (desc.equalsIgnoreCase("y")) {
                        staty = new double[nstat];
                        y = "y";
                        for (int j = 0; j < nstat; j++) {
                            staty[j] = Double.parseDouble(cleanToken(strTok.nextToken()));
                        }
                    }else if (desc.equalsIgnoreCase("lon")) {
                        staty = new double[nstat];
                        y = "lon";
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
                    strTok = new StringTokenizer(line, SEPARATOR);
                    token = strTok.nextToken();
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

    protected int getHeaderLineCount(){
        return this.headerLineCount;
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
    
    public void setLocation(int i, double loc[]){
        statx[i] = loc[0];
        staty[i] = loc[1];        
    }
    
    public void setStartTime(Date d){
        this.startTime.setTime(d);
    }
    
    public void setEndTime(Date d){
        this.endTime.setTime(d);
    }
    
    public void setProjection(String proj, String x, String y){
        this.projection = proj;
        this.x = x;
        this.y = y;
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
    } 
    
    protected SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd\tHH:mm"){{setTimeZone(TimeZone.getTimeZone("GMT"));}};    
    protected SimpleDateFormat format2 = new SimpleDateFormat("dd.MM.yyyy\tHH:mm"){{setTimeZone(TimeZone.getTimeZone("GMT"));}};    
    
    public void write(File target) throws IOException{
        StringBuilder content = new StringBuilder();
        BufferedWriter writer = null;
        try{            
            Date firstDate = null;
            Date lastDate = null;
            store = new GenericDataReader(this.dataFileName.getAbsolutePath(), true, headerLineCount+1);
            while(store.hasNext()){
                JAMSTableDataArray dataset = store.getNext();
                content.append("\n");
                lastDate = dataset.getTime().getTime();
                content.append(format.format(lastDate));
                if (firstDate == null){
                    firstDate = lastDate;
                }
                for (String s : dataset.getValues()){
                    if (!s.contains(":"))
                        content.append("\t" + s);        
                }
            }
            setStartTime(firstDate);
            setEndTime(lastDate);

            String header = buildHeader();
            
            writer = new BufferedWriter(new FileWriter(target));
            writer.write(header);
            writer.write(content.substring(0));
        }finally{
            store.close();
            writer.close();
        }
        
    }
    public String buildHeader() {
        String header = "#created by TSDataReader - projection is " + projection + "\n";
        header += "@dataValueAttribs\ndata\t0.0\t0.0\t?\n";
        header += "@dataSetAttribs\nmissingDataVal\t-9999\n";
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (startTime != null) {
            calendar.setTime(startTime.getTime());            
            header += "dataStart\t" + format2.format(calendar.getTime()) + "\n";
            calendar.setTime(endTime.getTime());
            //largeValueCalendarAdd(calendar, timeUnit, (long) endTime);
            calendar.setTime(endTime.getTime());
            header += "dataEnd\t" + format2.format(calendar.getTime()) + "\n";                
        } else {
            header += "dataStart\t" + startTime + "\n";
            header += "dataEnd\t" + endTime + "\n";
        }

        header += "tres\td\n";
        header += "@statAttribVal\n";
        String strStation = "name\t";
        String strID = "ID\t";
        String strElevation = "elevation\t";
        String strX = x + "\t";
        String strY = y + "\t";
        String strDataColumn = "datacolumn\t";

        //double grid[][][] = this.makeGrid(latArray, lonArray, transformer);

        for (int i = 0; i < name.length; i++) {
            strStation += name[i];
            strID += id[i];
            strElevation += statelev[i];
                        
            strX += statx[i];
            strY += staty[i];
            
            strDataColumn += (i + 1);
            if (i != name.length) {
                strStation += "\t";
                strID += "\t";
                strElevation += "\t";
                //changed coords .. 
                strY += "\t";
                strX += "\t";
                strDataColumn += "\t";
            }
        }

        header += strStation + "\n" + strID + "\n" + strElevation + "\n" + strX + "\n" + strY + "\n" + strDataColumn + "\n";
        header += "@dataVal";
        return header;
    }
    
}
