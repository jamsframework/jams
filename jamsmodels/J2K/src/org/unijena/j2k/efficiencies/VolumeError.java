/*
 * VolumeError.java
 *
 * Created on 23. Mai 2006, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.efficiencies;

import java.util.Arrays;

/**
 *
 * @author c0krpe
 */
public class VolumeError {
    
    /** Creates a new instance of VolumeError */
    public VolumeError() {
    }

    public static double absVolumeError(double[] validation, double[] prediction){
        double weight[] = new double[prediction.length];
        Arrays.fill(weight, 1.0);
        return absVolumeError(prediction, validation, weight);
    }

    public static double absVolumeError(double[] validation, double[] prediction, double[] weight){
        double volError = 0;
        for(int i = 0; i < prediction.length; i++){
            volError += weight[i]*(prediction[i] - validation[i]);
        }
        return Math.abs(volError); 
    }
    
    public static double pbias(double[] validation, double[] prediction){
    	double sumObs = 0;
    	double sumDif = 0;
    	for(int i = 0; i < prediction.length; i++){
            sumDif += (prediction[i] - validation[i]);
            sumObs += validation[i];
        }
    	double pbias = (sumDif / sumObs) * 100;
    	return pbias;
    }

    public static double pbias2(double[] validation, double[] prediction){
    	double sumObs = 0;
    	double sumDif = 0;
    	for(int i = 0; i < prediction.length; i++){
            sumDif += Math.abs(prediction[i] - validation[i]);
            sumObs += validation[i];
        }
    	double pbias = (sumDif / sumObs) * 100;
    	return pbias;
    }    
    
}
