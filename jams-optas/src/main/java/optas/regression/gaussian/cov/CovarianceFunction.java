/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.cov;

import Jama.Matrix;

/**
 *
 * @author christian
 */
public abstract class CovarianceFunction {    
    public static CovarianceFunction[] getCovFunctions(){
        return new CovarianceFunction[]{
            new covSEiso(),
            new covSEard()
        };
    }
    public abstract Matrix eval(double hyp[], double x[][]);
    public abstract Matrix selfVariance(double hyp[], double x[][]);
    public abstract Matrix crossVariance(double hyp[], double x[][], double xs[][]);
    
    public abstract double[][] getHyperparameterRange(double x[][]);
    public abstract int getNumberOfHyperparameters(double[][] x);
}
