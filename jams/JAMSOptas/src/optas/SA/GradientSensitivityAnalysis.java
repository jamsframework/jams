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
public class GradientSensitivityAnalysis extends SensitivityAnalyzer{
    
    @Override
    public void calculate() {
        super.calculate();
        
        int K = 2 * n;

        Matrix X = new Matrix(K, n + 1);
        Matrix Y = new Matrix(K, 1);
        double x0[] = new double[n];
        double x1[] = new double[n];

        //linear regression according to gradient estimation schemes of Brekelmans et al. 2005
        final double deltah = 0.01;
        Random rnd  = new Random();
        double error_avg = 0;
       
        for (int i = 0; i < sampleSize; i++) {            
            double value_0;

            for (int k = 0; k < n; k++) {
                x0[k] = rnd.nextDouble();//this.x[k].getValue(id_i);
            }
            value_0 = this.evaluateModel(x0);

            //do sampling
            for (int j=0;j<K;j++){                
                X.set(j, n, 1.0);

                for (int k = 0; k < n; k++) {
                    if (rnd.nextBoolean()){
                        x1[k] = x0[k]+deltah;
                        X.set(j, k, deltah);
                    }
                    else{
                        x1[k] = x0[k]-deltah;
                        X.set(j, k, -deltah);
                    }
                }
                
                double value = this.evaluateModel(x0);

                Y.set(j, 0, value);
            }

            LUDecomposition solver = new LUDecomposition(X.transpose().times(X));
            Matrix beta = solver.solve(X.transpose().times(Y));
            error_avg += beta.get(n, 0) - value_0;
                
            for (int j=0;j<n;j++){
                sensitivityIndex[j] = Math.max(sensitivityIndex[j], Math.abs(beta.get(j, 0)));
                //sensitivityIndex[j] +=  Math.abs(beta.get(j, 0));
            }
        }
        error_avg /= sampleSize;
        for (int j=0;j<n;j++){
                //sensitivityIndex[j] = Math.max(Math.abs(sensitivityIndex[j]), beta.get(j, 0));
                //sensitivityIndex[j] /=  L;
            }
        System.out.println("Average error for gradient regression is:" + error_avg);
    }
}
