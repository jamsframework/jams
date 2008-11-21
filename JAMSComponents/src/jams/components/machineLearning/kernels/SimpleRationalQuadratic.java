/*
 * RationalQuadratic.java
 *
 * Created on 1. Juni 2007, 16:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning.kernels;

/**
 *
 * @author Christian(web)
 */
public class SimpleRationalQuadratic extends Kernel {
    
    /** Creates a new instance of RationalQuadratic */
    public SimpleRationalQuadratic(int inputDim) {              
	this.inputDim = inputDim;	
	this.parameterCount = 2;
	this.KernelParameterCount = 2;
    }
       
    public String[] getParameterNames(){
        super.getParameterNames();
        this.KernelParameterNames[0] = "alpha";
        this.KernelParameterNames[1] = "sigma";
        
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
	double noise = 0.0;
	
	if (index1 == index2) {
	    noise = this.theta[1]*this.theta[1];
	}
	return Math.pow(1.0 + r / (2*theta[0]),-theta[0]) + noise;			
    }
    
    public double dkernel(double x[],double y[],int d) {	
	return 0.0;
    }       
}
