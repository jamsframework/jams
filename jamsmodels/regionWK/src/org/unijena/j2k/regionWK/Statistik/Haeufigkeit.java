/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.j2k.regionWK.Statistik;

import org.apache.commons.math.stat.Frequency;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Haeufigkeit extends JAMSComponent {

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

        Frequency f = new Frequency();

           for(int i = 0; i < rHum.length; i++){
                  f.addValue(rHum[i]);
           }
          //Haeufigkeit wieder rausholen, wie wozu???
    }


    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }


}
