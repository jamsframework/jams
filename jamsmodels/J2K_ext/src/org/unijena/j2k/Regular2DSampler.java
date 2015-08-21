/*
 * RandomParaSampler.java
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
        description="Samples a 2D parameter space in a regular way"
        )
        public class Regular2DSampler extends JAMSContext {
    
    /*
     *  Component variables
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of two parameter identifiers to be sampled"
            )
            public Attribute.String parameterIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public Attribute.String boundaries;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "resolution x"
            )
            public Attribute.Integer resX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "resolution y"
            )
            public Attribute.Integer resY;
    
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
    
    Attribute.Double[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;
    int currentCount;
    Random generator;
    GenericDataWriter paraWriter;
    GenericDataWriter attribWriter;
    double[][] valueArray;
    int timeStepCounter = 0;
    int runCounter = 0;
    int timeSteps = 0;
    
    double[] stepSize;
    int[] currentStep;
    //double[] currentVal;
    int currentXStep = 0;
    int sampleCount = 0;
    
    private boolean hasNext() {
        int sampleCount = resX.getValue() * resY.getValue();
        return currentCount < sampleCount;
    }
    public void init() {
        if(this.enable.getValue()){
            //add more checks!!!
            //retreiving parameter names
            int i;
            StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
            String key;
            parameters = new Attribute.Double[tok.countTokens()];
            parameterNames = new String[tok.countTokens()];
            
            i = 0;
            while (tok.hasMoreTokens()) {
                key = tok.nextToken();
                parameterNames[i] = key;
                parameters[i] = (Attribute.Double) getModel().getRuntime().getDataHandles().get(key);
                i++;
            }
            
            //retreiving boundaries
            tok = new StringTokenizer(boundaries.getValue(), ";");
            int n = tok.countTokens();
            lowBound = new double[n];
            upBound = new double[n];
            
            //check if number of parameter ids and boundaries match
            if (n != i) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": Different number of parameterIDs and boundaries!");
            }
            
            i = 0;
            while (tok.hasMoreTokens()) {
                key = tok.nextToken();
                key = key.substring(1, key.length()-1);
                
                StringTokenizer boundTok = new StringTokenizer(key, ">");
                lowBound[i] = Double.parseDouble(boundTok.nextToken());
                upBound[i] = Double.parseDouble(boundTok.nextToken());
                
                //check if upBound is higher than lowBound
                if (upBound[i] <= lowBound[i]) {
                    getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
                }
                
                i++;
            }
            
            //retreiving effMethodNames
            i = 0;
            tok = new StringTokenizer(effMethodNames.getValue(), ";");
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
            
            for(int j = 0; j < this.parameters.length; j++)
                paraWriter.addColumn(this.parameterNames[j]);
            
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
            sampleCount = this.resX.getValue() * this.resY.getValue();
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
            this.stepSize = new double[this.parameters.length];
            this.stepSize[0] = (upBound[0] - lowBound[0]) / (this.resX.getValue() - 1);
            this.stepSize[1] = (upBound[1] - lowBound[1]) / (this.resY.getValue() - 1);
            
            this.currentStep = new int[this.parameters.length];
            
            for(i = 0; i < this.parameters.length; i++){
                this.parameters[i].setValue(lowBound[i]);
                this.currentStep[i] = 0;
                
            }
        }
    }
    
    private void updateValues() {
        int sampleCount = resX.getValue() * resY.getValue();
        int count = this.currentCount + 1;
        getModel().getRuntime().println("Run No. " + count + " of " + sampleCount);
        //double[] sample = this.regularSampler();
        if(currentCount > 0){
            if(this.currentXStep < resX.getValue()){
                this.parameters[0].setValue(parameters[0].getValue() + stepSize[0]);
            } else{
                this.parameters[1].setValue(parameters[1].getValue() + stepSize[1]);
                this.parameters[0].setValue(this.lowBound[0]);
                this.currentXStep = 0;
            }
        }
        getModel().getRuntime().println("Para: " + parameterNames[0] + " = " + parameters[0]);
        getModel().getRuntime().println("Para: " + parameterNames[1] + " = " + parameters[1]);
        currentCount++;
        this.currentXStep++;
    }
    
    
    
    
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        
        for (int i = 0; i < parameters.length; i++) {
            
            parameters[i].setValue(lowBound[i]);
        }
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
                for(int i = 0; i < this.parameters.length; i++)
                    paraWriter.addData(this.parameters[i].getValue());
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
            int sampleCount = this.resX.getValue() * this.resY.getValue();
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
