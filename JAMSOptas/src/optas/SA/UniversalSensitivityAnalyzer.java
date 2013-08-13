/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Set;
import optas.SA.SobolsMethod.Measure;
import optas.core.AbstractDataSerie;
import optas.core.AbstractFunction;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.SimpleInterpolation;
import optas.regression.SimpleInterpolation.NormalizationMethod;
import optas.regression.SimpleNeuralNetwork;

/**
 *
 * @author chris
 */
public class UniversalSensitivityAnalyzer {
    public enum SAMethod{RSA, MaximumGradient, ElementaryEffects, ElementaryEffectsNonAbs, ElementaryEffectsVariance, FOSI1, FOSI2, TOSI, Interaction, LinearRegression};

    SAMethod method = SAMethod.RSA;
    
    SensitivityAnalyzer sa = null;
    SimpleInterpolation I  = null;
    
    boolean usingRegression = false;

    NormalizationMethod parameterNormalizationMethod = SimpleInterpolation.NormalizationMethod.Linear;
    NormalizationMethod objectiveNormalizationMethod = SimpleInterpolation.NormalizationMethod.Linear;

    SimpleEnsemble xData[] = null;
    EfficiencyEnsemble yData = null;
    double range[][] = null;        
    int sampleCount = 2000;
    int n = 0;

    public void setParameterNormalizationMethod(NormalizationMethod normalizationMethod){
        this.parameterNormalizationMethod = normalizationMethod;
    }
    public void setObjectiveNormalizationMethod(NormalizationMethod normalizationMethod){
        this.objectiveNormalizationMethod = normalizationMethod;
    }
    public NormalizationMethod getParameterNormalizationMethod(){
        return this.parameterNormalizationMethod;
    }
    public NormalizationMethod getObjectiveNormalizationMethod(){
        return this.objectiveNormalizationMethod;
    }
    public int getSampleCount(){
        return sampleCount;
    }
    public void setSampleCount(int sampleCount){
        this.sampleCount = sampleCount;
    }

    public SAMethod getMethod(){
        return method;
    }

    public void setMethod(SAMethod method){
        switch(method){
            case RSA: sa = new optas.SA.RegionalSensitivityAnalysis(); break;
            case MaximumGradient: sa = new optas.SA.GradientSensitivityAnalysis(); break;
            case ElementaryEffects: sa = new optas.SA.MorrisMethod(); break;
            case ElementaryEffectsNonAbs: sa = new optas.SA.MorrisMethod(MorrisMethod.Measure.NonAbsolute); break;
            case ElementaryEffectsVariance: sa = new optas.SA.MorrisMethod(MorrisMethod.Measure.Variance); break;
            case FOSI1: sa = new optas.SA.FAST(optas.SA.FAST.Measure.FirstOrder); break;
            case FOSI2: sa = new optas.SA.SobolsMethod(Measure.FirstOrder); break;
            case TOSI: sa = new optas.SA.SobolsMethod(Measure.Total); break;
            case Interaction: sa = new optas.SA.SobolsMethod(Measure.Interaction); break;
            case LinearRegression: sa = new optas.SA.LinearRegression(); break;
        }
    }

    public boolean isUsingRegression(){
        return this.usingRegression;
    }
    public void setUsingRegression(boolean flag){
        this.usingRegression = flag;
    }

    public void setup(SimpleEnsemble xData[], EfficiencyEnsemble yData){
        setup(xData,yData,new SimpleNeuralNetwork());
    }
    
    public void setup(SimpleEnsemble xData[], EfficiencyEnsemble yData, SimpleInterpolation interpolationAlgorithm){
        this.xData = xData;
        this.yData = yData;
        this.n     = xData.length;
        this.range = new double[n][2];
        
        
        if (usingRegression){
            this.I = interpolationAlgorithm;
            if (I == null){
                I = new SimpleNeuralNetwork();
            }
            I.setData(xData, yData);
            I.setxNormalizationMethod(parameterNormalizationMethod);
            I.setyNormalizationMethod(objectiveNormalizationMethod);
            I.init();
        }
        
        for (int i=0;i<n;i++){
            range[i][0] = xData[i].getMin();
            range[i][1] = xData[i].getMax();
        }
        if (usingRegression) {
            sa.setModel(new AbstractFunction() {
                @Override
                public int getInputDimension() {
                    return UniversalSensitivityAnalyzer.this.n;
                }

                @Override
                public int getOutputDimension() {
                    return 1;
                }

                @Override
                public double[][] getRange() {
                    return range;
                }

                @Override
                public String[] getInputFactorNames() {
                    String names[] = new String[n];
                    for (int i = 0; i < getInputDimension(); i++) {
                        names[i] = UniversalSensitivityAnalyzer.this.xData[i].getName();
                    }
                    return names;
                }

                @Override
                public String[] getOutputFactorNames() {
                    return new String[]{UniversalSensitivityAnalyzer.this.yData.getName()};
                }
                
                int counter = 0;
                SampleFactory factory = new SampleFactory();
                
                @Override
                public double[] evaluate(double[] x) {
                    if (usingRegression) {
                        return I.getInterpolatedValue(x);
                    } else {
                        return null;
                    }
                }

                @Override
                public void log(String msg) {
                    System.out.println(msg);
                }
            });
            sa.setSampleSize(sampleCount);
        }else{
            sa.setModel(new AbstractDataSerie() {
                @Override
                public void reset(){
                    this.counter = 0;
                    factory = new SampleFactory();
                }
                @Override
                public int getInputDimension() {
                    return UniversalSensitivityAnalyzer.this.n;
                }

                @Override
                public int getOutputDimension() {
                    return 1;
                }

                @Override
                public double[][] getRange() {
                    return range;
                }

                @Override
                public String[] getInputFactorNames() {
                    String names[] = new String[n];
                    for (int i = 0; i < getInputDimension(); i++) {
                        names[i] = UniversalSensitivityAnalyzer.this.xData[i].getName();
                    }
                    return names;
                }

                @Override
                public String[] getOutputFactorNames() {
                    return new String[]{UniversalSensitivityAnalyzer.this.yData.getName()};
                }
                int counter = 0;
                SampleFactory factory = new SampleFactory();

                @Override
                public Sample getNext() {
                    double x[] = new double[getInputDimension()];
                    if (counter >= UniversalSensitivityAnalyzer.this.xData[0].getSize()) {
                        return null;
                    }

                    int nextId = UniversalSensitivityAnalyzer.this.xData[0].getId(counter++);

                    for (int i = 0; i < x.length; i++) {
                        x[i] = UniversalSensitivityAnalyzer.this.xData[i].getValue(nextId);
                    }
                    return factory.getSampleSO(x, UniversalSensitivityAnalyzer.this.yData.getValue(nextId));
                }
                
                @Override
                public void log(String msg) {
                    System.out.println(msg);
                }
            });
            sa.setSampleSize(this.yData.getSize());
        }
        
        
    }

    public SimpleEnsemble[] getXDataSet(){
        return this.xData;
    }
    public EfficiencyEnsemble getYDataSet(){
        return this.yData;
    }
    
    public double[] getInteraction(Set<Integer> indexSet){
        if (sa instanceof SobolsMethod){
            SobolsMethod v = (SobolsMethod)sa;
            v.calcAll();
            return v.calcSensitivity(indexSet);
        }
        return null;
    }

    public double[] getSensitivity(){
        double result[] = new double[n];
        for (int i=0;i<n;i++){
            double s = sa.getSensitivity(i);
            //double v = sa.getVariance(i);
            result[i] = s;//s-v;
            /*result[i][1] = s;
            result[i][2] = s;//s+v;*/
        }
        return result;
    }

    public double[][] getUncertaintyOfSensitivity(int iterations){        
        double result[][] = new double[n][3];
        
        for (int i = 0; i < n; i++) {
            result[i][0] = Double.POSITIVE_INFINITY;
            result[i][1] = 0.0;
            result[i][2] = Double.NEGATIVE_INFINITY;
        }
        
        for (int j = 0; j < iterations; j++) {
            sa.calculate();
            for (int i = 0; i < n; i++) {
                double s = sa.getSensitivity(i);

                result[i][0] = Math.min(result[i][0], s);
                result[i][1] += s/(double)iterations;
                result[i][2] = Math.max(result[i][2], s);
            }
        }
        return result;
        
    }
    
    public double calculateError() {
        if (usingRegression){
            double error[] = I.estimateCrossValidationError(5, SimpleInterpolation.ErrorMethod.E2);
            double meanCrossValidationError = 0;
            for (int i=0;i<error.length;i++)
                meanCrossValidationError += error[i];
            meanCrossValidationError /= error.length;

            return meanCrossValidationError;
        }else
            return 0.0;
    }
}
