/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.directsearch;

import Jama.Matrix;
import java.io.Serializable;

import java.util.Random;
import java.util.Vector;
import optas.optimizer.Optimizer;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.core.SampleLimitException;
import optas.core.ObjectiveAchievedException;

/**
 *
 * @author Christian Fischer
 */
abstract public class PatternSearch implements Serializable{
    abstract public SampleSO step(Optimizer context,SampleSO[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]) throws SampleLimitException, ObjectiveAchievedException;
    static Random Generator = null;
        
    protected SampleSO step(Optimizer context,SampleSO best,SampleSO worst,Vector<Matrix> P) throws SampleLimitException, ObjectiveAchievedException{
        Matrix x = new Matrix(best.x,best.x.length);
        for (int i=0;i<P.size();i++){
            Matrix x_next = x.plus(P.get(i));
            SampleSO next = context.getSampleSO(x_next.getColumnPackedCopy());
            if (next.f() < worst.f()){
                return next;
            }
        }
        return null;
    }
    abstract public SampleSO search(Optimizer context,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb);
}
