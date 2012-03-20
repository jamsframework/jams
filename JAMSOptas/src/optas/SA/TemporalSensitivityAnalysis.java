/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Arrays;
import java.util.TreeSet;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Measurement;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerieEnsemble;
import optas.regression.Interpolation.NormalizationMethod;
import optas.tools.ObservableProgress;


/**
 *
 * @author chris
 */
public class TemporalSensitivityAnalysis extends ObservableProgress{
    SimpleEnsemble parameter[];
    EfficiencyEnsemble o;
    TimeSerieEnsemble ts;
    Measurement obs;
        
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

    //calculates weighting for each parameter and timestep
    public double[][] calculate(){
        SimpleEnsemble parameterCut[] = new SimpleEnsemble[parameter.length];
        EfficiencyEnsemble oCut;
        TimeSerieEnsemble tsCut;

        Integer[] ids = o.sort();

        Integer[] new_ids = Arrays.copyOfRange(ids, 0, (int)(0.95*ids.length));

        Integer[] idsO = o.getIds(),idsP = parameter[0].getIds(),idsTS=ts.getIds();

        TreeSet<Integer> setO = new TreeSet();
        setO.addAll(Arrays.asList(idsO));

        TreeSet<Integer> setP = new TreeSet();
        setP.addAll(Arrays.asList(idsO));

        TreeSet<Integer> setTS = new TreeSet();
        setTS.addAll(Arrays.asList(idsO));

        if (!setO.containsAll(setP)){
            setP.removeAll(setO);
            System.out.println("missing ids in O" + setP.toArray());
        }
        if (!setO.containsAll(setTS)){
            setTS.removeAll(setO);
            System.out.println("missing ids in O" + setTS.toArray());
        }
        if (!setP.containsAll(setO)){
            setO.removeAll(setP);
            System.out.println("missing ids in P" + setO.toArray());
        }
        if (!setP.containsAll(setTS)){
            setTS.removeAll(setP);
            System.out.println("missing ids in P" + setTS.toArray());
        }
        if (!setTS.containsAll(setO)){
            setO.removeAll(setTS);
            System.out.println("missing ids in TS" + setP.toArray());
        }
        if (!setTS.containsAll(setP)){
            setP.removeAll(setTS);
            System.out.println("missing ids in TS" + setP.toArray());
        }

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
        log("Calculating Temporal Sensitivity Index");
        if (isValid)
            return temporalSensitivityIndex;

        setProgress(0.0);
        
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
            setProgress((double)i / (double)T);
        }
        isValid = true;
        return sensitivity;
    }
}
