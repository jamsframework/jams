/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class RMSE extends EfficiencyCalculator {
    public double calc(double t1[], double t2[]){
        double rsme = 0;
        for (int i=0;i<t1.length;i++){
            rsme += (t1[i]-t2[i])*(t1[i]-t2[i]);
        }
        return Math.sqrt(rsme)/(double)t1.length;
    }

    public double calcNormative(double t1[], double t2[]){
        return calc(t1,t2);
    }
}
