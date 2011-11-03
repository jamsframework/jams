/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class VolumeError extends EfficiencyCalculator {
     public double calc(double m[], double s[]){
        int ve = 0;
        for (int i=0;i<m.length;i++){
            ve += (s[i]-m[i]);
        }
        return ve;
    }
    public double calcNormative(double t1[], double t2[]){
        return Math.abs(calc(t1,t2));
    }
}
