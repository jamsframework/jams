/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import reg.hydro.calculations.SlopeCalculations;
import reg.hydro.data.SimpleEnsemble;
import reg.hydro.data.TimeSerie;

/**
 *
 * @author chris
 */
public abstract class OptimizationScheme {

    double[][] weights;
    SimpleEnsemble[] parameter;
    SimpleEnsemble objective;
    TimeSerie ts;

    double threshold = 0.8;

    protected int n;
    protected int T;

    ArrayList<ParameterGroup> solutionGroups = new ArrayList<ParameterGroup>();
    ArrayList<int[]> dominatedTimeStepsForGroup = new ArrayList<int[]>();
    
    public OptimizationScheme(double weights[][], SimpleEnsemble parameterIDs[], SimpleEnsemble objective, TimeSerie ts) {
        this.weights = weights;
        this.parameter = parameterIDs;
        this.ts = ts;

        n = weights.length;
        T = weights[0].length;
    }

    static private class ArrayColumnComparator implements Comparator {

        private int col = 0;
        private int order = 1;

        public ArrayColumnComparator(int col, boolean decreasing_order) {
            this.col = col;
            if (decreasing_order) {
                order = -1;
            } else {
                order = 1;
            }
        }

        public int compare(Object d1, Object d2) {

            double[] b1 = (double[]) d1;
            double[] b2 = (double[]) d2;

            if (b1[col] < b2[col]) {
                return -1 * order;
            } else if (b1[col] == b2[col]) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }

    static public ArrayList<int[]> calcDominantParameters(double weights[][], double threshold){
        int n = weights.length;
        int T = weights[0].length;
        
        double weightList[][] = new double[n][2];
        ArrayList<int[]> result = new ArrayList<int[]>();
        int[] dominantParameters = new int[n];

        for (int i=0;i<T;i++){
            double sum = 0.0;

            for (int j=0;j<n;j++){
                weightList[j][0] = j;
                weightList[j][1] = weights[j][i];
                sum += weights[j][i];
            }
            if (sum==0)
                continue;
            
            Arrays.sort(weightList, new ArrayColumnComparator(1,true));
            
            double aggregatedWeight = 0.0;
            int c = 0;

            while (aggregatedWeight<threshold){
                aggregatedWeight += weightList[c][1] / sum;
                dominantParameters[c] = (int)weightList[c][0];
                c++;
            }
            result.add(Arrays.copyOf(dominantParameters, c));
        }
        return result;
    }

    protected int[] calcDominatedTimeSteps(ParameterGroup p, ParameterGroup all){
        boolean dominatedTime[] = new boolean[T];
        int counter = 0;
        for (int t=0;t<T;t++){
            double norm = 0;
            double w_t = 0;
            for (int i=0;i<all.size;i++){
                norm += weights[all.get(i)][t];
            }
            for (int i=0;i<p.size;i++){
                w_t += weights[p.get(i)][t];
            }
            if (norm > 0 && w_t / norm > threshold){
                dominatedTime[t] = true;
                counter++;
            }
        }
        int timeSteps[] = new int[counter];
        counter = 0;
        for (int i=0;i<T;i++){
            if (dominatedTime[i])
                timeSteps[counter++] = i;
        }
        return timeSteps;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < solutionGroups.size(); i++) {
            result += "+++++GROUP " + i + "++++++++++\n";
            result += solutionGroups.get(i).toString() + "+++++++++++++++++\n";
            result += "-------------TIME---------------\n";
            int[] tSteps = dominatedTimeStepsForGroup.get(i);
            for (int t=0;t<tSteps.length;t++)
                result += ts.getTime(tSteps[t]).getTime() + "\n";
        }
        return result;
    }
    
    public String getOptimizationIni(int groupIndex) {
        String result = "";
        ParameterGroup p = this.solutionGroups.get(groupIndex);
        double bounds[][] = SlopeCalculations.calculateBehavourialRange(parameter, objective, 0.33);

        int groupSize = p.size;

        result += "#+++++GROUP " + groupIndex + "++++++++++\n";
        result += "jobMode=optimizationRun\n";
        result += "method=6\n";
        result += "n=" + groupSize + "\n";
        String lowerBound = "lowerbounds=";
        String upperBound = "upperbounds=";
        String parameters = "parameters=";
        for (int i=0;i<groupSize;i++){
            int p_i = p.get(i);
            lowerBound += bounds[p_i][0] + ";";
            upperBound += bounds[p_i][1] + ";";
            parameters += this.parameter[p_i].name;
        }
        lowerBound = lowerBound.substring(0, lowerBound.length());
        upperBound = upperBound.substring(0, upperBound.length());
        parameters = parameters.substring(0, upperBound.length());

        String efficiencies = "J2K.e2";
        String efficiency_modes = "2";
        String outputAttr = "";
        String options = "removeUnusedComponents=1\n" +
                         "removeGUIComponents=1\n" +
                         "optimizeModelStructure=0\n" +
                         "maxn=1000\n" +
                         "prange=1E-05\n" +
                         "numberOfComplexes=2\n" +
                         "pcento=0.01\n" +
                         "kstop=10\n" +
                         "popSize=500\n" +
                         "maxGeneration=10\n" +
                         "crossoverProbability=0.5\n" +
                         "mutationProbability=0.5\n" +
                         "crossoverDistributionIndex=10\n" +
                         "mutationDistributionIndex=10\n";

        result += lowerBound + "\n";
        result += upperBound + "\n";
        result += parameters + "\n";
        result += efficiencies + "\n";
        result += efficiency_modes + "\n";
        result += outputAttr + "\n";
        result += options + "\n";

        //result += solutionGroups.get(groupIndex).toString(parameterIDs) + "+++++++++++++++++\n";
        //result += "-------------TIME---------------\n";
        //int[] tSteps = dominatedTimeStepsForGroup.get(i);
        //for (int t = 0; t < tSteps.length; t++) {
        //    result += ts.getTime(tSteps[t]).getTime() + "\n";
        //}
    
    return result;
}

    abstract public void calcOptimizationScheme();
}
