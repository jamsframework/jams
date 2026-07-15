/*
 * RationalQuadratic.java
 *
 * Created on 1. Juni 2007, 16:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.datamining.kernels;

/**
 *
 * @author Christian(web)
 */
public class RationalQuadratic extends Kernel {
    
    /** Creates a new instance of RationalQuadratic */
    public RationalQuadratic(int inputDim) {              
	this.inputDim = inputDim;	
	this.parameterCount = inputDim + 2;
	this.KernelParameterCount = inputDim + 2;
    }
       
    public String[] getParameterNames(){
        super.getParameterNames();
        for (int i=0;i<inputDim;i++){
            this.KernelParameterNames[i] = "l_" + i;
        }
        this.KernelParameterNames[inputDim] = "alpha";
        this.KernelParameterNames[inputDim+1] = "sigma";
        
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
	double noise = 0.0;
	
	if (index1 == index2) {
	    noise = this.theta[KernelParameterCount-1]*this.theta[KernelParameterCount-1];
	}
	return Math.pow(1.0 + r / (2*theta[KernelParameterCount-2]),-theta[KernelParameterCount-2]) + noise;			
    }
    
    public double dkernel(double x[],double y[],int d) {
	double alpha = theta[inputDim];
	double r = SqrDistance2(x,y);	
	double base = 1.0 + r / (2.0*alpha);
	double expterm = Math.pow(base,-alpha);
		
	if (d < inputDim) {		    		    
	    double dr = (x[d]-y[d])/theta[d];		
	    return expterm*dr*dr/(base*theta[d]);
	}
	if (d == inputDim) {
	    return expterm*(-Math.log(base)+r/(2.0*alpha*base));
	}		
	//do not adjust sigma
	if (d > inputDim)
	    return 0.0;
	return 0.0;
    }       
}
