/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.components.machineLearning.GaussianLearner;

//import jams.components.optimizer.
import jams.components.optimizer.SampleFactory.SampleSO;
import java.util.Arrays;
@JAMSComponentDescription(
        title="NNOptimizer",
        author="Christian Fischer",
        description="under construction!!"
        )
public class GPSearch extends SOOptimizer {
    /*
     *  Component variables
     */            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String outputFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String modelGridFileName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.Boolean writeGPData;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.Integer GPMethod;
    
    public class GaussEffFunction extends AbstractFunction{
        GaussianLearner GP = null;
        double target;
        int method;
                
        public double f(double x[]){
            if (method == 1)
                return GP.getProbabilityForXLessY(x,target);
            else if (method == 2)
                return GP.getExpectedImprovement(x,target);
            else
                return GP.getMarginalLikelihoodWithAdditionalSample(x,target);
        }
    }
                                                   
    @SuppressWarnings("unchecked")
    Vector<double[]> TrainData = new Vector<double[]>();
    
    Vector<double[]> samplePoint = new Vector<double[]>();
    Vector<Double>   sampleValue = new Vector<Double>();

    final int initalSampleSize = 25;
    double maxValue = Double.NEGATIVE_INFINITY, minValue = Double.POSITIVE_INFINITY;
    double [] minPosition = null;
        
    @Override
    public void init() {
        super.init();      
        if (!enable.getValue())
            return;
    }
                              
    double TransformAndEvaluate(double []in) throws SampleLimitException, ObjectiveAchievedException{
        double value[] = new double[in.length];
        for (int i=0;i<in.length;i++){
            value[i] = in[i]*(this.upBound[i]-this.lowBound[i]) + this.lowBound[i];
        }        
        return this.funct(value);
    }
        
    class DVector{
        double[] value;  
        
        DVector(int d){
            value = new double[d];
        }
        
        @Override
        public String toString() {
            String r = new String();
            for (int i=0;i<value.length;i++){
                r += value[i] + "\t";
            }
            return r;
        }
    }
    
    int createCount = 0;       
    double params[] = new double[n*n+5*n];
    GaussianLearner GP = null;
    int lastTrainingSize = 0;
    final int PerformanceMeasure = 2;
    
    GaussianLearner CreateGPModel(Vector<double[]> samplePoint,Vector<Double> sampleValue){        
        if (GP == null || createCount % 10 == 0){
            GP = new GaussianLearner();
            GP.MeanMethod = getModel().getRuntime().getDataFactory().createInteger();
            GP.MeanMethod.setValue(0);
            GP.PerformanceMeasure = getModel().getRuntime().getDataFactory().createInteger();
            GP.PerformanceMeasure.setValue(PerformanceMeasure);
            GP.mode = getModel().getRuntime().getDataFactory().createInteger();
            GP.mode.setValue(GaussianLearner.MODE_OPTIMIZE);                          
            GP.setModel(this.getModel());
            GP.kernelMethod = getModel().getRuntime().getDataFactory().createInteger();
            GP.kernelMethod.setValue(8);
            GP.resultFile = getModel().getRuntime().getDataFactory().createString();
            GP.resultFile.setValue("tmp.dat");
            GP.param_theta = getModel().getRuntime().getDataFactory().createDoubleArray();
            if (createCount == 0){
                params = new double[n*n+5*n];
                for (int i=0;i<params.length;i++){                
                    params[i] = 2.71;
                }            
                for (int i=0;i<n;i++){                
                    params[i*(n+2)] = 2.71;
                }                                  
            }
            GP.param_theta.setValue(params);
            double [][] data = new double[samplePoint.size()][];
            for (int i=0;i<samplePoint.size();i++){
                data[i] = samplePoint.get(i);
            }        
            double []predict = new double[sampleValue.size()];
            for (int i=0;i<sampleValue.size();i++){
                predict[i] = sampleValue.get(i).doubleValue();
            }
            GP.trainData = getModel().getRuntime().getDataFactory().createEntity();
            GP.trainData.setObject("data",data);
            GP.trainData.setObject("predict",predict);
        
            GP.optimizationData = (Attribute.Entity)getModel().getRuntime().getDataFactory().createEntity();
            GP.optimizationData.setObject("data",data);
            GP.optimizationData.setObject("predict",predict);
                        
            GP.run();
            lastTrainingSize = samplePoint.size();
        }
        else if (createCount % 10 != 0){
            for (int i=lastTrainingSize;i<samplePoint.size();i++)
                GP.RetrainWithANewObservation(PerformanceMeasure,samplePoint.get(i),sampleValue.get(i));
            
            lastTrainingSize = samplePoint.size();
        }
        createCount++;                
        return GP;
    }
                
    public void WriteSamples(Vector<Double> sampleValue,Vector<double[]> samplePoint,String file){           
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getModel().getWorkspaceDirectory().getPath() + "/" + file));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JAMSTools.handle(ioe);
        }
                
        for (int i=0;i<samplePoint.size();i++){           
            try{
                double point[] = samplePoint.get(i);
                double value   = sampleValue.get(i).doubleValue();
                for (int j=0;j<point.length;j++){
                    writer.write(point[j] + "\t");
                }
                writer.write(value + "\n");
                }catch(Exception e){
                    System.out.println(JAMS.i18n("Error") + " " + e.toString());
                }
        }
        try{
            writer.close();
        }catch(Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    public void WriteGPData(GaussianLearner GP,String GPmeanFile,String GPvarFile){
        if (this.n != 2){
            System.out.println((JAMS.i18n("Skip_rasterized_output")));
            return;
        }
        
        BufferedWriter writer_mean = null;
        BufferedWriter writer_var = null;
        try {
            writer_mean = new BufferedWriter(new FileWriter(getModel().getWorkspaceDirectory().getPath() + "/" + GPmeanFile));
            writer_var = new BufferedWriter(new FileWriter(getModel().getWorkspaceDirectory().getPath() + "/" + GPvarFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JAMSTools.handle(ioe);
        }                        
        for (int i=0;i<51;i++){
            for (int j=0;j<51;j++){
                double x[] = new double[2];
                x[0] = 0.0 + (double)i / 50.0;
                x[1] = 0.0 + (double)j / 50.0;
                double mean = GP.getMean(x);
                double variance = GP.getVariance(x);
                try{
                    writer_mean.write( mean + "\t");
                    writer_var.write( variance + "\t");
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println(JAMS.i18n("Error") + " " + e.toString());
                }           
            }
            try{
                    writer_mean.write("\n");
                    writer_var.write("\n");
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println(JAMS.i18n("Error") + " " + e.toString());
                }
        }
        try{
            writer_mean.close();
            writer_var.close();
        }catch(Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
     public void WriteGPProb(GaussianLearner GP,String GPprobFile,double target,int method){
        if (this.n != 2){
            System.out.println(JAMS.i18n("Skip_rasterized_output"));
            return;
        }
        
        BufferedWriter writer_prob = null;
        try {
            writer_prob = new BufferedWriter(new FileWriter(getModel().getWorkspaceDirectory().getPath() + "/" + GPprobFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JAMSTools.handle(ioe);
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
                double mean = GP.getMean(x);
                double optprob = function.f(x);
                try{
                    writer_prob.write( optprob + "\t");
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println(JAMS.i18n("Error") + " " + e.toString());
                }           
            }
            try{
                    writer_prob.write("\n");
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println(JAMS.i18n("Error") + " " + e.toString());
                }
        }
        try{
            writer_prob.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    public double[] FindMostProbablePoint(double[] startpoint,GaussianLearner GP,double target,int method){
        //for testing proposes .. random sampler        
        double best[] = new double[n];
        double value = -100000000000000000000000.0;
        double cumulatedProb = 0.0;

        if (this.n != -1){ 
            SimpleSCE sce = new SimpleSCE();
            double[] normedLowBound = new double[n];
            double[] normedUpBound = new double[n];
            for (int i=0;i<n;i++){
                normedLowBound[i] = 0.0;
                normedUpBound[i] = 1.0;
            }
            GaussEffFunction function = new GaussEffFunction();
            function.GP = GP;
            function.target = target;
            function.method = method;
            
            SampleSO solution = sce.offlineRun(startpoint,normedLowBound,normedUpBound,3,Optimizer.MODE_MAXIMIZATION,10000,12,0.05,0.0001,function);            
            
            best = solution.x;       
        }else{
            GaussEffFunction function = new GaussEffFunction();
            function.GP = GP;
            function.target = target;
            function.method = method;
            
            for (int i=0;i<51;i++){
                for (int j=0;j<51;j++){
                    double x[] = new double[2];
                    x[0] = 0.0 + (double)i / 50.0;
                    x[1] = 0.0 + (double)j / 50.0;
                    
                    double prob = function.f(x);
                    if (prob >= value){
                        best = x;
                        value = prob;
                    }
                }
            }
        }        
        return best;
    }
    
    public Vector<double[]> cluster(Vector<double[]> set){
        Vector<Vector<double[]>> clusterset = new Vector<Vector<double[]>>();
        for (int i=0;i<set.size();i++){
            double best = 100000000000.0;
            int bestindex = -1;
            for (int j=0;j<clusterset.size();j++){
                for (int k=0;k<clusterset.get(j).size();k++){
                    double d = 0;
                    for (int l=0;l<n;l++)
                        d += (clusterset.get(j).get(k)[l]-set.get(i)[l])*(clusterset.get(j).get(k)[l]-set.get(i)[l]);
                    if (d < best){
                        best = d;
                        bestindex = j;
                    }
                }
            }
            if (bestindex == -1 || best > 0.05){
                Vector<double[]> nextCluster = new Vector<double[]>();
                nextCluster.add(set.get(i));
                clusterset.add(nextCluster);
            }
            else{
                double[] newPoint = new double[n];
                for (int k=0;k<n;k++){
                    //newPoint[k] = 0.5*(result.get(bestindex)[k] + set.get(i)[k]);
                }
                clusterset.get(bestindex).add(set.get(i));
            }                
        }
        Vector<double[]> result = new Vector<double[]>();
        for (int j=0;j<clusterset.size();j++){
            result.add(clusterset.get(j).get(clusterset.get(j).size()-1));
        }
        return result;
    }
    
    public boolean inList(Vector<double[]> list, double[] point){
        for (int j=0;j<list.size();j++){
            double sampleInList[] = list.get(j);
            double d = 0;
            for (int k=0;k<n;k++){
                d += (sampleInList[k] - point[k])*(sampleInList[k] - point[k]);                       
            }
            if (d < 0.000000000001){
                return true;
            }
        }
        return false;
    }
    
    public void initalPhase(){
        GaussianLearner.BuildGaussDistributionTable();
                
        for (int i=0;i<n*initalSampleSize;i++){
            double nextSample[] = this.RandomSampler();
            if (i==0 && x0 != null){                
                nextSample = Arrays.copyOf(x0, n);
            }
            for (int j=0;j<n;j++){                                    
                nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
            }
            samplePoint.add(nextSample);
            double value=0;
            try{
                value = this.TransformAndEvaluate(nextSample);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e);
                return;
            }
            if (value < minValue){
                minValue = value;
                minPosition = nextSample;
            }else if (value > maxValue){
                maxValue = value;
            }
            
            sampleValue.add(value);
        }
        
    }
    
    public Vector<double[]> searchPhase_MaxExpectedImprovement(GaussianLearner GP){
        Vector<double[]> bestPoints = new Vector<double[]>();
        bestPoints.add(FindMostProbablePoint(minPosition,GP,minValue,2));
        
        System.out.println(JAMS.i18n("Expected_Improvement") + GP.getExpectedImprovement(bestPoints.get(0),minValue));
        if (writeGPData != null && writeGPData.getValue() == true){
            WriteGPProb(GP,"\\info\\gp_eimpr_" + iterationCounter + ".dat",minValue,2);
        }
        return bestPoints;
    }
    
    public Vector<double[]> searchPhase_MaxProbOfImprovement(GaussianLearner GP){
        double T[] = {0,0.0001,0.001,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.10,0.11,0.12,0.13,0.15,0.20,0.25,0.3,0.4,0.5,0.75,1.0,1.5,2.0,3.0};                
        //global search
        Vector<double[]> bestPoints = new Vector<double[]>();
        for (int i=0;i<T.length;i++){                       
            double opt = minValue - (T[i]*(maxValue-minValue));                                                    
            //zeige nachgebildetes modell! und deren w'keit
            if (writeGPData != null && writeGPData.getValue() == true){
                WriteGPProb(GP,"\\info\\gp_prob" + iterationCounter + "_T" + T[i] + ".dat",opt,1);
            }
            bestPoints.add(FindMostProbablePoint(minPosition,GP,opt,1));
        }
        return cluster(bestPoints);
    }
    
    public Vector<double[]> searchPhase_MaximalLikelihood(GaussianLearner GP){
        double T[] = {0,0.0001,0.001,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.10,0.11,0.12,0.13,0.15,0.20,0.25,0.3,0.4,0.5,0.75,1.0,1.5,2.0,3.0};                
        //global search
        Vector<double[]> bestPoints = new Vector<double[]>();
        for (int i=0;i<T.length;i++){                       
            double opt = minValue - (T[i]*(maxValue-minValue));                                                    
            //zeige nachgebildetes modell! und deren w'keit    
            if (writeGPData != null && writeGPData.getValue() == true){
                WriteGPProb(GP,"\\info\\gp_prob" + iterationCounter + "_T" + T[i] + ".dat",opt,3);
            }
            bestPoints.add(FindMostProbablePoint(minPosition,GP,opt,3));
        }
        return cluster(bestPoints);
    }
    
    @Override
    protected void procedure() throws SampleLimitException, ObjectiveAchievedException{
        if (!enable.getValue()){
            singleRun();
            return;
        }
        initalPhase();
        
        while(true){
            GaussianLearner GP = CreateGPModel(samplePoint,sampleValue);
                    
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
                                                
                samplePoint.add(nextSample);
                double value= TransformAndEvaluate(nextSample);
                                
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
                System.out.println(JAMS.i18n("value") + ":" + value);
            }                        
            System.out.println(JAMS.i18n("Evaluations") + ":" + this.iterationCounter.getValue() + "\n" + JAMS.i18n("Minimum") + ":" + minValue);
        }
    }                     
}
