/*
 * StandardEntityWriterN.java
 * Created on 15. Febuary 2006, 11:05
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

import java.util.Locale;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityWriterN extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "EntitySet"
            )
            public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time interval"
            )
            public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file header descriptions"
            )
            public Attribute.String header;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file attribute names"
            )
            public Attribute.String attributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity attribute name for weight [attName | none]"
            )
            public Attribute.String weight;
    
    private GenericDataWriter writer;
    private String[] attrs;
    private boolean headerWritten;
    private double[][] valueMatrix;
    private String[] dateVals;
    //private String timeFormat = "%1$td.%1$tm.%1$tY";
    private DateFormat dateFormat, aggfieldNameFormat;
    private int tcounter;
    private int nEnts;
    private double[][] aggMatrix;
    private int[] aggCounter;
    private String[] aggFieldNames;
    
    /*
     *  Component runstages
     */
    
    public void init() {                        
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),fileName.getValue()));
        
        int tsteps = (int)this.timeInterval.getNumberOfTimesteps();
        nEnts = this.entities.getEntityArray().length;
        valueMatrix = new double[tsteps][nEnts];
        dateVals = new String[tsteps];
        
        //daily values
        if(this.timeInterval.getTimeUnit() == 6){
            //timeFormat = "%1$td.%1$tm.%1$tY";
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");            
            aggMatrix = new double[tsteps][nEnts];
            aggCounter = new int[tsteps];
            aggFieldNames = new String[tsteps];
        }
        
        //monthly values
        if(this.timeInterval.getTimeUnit() == 2){
            //timeFormat = "%1$tb/%1$ty";
            dateFormat = new SimpleDateFormat("MMM/yy");            
            aggMatrix = new double[12][nEnts];
            aggCounter = new int[12];
            aggFieldNames = new String[12];
        }
        dateFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        aggfieldNameFormat = new SimpleDateFormat("MMM");
        aggfieldNameFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
        
        tcounter = 0;
        
    }
    
    public void run() {
        dateVals[tcounter] = time.toString(dateFormat);
        
        //no weight
        if(this.weight.getValue().equals("none")){
            for(int i = 0; i < nEnts; i++){
                valueMatrix[tcounter][i] = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.attributeName.getValue())).getValue());
            }
        }
        //user selected weight attribute
        else{
            for(int i = 0; i < nEnts; i++){
                double weight = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.weight.getValue())).getValue());
                valueMatrix[tcounter][i] = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.attributeName.getValue())).getValue()) / weight;
            }
        }
        
        //aggregated values
        //daily values
        if(this.timeInterval.getTimeUnit() == 5){
            int month = time.get(Attribute.Calendar.MONTH);
            aggCounter[month]++;
            //aggFieldNames[month] = time.toString("%1$tb");
            aggFieldNames[month] = time.toString(aggfieldNameFormat);
            
            
            for(int i = 0; i < nEnts; i++){
                aggMatrix[month][i] += valueMatrix[tcounter][i];
            }
        }
        //monthly values
        if(this.timeInterval.getTimeUnit() == 2){
            int month = time.get(Attribute.Calendar.MONTH);
            aggCounter[month]++;
            //aggFieldNames[month] = time.toString("%1$tb");
            aggFieldNames[month] = time.toString(aggfieldNameFormat);
            
            for(int i = 0; i < nEnts; i++){
                aggMatrix[month][i] += valueMatrix[tcounter][i];
            }
        }
        tcounter++;
    }
    
    public void cleanup() {
        
        getModel().getRuntime().println("Writing distributed output file ... may take a while ... please wait ...", JAMS.STANDARD);
        getModel().getRuntime().println("Number of entities: " + nEnts + ", number of timeSteps: " + dateVals.length);
        try {
            //header
            writer.addColumn("ID");
            for(int i = 0; i < dateVals.length; i++){
                writer.addColumn(dateVals[i]);
            }
            
            //aggregated values
            //monthly values
            if(this.timeInterval.getTimeUnit() == 2){
                for(int i = 0; i < 12; i++){
                    writer.addColumn(aggFieldNames[i]);
                }
                writer.addColumn("Year");
            }
            writer.writeHeader();
            
            //data matrix
            for(int e = 0; e < nEnts; e++){
                int ID = (int)(((Attribute.Double)entities.getEntityArray()[e].getObject("ID")).getValue());
                writer.addData(ID);
                for(int t = 0; t < dateVals.length; t++){
                    String dStr = String.format(Locale.US,"%.3f",valueMatrix[t][e]);
                    writer.addData(dStr);
                }
                //aggregated values
                //monthly values
                double aggSum = 0;
                if(this.timeInterval.getTimeUnit() == 2){
                    for(int t = 0; t < 12; t++){
                        aggSum += (aggMatrix[t][e] / aggCounter[t]);
                        String dStr = String.format(Locale.US,"%.3f",(aggMatrix[t][e] / aggCounter[t]));
                        writer.addData(dStr);
                    }
                    if(this.weight.getValue().equals("none")){
                        String dStr = String.format(Locale.US,"%.3f",(aggSum / 12.));
                        writer.addData(dStr);
                    }else{
                        String dStr = String.format(Locale.US,"%.3f",aggSum);
                        writer.addData(dStr);
                    }
                }
                writer.writeData();
            }
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().handle(jre);
        }
        writer.close();
        getModel().getRuntime().println("Finished distributed output file ... you may continue ...", JAMS.STANDARD);
    }
}
