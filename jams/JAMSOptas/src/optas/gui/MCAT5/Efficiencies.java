/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.MCAT5;

/**
 *
 * @author Christian Fischer
 */
public class Efficiencies {
    static public double[] CalculateLikelihood(double measure[]){
        int n = measure.length;
        double likelihood[] = new double[n];
        
        double Lmin = Double.POSITIVE_INFINITY;       
        for (int i=0;i<n;i++){
            //!!!
            likelihood[i] = measure[i];
            if (likelihood[i] < Lmin) 
                Lmin = likelihood[i];
        }
        if (Lmin < 0)
            for (int i=0;i<n;i++){
                likelihood[i] -= Lmin;            
            }
        double sum = 0;
        for (int i=0;i<n;i++){
            sum += likelihood[i];            
        }
        for (int i=0;i<n;i++){
            likelihood[i] /= sum;            
        }
        return likelihood;
    }
    
    public static double[] CalculateR2(double[] xData, double[] yData){
        double sumYValue = 0;
        double meanYValue = 0;
        double sumXValue = 0;
        double meanXValue = 0;
        double sumX = 0;
        double sumY = 0;
        double prod = 0;

        int nstat = xData.length;
        double[] regCoef = new double[3]; //(intercept, gradient, r^)

        //calculating sums
        for(int i = 0; i < nstat; i++){
            sumYValue += yData[i];
            sumXValue += xData[i];
        }
        //calculating means
        meanYValue = sumYValue / nstat;
        meanXValue = sumXValue / nstat;
        
        //calculating regression coefficients
        for(int i = 0; i < nstat; i++){
            sumX += Math.pow((xData[i] - meanXValue), 2);
            sumY += Math.pow((yData[i] - meanYValue), 2);
            prod += ((xData[i] - meanXValue)*(yData[i] - meanYValue));            
        }
        
        regCoef[0] = meanYValue - regCoef[1] * meanXValue; //intercept
        regCoef[1] = prod / sumX;  //gradient        
        regCoef[2] = Math.pow((prod / Math.sqrt(sumX * sumY)), 2); //r^
                                
        return regCoef;
    }
    
    static public double[] ArrayLog(double input[]){
        double output[] = new double[input.length];
        for (int i=0;i<output.length;i++){
            if (input[i]>0)
                output[i] = Math.log(input[i]);
            else
                output[i] = -1;
        }
        return output;
    }
    
    static public double CalculateE(double obs[],double sim[],int pow){
        double a = 0;
        double b = 0;
        double mw = 0;
        for (int i=0;i<obs.length;i++){
            mw += obs[i];
        }
        mw /= obs.length;
        for (int i=0;i<obs.length;i++){
            a += Math.pow(Math.abs(obs[i]-sim[i]),pow);
            b += Math.pow(Math.abs(obs[i]-mw),pow);
        }
        if (b==0){
            b = 0.001;
        }
        return 1 - a/b;
    }
    
    static public double CalculateIndexOfAgreement(double validation[],double prediction[], int pow){        
        double ioa;
        int td_size = prediction.length;
        int vd_size = validation.length;
        int steps = 0;
        double sum_td = 0;
        double sum_vd = 0;
        
        if(td_size != vd_size)  return Double.NEGATIVE_INFINITY;
        else                    steps = td_size;
               
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            sum_td += prediction[i];
            sum_vd += validation[i];
        }
        
        /** calculating mean values for both data sets */
        double mean_td = sum_td / steps;
        double mean_vd = sum_vd / steps;
        
        /** calculating mean cubic deviations */
        double td_vd = 0;
        double vd_mean = 0;
        for(int i = 0; i < steps; i++){
            td_vd = td_vd + (Math.pow((Math.abs(validation[i] - prediction[i])),pow));
            vd_mean = vd_mean + (Math.pow((Math.abs(validation[i] - mean_vd)),pow));
        }
        
        /** calculating absolute squared sum of deviations from verification mean */
        double abs_sqDevi = 0;
        for(int i = 0; i < steps; i++){
            abs_sqDevi += Math.pow(Math.abs(prediction[i] - mean_vd) + Math.abs(validation[i] - mean_vd), pow);
        }
        
        /** calculating ioa */
        ioa = 1.0 - (td_vd / abs_sqDevi);
        
        return ioa;    
    }
    
    public static double CalculateDSGrad(double[] prediction, double[] validation){
        int dsLength = prediction.length;
        
        double[] cumPred = new double[dsLength];
        double[] cumVali = new double[dsLength];
        double cp = 0;
        double cv = 0;
        
        for(int i = 0; i < dsLength; i++){
            cp += prediction[i];
            cv += validation[i];
            cumPred[i] = cp; 
            cumVali[i] = cv;
        }
                
        return CalculateR2(cumVali, cumPred)[1];
    }
    
    public static double CalculateAbsVolError(double[] validation, double[] prediction){
        double volError = 0;
        for(int i = 0; i < prediction.length; i++){
            volError += (prediction[i] - validation[i]);
        }
        return Math.abs(volError); 
    }
    
    public static double CalculateRMSE(double[] validation, double[] prediction){
        double rmse = 0;
        for(int i = 0; i < prediction.length; i++){
            rmse += (prediction[i] - validation[i])*(prediction[i] - validation[i]);
        }
        return Math.sqrt(rmse);
    }
            
    public static double CalculatePBIAS(double[] validation, double[] prediction){
    	double sumObs = 0;
    	double sumDif = 0;
    	for(int i = 0; i < prediction.length; i++){
            sumDif += (prediction[i] - validation[i]);
            sumObs += validation[i];
        }
    	return (sumDif / sumObs) * 100;
    }
}
