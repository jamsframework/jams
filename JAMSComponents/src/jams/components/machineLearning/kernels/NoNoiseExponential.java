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
public class NoNoiseExponential extends Kernel {    	                  
    public NoNoiseExponential(int inputDim) {
	this.inputDim = inputDim;	
	this.KernelParameterCount = 2*inputDim;
	this.parameterCount = 2*inputDim;
    }
       
    public double Distance(double x[],double y[]) {
	double sum = 0;
	double tmp;
	for (int i=0;i<x.length;i++) {
	    tmp = Math.abs(x[i]-y[i]);
	    sum += Math.pow(tmp*theta[2*i],theta[2*i+1]);
	}	
	return sum;
    }
    
    public String[] getKernelParameterNames(){
        for (int i=0;i<inputDim;i++){
            this.KernelParameterNames[2*i] = "l_" + i;
            this.KernelParameterNames[2*i+1] = "alpha_" + i;
        }
        
        return KernelParameterNames;
    }
    
    public double kernel(double x[],double y[],int index1,int index2) {
	double r = Distance(x,y);
	/*if (index1 == index2) {
	    return Math.exp(-0.5*r) + this.theta[KernelParameterCount-1]*this.theta[KernelParameterCount-1];
	}*/	
	return Math.exp(-0.5*r);
    }
    
    public double dkernel(double x[],double y[],int d) {
	/*double r = SqrDistance2(x,y);
	double dr = (x[d]-y[d])/theta[d];
		
	//do not adjust sigma
	if (d == inputDim)
	    return 0.0;
	
	return dr*dr*Math.exp(-0.5*r)/theta[d];*/
        return 0;
    }       
}
