/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.inf;

import Jama.Matrix;
import optas.regression.gaussian.HyperParameter;
import optas.regression.gaussian.cov.CovarianceFunction;
import optas.regression.gaussian.mean.MeanFunction;
import optas.regression.likelihood.LikelihoodFunction;

/**
 *
 * @author christian
 */
public interface Inference {
    public void inference(HyperParameter hyp, MeanFunction mean, CovarianceFunction cov, LikelihoodFunction lik,double x[][], double y[], double xs[][]);
    
    public Matrix getMu();
    public Matrix getSigma2();
    public double getNLZ();
}
