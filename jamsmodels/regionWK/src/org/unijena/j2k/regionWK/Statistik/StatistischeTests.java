/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.regionWK.Statistik;


import jams.data.*;
import jams.model.*;
import org.apache.commons.math.stat.inference.*;

@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class StatistischeTests extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the relative humidity values"
            )
            public Attribute.DoubleArray rhum;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "temperature for the computation"
            )
            public Attribute.DoubleArray temperature;


public void init() throws Attribute.Entity.NoSuchAttributeException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        double[] rHum = this.rhum.getValue();


    }


    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }


    public static void oneSample_t_Test(double[] timeseries) throws org.apache.commons.math.MathException {

        //To compare the mean of a double[] array to a fixed value:
        double mu = 2.5d;
        double t = TestUtils.t(mu, timeseries);

        //To compute the p-value associated with the null hypothesis
        //that the mean of a set of values equals a point estimate,
        //against the two-sided alternative that the mean is
        //from the target value:

        double tTest = TestUtils.tTest(mu, timeseries);

        //To perform the test using a fixed significance level, use:
        double alpha = 0.05;
        boolean tTestalpha = TestUtils.tTest(mu, timeseries, alpha);

    }

    public static void twoSample_t_Test(double [] timeseries) {
    }

    public static void chiSquare_Test(double [] timeseries) {
    }

    public static void oneWayAnova_Test(double [] timeseries) {
    }


}

