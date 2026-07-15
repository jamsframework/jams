/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;

/**
 *
 * @author chris
 */
public abstract class TimeSeriesInterpolation extends Interpolation{

    private TimeSerieEnsemble yTS;

    protected int n,m;   
    TimeSerie yMin,yMax;
    double yRange[];

    public int getN(){
        return n;
    }
    public int getM(){
        return m;
    }
        
    public void setData(SimpleEnsemble x[], TimeSerieEnsemble y){
        this.x = x;
        this.yTS = y;

        if (x.length==0){
            return;
        }

        n = x.length;
        m = y.getTimesteps();

        yMin = yTS.getMin();
        yMax = yTS.getMax();

        L = x[0].getSize();
        yRange = new double[m];
        
        for (int i=0;i<m;i++){
            if ((yMax.getValue(i) - yMin.getValue(i)) != 0)
                yRange[i] = 1.0 / (yMax.getValue(i) - yMin.getValue(i));
            else
                yRange[i] = 0.0;            
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
    
    public double getYData(int id, int time){
        return yTS.get(time, id);
    }
    
    protected double[] normalizeX(double u[]){
        double normalizedU[] = new double[u.length];
        for (int i=0;i<normalizedU.length;i++){
            normalizedU[i] = ((u[i]-this.xMin[i])*xRange[i]*2.0)-1.0;
        }

        return normalizedU;
    }

    protected double[] normalizeY(double y[]) {
        double normalizedY[] = new double[m];

        for (int i = 0; i < m; i++) {
            normalizedY[i] = ((y[i] - yMin.getValue(i)) * yRange[i] * 2.0) - 1.0;
        }

        return normalizedY;
    }

    protected double[] denormalizeX(double u[]){
        double denormalizedU[] = new double[u.length];
        for (int i=0;i<denormalizedU.length;i++){
            denormalizedU[i] = ((u[i]+1.0)/(2.0*xRange[i]))+xMin[i];
        }
        return denormalizedU;
    }

    protected double[] denormalizeY(double y[]){
        double denormalizedY[] = new double[m];
        for (int i = 0; i < m; i++) {
            if (yRange[i] != 0) {
                denormalizedY[i] = ((y[i] + 1.0) / (2.0 * yRange[i])) + yMin.getValue(i);
            } else {
                denormalizedY[i] = yMin.getValue(i);
            }
        }
        return denormalizedY;
    }
}
