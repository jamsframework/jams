/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.calculations;

import Jama.LUDecomposition;
import Jama.Matrix;
import jams.JAMS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import org.jfree.data.xy.XYSeries;
import optas.data.DataCollection;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.Resources;

/**
 *
 * @author chris
 */
public class SlopeCalculations {

    static private class Coordinate {

        double x, y;

        public Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static private double calcRegression(ArrayList<Coordinate> list) {
        double avg_x = 0;
        double avg_y = 0;
        for (Coordinate c : list) {
            avg_x += c.x;
            avg_y += c.y;
        }
        avg_x /= (double) list.size();
        avg_y /= (double) list.size();

        double sum1 = 0, sum2 = 0;
        for (Coordinate c : list) {
            sum1 += (c.x - avg_x) * (c.y - avg_y);
            sum2 += (c.x - avg_x) * (c.x - avg_x);
        }
        return sum1 / sum2;
    }
    
    static private XYSeries[] calcRegression(SimpleEnsemble param[], EfficiencyEnsemble eff) {
        int N = eff.getSize();
        int n = param.length;
        int k = 2 * n;
        int iter = N / (k+1);

        XYSeries dataset[] = new XYSeries[n];
        for (int i = 0; i < n; i++) {
            dataset[i] = new XYSeries(JAMS.i18n("DATA_POINT"));
            dataset[i].setDescription(param[i].getName());
        }

        Matrix X = new Matrix(k, n + 1);
        Matrix Y = new Matrix(k, 1);
        double x0[] = new double[n];

        //linear regression according to gradient estimation schemes of Brekelmans et al. 2005
        for (int i = 0; i < iter; i++) {
            int offset = i * (k + 1);
            boolean isValidIteration = true;

            for (int j = 0; j < n; j++) {
                x0[j] = param[j].getValue(offset);
            }

            for (int row = 0; row < k; row++) {
                X.set(row, 0, 1.0);
                double value = eff.getValue(offset + row);
                if (!isValidDouble(value)){
                    isValidIteration = false;
                    break;
                }
                Y.set(row, 0, value);

                for (int col = 0; col < n; col++) {
                    //X.set(row, col + 1, param[col].getValue(offset + row) - x0[col]);
                    X.set(row, col + 1, 0.01*Math.signum(param[col].getValue(offset+row) - x0[col]));

                }
            }
            if (isValidIteration){
                LUDecomposition solver = new LUDecomposition(X.transpose().times(X));
                Matrix beta = solver.solve(X.transpose().times(Y));
                //Matrix beta = X.transpose().times(X).inverse().times(X.transpose()).times(Y);


                //double error0 = beta.get(0, 0) - eff.getValue(offset);
                //System.out.println("Gradient error in x0:" + error0);
                double value = eff.getValue(offset);
                if (isValidDouble(value)){
                    for (int j = 0; j < n; j++) {
                        dataset[j].add(eff.getValue(offset), beta.get(j+1, 0));
                    }
                }
            }
        }
        return dataset;
    }

    static private boolean isValidDouble(double value){
        return !Double.isNaN(value) && Math.abs(value)<1E12;
    }

    static public XYSeries[] calculateDerivative(EfficiencyEnsemble eff, DataCollection dc) {
        /*XYSeries dataset = new XYSeries(JAMS.i18n("DATA_POINT"));

        int n = eff.getSize();
        int k = 5;
        double med_eff = 0;
        int i = 0;
        while (i < n - k) {
        ArrayList<Coordinate> list = new ArrayList<Coordinate>();
        double maxDist = 0;
        for (int j = i; j < i + k; j++) {
        String index_j = Integer.toString(j);
        double value = eff.getValue(index_j);
        if (j - i == 2) {
        med_eff = value;
        }
        list.add(new Coordinate(param.getValue(index_j), value));
        if (j > i) {
        String index_j_1 = Integer.toString(j - 1);
        if (Math.abs(param.getValue(index_j) - param.getValue(index_j_1) - 0.01) > maxDist) {
        maxDist = Math.abs(param.getValue(index_j) - param.getValue(index_j_1) - 0.01);
        }
        }
        }
        i += k;
        if (maxDist > 0.001) {
        continue;
        }

        double b = calcRegression(list);
        dataset.add(med_eff, Math.abs(b));
        }
        return dataset;*/

        return calcRegression(getParameterEnsembles(dc), eff);       
    }

    static private double[] getInitializedArray(int n, double v){
        double array[] = new double[n];
        for (int i=0;i<n;i++){
            array[i] = v;
        }
        return array;
    }

    static public XYSeries[] calculateGradientAtTime(DataCollection param, TimeSerieEnsemble timeseries, int timeIndex) {
        SimpleEnsemble p[] = getParameterEnsembles(param);
        int n = p.length;

        return calculateDerivativesAtTime(p, timeseries, timeIndex,
                getInitializedArray(n,Double.NEGATIVE_INFINITY), getInitializedArray(n,Double.POSITIVE_INFINITY));
    }

    static public XYSeries[] calculateDerivativesAtTime(SimpleEnsemble param[], TimeSerieEnsemble timeseries, int timeIndex, double range_min[], double range_max[]) {
        /*XYSeries dataset = new XYSeries(JAMS.i18n("DATA_POINT"));

        int n = param.getSize();
        int k = 5;
        double med_eff = 0;
        int i = 0;
        while (i < n - k) {
        ArrayList<Coordinate> list = new ArrayList<Coordinate>();
        double maxDist = 0;
        for (int j = i; j < i + k; j++) {
        String index_j = Integer.toString(j);
        double value_j = param.getValue(index_j);
        if (value_j < range_min || value_j > range_max)
        continue;
        double value = timeseries.get(timeIndex,index_j);
        if (j - i == 2) {
        med_eff = value;
        }
        list.add(new Coordinate(param.getValue(index_j), value));
        if (j > i) {
        String index_j_1 = Integer.toString(j - 1);
        double value_j_1 = param.getValue(index_j_1);
        if (Math.abs(value_j - value_j_1 - 0.01) > maxDist) {
        maxDist = Math.abs(value_j - value_j_1 - 0.01);
        }
        }
        }
        i += 5;
        if (maxDist > 0.001) {
        continue;
        }
        if (list.size()>=3){
        double b = calcRegression(list);
        dataset.add(med_eff, Math.abs(b));
        }
        }
        return dataset;*/
        int N = param[0].getSize();

        int n = param.length;
        int k = 2 * n;

        int iter = N / (k+1);
        XYSeries dataset[] = new XYSeries[n];
        double source_range_min[] = new double[n];
        double source_range_max[] = new double[n];

        for (int i = 0; i < n; i++) {
            source_range_min[i] = Double.MAX_VALUE;
            source_range_max[i] = Double.MIN_VALUE;
            dataset[i] = new XYSeries(Resources.get("DATA_POINT"));
            dataset[i].setDescription(param[i].getName());
        }

        //linear regression according to gradient estimation schemes of Brekelmans et al. 2005
        Matrix X = new Matrix(k, n + 1);
        Matrix Y = new Matrix(k, 1);

        for (int i = 0; i < iter; i++) {
            int offset = i * (k + 1);

            double x0[] = new double[n];
            for (int j = 0; j < n; j++) {
                x0[j] = param[j].getValue(offset);
                source_range_min[j] = Math.min(source_range_min[j], x0[j]);
                source_range_max[j] = Math.max(source_range_max[j], x0[j]);
            }
        }
        double weight[] = new double[n];
        for (int j = 0; j < n; j++) {
            weight[j] = (range_max[j] - range_min[j]) / (source_range_max[j] - source_range_min[j]);
        }

        for (int i = 0; i < iter; i++) {
            int offset = i * (k + 1);
            
            double x0[] = new double[n];
            boolean skip = false;
            for (int j = 0; j < n; j++) {
                x0[j] = param[j].getValue(offset);
                
                if (x0[j]<range_min[j] || x0[j]>range_max[j])
                    skip = true;
            }
            //hopefully there is still something
            if (skip)
                continue;

            boolean validData = true;

            for (int row = 0; row < k; row++) {
                double value = timeseries.get(timeIndex, offset+row);
                X.set(row, 0, 1.0);
                if (Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value)>1E14){
                    validData = false;
                    break;
                }
                Y.set(row, 0, value);

                for (int col = 0; col < n; col++) {
                    X.set(row, col + 1, weight[col]*0.01*Math.signum(param[col].getValue(offset+row) - x0[col]));
                }
            }
            /*boolean once = false;
            if (!once && timeIndex==700){
                once=true;
                System.out.println("X:");
                for (int x=0;x<k;x++){
                    String row="";
                    for (int y=0;y<n+1;y++){
                        row += X.get(x, y) + "\t";
                    }
                    System.out.println(row);
                }
                System.out.println("Y:");
                for (int y=0;y<k;y++){
                    System.out.println(Y.get(y, 0));
                }
            }*/
            if (validData){
                LUDecomposition solver = new LUDecomposition(X.transpose().times(X));
                Matrix beta = solver.solve(X.transpose().times(Y));
                //Matrix beta = X.transpose().times(X).inverse().times(X.transpose()).times(Y);
                double y0 = timeseries.get(timeIndex, offset);                
                double error0 = beta.get(0, 0) - y0;
                if (Math.abs(error0/y0) > 0.2){
                    //System.out.println("Gradient error in x0:" + Math.abs(error0/y0));
                }
                if (!(Double.isNaN(y0) || Math.abs(y0)>1E12))
                    for (int j = 0; j < n; j++) {
                        dataset[j].add(y0, beta.get(j+1, 0));
                }
            }
        }
        return dataset;
    }

    public static class Slope implements Comparable {

        int index;
        double value;

        public Slope(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public int compareTo(Object obj) {
            if (obj instanceof Slope) {
                Slope p2 = (Slope) obj;
                if (value < p2.value) {
                    return 1;
                } else if (value > p2.value) {
                    return -1;
                } else if (index < p2.index) {
                    return 1;
                } else if (index > p2.index) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }
/*
    static public double[] calcMaxSlopeTimeserie(SimpleEnsemble p, TimeSerieEnsemble timeserie, EfficiencyEnsemble eff, double threshold) {
        double best = eff.getValue(eff.findArgBest());
        double range_min = Double.POSITIVE_INFINITY;
        double range_max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < p.getSize(); i++) {
            boolean isBehavourial = false;
            if (eff.isPositiveBest() && best > 0 || !eff.isPositiveBest() && best < 0) {
                if (eff.getValue(i) >= best * threshold) {
                    isBehavourial = true;
                }
            }
            if (!eff.isPositiveBest() && best > 0 || eff.isPositiveBest() && best < 0) {
                if (eff.getValue(i) <= best * threshold) {
                    isBehavourial = true;
                }
            }
            if (isBehavourial) {
                range_min = Math.min(range_min, p.getValue(i));
                range_max = Math.max(range_max, p.getValue(i));
            }
        }

        double result[] = new double[timeserie.getTimesteps()];

        for (int i = 0; i < timeserie.getTimesteps(); i++) {
            XYSeries dataset = calculateSlopes(p, timeserie, i, range_min, range_max);
            double max = 0;
            for (int j = 0; j < dataset.getItemCount(); j++) {
                max = Math.max(max, dataset.getY(j).doubleValue());
            }
            result[i] = max;
        }
        return result;
    }*/

    static public SimpleEnsemble[] getParameterEnsembles(DataCollection d) {
        Set<String> parameterSets = d.getDatasets(Parameter.class);
        SimpleEnsemble p[] = new SimpleEnsemble[parameterSets.size()];

        Iterator<String> iter = parameterSets.iterator();
        int c = 0;
        while (iter.hasNext()) {
            p[c++] = d.getSimpleEnsemble(iter.next());
        }
        return p;
    }
    
    static public double[][] calculateBehavourialRange(SimpleEnsemble parameter[], SimpleEnsemble objective, double threshold){
        int M = objective.getSize();
        int n = parameter.length;
        Integer[] order = objective.sort();

        double ranges[][] = new double[n][2];
        for (int i=0;i<n;i++){
            ranges[i][0] = Double.POSITIVE_INFINITY;
            ranges[i][1] = Double.NEGATIVE_INFINITY;
        }
        for (int m=0;m<threshold*M;m++){
            for (int i=0;i<n;i++){
                ranges[i][0] = Math.min(ranges[i][0], parameter[i].getValue(order[m]));
                ranges[i][1] = Math.max(ranges[i][1], parameter[i].getValue(order[m]));
            }
        }
        return ranges;
    }

    static public class Domination{
        double weights[][];
        double sum[];

        boolean enable[];
        int n, T;

        ArrayList<Integer> timeSteps = new ArrayList<Integer>();

        public Domination( double weights[][]){
            this.weights = weights;
            n = weights.length;
            T = weights[0].length;

            enable = new boolean[n];
            sum = new double[T];
        }

        public void enableAll(){
            for (int i=0;i<n;i++)
                enable[i] = true;
        }

        public void setParameter(int param, boolean value){
            enable[param] = value;
        }

        public void updateSum(){
            for (int i=0;i<T;i++){
                sum[i] = 0;
                for (int j=0;j<n;j++){
                    if (enable[j]){
                        sum[i] += weights[j][i];
                    }
                }
            }
        }

        public ArrayList<Integer> getDominatedTimeSteps(){
            return this.timeSteps;
        }

        public double calcTimeCover(ArrayList<Integer> list){
            int paramList[] = new int[list.size()];
            for (int i=0;i<list.size();i++){
                paramList[i] = list.get(i);
            }
            return calcTimeCover(paramList);
        }
        public double calcTimeCover(int paramList[]){
            updateSum();
            timeSteps.clear();

            int counter = 0;
            double result[] = new double[T];

            for (int i=0;i<T;i++){
                result[i] = 0;
                for (int j=0;j<paramList.length;j++){
                    result[i] += weights[paramList[j]][i];
                }
                if (sum[i]!=0)
                    if (result[i] / sum[i] > 0.80){
                        timeSteps.add(i);
                        counter++;
                    }
            }

            return (double)counter / (double)T;
        }
    }
       
    static public double[][] calcParameterSensitivityTimeserie(DataCollection d, TimeSerieEnsemble timeserie, TimeSerie obs, double threshold) {
        SimpleEnsemble p[] = getParameterEnsembles(d);
        int n = p.length;
        int T = timeserie.getTimesteps();

        double result[][] = new double[n][timeserie.getTimesteps()];

        if (timeserie == null || obs == null) {
            return null;
        }

        for (int i = 0; i < timeserie.getTimesteps(); i++) {
            double real_value = obs.getValue(i);
            double range_min[] = new double[n];
            double range_max[] = new double[n];

            for (int j = 0; j < n; j++) {
                range_min[j] = Double.POSITIVE_INFINITY;
                range_max[j] = Double.NEGATIVE_INFINITY;
            }
            double errorList[] = new double[timeserie.getSize()];

            int counter=0;
            for (int j = 0; j < timeserie.getSize(); j++) {
                if (isValidDouble(timeserie.get(i, j))){
                    errorList[j] = Math.abs(real_value - timeserie.get(i, j));
                    counter++;
                }
            }
            Arrays.sort(errorList,0,counter);

            for (int k = 0; k < n; k++) {
                for (int j = 0; j < p[k].getSize(); j++) {
                    if (!isValidDouble(timeserie.get(i, j)))
                        continue;
                    double err = Math.abs(real_value - timeserie.get(i, j));
                    if (err < errorList[(int) (threshold * counter)]) {
                        range_min[k] = Math.min(range_min[k], p[k].getValue(j));
                        range_max[k] = Math.max(range_max[k], p[k].getValue(j));
                    }
                }
            }

            XYSeries dataset[] = calculateDerivativesAtTime(p, timeserie, i, range_min, range_max);
            for (int k = 0; k < n; k++) {
                double max = 0;
                for (int j = 0; j < dataset[k].getItemCount(); j++) {
                    max = Math.max(max, Math.abs(dataset[k].getY(j).doubleValue()));
                }
                result[k][i] = max;
            }
        }

        return result;

    }
/*
    static public double[][] calcMaxSlopeTimeserie(DataCollection d, TimeSerieEnsemble timeserie) {
        SimpleEnsemble p[] = getParameterEnsembles(d);
        int n = p.length;

        double result[][] = new double[n][timeserie.getTimesteps()];

        for (int i = 0; i < timeserie.getTimesteps(); i++) {
            XYSeries dataset[] = calculateSlopes(p, timeserie, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            for (int k = 0; k < n; k++) {
                double max = 0;
                for (int j = 0; j < dataset[k].getItemCount(); j++) {
                    max = Math.max(max, dataset[k].getY(j).doubleValue());
                }
                result[k][i] = max;
            }
        }

        return result;
    }*/
}
