/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.directsearch;

import Jama.Matrix;





import java.util.Random;
import java.util.Vector;
import optas.optimizer.LinearConstraintDirectPatternSearch;
import optas.optimizer.Optimizer;
import optas.core.SampleLimitException;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.optimizer.management.SampleFactory.SampleSOComperator;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings("unchecked")
public class NelderMead extends PatternSearch{
    public SampleSO step(Optimizer context,SampleSO[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]) throws SampleLimitException, ObjectiveAchievedException{

        //sort simplex        
        java.util.Arrays.sort(Simplex,new SampleSOComperator(false));

        int n = Simplex.length; //
	int m = Simplex[0].x.length; //nopt
	
	double alpha = 1.0;
	double beta = 0.5;
	
	// Assign the best and worst points:
	double sb[] = new double[m];
	double sw[] = new double[m];
	double fb = Simplex[0].f();
	double fw = Simplex[n-1].f();
	
	for (int i=0;i<m;i++) {
	    sb[i] = Simplex[0].x[i];
	    sw[i] = Simplex[n-1].x[i];
	}
	
	// Compute the centroid of the simplex excluding the worst point:
	double ce[] = new double[m];
	for (int i=0;i<m;i++) {
	    ce[i] = 0;
	    for (int j=0;j<n-1;j++) {
		ce[i] += Simplex[j].x[i];
	    }
	    ce[i] /= (n-1);
	}

	// Attempt a reflection point
	double snew1[] = new double[m];
	for (int i=0;i<m;i++) {
	    snew1[i] = ce[i] + alpha*(ce[i]-sw[i]);
	}
	
        // now attempt a contraction point:	
        double snew2[] = new double[m];
        for (int i=0;i<m;i++) {
            snew2[i] = sw[i] + beta*(ce[i]-sw[i]);		
        }
	
	LinearConstraintDirectPatternSearch LCDPS = new 
                LinearConstraintDirectPatternSearch();
        LCDPS.SetLinearConstraints(LinearConstraintMatrixA, LinearConstraintVectorb);
        //x,P,delta
        Matrix P = new Matrix(m,2);
        
        for (int i=0;i<m;i++){
            P.set(i, 0, snew1[i]-Simplex[0].x[i]);
            P.set(i, 1, snew2[i]-Simplex[0].x[i]);
        }
        
        Vector<Matrix> P_i = LCDPS.UpdateDirections(Simplex[0], P, 1.0);
        
        SampleSO next = super.step(context,Simplex[0],Simplex[n-1],P_i);
        if (next != null)
            return next;  
        //get random point
        boolean feasible = false;

        if (Generator == null){
            Generator = new Random();
        }
        double x[] = new double[m];
        int iterCounter = 0;
        while(!feasible){   
            //shit .. can^t find feasible point
            if (iterCounter++>10000)
                return null;
            for (int i=0;i<m;i++){
                x[i] = lowBound[i] + Generator.nextDouble()*(upBound[i]-lowBound[i]);
            }
            feasible = LCDPS.FeasibleDirection(new Matrix(x,m), new Matrix(x,m), 0.0);
        }
        return context.getSampleSO(x);
    }
    
    public SampleSO search(Optimizer f,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb){
        return null;
    }
}
