/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class LogarithmicNashSutcliffe extends EfficiencyCalculator {

    double pow = 2.0;
    public LogarithmicNashSutcliffe(){

    }

    public LogarithmicNashSutcliffe(double pow){
        this.pow = pow;
    }

    public double calc(double m[], double s[]){
        double rsme = 0;
        double var  = 0;
        double avg  = 0;
        for (int i=0;i<m.length;i++){
            if (m[i]>0)
                avg += Math.log(m[i]);
        }
        avg /= m.length;

        for (int i=0;i<m.length;i++){
            if (m[i]>0 & s[i]>0){
                rsme += Math.pow(Math.abs(Math.log(m[i])-Math.log(s[i])),pow);
                var  += Math.pow(Math.abs(Math.log(m[i])-avg),pow);
            }
        }
        return 1.0 - (rsme / var);
    }

    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
}
