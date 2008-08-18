/*
 * LinearRegression.java
 *
 * Created on 16. April 2007, 17:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import java.io.*;
import org.unijena.jams.JAMS;
import java.util.Random;
import Jama.*;
import Jama.Matrix;
import Jama.LUDecomposition;
import Jama.util.Maths;

public class LinearRegression extends Learner{
    public double InterpolationSize;
    public int KernelMethod = 1;
        
    int ApproxSize = 0;
    Matrix WeightTable = null;
    int InterpolationPoints[];
    
    public double Distance2(double x[],double y[]) {
	if (x.length != y.length) {
	    System.out.println("Distance -> x length != y length");
	    return Double.NaN;
	}
	double sum = 0;
	for (int i=0;i<x.length;i++) {
	    sum += (x[i]-y[i])*(x[i]-y[i]);
	}	
	return Math.sqrt(sum);
    }

     public double KernelValue(double r) {
	if (r == 0)
	    return 0;	
	//return r*r*Math.log(r);
	return r*r-r;		
    }
    
    public void setTrainingData(double data[][],double result[]) {
	super.setTrainingData(data,result);
	ApproxSize = (int)(this.TrainLength * InterpolationSize);
    }
            
    public void Train() {	
	Matrix W = new Matrix(TrainLength /*+ DataLength + DataLength*/,
			      ApproxSize  /*+ DataLength + DataLength*/);
	Matrix F = new Matrix(TrainLength/* + DataLength + DataLength*/,1);
		
	InterpolationPoints = new int[ApproxSize];
	for (int i=0;i<ApproxSize;i++) {
	    boolean allreadyIn = true;
	    int rndPoint = 0;
	    while (allreadyIn) {
		rndPoint = generator.nextInt(TrainLength);
		allreadyIn = false;
		for (int j=0;j<i;j++) {
		    if (InterpolationPoints[j] == rndPoint) {
			allreadyIn = true;
			break;
		    }
		}		    
	    }
	    InterpolationPoints[i] = rndPoint;
	}
	
	for (int i=0;i<TrainLength;i++) {
	    for (int j=0;j<ApproxSize;j++) {		
		W.set(i,j, KernelValue(
			Distance2(data[i],data[InterpolationPoints[j]])));		
		}
	    
/*	    for (int j=0;j<DataLength;j++) {
		W.set(i,ApproxSize+j,data[i][j]);		    
	    }

	    for (int j=0;j<DataLength;j++) {
		W.set(i,ApproxSize+DataLength+j,data[i][j]*data[i][j]);		    		
	    }*/
	    
	    F.set(i,0,result[i]);	    
	}
	
/*	for (int i=TrainLength;i<TrainLength+DataLength;i++) {
	    for (int j=0;j<this.ApproxSize;j++) {
		W.set(i,j,data[InterpolationPoints[j]][i-TrainLength]);
	    }	    
	    F.set(i,0,0);
	}
		
	for (int i=0;i<DataLength;i++) {	 
	    for (int k=0;k<ApproxSize;k++) {
	        W.set(i+TrainLength+DataLength,k,data[InterpolationPoints[k]][i]*data[InterpolationPoints[k]][i]);		    
	    }		
	    F.set(i+TrainLength+DataLength,0,0);
	}*/
								
	//kleinste quadrate methode
	Matrix F2 = W;
	F2 = F2.transpose();
	F2 = F2.times(F);
	Matrix W2 = W;
	W2 = W2.transpose();
	W2 = W2.times(W);
		
	WeightTable = W2.solve(F2);
	
	System.out.println("matrix 2 norm:" + W.times(WeightTable).minus(F).norm1());
	//System.out.println("matrix 2 norm:" + W2.cond());
    }

     public double Predict(double x[]) {
	double sum = 0;
	
	for (int i=0;i<this.ApproxSize;i++) {	   
	    sum += WeightTable.get(i,0) * KernelValue(Distance2(x,data[InterpolationPoints[i]]));	    
	}
/*	for (int k=0;k<this.DataLength;k++) {
	    sum += this.WeightTable.get(ApproxSize+k,0) * x[k];
	}*/

/*	for (int i=0;i<this.DataLength;i++) {
	    sum += this.WeightTable.get(ApproxSize+DataLength+i,0) * x[i] * x[i];
	}*/
	return sum;
    }

    
    public LinearRegression() {
	InterpolationSize = 0.1;
	generator.setSeed(1);
    }
    
}
