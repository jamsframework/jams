/*
 * MeanModell.java
 *
 * Created on 4. Juli 2007, 16:28
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
public abstract class MeanModell{
    int inputSize;
    int parameterCount;
    String meanModelParameterNames[];
    double beta[] = null;    
    
    MeanModell() {
	
    }
    
    public int GetParameterCount() {
	return parameterCount;
    }
    
    public void SetParameters(double param[]) {
	for (int i=0;i<beta.length;i++) {
	    beta[i] = param[i];
	}
    }
    
    public double[] GetParameters() {
	return beta;
    }
    
    abstract public Matrix Transform(double data[][],double result[]);
    abstract public double[] ReTransform(double data[][],Matrix prediction);
    abstract public String[] getMeanModelParameterNames();
    
}
