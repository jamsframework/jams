/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TimeZone;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.tools.ObservableProgress;

/**
 *
 * @author chris
 */
public abstract class OptimizationScheme extends ObservableProgress{

    double[][] weights;
    SimpleEnsemble[] parameter;
    SimpleEnsemble objective;
    TimeSerie ts;
    double threshold = 0.8;
    protected int n;
    protected int T;
    ArrayList<ParameterGroup> solutionGroups = new ArrayList<ParameterGroup>();

    public ArrayList<int[]> dominatedTimeStepsForGroup = new ArrayList<int[]>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ArrayList<ParameterGroup> getSolutionGroups(){
        return solutionGroups;
    }
    public void setSolutionGroups(ArrayList<ParameterGroup> groups){
        this.solutionGroups = groups;
        update();
    }

    public void update(){
        
    }
    public void setData(double weights[][], SimpleEnsemble parameterIDs[], SimpleEnsemble objective, TimeSerie ts){
        this.weights = weights;
        this.parameter = parameterIDs;
        this.objective = objective;
        this.ts = ts;

        n = weights.length;
        T = weights[0].length;

        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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

    static public ArrayList<int[]> calcDominantParameters(double weights[][], double threshold) {
        int n = weights.length;
        int T = weights[0].length;

        double weightList[][] = new double[n][2];
        ArrayList<int[]> result = new ArrayList<int[]>();
        int[] dominantParameters = new int[n];

        for (int i = 0; i < T; i++) {
            double sum = 0.0;

            for (int j = 0; j < n; j++) {
                weightList[j][0] = j;
                weightList[j][1] = weights[j][i];
                sum += weights[j][i];
            }
            if (sum == 0) {
                continue;
            }

            Arrays.sort(weightList, new ArrayColumnComparator(1, true));

            double aggregatedWeight = 0.0;
            int c = 0;

            while (aggregatedWeight < threshold) {
                aggregatedWeight += weightList[c][1] / sum;
                dominantParameters[c] = (int) weightList[c][0];
                c++;
            }
            result.add(Arrays.copyOf(dominantParameters, c));
        }
        return result;
    }

    public int[] getDominatedTimeSteps(int groupIndex){
        return this.dominatedTimeStepsForGroup.get(groupIndex);
    }
    /*protected int[] calcDominatedTimeSteps(ParameterGroup p, ParameterGroup all) {
        boolean dominatedTime[] = new boolean[T];
        int counter = 0;
        for (int t = 0; t < T; t++) {
            double norm = 0;
            double w_t = 0;
            for (int i = 0; i < all.size; i++) {
                norm += weights[all.get(i)][t];
            }
            for (int i = 0; i < p.size; i++) {
                w_t += weights[p.get(i)][t];
            }
            if (norm > 0 && w_t / norm > threshold) {
                dominatedTime[t] = true;
                counter++;
            }
        }
        int timeSteps[] = new int[counter];
        counter = 0;
        for (int i = 0; i < T; i++) {
            if (dominatedTime[i]) {
                timeSteps[counter++] = i;
            }
        }
        return timeSteps;
    }*/

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < solutionGroups.size(); i++) {
            result += "+++++GROUP " + i + "++++++++++\n";
            result += solutionGroups.get(i).toString() + "+++++++++++++++++\n";
            result += "-------------TIME---------------\n";
            int[] tSteps = dominatedTimeStepsForGroup.get(i);
            for (int t = 0; t < tSteps.length; t++) {
                result += ts.getTime(tSteps[t]).getTime() + "\n";
            }
        }
        return result;
    }

    /*public OptimizationDescriptionDocument getOptimizationDocument() {
        OptimizationDescriptionDocument doc = new OptimizationDescriptionDocument();

        for (int i = 0; i < this.solutionGroups.size(); i++) {
            doc.addOptimization(getGroupOptimization(i));

        }
        return doc;
    }

    private Optimization getGroupOptimization(int groupIndex) {
        Optimization o = new Optimization();

        optas.metamodel.Parameter[] parameterDesc = getParameters();

        for (int j=0;j<=groupIndex;j++){
            ParameterGroup p = this.solutionGroups.get(j);
            for (int i = 0; i < p.size; i++) {
                int p_i = p.get(i);
                o.addParameter(parameterDesc[p_i]);
            }
        }
        int nTotal = o.getParameter().size();
        int nGroup = this.solutionGroups.get(groupIndex).size;

        Objective c = new Objective();
        c.setCustomName("obj. group #" + (groupIndex+1));
        c.setMethod(UniversalEfficiencyCalculator.availableEfficiencies[UniversalEfficiencyCalculator.NSE2]);
        o.setOptimizerDescription(new optas.optimizer.SCE().getDescription());
        for (OptimizerParameter op : o.getOptimizerDescription().getPropertyMap().values()){
            if (op instanceof  NumericOptimizerParameter && op.getName().equals("maxn"))
                ((NumericOptimizerParameter)op).setValue(nTotal*10+nGroup*100);
        }
        TreeSet<Integer> tSteps = new TreeSet<Integer>();
        for (int i=0;i<=groupIndex;i++){
            int steps[] = dominatedTimeStepsForGroup.get(groupIndex);
            for (int step : steps)
                tSteps.add(step);
        }
        Iterator<Integer> iter = tSteps.iterator();
        while (iter.hasNext()) {
            int start = iter.next();
            int current = start;
            int end = start;
            while (iter.hasNext()) {
                int next = iter.next();

                if (next - current > 1) {
                    break;
                }
                end = current+1;
                current = next;
            }
            
            TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();

            Calendar startCal = DefaultDataFactory.getDataFactory().createCalendar();
            Calendar endCal = DefaultDataFactory.getDataFactory().createCalendar();

            startCal.setTime(ts.getTime(start));
            endCal.setTime(ts.getTime(end));

            interval.setStart(startCal);
            interval.setEnd(endCal);

            c.addTimeDomain(interval);
        }
        o.addObjective(c);
        o.setName("Optimize Group " + groupIndex);
        if (groupIndex == this.solutionGroups.size()-1)
            o.getOptimizerDescription().getAssessNonUniqueness().setValue(true);
        return o;
    }

    private optas.metamodel.Parameter[] getParameters() {
        int n = 0;
        for (ParameterGroup p : this.solutionGroups) {
            n += p.size;
        }
        optas.metamodel.Parameter[] parameterDesc = new optas.metamodel.Parameter[n];
        int c = 0;

        //double bounds[][] = this.SlopeCalculations.calculateBehavourialRange(parameter, objective, 0.33);

        for (int i = 0; i < parameter.length; i++) {
            //for (int i=0;i<p.size;i++){
            parameterDesc[c] = new optas.metamodel.Parameter();
            //this name must be resolved with the model
            parameterDesc[c].setAttributeName(this.parameter[i].getName());
            double minRange = parameter[i].getMin();
            double maxRange = parameter[i].getMax();
            parameterDesc[c].setLowerBound(minRange);
            parameterDesc[c].setUpperBound(maxRange);
            parameterDesc[c].setStartValue(new double[0]);
            parameterDesc[c].setId(c);
            c++;
            //}
        }
        return parameterDesc;
    }

    public void calcOptimizationScheme(){
        solutionGroups.clear();
    }*/
}
