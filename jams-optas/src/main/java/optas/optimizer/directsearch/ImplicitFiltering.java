/*
 * SimplexGradient.java
 *
 * Created on 7. M^rz 2008, 12:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.optimizer.directsearch;

import java.util.Vector;
import Jama.*;


import java.util.Random;
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
public class ImplicitFiltering extends PatternSearch{
        
    public SampleSO step(Optimizer context,SampleSO[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]) throws SampleLimitException, ObjectiveAchievedException{
        //1 compare worst
        //2 compare best
        //3 zuf^llige richtung
        int method = 3;        
                        
        if (Generator == null){
            Generator = new Random();
        }
        //sort simplex        
        java.util.Arrays.sort(Simplex,new SampleSOComperator(false));
        
        int ns = 0;
        int n = Simplex[0].x.length;
        
        double ScalingQueue[] = new double[10];//{1.0,1.0/2.0,1.0/4.0,1.0/8.0,1.0/16.0,1.0/32.0,1.0/64.0,1.0/128.0};
        for (int j=0;j<10;j++){
            ScalingQueue[j] = Math.pow(0.5,(double)j)*0.5;
        }                
        boolean improvement = false;
                
        Matrix V = new Matrix(n,2*n);
        V.setMatrix(0, n-1, 0,n-1,Matrix.identity(n,n));
        V.setMatrix(0, n-1, n,2*n-1,Matrix.identity(n,n).uminus());
                                
        LinearConstraintDirectPatternSearch LCDPS = new LinearConstraintDirectPatternSearch();
        if (LinearConstraintMatrixA != null & LinearConstraintVectorb != null)
            LCDPS.SetLinearConstraints(LinearConstraintMatrixA, LinearConstraintVectorb);
                        
        while(ns < ScalingQueue.length){
            if (method == 3){
                V = new Matrix(n,n);
                for (int i=0;i<n;i++){
                    Matrix D = new Matrix(n,1);
                    for (int j=0;j<n;j++){
                        D.set(j, 0, this.Generator.nextDouble()-0.5);
                    }
                    D = D.times(1.0/D.norm2());
                    V.setMatrix(0, n-1, i,i,D);            
                }
            }
            double h = ScalingQueue[ns];                         
            Vector<Matrix> P_i = LCDPS.UpdateDirections(Simplex[0], V, h);            
            for (int j=0;j<P_i.size();j++){
                double d_test[] = P_i.get(j).getColumnPackedCopy();
                double x_test[] = new double[n];
                for (int t=0;t<n;t++){
                    x_test[t] = Simplex[0].x[t] + d_test[t];
                }                
                SampleSO Sample_test = context.getSampleSO(x_test);
                if (method != 2){
                    if (Sample_test.f() < Simplex[0].f()){
                        return Sample_test;
                    }
                    else{
                        if (Sample_test.f() < Simplex[Simplex.length-1].f()){
                            return Sample_test;
                        } 
                    }
                }
            }
            if (!improvement){
                ns++;                       
            }
            else{
                if (ns > 0){
                    ns--;
                }           
            }
        }
        
        //get random point
        boolean feasible = false;

        if (Generator == null){
            Generator = new Random();
        }
        double x[] = new double[n];
        while(!feasible){            
            for (int i=0;i<n;i++){
                x[i] = lowBound[i] + Generator.nextDouble()*(upBound[i]-lowBound[i]);
            }
            feasible = LCDPS.FeasibleDirection(new Matrix(x,n), new Matrix(x,n), 0.0);
        }
        return context.getSampleSO(x);
    }

    public SampleSO search(Optimizer context,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb){
        return null;
    }
}
