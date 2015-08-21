/*
 * MultipleModelRunContext.java
 * Created on 10. July 2007, 17:03
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

package org.unijena.j2k;

import java.util.Random;
import java.util.StringTokenizer;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
title="InErrorContext",
        author="Peter Krause",
        description="Context component which helps in estimating the uncertainty" +
        "which results e.g. from problems with the input data. The component does" +
        "only let the model run for a specified number of times and helps in " +
        "producing specific model output."
        
        )
        public class MultipleModelRunContext extends JAMSContext {
    
    /*
     *  Component variables
     */

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Number of model evaluations"
            )
            public Attribute.Integer modelRuns;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "efficiency methods"
            )
            public Attribute.String effMethodNames;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "efficiency values"
            )
            public Attribute.Double[] effValues;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Flag for dis/enabling this sampler"
            )
            public Attribute.Boolean enable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String effFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String targetFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The model time interval"
            )
            public Attribute.TimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Output file attribute"
            )
            public Attribute.DoubleArray targetValue;
   
    Random generator = new Random();
    GenericDataWriter effWriter;
    GenericDataWriter targetWriter;
    int currentCount; 
    double[][] valueArray;
    int timeStepCounter = 0;
    int runCounter = 0;
    int timeSteps = 0;
    
    public void init() {
        if(enable.getValue()){

            //retreiving effMethodNames
            int i = 0;
            StringTokenizer tok = new StringTokenizer(effMethodNames.getValue(), ";");
            String[] effNames = new String[tok.countTokens()];
            i = 0;
            while (tok.hasMoreTokens()) {
                String key = tok.nextToken();
                effNames[i] = key;
                i++;
            }
            
            //create efficiency output file
            effWriter = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+this.effFileName.getValue());
            effWriter.addColumn("Run");
            
            for(int e = 0; e < effNames.length; e++){
                effWriter.addColumn(effNames[e]);
            }
            
            effWriter.writeHeader();
            
            //the targetValue output file
            targetWriter = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+targetFileName.getValue());
            
            targetWriter.addComment("J2K model output");
            targetWriter.addComment("");
            
            //always write time
            targetWriter.addColumn("date/time");
            
            for(int s = 0; s < this.modelRuns.getValue(); s++){
                int counter = s + 1;
                targetWriter.addColumn("Run_" + counter);
            }
            
            targetWriter.writeHeader();
            
            //setting up the dataArray
            this.timeSteps = (int)modelTimeInterval.getNumberOfTimesteps();
            this.valueArray = new double[this.modelRuns.getValue()][timeSteps];
            this.timeStepCounter = 0;
            this.runCounter = 0;
        }
    }
    
    public void run() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        //component disabled, just a normal model run
        if (!enable.getValue()) {
            singleRun();  
        } 
        //component enabled
        else{
            while (hasNext()) {
                singleRun();     
                effWriter.addData(currentCount);
                for(int e = 0; e < effValues.length; e++)
                    effWriter.addData(this.effValues[e].getValue());
                try{
                    effWriter.writeData();
                    effWriter.flush();
                }catch(jams.runtime.RuntimeException e){
                    
                }
         
                this.valueArray[runCounter] = this.targetValue.getValue();
                this.runCounter++;
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                Component comp = runEnumerator.next();
            }
        }
    }
    
    public void cleanup() {
        
        if (enable.getValue()) {
            
            //always write time
            //the time also knows a toString() method with additional formatting parameters
            //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")
            Attribute.Calendar timeStamp = this.modelTimeInterval.getStart();
            for(int t = 0; t < this.timeSteps; t++){
                targetWriter.addData(timeStamp.toString());
                timeStamp.add(modelTimeInterval.getTimeUnit(), 1);
                for(int r = 0; r < this.modelRuns.getValue(); r++){
                    targetWriter.addData(this.valueArray[r][t]);
                }
                try {
                    targetWriter.writeData();
                } catch (jams.runtime.RuntimeException jre) {
                    getModel().getRuntime().println(jre.getMessage());
                }
            }
            targetWriter.flush();
            targetWriter.close();
            effWriter.close();
        }
    }
    
    private void singleRun() {     
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            this.currentCount++;
            //comp.updateInit();
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.initAll();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
    
    private boolean hasNext() {
        return currentCount < modelRuns.getValue();
    }
}
