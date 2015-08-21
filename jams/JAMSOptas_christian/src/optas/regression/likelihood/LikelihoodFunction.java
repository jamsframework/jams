/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.likelihood;

import Jama.Matrix;

/**
 *
 * @author christian
 */
public abstract class LikelihoodFunction {
    public static LikelihoodFunction[] getLikelihoodFunctions(){
        return new LikelihoodFunction[]{
          new Gaussian()  
        };
    }
    public abstract Matrix[] calc(double[] hyp, double[] y, double[] mu, double[] s2);
    public abstract double[][] getHyperparameterRange();
    public abstract int getNumberOfHyperparameters();
}
