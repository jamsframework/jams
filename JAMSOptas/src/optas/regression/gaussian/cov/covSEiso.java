/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.cov;

import Jama.Matrix;
import optas.regression.GaussianProcessRegression;

/**
 *
 * @author christian
 */
public class covSEiso extends CovarianceFunction{
    
    public Matrix selfVariance(double hyp[], double x[][]){
        int n = x[0].length; //num of parameters
        int D = x.length; //num of samples
        
        double ell = Math.exp(hyp[0]);  //                               % characteristic length scale
        double sf2 = Math.exp(2.*hyp[1]);//     
        
        Matrix K = new Matrix(D,1);
        
        for (int i=0;i<K.getColumnDimension();i++){
            for (int j=0;j<K.getRowDimension();j++){
                double r = sf2*Math.exp(K.get(j, i) /2.0);
                K.set(j, i, r);
            }            
        }   
        
        return K;
    }
    
    public Matrix crossVariance(double hyp[], double x[][], double xs[][]){
        int n = x[0].length; //num of parameters
        int D1 = x.length; //num of training samples
        int D2 = xs.length; //num of validation samples
        
        if (x[0].length != xs[0].length){
            System.out.println("'Error: column lengths must agree.'");
            return null;
        }
        
        double ell = Math.exp(hyp[0]);  //                               % characteristic length scale
        double sf2 = Math.exp(2.*hyp[1]);//                                           % signal variance

        double x_copy[][] = new double[D1][n];
        double xs_copy[][] = new double[D2][n];
        
        for (int i=0;i<n;i++){
            for (int j=0;j<D1;j++){
                x_copy[j][i] = x[j][i] / ell;
            }
            for (int j=0;j<D2;j++){
                xs_copy[j][i] = xs[j][i] / ell;
            }
        }
        
        double mean[] = new double[n];
        for (int i=0;i<n;i++){
            for (int j=0;j<D1;j++){
                mean[i] += x_copy[j][i];
            }
            for (int j=0;j<D2;j++){
                mean[i] += xs_copy[j][i];
            }
            mean[i] /= (double)(D1+D2);            
        }
        
        for (int i=0;i<n;i++){
            for (int j=0;j<D1;j++){
                x_copy[j][i] -= mean[i];
            }
            for (int j=0;j<D2;j++){
                xs_copy[j][i] -= mean[i];
            }
        }
        
        Matrix C = new Matrix(D1,D2);
        for (int i=0;i<D1;i++){
            for (int j=0;j<D2;j++){
                double sum = 0;
                for (int k=0;k<n;k++){
                    sum += (x_copy[i][k])*(x_copy[i][k]) + (xs_copy[j][k])*(xs_copy[j][k]) - 2.0*(x_copy[i][k])*(xs_copy[j][k]);                    
                }
                double r = sf2*Math.exp(-sum/2.0);
                C.set(i, j, r);                
            }            
        }        
        return C;
    }
    
    @Override
    public Matrix eval(double hyp[], double x[][]){
        int n = x[0].length; //num of parameters
        int D = x.length; //num of samples
        
        double ell = Math.exp(hyp[0]);  //                               % characteristic length scale
        double sf2 = Math.exp(2.*hyp[1]);//                                           % signal variance

        double x_copy[][] = new double[D][n];
        for (int i=0;i<n;i++){
            for (int j=0;j<D;j++){
                x_copy[j][i] = x[j][i] / ell;
            }
        }
        
        double mean[] = new double[n];
        for (int i=0;i<n;i++){
            for (int j=0;j<D;j++){
                mean[i] += x_copy[j][i];
            }
            mean[i] /= (double)D;            
        }
        
        for (int i=0;i<n;i++){
            for (int j=0;j<D;j++){
                x_copy[j][i] -= mean[i];
            }
        }
        
        Matrix C = new Matrix(D,D);
        for (int i=0;i<D;i++){
            for (int j=i;j<D;j++){
                double sum = 0;
                for (int k=0;k<n;k++){
                    sum += (x_copy[i][k])*(x_copy[i][k]) + (x_copy[j][k])*(x_copy[j][k]) - 2.0*(x_copy[i][k])*(x_copy[j][k]);                    
                }
                double r = sf2*Math.exp(-sum/2.0);
                C.set(i, j, r);
                C.set(j, i, r);
            }            
        }        
        return C;
    }
    
    public static void main(String[] args) {
        double x[][] = GaussianProcessRegression.generateRandomX();
       
        double hyp[] = {Math.log(0.25), Math.log(1.0)};
        
        covSEiso test = new covSEiso();
        Matrix C = test.eval(hyp, x);
        
        for (int i=0;i<C.getRowDimension();i++){
            for (int j=0;j<C.getRowDimension();j++){
                System.out.print(C.get(i, j) + "\t");
            }
            System.out.println("");
        }
            
    }

    @Override
    public int getNumberOfHyperparameters(double[][] x) {
        return 2;
    }
    
    @Override
    public double[][] getHyperparameterRange(double[][] x) {
        return new double[][]{
            {-4,4},
            {-4,4}
        };   
    }
    @Override
    public String toString(){
        return "covSEiso";
    }
    
}
