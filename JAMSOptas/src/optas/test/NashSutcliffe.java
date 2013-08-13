/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.test;

/**
 *
 * @author chris
 */
public class NashSutcliffe extends EfficiencyCalculator {

    double pow = 2.0;
    public NashSutcliffe(){

    }

    public NashSutcliffe(double pow){
        this.pow = pow;
    }

    public double calc(double m[], double s[]){
        double rsme = 0;
        double var  = 0;
        double avg  = 0;
        for (int i=0;i<m.length;i++){
            avg += m[i];
        }
        avg /= m.length;

        for (int i=0;i<m.length;i++){
            rsme += Math.pow(Math.abs(m[i]-s[i]),pow);
            var  += Math.pow(Math.abs(m[i]-avg),pow);
        }
        return 1.0 - (rsme / var);
    }

    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
}
