/*
 * NashSutcliffe.java
 *
 * Created on 30. November 2005, 12:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jams.components.efficiencies;

/**
 *
 * @author Peter Krause
 */
public class NashSutcliffe {
    
    /** Creates a new instance of NashSutcliffe */
    public NashSutcliffe() {
    }
    
    /** Calculates the efficiency between a test data set and a verification data set
     * after Nash & Sutcliffe (1970). The efficiency is described as the proportion of
     * the cumulated cubic deviation between both data sets and the cumulated cubic
     * deviation between the verification data set and its mean value.
     * @param predicition the simulation data set
     * @param validation the validation (observed) data set
     * @param pow the power for the deviation terms
     * @return the calculated efficiency or -9999 if an error occurs
     */    
    public static double efficiency(double[] prediction, double[] validation, double pow){
        int pre_size = prediction.length;
        int val_size = validation.length;
                
        int steps = 0;
        
        double sum_td = 0;
        double sum_vd = 0;
        
        /** checking if both data arrays have the same number of elements*/
        if(pre_size != val_size){
            System.err.println("Prediction data and validation data are not consistent!");
            return -9999;
        }
        else{
            steps = pre_size;
        }
        
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            sum_td = sum_td + prediction[i];
            sum_vd = sum_vd + validation[i];
        }
        
        /** calculating mean values for both data sets */
        double mean_td = sum_td / steps;
        double mean_vd = sum_vd / steps;
        
        /** calculating mean pow deviations */
        double td_vd = 0;
        double vd_mean = 0;
        for(int i = 0; i < steps; i++){
            td_vd = td_vd + (Math.pow((Math.abs(validation[i] - prediction[i])),pow));
            vd_mean = vd_mean + (Math.pow((Math.abs(validation[i] - mean_vd)),pow));
        }
        
        /** calculating efficiency after Nash & Sutcliffe (1970) */
        double efficiency = 1 - (td_vd / vd_mean);
        
        return efficiency;
         
    }
    
    /** Calculates the efficiency between the log values of a test data set and a verification data set
     * after Nash & Sutcliffe (1970). The efficiency is described as the proportion of
     * the cumulated cubic deviation between both data sets and the cumulated cubic
     * deviation between the verification data set and its mean value. If either prediction or validation has a 
     * value of <= 0 then the pair is ommited from the calculation and a message is put to system out. 
     * @param predicition the simulation data set
     * @param validation the validation (observed) data set
     * @param pow the power for the deviation terms
     * @return the calculated log_efficiency or -9999 if an error occurs
     */    
    public static double logEfficiency(double[] prediction, double[] validation, double pow){
        int pre_size = prediction.length;
        int val_size = validation.length;
        
        int steps = 0;
        
        double sum_log_pd = 0;
        double sum_log_vd = 0;
        
        /** checking if both data arrays have the same number of elements*/
        if(pre_size != val_size){
            System.err.println("Prediction data and validation data are not consistent!");
            return -9999;
        }
        else{
            steps = pre_size;
        }
        
        /** calculating logarithmic values of both data sets. Sets 0 if data is 0 */
        double[] log_preData = new double[pre_size];
        double[] log_valData = new double[val_size];
        
        int validPairs = 0;
        
        for(int i = 0; i < steps; i++){
            //either prediction or validation shows a value of zero
            //in this case the pair is excluded from the further calculation,
            //simply by setting the values to -1 and not increasing valid pairs
            if(prediction[i] <= 0 || validation[i] <= 0){
                log_preData[i] = -1;
                log_valData[i] = -1;
            }
            //both prediction and validation shows a value of exact zero
            //in this case the pair is taken as a perfect fit and included 
            //into the further calculation
            if(prediction[i] == 0 && validation[i] == 0){
                log_preData[i] = 0;
                log_valData[i] = 0;
                validPairs++;
            }
            //both prediction and validation are greater than zero
            //no problem for the calculation
            if(prediction[i] > 0 && validation[i] > 0){
                log_preData[i] = Math.log(prediction[i]);
                log_valData[i] = Math.log(validation[i]);
                validPairs++;
            } 
        }
        
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            if(log_preData[i] >= 0){
                sum_log_pd = sum_log_pd + log_preData[i];
                sum_log_vd = sum_log_vd + log_valData[i];
            }
        }
        
        /** calculating mean values for both data sets */
        double mean_log_pd = sum_log_pd / validPairs;
        double mean_log_vd = sum_log_vd / validPairs;
        
        /** calculating mean pow deviations */
        double pd_log_vd = 0;
        double vd_log_mean = 0;
        for(int i = 0; i < steps; i++){
            if(log_preData[i] >= 0){
                pd_log_vd = pd_log_vd + (Math.pow(Math.abs(log_valData[i] - log_preData[i]),pow));
                vd_log_mean = vd_log_mean + (Math.pow(Math.abs(log_valData[i] - mean_log_vd),pow));
            }
        }
        
        /** calculating efficiency after Nash & Sutcliffe (1970) */
        double log_efficiency = 1 - (pd_log_vd / vd_log_mean);
        
        return log_efficiency;
         
    }
    
}
