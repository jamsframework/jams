/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.test;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public abstract class EfficiencyCalculator implements Serializable {

    

    public abstract double calc(double t1[], double t2[]);
    public abstract double calcNormative(double t1[], double t2[]);
}
