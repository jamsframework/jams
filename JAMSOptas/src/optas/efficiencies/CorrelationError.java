/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class CorrelationError extends EfficiencyCalculator {

    double pow = 2.0;
    public CorrelationError(){

    }

    public CorrelationError(double pow){
        this.pow = pow;
    }

    public double calc(double m[], double s[]){
        double meanx = 0, meany=0;

        for (int i=0;i<m.length;i++){
            meanx += m[i];
            meany += s[i];
        }
        meanx /= m.length;
        meany /= m.length;

        double sumx=0,sumy=0,prod=0;
        for(int i = 0; i < m.length; i++){
            sumx += Math.pow((m[i] - meanx), 2);
            sumy += Math.pow((s[i] - meany), 2);
            prod += ((m[i] - meanx)*(s[i] - meany));

        }
        double r2 = Math.pow((prod / Math.sqrt(sumx * sumy)), 2);
        return r2;
    }

    public double calcNormative(double t1[], double t2[]){
        return 1.0 - Math.abs(calc(t1,t2));
    }
}
