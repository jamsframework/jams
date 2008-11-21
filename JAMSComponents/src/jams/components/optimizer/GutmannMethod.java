/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import Jama.Matrix;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import jams.JAMS;
import jams.JAMSTools;
import jams.data.JAMSInteger;
import jams.model.JAMSComponentDescription;
@JAMSComponentDescription(
        title="GutmanMethod",
        author="Christian Fischer",
        description="under construction!!"
        )
public class GutmannMethod extends Optimizer {
               
    int initalSampleSize = 10;
    Sample minValue = new Sample(null,Double.POSITIVE_INFINITY);
    Sample maxValue = new Sample(null,Double.NEGATIVE_INFINITY);
    ArrayList<Sample> best10 = new ArrayList<Sample>();
    
    public double evaluate(int N,int M,Matrix coefficient,double[]x){
        double sn = 0;
        
        for (int i=0;i<N;i++){            
            sn += coefficient.get(i, 0)*kernel(Transform(sampleList.get(i).x),x);
        }
        sn += coefficient.get(N+M-1, 0);
        for (int j=0;j<M-1;j++){
            sn += coefficient.get(N+j, 0)*x[j];
        }
        return sn;
    }
    
    public class interpolator extends AbstractFunction{    
        int N;
        int M;
        Matrix coeff;
        
        interpolator(int N,int M,Matrix coeff){
            this.N = N;
            this.M = M;
            this.coeff = coeff;
        }
        
        public double f(double x[]){   
            return evaluate(N,M,coeff,x);
        }
        
    }
    
    public class innerOptimizer extends AbstractFunction{                    
        public double f(double x[]){   
            return funct(x);
        }
        
    }
    
    public class Bumpiness extends AbstractFunction{    
        double m0;
        int N;
        int M;
        Matrix solution;
        double fstar;
    
        Bumpiness(int N,Matrix solution,double fstar){
            m0 = 1;
            this.N = N;
            M = n+1;
            
            this.solution = solution;
            this.fstar = fstar;
        }
        public double f(double x[]){                                    
            Matrix A = new Matrix(N+1+M,N+1+M);
            Matrix b = new Matrix(N+1+M,1);
            
            for (int i=0;i<N;i++){
                double transformedSample_i[] = Transform(sampleList.get(i).x);
                b.set(i, 0, 0.0);
                for (int j=i;j<N;j++){
                    double transformedSample_j[] = Transform(sampleList.get(j).x);
                    //set big Phi
                    double phi_ij = kernel(transformedSample_i,transformedSample_j);
                    A.set(i, j, phi_ij);
                    A.set(j, i, phi_ij);
                                        
                }
                for (int j=0;j<M-1;j++){
                    A.set(i,N + j + 1, transformedSample_i[j]);
                    A.set(N + j + 1,i, transformedSample_i[j]);                                                    
                }
                A.set(i,N + M, 1.0);
                A.set(N + M,i, 1.0);
                //set u
                A.set(i, N, kernel(x,transformedSample_i));
                A.set(N, i, kernel(x,transformedSample_i));                                
            }
            for (int i=0;i<M-1;i++){
                //set pi
                A.set(N, N+1+i, x[i]);
                A.set(N+1+i, N, x[i]);  
                b.set(N+1+i, 0, 0.0);
                for (int j=0;j<M;j++){
                    A.set(N+1+i,N+1+j,0.0);
                }
            }            
            b.set(N, 0, 1.0);      
            //set phi(0)
            A.set(N, N, 0.0);
            A.set(N, N+M, 1.0);
            A.set(N+M, N, 1.0);
              
            Matrix mu = null;
            try{
                mu = A.solve(b);
            }catch(Exception e){
                System.out.print("cant solve interpolation system, because: singular matrix");
                return Double.POSITIVE_INFINITY;
            }
            double m0factor = -1;
            if (m0 == 1 || m0 == -1)
                m0factor = 1;
            else
                m0factor = -1;
            
            double sn = evaluate(N,M,solution,x);
            
            double value = mu.get(N, 0)*(sn - fstar)*(sn - fstar)*m0factor;
            //this is not possible
            if (value < 0){
                System.out.println("error, integral of 2nd order is less than zero");
                value = -value; //better possibilities??
            }
            return value;
        }
    }
                                                                
    public void init() {
        super.init();   
        if (!this.enable.getValue())
            return;
        this.generator.setSeed(1);
    }
                                       
    double kernel(double x[],double y[]){
        double r = 0;
        for (int i=0;i<n;i++){
            r += (x[i]-y[i])*(x[i]-y[i]);
        }
        if (r < 0.00000001)
            return 0.0;
        r = Math.sqrt(r);        
        return r*Math.log(r)*r;
    }
    
    Matrix CreateInterpolant(){
        //N number of samples
        
        int N = this.sampleList.size();
        int M = n+1;
        //system size
        Matrix A = new Matrix(N+M,N+M);
        Matrix b = new Matrix(N+M,1);
        
        for(int i=0;i<N;i++){
            double transformedSample_i[] = Transform(sampleList.get(i).x);
                        
            for (int j=i;j<N;j++){
                double transformedSample_j[] = Transform(sampleList.get(j).x);
                double phi_ij = kernel(transformedSample_i,transformedSample_j);
                A.set(i, j, phi_ij);
                A.set(j, i, phi_ij);                                
            }
            for (int j=0;j<M-1;j++){                
                //linear polynom
                A.set(i,N+j,transformedSample_i[j]);
                A.set(N+j,i,transformedSample_i[j]);                                
            }            
            A.set(i,N+M-1,1.0);
            A.set(N+M-1,i,1.0);
            
            b.set(i, 0, sampleList.get(i).fx);            
        }
        for (int i=0;i<M;i++){
            for (int j=0;j<M;j++){
                //zero matrix
                A.set(N+i,N+j,0.0);
            }
            b.set(i+N, 0, 0);
        }
        b.set(N+M-1, 0, 0);
        
        Matrix solution = null;
        try{
             solution = A.solve(b);
        }catch(Exception e){
            //so what do we do now???
            double min_d = Double.POSITIVE_INFINITY,min_d_index = 0;
            for (int i=0;i<sampleList.size();i++){
                for (int j=i+1;j<sampleList.size();j++){
                    double d = 0;
                    for (int k=0;k<n;k++){
                        d += (sampleList.get(i).x[k]-sampleList.get(j).x[k])*(sampleList.get(i).x[k]-sampleList.get(j).x[k]);
                    }
                    if (d <= 0.000000000001){
                        sampleList.remove(j);
                        j--;
                        continue;
                    }
                    if (d < min_d){
                        min_d = d;
                        min_d_index = i;
                    }
                }
            }
            System.out.println("remove index:" + min_d_index);
            sampleList.remove(min_d_index);
            return CreateInterpolant();
        }
        return solution;
    }
    
    
            
    public void WriteData(String GPmeanFile){
        if (this.n != 2){
            System.out.println("Skip rasterized output, because dim != 2");
            return;
        }
        Matrix coeff = CreateInterpolant();
        BufferedWriter writer_mean = null;
        BufferedWriter writer_var = null;
        try {
            writer_mean = new BufferedWriter(new FileWriter(JAMSTools.CreateAbsoluteFileName(this.getModel().getWorkspaceDirectory().getPath(), GPmeanFile)));
            //writer_var = new BufferedWriter(new FileWriter(this.dirName + "/" + GPvarFile));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }                        
        for (int i=0;i<51;i++){
            for (int j=0;j<51;j++){
                double x[] = new double[2];
                x[0] = 0.0 + (double)i / 50.0;
                x[1] = 0.0 + (double)j / 50.0;
                double guess = evaluate(this.sampleList.size(), n+1, coeff, x);
                try{
                    writer_mean.write( guess + "\t");                    
                }catch(Exception e){
                    System.out.println("Fehler" + e.toString());
                }           
            }
            try{
                    writer_mean.write("\n");
                    //writer_var.write("\n");
                }catch(Exception e){
                    System.out.println("Fehler" + e.toString());
                }
        }
        try{
            writer_mean.close();
        }catch(Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
/*     public void WriteGPProb(GaussianLearner GP,String GPprobFile,double target,int method){
        if (this.n != 2){
            System.out.println("Skip rasterized output, because dim != 2");
            return;
        }
        
        BufferedWriter writer_prob = null;
        try {
            writer_prob = new BufferedWriter(new FileWriter(this.dirName + "/" + GPprobFile));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }     
        GaussEffFunction function = new GaussEffFunction();
        function.GP = GP;
        function.target = target;
        function.method = method;
            
        for (int i=0;i<51;i++){
            for (int j=0;j<51;j++){
                double x[] = new double[2];
                x[0] = 0.0 + (double)i / 50.0;
                x[1] = 0.0 + (double)j / 50.0;
                double mean = GP.GetMean(x);
                double optprob = function.f(x);
                try{
                    writer_prob.write( optprob + "\t");
                }catch(Exception e){
                    System.out.println("Fehler" + e.toString());
                }           
            }
            try{
                    writer_prob.write("\n");
                }catch(Exception e){
                    System.out.println("Fehler" + e.toString());
                }
        }
        try{
            writer_prob.close();
        }catch(Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }*/
    
    public double[] FindMostProbablePoint(int N,Matrix coefficents,double target){
        SimpleSCE sce = new SimpleSCE();
        double[] normedLowBound = new double[n];
        double[] normedUpBound = new double[n];
        for (int i=0;i<n;i++){
            normedLowBound[i] = 0.0;
            normedUpBound[i] = 1.0;
        }
            
        Bumpiness function = new Bumpiness(N,coefficents,target);
                        
        Sample solution = sce.offlineRun(null,normedLowBound,normedUpBound,3,Optimizer.MODE_MINIMIZATION,10000,12,0.05,0.0001,function);            
            
        return solution.x;
    }
    
    public Sample FindInterpolatedMinimum(int N,Matrix coefficents){
        SimpleSCE sce = new SimpleSCE();
        double[] normedLowBound = new double[n];
        double[] normedUpBound = new double[n];
        for (int i=0;i<n;i++){
            normedLowBound[i] = 0.0;
            normedUpBound[i] = 1.0;
        }
        
        interpolator function = new interpolator(N,n+1,coefficents);
                        
        Sample solution = sce.offlineRun(null,normedLowBound,normedUpBound,4,Optimizer.MODE_MINIMIZATION,10000,5,0.025,0.0001,function);            
            
        return solution;
    }
            
    public void initalPhase(){                
        for (int i=0;i<n*initalSampleSize;i++){                        
            Sample s = this.getSample(this.RandomSampler());
                        
            if (minValue.fx > s.fx){
                minValue = s;
                best10.add(minValue);
            }
            if (maxValue.fx < s.fx)
                maxValue = s;            
        }
        
    }
    
    public long sigma(int counter,int n_snake,int cycleLength){
        if (counter == n_snake)
            return n_snake;
        return sigma(counter-1,n_snake,cycleLength)-Math.round((this.currentSampleCount-this.initalSampleSize)/(double)cycleLength);
    }
           
    public double[] Transform(double[]x){
        double nx[] = new double[x.length];
        for (int i=0;i<x.length;i++)
            nx[i] = (x[i]-this.lowBound[i])/(this.upBound[i]-this.lowBound[i]);
        return nx;
    }
    public double[] ReTransform(double[]x){
        double nx[] = new double[x.length];
        for (int i=0;i<x.length;i++)
            nx[i] = x[i]*(this.upBound[i]-this.lowBound[i])+this.lowBound[i];
        return nx;
    }
            
    public void run() { 
        if (!enable.getValue()){
            singleRun();
            return;
        }
        initalPhase();
                        
        int iterationCounter = 0;
        int cycleLength = 5;
        while(iterationCounter < this.maxn.getValue()){  
            iterationCounter++;
            
            Matrix coefficient = this.CreateInterpolant();
      
            int k = sampleList.size() % cycleLength;
            int n_snake = sampleList.size() - k - 1;
            
            
            Sample myMin = this.FindInterpolatedMinimum(sampleList.size(), coefficient);

            boolean toNear = false;
            for (int i=0;i<this.sampleList.size();i++){
                double y[] = Transform(this.sampleList.get(i).x);
                double r = 0;
                for (int j=0;j<n;j++){                    
                    r += (myMin.x[j] - y[j])*(myMin.x[j] - y[j]);
                }
                if (r < 0.000000001){                    
                    toNear = true;
                    break;
                }                    
            }
            
            //double target = this.minValue.fx - (double)(k*k)*(sampleList.get((int)sigma(iterationCounter,n_snake,cycleLength)).fx-this.minValue.fx);
            double target = (double)(k*k)*(sampleList.get((int)sigma(sampleList.size()-1,n_snake,cycleLength)-1).fx-this.minValue.fx);
            double next[] = null;
            if (toNear){
                System.out.println("use target point!");
                next = this.FindMostProbablePoint(this.sampleList.size(), coefficient, target);
            }
            else{
                System.out.println("use minimum!");
                next = myMin.x;
            }
            toNear = false;
            double minDist = Double.POSITIVE_INFINITY;
            
            do{
                minDist = Double.POSITIVE_INFINITY;
                for (int j=0;j<sampleList.size();j++){                
                    double y[] = Transform(this.sampleList.get(j).x);
                    double dist = 0;
                    for (int i=0;i<next.length;i++){                
                        dist += (y[i]-next[i])*(y[i]-next[i]);
                    }
                    minDist = Math.min(dist, minDist);                    
                    //sample randomly
                    if (minDist < 0.000000001){
                        System.out.println("distance between next point and a allready sampled point is too small");
                        next = Transform(this.RandomSampler());                    
                        break;
                    }
                }                    
            }while (minDist < 0.000000001);
            
            Sample s = this.getSample(ReTransform(next));
            
            if (minValue.fx > s.fx){
                minValue = s;
                best10.add(minValue);
            }
            if (this.sampleList.size() > this.maxn.getValue()-10){
                //find new minimum
                NelderMead neldermeadOptimizer = new NelderMead();
                neldermeadOptimizer.GoalFunction = new innerOptimizer();
                neldermeadOptimizer.boundaries = this.boundaries;
                neldermeadOptimizer.lowBound = this.lowBound;
                neldermeadOptimizer.maxn = new JAMSInteger(200);
                neldermeadOptimizer.mode = new JAMSInteger(NelderMead.MODE_MINIMIZATION);
                neldermeadOptimizer.n = n;
                neldermeadOptimizer.upBound = upBound;
                Sample initialSimplex[] = new Sample[n+1];
                for (int i=0;i<n+1;i++){
                    if (i==0)
                        initialSimplex[i] = best10.get(best10.size()-1);
                    else{
                        initialSimplex[i] = sampleList.get(generator.nextInt(sampleList.size()));
                    }
                }
                neldermeadOptimizer.initialSimplex = initialSimplex;
                neldermeadOptimizer.setModel(this.getModel());
                neldermeadOptimizer.run();                
                this.sampleList.addAll(neldermeadOptimizer.sampleList);
                iterationCounter += neldermeadOptimizer.sampleList.size();
                for (int i=0;i<sampleList.size();i++){
                    if (sampleList.get(i).fx < minValue.fx){
                        minValue = sampleList.get(i);
                        best10.add(minValue);
                    }
                    if (sampleList.get(i).fx > maxValue.fx)
                        maxValue = sampleList.get(i);
                }
            }
            if (maxValue.fx < s.fx)
                maxValue = s;
            System.out.println("minimum:" + minValue);
                        
//            WriteData("output\\datafile"+iterationCounter+".dat");
        }
            /*
            if (writeGPData != null && writeGPData.getValue() == true){
                WriteGPData(GP,"/info/gp_mean" + iterationCounter + ".dat","/info/gp_variance" + iterationCounter + ".dat");
            }
            
            Vector<double[]> nextSamples = null;
            if (GPMethod.getValue() == 1)
                nextSamples = searchPhase_MaxProbOfImprovement(GP);
            else if (GPMethod.getValue() == 2)
                nextSamples = searchPhase_MaxExpectedImprovement(GP);
            else if (GPMethod.getValue() == 3)
                nextSamples = this.searchPhase_MaximalLikelihood(GP);
                                    
            for (int i=0;i<nextSamples.size();i++){
                //test if point has been already sampled
                boolean pointInList = true;
                double nextSample[] = nextSamples.get(i);
                while (pointInList){
                    pointInList = inList(samplePoint,nextSample);
                    
                    if (pointInList){
                        nextSample = this.RandomSampler();
                        for (int j=0;j<n;j++){
                            nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
                        }
                    }                    
                }
                                                
           /*     samplePoint.add(nextSample);
                double value = TransformAndEvaluate(nextSample);
                
                if (value < minValue){
                    minValue = value;
                    minPosition = nextSample;                    
                }else if (value > maxValue){
                    maxValue = value;
                }
                sampleValue.add(value);
                
                for (int j=0;j<n;j++){
                    System.out.println(nextSample[j] + " ");                    
                }
                System.out.println("value:" + value);
            }                        
            System.out.println("Evaluations:" + this.currentSampleCount + "\nMinimum:" + minValue);
            if (currentSampleCount > this.maxn.getValue())
                return;
            iterationCounter++;
        }*/
    }                     
}
