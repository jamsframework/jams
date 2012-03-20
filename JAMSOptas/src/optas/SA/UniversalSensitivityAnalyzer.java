/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Set;
import optas.SA.VarianceBasedSensitivityIndex.Measure;

import optas.gui.MCAT5.MCAT5Plot.NoDataException;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.SimpleEnsemble;
import optas.regression.Interpolation;
import optas.regression.Interpolation.NormalizationMethod;
import optas.regression.NeuralNetwork;

/**
 *
 * @author chris
 */
public class UniversalSensitivityAnalyzer {
    public enum SAMethod{RSA, MaximumGradient, ElementaryEffects, ElementaryEffectsNonAbs, ElementaryEffectsVariance, FOSI1, FOSI2, TOSI, Interaction, LinearRegression};

    SAMethod method = SAMethod.RSA;
    optas.SA.SensitivityAnalyzer sa = null;
    boolean useANNRegression = false;

    NormalizationMethod parameterNormalizationMethod = Interpolation.NormalizationMethod.Linear;
    NormalizationMethod objectiveNormalizationMethod = Interpolation.NormalizationMethod.Linear;

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
            case ElementaryEffects: sa = new optas.SA.ElementaryEffects(); break;
            case ElementaryEffectsNonAbs: sa = new optas.SA.ElementaryEffects(ElementaryEffects.Measure.NonAbsolute); break;
            case ElementaryEffectsVariance: sa = new optas.SA.ElementaryEffects(ElementaryEffects.Measure.Variance); break;
            case FOSI1: sa = new optas.SA.FAST(optas.SA.FAST.Measure.FirstOrder); break;
            case FOSI2: sa = new optas.SA.VarianceBasedSensitivityIndex(Measure.FirstOrder); break;
            case TOSI: sa = new optas.SA.VarianceBasedSensitivityIndex(Measure.Total); break;
            case Interaction: sa = new optas.SA.VarianceBasedSensitivityIndex(Measure.Interaction); break;
            case LinearRegression: sa = new optas.SA.LinearRegression(); break;
        }
    }

    public boolean isUseANNRegression(){
        return this.useANNRegression;
    }
    public void setUseANNRegression(boolean flag){
        this.useANNRegression = flag;
    }

    public void setup(SimpleEnsemble xData[], EfficiencyEnsemble yData){
        sa.setInterpolation(useANNRegression);
        if (useANNRegression){
            sa.setInterpolationMethod(new NeuralNetwork());
            sa.getInterpolationMethod().setxNormalizationMethod(this.parameterNormalizationMethod);
            sa.getInterpolationMethod().setyNormalizationMethod(objectiveNormalizationMethod);
        }
        n = xData.length;
        sa.setData(xData, yData);
        sa.setSampleSize(this.sampleCount);
        sa.init();
    }

    public double[] getInteraction(Set<Integer> indexSet){
        if (sa instanceof VarianceBasedSensitivityIndex){
            VarianceBasedSensitivityIndex v = (VarianceBasedSensitivityIndex)sa;
            return v.calcSensitivity(indexSet);
        }
        return null;
    }

    public double[][] getSensitivity(){
        double result[][] = new double[n][3];
        for (int i=0;i<n;i++){
            double s = sa.getSensitivity(i);
            //double v = sa.getVariance(i);
            result[i][0] = s;//s-v;
            result[i][1] = s;
            result[i][2] = s;//s+v;
        }
        return result;
    }

    public double calculateError() throws NoDataException {
        return sa.getCVError();
    }
}
