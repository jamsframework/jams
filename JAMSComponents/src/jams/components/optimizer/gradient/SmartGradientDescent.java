/*
 * DDMC.java
 * Created on 30. Juni 2006, 15:12
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

package jams.components.optimizer.gradient;

import java.util.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import java.io.*;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="GradientDescent",
        author="Christian Fischer",
        description="distance driven monte carlo optimization with gradient descent"
        )
        public class SmartGradientDescent extends JAMSContext {
    
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
            description = "efficiency methods"
            )
            public JAMSString effMethodName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values; note: only first value is optimized"
            )
            public JAMSDouble[] effValue;   
                           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "file with optimization information and best parameter set"
            )
            public JAMSString resultFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "workspace directory"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "number of monte carlo runs to determine current minimal distance"
            )
            public JAMSInteger MonteCarloParameter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "minimal distance until optimization is stopped"
            )
            public JAMSDouble MinimalDistance;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "worst efficiency which is accepted"
            )
            public JAMSDouble ValueBoundary;
     
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Minimization,Mazimation or absolute optimization"
            )
            public JAMSInteger OptimizationType;
    
        
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;  
    static final int ABSMINIMIZATION = 4;
    
    String[] parameterNames;
    JAMSDouble parameters[] = null;
    
    double[] lowBound;
    double[] upBound;
            
    Vector<double[]> pointList = null;
    int numVisitedPoints;

    double [] bestpoint;
    double bestvalue;
    double lowLipschitzBound;
    
    double alpha_min = 0.01;
    double diff_min = 0.0000001;
    double approxError = 0.01;
    
    Random generator;    
    BufferedWriter writer;
    
    public void init() {
        generator = new Random();
	generator.setSeed(System.currentTimeMillis());
	
	pointList = new Vector<double[]>(10000,0);
	numVisitedPoints = 0;
	//this is a first guess
	lowLipschitzBound = 1.0;
	
	TranslateToMaximization(ValueBoundary);
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
            parameters[i] = ((JAMSDouble)getModel().getRuntime().getDataHandles().get(key));
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
        tok = new StringTokenizer(effMethodName.getValue(), ";");
        String[] effNames = new String[tok.countTokens()];
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            effNames[i] = key;
            i++;
        }      
	
	if (effNames.length != 1) {
	     getModel().getRuntime().sendHalt("Cant process multiobjective optimization problems!!");
	}
			
	this.bestvalue = Double.NEGATIVE_INFINITY;
    }
    
    private double[] RandomSampler(){
        int paras = this.parameterNames.length;        
        double[] sample = new double[paras];
        	
        for(int i = 0; i < paras; i++)	{
            double d = generator.nextDouble();
            // all other parameters
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }     
        return sample;
    }

    private double CalcDistance(double x0[],double x1[]) {
	double dist = 0;
	for (int i=0;i<this.parameters.length;i++) {
	    dist += (x0[i] - x1[i])*(x0[i] - x1[i]);
	}
	return Math.sqrt(dist);
    }
    
    private void UpdateLipschitz(double []x1) {
	double x0[];
	double f0,f1;
	//this needs tooo much time, so skip it and hope our lower bound is good enough
	if (numVisitedPoints > 10000) 
	    return;
	
	for (int i=0;i<numVisitedPoints;i++) {
	    x0 = pointList.get(i);
	    
	    f0 = x0[this.parameters.length];
	    f1 = x1[this.parameters.length];
	    
	    double distance = CalcDistance(x0,x1);
	    
	    if (distance < 0.001)
		continue;
	    
	    double L = Math.abs((f1 - f0) / distance);
	    
	    if (this.lowLipschitzBound < L) {
		this.lowLipschitzBound = L;
		UpdateForbiddenCircles();
	    }
	}	
    }
    
    private void UpdateForbiddenCircles() {
	double x0[];
	double f0;
	
	for (int i=0;i<numVisitedPoints;i++) {
	       x0 = pointList.get(i);
	       f0 = x0[this.parameters.length];
	       
	       x0[this.parameters.length+1] = (f0 - bestvalue)/this.lowLipschitzBound;
	   } 
    }
    
    private void TranslateToMaximization(JAMSDouble value) {
	if (OptimizationType.getValue() == MAXIMIZATION) {
            //do nothing
	}
        else if (OptimizationType.getValue() == MINIMIZATION) {
	    value.setValue(-value.getValue());
	}
        else if (OptimizationType.getValue() == ABSMAXIMIZATION) {
            value.setValue(Math.abs(value.getValue()));
	}
        else if (OptimizationType.getValue() == ABSMINIMIZATION) {
            value.setValue(-Math.abs(value.getValue()));
	}
    }
    private boolean singleRun() {
        double nextPoint[] = new double[this.parameters.length + 2];
	
	for (int i=0;i<this.parameters.length;i++) {
	    if ( (!(parameters[i].getValue() < 0) && !(parameters[i].getValue() > 0)) ||
		 ( (parameters[i].getValue() < 0) &&  (parameters[i].getValue() > 0)) ) {
		System.out.println("Single Run Failed!");
		return false;
	    }
	    nextPoint[i] = parameters[i].getValue();	    
	}
	
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
		System.out.println(e.toString());
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
	
	TranslateToMaximization(this.effValue[0]);
		
	//if efficiency is to bad adjust it otherwise lipschitzbound will get to bad
	if (this.effValue[0].getValue() < this.ValueBoundary.getValue()) {
	    this.effValue[0].setValue(this.ValueBoundary.getValue());
	}
	
	double f0 = this.effValue[0].getValue();
			
	nextPoint[this.parameters.length] = f0;			
	
	UpdateLipschitz(nextPoint);
		
	nextPoint[this.parameters.length+1] = Math.abs((f0 - bestvalue)/this.lowLipschitzBound);
		
	pointList.add(nextPoint);
	numVisitedPoints++;
	
	//check if this is a new best point	
	if (f0 > bestvalue) {
	    bestvalue = f0;	    
	    this.bestpoint = nextPoint;
	    UpdateForbiddenCircles();
	    
	    try {
		
		double realValue;
		if (this.OptimizationType.getValue() == MINIMIZATION || 
		    this.OptimizationType.getValue() == ABSMINIMIZATION)
		    realValue = -this.bestvalue;
		else
		    realValue = this.bestvalue;
		
		String output = "A new best point has been found! Value: " + realValue;
		getModel().getRuntime().println(output);
		
		writer.write(output);
		writer.newLine();		    
		
		output = "Parameters: ";
		
		for (int k=0;k<this.parameters.length;k++) {
		    output += this.bestpoint[k] + ",";		    
		}
		getModel().getRuntime().println(output);
		
		writer.write(output);
		writer.newLine();
		writer.flush();
	    }
	    catch(Exception e) {
	        System.out.println("Could not write to output file because:" + e.toString());
	    }	    
	}
	return true;
    }
        
    private boolean IsSampleValid(JAMSDouble [] sample) {
	int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0; 
        	
        for(int i = 0; i < paras; i++){          
	    // all other parameters
	    if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
	        return false;
        }     
        return true;
    }
    
    //calc distance to all other points
    private double calcMinimalDist(double x[]) {	
	double dist = 0;
	double mindist = Double.POSITIVE_INFINITY;
		
	for (int i=0;i<numVisitedPoints;i++) {
	    double otherpoint[] = pointList.get(i);
	    
	    dist = this.CalcDistance(otherpoint,x);	    	   	    
	    dist -= otherpoint[this.parameters.length+1];
	    
	    if (dist < 0)
		return 0;
	    
	    if (dist < mindist) {
		mindist = dist;
	    }
	}
	return mindist;
    }
    
    public void GradientDescent(double x[]) {
	double y1,y2,alpha,diff;						
	double [] grad = new double[this.parameters.length];	
	
	alpha = 0.1;
	diff  = 1.0;
	
	while ( alpha > alpha_min && diff > diff_min ) {
	    //set current vector
	    for (int i=0; i < this.parameters.length; i++) {	
		parameters[i].setValue(x[i]);    	    
	    }
	    
	    if (!singleRun())
		return;
	    	    		
	    y1 = this.effValue[0].getValue();
	    
	    //bad point??
	    if (y1 <= this.ValueBoundary.getValue())
		return;
	    
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
		    if (!singleRun())
			return;
		    
		    y2 = this.effValue[0].getValue();
	
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
		    if (!singleRun())
			return;
		
		if (this.effValue[0].getValue() > y1)
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
    }
    
    public void run() {	
	String output;
	
	//try to open output file
	try {
	    writer = new BufferedWriter(new FileWriter(this.dirName.getValue() + resultFile.getValue()));
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	}
			
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
	
	while (true) {
	    double x[] = null;
	    double distance = -1.0;
	    //generate n new samples
	    for (int i=0;i<MonteCarloParameter.getValue();i++) {
		double mc_point[] = RandomSampler();
		
		//calculate distance to other samples
		double distanceOfMCPoint = calcMinimalDist(mc_point);
		if (distanceOfMCPoint > distance) {
		    x = mc_point;
		    distance = distanceOfMCPoint;
		}
	    }
	    	    	    
	    output = "current minimal distance: " + distance + "\n" + "lowerlipschitzbound: " + this.lowLipschitzBound;
	    
	    getModel().getRuntime().println(output);
	    try {
		writer.newLine();
		writer.write(output);
		writer.newLine();
	    }
	    catch(Exception e) {
		System.out.println("Could not write to output file because:" + e.toString());
	    }
	    
	    if (distance < MinimalDistance.getValue()) {
		output = "optimization has stopped because: MinimalDistance has reached limit";
		getModel().getRuntime().println(output);
		try {
		    writer.write(output);
		    writer.newLine();
		}
		catch(Exception e) {
		    System.out.println("Could not write to output file because:" + e.toString());
		}
		break;
	    }	    	   	   
	    GradientDescent(x);	    	  
	}
	
	try {
	
	output = "Result of Optimization: Value: " + this.bestvalue;
	getModel().getRuntime().println(output);
	writer.write(output);
	writer.newLine();		    
		
	output = "Parameters: ";
		
	for (int k=0;k<this.bestpoint.length;k++) {
	    output += this.bestpoint[k] + ",";		    
	}
	getModel().getRuntime().println(output);
		
	writer.write(output);
	writer.newLine();
	writer.flush();
    
	writer.close();
	}
	catch (Exception e) {
	    System.out.println("Could not close output file because:" + e.toString());
	}
    }               
}
