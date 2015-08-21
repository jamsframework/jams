/*
 * HymodCore.java
 * Created on 14. March 2007, 16:54
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

/*
 * this implementation of hymod is based on the Mathlab version of Hoshin Gupta
 */
package optas.test;


/**
 *
 * @author c0krpe
 */
/*@JAMSComponentDescription(title = "Hymod01",
author = "Peter Krause",
description = "The HYMOD Model implemented based on the MatLab sources"
+ "of Hoshin V. Gupta from 9/18/2005")*/
public class HymodVrugt {

    /*
     *  Input data
     */
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "precipitation")*/
    public double precip;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "potential evapotranspiration")*/
    public double pet;
    /*
     * Model parameters
     */
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Max height of soil moisture accounting tank")*/
    public double cmax;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Distribution function shape parameter")*/
    public double bexp;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Quick-slow split parameter")*/
    public double alpha;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Number of quickflow routing tanks")*/
    public double nq;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Quickflow routing tanks rate parameter")*/
    public double kq;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Slowflow routing tanks rate parameter")*/
    public double ks;
    /*
     *Initialize state variables
     */
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Soil moisture accounting tank state contents")*/
    public double xcuz;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Quickflow routing tanks state contents")*/
    public double xq;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Slowflow routing tank state content")*/
    public double xs;
    /*
     *model states
     */
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed upper zone soil moisture tank state contents")*/
    public double mxhuz;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed upper zone soil moisture tank state contents")*/
    public double mxcuz;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed quickflow tank states contents")*/
    public double[] mxq;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed slowflow tank state contentss")*/
    public double mxs;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed evapotranspiration flux")*/
    public double met;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed precipitation excess flux")*/
    public double mov;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed quickflow flux")*/
    public double mqq;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed slowflow flux")*/
    public double mqs;
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Model computed total streamflow flux")*/
    public double mq;
    /*
     *  Component run stages
     */
    final int indexUT1 = 0, indexUT2 = 1, indexXn = 2, indexXslow = 0, indexOutflow = 1;
    
    double x_loss = 0.0;
    double x_slow = 0;
    double x_quick[] = null;
    double convFactor = 22.3;//22.3;
    double outflow = 0;
    int n = 0;
    public void init() {
        x_loss = 0.0;
        x_slow = 2.3503 / (ks * convFactor);

        n = (int) this.nq;
        if (n <= 1) {
            n = 1;
        }
        x_quick = new double[n];
        for (int i = 0; i < n; i++) {
            x_quick[i] = 0;
        }

        outflow = 0;
    }

    public void run() {
        double precip = this.precip;
        double pet = this.pet;

        // Compute excess precipitation and evaporation
        double tmp1[] = excess(x_loss, this.cmax, this.bexp, precip, pet);

        double UT1 = tmp1[indexUT1];
        double UT2 = tmp1[indexUT2];
        x_loss = tmp1[indexXn];
        // Partition UT1 and UT2 into quick and slow flow component
        double UQ = this.alpha * UT2 + UT1;
        double US = (1 - this.alpha) * UT2;

        // Route slow flow component with single linear reservoir
        double inflow = US;
        double tmp2[] = linres(x_slow, inflow, outflow, ks);
        x_slow = tmp2[indexXslow];
        outflow = tmp2[indexOutflow];
        double QS = outflow;

        // Route quick flow component with linear reservoirs
        inflow = UQ;

        for (int i = 0; i < n; i++) {
            double tmp3[] = linres(x_quick[i], inflow, outflow, kq);
            x_quick[i] = tmp3[indexXslow];
            outflow = tmp3[indexOutflow];
            inflow = outflow;
        }


        //Compute total flow for timestep
        double out = (QS + outflow) * convFactor;
        this.mq = out;
    }
    
    public double[] excess(double x_loss, double cmax, double bexp, double Pval, double PETval) {
        double result[] = new double[3];
        double xn_prev = x_loss;

        double ct_prev = cmax * (1. - Math.pow((1. - ((bexp + 1.) * (xn_prev) / cmax)), (1. / (bexp + 1))));
        result[indexUT1] = Math.max((Pval - cmax + ct_prev), 0.0);

        Pval = Pval - result[indexUT1];
        double dummy = Math.min(((ct_prev + Pval) / cmax), 1);
        double xn = (cmax / (bexp + 1.)) * (1 - Math.pow((1 - dummy), (bexp + 1)));
        result[indexUT2] = Math.max(Pval - (xn - xn_prev), 0);
        double evap = Math.min(xn, PETval);
        xn = xn - evap;

        result[indexXn] = xn;
        return result;
    }

    double[] linres(double x_slow, double inflow, double outflow, double Rs) {
// Linear reservoir
        double result[] = new double[2];
        x_slow = (1 - Rs) * x_slow + (1 - Rs) * inflow;
        result[indexOutflow] = (Rs / (1 - Rs)) * x_slow;

        result[indexXslow] = x_slow;
        return result;
    }
}
