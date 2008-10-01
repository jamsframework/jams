/*
 * PredictionNETCreator.java
 * Created on 12. Mai 2006, 17:41
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

package jams.components.machineLearning;

import jams.model.*;
import jams.data.*;
import java.util.*;
import java.io.*;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class PredictionControl extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Precip Data"
            )
             public JAMSString datafile;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger ExampleLength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
             public JAMSInteger numOfExamples;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger trainEnd;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationStart;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger validationEnd;
                          
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger relevantTime;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger TrainLengthForOptimizing;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean doOptimizing;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity trainData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity validationData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger dataShift;
                                        
    HashMap<Integer, double[]> rawData;    
    HashMap<Integer, double[]> rawPredict;

    double data[][];
    double predict[];
    
    double traindata[][];
    double trainpredict[];
          
    double optimizedata[][];
    double optimizepredict[];
    
    double validationdata[][];
    double validationpredict[];
    int RelevantTime = 1;
    
    int ExamplLength,numOfExampl;
    
    public void loadData() throws JAMSEntity.NoSuchAttributeException {  
	BufferedReader reader = null;
	HashMap<Integer, double[]> rawData = new HashMap<Integer,double[]>();
	HashMap<Integer, double[]> rawPredict = new HashMap<Integer,double[]>();
	
	ExamplLength = ExampleLength.getValue();
	numOfExampl  = numOfExamples.getValue();
	RelevantTime = this.relevantTime.getValue();
	
        try {
            reader = new BufferedReader(new FileReader(datafile.getValue()));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
	String nextString = null;
	try {
	int i = 0;
	while ((nextString = reader.readLine()) != null) {	    	    
	    StringTokenizer st = new StringTokenizer(nextString, "\t");
	    double[] Example = new double[ExamplLength];
	    double[] Predict = new double[1];
	    
	    try {
	    for (int j = 0; j < ExamplLength; j++) {		
		Example[j] = (new Double(st.nextToken())).doubleValue();
	    }
	    Predict[0] = (new Double(st.nextToken())).doubleValue();
	    } catch(Exception e) {
		System.out.println("Error in Dataset: " + i + " stop reading! (not enough tokens)");
		break;
	    }	    	    	    	    
	    if (st.hasMoreTokens()) {
		System.out.println("Error in Dataset: " + i + " stop reading! (too many tokens)");
		break;
	    }
	    
	    rawData.put(new Integer(i),Example);
	    rawPredict.put(new Integer(i),Predict);
	    
	    i++;
	}
	
	} catch (IOException ioe) {
            JAMS.handle(ioe);
        }
					
	data = new double[numOfExampl][RelevantTime*ExamplLength];
	predict = new double[numOfExampl];
	
	for (int i=0;i<numOfExampl;i++) {
	    for (int j=0;j<RelevantTime;j++) {
		double entry[] = rawData.get(new Integer(i+j));
		for (int k=0;k<ExamplLength;k++) {
		    data[i][j*ExamplLength+k] = entry[k];
		}
	    }
	    if (i+RelevantTime-1+dataShift.getValue() < 0) {
		System.out.println("Warning: Dataset: " + i + "contains no prediction!!");
		continue;
	    }
	    predict[i] = rawPredict.get(new Integer(i+RelevantTime-1+dataShift.getValue()))[0];
	}
	
	traindata = new double[trainEnd.getValue() - trainStart.getValue()][RelevantTime*ExamplLength];
	trainpredict = new double[trainEnd.getValue() - trainStart.getValue()];
	
	for (int i=trainStart.getValue();i<trainEnd.getValue();i++) {
	    traindata[i-trainStart.getValue()] = data[i];
	    trainpredict[i-trainStart.getValue()] = predict[i];
	}
	
	validationdata = new double[validationEnd.getValue() - validationStart.getValue()][RelevantTime*ExamplLength];
	validationpredict = new double[validationEnd.getValue() - validationStart.getValue()];
	
	for (int i=validationStart.getValue();i<validationEnd.getValue();i++) {
	    validationdata[i-validationStart.getValue()] = data[i];
	    validationpredict[i-validationStart.getValue()] = predict[i];
	}	
				
	validationData.setObject("data",validationdata);
	validationData.setObject("predict",validationpredict);
	
	if (doOptimizing.getValue()) {
	    SelectOptimalTrainingSet(this.TrainLengthForOptimizing.getValue());
	    
	    trainData.setObject("data",optimizedata);
	    trainData.setObject("predict",optimizepredict);
	}
	else {
	    trainData.setObject("data",traindata);
	    trainData.setObject("predict",trainpredict);
	}
    }
    
    public void SelectOptimalTrainingSet(int goal) {
	int set[] = new int[goal];
	
	double variance[] = new double[trainEnd.getValue()];
	boolean inSet[] = new boolean[trainEnd.getValue()];
	for (int j=trainStart.getValue();j<trainEnd.getValue();j++) {
	    variance[j] = 0.0;
	    inSet[j] = false;
	}
	inSet[0] = true;
	set[0] = 0;
	for (int i=1;i<goal;i++) {
	    int bestIndex = -1;
	    double bestValue = -1.0;
	    
	    for (int j=trainStart.getValue();j<trainEnd.getValue();j++) {
		//test if data allready in set
		/*boolean allreadyIn = false;
		double min = 0;//Double.MAX_VALUE;
		
		for (int k=0;k<i;k++) {				    
		    double sum = 0;
		    for (int l=0;l<this.ExamplLength;l++) {
			sum += (data[j][l] - data[set[k]][l])*(data[j][l] - data[set[k]][l]);
		    }
		    //if (sum < min)
			min += sum;
		    
		    if (set[k] == j) {
			allreadyIn = true;
			break;
		    }
		}*/
		if (inSet[j])
		    continue;
		for (int l=0;l<this.ExamplLength;l++) {
		    variance[j] += (data[j][l] - data[set[i-1]][l])*(data[j][l] - data[set[i-1]][l]);
		}
		if (variance[j] > bestValue) {
		    bestValue = variance[j];
		    bestIndex = j;
		}
	    }
	    inSet[bestIndex] = true;
	    set[i] = bestIndex;	 
	    System.out.println("Take:" + set[i]);
	}
	
	optimizedata = new double[goal][RelevantTime*ExamplLength];
	optimizepredict = new double[goal];
	
	for (int i=0;i<goal;i++) {
	    optimizedata[i] = data[set[i]];
	    optimizepredict[i] = predict[set[i]];
	}
    }
    
    public void init() throws JAMSEntity.NoSuchAttributeException {                	
	loadData();
    }
    public void run() throws JAMSEntity.NoSuchAttributeException {                	
    }
}
