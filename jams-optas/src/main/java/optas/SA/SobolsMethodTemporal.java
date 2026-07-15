/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.io.File;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import optas.core.AbstractFunction;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerieEnsemble;
import optas.core.SampleLimitException;
import optas.sampler.SobolsSequence;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.Interpolation.ErrorMethod;
import optas.regression.TimeSerieNeuralNetwork;

/**
 *
 * @author chris
 */
public class SobolsMethodTemporal extends TemporalSensitivityAnalysis{
    SimpleEnsemble x[];
            
    protected int L;
    protected int sampleSize = 2000;

    int MAIN_EFFECT = 0;
    int TOTAL_EFFECT = 1;

    double A[][] = null;
    double B[][] = null;

    double x0A[] = null;
    double x0B[] = null;

    double yA[][] = null;
    double yB[][] = null;

    double EyA[] = null;
    double VyA[] = null;
    TimeSerieNeuralNetwork I;
    TimeSerieEnsemble tsStar;

    double sensitivityIndex[][][];
    
    double samplesCurrent;
    double samplesTotal;

    public SobolsMethodTemporal(SimpleEnsemble parameter[], EfficiencyEnsemble o, TimeSerieEnsemble ts, Measurement obs ){
        super(parameter, o, ts, obs);
    }

    public void loadNetworkState(File networkStateFile){
        this.init(networkStateFile);
    }
    public void saveNetworkState(File networkStateFile){
        this.I.save(networkStateFile);
    }

    public void setSampleSize(int sampleSize){
        this.sampleSize = sampleSize;
        isInit = false;
    }

    public int getSampleSize(){
        return sampleSize;
    }
        
    protected void sampleData(int size){
        log("Sample data");
        samplesCurrent = 0;
        samplesTotal   = size;

        x = new SimpleEnsemble[n];
        for (int j=0;j<n;j++){
            x[j] = new SimpleEnsemble(parameter[j].name + "(*)", size);
        }
        tsStar = new TimeSerieEnsemble(ts.name,size,ts.getTimeInterval());

        SobolsSequence sampler = new SobolsSequence();
        //TODO reimplement
       sampler.setFunction(new AbstractFunction() {
           @Override
           public int getInputDimension(){
               return n;
           }
           
           @Override
           public int getOutputDimension(){
               return T;
           }
           
           @Override
           public double[][] getRange(){
               double range[][] = new double[I.getN()][2];
               for (int i=0;i<I.getN();i++){
                   range[i][0] = getLowBound()[i];
                   range[i][1] = getUpBound()[i];
               }
               return range;
           }
            @Override
            public double[] evaluate(double[] x) { //throws SampleLimitException, ObjectiveAchievedException {
                SobolsMethodTemporal.this.setProgress(samplesCurrent/samplesTotal);
                return I.getInterpolatedValue(x);
            }

            @Override
            public void log(String msg) {
                System.out.println(msg);
            }
        });

        //sampler.setAnalyzeQuality(false);
        //sampler.setBoundaries(getLowBound(), getUpBound());
        sampler.setDebugMode(false);
        //sampler.setInputDimension(n);
        sampler.setMaxn(size);
        //sampler.setOffset(0);
        //sampler.setOutputDimension(T);
        sampler.optimize();

        log("Sampling finished");
        ArrayList<Sample> result = sampler.getSamples();

        for (int i=0;i<result.size();i++){
            Sample s = result.get(i);
            for (int j=0;j<n;j++)
                x[j].add(i, s.x[j]);
            tsStar.add(i, s.F());
        }

        L = result.size();
    }
    
    private void updateData(){
        log("Generate Data");
        this.setProgress(0.0);

        sampleData(5000);

        log("Resample Data");
        this.setProgress(0.0);

        int Lh = L / 2;

        A = new double[Lh][];
        B = new double[Lh][];
        yA = new double[Lh][];
        yB = new double[Lh][];
        x0A = new double[n];
        x0B = new double[n];
        EyA = new double[T];
        VyA = new double[T];

        for (int i = 0; i < Lh; i++) {
            setProgress((double)i/(double)Lh);
            int id_iA = x[0].getId(i);
            int id_iB = x[0].getId(i + Lh);

            for (int j = 0; j < n; j++) {
                x0A[j] = x[j].getValue(id_iA);
                x0B[j] = x[j].getValue(id_iB);
            }
            A[i] = transformToUnitCube(x0A);
            yA[i] = this.tsStar.getValue(id_iA);
            B[i] = transformToUnitCube(x0B);
            yB[i] = this.tsStar.getValue(id_iB);

            for (int t=0;t<T;t++){
                EyA[t] += yA[i][t];
                VyA[t] += yA[i][t]*yA[i][t];
            }
        }
        for (int t=0;t<T;t++){
            EyA[t] /= Lh;
            VyA[t] = (VyA[t]/ Lh) - EyA[t]*EyA[t];
        }
    }

    protected double[] getInterpolation(double[] x){
        return I.getInterpolatedValue(x);
    }

    private void init(File f){
        log("Initialize Temporal Sensitivity Analysis");
        setProgress(0.0);

        I = new TimeSerieNeuralNetwork();
                
        log("Setup Interpolation method");
        for (Observer o : this.getObservers())
            I.addObserver(o);
        I.setData(parameter, ts);
        if (f!=null)
            I.load(f);

        I.init();
        
        
        updateData();        
        
        isInit = true;

        calcSensitivity();       
    }

    @Override
    public double[][] calculate(){
        super.calculate();
        
        log("Calculating Sensitivity Indicies");
        if (!isInit){
            init(null);
        }
        double sensitivity[][] = new double[n][T];

        for (int i=0;i<n;i++){
            for (int t=0;t<T;t++)
                sensitivity[i][t] = this.sensitivityIndex[i][t][TOTAL_EFFECT]; //using total sensitivity indices
        }

        //normalize
        for (int t = 0; t < T; t++) {
            double sum = 0;
            for (int i=0;i<n;i++){
                sum += sensitivity[i][t];
            }
            for (int i=0;i<n;i++){
                if (sensitivity[i][t] < 0.025*sum){
                    sensitivity[i][t] = 0;
                }
            }
            sum = 0;
            for (int i=0;i<n;i++){
                sum += sensitivity[i][t];
            }
            for (int i=0;i<n;i++){
                sensitivity[i][t] /= sum;
            }
        }

        return sensitivity;
    }
    
    private void calcSensitivity(){
        if (!isInit){
            init(null);
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
            init(null);
        }
        double sensitivityIndex[][] = new double[T][2];
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
        
        for (int t = 0; t < T; t++) {
            double ti1=0;
            double ti2=0;
            for (int i=0;i<Lh;i++){
                ti1 += yA[i][t]*yC[i][t];
                ti2 += yA[i][t]*yD[i][t];

                /*deltaY_AC[t] += Math.abs(yA[i][t] - yC[i][t]);
                deltaY_AD[t] += Math.abs(yA[i][t] - yD[i][t]);*/
            }
            ti1/=Lh;
            ti2/=Lh;

            double VyAC = ti1 - EyA[t]*EyA[t];
            double VyBC = ti2 - EyA[t]*EyA[t];

            sensitivityIndex[t][MAIN_EFFECT] = Math.max((VyAC / VyA[t]),0);
            sensitivityIndex[t][TOTAL_EFFECT] = Math.max(1.0 - (VyBC / VyA[t]),0);
        }

        return sensitivityIndex;
    }

    public double[] getCVError(int K){
        return this.I.estimateCrossValidationError(K, ErrorMethod.E2);
    }
}
