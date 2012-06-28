/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.Arrays;

/**
 *
 * @author chris
 */
public class SimilarityBasedOptimizationScheme extends OptimizationScheme{

    @Override
    public String toString(){
        return "Similarity";
    }

    public class Similarity{
        ParameterGroup p1;
        ParameterGroup p2;
        double similariy;

        public Similarity(ParameterGroup p1, ParameterGroup p2, double similarity){
            this.p1 = p1;
            this.p2 = p2;
            this.similariy = similarity;
        }
    }

    private double[] accumulateWeightsOverParameters(double[][]weights){
        double sum[] = new double[T];
        for (int i=0;i<n;i++){
            for (int j=0;j<T;j++){
                sum[j] += weights[i][j];
            }
        }
        return sum;
    }

    public double calcR2(double[]t1, double[]t2){
        double mean = 0;
        for (int t=0;t<T;t++){
            mean += t1[t];
        }
        mean /= (double)T;

        double numerator=0;
        double denumerator=0;
        for (int t=0;t<T;t++){
            numerator += ((t1[t]-t2[t])*(t1[t]-t2[t]));
            denumerator += ((t1[t]-mean)*(t1[t]-mean));

        }
        if (denumerator == 0)
            return 0;
        return 1.0 - numerator/denumerator;
    }

    public double calcr2(double[]t1, double[]t2){
        double mean_t1 = 0;
        double mean_t2 = 0;
        for (int t=0;t<T;t++){
            mean_t1 += t1[t];
            mean_t2 += t2[t];
        }
        mean_t1 /= (double)T;
        mean_t2 /= (double)T;


        double numerator=0;
        double denumerator1=0;
        double denumerator2=0;

        for (int t=0;t<T;t++){
            numerator += (t1[t]-mean_t1)*(t2[t]-mean_t2);
            denumerator1 += (t1[t]-mean_t1)*(t1[t]-mean_t1);
            denumerator2 += (t2[t]-mean_t2)*(t2[t]-mean_t2);

        }
        numerator = 1.0/T * numerator;
        denumerator1 = 1.0/T * denumerator1;
        denumerator2 = 1.0/T * denumerator2;

        if (denumerator1 == 0 || denumerator2 == 0)
            return 0.0;
        
        return numerator / (Math.sqrt(denumerator1)*Math.sqrt(denumerator2));
    }

    public double calcSimilarty(ParameterGroup p1, ParameterGroup p2){
        double w[] = new double[n];

        double ts1[] = new double[T];
        double ts2[] = new double[T];

        for (int t=0;t<T;t++){
            for (int i=0;i<n;i++){
                w[i] = weights[i][t];
            }
            p1.w = w;
            p2.w = w;
            ts1[t] = p1.calcNorm();
            ts2[t] = p2.calcNorm();
        }

        return calcr2(ts1,ts2);
    }



    public void calcOptimizationScheme(){
        int currentGroupCount = n;
        double w_sum[] = accumulateWeightsOverParameters(weights);
        ParameterGroup groups[] =  new ParameterGroup[n];
        for (int i=0;i<n;i++){
            groups[i] = (new ParameterGroup(this.parameter,n)).createEmptyGroup();
            groups[i].add(i);
        }

        while(true){
            double similarity = Double.NEGATIVE_INFINITY;
            int bestP1=0, bestP2=0;
            for (int i=0;i<n;i++){
                for (int j=i+1;j<n;j++){
                     double s = calcSimilarty(groups[i],groups[j]);
                     if (s > similarity){
                         bestP1 = i;
                         bestP2 = j;
                         similarity = s;
                     }
                }
            }
            if (similarity<=0.0 || currentGroupCount < 2)
                break;

            System.out.println("Similarity is:" + similarity);
            System.out.println("Group " + groups[bestP1].toString() + " together with " + groups[bestP2].toString());
            groups[bestP1].add(groups[bestP2]);
            groups[bestP2] = groups[--currentGroupCount];
        }

        for (int i=0;i<currentGroupCount;i++){
            this.solutionGroups.add(groups[i]);
            System.out.println("weights group " + i + groups[i]);
            double weight_cur[] = new double[T];
            for (int t=0;t<T;t++){
                for (int j=0;j<groups[i].getSize();j++){
                    weight_cur[t] += weights[groups[i].get(j)][t] / w_sum[t];
                }
            }
            System.out.println(Arrays.toString(weight_cur));
        }

        update();
    }
}
