/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;


/**
 *
 * @author Isabelle Gouttevin
 */
public class KGE extends EfficiencyCalculator {

    double pow = 2.0;
    public KGE(){

    }


    public double calc(double m[], double s[]){
        double r = 0; 
        double alpha  = 0;
        double beta  = 0;
	double avgm, avgs, sum, varm, vars ;
	avgm=avgs=sum=varm=vars=0.; 
        for (int i=0;i<m.length;i++){
            avgm += m[i];
	    avgs += s[i];
        }
        avgm /= m.length;
	avgs /= s.length;

        for (int i=0;i<m.length;i++){
            sum += (m[i]-avgm)*(s[i]-avgs);
            varm += Math.pow(Math.abs(m[i]-avgm),pow);
	    vars += Math.pow(Math.abs(s[i]-avgs),pow);
        }
        alpha=Math.sqrt(vars/varm) ; // IG : 0 should maybe be cared about
	beta=avgs/avgm ;
	r=sum/Math.sqrt(vars*varm) ; 
	return Math.sqrt( Math.pow(1-alpha,pow) + Math.pow(1-beta,pow) + Math.pow(1-r,pow)) ; 
    }

    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
}
