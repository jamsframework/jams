/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.ArrayList;
import optas.core.AbstractDataSerie;
import optas.core.AbstractFunction;
import optas.core.AbstractModel;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.sampler.SobolsSequence;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public abstract class SensitivityAnalyzer {
     
    protected int n,m, sampleSize = 1000;
    private AbstractModel model = null;
    private double range[][] = null;

    double sensitivityIndex[];
    double sensitivityVariance[];

    boolean isVarianceCalulated = false;
    
    private double[][] getParameterRange() {        
        return range;
    }

    public void setModel(AbstractModel model){
        this.model = model;
        this.range = model.getRange().clone();
        this.n = model.getInputDimension();
        this.m = model.getOutputDimension();

        range = this.getParameterRange();
        
        sensitivityIndex = null;
        sensitivityVariance = null;
    }
    
    public AbstractModel getModel(){
        return model;
    }

    public void setSampleSize(int sampleSize){
        this.sampleSize = sampleSize;
        
        sensitivityIndex = null;
        sensitivityVariance = null;
    }

    public int getSampleSize(){
        return sampleSize;
    }

    protected ArrayList<Sample> getRandomSampling(){
        if (model instanceof AbstractFunction) {
            SobolsSequence sampler = new SobolsSequence();
            sampler.setFunction((AbstractFunction) model);
            //sampler.setAnalyzeQuality(false);
            //sampler.setBoundaries(getLowBound(), getUpBound());
            sampler.setDebugMode(false);
            //sampler.setInputDimension(n);
            sampler.setMaxn(sampleSize);
            //sampler.setOffset(0);
            //sampler.setOutputDimension(1);
            sampler.optimize();
            return sampler.getSamples();
        }else if (model instanceof AbstractDataSerie){
            AbstractDataSerie staticModel = (AbstractDataSerie)this.model;
            staticModel.reset();
            Sample s = null;
            ArrayList<Sample> result = new ArrayList<Sample>();
            while ( (s = staticModel.getNext())!=null && result.size() < sampleSize){
                result.add(s);
            }
            return result;
        }else{
            return null;
        }
    }
       
    void calculate(){
        sensitivityIndex = new double[n];
        sensitivityVariance = new double[n];
    }
    
    public double getSensitivity(int parameter){
        if (sensitivityIndex == null){
            calculate();
        }
        return sensitivityIndex[parameter];
    }
    
    public double getVariance(int parameter){
        int K = 10;
        if (!isVarianceCalulated){
            double statistics[][] = new double[n][K];
            double mean[] = new double[n];
            for (int k=0;k<K;k++){
                calculate();
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
    
    protected double evaluateModel(double x[]){
        if (model instanceof AbstractFunction){
            try{
                return ((AbstractFunction)model).evaluate(transformFromUnitCube(x))[0];
            }catch(SampleLimitException sle){
                return 0.0;
            }catch(ObjectiveAchievedException oae){
                return 0.0;
            }
        }else{
            throw new UnsupportedOperationException("Not supported by Model!");
        }        
    }
}
