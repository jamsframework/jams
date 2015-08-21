/*
 * ABCRandomParaSampler.java
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

package org.unijena.abc;

import java.util.Random;
import java.util.StringTokenizer;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class ABCRandomParaSampler extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.String parameterIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public Attribute.String boundaries;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of samples to be taken"
            )
            public Attribute.Integer sampleCount;
    
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
            description = "Flag for disabling this sampler"
            )
            public Attribute.Boolean disable;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String fileName;
    
    Attribute.Double[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;
    int currentCount;
    Random generator;
    GenericDataWriter writer;
    
    
    public void init() {
        
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
        
        //create output file
        writer = new GenericDataWriter(this.fileName.getValue());
        writer.addColumn("Run");
        
        for(int j = 0; j < this.parameters.length; j++)
            writer.addColumn(this.parameterNames[j]);
        
        for(int e = 0; e < effNames.length; e++){
            writer.addColumn(effNames[e]);
        }
        
        
        writer.writeHeader();
        
    }
    
    private boolean hasNext() {
        return (currentCount < sampleCount.getValue()) && doRun;
    }
    
    private void updateValues() {
        getModel().getRuntime().println("Run No. " + this.currentCount + " of " + this.sampleCount.getValue());
        double[] sample = null;
        sample = abcRandomSampler();
        
        for (int i = 0; i < parameters.length; i++) {
            parameters[i].setValue(sample[i]);
        }
        
        currentCount++;
    }
    
    
    private double[] abcRandomSampler(){
        int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        double[] sample = new double[paras];
        
        for(int i = 0; i < paras; i++){
            if(parameterNames[i].equals("abcModel.a") || parameterNames[i].equals("abcModel.b")){
                //either a or b has already been sampled!
                if(criticalPara){
                    double d = generator.nextDouble();
                    double upperBound = 1.0 - criticalParaValue;
                    sample[i] = (lowBound[i] + d * (upperBound-lowBound[i]));
                }
                else{
                    //first criticalPara
                    double d = generator.nextDouble();
                    sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
                    criticalPara = true;
                    criticalParaValue = sample[i];
                }
            }else{
                double d = generator.nextDouble();
                // all other parameters
                sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
            }
            getModel().getRuntime().sendInfoMsg("Para: " + parameterNames[i] + " = " + sample[i]);
        }
        
        return sample;
    }
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        generator = new Random(System.currentTimeMillis());
        for (int i = 0; i < parameters.length; i++) {
            double d = generator.nextDouble();
            parameters[i].setValue(lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        currentCount = 0;
    }
    
    private void singleRun() {
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
        
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
    
    public void run() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        if (disable.getValue()) {    
            singleRun();
        } 
        else {
            resetValues();
            while (hasNext()) {             
                updateValues();
                singleRun();
                
                writer.addData(currentCount);
                for(int i = 0; i < this.parameters.length; i++)
                    writer.addData(this.parameters[i].getValue());
                for(int e = 0; e < effValues.length; e++)
                    writer.addData(this.effValues[e].getValue());
                try{
                    writer.writeData();
                }catch(org.unijena.jams.runtime.RuntimeException e){
                    
                }
               
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                JAMSComponent comp = runEnumerator.next();
            }
        }
    }
    
    
    
    
    public void cleanup() {
        if (!disable.getValue()) {
            /*System.out.println("overall max. goodness: " + bestGoodness);
            for (int i = 0; i < parameters.length; i++) {
                System.out.println("value["+i+"]: " + bestValues[i]);
            }*/
            writer.close();
        }
    }
    
}
