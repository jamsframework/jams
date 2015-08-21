/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.optimizer.experimental;

import optas.core.SampleLimitException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.JAMSComponentDescription;
import optas.datamining.GaussianLearner;
import java.util.ArrayList;
import java.util.Arrays;
import optas.core.AbstractFunction;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.SCE;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.StringOptimizerParameter;



@JAMSComponentDescription(
        title="NNOptimizer",
        author="Christian Fischer",
        description="under construction!!"
        )
public class GPSearch extends optas.optimizer.Optimizer {
    private String outputFileName;
    private String modelGridFileName;
    private Boolean writeGPData;
    public int gaussProcessMethod;

    public OptimizerDescription getDescription(){
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(GPSearch.class.getSimpleName(), GPSearch.class.getName(), 250, false);

        desc.addParameter(new NumericOptimizerParameter("gaussProcessMethod","Gaussian Process Method used for optimization",6,0,20));
        desc.addParameter(new StringOptimizerParameter("outputFileName","File to write data log","out.log"));
        desc.addParameter(new StringOptimizerParameter("writeGPData","File to write gaussian regression log","gp.log"));
        desc.addParameter(new StringOptimizerParameter("modelGridFileName","File to write model grid","grid.dat"));
        return desc;
    }

    /**
     * @return the outputFileName
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * @param outputFileName the outputFileName to set
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * @return the modelGridFileName
     */
    public String getModelGridFileName() {
        return modelGridFileName;
    }

    /**
     * @param modelGridFileName the modelGridFileName to set
     */
    public void setModelGridFileName(String modelGridFileName) {
        this.modelGridFileName = modelGridFileName;
    }

    /**
     * @return the writeGPData
     */
    public Boolean getWriteGPData() {
        return writeGPData;
    }

    /**
     * @param writeGPData the writeGPData to set
     */
    public void setWriteGPData(Boolean writeGPData) {
        this.writeGPData = writeGPData;
    }

    /**
     * @return the gaussProcessMethod
     */
    public double getGaussProcessMethod() {
        return gaussProcessMethod;
    }

    /**
     * @param gaussProcessMethod the gaussProcessMethod to set
     */
    public void setGaussProcessMethod(double gaussProcessMethod) {
        this.gaussProcessMethod = (int)gaussProcessMethod;
    }

    public class GaussEffFunction extends AbstractFunction{
        GaussianLearner GP = null;
        double target;
        int method;

        @Override
        public String[] getInputFactorNames(){
            String names[] = new String[n];
            for (int i=0;i<n;i++){
                names[i] = "p_" + i;
            }
            return names;
        }
        @Override
        public int getInputDimension(){
            return n;
        }
        @Override
        public double[][] getRange(){
            double range[][] = new double[n][2];
            for (int i=0;i<n;i++){
                range[i][0] = lowBound[i];
                range[i][1] = upBound[i];
            }
            return range;
        }
        @Override
        public String[] getOutputFactorNames(){
            return new String[]{"GaussEff"};
        }
        @Override
        public int getOutputDimension(){
            return m;
        }
        
        @Override
        public void log(String s){
            GPSearch.this.log(s);
        }

        @Override
        public double[] evaluate(double x[]){
            if (method == 1)
                return new double[]{GP.getProbabilityForXLessY(x,target)};
            else if (method == 2)
                return new double[]{GP.getExpectedImprovement(x,target)};
            else
                return new double[]{GP.getMarginalLikelihoodWithAdditionalSample(x,target)};
        }
    }
                                                   
    @SuppressWarnings("unchecked")
    ArrayList<double[]> TrainData = new ArrayList<double[]>();
    
    ArrayList<double[]> samplePoint = new ArrayList<double[]>();
    ArrayList<Double>   sampleValue = new ArrayList<Double>();

    final int initalSampleSize = 25;
    double maxValue = Double.NEGATIVE_INFINITY, minValue = Double.POSITIVE_INFINITY;
    double [] minPosition = null;
                                      
    SampleSO TransformAndEvaluate(double []in) throws SampleLimitException, ObjectiveAchievedException{
        double value[] = new double[in.length];
        for (int i=0;i<in.length;i++){
            value[i] = in[i]*(this.upBound[i]-this.lowBound[i]) + this.lowBound[i];
        }        
        return getSampleSO(value);
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
    
    GaussianLearner CreateGPModel(ArrayList<double[]> samplePoint,ArrayList<Double> sampleValue){
        if (GP == null || createCount % 10 == 0){
            GP = new GaussianLearner();
            GP.MeanMethod = DefaultDataFactory.getDataFactory().createInteger();
            GP.MeanMethod.setValue(0);
            GP.PerformanceMeasure = DefaultDataFactory.getDataFactory().createInteger();
            GP.PerformanceMeasure.setValue(PerformanceMeasure);
            GP.mode = DefaultDataFactory.getDataFactory().createInteger();
            GP.mode.setValue(GaussianLearner.MODE_OPTIMIZE);                          
            //GP.setModel(this.getModel());
            GP.kernelMethod = DefaultDataFactory.getDataFactory().createInteger();
            GP.kernelMethod.setValue(8);
            GP.resultFile = DefaultDataFactory.getDataFactory().createString();
            GP.resultFile.setValue("tmp.dat");
            GP.param_theta = DefaultDataFactory.getDataFactory().createDoubleArray();
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
            GP.trainData = DefaultDataFactory.getDataFactory().createEntity();
            GP.trainData.setObject("data",data);
            GP.trainData.setObject("predict",predict);
        
            GP.optimizationData = (JAMSEntity)DefaultDataFactory.getDataFactory().createEntity();
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
                
    public void WriteSamples(ArrayList<Double> sampleValue,ArrayList<double[]> samplePoint,String file){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getWorkspace().getAbsolutePath() + "/" + file));
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
            writer_mean = new BufferedWriter(new FileWriter(getWorkspace().getAbsolutePath() + "/" + GPmeanFile));
            writer_var = new BufferedWriter(new FileWriter(getWorkspace().getAbsolutePath() + "/" + GPvarFile));
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
            writer_prob = new BufferedWriter(new FileWriter(getWorkspace().getAbsolutePath() + "/" + GPprobFile));
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
                double optprob = function.evaluate(x)[0];
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

            SCE optimizer = new SCE();
            optimizer.setStartValue(startpoint);            
            optimizer.setWorkspace(this.getWorkspace());
            optimizer.complexesCount = 3;
            optimizer.setMaxn(10000);
            optimizer.pcento = 0.05;
            optimizer.kstop = 12;
            optimizer.peps = 0.0001;
            optimizer.setFunction(function);

            best = optimizer.optimize().get(0).x;            
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
                    
                    double prob = function.evaluate(x)[0];
                    if (prob >= value){
                        best = x;
                        value = prob;
                    }
                }
            }
        }        
        return best;
    }
    
    public ArrayList<double[]> cluster(ArrayList<double[]> set){
        ArrayList<ArrayList<double[]>> clusterset = new ArrayList<ArrayList<double[]>>();
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
                ArrayList<double[]> nextCluster = new ArrayList<double[]>();
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
        ArrayList<double[]> result = new ArrayList<double[]>();
        for (int j=0;j<clusterset.size();j++){
            result.add(clusterset.get(j).get(clusterset.get(j).size()-1));
        }
        return result;
    }
    
    public boolean inList(ArrayList<double[]> list, double[] point){
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
            double nextSample[] = this.randomSampler();
            if (i==0 && x0 != null){                
                nextSample = Arrays.copyOf(x0[0], n);
            }
            for (int j=0;j<n;j++){                                    
                nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
            }
            samplePoint.add(nextSample);
            SampleSO value=null;
            try{
                value = this.TransformAndEvaluate(nextSample);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e);
                return;
            }
            if (value.f() < minValue){
                minValue = value.f();
                minPosition = nextSample;
            }else if (value.f() > maxValue){
                maxValue = value.f();
            }
            
            sampleValue.add(value.f());
        }
        
    }
    
    public ArrayList<double[]> searchPhase_MaxExpectedImprovement(GaussianLearner GP){
        ArrayList<double[]> bestPoints = new ArrayList<double[]>();
        bestPoints.add(FindMostProbablePoint(minPosition,GP,minValue,2));
        
        System.out.println(JAMS.i18n("Expected_Improvement") + GP.getExpectedImprovement(bestPoints.get(0),minValue));
        if (getWriteGPData() != null && getWriteGPData() == true){
            WriteGPProb(GP,"\\info\\gp_eimpr_" + factory.getSize() + ".dat",minValue,2);
        }
        return bestPoints;
    }
    
    public ArrayList<double[]> searchPhase_MaxProbOfImprovement(GaussianLearner GP){
        double T[] = {0,0.0001,0.001,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.10,0.11,0.12,0.13,0.15,0.20,0.25,0.3,0.4,0.5,0.75,1.0,1.5,2.0,3.0};                
        //global search
        ArrayList<double[]> bestPoints = new ArrayList<double[]>();
        for (int i=0;i<T.length;i++){                       
            double opt = minValue - (T[i]*(maxValue-minValue));                                                    
            //zeige nachgebildetes modell! und deren w'keit
            if (getWriteGPData() != null && getWriteGPData() == true){
                WriteGPProb(GP,"\\info\\gp_prob" + factory.getSize() + "_T" + T[i] + ".dat",opt,1);
            }
            bestPoints.add(FindMostProbablePoint(minPosition,GP,opt,1));
        }
        return cluster(bestPoints);
    }
    
    public ArrayList<double[]> searchPhase_MaximalLikelihood(GaussianLearner GP){
        double T[] = {0,0.0001,0.001,0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.10,0.11,0.12,0.13,0.15,0.20,0.25,0.3,0.4,0.5,0.75,1.0,1.5,2.0,3.0};                
        //global search
        ArrayList<double[]> bestPoints = new ArrayList<double[]>();
        for (int i=0;i<T.length;i++){                       
            double opt = minValue - (T[i]*(maxValue-minValue));                                                    
            //zeige nachgebildetes modell! und deren w'keit    
            if (getWriteGPData() != null && getWriteGPData() == true){
                WriteGPProb(GP,"\\info\\gp_prob" + factory.getSize() + "_T" + T[i] + ".dat",opt,3);
            }
            bestPoints.add(FindMostProbablePoint(minPosition,GP,opt,3));
        }
        return cluster(bestPoints);
    }
    
    @Override
    protected void procedure(){        
        initalPhase();
        
        while(true){
            GaussianLearner GP = CreateGPModel(samplePoint,sampleValue);
                    
            if (getWriteGPData() != null && getWriteGPData() == true){
                WriteGPData(GP,"/info/gp_mean" + factory.getSize() + ".dat","/info/gp_variance" + factory.getSize() + ".dat");
            }
            
            ArrayList<double[]> nextSamples = null;
            switch((int)getGaussProcessMethod()){
                case 1:nextSamples = searchPhase_MaxProbOfImprovement(GP);
                case 2:nextSamples = searchPhase_MaxExpectedImprovement(GP);
                case 3:nextSamples = this.searchPhase_MaximalLikelihood(GP);
                default: nextSamples = null;
            }
                                                
            for (int i=0;i<nextSamples.size();i++){
                //test if point has been already sampled
                boolean pointInList = true;
                double nextSample[] = nextSamples.get(i);
                while (pointInList){
                    pointInList = inList(samplePoint,nextSample);
                    
                    if (pointInList){
                        nextSample = this.randomSampler();
                        for (int j=0;j<n;j++){
                            nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
                        }
                    }                    
                }
                                                
                samplePoint.add(nextSample);
                SampleSO value= null;
                try{
                    value= TransformAndEvaluate(nextSample);
                }catch(SampleLimitException sle){
                    return;
                }catch(ObjectiveAchievedException oae){
                    return;
                }
                                
                if (value.f() < minValue){
                    minValue = value.f();
                    minPosition = nextSample;                    
                }else if (value.f() > maxValue){
                    maxValue = value.f();
                }
                sampleValue.add(value.f());
                
                for (int j=0;j<n;j++){
                    System.out.println(nextSample[j] + " ");                    
                }
                log(JAMS.i18n("value") + ":" + value);
            }                        
            log(JAMS.i18n("Evaluations") + ":" + this.factory.getSize() + "\n" + JAMS.i18n("Minimum") + ":" + minValue);
        }
    }                     
}
