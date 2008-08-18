/*
 * FixedMeanModell.java
 *
 * Created on 4. Juli 2007, 09:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning.kernels;
import Jama.*;
import Jama.Matrix;

/**
 *
 * @author Christian(web)
 */
public class FixedMeanModell extends MeanModell {            
    double average;
    /** Creates a new instance of LinearMeanModell */
    public FixedMeanModell(int inputSize) {
	this.inputSize = inputSize;
	this.parameterCount = 0;
	beta = null;
    }
 
    public String[] getMeanModelParameterNames(){
        return null;
    }
    public Matrix Transform(double data[][],double result[]) {
	double sum = 0;
	average = 0;
	Matrix transformed = new Matrix(result.length,1);
	
	for (int i = 0;i<data.length;i++) {	    
	    average += result[i];
	}
	average /= (double)data.length;
	
	for (int i = 0;i<data.length;i++) {
	    transformed.set(i,0,result[i]-average);
	}
	return transformed;
    }
    
    public double[] ReTransform(double data[][],Matrix prediction) {
	double sum = 0;
	double transformed[] = new double[prediction.getRowDimension()];
	for (int i = 0;i<data.length;i++) {	    	    
	    transformed[i] = prediction.get(i,0)+average;
	}
	return transformed;
    }                        
}
