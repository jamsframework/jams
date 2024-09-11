/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.efficiencies;

/**
 *
 * @author Nico Hachgenei
 */
public class KGEprime_weighted extends WeightedEfficiencyCalculator {

    double pow = 2.0;

    public KGEprime_weighted() {

    }

    // calc method with weights
    public double calc(double m[], double s[], double weights[]) {
        double r_weight = weights[0];
        double gamma_weight = weights[1];
        double beta_weight = weights[2];
        double r = 0;
        double beta = 0;
        double gamma = 0;
        double avgm, avgs, sum, varm, vars, sigm, sigs, covm, covs;
        avgm = avgs = sum = varm = vars = covm = covs = 0.;

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
        sum /= m.length;
        varm /= m.length;
        vars /= s.length;
        sigm = Math.sqrt(varm);
        sigs = Math.sqrt(vars);
        covm = sigm / avgm;
        covs = sigs / avgs;
        
        gamma = covs / covm; // 0 should maybe be cared about (but 0 variation in measured value is not realistic)
        beta = avgs / avgm;
        r = sum / (sigs * sigm);

        return 1 - Math.sqrt(gamma_weight * Math.pow(gamma - 1, pow) + beta_weight * Math.pow(beta - 1, pow) + r_weight * Math.pow(r - 1, pow));
    }

    public double calcNormative(double t1[], double t2[], double weights[]) {
        return 1.0 - calc(t1, t2, weights);
    }
}
