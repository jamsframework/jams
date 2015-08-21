/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import optas.data.SimpleEnsemble;
import optas.tools.ObservableProgress;

/**
 *
 * @author chris
 */
public abstract class Interpolation extends ObservableProgress{

    public enum ErrorMethod{ABSE, RMSE,E2};

    protected SimpleEnsemble x[];
    double xRange[];
    double xMin[];

    protected int L=0;
    protected boolean initSuccessful = false;


    protected abstract double[] normalizeX(double u[]);
    protected abstract double[] normalizeY(double y[]);
        
    protected abstract double[] denormalizeX(double u[]);
    protected abstract double[] denormalizeY(double u[]);

    protected abstract int getN();
    protected abstract int getM();

    public abstract double getYData(int id, int index);

    public double init(){return 0;}
    
    protected double[] getX(int id){
        double row[] = new double[getN()];
        for (int i=0;i<getN();i++){
            row[i] = x[i].getValue(id);
        }
        return row;
    }

    abstract protected double[][] getInterpolatedValue(TreeSet<Integer> leaveOut);
    public abstract double[] getInterpolatedValue(double u[]);

    private double[] calcDifference(ErrorMethod e, double sim[][], double obs[][]){
        log("Calculating "+e+" Interpolation Error");
        int K = sim[0].length;
        double error[] = new double[getM()];
        switch (e){
            case ABSE:
                for (int j=0;j<getM();j++){
                    for (int i=0;i<K;i++){
                        error[j] += (Math.abs(sim[j][i] - obs[j][i]) / K);
                    }
                }                
            case RMSE:
                for (int j=0;j<getM();j++){
                    for (int i=0;i<K;i++){
                        error[j] += (sim[j][i] - obs[j][i])*(sim[j][i] - obs[j][i]);
                    }
                    error[j] = Math.sqrt(error[j]/K);
                }
            case E2:                
                for (int j=0;j<getM();j++){
                    double aobs = 0;
                    
                    for (int i=0;i<K;i++){
                        aobs += obs[j][i];
                    }

                    aobs /= K;

                    double numerator = 0;
                    double denumerator = 0;
                    for (int i=0;i<K;i++){
                        numerator += (sim[j][i] - obs[j][i])*(sim[j][i] - obs[j][i]);
                        denumerator += (obs[j][i] - aobs)*(obs[j][i] - aobs);
                    }
                    if (numerator != Double.NaN && denumerator != 0 && denumerator != Double.NaN)
                        error[j] = 1.0 - (numerator / denumerator);
                }
                return error;
        }
        return null;
    }
            
    protected void calculate(){
        
    }

    public double[] estimateCrossValidationError(int K, ErrorMethod e){
        log("Estimating Cross Validation Error");
        this.setProgress(0);
        double obs[][] = new double[getM()][L];
        double sim[][] = new double[getM()][L];

        this.calculate();
        
        for (int k=0;k<K;k++){
            log("Cross Validation " + k + " of " + K);

            int indexStart = k*(L/K);
            int indexEnd   = Math.min((k+1)*(L/K),L);
            int size = indexEnd-indexStart;
            if (size == 0)
                continue;

            TreeSet<Integer> validationSet = new TreeSet<Integer>();
            double validation[][] = new double[size][getM()];

            for (int j=indexStart;j<indexEnd;j++){
                int id_loi = x[0].getId(j);
                validationSet.add(id_loi);
                for (int i=0;i<getM();i++)
                    validation[j-indexStart][i] = getYData(id_loi,i);
            }

            double y_star[][] = this.getInterpolatedValue(validationSet);

            for (int j=0;j<size;j++){
                //error += Math.abs(y_star[j] - validation[j]);
                for (int i=0;i<getM();i++){
                    obs[i][indexStart+j] = validation[j][i];
                    sim[i][indexStart+j] = y_star[j][i];
                }
            }
        }

        double NSE[] = calcDifference(e, sim, obs);
        double maxAbsErr=0;
        double maxRelErr=0;
        
        double mean_obs = 0;
        for (int j=0;j<L;j++){
            mean_obs += obs[0][1];
        }
        mean_obs /= L;
        
        for (int i=0;i<L;i++){
            maxAbsErr = Math.max(maxAbsErr,Math.abs(sim[0][i]-obs[0][i]));
            maxRelErr = Math.max(maxRelErr,Math.abs(sim[0][i]-obs[0][i])/mean_obs);            
        }
        
        System.out.println("Crossvalidation errors (only first criteria: ");
        System.out.println("NSE: " + NSE[0]);
        System.out.println("max abs. error: " + maxAbsErr);
        System.out.println("max rel. error: " + maxRelErr);
        
        return NSE;
    }

    public double[] estimateLOOError(ErrorMethod e){
        return estimateCrossValidationError(this.L,e);
    }    
}
