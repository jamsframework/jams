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
import optas.optimizer.Optimizer;
import optas.optimizer.management.SampleFactory.Sample;
import optas.sampler.LatinHyperCubeSampler;
import optas.sampler.RandomSampler;
import optas.sampler.SobolsSequence;

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
    
    public enum SamplingMethod{Sobol, URS, URSStatic, LatinHyperCube, Default};
    
    SamplingMethod sm = SamplingMethod.URS;
    
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

    public void setSamplingMethod(SamplingMethod sm){
        this.sm = sm;
    }
    
    protected ArrayList<Sample> getRandomSampling(){
        if (model instanceof AbstractFunction) {
            Optimizer sampler = null;
            switch (sm){
                case Sobol: sampler = new SobolsSequence(); break;
                case URS:   sampler = new RandomSampler();  break;
                case URSStatic:   sampler = new RandomSampler();  break;
                case LatinHyperCube: sampler = new LatinHyperCubeSampler(); break;
                case Default: sampler = new RandomSampler();  break;
                default: sampler = new RandomSampler();  break;
                    
            }                        
            sampler.setFunction((AbstractFunction) model);
            sampler.setVerbose(false);
            //sampler.setAnalyzeQuality(false);
            //sampler.setBoundaries(getLowBound(), getUpBound());
            if (sm == SamplingMethod.URSStatic){
                sampler.setDebugMode(true);
            }else
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
