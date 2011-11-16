/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import java.util.TreeSet;
import optas.hydro.data.SimpleEnsemble;

/**
 *
 * @author chris
 */
public abstract class Interpolation {

    public enum ErrorMethod{ABSE, RMSE,E2};

    protected SimpleEnsemble x[];
    protected SimpleEnsemble y;

    protected int n,m;
    protected int L=0;

    protected boolean initSuccessful = false;
    protected double[] weights = null;

    public void setData(SimpleEnsemble x[], SimpleEnsemble y){
        this.x = x;
        this.y = y;
        
        if (x.length==0){
            return;
        }

        n = x.length;
        L = x[0].getSize();

        if (weights == null || weights.length != n){
            weights = new double[n];
            for (int i=0;i<n;i++){
                if (x[i].getSize()!=L)
                    return;
                weights[i] = 0.0;
            }
            setWeighting(weights);
        }        
        
        if (y.getSize()!=L)
            return;
        initSuccessful = true;
    }

    public void init() {
        weights = new double[n];
        for (int i = 0; i < n; i++) {
            if (x[i].getSize() != L) {
                return;
            }
            weights[i] = 0.0;
        }
        setWeighting(weights);
    }

    protected double[] getX(int id){
        double row[] = new double[n];
        for (int i=0;i<n;i++){
            row[i] = x[i].getValue(id);
        }
        return row;
    }

    abstract protected double[] getValue(TreeSet<Integer> leaveOut);
    public abstract double getValue(double u[]);

    private double calcDifference(ErrorMethod e, double sim[], double obs[]){
        int K = sim.length;
        double error = 0;
        switch (e){
            case ABSE:
                for (int i=0;i<K;i++){
                    error += Math.abs(sim[i] - obs[i]);
                }
                return error / K;
            case RMSE:
                for (int i=0;i<K;i++){
                    error += (sim[i] - obs[i])*(sim[i] - obs[i]);
                }
                return Math.sqrt(error/K);
            case E2:
                double aobs = 0;
                for (int i=0;i<K;i++){
                    aobs += obs[i];
                }
                aobs /= K;

                double numerator = 0;
                double denumerator = 0;
                for (int i=0;i<K;i++){
                    numerator += (sim[i] - obs[i])*(sim[i] - obs[i]);
                    denumerator += (obs[i] - aobs)*(obs[i] - aobs);
                }
                return 1.0 - (numerator / denumerator);
        }
        return 0.0;
    }

    protected void setWeighting(double []w){
        this.weights = w;
    }
    
    private double funct(double w[]){
        setWeighting(w);
        return estimateLOOError(ErrorMethod.RMSE);
    }

    public void optimizeWeights(){
        String paramName[] = new String[n];
        for (int i=0;i<n;i++){
            paramName[i] = "w_" + i;
        }
        gradientDescent(weights, paramName);
    }
    private void gradientDescent(double x[],String paramName[]) {
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
        System.out.println("Performing Gradient Descent Optimization!");
        System.out.println("starting with function value:" + y1);
        int iteration = 0;
	while ( calpha > alpha_min && diff > diff_min ) {
            iteration++;
            System.out.println("iteration:" + iteration);
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


            for (int i=0;i<x.length;i++) {
		if (alpha[i]>calpha)
		    calpha = alpha[i];
	    }

	    diff = Math.abs((y_neu-y_alt)/y_neu);

            y_alt = y_neu;
            
            System.out.println(info);
            System.out.println("function value:\t" + y1 + "\t Alpha: " + calpha + "\t diff:" + diff);
	}
        funct(x);
    }

    protected void calculate(){
        
    }

    public double estimateCrossValidationError(int K, ErrorMethod e){
        double error = 0;

        double obs[] = new double[L];
        double sim[] = new double[L];

        this.calculate();

        for (int k=0;k<K;k++){


            int indexStart = k*(L/K);
            int indexEnd   = Math.min((k+1)*(L/K)-1,L-1);
            int size = indexEnd-indexStart;
            if (size == 0)
                continue;

            TreeSet<Integer> validationSet = new TreeSet<Integer>();
            double validation[] = new double[size];

            for (int j=indexStart;j<indexEnd;j++){
                int id_loi = x[0].getId(j);
                validationSet.add(id_loi);
                validation[j-indexStart] = y.getValue(id_loi);
            }

            double y_star[] = getValue(validationSet);

            for (int j=0;j<size;j++){
                error += Math.abs(y_star[j] - validation[j]);

                obs[j] = validation[j];
                sim[j] = y_star[j];
            }

        }

        return calcDifference(e, sim, obs);
    }

    public double estimateLOOError(ErrorMethod e){
        double error = 0;

        double obs[] = new double[L];
        double sim[] = new double[L];

        this.calculate();

        for (int leaveOutIndex = 0;leaveOutIndex<L;leaveOutIndex++){
            double x_star[] = new double[n];

            int id_loi = x[0].getId(leaveOutIndex);

            //TODO: wer stellt sicher, dass wir unter dem gleichen index auch die gleiche id finden???
            for (int i=0;i<n;i++){
                x_star[i] = x[i].getValue(id_loi);
            }

            TreeSet<Integer> indexSet = new TreeSet<Integer>();
            indexSet.add(id_loi);
            double y_star[] = getValue(indexSet);


            error += Math.abs(y_star[0] - y.getValue(id_loi));

            obs[leaveOutIndex] = y.getValue(id_loi);
            sim[leaveOutIndex] = y_star[0];
        }

        return calcDifference(e, sim, obs);
    }    
}
