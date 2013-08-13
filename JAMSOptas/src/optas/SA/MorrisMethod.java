/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Random;

/**
 *
 * @author chris
 */
public class MorrisMethod extends SensitivityAnalyzer{
    public enum Measure {
        Absolute, NonAbsolute, Variance
    };

    final double p = 30;
    final double R = 2.5*p;
    final double delta = 0.1;//p / (2*(p-1));

    double distributions[][] = null;

    double mean[], meanstar[], sigma[];

    Random rnd = new Random();

    Measure measure = Measure.Absolute;
    
    public MorrisMethod(){
        
    }
    public MorrisMethod(Measure m){
        measure = m;
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

    @Override
    public void calculate() {
        super.calculate();
                
        distributions = new double[(int)R][n];
        mean = new double[n];
        meanstar = new double[n];
        sigma = new double[n];
        
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
                    if (x0[j]<0.0)
                        x0[j]+=2*delta;
                }
            }

            int map[] = generatePermutation();

            values[0] = evaluateModel(x0);//this.getInterpolation(transform(x0));

            for (int j = 0;j<n;j++){
                int index = map[j];
                boolean up = rnd.nextBoolean();
                if (up){
                    x0[index] += delta;
                }else{
                    x0[index] -= delta;
                }
                values[j+1] = evaluateModel(x0);//this.getInterpolation(transform(x0));
                this.distributions[i][index] = values[j+1] - values[j];
                if (up){
                    this.mean[index] += this.distributions[i][index];
                }else{
                    this.mean[index] -= this.distributions[i][index];
                }
                this.meanstar[index] += Math.abs(this.distributions[i][index]);
            }
        }
        double sum1 = 0, sum2 = 0;
        for (int j=0;j<n;j++){
            this.mean[j] /= R;
            this.meanstar[j] /= R;

            for (int i=0;i<R;i++){
                this.sigma[j] += Math.pow((Math.abs(this.distributions[i][j]) - meanstar[j]),2);
            }
            this.sigma[j] /= (R);
            this.sigma[j] = Math.sqrt(this.sigma[j]);
            sum1 += meanstar[j];
            sum2 += mean[j];
        }
        for (int j=0;j<n;j++){
            if (this.measure == Measure.Absolute){
                this.sensitivityIndex[j] = meanstar[j];// / sum1;
            }else if (this.measure == Measure.NonAbsolute){
                this.sensitivityIndex[j] = mean[j];// / sum1;
            }else if (this.measure == Measure.Variance){
                this.sensitivityIndex[j] = sigma[j];
            }
        }
    }
}
