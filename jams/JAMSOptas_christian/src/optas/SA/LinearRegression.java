/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import Jama.Matrix;
import java.util.ArrayList;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class LinearRegression extends SensitivityAnalyzer{            

    @Override
    public void calculate() {   
        super.calculate();
        
        Matrix M = new Matrix(sampleSize,n+1);
        Matrix Y = new Matrix(sampleSize,1);
        ArrayList<Sample> x = getRandomSampling();
        for (int i=0;i<sampleSize;i++){
            //int id_i = x[0].getId(i);

            Sample s0 = x.get(i);
            
            for (int j=0;j<n;j++){                
                M.set(i, j, s0.x[j]);//x[j].getValue(id_i));
            }
            M.set(i,n,1.0);
            Y.set(i, 0, s0.F()[0]);//this.y.getValue(id_i));
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
