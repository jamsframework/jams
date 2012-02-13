/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import optas.hydro.data.SimpleEnsemble;

/**
 *
 * @author chris
 */
public abstract class Interpolation {

    /**
     * @return the xNormalizationMethod
     */
    public NormalizationMethod getxNormalizationMethod() {
        return xNormalizationMethod;
    }

    /**
     * @param xNormalizationMethod the xNormalizationMethod to set
     */
    public void setxNormalizationMethod(NormalizationMethod xNormalizationMethod) {
        this.xNormalizationMethod = xNormalizationMethod;
    }

    /**
     * @return the yNormalizationMethod
     */
    public NormalizationMethod getyNormalizationMethod() {
        return yNormalizationMethod;
    }

    /**
     * @param yNormalizationMethod the yNormalizationMethod to set
     */
    public void setyNormalizationMethod(NormalizationMethod yNormalizationMethod) {
        this.yNormalizationMethod = yNormalizationMethod;
    }

    public enum ErrorMethod{ABSE, RMSE,E2};

    protected SimpleEnsemble x[];
    protected SimpleEnsemble y;

    protected int n,m;
    protected int L=0;

    protected boolean initSuccessful = false;

    double xRange[];
    double xMin[];

    double yRange, yMin;

    TreeMap<Double, Double>[] xHistorgramm = null;
    TreeMap<Double, Double>   yHistorgramm = null;

    TreeMap<Double, Double>[] xInvHistorgramm = null;
    TreeMap<Double, Double>   yInvHistorgramm = null;

    public enum NormalizationMethod{Linear, Histogramm};
    private NormalizationMethod xNormalizationMethod = NormalizationMethod.Linear;
    private NormalizationMethod yNormalizationMethod = NormalizationMethod.Linear;

    public void setData(SimpleEnsemble x[], SimpleEnsemble y){
        this.x = x;
        this.y = y;
        
        if (x.length==0){
            return;
        }

        n = x.length;
        L = x[0].getSize();
                  
        if (y.getSize()!=L)
            return;

        this.xRange = new double[n];
        this.xMin = new double[n];
        //normalize between -1 and 1
        for (int i=0;i<n;i++){
            double min = x[i].getMin();
            double max = x[i].getMax();

            xRange[i] = 1.0 / (max-min);
            xMin[i] = min;
        }
        yRange = 1.0 / (y.getMax() - y.getMin());
        yMin = y.getMin();

        xHistorgramm = new TreeMap[n];
        yHistorgramm = new TreeMap<Double, Double>();

        xInvHistorgramm = new TreeMap[n];
        yInvHistorgramm = new TreeMap<Double, Double>();

        for (int i=0;i<n;i++){
            TreeMap<Double, Double> hist = new TreeMap<Double, Double>();
            TreeMap<Double, Double> histInv = new TreeMap<Double, Double>();
            Integer sortedMap[] = x[i].sort(true);
            double sum = -1.0;
            double delta = 2.0 / sortedMap.length;
            for (int j=0;j<sortedMap.length;j++){
                double v = x[i].getValue(sortedMap[j]);
                hist.put(v, sum);
                histInv.put(sum, v);
                sum += delta;
            }
            xHistorgramm[i] = hist;
        }

        Integer sortedMap[] = y.sort(true);
        double sum = -1.0;
        double delta = 2.0 / sortedMap.length;
        for (int j=0;j<sortedMap.length;j++){
            double v = y.getValue(sortedMap[j]);
            yHistorgramm.put(v, sum);
            yInvHistorgramm.put(sum, v);
            sum += delta;
        }

        initSuccessful = true;
    }

    private double histogrammNormalization(double u, TreeMap<Double, Double> histogramm){
        Entry upper = histogramm.ceilingEntry(u);
        Entry lower = histogramm.floorEntry(u);
        double normalizedU = 0;
        if (upper == null) {
            normalizedU = histogramm.lastEntry().getValue();
        } else if (lower == null) {
            normalizedU = histogramm.firstEntry().getValue();
        } else {
            double w1 = u - (Double) lower.getKey();
            double w2 = (Double) upper.getKey() - u;
            if (w1 + w2 == 0) {
                w1 = 1.0;
                w2 = 0.0;
            }
            double r = w1 / (w1 + w2);
            double s = w2 / (w1 + w2);

            normalizedU = s * (Double) lower.getValue() + r * (Double) upper.getValue();
        }
        return normalizedU;
    }

    protected double[] normalizeX(double u[]){
        double normalizedU[] = new double[u.length];
        for (int i=0;i<normalizedU.length;i++){
            if (this.getxNormalizationMethod() == NormalizationMethod.Linear)
                normalizedU[i] = ((u[i]-this.xMin[i])*xRange[i]*2.0)-1.0;
            else if (this.getxNormalizationMethod() == NormalizationMethod.Histogramm) {
                normalizedU[i] = histogrammNormalization(u[i], xHistorgramm[i]);
            }
        }

        return normalizedU;
    }

    protected double normalizeY(double y){
        if (this.getyNormalizationMethod() == NormalizationMethod.Linear)
            return ((y-yMin)*yRange*2.0)-1.0;
        else if (this.getyNormalizationMethod() == NormalizationMethod.Histogramm) {
            return histogrammNormalization(y, yHistorgramm);
        }else
            return 0;
    }

    protected double[] denormalizeX(double u[]){
        double denormalizedU[] = new double[u.length];
        for (int i=0;i<denormalizedU.length;i++){
            if (this.getxNormalizationMethod() == NormalizationMethod.Linear)
                denormalizedU[i] = ((u[i]+1.0)/(2.0*xRange[i]))+xMin[i];
            else if (this.getxNormalizationMethod() == NormalizationMethod.Histogramm) {
                denormalizedU[i] = histogrammNormalization(u[i], xInvHistorgramm[i]);
            }
        }
        return denormalizedU;
    }

    protected double denormalizeY(double y){
        if (this.getyNormalizationMethod() == NormalizationMethod.Linear)
            return ((y+1.0)/(2.0*yRange))+yMin;
        else if (this.getyNormalizationMethod() == NormalizationMethod.Histogramm) {
            return histogrammNormalization(y, yInvHistorgramm);
        }else
            return 0;

    }

    public void init() {        
    }

    protected double[] getX(int id){
        double row[] = new double[n];
        for (int i=0;i<n;i++){
            row[i] = x[i].getValue(id);
        }
        return row;
    }

    abstract protected double[] getValue(TreeSet<Integer> leaveOut);
    public abstract double getValue(double u[]);

    private double calcDifference(ErrorMethod e, double sim[], double obs[]){
        int K = sim.length;
        double error = 0;
        switch (e){
            case ABSE:
                for (int i=0;i<K;i++){
                    error += Math.abs(sim[i] - obs[i]);
                }
                return error / K;
            case RMSE:
                for (int i=0;i<K;i++){
                    error += (sim[i] - obs[i])*(sim[i] - obs[i]);
                }
                return Math.sqrt(error/K);
            case E2:
                double aobs = 0;
                for (int i=0;i<K;i++){
                    aobs += obs[i];
                }
                aobs /= K;

                double numerator = 0;
                double denumerator = 0;
                for (int i=0;i<K;i++){
                    numerator += (sim[i] - obs[i])*(sim[i] - obs[i]);
                    denumerator += (obs[i] - aobs)*(obs[i] - aobs);
                }
                return 1.0 - (numerator / denumerator);
        }
        return 0.0;
    }
        
    private double funct(){        
        return estimateLOOError(ErrorMethod.RMSE);
    }
    
    protected void calculate(){
        
    }

    public double estimateCrossValidationError(int K, ErrorMethod e){       
        double obs[] = new double[L];
        double sim[] = new double[L];

        this.calculate();

        for (int k=0;k<K;k++){
            int indexStart = k*(L/K);
            int indexEnd   = Math.min((k+1)*(L/K),L);
            int size = indexEnd-indexStart;
            if (size == 0)
                continue;

            TreeSet<Integer> validationSet = new TreeSet<Integer>();
            double validation[] = new double[size];

            for (int j=indexStart;j<indexEnd;j++){
                int id_loi = x[0].getId(j);
                validationSet.add(id_loi);
                validation[j-indexStart] = y.getValue(id_loi);
            }

            double y_star[] = getValue(validationSet);

            for (int j=0;j<size;j++){
                //error += Math.abs(y_star[j] - validation[j]);
                obs[indexStart+j] = validation[j];
                sim[indexStart+j] = y_star[j];
                
            }

        }
        /*
        for (int i=0;i<obs.length;i++){
            System.out.println(obs[i] + "\t" + sim[i]);
        }*/

        return calcDifference(e, sim, obs);
    }

    public double estimateLOOError(ErrorMethod e){
        double error = 0;

        double obs[] = new double[L];
        double sim[] = new double[L];

        this.calculate();

        for (int leaveOutIndex = 0;leaveOutIndex<L;leaveOutIndex++){
            double x_star[] = new double[n];

            int id_loi = x[0].getId(leaveOutIndex);

            //TODO: wer stellt sicher, dass wir unter dem gleichen index auch die gleiche id finden???
            for (int i=0;i<n;i++){
                x_star[i] = x[i].getValue(id_loi);
            }

            TreeSet<Integer> indexSet = new TreeSet<Integer>();
            indexSet.add(id_loi);
            double y_star[] = getValue(indexSet);


            error += Math.abs(y_star[0] - y.getValue(id_loi));

            obs[leaveOutIndex] = y.getValue(id_loi);
            sim[leaveOutIndex] = y_star[0];
        }

        return calcDifference(e, sim, obs);
    }    
}
