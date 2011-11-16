/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import Jama.LUDecomposition;
import Jama.Matrix;
import java.util.Random;

/**
 *
 * @author chris
 */
public class LinearRegression extends SensitivityAnalyzer{            
    @Override
    public void init(){
        super.init();                
        calcSensitivity();
    }

    private void calcSensitivity() {        
        Matrix M = new Matrix(L,n+1);
        Matrix Y = new Matrix(L,1);
        
        for (int i=0;i<L;i++){
            int id_i = x[0].getId(i);

            for (int j=0;j<n;j++){
                M.set(i, j, x[j].getValue(id_i));
            }
            M.set(i,n,1.0);
            Y.set(i, 0, this.y.getValue(id_i));
        }

        Matrix X = M.transpose().times(M).solve(M.transpose().times(Y));

        double sum = 0;
        for (int k=0;k<n;k++){
            sum += Math.abs(X.get(k, 0));
            sensitivityIndex[k] = Math.abs(X.get(k, 0));
        }
        for (int k=0;k<n;k++){
            sensitivityIndex[k] /= sum;
        }
    }   
}
