/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.SampleFactory.SampleComperator;
import optas.regression.SimpleInterpolation;
import optas.regression.SimpleNeuralNetwork;


/**
 *
 * @author chris
 */
public class Statistics implements Serializable{
    ArrayList<Sample> sampleList;

    ArrayList<Sample> bestSampleList=new ArrayList<Sample>();

    //this transient is more a performance issue
    transient ArrayList<SimpleInterpolation> I = null;

    public Statistics(ArrayList<Sample> sampleList) {
        this.sampleList = sampleList;
    }

    public void fireChangeEvent(){
        this.bestSampleList.clear();
    }



    public double[][] getParameterSpace(int start, int end, int index, double percentil){
        double mean=calcMean(start,end,index);
        double var=calcVariance(start, start, index);

        int n = n();
        double min[] = new double[n];
        double max[] = new double[n];
        
        for (int i=0;i<n;i++){
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
        }
        double omega = jams.math.distributions.CDF_Normal.xnormi(percentil);

        for (int i=start;i<end;i++){
            Sample s = sampleList.get(i);
            if (s.F()[index]<mean+omega*var ){
                for (int j=0;j<n;j++){
                    min[j] = Math.min(s.getParameter()[j],min[j]);
                    max[j] = Math.max(s.getParameter()[j],max[j]);
                }
            }
        }
        return new double[][]{min,max};
    }


    public double calcVariance(int index){
        return calcVariance(0, size(), index);
    }

    public double calcVariance(int start, int last, int index){
        start = checkBounds(start, last)[0];
        last = checkBounds(start, last)[1];

        double var = 0;
        double mean = calcMean(start, last, index);
        for (int i=start;i<last;i++){
            double v = (sampleList.get(i).F()[index] - mean);
            var += v*v;
        }
        return Math.sqrt(var / (double)(last-start-1));

    }



    public double calcMean(int index){
        return calcMean(0, size(), index);
    }

    public double calcMean(int start, int last, int index){
        start = checkBounds(start, last)[0];
        last = checkBounds(start, last)[1];

        double mean = 0;
        for (int i=start;i<last;i++){
            mean += this.sampleList.get(i).F()[index];
        }
        return mean / (double)(last-start);
    }

    public double calcGeometricRange(int last){
        return calcGeometricRange(0,this.sampleList.size()-last);
    }

    public double calcGeometricRange(int start, int last){
        start = checkBounds(start, last)[0];
        last = checkBounds(start, last)[1];
       
        double range[] = new double[n()];
        double sum = 0;

        for (int j=0;j<n();j++){
            range[j] = calcGeometricRange(start, last, j);
            sum += range[j]*range[j];
        }

        return Math.sqrt(sum);
    }

    public double calcGeometricRange(int start, int last, int index){
        start = checkBounds(start,last)[0];
        last = checkBounds(start,last)[1];

        double min=Double.POSITIVE_INFINITY,
               max=Double.NEGATIVE_INFINITY;

        for (int i=start;i<last;i++){
            min = Math.min(min, this.sampleList.get(i).getParameter()[index]);
            max = Math.max(max, this.sampleList.get(i).getParameter()[index]);
        }
        
        return max-min;
    }

    public double calcImprovement(int last, int index){
        int start = Math.max(0, size()-last);
        double v1=0.0,v2=0.0;
        if (start!=0)
            v1 = getMinimumInRange(0, start, index).F()[index];
        else
            v1 = Double.POSITIVE_INFINITY;

        if (start+1<size())
            v2 = getMinimumInRange(start+1, size(), index).F()[index];
        else
            v2 = Double.POSITIVE_INFINITY;

        return 1.0 - (v2 / v1);
    }

    public Sample getMin(int index){
        return getMinimumInRange(0, size(), index);
    }

    public Sample getMax(int index){
        return getMaximumInRange(0, size(), index);
    }

    public Sample getMinimumInRange(int start, int last, int index){
        start = checkBounds(start,last)[0];
        last = checkBounds(start,last)[1];

        double min = Double.POSITIVE_INFINITY;
        Sample argMin = null;
        for (int i=start;i<last;i++){
            Sample s = this.sampleList.get(i);
            if (min >= s.F()[index]){
                min = s.F()[index];
                argMin = s;
            }
        }
        return argMin;
    }

    public Sample getMaximumInRange(int start, int last, int index){
        start = checkBounds(start,last)[0];
        last = checkBounds(start,last)[1];

        double max = Double.NEGATIVE_INFINITY;
        Sample argMax = null;
        for (int i=start;i<last;i++){
            Sample s = this.sampleList.get(i);
            if (max <= s.F()[index]){
                max = s.F()[index];
                argMax = s;
            }
        }
        return argMax;
    }

    private int[] checkBounds(int t1, int t2){
        if (t1 < 0 )    t1 = 0;
        if (t2 < 0)     t2 = 0;

        if (t1 >= size())    t1 = size()-1;
        if (t2 > size())    t2 = size();

        if (t1 > t2)
            t1 = t2;
        return new int[]{t1,t2};
    }

    public int size(){
        return this.sampleList.size();
    }

    public int n(){
        if (this.sampleList.isEmpty())
            return 0;
        else
            return this.sampleList.get(0).getParameter().length;
    }

    public int m(){
        if (this.sampleList.isEmpty())
            return 0;
        else
            return this.sampleList.get(0).F().length;
    }

    //TODO:implement this function ... 
    public ArrayList<Sample> getSamplesByRank(int rk){
        return null;
    }

    
    public ArrayList<Sample> getParetoSubset(boolean subset[]){
        if (subset.length != this.m())
            return null;
        
        /*if (!bestSampleList.isEmpty())
            return bestSampleList;*/
        bestSampleList.clear();
        if (this.m()==1){
            if (!subset[0])
                return new ArrayList<Sample>();
            
            SampleComperator comparer = new SampleComperator(true, subset);
            Sample best = null;
            Iterator<Sample> iter = sampleList.iterator();
            while(iter.hasNext()){
                Sample rivale = iter.next();
                if(best==null)
                    best=rivale;
                else if (comparer.compare(best,rivale)<0)
                    best = rivale;
            }
            bestSampleList.add(best);
            return bestSampleList;
        }

        SampleComperator comparer = new SampleComperator(true, subset);
        Iterator<Sample> iter = sampleList.iterator();
        while(iter.hasNext()){
            Sample candidate = iter.next();
            boolean isDominated = false;
            Iterator<Sample> iter2 = sampleList.iterator();
            while(iter2.hasNext()){
                Sample rivale = iter2.next();
                if (candidate == rivale)
                    continue;
                if (comparer.compare(candidate,rivale)<0){
                    isDominated = true;
                    break;
                }
            }
            if (!isDominated)
                bestSampleList.add(candidate);
        }
        return bestSampleList;
    }
    
    public ArrayList<Sample> getParetoFront(){        
        boolean subsetFilter[] = new boolean[m()];
        Arrays.fill(subsetFilter, true);
        return getParetoSubset(subsetFilter);
    }

    public void optimizeInterpolation(){
        for (SimpleInterpolation idw : I){
            idw.init();
            //idw.optimizeWeights();
        }
    }

    public double getMinimalParameter(int index){
        double min = Double.POSITIVE_INFINITY;
        for (Sample s : this.sampleList){
            min = Math.min(min, s.x[index]);
        }
        return min;
    }

    public double getMaximalParameter(int index){
        double max = Double.NEGATIVE_INFINITY;
        for (Sample s : this.sampleList){
            max = Math.max(max, s.x[index]);
        }
        return max;
    }
    
    public double calcQuality(){
        int L = this.sampleList.size();
        int Ls = (int)(L*0.9);
        int m = this.sampleList.get(0).fx.length;
        
        if (I == null){
            I = new ArrayList<SimpleInterpolation>();
            for (int i = 0; i < m; i++) {            
                SimpleNeuralNetwork nn = new SimpleNeuralNetwork();
                I.add(nn);
            }
        }
        SimpleEnsemble ensemble[] = new SimpleEnsemble[this.n()];

        if (this.sampleList.isEmpty()) {
            return 0.0;
        }

        double errorLOO = Double.POSITIVE_INFINITY;

        for (int i = 0; i < m; i++) {
            ArrayList<Double> valueList = new ArrayList<Double>();
            for (int j = 0; j < L; j++) {
                valueList.add(j, sampleList.get(j).fx[i]);
            }
            Collections.sort(valueList);

            double threshold = valueList.get(Ls);

            SimpleEnsemble y[] = new SimpleEnsemble[m];

            double rmin = valueList.get(L-Ls);
            double rmax = valueList.get(Ls);

            rmin -= (rmax-rmin)*0.2;
            rmax += (rmax-rmin)*0.2;

            y[i] = new EfficiencyEnsemble("test", L, false, rmin, rmax); //TODO .. die grenzen sind obj. abhängig ..

            for (int k = 0; k < n(); k++) {
                ensemble[k] = new SimpleEnsemble("test", L);
            }
            int counter = 0;
            for (int j = 0; j < L; j++) {
                /*if (sampleList.get(j).fx[i] < threshold )*/{
                    for (int k = 0; k < n(); k++) {
                        ensemble[k].add(counter, sampleList.get(j).x[k]);
                    }
                    y[i].add(counter, Math.min(sampleList.get(j).fx[i],threshold));
                }
                counter++;
            }

            SimpleInterpolation nn = I.get(i);
            nn.setData(ensemble, y[i]);
            double error[] = nn.estimateCrossValidationError(5, SimpleInterpolation.ErrorMethod.E2);
            for (int j=0;j<error.length;j++)
                errorLOO = Math.min(error[j], errorLOO);
        }

        return errorLOO;
    }

}
