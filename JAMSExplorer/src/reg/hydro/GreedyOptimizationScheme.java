/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import reg.hydro.calculations.SlopeCalculations.Domination;
import reg.hydro.data.SimpleEnsemble;
import reg.hydro.data.TimeSerie;

/**
 *
 * @author chris
 */
public class GreedyOptimizationScheme extends OptimizationScheme{

    public GreedyOptimizationScheme(double weights[][], SimpleEnsemble parameterIDs[], SimpleEnsemble objective, TimeSerie ts) {
        super(weights, parameterIDs, objective, ts);
    }

    /*
    static public class OptimizationGroup{
        ArrayList<Integer>parameter = new ArrayList<Integer>();
        ArrayList<Integer> timeIDs;

        double dominationWeight;

        public void addParameter(int parameter, ArrayList<Integer> timeIDs, double dominationWeight){
            this.timeIDs = timeIDs;
            this.dominationWeight = dominationWeight;
            this.parameter.add(parameter);
        }
        public String toString(){
            String result = "";
            result += "Parameter: ";
            for (int i=0;i<parameter.size();i++)
                result += parameter.get(i) + "  ";

            result += "\nDomination:" + dominationWeight + "\n";
            return result;
        }

        public String toString(String parameterIDs[]){
            String result = "";
            result += "Parameter: ";
            for (int i=0;i<parameter.size();i++)
                result += parameterIDs[parameter.get(i)] + "  ";

            result += "\nDomination:" + dominationWeight + "\n";
            return result;
        }

        public String getTimeSteps(TimeSerie ts){
            String result = "";
            for (int i=0;i<this.timeIDs.size();i++){
                result+=ts.getTime(timeIDs.get(i)).getTime()+"\n";
            }
            return result;
        }
    }

    static public class OptimizationSceme{
        ArrayList<OptimizationGroup> groups = new ArrayList<OptimizationGroup>();
        double [][] weights;

        public Domination getDomination(){
            return new Domination(weights);
        }

        public void addGroup(OptimizationGroup group){
            groups.add(group);
        }
        public String toString(String parameterIDs[], TimeSerie ts){
            String result = "";
            for (int i=0;i<groups.size();i++){
                result += "+++++GROUP "+i+"++++++++++\n";
                result += groups.get(i).toString(parameterIDs) + "+++++++++++++++++\n";
                result += "-------------TIME---------------\n";
                result += groups.get(i).getTimeSteps(ts);
            }
            return result;
        }
        @Override
        public String toString(){
            String result = "";
            for (int i=0;i<groups.size();i++){
                result += "+++++GROUP "+i+"++++++++++\n";
                result += groups.get(i) + "+++++++++++++++++\n";
            }
            return result;
        }
    }*/

   
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
