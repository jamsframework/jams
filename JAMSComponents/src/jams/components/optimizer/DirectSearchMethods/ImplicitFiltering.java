/*
 * SimplexGradient.java
 *
 * Created on 7. März 2008, 12:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer.DirectSearchMethods;

import jams.components.optimizer.DirectSearchMethods.*;
import java.util.Vector;
import Jama.*;
import jams.components.optimizer.DirectSearchMethods.PatternSearch;
import jams.components.optimizer.LinearConstraintDirectPatternSearch;
import jams.components.optimizer.Optimizer.AbstractFunction;
import jams.components.optimizer.Optimizer.Sample;
import jams.components.optimizer.Optimizer.SampleComperator;
import java.util.Random;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings("unchecked")
public class ImplicitFiltering extends PatternSearch{
        
    public Sample step(AbstractFunction f,Sample[] Simplex,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb,double lowBound[],double upBound[]){
        //1 compare worst
        //2 compare best
        //3 zufällige richtung
        int method = 3;        
                        
        if (Generator == null){
            Generator = new Random();
        }
        //sort simplex        
        java.util.Arrays.sort(Simplex,new SampleComperator(false));
        
        int ns = 0;
        int n = Simplex[0].x.length;
        
        double ScalingQueue[] = new double[10];//{1.0,1.0/2.0,1.0/4.0,1.0/8.0,1.0/16.0,1.0/32.0,1.0/64.0,1.0/128.0};
        for (int j=0;j<10;j++){
            ScalingQueue[j] = Math.pow(0.5,(double)j)*0.5;
        }                
        boolean improvement = false;
                
        Matrix V = new Matrix(n,2*n);
        V.setMatrix(0, n-1, 0,n-1,Matrix.identity(n,n));
        V.setMatrix(0, n-1, n,2*n-1,Matrix.identity(n,n).uminus());
                                
        LinearConstraintDirectPatternSearch LCDPS = new LinearConstraintDirectPatternSearch();
        if (LinearConstraintMatrixA != null & LinearConstraintVectorb != null)
            LCDPS.SetLinearConstraints(LinearConstraintMatrixA, LinearConstraintVectorb);
                        
        while(ns < ScalingQueue.length){
            if (method == 3){
                V = new Matrix(n,n);
                for (int i=0;i<n;i++){
                    Matrix D = new Matrix(n,1);
                    for (int j=0;j<n;j++){
                        D.set(j, 0, this.Generator.nextDouble()-0.5);
                    }
                    D = D.times(1.0/D.norm2());
                    V.setMatrix(0, n-1, i,i,D);            
                }
            }
            double h = ScalingQueue[ns];                         
            Vector<Matrix> P_i = LCDPS.UpdateDirections(Simplex[0], V, h);            
            for (int j=0;j<P_i.size();j++){
                double d_test[] = P_i.get(j).getColumnPackedCopy();
                double x_test[] = new double[n];
                for (int t=0;t<n;t++){
                    x_test[t] = Simplex[0].x[t] + d_test[t];
                }
                double value_test = f.f(x_test);
                Sample Sample_test = new Sample(x_test,value_test);
                if (method != 2){
                    if (value_test < Simplex[0].fx){
                        return Sample_test;
                    }
                    else{
                        if (value_test < Simplex[Simplex.length-1].fx){
                            return Sample_test;
                        } 
                    }
                }
            }
            if (!improvement){
                ns++;                       
            }
            else{
                if (ns > 0){
                    ns--;
                }           
            }
        }
        
        //get random point
        boolean feasible = false;

        if (Generator == null){
            Generator = new Random();
        }
        double x[] = new double[n];
        while(!feasible){            
            for (int i=0;i<n;i++){
                x[i] = lowBound[i] + Generator.nextDouble()*(upBound[i]-lowBound[i]);
            }
            feasible = LCDPS.FeasibleDirection(new Matrix(x,n), new Matrix(x,n), 0.0);
        }
        return new Sample(x,f.f(x));
    }

    public Sample search(AbstractFunction f,Matrix LinearConstraintMatrixA,Matrix LinearConstraintVectorb){
        return null;
    }
/*    public void run(){
        int xCount = 0;
        int k=1;        
        
        //List of samples
        Vector<Sample> Q = new Vector<Sample>();
        
        if (regularSampleFileName != null)
            WriteRegularSampling(regularSampleFileName.getValue(),0,1);      

        LinearConstraintDirectPatternSearch LCDPS = new LinearConstraintDirectPatternSearch();
        if (A != null & b != null)
            LCDPS.SetLinearConstraints(A, b);
        
        for (int r=0;r<restartCount.getValue();r++){                                    
            double ScalingQueue[] = new double[10];//{1.0,1.0/2.0,1.0/4.0,1.0/8.0,1.0/16.0,1.0/32.0,1.0/64.0,1.0/128.0};

            for (int j=0;j<10;j++){
                ScalingQueue[j] = Math.pow(0.5,(double)j)*0.5;
            }
            //get random x0
            double x[] = null;
            double fvalue = 0.0;
            //its not that easy to get a feasible point .. try random searhc
            boolean isFeasible = false;
            while (!isFeasible){
                x = this.RandomSampler();
                Matrix mx = new Matrix(x,x.length);
                isFeasible = LCDPS.FeasibleDirection(mx, mx, 0);
            }
                                
            fvalue = funct(x);
            Sample SampleX = new Sample(x,fvalue);                
            
            Matrix V = new Matrix(n,2*n);
            V.setMatrix(0,n-1,0,n-1,Matrix.identity(n,n));
            V.setMatrix(0,n-1,n,2*n-1,Matrix.identity(n,n).uminus());
                                    
            int ns = 0;
            boolean innerFail = true;
            
            
                
        try{
            Sample.writer.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    /*
    public class SimplexGradientInfo{
        public Matrix SimplexGradient;
        public Sample best;
        public boolean stencilFailureFlag;
    }
    
    SimplexGradientInfo SimplexGradient(Sample x,Matrix v){
        SimplexGradientInfo SimGradInfo = new SimplexGradientInfo();
        SimGradInfo.best = x;
        SimGradInfo.stencilFailureFlag = true;
        
        Matrix delta1 = new Matrix(n,1);
        Matrix delta2 = new Matrix(n,1);
            
        for (int i=0;i<v.getColumnDimension();i++){
            double x1[] = new double[x.x.length];
            double x2[] = new double[x.x.length];
            for (int j=0;j<n;j++){
                x1[j] = x.x[j] + v.get(i,j);
                x2[j] = x.x[j] - v.get(i,j);
            }
            Sample Sx1 = new Sample(x1,funct(x1));
            Sample Sx2 = new Sample(x2,funct(x2));
            
            delta1.set(i,0,Sx1.fx - x.fx);
            delta2.set(i,0,x.fx - Sx2.fx);
            
            if (Sx1.fx < SimGradInfo.best.fx){
                SimGradInfo.best = Sx1;
                SimGradInfo.stencilFailureFlag = false;
            }
            if (Sx2.fx < SimGradInfo.best.fx){
                SimGradInfo.best = Sx2;
                SimGradInfo.stencilFailureFlag = false;
            }                                   
        }
        //not quite sure if correct ..         
        Matrix tmp1 = v.times(v.transpose()).inverse().times(v.times(delta1));
        Matrix tmp2 = v.times(v.transpose()).inverse().times(v.times(delta2));
        
        SimGradInfo.SimplexGradient = tmp1.plus(tmp2).times(0.5);
//        SimGradInfo.SimplexGradient = SimGradInfo.SimplexGradient.times(1.0/SimGradInfo.SimplexGradient.norm1());
        return SimGradInfo;
    }
    

    public Matrix bfUpdate(double[] x,double[] xc,SimplexGradientInfo sgrad,SimplexGradientInfo gc, Matrix Hessian  ){
        Matrix y = sgrad.SimplexGradient.minus(gc.SimplexGradient);        
        Matrix s = new Matrix(n,1);       
        for (int i=0;i<n;i++){
            s.set(i,0,x[i] - xc[i]);
        }
        Matrix z = Hessian.times(s);
        if (y.transpose().times(s).get(0,0)>0){
            Matrix tmp1 = y.times(y.transpose());
            Matrix tmp2 = y.transpose().times(s);
            
            Matrix op1 = tmp1.transpose().times(tmp1).inverse().times(tmp1.transpose().times(tmp2));
            
            Matrix tmp3 = z.times(z.transpose());
            Matrix tmp4 = s.transpose().times(z);
            
            Matrix op2 = tmp3.transpose().times(tmp3).inverse().times(tmp3.transpose().times(tmp4));
            
            return Hessian.plus(op1.minus(op2));
        }
        return Hessian;
    }
        
    public Sample LineArmijoSearch(Matrix dGradient,Sample x,Matrix Hess,double beta,double h){
        double lambda = 1.0;
        
        int ArmijoIterations = 0;
        while(ArmijoIterations < 10){                        
            double xd[] = new double[n];
            for (int i=0;i<n;i++){
                if (dGradient.get(i,0) < 1.0)
                    xd[i] = x.x[i] - lambda*dGradient.get(i,0);
                else
                    xd[i] = x.x[i] - lambda;
            }
            Sample x_new = new Sample(xd,funct(xd));
            if (x_new.fx < x.fx)
                return x_new;
            lambda = lambda * beta;
            ArmijoIterations++;
        }
        //failure
        return null;
    }
    
    public void run() {
        int xCount = 0;
        int k=1;                
        //List of samples
        Vector<Sample> Q = new Vector<Sample>();
        
        if (regularSampleFileName != null)
            WriteRegularSampling(regularSampleFileName.getValue(),0,1);      

        for (int i=0;i<restartCount.getValue();i++){
            //min_gscal: if norm(difference_grad) < min_gscal*h  we terminate at the scale with success
            //beta: step size reduction factor in the line search
            //iquit: After iquit consecutive line search failures, we terminate the iteration
            //nterm: controls termination on stencil failure for centered diffs (defalt = 0)
            //  0 to terminate on stencil failure before starting the line search
            //  1 to ignore stencil failure
            //  maxit and maxitarm (defaults 2 and 5)
            //      At most maxit*n iterations are taken for each scale and at most
            //      maxitarm step length reductions are allowed
            final double    min_gscal = 0.05,
                            beta=0.5,
                            GradientTolerance = 0.00001;
                                    
            double ScalingQueue[] = new double[10];//{1.0,1.0/2.0,1.0/4.0,1.0/8.0,1.0/16.0,1.0/32.0,1.0/64.0,1.0/128.0};

            for (int j=0;j<10;j++){
                ScalingQueue[j] = Math.pow(0.5,(double)j)*0.5;
            }
            //get random x0
            double x[] = this.RandomSampler();
            double xc[] = x;
            double xold[] = x;
            
            Matrix V = Matrix.identity(n,n);
            Matrix Hess = Matrix.identity(n,n);
            int ns = 0;
            boolean innerFail = true;
            
            while (xCount < maxn.getValue()){                
                xCount++;
                
                double h = ScalingQueue[ns];
                
                double fvalue = funct(x);
                                
                Sample SampleX = new Sample(x,fvalue);                
                SimplexGradientInfo GradientInfo = SimplexGradient(SampleX,V.times(h));
                
                if (GradientInfo.SimplexGradient.normInf() < GradientTolerance || GradientInfo.stencilFailureFlag || innerFail){
                    //do something .. 
                    ns++;
                    if (ns >= ScalingQueue.length)
                        break;
                }
                else
                    ns = Math.max(0,ns-1);
                
                //simple version gradient descent
                //x = GradientInfo.best.x;
                                
                //newton iterations
                int innerIterationCounter = 0;
                innerFail = true;
            
                while(innerIterationCounter < 10){
                    SimplexGradientInfo innerGradient = GradientInfo;
                    SimplexGradientInfo dGradient = null;
                    
                    if (innerIterationCounter > 0){
                        GradientInfo = SimplexGradient(SampleX,V.times(h));
                    }
                    
                    dGradient = GradientInfo;
                    
                    if (dGradient.stencilFailureFlag){
                        break;
                    }
                    //bfUpdate(Sample x,Sample xc,SimplexGradientInfo sgrad,SimplexGradientInfo gc, Matrix Hessian  ){
                    Hess = bfUpdate(x,xc,dGradient,GradientInfo,Hess);
                    xc = x;
                    
                    Matrix direction = Hess.transpose().times(Hess).inverse().times(Hess.transpose().times(dGradient.SimplexGradient));
                    
                    Sample nextPoint = LineArmijoSearch(direction,SampleX,Hess,beta,h);
                    if (nextPoint != null){
                        SampleX = nextPoint;
                        x = SampleX.x;
                        fvalue = SampleX.fx;
                        innerFail = false;
                    }else
                        break;
                    innerIterationCounter++;
                    
                    System.out.println("*******************************************");
                    System.out.println("Restart:" + i);
                    System.out.println("Step:" + xCount + "." + innerIterationCounter + " / Functionevaluations: " + currentSampleCount);
                    System.out.println("Minimum:" + SampleX.toString());
                    System.out.println("h:" + ScalingQueue[ns]);
                    System.out.println("*******************************************");
                }
                                                                
            }
        }
                
        try{
            Sample.writer.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }*/
}
