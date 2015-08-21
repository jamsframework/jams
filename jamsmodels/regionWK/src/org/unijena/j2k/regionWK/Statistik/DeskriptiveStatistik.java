package org.unijena.j2k.regionWK.Statistik;

import org.apache.commons.math.stat.*;
import org.apache.commons.math.stat.descriptive.*;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class DeskriptiveStatistik extends JAMSComponent {

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

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "temperature for the computation"
            )
            public Attribute.Double min;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "temperature for the computation"
            )
            public Attribute.Double max;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "mean"
            )
            public Attribute.Double mean;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "median"
            )
            public Attribute.Double median;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "first quartil"
            )
            public Attribute.Double q1;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "third quartil"
            )
            public Attribute.Double q3;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "variance"
            )
            public Attribute.Double var;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "standard deviation"
            )
            public Attribute.Double sA;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "spannweite"
            )
            public Attribute.Double sw;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "durchschnittliche Abweichung"
            )
            public Attribute.Double da;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "durchschnittliche Abweichung"
            )
            public Attribute.Double kurtosis;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "durchschnittliche Abweichung"
            )
            public Attribute.Double skewness;



public void init() throws Attribute.Entity.NoSuchAttributeException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {

        double[] rHum = this.rhum.getValue();

        // Get a DescriptiveStatistics instance using factory method
        DescriptiveStatistics stats = DescriptiveStatistics.newInstance();

        // Add the data from the array
        for (int i = 0; i < rHum.length; i++) {
            stats.addValue(rHum[i]);
        }
         //Extremwerte
        double mini = StatUtils.min(rHum);
        double maxi = StatUtils.max(rHum);
             
        //Lageparameter
        double mean_ = StatUtils.mean(rHum);
        double median_ = StatUtils.percentile(rHum, 50);
        double quartil1 = StatUtils.percentile(rHum, 25);
        double quartil3 = StatUtils.percentile(rHum, 75);

        this.mean.setValue(mean_);
        this.median.setValue(median_);
        this.q1.setValue(quartil1);
        this.q3.setValue(quartil3);

        //Streuung
        double varianz = StatUtils.variance(rHum);
        double stabw = Math.sqrt(StatUtils.variance(rHum));
        double spannweite = StatUtils.max(rHum)-StatUtils.min(rHum);

        this.var.setValue(varianz);
        this.sA.setValue(stabw);
        this.sw.setValue(spannweite);

        double dA = 0;

            double [] F = new double [rHum.length] ;

                for(int i = 0; i < rHum.length; i++){
                    F[i] = Math.abs(rHum[i] - mean_);
                    dA = dA + F[i];
                }

                dA = dA/(rHum.length);
                this.da.setValue(dA);
       
          //Formaparameter
                double kurt = stats.getKurtosis();
                double skew = stats.getSkewness();

                this.kurtosis.setValue(stabw);
                this.skewness.setValue(spannweite);

             //System.out.println("schiefeSTAT " + skew);
           //System.out.println("woelbungSTAT " + kurt);
    }


    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }


}

