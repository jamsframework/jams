/*
 * IndexOfAgreement.java
 *
 * Created on 2. Februar 2006, 09:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.efficiencies;

import jams.JAMSConstants;
import jams.model.JAMSModel;

/**
 *
 * @author c0krpe
 */
public class IndexOfAgreement {
    
    /** Creates a new instance of IndexOfAgreement */
    public IndexOfAgreement() {
    }
    
    /** Calculates the index of agreement (ioa) between a test data set and a verification data set
     * after Willmot & Wicks (1980). The ioa is described as the proportion of
     * the cumulated cubic deviation between both data sets and the squared sum of the absolute
     * deviations between the verification data set and the test mean value and the test data set and 
     * its mean value.
     * @param testData the test Data set
     * @param verificationData the verification data set
     * @return the calculated ioa or -9999 if an error occurs
     */    
    public static double calc_IOA(double[] prediction, double[] validation, double pow, JAMSModel model){
        double ioa;
        int td_size = prediction.length;
        int vd_size = validation.length;
        if(td_size != vd_size){
            model.getRuntime().println("Data sets in ioa does not match!");
            return -99999;
        }
        
        int steps = 0;
        
        double sum_td = 0;
        double sum_vd = 0;
        
        /** checking if both data arrays have the same number of elements*/
        if(td_size != vd_size){
            model.getRuntime().println("Test Data and Verification Data are not consistent!");
            return -9999;
        }
        else{
            steps = td_size;
        }
        
        /**summing up both data sets */
        for(int i = 0; i < steps; i++){
            sum_td = sum_td + prediction[i];
            sum_vd = sum_vd + validation[i];
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
        double ad_test = 0;
        double ad_veri = 0;
        double abs_sqDevi = 0;
        for(int i = 0; i < steps; i++){
            abs_sqDevi = abs_sqDevi + Math.pow(Math.abs(prediction[i] - mean_vd) + Math.abs(validation[i] - mean_vd), pow);
        }
        
        /** calculating ioa */
        ioa = 1.0 - (td_vd / abs_sqDevi);
        
        return ioa;
    }
    
}
