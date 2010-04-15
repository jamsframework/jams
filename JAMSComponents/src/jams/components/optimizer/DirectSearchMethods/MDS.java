/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer.DirectSearchMethods;

import Jama.Matrix;
import jams.components.optimizer.LinearConstraintDirectPatternSearch;
import jams.components.optimizer.SOOptimizer;
import jams.components.optimizer.SOOptimizer.AbstractFunction;
import jams.components.optimizer.SOOptimizer.SampleSO;
import jams.components.optimizer.SOOptimizer.SampleSOComperator;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings("unchecked")
public class MDS extends PatternSearch{

    public SampleSO step(SOOptimizer context,SampleSO[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]){                                 
        if (Generator == null){
            Generator = new Random();
        }
        //sort simplex        
        java.util.Arrays.sort(Simplex,new SampleSOComperator(false));
        
        int ns = 0;
        int n = Simplex[0].x.length;
        int m = Simplex.length;
        double mu_e = 2.0,mu_c = 0.5;
        
        LinearConstraintDirectPatternSearch LCDPS = new LinearConstraintDirectPatternSearch();
        if (LinearConstraintMatrixA != null & LinearConstraintVectorb != null)
            LCDPS.SetLinearConstraints(LinearConstraintMatrixA, LinearConstraintVectorb);
        
        // Attempt a reflection point
        Matrix P = new Matrix(n,m-1);
        for (int j=1;j<m;j++){
            for (int i=0;i<n;i++) {
                P.set(i, j-1, Simplex[0].x[i] - Simplex[j].x[i]);
            }
	}
                    
        Vector<Matrix> P_i = LCDPS.UpdateDirections(Simplex[0],P , 1.0);
        
        Matrix x0 = new Matrix(Simplex[0].x,n);
        boolean successful = false;
        
        SampleSO best = Simplex[0];
        
        SampleSO reflection[] = new SampleSO[m-1];
        
        for (int i=0;i<m-1/*P_i.size()*/;i++){
            Matrix d = P.getMatrix(0, n-1,i,i);//P_i.get(i);
            Matrix x_new = x0.plus(d);
                     
            if (i<m-1){
                reflection[i] = context.getSample(x_new.getColumnPackedCopy());
            
                if (reflection[i].fx < best.fx){
                    successful = true;
                    best = reflection[i];
                }
            }
        }
        
        if (successful){                        
            Matrix P_expand = new Matrix(n,m-1);            
            for (int j=1;j<m;j++){
                for (int i=0;i<n;i++) {
                    P_expand.set(i, j-1,  - mu_e*(Simplex[j].x[i] - Simplex[0].x[i]));
                }
            }
            
            Vector<Matrix> P_i_expand = LCDPS.UpdateDirections(Simplex[0],P_expand , 1.0);
            
            boolean expansion_successful = false;
            SampleSO expansion[] = new SampleSO[m-1];
            
            for (int i=0;i<m-1/*P_i_expand.size()*/;i++){
                Matrix d = P_expand.getMatrix(0, n-1,i,i);//P_i_expand.get(i);
                Matrix x_new = x0.plus(d);
                         
                if (i<m-1){
                    expansion[i] = context.getSample(x_new.getColumnPackedCopy());
                    if (expansion[i].fx < best.fx){                   
                        best = expansion[i];
                        expansion_successful = true;
                    }   
                }                
            } 
            
            if (expansion_successful){
                for (int i=0;i<m-1;i++){
                    Simplex[i+1] = expansion[i];
                }
            }
            else{
                for (int i=0;i<m-1;i++){
                    Simplex[i+1] = reflection[i];
                }
            }
            
            return Simplex[n-1];
        }else{
            Matrix P_contract = new Matrix(n,m-1);            
            for (int j=1;j<m;j++){
                for (int i=0;i<n;i++) {
                    P_contract.set(i, j-1, mu_c*(Simplex[0].x[i] - Simplex[j].x[i]));
                }
            }
            
            Vector<Matrix> P_i_contract = LCDPS.UpdateDirections(Simplex[0],P_contract , 1.0);
            
            for (int i=0;i<m-1/*P_i_contract.size()*/;i++){
                Matrix d = P_contract.getMatrix(0, n-1,i,i);//P_i_contract.get(i);
                Matrix x_new = x0.plus(d);
                         
                SampleSO sample_new = context.getSample(x_new.getColumnPackedCopy());
                if (i+1 < Simplex.length)
                    Simplex[i+1] = sample_new;
                if (sample_new.fx < best.fx){                   
                    best = sample_new;
                }
            }
            return Simplex[n-1];
        }                
    }

    public SampleSO search(SOOptimizer context,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb){
        return null;
    }
}
