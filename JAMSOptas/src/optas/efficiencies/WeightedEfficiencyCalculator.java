/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public abstract class WeightedEfficiencyCalculator implements Serializable {

    

    public abstract double calc(double t1[], double t2[], double weights[]);
    public abstract double calcNormative(double t1[], double t2[], double weights[]);
}
