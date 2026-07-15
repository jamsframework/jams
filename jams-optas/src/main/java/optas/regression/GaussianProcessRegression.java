/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.regression;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import optas.core.AbstractFunction;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.optimizer.SCE;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.SampleFactory.Sample;
import optas.regression.gaussian.HyperParameter;
import optas.regression.gaussian.cov.CovarianceFunction;
import optas.regression.gaussian.cov.covSEiso;
import optas.regression.gaussian.inf.Inference;
import optas.regression.gaussian.inf.infExact;
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
public class GaussianProcessRegression {    
    HyperParameter hyp = null;
    Inference inf = null;
    MeanFunction mean = null;
    CovarianceFunction cov = null;
    LikelihoodFunction lik = null;
                
    double nlZ = Double.NaN;
    
    double x[][];
    double y[];
    
    public GaussianProcessRegression(){
        
    }
    
    public void setMeanFunction(MeanFunction mean){
        this.mean = mean;
    }
    
    public void setCovFunction(CovarianceFunction cov){
        this.cov = cov;
    }
    
    public void setLikelihoodFunction(LikelihoodFunction lik){
        this.lik = lik;
    }
    
    public void setTrainingDataset(double x[][], double y[]){
        this.x = x;
        this.y = y;
    }
    public Matrix[] inference(double xs[][]){        
        if (mean == null){
            mean = new Constant();
        }
        if (cov == null){
            cov = new covSEiso();
        }
        if (lik == null){
            lik = new Gaussian();
        }
        
        if (inf == null){
            inf = new infExact();
        }
        if (hyp == null){
            hyp = new HyperParameter();
            hyp.cov = new double[cov.getNumberOfHyperparameters(x)];
            hyp.mean = new double[mean.getNumberOfHyperparameters(x)];
            hyp.lik = new double[lik.getNumberOfHyperparameters()];
        }
        inf.inference(hyp, mean, cov, lik, x, y, xs);

        if (inf instanceof infExact){
            nlZ = ((infExact)inf).getNLZ(); 
        }
        
        Matrix mu = inf.getMu();
        Matrix s2 = inf.getSigma2();
        
        return new Matrix[]{mu,s2};
    }
    
    public double getMarginalLikelihood(){
        return nlZ;
    }
    
    public void setHyperParameters(HyperParameter p){
        this.hyp = p;
    }
    
    public HyperParameter getHyperParameter(){
        return createHyperParameter();
    }
    
    private HyperParameter createHyperParameter(){
        if (cov == null || mean == null || lik == null){
            hyp = new HyperParameter();
            hyp.cov = new double[0];
            hyp.mean = new double[0];
            hyp.lik = new double[0];
            return hyp;
        }
        if (hyp == null){
            hyp = new HyperParameter();
            hyp.cov = new double[cov.getNumberOfHyperparameters(x)];
            hyp.mean = new double[mean.getNumberOfHyperparameters(x)];
            hyp.lik = new double[lik.getNumberOfHyperparameters()];
        }
        if (hyp.cov.length != cov.getNumberOfHyperparameters(x)){
            double tmp[] = hyp.cov;
            hyp.cov = new double[cov.getNumberOfHyperparameters(x)];
            System.arraycopy(tmp, 0, hyp.cov, 0, Math.min(tmp.length,hyp.cov.length));
        }
        if (hyp.mean.length != mean.getNumberOfHyperparameters(x)){
            double tmp[] = hyp.mean;
            hyp.mean = new double[mean.getNumberOfHyperparameters(x)];
            System.arraycopy(tmp, 0, hyp.mean, 0, Math.min(tmp.length,hyp.mean.length));
        }
        if (hyp.lik.length != lik.getNumberOfHyperparameters()){
            double tmp[] = hyp.lik;
            hyp.lik = new double[lik.getNumberOfHyperparameters()];
            System.arraycopy(tmp, 0, hyp.lik, 0, Math.min(tmp.length,hyp.lik.length));
        }
        
        return hyp;
    }
    
    public double[][] getX(){
        return x;
    }
    
    public void optimizeHyperParameters(){
        if (mean == null){
            mean = new Constant();
        }
        if (cov == null){
            cov = new covSEiso();
        }
        if (lik == null){
            lik = new Gaussian();
        }
        
        if (inf == null){
            inf = new infExact();
        }
        hyp = createHyperParameter();
                
        SCE sce = new SCE();

        class CalcInference extends AbstractFunction {

            @Override
            public void log(String str) {
                System.out.println(str);
            }
            

           
            @Override
            public double[] evaluate(double p[]) {
                HyperParameter hyp_new = new HyperParameter();
                int n1 = hyp.cov.length;
                int n2 = hyp.mean.length;
                int n3 = hyp.lik.length;
                
                hyp_new.cov = Arrays.copyOfRange(p,0,n1);
                hyp_new.mean = Arrays.copyOfRange(p,n1,n1+n2);
                hyp_new.lik = Arrays.copyOfRange(p,n1+n2,n1+n2+n3);
                
                
                inf.inference(hyp_new, mean, cov, lik, x, y, null);
                
                
                return new double[]{inf.getNLZ()};
            }

            @Override
            public int getInputDimension() {
                return hyp.cov.length + hyp.lik.length + hyp.mean.length;
            }

            @Override
            public int getOutputDimension() {
                return 1;
            }

            @Override
            public double[][] getRange() {
                double range1[][] = cov.getHyperparameterRange(x);
                double range2[][] = mean.getHyperparameterRange(x);
                double range3[][] = lik.getHyperparameterRange();
                int n1 = range1.length, 
                    n2 = range2.length,
                    n3 = range3.length;
                
                double range[][] = new double[n1+n2+n3][];
                System.arraycopy(range1, 0, range, 0, n1);
                System.arraycopy(range2, 0, range, n1, n2);
                System.arraycopy(range3, 0, range, n1+n2, n3);
                return range;
            }
        }
        
         sce.setFunction(new CalcInference());
         sce.complexesCount = 2;
         sce.pcento = 0.01;
         sce.kstop = 5;
         sce.peps = 0.01;
         sce.setMaxn(5000);
         sce.setVerbose(true);

         sce.init();
         try{
            sce.procedure();
         }catch(SampleLimitException sle){
             System.out.println("Finished because SampleLimit exceded!");
         }catch(ObjectiveAchievedException oae){
             //...
         }
         //sce.printSamples();
         
         ArrayList<SampleFactory.Sample> solution = sce.getSolution();
         for (int i=0;i<solution.size();i++){
             System.out.println(Arrays.toString(solution.get(i).x) + Arrays.toString(solution.get(i).F()));             
         }
         
         Sample bestHyperparameterSet = solution.get(0);
         
         int n1 = hyp.cov.length;
         int n2 = hyp.mean.length;
         int n3 = hyp.lik.length;
         
         System.arraycopy(bestHyperparameterSet.x, 0, hyp.cov, 0, n1);
         System.arraycopy(bestHyperparameterSet.x, n1, hyp.mean, 0, n2);
         System.arraycopy(bestHyperparameterSet.x, n1+n2, hyp.lik, 0, n3);
    }
    
    
    public static double[][] generateRandomX(){
        double x[][] = {
            {2.08397042775073},
            {-0.821018066101379},
            {-0.617870699182597},
            {-1.18382260886069},
            {0.274087442277144},
            {0.599441729295593},
            {1.76889791920444},
            {-0.465645549031928},
            {0.588852784375935},
            {-0.832982214438054},
            {-0.512106527960363},
            {0.277883144210116},
            {-0.0658704269222113},
            {-0.821412363806325},
            {0.185399443778088},
            {-0.858296174995998},
            {0.370786630037059},
            {-1.40986916241664},
            {-0.144668412325022},
            {-0.553299615220374}
        };
        
        return x;
    }
    
    public static double[] generateRandomY(){
        double y[] = {
            4.54920374633170,0.371985481182978,0.677971792971015,-0.0186319168311214,2.25620496141566,1.13328267008169,3.93058703841944,0.502497679698423,1.40834443035711,0.248578692641006,0.386250672994108,2.23007255363489,1.56331251927033,0.438357894231606,2.36941345230028,0.414327246799461,2.25668167641071,0.288359818253986,0.961386857721802,0.618695364405778
        };
        
        return y;
    }
    
    public static void main(String[] args) {
        
        GaussianProcessRegression gp = new GaussianProcessRegression();
                
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
        
        gp.cov = cov;
        gp.lik = lik;
        gp.inf = inf;
        gp.mean = mean;        
        gp.hyp = hyp;
        gp.x = x;
        gp.y = y;
        
        Matrix result[] = gp.inference(z);
        int n = 101;
        for (int i=0;i<101;i++){
            System.out.println("min: " + (result[0].get(0, i)-2.*result[1].get(0, i)) + "\t " + (result[0].get(0, i)+2.*result[1].get(0, i)));
        }
        
        gp.optimizeHyperParameters();
        
        result = gp.inference(z);
        for (int i=0;i<101;i++){
            System.out.println("min: " + (result[0].get(0, i)-2.*result[1].get(0, i)) + "\t " + (result[0].get(0, i)+2.*result[1].get(0, i)));
        }
    }
}
