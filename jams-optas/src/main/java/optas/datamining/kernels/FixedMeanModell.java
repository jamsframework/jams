/*
 * FixedMeanModell.java
 *
 * Created on 4. Juli 2007, 09:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.datamining.kernels;
import Jama.*;
import Jama.Matrix;

/**
 *
 * @author Christian(web)
 */
public class FixedMeanModell extends MeanModell {            
    /** Creates a new instance of LinearMeanModell */
    public FixedMeanModell(int inputSize) {
	this.inputSize = inputSize;
	this.parameterCount = 1;
	beta = null;
    }
    
    public void create(double data[][],double result[]){
        beta = new double[1];
        
        double average = 0;
        for (int i = 0;i<data.length;i++) {	    
	    average += result[i];
	}
	average /= (double)data.length;
        beta[0] = average;
    }
    
    public String[] getMeanModelParameterNames(){
        String stringRep[] = new String[1];
        stringRep[0] = "mean";
        return stringRep;
    }
    
    public Matrix Transform(double data[][],double result[]) {	
	Matrix transformed = new Matrix(result.length,1);
			
	for (int i = 0;i<data.length;i++) {
	    transformed.set(i,0,result[i]-beta[0]);
	}
	return transformed;
    }
    
    public double[] ReTransform(double data[][],Matrix prediction) {
	double transformed[] = new double[prediction.getRowDimension()];
	for (int i = 0;i<data.length;i++) {	    	    
	    transformed[i] = prediction.get(i,0)+beta[0];
	}
	return transformed;
    }                        
}
