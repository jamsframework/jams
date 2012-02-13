/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import optas.hydro.calculations.SlopeCalculations.Domination;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerie;

/**
 *
 * @author chris
 */
public class GreedyOptimizationScheme extends OptimizationScheme{
   
    @Override
    public String toString(){
        return "Greedy";
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

    public void calcOptimizationScheme(){
        double weight_old[] = new double[T];
        double weight_sum[] = accumulateWeightsOverParameters(weights);

        Set<Integer> usedParameters = new HashSet<Integer>();
        ParameterGroup allParameters = (new ParameterGroup(this.parameter,n));

        while(usedParameters.size()<n){
            boolean addMore = true;
            double weight_cur[] = new double[T];

            //OptimizationGroup group = new OptimizationGroup();
            ParameterGroup group = (new ParameterGroup(this.parameter,n)).createEmptyGroup();

            while (addMore) {
                int bestParameter = -1;
                int bestTimeCover = -1;
                double bestDominationWeight = 0;
                double best_weight_cur[] = null;

                ArrayList<Integer> bestTimeList = null;

                //find best parameter
                for (int parameter = 0; parameter < n; parameter++) {
                    if (usedParameters.contains(parameter)){
                        continue;
                    }
                    ArrayList<Integer> timeList = new ArrayList<Integer>();
                    double weight_cur_tmp[] = Arrays.copyOf(weight_cur, weight_cur.length);

                    double domination_weight = 0;
                    for (int i = 0; i < T; i++) {
                        weight_cur_tmp[i] += weights[parameter][i];

                        if (weight_sum[i] != 0) {
                            domination_weight += weight_cur_tmp[i] / weight_sum[i];
                        }

                        double delta_weight = weight_sum[i] - weight_old[i];
                        if (delta_weight != 0) {
                            if (weight_cur_tmp[i] / delta_weight > 0.8) {
                                timeList.add(i);
                            }
                        }
                    }
                    if ( timeList.size() > bestTimeCover ||
                            (timeList.size() == bestTimeCover && domination_weight >= bestDominationWeight) ){
                        bestTimeCover        = timeList.size();
                        bestParameter        = parameter;
                        bestDominationWeight = domination_weight;
                        bestTimeList         = timeList;
                        best_weight_cur = Arrays.copyOf(weight_cur_tmp, weight_cur_tmp.length);
                    }
                }
                //group.addParameter(bestParameter, bestTimeList, bestDominationWeight / (double) T);
                group.add(bestParameter);
                usedParameters.add(bestParameter);
                weight_cur = best_weight_cur;

                if ((double) bestTimeList.size() / (double) T > 0.1 || usedParameters.size()>=n ) {
                    addMore = false;
                }
            }
            for (int i=0;i<T;i++){
                weight_old[i] += weight_cur[i];
            }
            this.solutionGroups.add(group);
            this.dominatedTimeStepsForGroup.add(this.calcDominatedTimeSteps(group, allParameters));
            allParameters.sub(group);
        }
    }  
}
