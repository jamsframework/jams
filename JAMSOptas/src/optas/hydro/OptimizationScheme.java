/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.TimeZone;
import optas.hydro.calculations.SlopeCalculations;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerie;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.OptimizationDescriptionDocument;
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
    ArrayList<int[]> dominatedTimeStepsForGroup = new ArrayList<int[]>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ArrayList<ParameterGroup> getSolutionGroups(){
        return solutionGroups;
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

    protected int[] calcDominatedTimeSteps(ParameterGroup p, ParameterGroup all) {
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
    }

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

    public OptimizationDescriptionDocument getOptimizationDocument() {
        OptimizationDescriptionDocument doc = new OptimizationDescriptionDocument();

        for (int i = 0; i < this.solutionGroups.size(); i++) {
            doc.addOptimization(getGroupOptimization(i));
        }
        return doc;
    }

    private Optimization getGroupOptimization(int groupIndex) {
        Optimization o = new Optimization();

        optas.metamodel.Parameter[] parameterDesc = getParameters();
        ParameterGroup p = this.solutionGroups.get(groupIndex);

        for (int i = 0; i < p.size; i++) {
            int p_i = p.get(i);
            o.addParameter(parameterDesc[p_i]);
        }

        Objective c = new Objective();

        int[] tSteps = dominatedTimeStepsForGroup.get(groupIndex);
        int t = 0;
        while (t < tSteps.length) {
            int start = tSteps[t];
            int end = start;
            while (++t < tSteps.length) {
                if (tSteps[t] - tSteps[t - 1] > 1) {
                    break;
                }
                end = tSteps[t];
            }
            String startString = sdf.format(ts.getTime(start));
            String endString = sdf.format(ts.getTime(end));

            TimeInterval interval = JAMSDataFactory.createTimeInterval();

            Calendar startCal = JAMSDataFactory.createCalendar();
            Calendar endCal = JAMSDataFactory.createCalendar();

            startCal.setTime(ts.getTime(start));
            endCal.setTime(ts.getTime(end));

            interval.setStart(startCal);
            interval.setEnd(endCal);

            c.addTimeDomain(interval);
        }
        o.addObjective(c);
        o.setName("Optimize Group " + groupIndex);
        return o;
    }

    private optas.metamodel.Parameter[] getParameters() {
        int n = 0;
        for (ParameterGroup p : this.solutionGroups) {
            n += p.size;
        }
        optas.metamodel.Parameter[] parameterDesc = new optas.metamodel.Parameter[n];
        int c = 0;

        double bounds[][] = SlopeCalculations.calculateBehavourialRange(parameter, objective, 0.33);

        for (int i = 0; i < parameter.length; i++) {
            //for (int i=0;i<p.size;i++){
            parameterDesc[c] = new optas.metamodel.Parameter();
            //this name must be resolved with the model
            parameterDesc[c].setAttributeName(this.parameter[i].getName());
            parameterDesc[c].setLowerBound(bounds[i][0]);
            parameterDesc[c].setUpperBound(bounds[i][1]);
            parameterDesc[c].setStartValueValid(false);
            parameterDesc[c].setId(c);
            c++;
            //}
        }
        return parameterDesc;
    }

    abstract public void calcOptimizationScheme();
}
