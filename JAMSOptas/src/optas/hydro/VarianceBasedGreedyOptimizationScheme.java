/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import optas.SA.SobolsMethodTemporal;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.tools.ObservableProgress;

/**
 *
 * @author chris
 */
public class VarianceBasedGreedyOptimizationScheme extends OptimizationScheme{

    SobolsMethodTemporal VBTSA;

    double tau = 0.7;
    double minDominatedTimesteps = 0.2;

    @Override
    public String toString(){
        return "Greedy";
    }

    public VarianceBasedGreedyOptimizationScheme(){

    }

    public void setData(SobolsMethodTemporal VBTSA, SimpleEnsemble p[], EfficiencyEnsemble eff, TimeSerie ts){
        this.n = p.length;
        this.T = ts.getTimesteps();
        this.VBTSA = VBTSA;
        this.parameter = p;
        this.ts = ts;
    }

    public void update(){
        dominatedTimeStepsForGroup.clear();

        double varianceExplainedCurrent[] = new double[T];

        for (int i=0;i<this.solutionGroups.size();i++){
            ParameterGroup p = solutionGroups.get(i);
            ArrayList<Integer> timeStepsTmp = new ArrayList<Integer>();
            Set<Integer> groupSet = new TreeSet<Integer>();
            for (int j=0;j<p.getSize();j++)
                groupSet.add(p.get(j));


            double varianceExplainedUpdate[][] = VBTSA.calcSensitivity(groupSet);

            for (int t = 0; t < T; t++) {
                double vt = varianceExplainedUpdate[t][1];

                double critValue = tau + (1.0 - tau) * varianceExplainedCurrent[t];
                if (vt > critValue) {
                    timeStepsTmp.add(t);
                }
            }

            int timeSteps[] = new int[timeStepsTmp.size()];
            for (int k=0;k<timeStepsTmp.size();k++){
                timeSteps[k] = timeStepsTmp.get(k);
            }
            this.dominatedTimeStepsForGroup.add(timeSteps);

            for (int t=0;t<T;t++)
                varianceExplainedCurrent[t] = varianceExplainedUpdate[t][1];
        }

    }

    //ok always take the best set
    public void calcOptimizationScheme(){        
        Set<Integer> parametersInUse = new TreeSet<Integer>();
        double varianceExplainedCurrent[] = new double[T];
        double maxTotalVarianceExplained = 0;
        double maxVarianceExplained[] = new double[T];

        while(parametersInUse.size()<n){
            boolean addMore = true;
            int dominationOfGroup[] = new int[T];

            ParameterGroup group = (new ParameterGroup(this.parameter,n)).createEmptyGroup();

            while (addMore) {
                int bestParameter = -1;
                int bestTimeCover = -1;
                maxTotalVarianceExplained = 0;
                maxVarianceExplained = new double[T];

                ArrayList<Integer> bestTimeList = null;

                //find best parameter
                for (int parameter = 0; parameter < n; parameter++) {
                    if (parametersInUse.contains(parameter)){
                        continue;
                    }
                    ArrayList<Integer> timeList = new ArrayList<Integer>();
                    Set<Integer> testSet = new TreeSet<Integer>();
                    testSet.addAll(parametersInUse);
                    testSet.add(parameter);

                    double varianceExplainedUpdate[][] = VBTSA.calcSensitivity(testSet);

                    double totalVarianceExplained = 0;
                    for (int t = 0; t < T; t++) {
                        double vt = varianceExplainedUpdate[t][1];
                        totalVarianceExplained += vt;

                        double critValue = tau + (1.0-tau)*varianceExplainedCurrent[t];
                        if (vt > critValue) {
                            timeList.add(t);
                        }
                    }
                    if ( timeList.size() > bestTimeCover ||
                            (timeList.size() == bestTimeCover && totalVarianceExplained >= maxTotalVarianceExplained) ){
                        bestTimeCover        = timeList.size();
                        bestParameter        = parameter;
                        maxTotalVarianceExplained = totalVarianceExplained;
                        bestTimeList         = timeList;
                        for (int t=0;t<T;t++)
                            maxVarianceExplained[t] = varianceExplainedUpdate[t][1];
                    }
                }                
                group.add(bestParameter);
                parametersInUse.add(bestParameter);                

                if ( ((double) bestTimeList.size() / (double) T) > minDominatedTimesteps || parametersInUse.size()>=n ) {
                    addMore = false;
                    for (int i=0;i<bestTimeList.size();i++)
                        dominationOfGroup[i] = bestTimeList.get(i);
                }
            }
            varianceExplainedCurrent = maxVarianceExplained;

            this.solutionGroups.add(group);
            this.dominatedTimeStepsForGroup.add(dominationOfGroup);
        }
    }  
}
