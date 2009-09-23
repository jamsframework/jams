/*
 * ABCGradientDescent.java
 * Created on 30. Juni 2006, 15:12
 *
 * This file is part of JAMSConstants
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

package jams.components.optimizer.gradient;

import java.util.Random;
import java.util.StringTokenizer;
import jams.JAMSConstants;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class ABCGradientDescent extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of samples to be taken"
            )
            public JAMSInteger sampleCount;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "efficiency methods"
            )
            public JAMSString effMethodNames;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble[] effValues;        
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;

    int currentCount;
    Random generator;
    
    GenericDataWriter writer;
    
    double alpha = 1.0;
    double diff  = 1.0;
    
    double alpha_min = 0.0000000001;
    double diff_min = 0.0000001;
    double approxError = 0.01;
    
    public void init() {
        
//add more checks!!!
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(key);
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
    }
    
    private boolean hasNext() {
        if (alpha > alpha_min && diff > diff_min )
	    return true;
	return false;
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
		System.out.println(e.getMessage());
            }
        }
                
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private boolean IsSampleValid(JAMSDouble [] sample) {
	int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        	
        for(int i = 0; i < paras; i++){
            if(parameterNames[i].equals("abcModel.a") || parameterNames[i].equals("abcModel.b")){
                //either a or b has already been sampled!
                if(criticalPara){                    
                    double upperBound = 1.0 - criticalParaValue;
		    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upperBound )
			return false;
                }
                else{
                    //first criticalPara
		    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
			return false;
                    criticalPara = true;
                    criticalParaValue = sample[i].getValue();
                }
            }else{
                // all other parameters
		if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
		    return false;
            }
        }     
        return true;
    }
    
    public void run() {	
	
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }

	resetValues();
		
	double y1,y2;				
	
	for (int e = 0;e <effValues.length;e++) {	    		
	    //get inital vector
	    double [] x = abcRandomSampler();	
	    double [] grad = new double[this.parameters.length];	
	
	    alpha = 1.0;
	    diff  = 1.0;
	
	    
	    while (hasNext()) {
		//set current vector
		for (int i=0; i < this.parameters.length; i++) {	
		    parameters[i].setValue(x[i]);    	    
		}
		singleRun();
	    	    		
		y1 = this.effValues[e].getValue();
                    	
		//partial differences quotients
		for (int i=0; i < this.parameters.length; i++) {	
		    for (int j=0; j < this.parameters.length; j++) {	
			if (j == i) {
			    parameters[j].setValue(x[j]+approxError);			
			}		    
			else
			    parameters[j].setValue(x[j]);		
		    }	    
		    if ( !IsSampleValid(parameters) )
			grad[i] = 0;
		    else {
			//calculate
			singleRun();
		    
			y2 = this.effValues[e].getValue();
	
			grad[i] = (y2 - y1) / approxError;    
		    }		
		}		    

		//use armijo - method to obtain step width
		//decrease step - width until result is better than the last one
		
		//try to increase step - width
		alpha *= 4.0;
	    
		while (true) {		
		    for (int i=0; i < this.parameters.length; i++) {	
			parameters[i].setValue(x[i] + alpha*grad[i]);		    
		    }
		
		    if (this.IsSampleValid(parameters)) {
			singleRun();
		
			if (this.effValues[e].getValue() > y1)
			    break;
		    }
		    alpha /= 2.0;
		
		    if (alpha < alpha_min)
			break;
		}
		
		String info = "Gradient:\t";		
		for (int i=0; i < this.parameters.length; i++) {	
		    x[i] += alpha * grad[i];		    
		    info += grad[i] + "\t";
		}
		getModel().getRuntime().println(info);

		info = "Stelle:\t\t";
		for (int i=0; i < this.parameters.length; i++) {	
		    info += parameters[i].getValue() + "\t";
		}
		getModel().getRuntime().println(info);											
		getModel().getRuntime().println("Funktionswert:\t" + y1 + "\t Alpha: " + alpha);
	    }
	getModel().getRuntime().println("***************************************************************");
	getModel().getRuntime().println("***************************************************************");
	getModel().getRuntime().println("Optimierung für Maß Nummer: " + e + " abgeschlossen!!");
	getModel().getRuntime().println("***************************************************************");
	getModel().getRuntime().println("***************************************************************");
	}
    }               
}
