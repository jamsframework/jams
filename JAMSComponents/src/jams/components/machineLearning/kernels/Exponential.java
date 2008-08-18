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
public class Exponential extends Kernel {    	
    
    public Exponential(int inputDim) {
	this.inputDim = inputDim;	
	this.KernelParameterCount = inputDim + 1;
	this.parameterCount = inputDim + 1;
                
    }
       
    public String[] getKernelParameterNames(){
        for (int i=0;i<inputDim;i++){
            this.KernelParameterNames[i] = "l_" + i;
        }
        this.KernelParameterNames[inputDim] = "sigma" ;
        
        return KernelParameterNames;
    }
    
    public double SqrDistance2(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = (x[i]-y[i])/theta[i];
	    sum += tmp*tmp;
	}	
	return sum;
    }
    
    public double kernel(double x[],double y[],int index1,int index2) {
	double r = SqrDistance2(x,y);
	
	if (index1 == index2) {
	    return Math.exp(-0.5*r) + this.theta[KernelParameterCount-1]*this.theta[KernelParameterCount-1];
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
