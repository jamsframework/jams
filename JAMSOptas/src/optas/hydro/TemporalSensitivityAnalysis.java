/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro;

import java.util.Arrays;
import java.util.Observable;
import optas.SA.UniversalSensitivityAnalyzer;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Measurement;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerieEnsemble;
import optas.regression.Interpolation.NormalizationMethod;

/**
 *
 * @author chris
 */
public class TemporalSensitivityAnalysis extends Observable{
    SimpleEnsemble parameter[];
    EfficiencyEnsemble o;
    TimeSerieEnsemble ts;
    Measurement obs;

    double currentProgress = 0;
    
    double temporalSensitivityIndex[][]=null;

    int T = 0;

    boolean isValid = false;

    public TemporalSensitivityAnalysis(SimpleEnsemble parameter[], EfficiencyEnsemble o, TimeSerieEnsemble ts, Measurement obs ){
        this.parameter = parameter;
        this.o = o;
        this.ts = ts;
        this.obs = obs;
        T = obs.getTimesteps();
        isValid = false;
    }

    public double[][] calculate(){
        SimpleEnsemble parameterCut[] = new SimpleEnsemble[parameter.length];
        EfficiencyEnsemble oCut;
        TimeSerieEnsemble tsCut;

        Integer[] ids = o.sort();

        Integer[] new_ids = Arrays.copyOfRange(ids, 0, (int)(0.95*ids.length));

        //exclude the worst candidates
        for (int i=0;i<parameter.length;i++){
            parameterCut[i] = ((SimpleEnsemble)(parameter[i].clone()));
            parameterCut[i].retainIds(new_ids);
        }
        oCut = (EfficiencyEnsemble)o.clone();
        oCut.retainIds(new_ids);

        tsCut = (TimeSerieEnsemble)ts.clone();
        tsCut.retainIds(new_ids);
        
        return (temporalSensitivityIndex = calcTemporalSensitivity(parameterCut, oCut, tsCut));
    }

    private double[][] calcTemporalSensitivity(SimpleEnsemble parameter[], EfficiencyEnsemble o, TimeSerieEnsemble ts ){
        this.notifyObservers("Calculating Temporal Sensitivity Index");
        if (isValid)
            return temporalSensitivityIndex;

        currentProgress = 0;
        
        UniversalSensitivityAnalyzer SA = new UniversalSensitivityAnalyzer();
        SA.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
        SA.setObjectiveNormalizationMethod(NormalizationMethod.Linear);
        SA.setParameterNormalizationMethod(NormalizationMethod.Linear);
        SA.setSampleCount(2000);
        SA.setUseANNRegression(false);

        int n = parameter.length;
        double sensitivity[][] = new double[n][T];

        for (int i=0;i<T;i++){
            double observationT = obs.getValue(i);
            SimpleEnsemble s = ts.get(i);
            s.calcPlus(-observationT);
            s.calcAbs();

            EfficiencyEnsemble e = new EfficiencyEnsemble(s, false);

            SA.setup(parameter, e);
            double result[][] = SA.getSensitivity();
            double sum = 0;
            for (int j=0;j<n;j++){
                sensitivity[j][i] = result[j][1];
                sum += result[j][1];
            }
            for (int j=0;j<n;j++){
                sensitivity[j][i] /= sum;
            }
            currentProgress = (double)i / (double)T;
            this.notifyObservers();
        }
        isValid = true;
        return sensitivity;
    }
}
