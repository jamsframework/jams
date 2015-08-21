/*
 * DoubleSumAnalysis.java
 *
 * Created on 23. Mai 2006, 08:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.efficiencies;

/**
 *
 * @author c0krpe
 */
public class DoubleSumAnalysis {
    
    /** Creates a new instance of DoubleSumAnalysis */
    public DoubleSumAnalysis() {
    }
    
    public static double dsGrad(double[] validation, double[] prediction){
        double dsGrad = 0;
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
        
        //interc., grad., r²
        double[] regCoef = org.unijena.j2k.statistics.Regression.calcLinReg(cumVali, cumPred);
        
        return regCoef[1];
    }
}
