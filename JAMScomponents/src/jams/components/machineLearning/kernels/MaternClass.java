/*
 * MaternClass.java
 *
 * Created on 1. Juni 2007, 16:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning.kernels;

/**
 *
 * @author Christian(web)
 */
public class MaternClass extends Kernel {  
                      
    public MaternClass(int inputDim) {
	this.inputDim = inputDim;	
	this.parameterCount = inputDim + 1;
	this.KernelParameterCount = inputDim + 1;
    }
       
    public String[] getParameterNames(){
        super.getParameterNames();
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
	double r = Math.sqrt(SqrDistance2(x,y));
	double noise = 0.0;
	
	if (index1 == index2) {
	    noise = this.theta[KernelParameterCount-1]*this.theta[KernelParameterCount-1];
	}
	return (1.0 + 1.732*r/theta[0])*Math.exp(-1.732*r/theta[0]);
    }
    
    public double dkernel(double x[],double y[],int d) {
	if (d >= inputDim)
	    return 0.0;
	
	double r = SqrDistance2(x,y);
	double dr = (x[d]-y[d])/theta[d];
			
	return dr*dr*Math.exp(-0.5*r)/theta[d];
    }       
}
