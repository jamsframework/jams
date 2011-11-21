/*
 * LinearMeanModell.java
 *
 * Created on 4. Juli 2007, 09:18
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
public class LinearMeanModell extends MeanModell {            
    /** Creates a new instance of LinearMeanModell */
    public LinearMeanModell(int inputSize) {
	this.inputSize = inputSize;
	this.parameterCount = inputSize+1;	
    }
 
    public String[] getMeanModelParameterNames(){
        meanModelParameterNames = new String[inputSize+1];
        for (int i=0;i<inputSize;i++){
            meanModelParameterNames[i] = "linRegCoeff_" + i;
        }
        meanModelParameterNames[inputSize] = "linRegCoeff_bias";
        return meanModelParameterNames;
    }
    
    public void create(double data[][],double predict[]){
        int n = data.length;
        int m = data[0].length;
        //lin polynom sum(A*x_i - b) -> min
        
        Matrix A = new Matrix(n,m+1);
        for (int i=0;i<n;i++){
            for (int j=0;j<m;j++){
                A.set(i,j,data[i][j]);
            }
            A.set(i,m,1.0);
        }
        Matrix b = new Matrix(n,1);
        for (int i=0;i<n;i++){
            b.set(i, 0, predict[i]);
        }
        Matrix reg = A.transpose().times(A).solve(A.transpose().times(b));
        beta = reg.getColumnPackedCopy();
    }
    
    public Matrix Transform(double data[][],double result[]) {
	double sum = 0;	
	Matrix transformed = new Matrix(result.length,1);
	
	for (int i = 0;i<data.length;i++) {
	    sum = beta[beta.length-1];
	    for (int j=0;j<inputSize;j++) {
		sum += beta[j]*data[i][j];
	    }
	    transformed.set(i,0,result[i]-sum);	    
	}	
	for (int i = 0;i<data.length;i++) {
	    transformed.set(i,0,transformed.get(i,0));
	}
	return transformed;
    }
    
    public double[] ReTransform(double data[][],Matrix prediction) {
	double sum = 0;
	double transformed[] = new double[prediction.getRowDimension()];
	for (int i = 0;i<data.length;i++) {
	    sum = beta[beta.length-1];
	    for (int j=0;j<inputSize;j++) {
		sum += beta[j]*data[i][j];
	    }
	    transformed[i] = prediction.get(i,0)+sum;
	}
	return transformed;
    }                        
}
