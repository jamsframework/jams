/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.ArrayList;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Measurement;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerieEnsemble;
import optas.optimizer.HaltonSequenceSampling;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.SampleLimitException;
import optas.optimizer.management.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.Interpolation;
import optas.regression.NeuralNetwork;

/**
 *
 * @author chris
 */
public class VarianceBasedTemporalSensitivityAnalysis extends TemporalSensitivityAnalysis{
    SimpleEnsemble x[];
    
    protected SimpleEnsemble x_raw[];
    protected TimeSerieEnsemble ts_raw;

    protected int n;
    protected int L_raw=0,L;
    protected int sampleSize = 1000;
    protected boolean isInit = false;

    double A[][] = null;
    double B[][] = null;

    double x0A[] = null;
    double x0B[] = null;

    double yA[][] = null;
    double yB[][] = null;

    double deltaY_AB[] = null;

    Interpolation I;

    double range[][] = null;

    double sensitivityIndex[][][];

    public VarianceBasedTemporalSensitivityAnalysis(SimpleEnsemble parameter[], EfficiencyEnsemble o, TimeSerieEnsemble ts, Measurement obs ){
        super(parameter, o, ts, obs);

        this.x_raw = parameter;
        this.ts_raw = ts;
                
        n = x_raw.length;
        if (n==0){
            return;
        }
        L_raw = x_raw[0].getSize();

        for (int i=0;i<n;i++){
            if (parameter[i].getSize()!=L_raw)
                return;
        }
        if (ts_raw.getSize()!=L_raw)
            return;

        isInit = false;
    }

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

    double samplesCurrent;
    double samplesTotal;
    protected void sampleData(int size){
        log("Sample data");
        samplesCurrent = 0;
        samplesTotal   = size;

        x = new SimpleEnsemble[n];
        for (int j=0;j<n;j++){
            x[j] = new SimpleEnsemble(x_raw[j].name + "(*)", size);
        }
        ts = new TimeSerieEnsemble(ts_raw.name,size,ts_raw.getTimeInterval());

        HaltonSequenceSampling sampler = new HaltonSequenceSampling();        
        sampler.setFunction(new AbstractFunction() {
            @Override
            public double[] f(double[] x) throws SampleLimitException, ObjectiveAchievedException {
                VarianceBasedTemporalSensitivityAnalysis.this.setProgress(samplesCurrent/samplesTotal);
                return I.getValue(x);
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
        sampler.setOutputDimension(T);
        sampler.optimize();

        log("Sampling finished");
        ArrayList<Sample> result = sampler.getSamples();

        for (int i=0;i<result.size();i++){
            Sample s = result.get(i);
            for (int j=0;j<n;j++)
                x[j].add(i, s.x[j]);
            ts.add(i, s.F());
        }

        L = result.size();
    }
    
    private void updateData(){
        log("Generate Data");
        this.setProgress(0.0);

        sampleData(2000);

        log("Resample Data");
        this.setProgress(0.0);

        int Lh = L / 2;

        A = new double[Lh][];
        B = new double[Lh][];
        yA = new double[Lh][];
        yB = new double[Lh][];
        x0A = new double[n];
        x0B = new double[n];
        deltaY_AB = new double[T];

        for (int i = 0; i < Lh; i++) {
            setProgress((double)i/(double)Lh);
            int id_iA = x[0].getId(i);
            int id_iB = x[0].getId(i + Lh);

            for (int j = 0; j < n; j++) {
                x0A[j] = x[j].getValue(id_iA);
                x0B[j] = x[j].getValue(id_iB);
            }
            A[i] = transformToUnitCube(x0A);
            yA[i] = this.ts.getValue(id_iA);
            B[i] = transformToUnitCube(x0B);
            yB[i] = this.ts.getValue(id_iB);

            for (int t=0;t<T;t++){
                deltaY_AB[t] += Math.abs(yA[i][t] - yB[i][t]);
            }
        }
        for (int t=0;t<T;t++){
            deltaY_AB[t] /= Lh;
        }
    }

    protected double[] getInterpolation(double[] x){
        return I.getValue(x);
    }

    private void init(){
        log("Initialize Temporal Sensitivity Analysis");
        setProgress(0.0);

        I = new NeuralNetwork();
        SimpleEnsemble s[] = new SimpleEnsemble[T];
        for(int t=0;t<T;t++){
            s[t]= ts_raw.get(t);
        }
        log("Setup Interpolation method");
        for (Observer o : this.getObservers())
            I.addObserver(o);
        I.setData(x_raw, s);
        I.init();
        
        range = this.getParameterRange();
        updateData();        
        
        isInit = true;

        calcSensitivity();       
    }

    @Override
    public double[][] calculate(){
        log("Calculating Sensitivity Indicies");
        if (!isInit){
            init();
        }
        double sensitivity[][] = new double[n][T];

        for (int i=0;i<n;i++){
            for (int t=0;t<T;t++)
                sensitivity[i][t] = this.sensitivityIndex[i][t][0];
        }
        return sensitivity;
    }
    
    private void calcSensitivity(){
        if (!isInit){
            init();
        }
        sensitivityIndex = new double[n][][];

        for (int i=0;i<n;i++){
            TreeSet<Integer> set = new TreeSet<Integer>();
            set.add(i);
            log("Calculating Sensitivity for " + x[i].name);            
            sensitivityIndex[i] = calcSensitivity(set);
        }
    }

    public double[][] calcSensitivity(Set<Integer> indexSet) {
        if (!isInit){
            init();
        }
        double sensitivityIndex[][] = new double[T][3];
        int Lh = L / 2;
        
        double C[][] = new double[Lh][n];
        double D[][] = new double[Lh][n];
        double yC[][] = new double[Lh][];
        double yD[][] = new double[Lh][];

        for (int i = 0; i < Lh; i++) {
            for (int j = 0; j < n; j++) {
                if (indexSet.contains(j)) {
                    C[i][j] = A[i][j];
                    D[i][j] = B[i][j];
                } else {
                    C[i][j] = B[i][j];
                    D[i][j] = A[i][j];
                }
            }
            yC[i] = this.getInterpolation(transformFromUnitCube(C[i]));
            yD[i] = this.getInterpolation(transformFromUnitCube(D[i]));

            setProgress((double)i/(double)Lh);
        }

        double deltaY_AC[] = new double[T],
               deltaY_AD[] = new double[T];

        for (int i = 0; i < Lh; i++) {
            for (int t=0;t<T;t++){
                deltaY_AC[t] += Math.abs(yA[i][t] - yC[i][t]);
                deltaY_AD[t] += Math.abs(yA[i][t] - yD[i][t]);
            }
        }
        for (int t=0;t<T;t++){
            deltaY_AC[t] = deltaY_AC[t] / Lh;// - EY_AC*EY_AC;
            deltaY_AD[t] = deltaY_AD[t] / Lh;// - EY_BC*EY_BC;
            
            sensitivityIndex[t][0] = (deltaY_AD[t] / deltaY_AB[t]);
            sensitivityIndex[t][1] = 1.0 - (deltaY_AC[t] / deltaY_AB[t]);
        }

        return sensitivityIndex;
    }
}
