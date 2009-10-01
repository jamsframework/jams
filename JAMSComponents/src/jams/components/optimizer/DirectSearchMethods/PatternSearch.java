/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer.DirectSearchMethods;

import Jama.Matrix;
import jams.components.optimizer.Optimizer;
import jams.components.optimizer.Optimizer.AbstractFunction;
import jams.components.optimizer.Optimizer.Sample;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author Christian Fischer
 */
abstract public class PatternSearch {
    abstract public Sample step(Optimizer context,Sample[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]);
    static Random Generator = null;
        
    protected Sample step(Optimizer context,Sample best,Sample worst,Vector<Matrix> P){
        Matrix x = new Matrix(best.x,best.x.length);
        for (int i=0;i<P.size();i++){
            Matrix x_next = x.plus(P.get(i));
            Sample next = context.getSample(x_next.getColumnPackedCopy());
            if (next.fx < worst.fx){
                return next;
            }
        }
        return null;
    }
    abstract public Sample search(Optimizer context,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb);
}
