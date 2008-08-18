package jams.components.machineLearning;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;
import Jama.*;
import Jama.Matrix;
import Jama.LUDecomposition;
import Jama.util.Maths;

/**
 *
 * @author Christian(web)
 */
public class SplitValidation extends JAMSContext {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger TrainingSetSize;
                   
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
     
     public SplitValidation() {
	 
     }
     
     public void init() {  
     
     }
     
     private void singleRun() {    
	 if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
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
     
    public void run() {  
        if (enable != null){
            if (enable.getValue() == false){
                singleRun();
                return;
            }
        }
	double data[][] = null;
	double predict[] = null;
	try {
	    data =(double[][])Data.getObject("data");
	    predict = (double[])Data.getObject("predict");
	}
	catch(Exception e) {
            this.getModel().getRuntime().sendHalt("Could not find dataset" + e.toString());
	}		
        this.getModel().getRuntime().sendInfoMsg("Split Validation");
        	
        int n = data.length;
        int m = TrainingSetSize.getValue();
        
        if (m>n){
             this.getModel().getRuntime().sendHalt("training set size is larger than number of observations");
        }
        if (m<=0){
            this.getModel().getRuntime().sendHalt("training set size is less or equal to zero");
        }
        
	//split up data	    	    	    
	double valData[][] = new double[n-m][];
	double valPredict[] = new double[n-m]; 
	double trainData[][] = new double[m][];	    	    
	double trainPredict[] = new double[m]; 
	 
	for (int j=0;j<data.length;j++) {
	    if ( j >= m && j < n) {
	        valData[j - m] = data[j];
	        valPredict[j - m] = predict[j];
	    }
	
	    if ( j >= 0 && j < m) {
	        trainData[j] = data[j];
	        trainPredict[j] = predict[j];
	    }
	}	 
	    
	trainingData.setObject("data",trainData);
	trainingData.setObject("predict",trainPredict);
	    
	validationData.setObject("data",valData);
	validationData.setObject("predict",valPredict);
	    
	singleRun();
    }
}
