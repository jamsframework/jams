/*
 * CrossValidation.java
 *
 * Created on 3. Juli 2007, 12:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author Christian(web)
 */
public class CrossValidation extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger k;
           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity Data;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity trainingData;
    
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
            public JAMSBoolean enable;
     
     public CrossValidation() {
	 
     }
     
     public void init() {  
     
     }
     
     private void singleRun() { 
	if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
    }
     
    public void run() {  
        if (enable != null){
            if (enable.getValue() == false){
                singleRun();
                return;
            }
        }
        
        if (k.getValue() <= 0){
            this.getModel().getRuntime().sendHalt("Number of Crossvalidation Iterations less or equal zero!!");
            return;
        }
        
	double data[][] = null;
	double predict[] = null;
	try {
	    data =(double[][])Data.getObject("data");
	    predict = (double[])Data.getObject("predict");
	}
	catch(Exception e) {
            this.getModel().getRuntime().sendHalt("Keine Datens^tze gefunden!!" + e.toString());
	}
        this.getModel().getRuntime().sendInfoMsg("Cross Validation");
	
	int N = data.length;
	int M = data[0].length;
	int d = N / k.getValue();
	//aufrunden
	if (d * k.getValue() != N) {
	    d += 1;
	}
	//split up data
	for (int i=0;i<k.getValue();i++) {
	    int trainCounter = 0;
	    int valCounter = 0;
	    this.getModel().getRuntime().sendInfoMsg("Run " + (i+1) + " of " + k.getValue());
	    
	    //testrun
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valCounter++;
		}
		else {		    		    
		    trainCounter++;
		}
	    }	    
	    double valData[][] = new double[valCounter][];
	    double valPredict[] = new double[valCounter]; 
	    double trainData[][] = new double[trainCounter][];	    	    
	    double trainPredict[] = new double[trainCounter]; 
	    trainCounter = 0;
	    valCounter = 0;
	    
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valData[valCounter] = data[j];
		    valPredict[valCounter] = predict[j];
		    valCounter++;
		}
		else {		    		    
		    trainData[trainCounter] = data[j];
		    trainPredict[trainCounter] = predict[j];
		    trainCounter++;
		}
	    }	 
	    
	    trainingData.setObject("data",trainData);
	    trainingData.setObject("predict",trainPredict);
	    
	    validationData.setObject("data",valData);
	    validationData.setObject("predict",valPredict);
	    
	    singleRun();
	 }
     }
}
