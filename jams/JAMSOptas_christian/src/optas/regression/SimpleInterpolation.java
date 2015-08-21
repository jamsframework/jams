/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import optas.data.SimpleEnsemble;
import optas.tools.ObservableProgress;

/**
 *
 * @author chris
 */
public abstract class SimpleInterpolation extends Interpolation{

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

    protected SimpleEnsemble y[];

    protected int n,m;
    public int getN(){
        return n;
    }
    public int getM(){
        return m;
    }
    
    double yRange[], yMin[];

    TreeMap<Double, Double>[] xHistorgramm = null;
    TreeMap<Double, Double>[]   yHistorgramm = null;

    TreeMap<Double, Double>[] xInvHistorgramm = null;
    TreeMap<Double, Double>[] yInvHistorgramm = null;

    public enum NormalizationMethod{Linear, Histogramm};
    private NormalizationMethod xNormalizationMethod = NormalizationMethod.Linear;
    private NormalizationMethod yNormalizationMethod = NormalizationMethod.Linear;

    public void setData(SimpleEnsemble x[], SimpleEnsemble y){
        setData(x,new SimpleEnsemble[]{y});
    }
    public void setData(SimpleEnsemble x[], SimpleEnsemble y[]){
        this.x = x;
        this.y = y;
        
        if (x.length==0){
            return;
        }

        n = x.length;
        m = y.length;

        L = x[0].getSize();
        yRange = new double[m];
        yMin = new double[m];
        for (int i=0;i<m;i++){
            if (y[i].getSize()!=L)
                return;
            if ((y[i].getMax() - y[i].getMin()) != 0)
                yRange[i] = 1.0 / (y[i].getMax() - y[i].getMin());
            else
                yRange[i] = 0.0;
            yMin[i] = y[i].getMin();
        }

        this.xRange = new double[n];
        this.xMin = new double[n];
        //normalize between -1 and 1
        for (int i=0;i<n;i++){
            double min = x[i].getMin();
            double max = x[i].getMax();

            xRange[i] = 1.0 / (max-min);
            xMin[i] = min;
        }
        initSuccessful = true;
    }

    public double getYData(int id, int index){
        return y[index].getValue(id);
    }

    boolean histogrammValid = false;
    private void buildHistogramm(){
        xHistorgramm = new TreeMap[n];
        yHistorgramm = new TreeMap[m];

        xInvHistorgramm = new TreeMap[n];
        yInvHistorgramm = new TreeMap[m];

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
            xInvHistorgramm[i] = histInv;
        }

        for (int i = 0; i < m; i++) {
            TreeMap<Double, Double> hist = new TreeMap<Double, Double>();
            TreeMap<Double, Double> histInv = new TreeMap<Double, Double>();

            Integer sortedMap[] = y[i].sort(true);
            double sum = -1.0;
            double delta = 2.0 / sortedMap.length;
            for (int j = 0; j < sortedMap.length; j++) {
                double v = y[i].getValue(sortedMap[j]);
                hist.put(v, sum);
                histInv.put(sum, v);
                sum += delta;
            }
            yHistorgramm[i] = hist;
            yInvHistorgramm[i] = histInv;
        }
        histogrammValid = true;
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
                if (!histogrammValid)
                    buildHistogramm();
                normalizedU[i] = histogrammNormalization(u[i], xHistorgramm[i]);
            }
        }

        return normalizedU;
    }

    protected double[] normalizeY(double y[]){
        double normalizedY[] = new double[m];
        if (this.getyNormalizationMethod() == NormalizationMethod.Linear){
            for (int i=0;i<m;i++){
                normalizedY[i] = ((y[i]-yMin[i])*yRange[i]*2.0)-1.0;
            }
        }
        else if (this.getyNormalizationMethod() == NormalizationMethod.Histogramm) {
            if (!histogrammValid)
                buildHistogramm();
            for (int i=0;i<m;i++){
                normalizedY[i] = histogrammNormalization(y[i], yHistorgramm[i]);
            }
        }
        return normalizedY;
    }

    protected double[] denormalizeX(double u[]){
        double denormalizedU[] = new double[u.length];
        for (int i=0;i<denormalizedU.length;i++){
            if (this.getxNormalizationMethod() == NormalizationMethod.Linear)
                denormalizedU[i] = ((u[i]+1.0)/(2.0*xRange[i]))+xMin[i];
            else if (this.getxNormalizationMethod() == NormalizationMethod.Histogramm) {
                if (!histogrammValid)
                    buildHistogramm();
                denormalizedU[i] = histogrammNormalization(u[i], xInvHistorgramm[i]);
            }
        }
        return denormalizedU;
    }

    protected double[] denormalizeY(double y[]){
        double denormalizedY[] = new double[m];
        for (int i=0;i<m;i++){
            if (this.getxNormalizationMethod() == NormalizationMethod.Linear)
                if (yRange[i]!=0)
                    denormalizedY[i] = ((y[i]+1.0)/(2.0*yRange[i]))+yMin[i];
                else
                    denormalizedY[i] = yMin[i];
            else if (this.getxNormalizationMethod() == NormalizationMethod.Histogramm) {
                if (!histogrammValid)
                    buildHistogramm();
                denormalizedY[i] = histogrammNormalization(y[i], yInvHistorgramm[i]);
            }
        }
        return denormalizedY;
    }
}
