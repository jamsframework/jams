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
public class Gaussian extends LikelihoodFunction{

    @Override
    public double[][] getHyperparameterRange() {
        return new double[][]{
            {-4,4}
        };
    }

    @Override
    public int getNumberOfHyperparameters() {
        return 1;
    }
    enum Mode{infLaplace, infEP, infVB };
    
    Mode mode = Mode.infEP;
    
    public Matrix calc(double[] hyp, double[] y, double[] mu, double[] s2, Mode mode) {
        double sn2 = Math.exp(2.*hyp[0]);
        
        switch (mode) {
            case infLaplace:
                return null;
            case infVB:
                return null;
            case infEP: {
                int D2 = y.length;
                //lZ = -(y-mu).^2./(sn2+s2)/2 - log(2*pi*(sn2+s2))/2;
                Matrix lZ = new Matrix(D2, 1);
                for (int i = 0; i < D2; i++) {
                    double value = -(y[i] - mu[i]) * (y[i] - mu[i]) / (sn2 + s2[i]) / 2.0 - Math.log(2.0 * Math.PI * (sn2 + s2[i])) / 2.0;
                    lZ.set(i, 0, value);
                }
                return lZ;
            }
        }
        return null;
    }
    @Override
    public Matrix[] calc(double[] hyp, double[] y, double[] mu, double[] s2) {
        double sn2 = Math.exp(2.*hyp[0]);
        
        boolean s2zero = false;
        
        if (y==null){
            y = new double[mu.length];
        }
        if (s2zero){
            //lp = -(y-mu).^2./sn2/2-log(2*pi*sn2)/2; s2 = 0;
            return null;
        }else{
            Matrix lp = calc(hyp, y, mu, s2, Mode.infEP);
            
            Matrix ymu = new Matrix(mu, 1);
            Matrix ys2 = new Matrix(s2, 1);
            
            for (int i=0;i<s2.length;i++){
                ys2.set(0, i, s2[i]+sn2);
            }
            
            return new Matrix[]{lp, ymu, ys2};
        }
        
    }
    
    @Override
    public String toString(){
        return "Gaussian";
    }
    
}
