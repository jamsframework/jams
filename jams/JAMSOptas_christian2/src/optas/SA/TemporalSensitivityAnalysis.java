/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Arrays;
import java.util.TreeSet;
import optas.data.api.DataView;
import optas.data.ensemble.DefaultEfficiencyEnsemble;
import optas.data.time.MeasuredTimeSerie;
import optas.data.ensemble.DefaultSimpleEnsemble;
import optas.data.ensemble.DefaultTimeSerieEnsemble;
import optas.data.ensemble.api.SimpleEnsemble;
import optas.data.view.ViewFactory;
import optas.regression.SimpleInterpolation.NormalizationMethod;
import optas.tools.ObservableProgress;


/**
 *
 * @author chris
 */
public class TemporalSensitivityAnalysis extends ObservableProgress{
    SimpleEnsemble parameter[];
    DefaultEfficiencyEnsemble o;
    DefaultTimeSerieEnsemble ts;
    MeasuredTimeSerie obs;
        
    double temporalSensitivityIndex[][]=null;

    int T = 0;
    int size = 0;
    int n = 0;
    int windowSize = 1;
    
    double range[][] = null;

    boolean isInit = false;
    boolean isCalculated = false;

    public TemporalSensitivityAnalysis(SimpleEnsemble parameter[], DefaultEfficiencyEnsemble o, DefaultTimeSerieEnsemble ts, MeasuredTimeSerie obs ){
        this.parameter = parameter;
        this.o = o;
        this.ts = ts;
        this.obs = obs;
        T = obs.getNumberOfTimesteps();

        n = parameter.length;
        if (n==0){
            return;
        }

        size = parameter[0].getSize();

        for (int i=0;i<n;i++){
            if (parameter[i].getSize()!=size)
                return;
        }
        if (ts.getSize()!=size)
            return;

        range = this.getParameterRange();
        isInit = false;
        isCalculated = false;
    }

    protected double[] getLowBound(){
        double lb[] = new double[n];
        for (int j = 0; j < n; j++) {
            lb[j] = parameter[j].getMin();
        }
        return lb;
    }

    protected double[] getUpBound(){
        double ub[] = new double[n];
        for (int j = 0; j < n; j++) {
            ub[j] = parameter[j].getMax();
        }
        return ub;
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

    final protected double[][] getParameterRange() {
        if (range == null){
            range = new double[n][2];

            for (int j = 0; j < n; j++) {
                range[j][0] = parameter[j].getMin();
                range[j][1] = parameter[j].getMax();
            }
        }
        return range;
    }
    
    //calculates weighting for each parameter and timestep
    public double[][] calculate(){
        DefaultSimpleEnsemble parameterCut[] = new DefaultSimpleEnsemble[parameter.length];
        DefaultEfficiencyEnsemble oCut;
        DefaultTimeSerieEnsemble tsCut;

        Integer[] ids = o.sort();
//filter?!
        DataView<Integer> new_ids = ViewFactory.createView(Arrays.copyOfRange(ids, 0, (int)(0.95*ids.length)));

        Integer[] idsO = ViewFactory.toArray(o.ids()),
                  idsP = ViewFactory.toArray(parameter[0].ids()),
                  idsTS= ViewFactory.toArray(ts.ids());

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
            parameterCut[i] = ((DefaultSimpleEnsemble)(parameter[i].clone()));
            parameterCut[i].filter(new_ids);
        }
        oCut = (DefaultEfficiencyEnsemble)o.clone();
        oCut.filter(new_ids);

        tsCut = (DefaultTimeSerieEnsemble)ts.clone();
        tsCut.filter(new_ids);
        
        return (temporalSensitivityIndex = calcTemporalSensitivity(parameterCut, tsCut));
    }

    private double[][] calcTemporalSensitivity(DefaultSimpleEnsemble parameter[], DefaultTimeSerieEnsemble ts ){
        log("Calculating Temporal Sensitivity Index");
        if (isCalculated)
            return temporalSensitivityIndex;

        setProgress(0.0);
        
        UniversalSensitivityAnalyzer SA = new UniversalSensitivityAnalyzer();
        SA.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
        SA.setObjectiveNormalizationMethod(NormalizationMethod.Linear);
        SA.setParameterNormalizationMethod(NormalizationMethod.Linear);
        SA.setSampleCount(2000);
        SA.setUsingRegression(false);
        
        double sensitivity[][] = new double[n][T];

        for (int i=0;i<T;i++){
            DefaultSimpleEnsemble sumOfWindow = null;
            
            for (int j=i-windowSize;j<i+windowSize;j++){
                int k=Math.min(Math.max(0, j), T-1);
                
                DefaultEnsemble s = ts.getEnsemble(k);
                double observationT = obs.getValue(k);                
                s.calcPlus(-observationT);
                s.calcAbs();
                if (sumOfWindow == null){
                    sumOfWindow = s;                 
                }else{
                    sumOfWindow.calcPlus(s);
                }
            }
            
            DefaultEfficiencyEnsemble e = new DefaultEfficiencyEnsemble(sumOfWindow, false);

            SA.setup(parameter, e);
            double result[] = SA.getSensitivity();
            double sum = 0;
            for (int j=0;j<n;j++){
                sensitivity[j][i] = result[j];
                sum += result[j];
            }
            for (int j=0;j<n;j++){
                sensitivity[j][i] /= sum;
            }
            setProgress((double)i / (double)T);
        }
        isCalculated = true;
        return sensitivity;
    }
}
