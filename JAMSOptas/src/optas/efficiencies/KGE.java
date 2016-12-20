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

    public KGE() {

    }

    public double calc(double m[], double s[]) {
        double r = 0;
        double alpha = 0;
        double beta = 0;
        double avgm, avgs, sum, varm, vars;
        avgm = avgs = sum = varm = vars = 0.;

        for (int i = 0; i < m.length; i++) {
            avgm += m[i];
            avgs += s[i];
        }
        avgm /= m.length;
        avgs /= s.length;

        for (int i = 0; i < m.length; i++) {
            sum += (m[i] - avgm) * (s[i] - avgs);
            varm += Math.pow(Math.abs(m[i] - avgm), pow);
            vars += Math.pow(Math.abs(s[i] - avgs), pow);
        }

        double sigm = Math.sqrt(varm);
        double sigs = Math.sqrt(vars);

        alpha = sigs / sigm; // IG : 0 should maybe be cared about
        beta = (avgs - avgm) / sigm;
        r = sum / (sigs * sigm);
        
        return 1- Math.sqrt(Math.pow(alpha - 1, pow) + Math.pow(beta - 1, pow) + Math.pow(r - 1, pow));
    }

    public double calcNormative(double t1[], double t2[]) {
        return 1.0 - calc(t1, t2);
    }
}
