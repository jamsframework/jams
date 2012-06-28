/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.ArrayList;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.SimpleEnsemble;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.SampleLimitException;
import optas.optimizer.SobolsSequenceSampling;
import optas.optimizer.management.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.SimpleInterpolation;
import optas.regression.SimpleNeuralNetwork;

/**
 *
 * @author chris
 */
public abstract class SensitivityAnalyzer {
    SimpleEnsemble x[];
    EfficiencyEnsemble y;

    protected int n,m;
    protected int L_raw=0,L;

    protected boolean isInit = false;

    SimpleInterpolation I;

    private double CVError = 0;

    protected SimpleEnsemble x_raw[];
    protected EfficiencyEnsemble y_raw;

    private boolean isUsingInterpolation = true;
    protected int sampleSize = 1000;
    double range[][] = null;

    double sensitivityIndex[];
    double sensitivityVariance[];

    boolean isVarianceCalulated = false;

    protected double[] transformFromUnitCube(double x[]){
        double[] y = new double[n];
        for (int i=0;i<n;i++){
            y[i] = range[i][0] + x[i]*(range[i][1]-range[i][0]);
        }
        return y;
    }
    protected double[] transformToUnitCube(double x[]){
        double[] y = new double[n];
        for (int i=0;i<n;i++){
            y[i] = (x[i]-range[i][0])/(range[i][1]-range[i][0]);
        }
        return y;
    }

    protected double[][] getParameterRange() {
        double range[][] = new double[n][2];

        for (int j = 0; j < n; j++) {
            range[j][0] = x_raw[j].getMin();
            range[j][1] = x_raw[j].getMax();
        }
        return range;
    }

    public void setInterpolation(boolean isUsingInterpolation){
        this.setIsUsingInterpolation(isUsingInterpolation);
        isInit = false;
    }

    public boolean getInterpolation(){
        return this.isIsUsingInterpolation();
    }

    public void setSampleSize(int sampleSize){
        this.sampleSize = sampleSize;
        isInit = false;
    }

    public int getSampleSize(){
        return sampleSize;
    }

    private double[] getLowBound(){
        double lb[] = new double[n];
        for (int j = 0; j < n; j++) {
            lb[j] = x_raw[j].getMin();
        }
        return lb;
    }

    private double[] getUpBound(){
        double ub[] = new double[n];
        for (int j = 0; j < n; j++) {
            ub[j] = x_raw[j].getMax();
        }
        return ub;
    }

    protected void sampleData(int size){
        x = new SimpleEnsemble[n];
        for (int j=0;j<n;j++){
            x[j] = new SimpleEnsemble(x_raw[j].name + "(*)", size);
        }

        y = new EfficiencyEnsemble(y_raw.name + "(*)", size, y_raw.isPositiveBest());

        SobolsSequenceSampling sampler = new SobolsSequenceSampling();
        //RandomSampler sampler = new RandomSampler();
        sampler.setFunction(new AbstractFunction() {
            @Override
            public double[] f(double[] x) throws SampleLimitException, ObjectiveAchievedException {
                return I.getInterpolatedValue(x);
            }

            @Override
            public void logging(String msg) {
                System.out.println(msg);
            }
        });

        sampler.setAnalyzeQuality(false);


        sampler.setBoundaries(getLowBound(), getUpBound());
        sampler.setDebugMode(false);
        sampler.setInputDimension(n);
        sampler.setMaxn(size);
        sampler.setOffset(0);
        sampler.setOutputDimension(1);
        sampler.optimize();
        ArrayList<Sample> result = sampler.getSamples();

        for (int i=0;i<result.size();i++){
            Sample s = result.get(i);
            for (int j=0;j<n;j++)
                x[j].add(i, s.x[j]);
            y.add(i, s.F()[0]);
        }

        L = result.size();
    }

    public void setData(SimpleEnsemble x[], EfficiencyEnsemble y){
        this.x_raw = x;
        this.y_raw = y;

        n = x_raw.length;
        if (n==0){
            return;
        }
        L_raw = x_raw[0].getSize();
        for (int i=0;i<n;i++){
            if (x[i].getSize()!=L_raw)
                return;            
        }
        if (y_raw.getSize()!=L_raw)
            return;
        
        isInit = false;
    }

    public void setInterpolationMethod(SimpleInterpolation I){
        this.I = I;
        isInit = false;
    }

    public SimpleInterpolation getInterpolationMethod(){
        return I;
    }

    private void updateData(){
        if (this.isIsUsingInterpolation() && I != null){
            sampleData(sampleSize);
        }else{
            this.x = x_raw;
            this.y = y_raw;
            L = y.getSize();
        }
    }

    public double getCVError(){
        if (this.isUsingInterpolation){
            double error[] = I.estimateCrossValidationError(5, SimpleInterpolation.ErrorMethod.E2);
            CVError = 0;
            for (int i=0;i<error.length;i++)
                CVError += error[i];
            CVError /= error.length;

            return this.CVError;
        } else
            return 0.0;
    }

    protected double getInterpolation(double[] x){
        return I.getInterpolatedValue(x)[0];
    }
            
    public void init(){
        if (this.isUsingInterpolation) {
            if (I == null) {
                setInterpolationMethod(new SimpleNeuralNetwork());
            }

            I.setData(x_raw, y_raw);
            I.init();

            
        }
        updateData();

        range = this.getParameterRange();

        sensitivityIndex =new double[n];
        sensitivityVariance = new double[n];

        isInit = true;
    }

    public double getSensitivity(int parameter){
        if (!isInit){
            System.out.println("Call to getSensitivity without calling init at first!");
            return -1.0;
        }
        return sensitivityIndex[parameter];
    }
    
    public double getVariance(int parameter){
        int K = 10;
        if (!isVarianceCalulated){
            double statistics[][] = new double[n][K];
            double mean[] = new double[n];
            for (int k=0;k<K;k++){
                init();
                for (int j=0;j<n;j++){
                    statistics[j][k] = sensitivityIndex[j];
                    mean[j] += statistics[j][k];
                    sensitivityVariance[j] = 0;
                }
            }
            for (int k=0;k<K;k++){
                for (int j=0;j<n;j++){
                    sensitivityVariance[j] += (statistics[j][k] - mean[j]/K)*(statistics[j][k] - mean[j]/K);
                }
            }
            for (int j=0;j<n;j++){
                sensitivityVariance[j] /= (K-1);
            }
            isVarianceCalulated = true;
        }
        return Math.sqrt(sensitivityVariance[parameter]);
    }

    /**
     * @return the isUsingInterpolation
     */
    public boolean isIsUsingInterpolation() {
        return isUsingInterpolation;
    }

    /**
     * @param isUsingInterpolation the isUsingInterpolation to set
     */
    public void setIsUsingInterpolation(boolean isUsingInterpolation) {
        this.isUsingInterpolation = isUsingInterpolation;
    }
}
