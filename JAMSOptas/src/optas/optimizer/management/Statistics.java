/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.SampleFactory.SampleComperator;
import optas.regression.IDW;
import optas.regression.Interpolation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 *
 * @author chris
 */
public class Statistics {
    ArrayList<Sample> sampleList;

    ArrayList<Sample> bestSampleList=new ArrayList<Sample>();

    ArrayList<Interpolation> I = null;

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
        double omega = optas.math.distributions.CDF_Normal.xnormi(percentil);

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
            if (min > s.F()[index]){
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
            if (max < s.F()[index]){
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

    public ArrayList<Sample> getSamplesByRank(int rk){
        throw new NotImplementedException();
    }

    public ArrayList<Sample> getParetoFront(){
        if (!bestSampleList.isEmpty())
            return bestSampleList;

        SampleComperator comparer = new SampleComperator(true);
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

    public void optimizeInterpolation(){
        for (Interpolation idw : I){
            idw.init();
            idw.optimizeWeights();
        }
    }

    public double calcQuality(){
        int L = this.sampleList.size();
        int m = this.sampleList.get(0).fx.length;
        
        if (I == null){
            I = new ArrayList<Interpolation>();
            for (int i = 0; i < m; i++) {            
                IDW idw = new IDW();
                I.add(idw);
            }
        }
        SimpleEnsemble ensemble[] = new SimpleEnsemble[this.n()];

        if (this.sampleList.isEmpty()) {
            return 0.0;
        }

        SimpleEnsemble y[] = new SimpleEnsemble[m];

        for (int i = 0; i < n(); i++) {
            ensemble[i] = new SimpleEnsemble("test", L);

            for (int j = 0; j < L; j++) {
                ensemble[i].add(j, sampleList.get(j).x[i]);
            }
        }

        double errorLOO = Double.POSITIVE_INFINITY;
        
        for (int i = 0; i < m; i++) {
            y[i] = new EfficiencyEnsemble("test", L, false, 0, 5); //TODO .. die grenzen sind obj. abhängig ..

            for (int j = 0; j < L; j++) {
                y[i].add(j, sampleList.get(j).fx[i]);
            }
            Interpolation idw = I.get(i);
            idw.setData(ensemble, y[i]);            
            errorLOO = Math.min(idw.estimateLOOError(Interpolation.ErrorMethod.E2), errorLOO);
        }

        return errorLOO;
    }

}
