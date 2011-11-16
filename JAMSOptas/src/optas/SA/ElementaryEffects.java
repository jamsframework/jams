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
public class ElementaryEffects extends SensitivityAnalyzer{
    final double p = 30;
    final double R = 2.5*p;
    final double delta = p / (2*(p-1));

    double distributions[][] = null;

    double mean[], meanstar[], sigma[];

    Random rnd = new Random();

    @Override
    public void init(){
        super.init();
        
        distributions = new double[(int)R][n];
        mean = new double[n];
        meanstar = new double[n];
        sigma = new double[n];
        range = this.getParameterRange();
        calcEffects();
    }

    private double[] transform(double x[]){
        double[] y = new double[n];
        for (int i=0;i<n;i++){
            y[i] = range[i][0] + x[i]*(range[i][1]-range[i][0]);
        }
        return y;
    }

    private int[] generatePermutation(){
        int map[] = new int[n];
        int set[] = new int[n];

        for (int i=0;i<n;i++){
            set[i] = i;
        }
        for (int i=0;i<n;i++){
            int index = rnd.nextInt(n-i);
            map[i] = set[index];
            set[index] = set[n-i-1];
        }
        return map;
    }

    private void calcEffects() {
        for (int i=0;i<R;i++){
            double values[] = new double[n+1];
            //initial point
            double x0[] = new double[n];
            for (int j=0;j<n;j++){
                x0[j] = rnd.nextDouble();
            }
            // i am not sure why this step is necessary
            for (int j = 0;j<n;j++){
                if (rnd.nextBoolean()){
                    x0[j] += delta;
                    if (x0[j]>1.0)
                        x0[j]-=2*delta;
                }else{
                    x0[j] -= delta;
                    if (x0[j]>1.0)
                        x0[j]+=2*delta;
                }
            }

            int map[] = generatePermutation();

            values[0] = this.getInterpolation(transform(x0));

            for (int j = 0;j<n;j++){
                int index = map[j];
                if (rnd.nextBoolean()){
                    x0[index] += delta;
                }else
                    x0[index] -= delta;

                values[j+1] = this.getInterpolation(transform(x0));
                this.distributions[i][index] = values[j+1] - values[j];
                this.mean[index] += this.distributions[i][index];
                this.meanstar[index] += Math.abs(this.distributions[i][index]);
            }
        }
        double sum = 0;
        for (int j=0;j<n;j++){
            this.mean[j] /= R;
            this.meanstar[j] /= R;

            for (int i=0;i<R;i++){
                this.sigma[j] += Math.pow((Math.abs(this.distributions[i][j]) - meanstar[j]),2);
            }
            this.sigma[j] /= (R-1);
            sum += meanstar[j];
        }
        for (int j=0;j<n;j++){
            this.sensitivityIndex[j] = meanstar[j] / sum;
        }
    }
}
