/*
 * GaussianLearner.java
 *
 * Created on 16. April 2007, 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package optas.datamining;


import jams.data.*;
import jams.model.*;
import java.io.*;
import Jama.*;
import Jama.Matrix;
import optas.datamining.kernels.*;
import jams.tools.FileTools;

public class GaussianLearner extends Learner  {
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSEntity optimizationData;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Integer kernelMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Integer MeanMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.Integer PerformanceMeasure;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.DoubleArray param_theta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public JAMSString parameterFile = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "TimeSerie of Temp Data"
            )
            public Attribute.String resultFile = null;
                          
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "component mode",
            defaultValue="0"
            )
            public Attribute.Integer mode;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "file containing model",
            defaultValue=""
            )
            public JAMSString modelDataFile;
    
    Matrix CovarianzMatrix;
    LUDecomposition Solver;
    CholeskyDecomposition fastSolver = null;
            
    Matrix Observations;
    Matrix alpha;
    Matrix invCovarianzMatrix;
               
    double logtheta[];    
    double theta[];
    double meantheta[];
                        
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;  
    static final int ABSMINIMIZATION = 4;
     
    int MaximizeEff = 1;
    
    Kernel kernel;
    String kernelParameterNames[];
    static final double resolution = 0.001;
    static final double limit = 20;
    static double gaussianDistribution[];

    
    public static final int MODE_TRAIN = 0;
    public static final int MODE_APPLYMODEL = 1;
    public static final int MODE_OPTIMIZE = 2;
    
    public double getMarginalLikelihood() {
	double n = this.TrainLength;
	double term1 = -0.5*Observations.transpose().times(this.alpha).get(0,0);		
	double term2 = 0;
        Matrix L = null;
        
        //todo .. how to determine term2 without solver .. only by using inverse cov matrix?
        
	if (fastSolver != null)
            L = fastSolver.getL();
        else if (Solver != null)
            L = Solver.getL();
        else{
            L = CovarianzMatrix.lu().getL();
        }
	
	for (int i=0;i<L.getColumnDimension();i++) {
	    term2 += 2.0 * Math.log(L.get(i,i));
	}
	
	term2 = -0.5*term2;
	double term3 = -n/2.0 * Math.log(2*Math.PI);
	return term1 + term2 + term3;
    }
    
    public double getLOOlogPredictiveProbability() {	
	if (invCovarianzMatrix == null)
            invCovarianzMatrix = this.CovarianzMatrix.inverse();
	double logp = 0;
	
	for (int i=0;i<TrainLength;i++) {
	    //leave i out
	    double mu_i = this.Observations.get(i,0) - (alpha.get(i,0)/ invCovarianzMatrix.get(i,i));
	    double sigma_i = 1.0 / invCovarianzMatrix.get(i,i);

	    if (sigma_i < 0)
		sigma_i = -sigma_i;
	    //sigma_i = sigma_i*sigma_i;
	    sigma_i = Math.sqrt(sigma_i);
	    
	    logp += (- Math.log(sigma_i) - Math.pow((this.Observations.get(i,0) - mu_i)/sigma_i,2.0)/2.0 /* - 0.5*Math.log(2.0*Math.PI)*/);
	}
	return logp;
    }
    
    public double getLOOSquareError() {	
	if (invCovarianzMatrix == null)
            invCovarianzMatrix = this.CovarianzMatrix.inverse();
	double error = 0;
	
	for (int i=0;i<TrainLength;i++) {
	    //leave i out
	    double mu_i = this.Observations.get(i,0) - alpha.get(i,0)/ invCovarianzMatrix.get(i,i);
	    
	    //System.out.println("Leave " + i + " out -> " + mu_i + "  observation -> " + this.Observations.get(i,0));
	    error += (mu_i - this.Observations.get(i,0))*(mu_i - this.Observations.get(i,0));
	}
	return -error;
    }        
    
    public double getSplitValidationError() {
	double result[] = null;
	double correctValue[] = null;
	try {
	    result = this.Predict(false);
	    correctValue = ((double[])this.validationData.getObject("predict"));
	}
	catch(Exception e) {
            this.getModel().getRuntime().sendInfoMsg("error occured, while calculating performance measure " + e.toString());
	}
	double sum = 0.0;
	for (int i=0;i<result.length;i++) {
	    sum += (result[i] - correctValue[i])*(result[i] - correctValue[i]);
	}
	return -sum;
    }
    //das macht einen kleinen unterschied zum normalen training, weil die eingabedaten nicht mit dem
    //neuen datensatz normiert werden! .. im normalfall sollte dieser kleine fehler aber vernachl^ssigbar
    //gering sein!
    public double RetrainWithANewObservation(int PerformanceMeasure,double[] input,double obs){
        Matrix oldCovarianzMatrix = CovarianzMatrix;
        CovarianzMatrix = new Matrix(TrainLength+1,TrainLength+1);
        Observations = new Matrix(TrainLength+1,1);        
        
        double data_new[][] = new double[data.length+1][data[0].length];
        double result_new[] = new double[result.length+1];
        
        for (int i=0;i<TrainLength;i++){
            data_new[i] = data[i];
            result_new[i] = result[i];
        }
        data_new[TrainLength] = input;
        result_new[TrainLength] = obs;
        
        data = data_new;
        result = result_new;
        
        CovarianzMatrix.setMatrix(0,TrainLength-1,0,TrainLength-1,oldCovarianzMatrix);
        
        Matrix u1 = new Matrix(TrainLength+1,1),
               v1 = new Matrix(1,TrainLength+1),
               u2 = new Matrix(TrainLength+1,1), 
               v2 = new Matrix(1,TrainLength+1);
        
        for (int i=0;i<TrainLength;i++) {	
            double varianz = this.kernel.kernel(normalize(data[i]),normalize(input),i,TrainLength);
            v1.set(0,i,0.0);
            u1.set(i,0,varianz);
            v2.set(0,i,varianz);
            u2.set(i,0,0.0);
            CovarianzMatrix.set(i,TrainLength,varianz);
            CovarianzMatrix.set(TrainLength,i,varianz);            	    	    
	}	
        double varianz = this.kernel.kernel(normalize(input),normalize(input),TrainLength,TrainLength);
        CovarianzMatrix.set(TrainLength,TrainLength,varianz);	    
        
        v1.set(0,TrainLength,1.0);
        u1.set(TrainLength,0,(varianz-1.0)/2.0 );
        v2.set(0,TrainLength,(varianz-1.0)/2.0 );
        u2.set(TrainLength,0,1.0);
        
        Observations = this.kernel.MM.Transform(data,result);
        
        //update inverse matrix
        if (invCovarianzMatrix == null){
            invCovarianzMatrix = CovarianzMatrix.inverse();            
        }else{
            //proofed, this is correct
            /*Matrix biggerInvCovarianzMatrix = Matrix.identity(TrainLength+1,TrainLength+1);
            biggerInvCovarianzMatrix.setMatrix(0,TrainLength-1,0,TrainLength-1,invCovarianzMatrix);
            
            Matrix vTA1 = v1.times(biggerInvCovarianzMatrix);
            
            double a = 1.0 / (1.0 + vTA1.times(u1).get(0,0));
            
            invCovarianzMatrix = biggerInvCovarianzMatrix.minus(biggerInvCovarianzMatrix.times(u1).times(vTA1).times(a));
            
            Matrix vTA2 = v2.times(invCovarianzMatrix);
            
            a = 1.0 / (1.0 + vTA2.times(u2).get(0,0));
            
            invCovarianzMatrix = invCovarianzMatrix.minus(invCovarianzMatrix.times(u2).times(vTA2).times(a));            */
            
            //faster version:
            double a = 1.0 / (1.0 + u1.get(TrainLength,0));
          
            Matrix biggerInvCovarianzMatrix = Matrix.identity(TrainLength+1,TrainLength+1);
            biggerInvCovarianzMatrix.setMatrix(0,TrainLength-1,0,TrainLength-1,invCovarianzMatrix);
                                    
            Matrix invTu1 = biggerInvCovarianzMatrix.times(u1);
            for (int i=0;i<TrainLength+1;i++){
                biggerInvCovarianzMatrix.set(i,TrainLength,
                        biggerInvCovarianzMatrix.get(i,TrainLength)-invTu1.get(i,0)*a);
            }
            invCovarianzMatrix = biggerInvCovarianzMatrix;
            
            Matrix vTA2 = v2.times(invCovarianzMatrix);
            
            a = 1.0 / (1.0 + vTA2.get(0,TrainLength));
            
            Matrix invTu2 = invCovarianzMatrix.getMatrix(0,TrainLength,TrainLength,TrainLength).times(a);
            invCovarianzMatrix = invCovarianzMatrix.minus(invTu2.times(vTA2));      
        }
        
        alpha = invCovarianzMatrix.times(Observations);
        TrainLength++;
        
        switch (PerformanceMeasure) {
	    case 1: return getMarginalLikelihood();
	    case 2: return this.getLOOlogPredictiveProbability();
	    case 3: return this.getLOOSquareError();
	    case 4: return this.getSplitValidationError();
	    default: return 0.0;
	}
        
    }
    
    public double Train(int PerformanceMeasure) {
	CovarianzMatrix = new Matrix(TrainLength,TrainLength);
	Observations = new Matrix(TrainLength,1);        
	theta = new double[this.kernel.getParameterCount()];
        meantheta = new double[this.kernel.MM.GetParameterCount()];
	logtheta = new double[this.kernel.getParameterCount()];
	invCovarianzMatrix = null;
        
	//read params from file
	if ((this.mode.getValue() & MODE_OPTIMIZE)==0 && this.parameterFile != null && param_theta == null) {
	    BufferedReader reader;
	    try {
		reader = new BufferedReader(new FileReader(FileTools.createAbsoluteFileName(this.getModel().getWorkspaceDirectory().getAbsolutePath(),
                        this.parameterFile.getValue())));
		for (int i=0;i<theta.length;i++) {
		    logtheta[i] = (new Double(reader.readLine()).doubleValue());	
		}
                for (int i=0;i<meantheta.length;i++) {
		    meantheta[i] = (new Double(reader.readLine()).doubleValue());	
		}
		reader.close();
	    }
	    catch (Exception e) {
                this.getModel().getRuntime().sendInfoMsg("Could not open or read parameter file, becauce:" + e.toString());
	        for (int i=0;i<logtheta.length;i++){
                    logtheta[i] = 0.0;
                }                
	    }	
	}else{
            for (int i=0;i<logtheta.length;i++){
                logtheta[i] = 0.0;
            }                
        }
        if (!this.kernel.MM.isTrained()){
            this.kernel.MM.create(data, result);
            this.meantheta = this.kernel.MM.GetParameters();
        }
	//try to use parameters directly
	if (param_theta != null) {
	    for (int i=0;i<logtheta.length;i++) {
		logtheta[i] = Math.log(param_theta.getValue()[i]);
	    }
            for (int i=0;i<this.kernel.MM.GetParameterCount();i++) {
		meantheta[i] = param_theta.getValue()[logtheta.length+i];
	    }
	}
	
	for (int i=0;i<this.theta.length;i++) {
	    theta[i] = Math.exp(logtheta[i]);
	}
	if (!this.kernel.SetParameter(theta)) {
            this.getModel().getRuntime().sendInfoMsg("covariance function has more parameters than specified!");	    
	}
        if (!this.kernel.MM.SetParameters(meantheta)) {
            this.getModel().getRuntime().sendInfoMsg("mean function has more parameters than specified!");	    
	}
			
	for (int i=0;i<TrainLength;i++) {	    
	    for (int j=0;j<i;j++) {
		//calculate covariance for xi,xj
		double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[j]),i,j);
		CovarianzMatrix.set(i,j,varianz);
		CovarianzMatrix.set(j,i,varianz);
	    }
	    double varianz = this.kernel.kernel(normalize(data[i]),normalize(data[i]),i,i);
	    CovarianzMatrix.set(i,i,varianz);	    
	}	        
	Observations = this.kernel.MM.Transform(data,result);
        this.fastSolver = CovarianzMatrix.chol();
        if (!fastSolver.isSPD()){
            fastSolver = null;
            this.getModel().getRuntime().sendInfoMsg("current covariancematrix is not SPD, using LU decomposition instead");	    
            Solver = CovarianzMatrix.lu();
            if (!Solver.isNonsingular()) {
                this.getModel().getRuntime().sendInfoMsg("current covariancematrix is singular, can`t create model with this dataset/parameter combination!");	    
                return -1000000000000.0;
            }
        }	
        if(fastSolver==null)
            alpha = Solver.solve(Observations);
        else
            alpha = fastSolver.solve(Observations);
		
	switch (PerformanceMeasure) {
	    case 1: return getMarginalLikelihood();
	    case 2: return this.getLOOlogPredictiveProbability();
	    case 3: return this.getLOOSquareError();
	    case 4: return this.getSplitValidationError();
	    default: return 0.0;
	}	
    }

    public double getMean(double x[]) {
        
        Matrix kstar = new Matrix(1,TrainLength);
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,variance);
        }
        
        Matrix prediction = (kstar.times(alpha));
        
        double x_tmp[][] = new double[1][];
        x_tmp[0] = x;
        
        double result[] = this.kernel.MM.ReTransform(x_tmp,prediction);
        
        return result[0];
    }
        
    public double[] getVariance(double x[][]){
        double predicted_variance[] = new double[x.length];
        for (int i=0;i<x.length;i++){
            predicted_variance[i] = this.getVariance(x[i]);
        }
        return predicted_variance;
    }
    
    public double getVariance(double x[]) {
        
        Matrix kstar = new Matrix(1,TrainLength);
        Matrix kstarT = new Matrix(TrainLength,1);
        
        Matrix one = new Matrix(1,TrainLength);
        Matrix oneT = new Matrix(TrainLength,1);
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,variance);
            kstarT.set(i,0,variance);
            
            one.set(0,i,1.0);
            oneT.set(i,0,1.0);            
        }                  
        Matrix RMinus1r = null;
        Matrix rRMinus1r = null;
        Matrix RMinus1Eins = null;
        Matrix EinsRMinus1Eins = null;    
                
        if (invCovarianzMatrix != null){
            RMinus1r = this.invCovarianzMatrix.times(kstarT);                        
            RMinus1Eins = this.invCovarianzMatrix.times(oneT);            
        }
        else if (fastSolver != null){
            RMinus1r = fastSolver.solve(kstarT);                    
            RMinus1Eins = fastSolver.solve(oneT);            
        }else{
            RMinus1r = Solver.solve(kstarT);                    
            RMinus1Eins = Solver.solve(oneT);
        }
            
        rRMinus1r = (kstar.times(RMinus1r));
        EinsRMinus1Eins = (one.times(RMinus1Eins));
        
        double t = 1.0 - rRMinus1r.get(0,0);
        double tOne = EinsRMinus1Eins.get(0,0);
                
        double my_hat = one.times(alpha).get(0,0) / tOne;
        
        Matrix tmp = new Matrix(1,TrainLength);
        for (int i=0;i<TrainLength;i++) {
            tmp.set(0,i,Observations.get(i,0)-my_hat);
        }
        double k = (double)this.Observations.getRowDimension();
        
        Matrix solveTmp = null;
        if (this.invCovarianzMatrix != null){
            solveTmp = this.invCovarianzMatrix.times(tmp.transpose());
        }else if (fastSolver == null){
            solveTmp = Solver.solve(tmp.transpose());
        }else{
            solveTmp = fastSolver.solve(tmp.transpose());
        }
        
        double my_sigma = tmp.times(solveTmp).get(0,0) / k;
                
        //double sigma2 = 1.0 - kstar.times(Solver.solve(kstarT)).get(0,0);
        double sigma2 = 1.0 - kstar.times(RMinus1r).get(0,0);
                
        return Math.abs(my_sigma)*Math.sqrt(sigma2 + sigma2*sigma2 / tOne);                  
    }
    
    static public double NormalDensityFunction(double a){
        return (1.0/Math.sqrt(2*Math.PI))*Math.exp(-0.5*a*a);
    }
    
    static public void BuildGaussDistributionTable(){
        double x1 = 0;
        double x2 = resolution;
                
        gaussianDistribution = new double[(int)(limit/resolution)+1];
        int counter = 0;
        double integral = 0.5;
        while (x1 < limit){
            //Simpsonsche Formel
            integral += (x2 - x1)/6.0 * (NormalDensityFunction(x1) + 4*NormalDensityFunction(0.5*(x1+x2))+NormalDensityFunction(x2));
            gaussianDistribution[counter++] = integral;
            x1 = x2;
            x2 = x2 + resolution;
        }
    }

    public double CumulativeNormalDistributionFunction(double x){        
        long index = (long)((Math.abs(x)/limit)*(double)(gaussianDistribution.length));
        double probability = 0.0;
        if (index >= (long)gaussianDistribution.length){
            //System.out.println("gp out of range!!");
            probability = 1.0;
            }
        else{            
            probability = gaussianDistribution[(int)index];
            }
        
        if (x < 0){
            probability = 1.0 - probability;
        }       
        return probability;
    }
    
    //probability for value of f(x) < y
    public double getProbabilityForXLessY(double x[],double target){
        double mean = getMean(x);
        //variancecontrol, because probability decreases very fast at edges of distrubtion
        double variance = getVariance(x);
        
        if (variance < 0.00001)
            return -1000;
                
        //transform to 0/1 distriburion        
        target = target - mean;                            
        target /= variance;       
                       
        return CumulativeNormalDistributionFunction(target);
    }
    
    public double getExpectedImprovement(double x[],double fmin){
        double mean = getMean(x);
        //variancecontrol, because probability decreases very fast at edges of distrubtion
        double variance = getVariance(x);
        
        if (variance < 0.00001)
            return -1000;
                
        //transform to 0/1 distriburion        
        double u = (fmin - mean) / variance;  
        
        return variance*(CumulativeNormalDistributionFunction(u)*u + NormalDensityFunction(u));
    }
    
    public double getMarginalLikelihoodWithAdditionalSample(double x[],double value){
        if (invCovarianzMatrix == null)
            invCovarianzMatrix = this.CovarianzMatrix.inverse(); 
                        
        Matrix kstar = new Matrix(TrainLength,1);        
        Matrix one = new Matrix(TrainLength,1);        
        
        for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
            double variance = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(i,0,variance);                          
            one.set(i,0,1.0);                          
        }   
                        
        Matrix Zu = invCovarianzMatrix.times(kstar);
        double uZu = 1.0/(1.0 + kstar.transpose().times(invCovarianzMatrix.times(kstar)).get(0,0));
        
        //sherman morrision
        //(Z + uv^T)^-1 = Z-1 - (Z^-1*uvT*Z^-1)/(1+vTZ^-1u)
        Matrix modifiedInvCovarianzMatrix = invCovarianzMatrix.minus(Zu.times(Zu.transpose()).times(uZu));
                
        double mean = one.transpose().times(modifiedInvCovarianzMatrix.times(Observations)).get(0,0) / one.transpose().times(one).get(0,0);
        
        Matrix modifiedObservations = Observations.minus(one.times(mean).plus(kstar.times(value-mean)));
        
        double n = this.TrainLength;
	double term1 = -0.5*modifiedObservations.transpose().times(modifiedInvCovarianzMatrix.times(modifiedObservations)).get(0,0);		
	double term2 = -0.5*Math.log(modifiedInvCovarianzMatrix.det()) ;		
	double term3 = -n/2.0 * Math.log(2*Math.PI);
	return (term1 + term2 + term3);        
    }
    
    public double[] Predict(boolean writeOutput) {	
	double x[][] = null;	
	try {
	    x = (double[][])validationData.getObject("data");	    
	}catch(Exception e) {
            this.getModel().getRuntime().sendInfoMsg("there are no datasets for validation!" + e.toString());
            return null;	    
	}
        double result[] = Predict(x);
        
        if (!writeOutput)
            return result;
        
        this.writePrediction(result, this.getCorrectValues(), this.getVariance(x));
	                                        
	return result;
    }  
    
    public double Predict(double[] x){
        Matrix kstar = new Matrix(1,TrainLength);                
        
        for (int i=0;i<TrainLength;i++) {	    
	//calculate covariance for xi,x
            double varianz = this.kernel.kernel(normalize(data[i]),normalize(x),i,-1);
            kstar.set(0,i,varianz);
        }
	Matrix prediction = (kstar.times(alpha));
        
        double xx[][] = new double[1][2];
        xx[0][0] = x[0];
        xx[0][1] = x[1];
        
        return this.kernel.MM.ReTransform(xx,prediction)[0];                
    }
    
    public double[] Predict(double[][] x){
        Matrix kstar = new Matrix(x.length,TrainLength);                
        
        for (int j=0;j<x.length;j++){
            for (int i=0;i<TrainLength;i++) {	    
            //calculate covariance for xi,x
                double varianz = this.kernel.kernel(normalize(data[i]),normalize(x[j]),i,-1);
                kstar.set(j,i,varianz);
            }
        }
	Matrix prediction = (kstar.times(alpha));
                                
        return this.kernel.MM.ReTransform(x,prediction);                
    }
    
    public void writePrediction(double prediction[], double correctValue[], double variance[]){
        BufferedWriter writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(FileTools.createAbsoluteFileName(this.getModel().getWorkspaceDirectory().getAbsolutePath(),
                    this.resultFile.getValue()),true));	    
	}
	catch (Exception e) {
            this.getModel().getRuntime().sendHalt("could not open result-file, because:" + e.toString() + "\nresults won't be saved!");
            return;	    
	}
	    
	for (int i=0;i<prediction.length;i++) {			    
	    try {
                if (correctValue != null)
                    writer.write(correctValue[i] + "\t");	
                if (variance != null)
                    writer.write(variance[i] + "\t");	
                writer.write(prediction[i]+"\n");		
		writer.flush();
	    }
	    catch(Exception e) {
                this.getModel().getRuntime().sendHalt("could not write to result-file, because:" + e.toString());
                return;
	    }
	}
	try {
	    writer.close();
	}
	catch(Exception e) {
	    this.getModel().getRuntime().sendHalt("Error occured while closing result file" + e.toString());
            return;
	}
    }
    
    public double[] getCorrectValues(){
        double correctValue[] = null;
        try{
            correctValue = (double[])validationData.getObject("predict");
        }catch(Exception e) {
            return null;
	}
        return correctValue;        
    }
            
    
    
    public void deserializeModel() throws IOException, ClassNotFoundException{
        String file = this.getModel().getWorkspacePath() + "/" + this.modelDataFile.getValue();
        ObjectInputStream objOut = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));        
        this.min = (double[])objOut.readObject();
        this.max = (double[])objOut.readObject();
        this.base = (double[])objOut.readObject();
        this.data = (double[][])objOut.readObject();
        this.alpha = (Jama.Matrix)objOut.readObject();
        this.Observations = (Jama.Matrix)objOut.readObject();
        TrainLength = data.length;
        //create dummy kernel
        this.kernel    = (optas.datamining.kernels.Kernel)objOut.readObject();
        this.kernel.MM = (optas.datamining.kernels.MeanModell)objOut.readObject();
        Object objSolver = objOut.readObject();
        if (objSolver instanceof CholeskyDecomposition)
            fastSolver = (CholeskyDecomposition)objSolver;
        else
            Solver = (LUDecomposition)objSolver;
        this.invCovarianzMatrix = null;
        objOut.close();      
    }
    
    //write model        
    public void serializeModel() throws IOException{
        String file = this.getModel().getWorkspacePath() + "/" + this.modelDataFile.getValue();
        ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));                
        objOut.writeObject(this.min);
        objOut.writeObject(this.max);
        objOut.writeObject(this.base);
        objOut.writeObject(this.data);
        objOut.writeObject(this.alpha);
        objOut.writeObject(this.Observations);
        objOut.writeObject(this.kernel);
        objOut.writeObject(this.kernel.MM);       
        if (this.fastSolver != null){
            objOut.writeObject(fastSolver);
        }else
            objOut.writeObject(Solver);                
        objOut.flush();
        objOut.close();                
    }
                        
    public void optInit() {
	super.trainData = this.optimizationData;
	super.validationData = this.validationData;
		
	try {
	    super.run();
	}
	catch (Exception e) {
	    this.getModel().getRuntime().sendHalt("Error occured while initializing Gaussian Process Module" + e.toString());
            return;
	}			
    }
                
    public void trainInit() {                
	super.trainData = this.trainData;
	super.validationData = this.validationData;
		                
	try {
	    super.run();
	}
	catch (Exception e) {
            this.getModel().getRuntime().sendHalt("Error occured while initializing Gaussian Process Module" + e.toString());
            return;
	}			
    }
                                            
    public double funct(double x[]) {
        double value = 0;
        
	if (param_theta == null) {
	    param_theta = DefaultDataFactory.getDataFactory().createDoubleArray();
	    double array[] = new double[x.length+kernel.MM.GetParameterCount()];
	    param_theta.setValue(array);
	}
	
	for (int j=0;j<x.length;j++) {	    
	    param_theta.getValue()[j] = Math.exp(x[j]);
	}
        for (int j=0;j<kernel.MM.GetParameterCount();j++) {	    
            param_theta.getValue()[j+kernel.getParameterCount()] = kernel.MM.GetParameters()[j];
        }
        
        double performance = this.Train(this.PerformanceMeasure.getValue());

        if (MaximizeEff == MINIMIZATION)
            return performance;
        else if (MaximizeEff == ABSMINIMIZATION)
            return Math.abs(performance);
        else if (MaximizeEff == ABSMAXIMIZATION)
            return -Math.abs(performance);
        else if (MaximizeEff == MAXIMIZATION)
            return -performance;
        else
            return 0;
    }
         
    public void GradientDescent(double x[],String paramName[]) {
	double y1,y2,diff;						
	double [] grad = new double[x.length];		
	double [] alpha = new double[x.length];		
	double xp[] = new double[x.length];
	
	double alpha_min = 0.001;
	double diff_min = 0.025;
	double approxError = 0.0001;
		
	diff  = 1.0;
	
	y1 = funct(x);
        
	double y_alt;
	double y_neu = 1.0;
	double calpha = 0.1;
	
	for (int i=0;i<x.length;i++) {
	    alpha[i] = 0.1;
	}
	this.getModel().getRuntime().sendInfoMsg("Performing Gradient Descent Optimization!");
        this.getModel().getRuntime().sendInfoMsg("starting with function value:" + y1);
        int iteration = 0;
	while ( calpha > alpha_min && diff > diff_min ) {	
            iteration++;
            this.getModel().getRuntime().sendInfoMsg("iteration:" + iteration);
	    y_alt = y1;
	    //partial differences quotients
	    for (int i=0; i < x.length; i++) {	
		if (alpha[i] == 0) {
		    continue;
		}
	        for (int j=0; j < x.length; j++) {	
		    if (j == i) {
		        xp[j] = x[j]+approxError;			
		    }		    
		    else
		        xp[j] = x[j];			
		}
						
		y2 = funct(xp);		    		    	
		grad[i] = ((y2 - y1) / approxError);    
		
		if (grad[i] < 0) grad[i] = -1.0;
		else		 grad[i] = 1.0;
		//use armijo - method to obtain step width
		//decrease step - width until result is better than the last one
		
		//try to increase step - width
		alpha[i] *= 4.0;
		if (alpha[i] >= 2.0) alpha[i] = 2.0;
		while (true) {		
		    for (int k=0; k < x.length; k++) {	
			xp[k] = x[k];
			if (k==i) {
			     xp[k] = x[i] - alpha[i]*grad[i];
			     
			     if (xp[k] < -10.0)	xp[k] = -10.0;
			     if (xp[k] >  10.0)	xp[k] =  10.0;
			}
		    }
		
		    y_neu = funct(xp);
		
		    if (y_neu < y1)
			break;

		    alpha[i] /= 2.0;
		
		    if (alpha[i] < alpha_min) {
			xp[i] = x[i];
			alpha[i] = 0;
			y_neu = funct(xp);
			break;
		    }
		}
		y1 = y_neu;	
                for (int k=0; k < x.length; k++) {	
                    x[k] = xp[k];                    
                }	                
	    }		    

            String info = "current parameter - set:\n";		
            for (int k=0; k < x.length; k++) {	
                x[k] = xp[k];
                info += paramName[k] + ":";
		info += Math.exp(x[k]) + "\n";
            }
            for (int t=0; t<kernel.MM.GetParameterCount();t++){
                info += kernel.MM.getMeanModelParameterNames()[t] + ":";
		info += kernel.MM.GetParameters()[t] + "\n";
            }
            if (this.getModel()!=null){
                getModel().getRuntime().println(info);		
                getModel().getRuntime().println("function value:\t" + y1 + "\t alpha: " + calpha + "\t diff:" + diff);
            }else{
                System.out.println(info);
                System.out.println("function value:\t" + y1 + "\t Alpha: " + calpha + "\t diff:" + diff);
            }  
            
	    for (int i=0;i<x.length;i++) {
		if (alpha[i]>calpha)
		    calpha = alpha[i];
	    }
	    
	    diff = Math.abs((y_neu-y_alt)/y_neu);
	    
	    y_alt = y_neu;	    	    
	}	
    }

 /*   public void MomentumGradientDescent(double x[]) {
	double y1,y2,alpha,diff;						
	double [] grad = new double[x.length];	
	double xp[] = new double[x.length];
	
	double alpha_min = 0.000000000000001;
	double diff_min = 0.0000000001;
	double approxError = 0.0001;
	
	alpha = 0.1;
	diff  = 1.0;
	
	y1 = funct(x);
	
	while ( alpha > alpha_min && diff > diff_min ) {	    	    
	    //partial differences quotients
	    for (int i=0; i < x.length; i++) {	
	        for (int j=0; j < x.length; j++) {	
		    if (j == i) {
		        xp[j] = x[j]+approxError;			
		    }		    
		    else
		        xp[j] = x[j];			
		}	    
		y2 = funct(xp);		    		    	
		grad[i] = (y2 - y1) / approxError;    
	    }		    
	    
	    //normalize gradient
	    double sum = 0.0;
	    for (int i=0;i<grad.length;i++) {
		sum += grad[i]*grad[i];
	    }
	    sum = Math.sqrt(sum);
	    
	    for (int i=0;i<grad.length;i++) {
		grad[i] /= sum;
	    }	    
	    //use armijo - method to obtain step width
	    //decrease step - width until result is better than the last one
		
	    //try to increase step - width
	    alpha *= 4.0;
	    
	    double y_neu;
	    
	    while (true) {		
	        for (int i=0; i < x.length; i++) {	
		    xp[i] = x[i] - alpha*grad[i];		    
		}
		
		y_neu = funct(xp);
		
		if (y_neu < y1)
		    break;

		alpha /= 2.0;
		
		if (alpha < alpha_min)
		    break;
	    }
	    
	    diff = Math.abs((y1/y_neu) - 1.0);
	    
	    y1 = y_neu;
	    
	    String info = "Gradient:\t";		
	    for (int i=0; i < x.length; i++) {	
		x[i] -= alpha * grad[i];		    
		info += grad[i] + "\t";
	    }
	    getModel().getRuntime().println(info);
	    info = "Stelle:\t\t";
	    for (int i=0; i < x.length; i++) {	
		info += x[i] + "\t";
	    }
	    getModel().getRuntime().println(info);									
	    getModel().getRuntime().println("Funktionswert:\t" + y1 + "\t Alpha: " + alpha);
	}	
    }*/
    
    public void setKernels(){
        switch (this.kernelMethod.getValue()) {
	    case 0: this.kernel = new optas.datamining.kernels.TestKernel(this.DataLength); break;
	    case 2: this.kernel = new optas.datamining.kernels.Exponential(this.DataLength); break;
	    case 3: this.kernel = new optas.datamining.kernels.MaternClass(this.DataLength); break;
	    case 5: this.kernel = new optas.datamining.kernels.RationalQuadratic(this.DataLength); break;
	    case 6: this.kernel = new optas.datamining.kernels.NeuralNetwork(this.DataLength); break;
            case 7: this.kernel = new optas.datamining.kernels.NoNoiseExponential(this.DataLength); break;
            case 8: this.kernel = new optas.datamining.kernels.NeuralNetworkFull(this.DataLength); break;
	    
	    case 12: this.kernel = new optas.datamining.kernels.SimpleExponential(this.DataLength); break;
	    case 13: this.kernel = new optas.datamining.kernels.SimpleMatern(this.DataLength); break;
	    case 15: this.kernel = new optas.datamining.kernels.SimpleRationalQuadratic(this.DataLength); break;
	    case 16: this.kernel = new optas.datamining.kernels.SimpleNeuralNetwork(this.DataLength); break;
	    case 17: this.kernel = new optas.datamining.kernels.SimplePeriodic(this.DataLength); break;
	    default: this.kernel = null; this.getModel().getRuntime().sendInfoMsg("No valid Kernel specified, using Neural-Network Kernel"); break;
	}                
	switch (this.MeanMethod.getValue()) {
	    case 0: this.kernel.SetMeanModell(new optas.datamining.kernels.FixedMeanModell(this.DataLength)); break;
	    case 1: this.kernel.SetMeanModell(new optas.datamining.kernels.LinearMeanModell(this.DataLength)); break;
	    case 2: this.kernel.SetMeanModell(new optas.datamining.kernels.QuadraticMeanModell(this.DataLength)); break;
	    default: this.getModel().getRuntime().sendInfoMsg("No valid mean function specified, using Fixed Mean Model"); this.kernel.SetMeanModell(new optas.datamining.kernels.FixedMeanModell(this.DataLength)); break;
	}
        
        kernelParameterNames = this.kernel.getParameterNames();                
    }
    
    public void run() {
        if ((mode.getValue() & MODE_APPLYMODEL)!=0){        
            try{
                this.deserializeModel();
                double x[]=this.Predict(false);                
                this.writePrediction(x, null, null);                
            }catch(Exception e){
                this.getModel().getRuntime().sendInfoMsg("Could not apply model, because: " + e.toString());	        
            }
            return;
        }
	trainInit();
        setKernels();
        
	if ( (mode.getValue() & MODE_OPTIMIZE) != 0) {
            optInit();
            double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++){
                if (this.param_theta != null)
                    x[i] = Math.log(this.param_theta.getValue()[i]);
                else
                    x[i] = 1.0/x.length;
            }
            while (Train(0) < -100000.0){
                for (int i=0;i<x.length;i++){
                    x[i] = (generator.nextDouble()*10.0);
                }
                if (this.param_theta != null)
                    this.param_theta.setValue(x);
            }	    
	    	    
	    GradientDescent(x,this.kernelParameterNames);   
            
            //save parameters
            BufferedWriter writer;
	    try {
		writer = new BufferedWriter(new FileWriter(FileTools.createAbsoluteFileName(this.getModel().getWorkspaceDirectory().getAbsolutePath(),this.parameterFile.getValue())));
                this.meantheta = this.kernel.MM.GetParameters();
		for (int i=0;i<theta.length;i++) {
                    writer.write(Double.toString(this.logtheta[i]) + "\n");		    
		}
                for (int i=0;i<meantheta.length;i++) {
                    writer.write(Double.toString(this.meantheta[i]) + "\n");		    
		}
		writer.close();
	    }
	    catch (Exception e) {
                this.getModel().getRuntime().sendInfoMsg("Could not open or writer parameter file, becauce:" + e.toString());	        
	    }	
	}
	trainInit();
        double performance;
	while ((performance = Train(0)) < -100000.0){            
	    double x[] = new double[kernel.getParameterCount()];
	    for (int i=0;i<x.length;i++)
		x[i] = (generator.nextDouble()*10.0);
            if (this.param_theta != null)
                this.param_theta.setValue(x);
	    if (performance > -100000.0){
                optInit();
                GradientDescent(x,this.kernelParameterNames);                                
            }
        }
        System.out.println("TrainPerformance:" + performance);
	Predict(true);
        if (this.modelDataFile!=null){
            if (!this.modelDataFile.getValue().equals("")){
                try{
                    this.serializeModel();
                }catch(Exception e){
                    this.getModel().getRuntime().sendInfoMsg("Could not serialize model to given file:" + e.toString());	        
                }
            }
        }
    }    
}
