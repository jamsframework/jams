/*
 * JAMSParaSampler.java
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
        public class ABCRegularSampler extends JAMSContext {
    
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
            description = "sample resolution"
            )
            public Attribute.Integer sampleCount;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "efficiency criteria"
            )
            public Attribute.Double[] effMethod;
    
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
    double[] lowBound;
    double[] upBound;
    double[] stepSize;
    
    int currentCount;
    Random generator;
    double bestGoodness = Double.MAX_VALUE;
    double bestMinimum = Double.MAX_VALUE;
    double[] bestValues;
    GenericDataWriter writer;
    
    
    private boolean hasNext() {
        return currentCount < sampleCount.getValue();
    }
    
    private void updateValues() {
        System.out.println("Run No. " + this.currentCount + " of " + this.sampleCount.getValue());
        //set parameter values to corresponding lower boundaries
        double[] sample = null;
        if(parameters.length == 1){
           sample = new double[1];
           double d = generator.nextDouble();
            //parameter a
            sample[0] = (lowBound[0] + d * (upBound[0]-lowBound[0])); 
        }
        if(parameters.length == 2)
            sample = abRandomSampler();
        else if(parameters.length == 3)
            sample = abcRandomSampler();
        
        for (int i = 0; i < parameters.length; i++) {
            double d = generator.nextDouble();
            parameters[i].setValue(sample[i]);//lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        
        currentCount++;
    }
    private double[] abRandomSampler(){
        double[] sample = new double[2];
        //sample value a
        double d = generator.nextDouble();
        //parameter a
        sample[0] = (lowBound[0] + d * (upBound[0]-lowBound[0]));
        //parameter b
        d = generator.nextDouble();
        sample[1] = (lowBound[1] + d * (upBound[1]-lowBound[1]-sample[0]));
        
        getModel().getRuntime().sendInfoMsg("a: " + sample[0]);
        getModel().getRuntime().sendInfoMsg("b: " + sample[1]);
        
        return sample;
    }
    private double[] abcRandomSampler(){
        double[] sample = new double[3];
        double d = generator.nextDouble();
        //parameter b
        sample[1] = (lowBound[1] + d * (upBound[1]-lowBound[1]));
        //parameter b
        
        d = generator.nextDouble();
        sample[0] = (lowBound[0] + d * (upBound[0]-lowBound[0]-sample[1]));
        /*
        do {
            d = generator.nextDouble();
            sample[1] = (lowBound[1] + d * (upBound[1]-lowBound[1]));
        } while (sample[0] + sample[1] > 1);
         */
        
        // parameter c
        d = generator.nextDouble();
        sample[2] = (lowBound[2] + d * (upBound[2]-lowBound[2]));
        
        getModel().getRuntime().sendInfoMsg("a: " + sample[0]);
        getModel().getRuntime().sendInfoMsg("b: " + sample[1]);
        getModel().getRuntime().sendInfoMsg("c: " + sample[2]);
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
        /*double[] effValues = new double[this.effMethod.length];//
        for(int i = 0; i < effValues.length; i++)
            effValues[i] = this.effMethod[i].getValue();*/
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        if (disable.getValue()) {
            
            singleRun();
            
        } else {
            
            resetValues();
            
            while (hasNext()) {
                
                updateValues();
                
                singleRun();
                
                //JAMS.sendInfoMsg("Goodness: " + goodness);
                //write outputFile
                writer.addData(currentCount);
                for(int i = 0; i < this.parameters.length; i++)
                    writer.addData(this.parameters[i].getValue());
                for(int e = 0; e < effMethod.length; e++)
                    writer.addData(this.effMethod[e].getValue());
                try{
                    writer.writeData();
                }catch(org.unijena.jams.runtime.RuntimeException e){
                    
                }
                /*
                double minimumCrit = Math.abs(1 - goodness.getValue());
                
                if(minimumCrit < bestMinimum){
                    //if (goodness.getValue() > bestGoodness) {
                    bestMinimum = Math.abs(1 - goodness.getValue());
                    bestGoodness = goodness.getValue();
                    for (int i = 0; i < parameters.length; i++) {
                        bestValues[i] = parameters[i].getValue();
                    }
                    
                }*/
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                JAMSComponent comp = runEnumerator.next();
                try {
                    //comp.cleanup();
                } catch (Exception e) {
                    //JAMS.handle(e, comp.getInstanceName());
                }
            }
            
            //System.out.println("Goodness: " + goodness);
        }
    }
    
    
    public void init() {
        
//add more checks!!!
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new Attribute.Double[tok.countTokens()];
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameters[i++] = (Attribute.Double) getModel().getRuntime().getDataHandles().get(key);
        }
        
        tok = new StringTokenizer(boundaries.getValue(), ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];
        stepSize = new double[n];
        bestValues = new double[n];
        
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
        
        
        
        //create output file
        writer = new GenericDataWriter(this.fileName.getValue());
        writer.addColumn("Run");
        
        for(int j = 0; j < this.parameters.length; j++)
            writer.addColumn("para_" + j);
        
        writer.addColumn("e2");
        writer.addColumn("le2");
        
        writer.writeHeader();
        
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
