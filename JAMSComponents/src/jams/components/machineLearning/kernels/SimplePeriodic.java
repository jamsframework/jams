package jams.components.machineLearning.kernels;

/**
 *
 * @author Christian(web)
 */
public class SimplePeriodic extends Kernel {
    
    /** Creates a new instance of RationalQuadratic */
    public SimplePeriodic(int inputDim) {              
	this.inputDim = inputDim;	
	this.parameterCount = 3;
	this.KernelParameterCount = 3;
    }
       
    public String[] getParameterNames(){
        super.getParameterNames();
        this.KernelParameterNames[0] = "l";
        this.KernelParameterNames[1] = "period";
        this.KernelParameterNames[2] = "sigma";
        
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
	double r = SqrDistance2(x,y) / 2.0*theta[0];
	double sin = Math.sin(Math.PI*Math.sqrt(SqrDistance2(x,y)));
	
	double noise = 0;
	if (index1 == index2) {
	    noise = theta[2]*theta[2];
	}
	
	return Math.exp(-r - (2.0*sin*sin/theta[1])) + noise; 
    }
    
    public double dkernel(double x[],double y[],int d) {	
	return 0.0;
    }       
}
