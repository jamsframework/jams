/*
 * TimeSeriesCapturer.java
 * Created on 24. November 2005, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
import java.util.Vector;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.GregorianCalendar;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="Time series capturer",
        author="Peter Krause",
        description="Saves time series in the memory for later processing" +
        "e.g. plotting, efficiency calculation or whatever"
        )
        public class TimeSeriesCapturer extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The model time interval"
            )
            public Attribute.TimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The time interval to capture"
            )
            public Attribute.TimeInterval captureTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The months to be captured"
            )
            public Attribute.IntegerArray captureMonthList;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the timeSeries values to capture"
            )
            public Attribute.Double timeSeriesVal;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the captured time series"
            )
            public Attribute.DoubleArray timeSeries;
    
    private final int TOTAL_PERIOD = 0;
    private final int HYDROLOGICAL_YEAR = 1;
    private final int CALENDAR_YEAR = 2;
    
    private double[] captData;
    
    private int counter = 0;
    
    private int interValStart = 0;
    private int interValEnd = 0;
    
    private int effTsteps = 0;
    
    private boolean monthly = false;
    private int monthCount = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        //some checking if time intervals correlate well
        //....
        //....
        this.counter = 0;
        this.monthCount = 0;
        Attribute.Calendar model_sd = this.modelTimeInterval.getStart().clone();
        Attribute.Calendar model_ed = this.modelTimeInterval.getEnd().clone();
        int model_tres = this.modelTimeInterval.getTimeUnit();
        long sdMod = model_sd.getTimeInMillis();
        long edMod = model_ed.getTimeInMillis();
        long model_tsteps = 0;
        /*if(model_tres == model_sd.DAY_OF_YEAR){
            model_tsteps = (edMod - sdMod) / (1000 * 60 * 60 * 24);
            model_tsteps = model_tsteps + 1 + 1;
        }*/
        model_tsteps = modelTimeInterval.getNumberOfTimesteps();
        
        Attribute.Calendar eff_sd = this.captureTimeInterval.getStart().clone();
        Attribute.Calendar eff_ed = this.captureTimeInterval.getEnd().clone();
        int eff_tres = this.captureTimeInterval.getTimeUnit();
        long sdEff = eff_sd.getTimeInMillis();
        long edEff = eff_ed.getTimeInMillis();
        
        //check if effTimeInterval is in the bounds of the model time interval
        //otherwise it will be set to the model interval bounds
        if(eff_sd.before(model_sd)){
            this.captureTimeInterval.setStart(model_sd);
            getModel().getRuntime().println("capture Startdate was set equal to model startdate", JAMS.STANDARD);
        }
        if(model_ed.before(eff_ed)){
            this.captureTimeInterval.setEnd(model_ed);
            getModel().getRuntime().println("capture Enddate was set equal to model enddate", JAMS.STANDARD);
        }
        
        /*if(eff_tres == eff_sd.DAY_OF_YEAR){
            this.effTsteps = (int)((edEff - sdEff) / (1000 * 60 * 60 * 24));
            this.effTsteps = this.effTsteps + 1;
        }*/
        effTsteps = (int) captureTimeInterval.getNumberOfTimesteps();
        
        //System.out.println("TS:"+effTsteps);
        //int ts = (int)tsteps;
        int ts = (int) this.getContext().getNumberOfIterations();
        getModel().getRuntime().println("effStartdate:\t" + eff_sd.toString(), JAMS.VERBOSE);
        getModel().getRuntime().println("effEnddate:\t" + eff_ed.toString(), JAMS.VERBOSE);
        
        
        captData = new double[effTsteps];
        
        counter = 0;
        
        //determine start and end array index for timeInterval
        
        if(eff_tres == GregorianCalendar.DAY_OF_YEAR){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60 * 24));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == GregorianCalendar.HOUR_OF_DAY){
            this.interValStart =(int)((sdEff - sdMod) / (1000 * 60 * 60));
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == GregorianCalendar.MONTH){
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = captureTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(Attribute.Calendar.MONTH,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        } else if(eff_tres == GregorianCalendar.YEAR){
            Attribute.Calendar modStart = modelTimeInterval.getStart().clone();
            Attribute.Calendar effStart = captureTimeInterval.getStart().clone();
            int startStep = 0;
            while(modStart.before(effStart)){
                startStep++;
                modStart.add(Attribute.Calendar.YEAR,1);
            }
            this.interValStart = startStep;
            this.interValEnd = this.interValStart + this.effTsteps;
        }
        int junk = 0;
        
        if(this.captureMonthList != null){
            this.monthly = true;
        }
        
        this.timeSeries.setValue(captData);
    }
    
    public void run() {
        captData = this.timeSeries.getValue();
        boolean capture = false;
        if(this.time.equals(this.captureTimeInterval.getStart())){
            capture = true;
        } else if(this.time.after(this.captureTimeInterval.getStart())){
            if(this.time.before(this.captureTimeInterval.getEnd()))
                capture = true;
        } else if(this.time.equals(this.captureTimeInterval.getEnd())){
            capture = true;
        }
        
        if(capture){
            if(monthly){
                int month = time.get(Attribute.Calendar.MONTH) + 1;
                for(int i = 0; i < this.captureMonthList.getValue().length; i++){
                    if(month == this.captureMonthList.getValue()[i]){
                        this.captData[counter] = this.timeSeriesVal.getValue();
                        this.counter++;
                        this.monthCount++;
                    }
                }
            } else{
                this.captData[counter] = this.timeSeriesVal.getValue();
                this.counter++;
            }
            this.timeSeries.setValue(captData);
        }
    }
    
    public void cleanup() {
        /*double[] tsv = this.timeSeries.getValue();
        for(int i = 0; i < tsv.length; i++)
            System.out.println("tsv: " + tsv[i]);*/
    }
}
