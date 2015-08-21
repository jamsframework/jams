/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression.gaussian.inf;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import optas.regression.GaussianProcessRegression;
import optas.regression.gaussian.HyperParameter;
import optas.regression.gaussian.cov.CovarianceFunction;
import optas.regression.gaussian.cov.covSEiso;
import optas.regression.gaussian.mean.Constant;
import optas.regression.gaussian.mean.Linear;
import optas.regression.gaussian.mean.MeanFunction;
import optas.regression.gaussian.mean.Sum;
import optas.regression.likelihood.Gaussian;
import optas.regression.likelihood.LikelihoodFunction;

/**
 *
 * @author christian
 */
public class infExact implements Inference{

    Matrix alpha = null;
    Matrix sw = null;
    Matrix L = null;
    
    Matrix lp = null;
    Matrix ymu = null;
    Matrix ys2 = null;
        
    double nlZ;
    
    @Override
    public void inference(HyperParameter hyp, MeanFunction mean, CovarianceFunction cov, LikelihoodFunction lik, double[][] x, double[] y, double[][] xs) {
        boolean debug = false;
        if (!(lik instanceof optas.regression.likelihood.Gaussian)){
            System.out.println("Exact inference only possible with Gaussian likelihood");
            return;
        }
        
        if (x.length == 0){
            System.out.println("Empty training dataset provided");
            return;
        }
        
        int n = x[0].length; //num of parameters
        int D = x.length; //num of samples
        
        //K = feval(cov{:}, hyp.cov, x);                      % evaluate covariance matrix
        Matrix K = cov.eval(hyp.cov, x);
        
        if (debug) {
            System.out.println("**********K**************");
            for (int i = 0; i < K.getRowDimension(); i++) {
                for (int j = 0; j < K.getColumnDimension(); j++) {
                    System.out.print(K.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }        
        //m = feval(mean{:}, hyp.mean, x);                          % evaluate mean vector
        Matrix m = mean.eval(hyp.mean, x);
        
        if (debug) {
            System.out.println("**********m**************");
            for (int i = 0; i < m.getRowDimension(); i++) {
                for (int j = 0; j < m.getColumnDimension(); j++) {
                    System.out.print(m.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }
        //sn2 = exp(2*hyp.lik);                               % noise variance of likGauss
        double sn2 = Math.exp(2.*hyp.lik[0]);

        //L = chol(K/sn2+eye(n));               % Cholesky factor of covariance with noise
        for (int i=0;i<D;i++){
            for (int j=0;j<D;j++){
                if (i==j){
                    K.set(i, j, K.get(i, j) / sn2 + 1.0);
                }else{
                    K.set(i, j, K.get(i, j) / sn2);
                }
            }
        }
        
        CholeskyDecomposition chol = K.chol();
        Matrix y_minus_m = new Matrix(D,1);
        for (int i=0;i<D;i++){
            y_minus_m.set(i, 0, y[i] - m.get(i, 0));
        }
        //alpha = solve_chol(L,y-m)/sn2;
        alpha = chol.solve(y_minus_m);
        L = chol.getL();
        for (int i=0;i<D;i++){
            alpha.set(i, 0, alpha.get(i,0)/sn2);
        }
        
        sw = new Matrix(D, 1);
        for (int i=0;i<D;i++){
            sw.set(i,0,1.0 / Math.sqrt(sn2));
        }
        
        if (debug) {
            System.out.println("**********alpha**************");
            for (int i = 0; i < alpha.getRowDimension(); i++) {
                for (int j = 0; j < alpha.getColumnDimension(); j++) {
                    System.out.print(alpha.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }
        // nlZ = (y-m)'*alpha/2 + sum(log(diag(L))) + n*log(2*pi*sn2)/2;  % -log marg lik
        nlZ = 0;
        for (int i=0;i<D;i++){
            nlZ += y_minus_m.get(i, 0)*alpha.get(i, 0)/2.0;
        }
        for (int i=0;i<D;i++){
            nlZ += Math.log(L.get(i, i));
        }
        nlZ += D*Math.log(2.0*Math.PI*sn2)/2.0;
        
        System.out.println("nlZ is " + nlZ);
        
        if (xs == null){
            return;
        }
        
        int D2 = xs.length;
        //kss = feval(cov{:}, hyp.cov, xs(id,:), 'diag');              % self-variance
        Matrix kss = cov.selfVariance(hyp.cov, xs);
        
        if (debug) {
            System.out.println("**********kss**************");
            for (int i = 0; i < kss.getRowDimension(); i++) {
                for (int j = 0; j < kss.getColumnDimension(); j++) {
                    System.out.print(kss.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }
        //Ks  = feval(cov{:}, hyp.cov, x(nz,:), xs(id,:));         % cross-covariances
        Matrix Ks = cov.crossVariance(hyp.cov, x, xs);
        
        if (debug) {
            System.out.println("**********ks**************");
            for (int i = 0; i < Ks.getRowDimension(); i++) {
                for (int j = 0; j < Ks.getColumnDimension(); j++) {
                    System.out.print(Ks.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }
        //ms = feval(mean{:}, hyp.mean, xs(id,:));
        Matrix ms = mean.eval(hyp.mean, xs);
        //fmu(id) = ms + Ks'*full(alpha(nz));                       % predictive means
        Matrix fmu = ms.plus(Ks.transpose().times(alpha));
        if (debug) {
            System.out.println("**********fmu**************");
            for (int i = 0; i < fmu.getRowDimension(); i++) {
                for (int j = 0; j < fmu.getColumnDimension(); j++) {
                    System.out.print(fmu.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }
        
        //if Ltril           % L is triangular => use Cholesky parameters (alpha,sW,L)
        boolean upperTriangularMatrix = true;
        for (int i=0;i<L.getColumnDimension();i++){
            for (int j=0;j<L.getRowDimension();j++){
                if (i<j && L.get(i, j)!=0){
                    upperTriangularMatrix = false;
                    break;
                }
            }
        }
        Matrix fs2 = new Matrix(1,D2);
        if (upperTriangularMatrix) {
            //V  = L'\(repmat(sW,1,length(id)).*Ks);
            Matrix tmp = Ks.copy();
            for (int i = 0; i < tmp.getRowDimension(); i++) {
                for (int j = 0; j < tmp.getColumnDimension(); j++) {
                    tmp.set(i, j, Ks.get(i,j) * sw.get(i, 0)); //not exaclty sure about that 
                }
            }
            if (debug) {
                System.out.println("**********L**************");
                for (int i = 0; i < L.getRowDimension(); i++) {
                    for (int j = 0; j < L.getColumnDimension(); j++) {
                        System.out.print(L.get(i, j) + "\t");
                    }
                    System.out.println("");
                }                
            }
            // V  = L'\(repmat(sW,1,length(id)).*Ks);
            Matrix V = L.inverse().times(tmp);   //skip transpose here as L is allready transposed
            if (debug) {
                System.out.println("**********V**************");
                for (int i = 0; i < V.getRowDimension(); i++) {
                    for (int j = 0; j < V.getColumnDimension(); j++) {
                        System.out.print(V.get(i, j) + "\t");
                    }
                    System.out.println("");
                }                
            }
            //fs2(id) = kss - sum(V.*V,1)';                       % predictive variances            
            for (int i=0;i<D2;i++){
                double sum = 0;
                for (int j=0;j<D;j++){
                    sum += V.get(j, i)*V.get(j, i);
                }
                //fs2(id) = max(fs2(id),0);   % remove numerical noise i.e. negative variances
                fs2.set(0, i, Math.max(kss.get(i, 0) - sum,0));
            }
            if (debug) {
                System.out.println("**********fs2**************");
                for (int i = 0; i < fs2.getRowDimension(); i++) {
                    for (int j = 0; j < fs2.getColumnDimension(); j++) {
                        System.out.print(fs2.get(i, j) + "\t");
                    }
                    System.out.println("");
                }                
            }
        }else{ //don't know in which situation this can happen
            //fs2(id) = kss + sum(Ks.*(L*Ks),1)';                 % predictive variances
            Matrix tmp = Ks.times(L.times(Ks));
            for (int i=0;i<D2;i++){
                double sum = 0;
                for (int j=0;j<D;j++){
                    sum += tmp.get(j, i)*tmp.get(j, i);
                }
                //fs2(id) = max(fs2(id),0);   % remove numerical noise i.e. negative variances
                fs2.set(0, i, Math.max(0,kss.get(i, 0) + sum));
            }
        }
        
        
        //[lp(id) ymu(id) ys2(id)] = lik(hyp.lik, [], fmu(id), fs2(id));
        Matrix result[] = lik.calc(hyp.lik, null, fmu.getColumnPackedCopy(), fs2.getColumnPackedCopy());
        lp = result[0];
        ymu = result[1];
        ys2 = result[2];
        if (debug) {
            System.out.println("**********lp**************");
            for (int i = 0; i < lp.getRowDimension(); i++) {
                for (int j = 0; j < lp.getColumnDimension(); j++) {
                    System.out.print(lp.get(i, j) + "\t");
                }
                System.out.println("");
            }
        }

        return;
    }
    
    public double getNLZ(){
        return nlZ;
    }
    
    public static void main(String[] args) {
        double x[][] = GaussianProcessRegression.generateRandomX();
        double y[] = GaussianProcessRegression.generateRandomY();
        
        HyperParameter hyp = new HyperParameter();
        hyp.cov = new double[]{Math.log(0.25), Math.log(1.0)};
        hyp.mean = new double[]{0.5,1.0};
        hyp.lik = new double[]{Math.log(0.1)};
        MeanFunction mean = new Sum(new Linear(), new Constant());
        infExact inf = new infExact();
        
        CovarianceFunction cov = new covSEiso();
        LikelihoodFunction lik = new Gaussian();
        double z[][] = new double[101][1];
        for (int i=0;i<101;i++){
            z[i][0] = -1.9 + (i/100.0)*3.8;
        }
        inf.inference(hyp, mean , cov, lik, x, y, z);
    }

    @Override
    public Matrix getMu() {
        return this.ymu;
    }

    @Override
    public Matrix getSigma2() {
        return this.ys2;
    }
}
