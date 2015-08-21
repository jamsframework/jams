/*
 * PredictionErrors.java
 *
 * Created on 23. Mai 2006, 11:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.efficiencies;

/**
 *
 * @author c0krpe
 */
public class PredictionErrors {
    
    /** Creates a new instance of PredictionErrors */
    public PredictionErrors() {
    }
    
    public static double rootMeanSquareError(double[] prediction, double[] validation){
        double error = 0;
        for(int i = 0; i < prediction.length; i++){
            error += Math.pow((prediction[i] - validation[i]), 2);
        }
        error = error / prediction.length;
        return Math.sqrt(error);
    }
    
}
