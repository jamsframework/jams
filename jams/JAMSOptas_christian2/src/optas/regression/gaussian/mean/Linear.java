/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.mean;

import Jama.Matrix;

/**
 *
 * @author christian
 */
public class Linear extends MeanFunction{
    public Matrix eval(double hyp[], double x[][]){
        if (x == null || x.length == 0){
            System.out.println("Error empty training set provided!");
            return null;
        }
        if (hyp.length < x[0].length){
            System.out.println("Exactly n hyperparameters needed.");
            return null;
        }
        int D = x.length;
        int n = x[0].length;
        
        Matrix A = new Matrix(D,1);
        for (int i=0;i<D;i++){
            double sum = 0;
            for (int j=0;j<n;j++){
                sum += hyp[j]*x[i][j];
            }
            A.set(i, 0, sum);
        }
        return A;
    }

    @Override
    public int getNumberOfHyperparameters(double[][] x) {
        if (x == null || x.length == 0){
            System.out.println("Error empty training set provided!");
            return 0;
        }
        return x[0].length;
    }

    @Override
    public double[][] getHyperparameterRange(double[][] x) {
        int n = getNumberOfHyperparameters(x);
        double range[][] = new double[n][2];
        for (int i=0;i<n;i++){
            range[i][0] = -10;
            range[i][1] = 10;
        }
        return range;
    }
    
    @Override
    public String toString(){
        return "Linear";
    }
}
