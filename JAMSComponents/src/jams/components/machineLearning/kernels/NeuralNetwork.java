/*
 * NeuralNetwork.java
 *
 * Created on 1. Juni 2007, 16:45
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
public class NeuralNetwork extends Kernel {
        
    Matrix nnKernel = null;
    double diag[] = null;
    
    public NeuralNetwork(int inputDim) {
	this.inputDim = inputDim;	
	this.parameterCount = inputDim + 2;	
	this.KernelParameterCount = inputDim + 2;	
    }

    public String[] getParameterNames(){
        super.getParameterNames();
        for (int i=0;i<inputDim;i++){
            this.KernelParameterNames[i] = "l_" + i;
        }
        this.KernelParameterNames[inputDim] = "bias";
        this.KernelParameterNames[inputDim+1] = "sigma";
        
        return KernelParameterNames;
    }
    
    public boolean SetParameter(double []theta) {
	if (theta.length < parameterCount) {
	    return false;
	}
	this.theta = theta;
	
	nnKernel = new Matrix(KernelParameterCount-1,KernelParameterCount-1,0);
	diag = new double[KernelParameterCount-1];
	//light version only main diag
	for (int i=0;i<KernelParameterCount-1;i++) {	    
	    nnKernel.set(i,i,theta[i]);	   
	    diag[i] = theta[i];
	}
	return true;
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
	/*Matrix mx = new Matrix(1,x.length+1);		
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
	*/
	double value1 = diag[diag.length-1];;
	double value2 = diag[diag.length-1];;
	double value3 = diag[diag.length-1];;
	
	for (int i=0;i<x.length;i++) {
	    value1 += x[i]*diag[i]*y[i];
	    value2 += x[i]*diag[i]*x[i];
	    value3 += y[i]*diag[i]*y[i];
	}
	
	double noise = 0;
	if (index1 == index2) {
	    noise = theta[KernelParameterCount-1]*theta[KernelParameterCount-1];
	}
	return (2.0/Math.PI)*Math.asin(2.0*value1 / Math.sqrt((1+2.0*value2)*(1+2.0*value3))) + noise;		
    }
    
    public double dkernel(double x[],double y[],int d) {
	Matrix mx = new Matrix(1,x.length+1);		
	Matrix my = new Matrix(y.length+1,1);
	for (int k=0;k<x.length;k++) {
	    mx.set(0,k+1,x[k]);
	    my.set(k+1,0,y[k]);
	}
	mx.set(0,0,1);
	my.set(0,0,1);
			
	//knn(x,y) = 2/Pi * sin^(-1)(2x^T*sigma*y / sqrt((1+2x^T*sigma*x)*(1+2y^T*sigma*y))
	//                                u                       v
	//ableitung = 2/Pi * 1/sqrt(1-arg^2)*(du/v - u*dv / v^2) 
	//            --------äußere-------
	double xSy = mx.times(nnKernel).times(my).get(0,0);
	double xSx = mx.times(nnKernel).times(mx.transpose()).get(0,0);
	double ySy = my.transpose().times(nnKernel).times(my).get(0,0);
	
	double v = Math.sqrt((1+2.0*xSx)*(1+2.0*ySy));
		
	//äussere ableitung
	double outer_derivative = (2.0/Math.PI)/Math.sqrt(1.0-((4.0*xSy*xSy)/(v*v)));
		
	int t1 = d;// / (x.length + 1); // zeilenindex
	int t2 = d;// % (x.length + 1); // spaltenindex --> ableitung nach theta_(t1,t2)
		
	//jetze innere ableitung
	double  u = 2.0*xSy;
	double du = 2.0*mx.get(0,t1)*my.get(t2,0);
	// v = sqrt(a(x)*b(x))) ==> dv = da*b + a*db / 2*sqrt(a,b)
	double dv = (2.0*mx.get(0,t1)*mx.get(0,t2)*(1.0+2.0*ySy) + 2.0*my.get(t1,0)*my.get(t2,0)*(1.0+2.0*xSx)) / (2.0*v);

	return outer_derivative*( (du/v) - (u*dv / (v*v) ));
    }       
}
