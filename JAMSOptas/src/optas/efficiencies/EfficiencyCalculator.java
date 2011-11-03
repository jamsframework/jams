/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public abstract class EfficiencyCalculator {

    static public Class[] availableEfficiencies = {Rsme.class, VolumeError.class, NashSutcliffe.class};

    public abstract double calc(double t1[], double t2[]);
    public abstract double calcNormative(double t1[], double t2[]);
}
