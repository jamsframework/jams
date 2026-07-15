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
public abstract class MeanFunction {
    public static MeanFunction[] getMeanFunctions(){
        return new MeanFunction[]{
            new Constant(),
            new Linear(),
            new Sum(new Linear(),new Constant())
        };
    }
    abstract public Matrix eval(double hyp[], double[][] x);
    abstract public int getNumberOfHyperparameters(double x[][]);
    abstract public double[][] getHyperparameterRange(double x[][]);
}
