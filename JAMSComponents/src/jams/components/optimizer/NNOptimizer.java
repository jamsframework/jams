/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.components.optimizer.nn.InputNeuron;
import jams.components.optimizer.nn.NeuralConnection;
import jams.components.optimizer.nn.Neuron;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSComponentDescription;
import org.unijena.jams.model.JAMSContext;
import org.unijena.jams.model.JAMSVarDescription;

//import jams.components.optimizer.
@JAMSComponentDescription(
        title="NNOptimizer",
        author="Christian Fischer",
        description="under construction!!"
        )
public class NNOptimizer extends JAMSContext{
    /*
     *  Component variables
     */    
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
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode"
            )
            public JAMSInteger mode;
          
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum runs"
            )
            public JAMSInteger maxn;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler"
            )
            public JAMSBoolean enable;
    
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
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    double[] lowBound;
    double[] upBound;
    
    //number of parameters!!
    int n;
        
    int currentSampleCount;
    int SamplesPerIteration = 60;
    Random generator = new Random();
    
    GenericDataWriter writer;
    
    static final int MODE_MAXIMIZATION = 1;
    static final int MODE_MINIMIZATION = 2;
    static final int MODE_ABSMAXIMIZATION = 3;  
    static final int MODE_ABSMINIMIZATION = 4;
    
    int LayerCount = 4;
    @SuppressWarnings("unchecked")
    Vector<Neuron> Layer[] = new Vector[LayerCount];
    int LayerSize[] = new int[LayerCount];
    Vector<double[]> TrainData = new Vector<double[]>();
    Neuron outNeuron = null;
    Neuron rootNeuron = null;
    
    public void init() {                           
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
            
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(key);
            i++;
        }
            
        //retreiving boundaries
        tok = new StringTokenizer(boundaries.getValue(), ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];
            
        //check if number of parameter ids and boundaries match
        if (n != i) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": Different number of parameterIDs and boundaries!");
        }
            
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            key = key.substring(1, key.length()-1);
                
            StringTokenizer boundTok = new StringTokenizer(key, ">");
            lowBound[i] = Double.parseDouble(boundTok.nextToken());
            upBound[i] = Double.parseDouble(boundTok.nextToken());
                
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
            }
                
            i++;
        }
            
        //initialising output file
        writer = new GenericDataWriter(dirName.getValue()+"/"+outputFileName.getValue());
        writer.addComment("SCE output");
        for(int p = 0; p < this.parameterNames.length; p++){
            writer.addColumn(this.parameterNames[p]);
        }
        writer.addColumn(this.effMethodName.getValue());
        writer.writeHeader();
        writer.flush();                           
        
        n = this.parameters.length;
        currentSampleCount = 0;
        generator.setSeed(1);
        //build network
        rootNeuron = new InputNeuron();
        
        //simple input layer
        Layer[0] = new Vector<Neuron>();
        Layer[1] = new Vector<Neuron>();
        Layer[2] = new Vector<Neuron>();
        Layer[3] = new Vector<Neuron>();
        
        Layer[0].add(rootNeuron);
        
        //parameter layer
        for (int k=0;k<n;k++) {            
            Neuron parameterNeuron = new Neuron();
            Layer[1].add(parameterNeuron);
            
            rootNeuron.AddConnection(parameterNeuron,generator.nextDouble() - 0.5);
        }
        
        //1st hidden layer
        int h1Size = 20;
        for (int k=0;k<h1Size;k++) {            
            Neuron hiddenNeuron = new Neuron(true);
            Layer[2].add(hiddenNeuron);
            for (int r=0;r<n;r++) {            
                Layer[1].get(r).AddConnection(hiddenNeuron,generator.nextDouble() - 0.5);
            }
        }
        
        //output neuron
        outNeuron = new Neuron();
        for (int k=0;k<h1Size;k++) {            
            Layer[2].get(k).AddConnection(outNeuron,generator.nextDouble() - 0.5);
        }
        Layer[3].add(outNeuron);
    }
    
    private void ResetNet(){
        for (int k=0;k<n;k++) {  
            rootNeuron.getConnection(k).Weight = generator.nextDouble() - 0.5;
            Layer[2].get(k).getConnection(0).Weight = generator.nextDouble() - 0.5;
            for (int r=0;r<Layer[2].size();r++) {
                Layer[1].get(k).getConnection(r).Weight = generator.nextDouble() - 0.5;
            }
            
        }
        
    }
    
    private double[] RandomSampler(){
        int paras = this.parameterNames.length;
        double[] sample = new double[paras];
        
        for(int i = 0; i < paras; i++){
            double d = generator.nextDouble();
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }        
        return sample;
    }
    
    public double funct(double x[]) {
        double value = 0;
        
        for (int j=0;j<parameters.length;j++) {
            parameters[j].setValue(x[j]);
        }
        
        //model run
        singleRun();
                                
        if (this.mode.getValue() == MODE_MINIMIZATION)
            return this.effValue.getValue();
        else if (mode.getValue() == MODE_ABSMINIMIZATION)
            return Math.abs(this.effValue.getValue());
        else if (mode.getValue() == MODE_ABSMAXIMIZATION)
            return -Math.abs(this.effValue.getValue());
        else if (mode.getValue() == MODE_MAXIMIZATION)
            return -this.effValue.getValue();
        else
            return 0;
    }
        
    private void singleRun() {    
	this.currentSampleCount++;
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private void collectSamples() {
        for (int i=0;i<SamplesPerIteration;i++) {
            double parameter[] = this.RandomSampler();
            double result   = this.funct(parameter);
            if (result < -10.0)
                result = -10.0;
            if (result >  10.0)
                result =  10.0;
            double sample[] = new double[parameter.length+1];
            for (int k=0;k<parameter.length;k++) {
                sample[k] = parameter[k];
            }
            sample[sample.length-1] = result;
            TrainData.add(sample);
        }
    }
    
    public double Propagate() {
	for (int k=0;k<LayerCount;k++) {
	    for (int i=0;i<this.Layer[k].size();i++) {
		Layer[k].get(i).propagate();
		}
	    }				
	return outNeuron.getActivation();
    }
    
    public void BackPropagate(double error) {
	outNeuron.addToError(error);			
		    
	for (int k=LayerCount-1;k>=0;k--) {
	    for (int i=0;i<Layer[k].size();i++) {
		Layer[k].get(i).backpropagate();
		Layer[k].get(i).updateWeightDelta();		
	    }
	}	
    }
    
    public void AdjustWeights() {
	for (int k=1;k<LayerCount;k++) {
	    for (int i=0;i<Layer[k].size();i++) {
		Layer[k].get(i).adjustWeight();
		}
	    }	
    }
    
    public double Predict(double input[]) {
        rootNeuron.reset();
        rootNeuron.addToInput(1.0);        
        for (int i=0;i<input.length;i++) {            
            rootNeuron.getConnection(i).setLock(true);
            rootNeuron.getConnection(i).Weight = input[i];
        }
        return this.Propagate();
    }
    
    public double Cycle(double TrainingSet[][],boolean train) {
	double accError = 0;
		    
        double parameter[] = new double[n];
        double mean = 0;
        double normalizer = 0;
        for (int p=0;p<TrainingSet.length;p++) {
            mean += TrainingSet[p][n];
        }
        mean /= (double)TrainingSet.length;
        
	for (int p=0;p<TrainingSet.length;p++) {
            for (int r=0;r<n;r++){
                //normalize data
                parameter[r] = 2.0*(TrainingSet[p][r] - this.lowBound[r]) / (this.upBound[r] - this.lowBound[r]) - 1.0;
            }
	    double predValue = Predict(parameter);
	    double correctValue = TrainingSet[p][n];
            	   
	    accError += (correctValue - predValue)*(correctValue - predValue);
	    normalizer += (correctValue-mean)*(correctValue-mean);
            
            if (train) {
                BackPropagate(correctValue - predValue);
            }
	}    	
        if (train) {
            AdjustWeights();
        }
	return 1.0 - accError/normalizer;
    }
            
    private double Train(double TrainingSet[][],double ValidationSet[][]) {
        double TrainingEff = Double.NEGATIVE_INFINITY,
               oldTrainEff = 0.0,
               ValidationEff = 0.0,
               meanValidationEff_older = 0.0,
               meanValidationEff_newer = 0.0,
               meanTrainImprovement = 1.0;
        Vector<Double> ValidationEffHistory = new Vector<Double>();
                
        boolean ValidationImprovement = true;
        Neuron.learningRate = 0.001;
        int cycle = 0;
        ResetNet();
        while ( (TrainingEff < 0.7 || ValidationImprovement || cycle <= 10) && meanTrainImprovement > 0.0000001) {
            
            TrainingEff = Cycle(TrainingSet,true);
            ValidationEff = Cycle(ValidationSet,false);

            if (oldTrainEff == 0.0)
                oldTrainEff = TrainingEff;
            
            ValidationEffHistory.add(ValidationEff);
            
            meanValidationEff_older = 0.1*ValidationEff + 0.9*meanValidationEff_older;
            meanValidationEff_newer = 0.2*ValidationEff + 0.8*meanValidationEff_newer;
            
            meanTrainImprovement = 0.001*(TrainingEff - oldTrainEff) + 0.999*meanTrainImprovement;
                    
            ValidationImprovement = meanValidationEff_newer > meanValidationEff_older;
                            
            cycle++;
            System.out.println("Cycle:" + cycle + "\tTrainEff:" + TrainingEff +  "\tValidationEff:" + ValidationEff + "\timpr:" + meanTrainImprovement );
            if (oldTrainEff > TrainingEff){
                Neuron.learningRate *= 0.99;
            }
            else
                Neuron.learningRate /= 0.999;
            oldTrainEff = TrainingEff;
        }
        return ValidationEffHistory.lastElement().doubleValue();
    }
    
    private double AdjustNet() {
        //perform crossvalidation
        Object tmp[] = (Object[])TrainData.toArray();
        double samples[][] = new double[tmp.length][];
        for (int i=0;i<tmp.length;i++) {
            samples[i] = (double[])tmp[i];
        }
        int count = samples.length;
        
        int folds = 3;
        
        int beginIndex = 0;
        int endIndex   = (int)count/folds;
        
        double error = 0;
        
        for (int k=0;k<folds;k++) {
            //splitt up data
            double TrainingSet[][] = new double[count - (endIndex - beginIndex)][];
            double ValidationSet[][] = new double[endIndex - beginIndex][];
            
            int vCounter = 0,
                tCounter = 0;
            
            for (int r=0;r<count;r++){
                if (r >= beginIndex && r < endIndex) {
                    ValidationSet[vCounter] = samples[r];
                    vCounter++;
                }
                else {
                    TrainingSet[tCounter] = samples[r];
                    tCounter++;
                }                    
            }            
            error += Train(TrainingSet,ValidationSet);                        
        }
        return error / (double)folds;
    }
    
    public void run() {
        n = this.parameters.length;
        Neuron.learningRate = 0.001;
        NeuralConnection.momentum = 0.9;
        NeuralConnection.MAXW = 10.0;
        while (true) {
            collectSamples();
            
            System.out.println("Current error:" + AdjustNet());
            
            //parameter optimieren
        }
    }
               
}
