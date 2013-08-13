/*
 * DataReader.java
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
package optas.test;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author P. Krause
 */
/*@JAMSComponentDescription(
title = "hymod.DataReader",
author = "Peter Krause",
description = "Component which reads HYMOD specific data files. Such files "
+ "should contain columns with date, precip, potential ET and observed runoff "
+ "for each time step")*/
public class DataReader {

    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "A full qualfied file name pointing to a hymod"
    + "compliant data file")*/
    public String fileName;
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Internal variable name for the precipitation value of"
    + "each time step")*/
    public double precip;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Internal variable name for the potential ET value of"
    + "each time step")*/
    public double pet;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
    description = "Internal variable name for the observed runoff value of"
    + "each time step")*/
    public double obsRunoff;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
    description = "Time interval of current temporal context")
    public JAMSTimeInterval timeInterval;*/
    
    
    /*private JAMSTableDataStore store;    
    private JAMSTableDataArray da;*/

    private String[] md;
    BufferedReader reader;
    public java.util.HashMap dataMap = new java.util.HashMap();    
    
    public void init() {

        try {
            //read start and end time of data set
            reader = new BufferedReader(new FileReader(new File(fileName)));
            String line = reader.readLine();
            StringTokenizer strTok = new StringTokenizer(line, "\t");
            String desc = strTok.nextToken();
            String start = strTok.nextToken();
            Calendar startTime = parseTime(start);

            line = reader.readLine();
            strTok = new StringTokenizer(line, "\t");
            desc = strTok.nextToken();
            String end = strTok.nextToken();
            Calendar endTime = parseTime(end);

            //read column headings
            line = reader.readLine();
            strTok = new StringTokenizer(line, "\t");
            int cols = strTok.countTokens() - 1;
            md = new String[cols];

            //skip date
            strTok.nextToken();
            for (int i = 0; i < cols; i++) {
                md[i] = strTok.nextToken();
            }

            /*store = new GenericDataReader(new File(getModel().getWorkspaceDirectory(), fileName.getValue()).getAbsolutePath(), true, 1, 4);

            //check dates and move to the right position
            if (timeInterval != null) {
                //check if the time series start and end date match the temporal context's time interval
                if ((timeInterval.getStart().before(startTime) || timeInterval.getEnd().after(endTime))) {
                    getModel().getRuntime().sendHalt("TSData start and end time of " + this.fileName.getValue() + " do not match current temporal context!");
                }
                int timeUnit = timeInterval.getTimeUnit();
                Attribute.Calendar tiStart = timeInterval.getStart();
                Attribute.Calendar date = getModel().getRuntime().getDataFactory().createCalendar();
                date.set(tiStart.get(Calendar.YEAR), tiStart.get(Calendar.MONTH), tiStart.get(Calendar.DAY_OF_MONTH), startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND));

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
            }*/
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")    
    public void run() {

        /*JAMSTableDataArray da = store.getNext();
        double[] vals = JAMSTableDataConverter.toDouble(da);*/
        //this.data.setValue(vals);

        String line = null;
        try{
            line = reader.readLine();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return;
        }
        
        String tokens[] = line.split("\t");
        Double vals[] = new Double[md.length];
        
        for (int i = 0; i < md.length; i++) {
            try{
                vals[i] = Double.parseDouble(tokens[i+2]);
            }catch(NumberFormatException nfe){
                vals[i] = 0.0;
            }
        }
        
        for (int i = 0; i < md.length; i++) {
            dataMap.put(md[i], vals[i]);
        }

        this.precip = (Double)dataMap.get("precip");
        this.pet = (Double)dataMap.get("pet");
        this.obsRunoff = (Double)dataMap.get("runoff");
    }

    public void cleanup() {
        try{
        reader.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    private Calendar parseTime(String timeString) {

        //Array keeping values for year, month, day
        String[] timeArray = new String[3];
        timeArray[0] = "1";
        timeArray[1] = "1";
        timeArray[2] = "0";

        StringTokenizer st = new StringTokenizer(timeString, ".-/ :");
        int n = st.countTokens();

        for (int i = 0; i < n; i++) {
            timeArray[i] = st.nextToken();
        }

        Calendar cal = new GregorianCalendar();
        try{
            cal.set(Integer.parseInt(timeArray[2]), Integer.parseInt(timeArray[1]), Integer.parseInt(timeArray[0]));
        }catch(NumberFormatException pe){
            cal.set(1, 1, 1);
        }
        //cal.setValue(timeArray[2] + "-" + timeArray[1] + "-" + timeArray[0]);
        return cal;
    }
}
