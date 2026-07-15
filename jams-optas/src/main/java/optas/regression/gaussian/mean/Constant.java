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
public class Constant extends MeanFunction{
    public Matrix eval(double hyp[], double x[][]){
        if (x == null || x.length == 0){
            System.out.println("Error empty training set provided!");
            return null;
        }
        if (hyp.length < 1){
            System.out.println("Exactly 1 hyperparameters needed.");
            return null;
        }
        int D = x.length;
        
        Matrix A = new Matrix(D,1);
        for (int i=0;i<D;i++){            
            A.set(i, 0, hyp[0]);
        }
        return A;
    }

    @Override
    public int getNumberOfHyperparameters(double[][] x) {
        return 1;
    }
    
    @Override
    public double[][] getHyperparameterRange(double[][] x) {
        return new double[][]{
            {-100,100}
        };
    }
    
    @Override
    public String toString(){
        return "Constant";
    }
}
