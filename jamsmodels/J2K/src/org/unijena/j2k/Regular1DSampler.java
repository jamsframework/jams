/*
 * Regular1DSampler.java
 * Created on 10. Mai 2006, 17:03
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
import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
        title="Regular2dParameterSampler",
        author="Peter Krause",
        description="Samples one parameter over its range in a regular way"
        )
        public class Regular1DSampler extends JAMSContext {
    
    /*
     *  Component variables
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Parameter identifiers to be sampled"
            )
            public Attribute.String parameterID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Parameter value bounaries corresponding to parameter identifiers"
            )
            public Attribute.String boundaries;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "resolution"
            )
            public Attribute.Integer resolution;
    
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
            description = "Flag for enabling this sampler"
            )
            public Attribute.Boolean enable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String paraFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String attribFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The model time interval"
            )
            public Attribute.TimeInterval modelTimeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file header descriptions"
            )
            public Attribute.String attribHeader;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Output file attribute"
            )
            public Attribute.DoubleArray targetValue;
    
    Attribute.Double parameter;
    String parameterName;
    double lowBound;
    double upBound;
    int currentCount;
    Random generator;
    GenericDataWriter paraWriter;
    GenericDataWriter attribWriter;
    double[][] valueArray;
    int timeStepCounter = 0;
    int runCounter = 0;
    int timeSteps = 0;
    
    double stepSize;
    int currentStep = 0;
    //double[] currentVal;
    
    int sampleCount = 0;
    
    private boolean hasNext() {
        int sampleCount = resolution.getValue();
        return currentCount < sampleCount;
    }
    public void init() {
        if(this.enable.getValue()){
            //add more checks!!!
            //retreiving parameter names
            int i;
            parameterName = this.parameterID.getValue();
            parameter = (Attribute.Double) getModel().getRuntime().getDataHandles().get(parameterName);
                        
            String key = this.boundaries.getValue().substring(1, boundaries.getValue().length()-1);
            StringTokenizer boundTok = new StringTokenizer(key, ">");
            lowBound = Double.parseDouble(boundTok.nextToken());
            upBound = Double.parseDouble(boundTok.nextToken());
            
            //check if upBound is higher than lowBound
            if (upBound <= lowBound) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
            }
            
            //retreiving effMethodNames
            i = 0;
            StringTokenizer tok = new StringTokenizer(effMethodNames.getValue(), ";");
            String[] effNames = new String[tok.countTokens()];
            i = 0;
            while (tok.hasMoreTokens()) {
                key = tok.nextToken();
                effNames[i] = key;
                i++;
            }
            
            //create para output file
            paraWriter = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+this.paraFileName.getValue());
            paraWriter.addColumn("Run");
            
            paraWriter.addColumn(this.parameterName);
            
            for(int e = 0; e < effNames.length; e++){
                paraWriter.addColumn(effNames[e]);
            }
            
            
            paraWriter.writeHeader();
            
            //the attribute output file
            attribWriter = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+attribFileName.getValue());
            
            attribWriter.addComment("J2K model output");
            attribWriter.addComment("");
            
            //always write time
            attribWriter.addColumn("date/time");
            sampleCount = this.resolution.getValue();
            for(int s = 0; s < this.sampleCount; s++){
                int counter = s + 1;
                attribWriter.addColumn(attribHeader.getValue() + "_run_" + counter);
            }
            
            
            attribWriter.writeHeader();
            
            //setting up the dataArray
            this.timeSteps = (int)modelTimeInterval.getNumberOfTimesteps();
            this.valueArray = new double[sampleCount][timeSteps];
            this.timeStepCounter = 0;
            this.runCounter = 0;
            
            //determine x and y stepSize
            this.stepSize = (upBound - lowBound) / (this.resolution.getValue() - 1);
            this.parameter.setValue(lowBound);
            this.currentStep = 0;
        }
    }
    
    private void updateValues() {
        int sampleCount = resolution.getValue();
        int count = this.currentCount + 1;
        getModel().getRuntime().println("Run No. " + count + " of " + sampleCount);
        //double[] sample = this.regularSampler();
        if(currentCount > 0){
            if(this.currentStep < resolution.getValue()){
                this.parameter.setValue(parameter.getValue() + stepSize);
            } 
        }
        getModel().getRuntime().println("Para: " + parameterName + " = " + parameter);
        currentCount++;
        this.currentStep++;
    }
    
    
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        parameter.setValue(lowBound);
        
        currentCount = 0;
        
        
    }
    
    private void singleRun() {
        
        System.gc();
        long start = System.currentTimeMillis();
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.updateInit();
            try {
                comp.init();
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
        long end = System.currentTimeMillis();
        getModel().getRuntime().println("Exec time: " + (end-start) + " ms", JAMS.STANDARD);
    }
    
    public void run() {
        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        if (!enable.getValue()) {
            singleRun();
        } else {
            resetValues();
            while (hasNext()) {
                updateValues();
                singleRun();
                
                paraWriter.addData(currentCount);
                
                paraWriter.addData(this.parameter.getValue());
                for(int e = 0; e < effValues.length; e++)
                    paraWriter.addData(this.effValues[e].getValue());
                try{
                    paraWriter.writeData();
                    paraWriter.flush();
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
            int sampleCount = this.resolution.getValue();
            Attribute.Calendar timeStamp = this.modelTimeInterval.getStart();
            for(int t = 0; t <= this.timeSteps; t++){
                attribWriter.addData(timeStamp.toString());
                timeStamp.add(modelTimeInterval.getTimeUnit(), 1);
                for(int r = 0; r < sampleCount; r++){
                    attribWriter.addData(this.valueArray[r][t]);
                }
                try {
                    attribWriter.writeData();
                } catch (jams.runtime.RuntimeException jre) {
                    getModel().getRuntime().println(jre.getMessage());
                }
            }
            attribWriter.close();
            paraWriter.close();
        }
    }
    
}
