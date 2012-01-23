/*
 * Kernel.java
 *
 * Created on 1. Juni 2007, 15:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning.kernels;

import java.io.Serializable;

/**
 *
 * @author Christian(web)
 */
public abstract class Kernel implements Serializable{
    int inputDim;
    int parameterCount;
    int KernelParameterCount;
    double theta[] = null;    
    double meanTheta[] = null;
    String parameterNames[] = null;
    String[] KernelParameterNames;
    public MeanModell MM = null;
            
    public void SetMeanModell(MeanModell MM) {
	this.MM = MM;
	
	parameterCount = KernelParameterCount;
    }
    
    public boolean SetParameter(double []theta) {
	if (theta.length < parameterCount) {
	    return false;
	}        
	this.theta = new double[KernelParameterCount];        
	for (int i=0;i<KernelParameterCount;i++) {
	    this.theta[i] = theta[i];
	}	
	//this.theta = theta;
	return true;
    }    
             
    abstract public double kernel(double x[],double y[],int index1,int index2);
    abstract public double dkernel(double x[],double y[],int d);
    public String[] getParameterNames(){
        KernelParameterNames = new String[this.parameterCount];
        return KernelParameterNames;
    }
    
    public int getParameterCount() {
	return parameterCount;
    }                
}
