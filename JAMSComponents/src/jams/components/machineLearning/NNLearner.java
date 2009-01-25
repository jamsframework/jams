/*
 * Learner.java
 *
 * Created on 10. April 2007, 14:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.machineLearning;

import java.io.BufferedWriter;
import java.io.FileWriter;
import jams.data.*;
import jams.model.*;
import java.util.*;
import Jama.*;
import Jama.Matrix;
/*import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;*/

/**
 *
 * @author Christian(web)
 */
public class NNLearner extends Learner {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity trainData;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity validationData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble learningrate;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSDouble momentum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString layers;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSInteger epochen;
                            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString resultFile = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString options = null;
                           
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSBoolean applyLinearRegression;
    
    int LayerCount;
    Vector<Neuron> Layers[];                
    Vector<Integer> LayerSize;
    Matrix R;
    Neuron outNeuron;
    //weka.classifiers.functions.MultilayerPerceptron MLP = null;
    
    @SuppressWarnings("unchecked")
    public NNLearner() {
	normalizeData = true;
	LayerCount = 3;	
	
	Layers = new Vector[LayerCount];                
	LayerSize = new Vector<Integer>();
    }
        
    public void setLayerSize(int option) {
	//this is standard
	if (option == 1) {
	    this.LayerCount = 3;
	    
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer((DataLength+1)/2));
	    this.LayerSize.add(2,1);
	}
	if (option == 2) {
	    this.LayerCount = 4;
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer((DataLength+1))/2);
	    this.LayerSize.add(2,new Integer((DataLength+1))/2);
	    this.LayerSize.add(3,1);
	}
	if (option == 3) {
	    this.LayerCount = 3;
	    
	    this.LayerSize.add(0,new Integer(DataLength)+1);
	    this.LayerSize.add(1,new Integer(2));
	    this.LayerSize.add(2,1);
	}
    }
        
    public void setupNET() {                		
	generator.setSeed(-1);
	
	for (int i=0;i<LayerCount;i++) {
	    Layers[i] = new Vector<Neuron>();
	    Layers[i].setSize(LayerSize.get(i));
	}
														
	//setup layer 0
	for (int i=0;i<LayerSize.get(0).intValue();i++) {
	    InputNeuron inNeuron = new InputNeuron();

	    inNeuron.initalize();
	    Layers[0].set(i,inNeuron);		
	}	    
		
	for (int m=1;m<LayerCount;m++) {	
	    for (int i=0;i<LayerSize.get(m);i++) {
		Neuron innerNeuron  = new Neuron();
		
		LogisticFunction logf = new LogisticFunction(1.0);		
		GenericFunction gf = new GenericFunction(logf);
		
		innerNeuron.initalize();
		if (m != LayerCount - 1)
		    innerNeuron.addFilter(gf);		
		
		Layers[m].set(i,innerNeuron);
		
	/*	if (m==1) {
		if (i == 0) {
		    innerNeuron.AddConnection(Layers[0].get(0),innerNeuron,-0.5);
		    innerNeuron.AddConnection(Layers[0].get(1),innerNeuron,-0.22);
		    innerNeuron.AddConnection(Layers[0].get(2),innerNeuron,0);
		}
		if (i == 1) {
		    innerNeuron.AddConnection(Layers[0].get(0),innerNeuron,-0.44);
		    innerNeuron.AddConnection(Layers[0].get(1),innerNeuron,-0.09);
		    innerNeuron.AddConnection(Layers[0].get(2),innerNeuron,0);
		}
		}
		if (m==2) {
		    innerNeuron.AddConnection(Layers[1].get(0),innerNeuron,0.05);
		    innerNeuron.AddConnection(Layers[1].get(1),innerNeuron,0.16);
		}*/
		
		for (int k=0;k<Layers[m-1].size();k++) {		    
		    innerNeuron.AddConnection(Layers[m-1].get(k),innerNeuron,(generator.nextDouble()*1.0) - 0.5);
		}				
	    }
	}
	
	outNeuron = Layers[LayerCount-1].get(0);
    }
            
    public double Predict(double p[]) {
	double tmp[] = new double[p.length];
	
	if (this.normalizeData) {
	    for (int i=0;i<p.length;i++) {
		tmp[i] = 2.0 * (p[i] - base[i]) / (max[i] - min[i]);
	    }
	}
	else
	    tmp = p;
			
	for (int i=0;i<=this.DataLength;i++) {
	    InputNeuron inNeuron = (InputNeuron)Layers[0].get(i);
	    if (i == this.DataLength)
		inNeuron.SetInput(1.0);
	    else
		inNeuron.SetInput(tmp[i]);
	}
			
	Propagate();
	
	return this.outNeuron.getActivation();//*0.5*(this.pmax - this.pmin) + this.pbase;
    }
    
    public double Propagate() {
	for (int k=0;k<LayerCount;k++) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).propagate();
		}
	    }				
	return outNeuron.getActivation();
    }
    
    public void BackPropagate(double error) {
	outNeuron.addToError(error);			
		    
	for (int k=LayerCount-1;k>=0;k--) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).backpropagate();
		Layers[k].get(i).updateWeightDelta();		
	    }
	}	
    }
    
    public void AdjustWeights() {
	for (int k=1;k<LayerCount;k++) {
	    for (int i=0;i<Layers[k].size();i++) {
		Layers[k].get(i).adjustWeight();
		}
	    }	
    }
    
    public double TrainCycle() {
	double accError = 0;
		    
	for (int p=0;p<TrainLength;p++) {
//	    int ps = this.generator.nextInt(TrainLength);
	    double predValue = Predict(this.data[p]);
	    double correctValue = this.result[p];
	   /* if (this.normalizeData) {
		predValue = 2.0*(predValue)/(pmax-pmin);
		correctValue = 2.0*(correctValue)/(pmax-pmin);
	    }*/
	    accError += Math.abs(correctValue - predValue);
	    
	    BackPropagate(correctValue - predValue);	    
	}    		
	AdjustWeights();
	return accError;
    }
    public double[] Predict(double data[][],double predict[],boolean writeResult) {
	int M = data[0].length;
	int P = data.length;
		
/*	FastVector atts = new FastVector(M+1);
	for (int i=0;i<M+1;i++) {
	    atts.addElement(new Attribute("data" + Integer.toString(i)));	    
	}
	
	Instances dataSet = new Instances("data", atts, P);
	
	for (int i=0;i<P;i++) {
	    double next[] = new double[M+1];
	    for (int j=0;j<M;j++) {
		next[j] = data[i][j];
	    }
	    next[M] = 0;
	    Instance singleInst = new Instance(1,next);	    	    
	    dataSet.add(singleInst);
	}
	dataSet.setClassIndex(M);
	double result[] = new double[P];
	
	Matrix A = new Matrix(data.length,data[0].length+1);
	for (int i=0;i<data.length;i++) {
	    for (int j=0;j<data[0].length;j++) {
		A.set(i,j,data[i][j]);
	    }
	    A.set(i,data[0].length,1.0);	    
	}
	Matrix B2 = null;
	if (this.applyLinearRegression.getValue())
	    B2 = A.times(R);
	
	for (int i=0;i<P;i++) {	   	    	    
	    try {
		if (this.applyLinearRegression.getValue()) {
		    result[i] = B2.get(i,0) + MLP.classifyInstance(dataSet.instance(i));		
		}
		else
		    result[i] = MLP.classifyInstance(dataSet.instance(i));
	    }
	    catch(Exception e) {
		System.out.println("Could not classify instance -> " + e.toString());
	    }
	    //System.out.println(validation_predict[i] + "\t" + result);
	}
			
	if (!writeResult)
	    return result;
	
	BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(this.resultFile.getValue(),true));	    
	}
	catch (Exception e) {
	    System.out.println("Could not open result file, becauce:" + e.toString());
	    System.out.println("results won't be saved");
	}
	    
	for (int i=0;i<P;i++) {			    
	    try {
		writer.write(new String(predict[i] + "\t" + result[i] + "\n"));		    
		writer.flush();
	    }
	    catch(Exception e) {
		System.out.println("could not write, because: " + e.toString());
	    }
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    System.out.println("NN - Error" + e.toString());
	}
	return result;*/return null;
    }
    
    public void Train(double data[][],double predict[]) {
	int M = data[0].length;
	int N = data.length;
	
	
	double predict_linreg[] = null;
	if (applyLinearRegression.getValue() == true) {
	    predict_linreg = LinearRegression(data,predict);
	}
	else
	    predict_linreg = predict;
	
/*	FastVector atts = new FastVector(M+1);
	for (int i=0;i<M+1;i++) {
	    atts.addElement(new Attribute("data" + Integer.toString(i)));	    
	}
	Instances dataSet = new Instances("data", atts, N);

	for (int i=0;i<N;i++) {
	    double next[] = new double[M+1];
	    for (int j=0;j<M;j++) {
		next[j] = data[i][j];
	    }
	    next[M] = predict_linreg[i];
	    Instance singleInst = new Instance(1,next);	    	    
	    dataSet.add(singleInst);
	}
	dataSet.setClassIndex(M);
	MLP = new weka.classifiers.functions.MultilayerPerceptron();
	MLP.setGUI(false);
	MLP.setValidationSetSize(20);
	MLP.setHiddenLayers(this.layers.getValue());
	MLP.setLearningRate(this.learningrate.getValue());
	//MLP.setDecay(true);
	MLP.setMomentum(this.momentum.getValue());
	MLP.setTrainingTime(this.epochen.getValue());
	MLP.setReset(true);	
	try {
	    MLP.buildClassifier(dataSet);
	}
	catch(Exception e) {
	    System.out.println("MLP didn´t want to train ... " + e.toString());
	}*/
	//System.out.println("Trained:" + MLP.);
    }
    
    public double SingleRun(double trainData[][],double trainPredict[],double valData[][],double valPredict[],boolean writeResult)  {
	Train(trainData,trainPredict);
	double result[] = Predict(valData,valPredict,writeResult);
	double MSE = 0;
	
	for (int i=0;i<result.length;i++) {
	    MSE += (valPredict[i] - result[i])*(valPredict[i] - result[i]);
	}
	    
	return MSE;
    }
    
    public double crossvalidation(double data[][],double predict[]) {
	long t1 = System.currentTimeMillis();
		
	int k = 5;
	double error = 0;
	
	int N = data.length;
	int M = data[0].length;
	int d = N / k;
	//aufrunden
	if (d * k != N) {
	    d += 1;
	}
	//split up data
	for (int i=0;i<k;i++) {
	    int trainCounter = 0;
	    int valCounter = 0;
	    	    	    	    
	    //testrun
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valCounter++;
		}
		else {		    		    
		    trainCounter++;
		}
	    }	    
	    double valData[][] = new double[valCounter][];
	    double valPredict[] = new double[valCounter]; 
	    double trainData[][] = new double[trainCounter][];	    	    
	    double trainPredict[] = new double[trainCounter]; 
	    trainCounter = 0;
	    valCounter = 0;
	    
	    for (int j=0;j<N;j++) {
		if ( (j / d) == i) {
		    valData[valCounter] = data[j];
		    valPredict[valCounter] = predict[j];
		    valCounter++;
		}
		else {		    		    
		    trainData[trainCounter] = data[j];
		    trainPredict[trainCounter] = predict[j];
		    trainCounter++;
		}
	    }	    
	    error += SingleRun(trainData,trainPredict,valData,valPredict,false);	    
	}   
	long t2 = System.currentTimeMillis();
	
	//System.out.println("CV - Time:" + (double)(t2 - t1) / 1000.0);
	return error;
    }
    
    public void SetParams(int EpochCounter,double LearningRate, double Momentum) {	
	if (EpochCounter <= 0) EpochCounter = 1;
	if (LearningRate <= 0.0)   LearningRate = 0.01;
	if (Momentum < 0.0) Momentum = 0.01;
	
	if (this.epochen == null)	this.epochen = JAMSDataFactory.getInteger();
	if (this.learningrate == null)  this.learningrate = JAMSDataFactory.getDouble();
	if (this.momentum == null)	this.momentum = JAMSDataFactory.getDouble();
	
	this.epochen.setValue(EpochCounter);
	this.learningrate.setValue(LearningRate);
	this.momentum.setValue(Momentum);

    }
        
    public void optimize(double data[][],double predict[]) {
	//5 fold cross validation
	System.out.println("Optimization");
	boolean noImprovement = false;
	
	double EpochCounter = 1000;
	double LearningRate = 0.3;
	double Momentum = 0.2;
	
	double delta = 0.001;
	double alpha_min = 0.0001;
	
	int alpha_Epoch = 10;
	double alpha_lrate = 0.1,alpha_mom = 0.1;
		
	int igradient;
	double gradient;
		
	double y_neu;
	double y_best = 100000000.0;
	double y_tmp;
	double y_best_alt = 100000000000.0;
	
	int Counter = 0;
	
	double bestEpoch = 0;
	
	do {	    	    
	    SetParams( (int)EpochCounter,LearningRate,Momentum);
	    y_best = y_neu = crossvalidation(data,predict);
	    y_best_alt = y_best;
	    System.out.println("Startwert:" + y_best_alt);
	    /*//epoch
	    //calculate gradient
	    /*SetParams( (int)EpochCounter+1,LearningRate,Momentum);
	    y_tmp = crossvalidation(data,predict);
	    if (y_tmp < y_neu) {
		igradient = 1;
	    }
	    else {
		igradient = -1;	    	    
	    }
	    alpha_Epoch = 4*alpha_Epoch + 2;
	    double ysave = y_neu;
	    
	    do {
		alpha_Epoch /= 2;
		if (alpha_Epoch <= 1) {
		    alpha_Epoch = 0;
		    y_neu = ysave;
		    break;		    
		}
		SetParams( (int)EpochCounter+igradient*alpha_Epoch,LearningRate,Momentum);
		y_neu = crossvalidation(data,predict);
	    }while (y_neu >= y_best);
		
	    EpochCounter+=igradient*alpha_Epoch;
	    y_best = y_neu;
	    System.out.println("Epochen:" + EpochCounter + "\tLernrate:" + LearningRate +"\tMomentum:" + Momentum + "\tyBest:" + y_best);
	    */
	    //learning rate
	    //calculate gradient
	    SetParams( (int)EpochCounter,LearningRate+0.01,Momentum);
	    y_tmp = crossvalidation(data,predict);
	    if (y_tmp < y_neu) {
		gradient = 1.0;
	    }
	    else {
		gradient = -1.0;	    	    
	    }
	    alpha_lrate = 4.0*alpha_lrate + 0.05;
	    double ysave = y_neu;
	    do {
		do 
		    alpha_lrate /= 2;
		while (LearningRate+alpha_lrate*gradient <= 0.0);

		if (alpha_lrate <= 0.0001) {
		    alpha_lrate = 0.0;
		    y_neu = ysave;
		    break;
		}		
		SetParams( (int)EpochCounter,LearningRate+alpha_lrate*gradient,Momentum);
		y_neu = crossvalidation(data,predict);
	    }while (y_neu >= y_best);
	    y_best = y_neu;
	    LearningRate+=alpha_lrate*gradient;	    
	    System.out.println("Epochen:" + EpochCounter + "\tLernrate:" + LearningRate +"\tMomentum:" + Momentum + "\tyBest:" + y_best);
	    //momentum
	    //calculate gradient
	    SetParams( (int)EpochCounter,LearningRate,Momentum+0.01);
	    y_tmp = crossvalidation(data,predict);
	    if (y_tmp < y_neu) {
		gradient = 1.0;
	    }
	    else {
		gradient = -1.0;	    	    
	    }
	    alpha_mom = 4.0*alpha_mom + 0.1;
	    ysave = y_neu;
	    do {
		do
		    alpha_mom /= 2;
		while (Momentum+alpha_mom*gradient <= 0.0);
		    		
		if (alpha_mom <= 0.0001) {
		    alpha_mom = 0.0;
		    y_neu = ysave;
		    break;
		}		
		SetParams( (int)EpochCounter,LearningRate,Momentum+alpha_mom*gradient);
		y_neu = crossvalidation(data,predict);
	    }while (y_neu > y_best);
	    y_best = y_neu;
	    Momentum+=alpha_mom*gradient;
	    System.out.println("Epochen:" + EpochCounter + "\tLernrate:" + LearningRate +"\tMomentum:" + Momentum + "\tyBest:" + y_best);
	    System.out.println("Verbessung in diesem Durchgang:" + y_best_alt / y_best);
	    Counter++;
	}while (y_best_alt / y_best >1.05 || Counter <= 3);
    }
    
    double[] LinearRegression(double data[][], double predict[]) {
	Matrix A = new Matrix(data.length,data[0].length+1);
	Matrix B = new Matrix(data.length,1);
	
	for (int i=0;i<data.length;i++) {
	    for (int j=0;j<data[0].length;j++) {
		A.set(i,j,data[i][j]);
	    }
	    A.set(i,data[0].length,1.0);
	    B.set(i,0,predict[i]);
	}
	Matrix K = (A.transpose()).times(A);
	B = A.transpose().times(B);
	
	CholeskyDecomposition C = K.chol();
	if (!C.isSPD()) {
	    System.out.println("Error!! not a SPD - Matrix");
	}	
	R = C.solve(B);
	
	Matrix B2 = A.times(R);
	double result[] = new double[predict.length];
	for (int i=0;i<data.length;i++) {
	    result[i] = predict[i] - B2.get(i,0);
	} 
	return result;
    }
    public void layerTest(double data[][],double predict[],double validation_data[][], double validation_predict[]) {
	//layer ausprobieren!!
	for (int i=1;i<40;i+=2) {
	    //for (int j=1;j<20;j+=2) {
	    this.resultFile.setValue(options.toString() + "_result_" + Integer.toString(i)/* + "_" + Integer.toString(j)*/ + ".txt");
	    this.layers.setValue(Integer.toString(i)/* + "," + Integer.toString(j)*/);
	    //optimize(data,predict);
	    this.Train(data,predict);
	    this.Predict(validation_data,validation_predict,true);
	    //}
	}
    }
    public void run() {			
	double data[][] = null;
	double predict[] = null;
	
	double validation_data[][] = null;
	double validation_predict[] = null;
	try {
	    data = (double[][])trainData.getObject("data");
	    predict = (double[])trainData.getObject("predict");
	    
	    validation_data = (double[][])validationData.getObject("data");
	    validation_predict = (double[])validationData.getObject("predict");
	} catch(Exception e) {
	    System.out.println("could not find data!!" + e.toString());
	}
			
	//layerTest(data,predict,validation_data,validation_predict);
	
	
	this.Train(data,predict);
	this.Predict(validation_data,validation_predict,true);
    }
}
