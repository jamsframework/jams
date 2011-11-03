/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class NashSutcliffe extends EfficiencyCalculator {
    public double calc(double m[], double s[]){
        double rsme = 0;
        double var  = 0;
        double avg  = 0;
        for (int i=0;i<m.length;i++){
            avg += m[i];
        }
        avg /= m.length;

        for (int i=0;i<m.length;i++){
            rsme += (m[i]-s[i])*(m[i]-s[i]);
            var  += (m[i]-avg)*(m[i]-avg);
        }
        return 1.0 - (rsme / var);
    }

    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
}
