/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class GreedyOptimizationScheme extends OptimizationScheme{
    double tau = 0.7;
    double minDominatedTimesteps = 0.2;
    
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

    @Override
    public void update(){
        double weight_cur[] = new double[T];
        dominatedTimeStepsForGroup.clear();
        double weight_sum[] = accumulateWeightsOverParameters(weights);
        
        for (int i=0;i<this.solutionGroups.size();i++){
            ArrayList<Integer> timeStepsTmp = new ArrayList<Integer>();
            ParameterGroup p = this.solutionGroups.get(i);
            

            for (int t=0;t<T;t++){
                for (int j=0;j<p.getSize();j++){
                    weight_cur[t] += weights[p.get(j)][t];
                }
                if (weight_cur[t]>=tau*weight_sum[t]-1E-14){ //rundungsfehler
                    timeStepsTmp.add(t);
                }
                //weight_sum[t] -= weight_cur[t];
                weight_cur[t]  = 0;
            }
            int timeSteps[] = new int[timeStepsTmp.size()];
            for (int k=0;k<timeStepsTmp.size();k++){
                timeSteps[k] = timeStepsTmp.get(k);
            }
            this.dominatedTimeStepsForGroup.add(timeSteps);
        }

    }

    public void calcOptimizationScheme(){
        double weight_old[] = new double[T];
        double weight_sum[] = accumulateWeightsOverParameters(weights);

        Set<Integer> usedParameters = new TreeSet<Integer>();
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
                            if (weight_cur_tmp[i] / delta_weight > tau) {
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

                if ((double) bestTimeList.size() / (double) T > minDominatedTimesteps || usedParameters.size()>=n ) {
                    addMore = false;
                }
            }
            for (int i=0;i<T;i++){
                weight_old[i] += weight_cur[i];
            }
            this.solutionGroups.add(group);
            //this.dominatedTimeStepsForGroup.add(this.calcDominatedTimeSteps(group, allParameters));
            allParameters.sub(group);
        }
        update();
    }  
}
