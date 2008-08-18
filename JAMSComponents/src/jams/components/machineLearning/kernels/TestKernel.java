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
public class TestKernel extends Kernel {    	                  
    public TestKernel(int inputDim) {
	this.inputDim = inputDim;	
	this.KernelParameterCount = inputDim + 1;
	this.parameterCount = inputDim + 1;
    }
       
    public String[] getKernelParameterNames(){
        for (int i=0;i<KernelParameterCount;i++)
            this.KernelParameterNames[i] = "p_" + i;
        return KernelParameterNames;
    }
    
    public double SqrDistance2(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = (x[i]-y[i]);
	    //if (i < 3) {
		tmp /= theta[i];
	    //}
	    sum += tmp*tmp;
	}	
	return sum;
    }
    
    public double kernel(double x[],double y[],int index1,int index2) {
	double r = SqrDistance2(x,y);
	
	if (index1 == index2) {
	    return Math.exp(-0.5*r) + theta[KernelParameterCount-1]*theta[KernelParameterCount-1];
	}
	return Math.exp(-0.5*r);
    }
    
    public double dkernel(double x[],double y[],int d) {
	double r = SqrDistance2(x,y);
	double dr = (x[d]-y[d])/theta[d];
		
	//do not adjust sigma
	if (d == inputDim)
	    return 0.0;
	
	return dr*dr*Math.exp(-0.5*r)/theta[d];
    }       
}
