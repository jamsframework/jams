/*
 * LinearMeanModell.java
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
public class LinearMeanModell extends MeanModell {            
    double average;
    /** Creates a new instance of LinearMeanModell */
    public LinearMeanModell(int inputSize) {
	this.inputSize = inputSize;
	this.parameterCount = inputSize;
	beta = new double[inputSize];
    }
 
    public String[] getMeanModelParameterNames(){
        meanModelParameterNames = new String[inputSize];
        for (int i=0;i<inputSize;i++){
            meanModelParameterNames[i] = "linRegCoeff_" + i;
        }
        return meanModelParameterNames;
    }
    
    public Matrix Transform(double data[][],double result[]) {
	double sum = 0;
	average = 0;
	Matrix transformed = new Matrix(result.length,1);
	
	for (int i = 0;i<data.length;i++) {
	    sum = 0;
	    for (int j=0;j<inputSize;j++) {
		sum += beta[j]*data[i][j];
	    }
	    transformed.set(i,0,result[i]-sum);
	    average += result[i]-sum;
	}
	average /= (double)data.length;
	for (int i = 0;i<data.length;i++) {
	    transformed.set(i,0,transformed.get(i,0)-average);
	}
	return transformed;
    }
    
    public double[] ReTransform(double data[][],Matrix prediction) {
	double sum = 0;
	double transformed[] = new double[prediction.getRowDimension()];
	for (int i = 0;i<data.length;i++) {
	    sum = 0;
	    for (int j=0;j<inputSize;j++) {
		sum += beta[j]*data[i][j];
	    }
	    transformed[i] = prediction.get(i,0)+sum+average;
	}
	return transformed;
    }                        
}
