/*
 * SensitivityAnalyser.java
 *
 * Created on 25. März 2008, 10:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.components.optimizer.nn.InputNeuron;
import jams.components.optimizer.nn.Neuron;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.components.machineLearning.GaussianLearner;

/**
 *
 * @author Christian Fischer
 */
public class SensitivityAnalyser extends Optimizer{
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
           
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "objective function name"
            )
            public JAMSString effMethodName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the prediction series"
            )
            public JAMSDouble effValue;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString outputFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSInteger method;
    
    public void init(){            
        super.init();            
    }
    
    GaussianLearner CreateGPModel(Vector<double[]> samplePoint,Vector<Double> sampleValue){        
        GaussianLearner GP = new GaussianLearner();
        GP.MeanMethod = new JAMSInteger();
        GP.MeanMethod.setValue(0);
        GP.PerformanceMeasure = new JAMSInteger();
        GP.PerformanceMeasure.setValue(2);
        GP.doOptimization = new JAMSBoolean();
        GP.doOptimization.setValue(true);          
        GP.setModel(this.getModel());
        GP.kernelMethod = new JAMSInteger();
        GP.kernelMethod.setValue(2);
        GP.resultFile = new JAMSString();
        GP.resultFile.setValue("tmp.dat");
        GP.param_theta = new JAMSDoubleArray();
        
        double params[] = new double[n+1];
        for (int i=0;i<n;i++){
            params[i] = 1.0/n;
        }
        params[n] = 0.1000;        
        
        GP.param_theta.setValue(params);
        
        double [][] data = new double[samplePoint.size()][];
        for (int i=0;i<samplePoint.size();i++){
            data[i] = samplePoint.get(i);
        }
        
        double []predict = new double[sampleValue.size()];
        for (int i=0;i<sampleValue.size();i++){
            predict[i] = sampleValue.get(i).doubleValue();
        }
        GP.trainData = JAMSDataFactory.createEntity();
        GP.trainData.setObject("data",data);
        GP.trainData.setObject("predict",predict);
        
        GP.optimizationData = JAMSDataFactory.createEntity();
        GP.optimizationData.setObject("data",data);
        GP.optimizationData.setObject("predict",predict);
                        
        GP.run();
               
        return GP;
    }
    double TransformAndEvaluate(double []in){
        double value[] = new double[in.length];
        for (int i=0;i<in.length;i++){
            value[i] = in[i]*(this.upBound[i]-this.lowBound[i]) + this.lowBound[i];
        }        
        return this.funct(value);
    }
    
    public void GaussianAnalysis(){
        Vector<double[]> samplePoint = new Vector<double[]>();
        Vector<Double> sampleValue = new Vector<Double>();
                
        for (int i=0;i<50*n;i++){
            double nextSample[] = this.RandomSampler();
            for (int j=0;j<n;j++){
                nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
            }
            samplePoint.add(nextSample);
            double value = this.TransformAndEvaluate(nextSample);            
            sampleValue.add(value);
        }
        
        GaussianLearner GP = CreateGPModel(samplePoint,sampleValue);
        
        double param[] = GP.param_theta.getValue();
        String infoString = "##########################\nParametersensitivity (Gaussian Analysis)\n";
        
        for (int i=0;i<n;i++){
            String classification = "";
            if (param[i] < 0.0)
                classification = "high";
            else if (param[i] < 1.0)
                classification = "medium";
            else
                classification = "low";
            
            infoString += this.parameterNames[i] + ":" + Math.exp(param[i]) + "-->" + classification + "\n";
        }
        
        this.getModel().getRuntime().sendInfoMsg(infoString);        
        
        if (this.outputFileName == null)
            return;
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(this.dirName + "/" + this.outputFileName.getValue()));
            writer.write(infoString);
            writer.close();
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        } 
    }
    
    void GradientAnalysis(double p){
        double startPoint[] = new double[n];
        double x[] = new double[n];
                        
        double lowValue[] = new double[n];
        double highValue[] = new double[n];
        for (int i=0;i<n;i++){
            startPoint[i] = parameters[i].getValue();
        }
        
        double value0 = funct(startPoint);
        
        //low run
        for (int i=0;i<n;i++){
            for (int j=0;j<n;j++){
                if (i==j)
                    x[j] = startPoint[j] - p*(this.upBound[j] - this.lowBound[j]);
                else
                    x[j] = startPoint[j];
            }
            lowValue[i] = this.funct(x);
        }        
        //high run
        for (int i=0;i<n;i++){
            for (int j=0;j<n;j++){
                if (i==j)
                    x[j] = startPoint[j] + p*(this.upBound[j] - this.lowBound[j]);
                else
                    x[j] = startPoint[j];
            }
            highValue[i] = this.funct(x);
        }  
        
        String infoString = "##########################\nParametersensitivity (gradient(" + "p" + "))\n";
        
        for (int i=0;i<n;i++){
            String classification = "";
            if ( (highValue[i] - lowValue[i])/value0 < 0.001)
                classification = "not sensitive";
            else if ( (highValue[i] - lowValue[i])/value0 < 0.002)
                classification = "less sensitive";
            else if ( (highValue[i] - lowValue[i])/value0 < 0.004)
                classification = "sensitive";
            else
                classification = "very sensitive";
            
            infoString += this.parameterNames[i] + ":" + (highValue[i] - lowValue[i])/value0 + "-->" + classification + "\n";
        }
        
        this.getModel().getRuntime().sendInfoMsg(infoString);        
        
        if (this.outputFileName == null)
            return;
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(this.dirName + "/" + this.outputFileName.getValue()));
            writer.write(infoString);
            writer.close();
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        } 
    }
    
    void GSA(){
        Vector<double[]> samplePoint = new Vector<double[]>();
        Vector<Double> sampleValue = new Vector<Double>();
                
        for (int i=0;i<250*n;i++){
            double nextSample[] = this.RandomSampler();
            for (int j=0;j<n;j++){
                nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
            }
            samplePoint.add(nextSample);
            double value = this.TransformAndEvaluate(nextSample);            
            sampleValue.add(value);
        }
        double threshold = 0;
        for (int i=0;i<sampleValue.size();i++){
            threshold += sampleValue.get(i);
        }
        threshold /= (double)sampleValue.size();
        
        int good[] = new int[20],
            bad[]  = new int[20];
        for (int j=0;j<20;j++){
            good[j] = bad[j] = 0;
        }
        for (int j=0;j<sampleValue.size();j++){
            if (sampleValue.get(j) < threshold){
                for (int i=0;i<n;i++){
                    
                }
            }
            else{
                for (int i=0;i<n;i++){
                    
                }
            }
        }
    }
    
    public void run() {
        if (method.getValue() == 1){
            GaussianAnalysis();
        }
        else if (method.getValue() == 2){
            GradientAnalysis(0.1);
        }
        else if (method.getValue() == 3){
            GradientAnalysis(0.0001);
        }             
        else if (method.getValue() == 4){
            GSA();
        }             
    }
    
}
