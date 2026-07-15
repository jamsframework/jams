/*
 * EntityWriterMonthlyAgg.java
 * Created on 03. September 2007
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

package jams.components.io;

import java.util.Locale;
import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author D. Varga
 */
@JAMSComponentDescription(
title="Entity file writer (spatial+monthly)",
        author="D. Varga",
        description="Base: StandardEntityWriterN (P.Krause)." +
        "Use: For calculating monthly averages, the time Interval should be always one day longer."
        )
        public class EntityWriterMonthlyAgg extends JAMSComponent {
    
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
            description = "The model time interval"
            )
            public Attribute.TimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The aggregation time interval"
            )
            public Attribute.TimeInterval aggTimeInterval;
    
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
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "true/false enables output of each simulated month"
            )
            public Attribute.Boolean monthlyValuesWriting;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "type of calculation - sum, average, stDev"
            )
            public Attribute.String type;
    
    private GenericDataWriter writer;
    private double[][] valueMatrix;
    private double[] valueMatrixAverage;
    private double[][] monthValueMatrix;
    private double[] StDev;
    private double[] dailyValue;
    private double[] weightVal;
    private String[] dateVals;
    private String timeFormat = "%1$td.%1$tm.%1$tY";
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private DateFormat dateMonthFormat = new SimpleDateFormat("ddd");
    private int tcounter;
    private int daycounter = 0;
    private int oldMonth;
    private int nEnts;
    private double[][] aggMatrix;
    private int[] aggCounter;
    private String[] aggFieldNames;
    private Attribute.TimeInterval timeInterval2;
    
    
    private int counter = 0;
    
    private int aggTsteps = 0;
    
    /*
     *  Component runstages
     */
    
    public void init() {
        
        //some checking if time intervals correlate well
        //....
        //....
        
        Attribute.Calendar model_sd = this.modelTimeInterval.getStart().clone();
        Attribute.Calendar model_ed = this.modelTimeInterval.getEnd().clone();
        int model_tres = this.modelTimeInterval.getTimeUnit();
        long sdMod = model_sd.getTimeInMillis();
        long edMod = model_ed.getTimeInMillis();
        long model_tsteps = 0;
        
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();
        
        Attribute.Calendar agg_sd = this.aggTimeInterval.getStart().clone();
        Attribute.Calendar agg_ed = this.aggTimeInterval.getEnd().clone();
        int agg_tres = this.aggTimeInterval.getTimeUnit();
        long sdAgg = agg_sd.getTimeInMillis();
        long edAgg = agg_ed.getTimeInMillis();
        
        //check if aggTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if(agg_sd.before(model_sd)){
            this.aggTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("aggStartdate was set equal to model startdate", JAMS.STANDARD);
        }
        if(model_ed.before(agg_ed)){
            this.aggTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("aggEnddate was set equal to model enddate", JAMS.STANDARD);
        }
        
        aggTsteps = (int) aggTimeInterval.getNumberOfTimesteps();
        
        int ts = (int) this.getContext().getNumberOfIterations();
        getModel().getRuntime().println("aggStartdate:\t" + agg_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("aggEnddate:\t" + agg_ed.toString(), JAMS.VERBOSE);
        
        edAgg = edAgg + 86400000;
        agg_ed.setTimeInMillis(edAgg);
        this.aggTimeInterval.setEnd(agg_ed);
        
        writer = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+fileName.getValue());
        
        timeInterval2 = new JAMSTimeInterval(this.aggTimeInterval.getStart(), this.aggTimeInterval.getEnd(), (int) Attribute.Calendar.MONTH, 1);
        
        int tsteps = (int)this.timeInterval2.getNumberOfTimesteps();
        
        nEnts = this.entities.getEntityArray().length;
        valueMatrix = new double[tsteps+1][nEnts];
        valueMatrixAverage = new double[nEnts];
        StDev = new double[nEnts];
        
        dailyValue = new double[nEnts];
        dateVals = new String[tsteps];
        weightVal = new double[nEnts];
        
        //monthly values
        timeFormat = "%1$tb/%1$ty";
        dateFormat = new SimpleDateFormat("MMM/yy");
        aggMatrix = new double[12][nEnts];
        aggCounter = new int[12];
        aggFieldNames = new String[12];
        
        tcounter = 0;
        
        
        int oldMonth = time.get(Attribute.Calendar.MONTH);
    }
    
    public void run() {
        
        if (!time.after(aggTimeInterval.getEnd()) && !time.before(aggTimeInterval.getStart())) {
            
            int newMonth=time.get(Attribute.Calendar.MONTH);  //hier wird überprüft, ob ein Monatswechsel stattgefunden hat
            if(newMonth != oldMonth){           //wenn das der Fall ist, dann werden im Folgenden die Werte der valueMatrix zur aggMatrix aggregiert
                
                //Einfügen: Lesen von Monat und Jahr für Header!
                dateVals[tcounter] = time.toString(dateFormat);
                
                //aggregated values
                //monthly values
                if(tcounter > 0){
                    int month = oldMonth;//time.get(time.MONTH);
                    aggFieldNames[month] = time.toString(dateMonthFormat);
                    if(this.type.getValue().equals("average")){
                        for(int i = 0; i < nEnts; i++){
                            valueMatrixAverage[i] = valueMatrix[tcounter][i] / daycounter;
                            aggMatrix[month][i] += valueMatrixAverage[i];
                            valueMatrix[tcounter][i] = valueMatrixAverage[i];
                        }
                    } else if(this.type.getValue().equals("stDev")){
                        for(int i = 0; i < nEnts; i++){
                            valueMatrixAverage[i] = valueMatrix[tcounter][i] / daycounter;
                            double sum = 0;
                            for(int d = 0; d < daycounter; d++){
                                sum = sum + Math.pow((monthValueMatrix[d][i] - valueMatrixAverage[i]), 2);
                            }
                            StDev[i] = Math.sqrt(sum / (daycounter - 1));
                            valueMatrix[tcounter][i] = StDev[i];
                            aggMatrix[month][i] += valueMatrix[tcounter][i];
                        }
                    } else if(this.type.getValue().equals("sum")){
                        for(int i = 0; i < nEnts; i++){
                            aggMatrix[month][i] += valueMatrix[tcounter][i];
                        }
                    }
                    
                    aggCounter[month]++; // in diesem Array wird gezählt wie oft ein bestimmter Monat aggregiert wurde. Dient zur Berechnung des Mittelwertes
                }
                tcounter++;
                daycounter = 0;
            }
            
            //no weight
            if(daycounter == 0){
                int d = time.getActualMaximum(Attribute.Calendar.DATE);
                monthValueMatrix = new double[d][nEnts];
            }
            if(this.weight.getValue().equals("none")){
                for(int i = 0; i < nEnts; i++){
                    //hier wird der Tageswert aus dem Modell geholt
                    dailyValue[i] = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.attributeName.getValue())).getValue());
                    //hier für die Berechnung der Standardabweichung zwischengespeichert
                    if(this.type.getValue().equals("stDev")){
                        monthValueMatrix[daycounter][i] = dailyValue[i];
                    }
                    
                    //und hier zum jeweiligen Monat (durch tcounter bestimmt!) hinzuaddiert
                    valueMatrix[tcounter][i] = valueMatrix[tcounter][i] + dailyValue[i];
                    
                }
                
            }
            //user selected weight attribute
            else{
                for(int i = 0; i < nEnts; i++){
                    weightVal[i] = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.weight.getValue())).getValue());
                    //double weight = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.weight.getValue())).getValue());
                    dailyValue[i] = (((Attribute.Double)entities.getEntityArray()[i].getObject(this.attributeName.getValue())).getValue()) / weightVal[i];
                    if(this.type.getValue().equals("stDev")){
                        monthValueMatrix[daycounter][i] = dailyValue[i];
                    }
                    valueMatrix[tcounter][i] = valueMatrix[tcounter][i] + dailyValue[i];
                }
                
            }
            daycounter++;
            oldMonth = time.get(Attribute.Calendar.MONTH);    //hier wird der Monat der gerade verarbeiteten Zeitschritts gespeichert
        }
    }
    
    public void cleanup() {
        
        getModel().getRuntime().println("Writing distributed output file ... may take a while ... please wait ...", JAMS.STANDARD);
        getModel().getRuntime().println("Number of entities: " + nEnts + ", number of timeSteps: " + dateVals.length);
        try {
            writer.addComment("J2K model output: "+header.getValue());
            
            writer.addComment("");
            
            //header
            writer.addColumn("ID");
            if(monthlyValuesWriting.getValue()){
                for(int i = 0; i < tcounter-1; i++){
                    writer.addColumn(dateVals[i]);
                }
            }
            
            //aggregated values
            //monthly values
            /*writer.addColumn(aggFieldNames[11]);
            for(int i = 0; i < 11; i++){
                writer.addColumn(aggFieldNames[i]);
            }*/
            writer.addColumn("Jan");
            writer.addColumn("Feb");
            writer.addColumn("Mar");
            writer.addColumn("Apr");
            writer.addColumn("May");
            writer.addColumn("Jun");
            writer.addColumn("Jul");
            writer.addColumn("Aug");
            writer.addColumn("Sep");
            writer.addColumn("Oct");
            writer.addColumn("Nov");
            writer.addColumn("Dec");
            
            writer.addColumn("Year");
            writer.writeHeader();
            
            //data matrix
            for(int e = 0; e < nEnts; e++){
                int ID = (int)(((Attribute.Double)entities.getEntityArray()[e].getObject("ID")).getValue());
                writer.addData(ID);
                if(monthlyValuesWriting.getValue()){
                    for(int t = 1; t < tcounter; t++){
                        String dStr = String.format(Locale.US,"%.6f",valueMatrix[t][e]);
                        writer.addData(dStr);
                    }
                }
                
                //aggregated values
                //monthly values
                
                double aggSum = 0;
                for(int t = 0; t < 12; t++){
                    aggSum += (aggMatrix[t][e] / aggCounter[t]);
                    String dStr = String.format(Locale.US,"%.3f",(aggMatrix[t][e]) / aggCounter[t]);
                    writer.addData(dStr);
                }
                if (!type.getValue().equals("sum")){
                    aggSum = aggSum/12;
                }
                
                if(this.weight.getValue().equals("none")){
                    String dStr = String.format(Locale.US,"%.3f",(aggSum));
                    writer.addData(dStr);
                }else{
                    String dStr = String.format(Locale.US,"%.3f",aggSum);
                    writer.addData(dStr);
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
