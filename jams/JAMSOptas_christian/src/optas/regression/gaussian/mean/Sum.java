/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.mean;

import Jama.Matrix;
import java.util.Arrays;

/**
 *
 * @author christian
 */
public class Sum extends MeanFunction{
    MeanFunction a;
    MeanFunction b;
    
    public Sum (MeanFunction a, MeanFunction b){
        this.a = a;
        this.b = b;
    }

    @Override
    public Matrix eval(double[] hyp, double[][] x) {
        int n1 = a.getNumberOfHyperparameters(x);
        int n2 = b.getNumberOfHyperparameters(x);
        
        if (hyp.length < n1+n2){
            System.out.println("Error expected number of hyperparameters was " + (n1+n2) + " but received " + hyp.length);
        }
        double hyp1[] = Arrays.copyOfRange(hyp, 0, n1);
        double hyp2[] = Arrays.copyOfRange(hyp, n1, n1+n2);
        
        return a.eval(hyp1, x).plus(b.eval(hyp2, x));
    }

    @Override
    public int getNumberOfHyperparameters(double[][] x) {
        return a.getNumberOfHyperparameters(x) + b.getNumberOfHyperparameters(x);
    }
    
    @Override
    public double[][] getHyperparameterRange(double[][] x) {
        int n = getNumberOfHyperparameters(x);
        int n1 = a.getNumberOfHyperparameters(x);
        int n2 = b.getNumberOfHyperparameters(x);
        
        double range[][] = new double[n][2];
        for (int i=0;i<n;i++){
            if (i < n1){
                range[i] = a.getHyperparameterRange(x)[i];
            }else{
                range[i] = b.getHyperparameterRange(x)[i-n1];
            }
        }
        return range;
    }
   
    @Override
    public String toString(){
        return a.toString() + " + " + b.toString();
    }
}
