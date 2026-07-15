/*
 * Exponential.java
 *
 * Created on 1. Juni 2007, 15:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning.kernels;

/**
 *
 * @author Christian(web)
 */
public class SimpleExponential extends Kernel {    	                  
    public SimpleExponential(int inputDim) {	
	this.inputDim = inputDim;	
	this.KernelParameterCount = 1;
	this.parameterCount = 1;
    }
       
    public String[] getParameterNames(){
        super.getParameterNames();
        this.KernelParameterNames[0] = "l";
        
        return KernelParameterNames;
    }
    
    public double SqrDistance2(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = (x[i]-y[i]);
	    sum += tmp*tmp;
	}	
	return sum;
    }
    
    public double kernel(double x[],double y[],int index1,int index2) {
	double r = SqrDistance2(x,y);
	
	if (index1 == index2) {
	    return Math.exp(-0.5*r) + this.theta[0]*this.theta[0];
	}
	return Math.exp(-0.5*r);
    }
    
    public double dkernel(double x[],double y[],int d) {
	return 0.0;
    }       
}
