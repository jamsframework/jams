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
    
    public boolean isTrained(){
        return beta != null;
    }
    
    public void create(double data[][],double predict[]){
        return;
    }
    
    public int GetParameterCount() {
	return parameterCount;
    }
    
    public boolean SetParameters(double param[]) {
        beta = new double[parameterCount];
        try{
            for (int i=0;i<beta.length;i++) {
                beta[i] = param[i];
            }
        }catch(Exception e){
            beta = null;
            return false;
        }
        return true;
    }
    
    public double[] GetParameters() {
	return beta;
    }
    
    abstract public Matrix Transform(double data[][],double result[]);
    abstract public double[] ReTransform(double data[][],Matrix prediction);
    abstract public String[] getMeanModelParameterNames();
    
}
