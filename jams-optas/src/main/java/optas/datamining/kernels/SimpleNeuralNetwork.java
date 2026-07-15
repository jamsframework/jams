/*
 * NeuralNetwork.java
 *
 * Created on 1. Juni 2007, 16:45
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
public class SimpleNeuralNetwork extends Kernel {
        
    Matrix nnKernel = null;
    
    public SimpleNeuralNetwork(int inputDim) {
	this.inputDim = inputDim;	
	this.parameterCount = 2;
	this.KernelParameterCount = 2;
    }
    public String[] getParameterNames(){
        super.getParameterNames();
        this.KernelParameterNames[0] = "bias";
        this.KernelParameterNames[1] = "sigma";
        
        return KernelParameterNames;
    }
    
    public boolean SetParameter(double []theta) {
	if (theta.length < parameterCount) {
	    return false;
	}
	this.theta = theta;
	
	nnKernel = new Matrix(inputDim+1,inputDim+1,0);
	
	//light version only main diag
	for (int i=0;i<parameterCount;i++) {	    
	    nnKernel.set(i,i,1.0);	   
	}
	nnKernel.set(inputDim,inputDim,theta[0]);
	
	return true;
    }          
	            
    public double kernel(double x[],double y[],int index1,int index2) {
	Matrix mx = new Matrix(1,x.length+1);		
	Matrix my = new Matrix(y.length+1,1);
	for (int i=0;i<x.length;i++) {
	    mx.set(0,i+1,x[i]);
	    my.set(i+1,0,y[i]);
	}
	mx.set(0,0,1);
	my.set(0,0,1);
		
	double value1 = mx.times(nnKernel).times(my).get(0,0);
	double value2 = mx.times(nnKernel).times(mx.transpose()).get(0,0);
	double value3 = my.transpose().times(nnKernel).times(my).get(0,0);

	double noise = 0;
	if (index1 == index2) {
	    noise = theta[1]*theta[1];
	}
	return (2.0/Math.PI)*Math.asin(2.0*value1 / Math.sqrt((1+2.0*value2)*(1+2.0*value3))) + noise;
    }
    
    public double dkernel(double x[],double y[],int d) {
	return 0.0;
    }       
}
