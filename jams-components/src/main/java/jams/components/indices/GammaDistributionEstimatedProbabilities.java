/*
 * GammaDistributionEstimatedProbabilities.java
 * Created on 06.03.2019, 09:43:57
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.indices;

import jams.JAMS;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 *
 * based on SPI Generator v1.7.5 National Drought Mitigation Center - UNL
 * 11/29/2018
 *
 */
public class GammaDistributionEstimatedProbabilities {

    private final static int MAX_ITERATIONS = 100;
    private final static double DBL_EPSILON = 3.0e-7;

    /**
     * **************************************************************************
     *
     * input prob; return z.
     *
     * See Abromowitz and Stegun _Handbook of Mathematical Functions_, p. 933
     *
     ***************************************************************************
     */
    static double inv_normal(double prob) {

        double t, minus;
        double c0 = 2.515517;
        double c1 = 0.802853;
        double c2 = 0.010328;
        double d1 = 1.432788;
        double d2 = 0.189269;
        double d3 = 0.001308;

        if (prob > 0.5) {
            minus = 1.0;
            prob = 1.0 - prob;
        } else {
            minus = -1.0;
        }

        if (prob < 0.0) {
            throw new ArithmeticException("Probability not in [0,1.0].");
            //fprintf (stderr, "Error in inv_normal(). Prob. not in [0,1.0].\n");
            //return ((double)0.0);
        }

        if (prob == 0.0) {
            return (9999.0 * minus);
        }

        t = Math.sqrt(Math.log(1.0 / (prob * prob)));

        return (minus * (t - ((((c2 * t) + c1) * t) + c0)
                / ((((((d3 * t) + d2) * t) + d1) * t) + 1.0)));

    }

    /**
     * *************************************************************************
     *
     * Estimate incomplete gamma parameters.
     *
     * Input: datarr - Data array
     *
     * Output: alpha, beta, gamma - gamma paarameters pzero - probability of
     * zero.
     *
     * Return: number of non zero items in datarr.
     *
     ***************************************************************************
     */
    static int gamma_fit(double[] datarr, Parameters params) {

        if (datarr == null || datarr.length <= 0) {
            return (0);
        }

        double sum = 0.0;
        double sumlog = 0.0;
        double mn = 0;
        double nact = 0;

        double alpha;
        double beta;
        double gamma;
        double pzero = 0.0;

        /*  compute sums */
        for (int i = 0; i < datarr.length; i++) {
            // do the fitting using only non missing data
            if (datarr[i] == JAMS.getMissingDataValue()) {
                continue;
            }
            if (datarr[i] > 0.0) {
                sum += datarr[i];
                sumlog += Math.log(datarr[i]);
                nact++;
            } else {
                pzero++;
            }
        }

        pzero /= (nact + pzero);
        if (nact != 0.0) {
            mn = sum / nact;
        }

        if (nact == 1) /* Bogus Data array but do something reasonable */ {
            alpha = 0.0;
            gamma = 1.0;
            beta = mn;
            return (int) nact;
        }

        if (pzero == 1.0) /* They were all zeroes. */ {
            alpha = 0.0;
            gamma = 1.0;
            beta = mn;
            return (int) nact;
        }

        /* Use MLE */
        alpha = Math.log(mn) - sumlog / nact;
        gamma = (1.0 + Math.sqrt(1.0 + 4.0 * alpha / 3.0)) / (4.0 * alpha);
        beta = mn / gamma;

        params.alpha = alpha;
        params.beta = beta;
        params.gamma = gamma;
        params.pzero = pzero;

        return (int) nact;

    }

    /**
     * ************************************************************************
     *
     * Compute probability of a<=x using incomplete gamma parameters.
     *
     * Input: beta, gamma - gamma parameters pzero - probability of zero. x -
     * value.
     *
     * Return: Probability a<=x.
     *
     ***************************************************************************
     */
    static double gamma_cdf(double beta, double gamm, double pzero, double x) {

        if (x <= 0.0) {
            return (pzero);
        } else {
            return (pzero + (1.0 - pzero) * gammap(gamm, x / beta));
        }

    }

    /**
     * *****************************************************************
     *
     * Functions for the incomplete gamma functions P and Q
     *
     * 1 /x -t a-1 P (a, x) = -------- | e t dt, a > 0 Gamma(x)/ 0
     *
     * Q (a, x) = 1 - P (a, x)
     *
     * Reference: Press, Flannery, Teukolsky, and Vetterling, _Numerical
     * Recipes_, pp. 160-163
     *
     * Thanks to kenny@cs.uiuc.edu
     *
     ********************************************************************
     */

    /* Evaluate P(a,x) by its series representation.  Also put log gamma(a) into gln. */
    private static double gammser(double a, double x) {

        double ap, sum, del;
        int n;
        double gln = lgamma(a);

        if (x == 0.0) {
            return 0.0;
        }

        ap = a;
        sum = 1.0 / a;
        del = sum;

        for (n = 0; n < MAX_ITERATIONS; ++n) {
            sum += (del *= (x / ++ap));
            if (Math.abs(del) < DBL_EPSILON * Math.abs(sum)) {
                break;
            }
        }

        return sum * Math.exp(-x + a * Math.log(x) - gln);

    }

    /* Evaluate P(a,x) in its continued fraction representation.  Once again, return gln = log gamma (a). */
    private static double gammcf(double a, double x) {

        double gln = lgamma(a);
        double g = 0.0;
        double gold = 0.0;
        double a0 = 1.0;
        double a1 = x;
        double b0 = 0.0;
        double b1 = 1.0;
        double fac = 1.0;

        for (int n = 1; n <= MAX_ITERATIONS; ++n) {
            double an = n;
            double ana = an - a;
            double anf;
            a0 = (a1 + a0 * ana) * fac;
            b0 = (b1 + b0 * ana) * fac;
            anf = an * fac;
            a1 = x * a0 + anf * a1;
            b1 = x * b0 + anf * b1;
            if (a1 != 0.0) {
                fac = 1.0 / a1;
                g = b1 * fac;
                if (Math.abs((g - gold) / g) < DBL_EPSILON) {
                    break;
                }
                gold = g;
            }
        }

        return g * Math.exp(-x + a * Math.log(x) - gln);

    }

    /* Evaluate the incomplete gamma function P(a,x), choosing the most appropriate representation. */
    private static double gammap(double a, double x) {
        if (x < a + 1.0) {
            return gammser(a, x);
        } else {
            return 1.0 - gammcf(a, x);
        }
    }

    private static double lgamma(double gmm) {

        double x, y, ser, tmp;

        double[] cof = new double[6];
        cof[0] = 76.18009173;
        cof[1] = -86.50532033;
        cof[2] = 24.01409822;
        cof[3] = -1.231739516;
        cof[4] = 0.120858003e-2;
        cof[5] = -0.536382e-5;

        x = y = gmm;
        tmp = x + 5.5;
        tmp = tmp - (x + 0.5) * Math.log(tmp);
        ser = 1.00000000019005;

        for (int j = 0; j < cof.length; j++) {
            ser = ser + cof[j] / ++y;
        }

        return (-tmp + Math.log(2.50662827465 * ser / x));

    }

    public static class Parameters {

        double alpha, beta, gamma, pzero;
    }
}
