/*
 * Regionalisation.java
 * Created on 17. November 2005, 14:20
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.j2k.regionalisation;

import jams.JAMS;
import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
public class Regionalisation extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of station elevations")
    public Attribute.DoubleArray statElevation;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array of station's weights")
    public Attribute.DoubleArray statWeights;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Array position of weights")
    public Attribute.IntegerArray statOrder;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "regionalised data value")
    public Attribute.Double dataValue;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Attribute name elevation")
    public Attribute.Double entityElevation;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Number of IDW stations")
    public Attribute.Integer nidw;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Apply elevation correction to measured data")
    public Attribute.Boolean elevationCorrection;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Minimum r² value for elevation correction application")
    public Attribute.Double rsqThreshold;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Absolute possible minimum value for data set",
                         defaultValue = "-Infinity")
    public Attribute.Double fixedMinimum;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Absolute possible maximum value for data set",
                         defaultValue = "Infinity")
    public Attribute.Double fixedMaximum;

    boolean invalidDatasetReported = false;

    ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);
    
    @Override
    public void run() throws IOException {
        //Retreiving data, elevations and weights
        double[] regCoeff = this.regCoeff.getValue();
        double gradient = regCoeff[1];
        double rsq = regCoeff[2];

        double[] sourceElevations = statElevation.getValue();
        double[] sourceData = dataArray.getValue();
        double[] sourceWeights = statWeights.getValue();
        double targetElevation = entityElevation.getValue();
        int[] wA = this.statOrder.getValue();
        
        double value = 0;
        double deltaElev = 0;
        
        int nIDW = this.nidw.getValue();
        double[] data = memPool.alloc(nIDW);
        double[] weights = memPool.alloc(nIDW);
        double[] elev = memPool.alloc(nIDW);
        

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
                
        int counter = 0, element = 0;      
        boolean valid = false;

        while (counter < nIDW) {
            int t = wA[element];
            //check if data is valid or no data
            if (sourceData[t] == JAMS.getMissingDataValue()) {
                element++;
                if (element >= wA.length) {
                    //getModel().getRuntime().println("BREAK1: too less data NIDW had been reduced!");
                    break;                    
                } 
            } else {
                valid = true;
                data[counter] = sourceData[t];
                weights[counter] = sourceWeights[t];
                elev[counter] = sourceElevations[t];

                counter++;
                element++;
                if (element >= wA.length) {                    
                    break;
                }
            }
        }
        //normalising weights
        double weightsum = 0;
        for (int i = 0; i < counter; i++) {
            weightsum += weights[i];
        }
        for (int i = 0; i < counter; i++) {
            weights[i] = weights[i] / weightsum;
        }

        if (valid) {
            for (int i = 0; i < counter; i++) {
                if ((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                    deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + data[i]) * weights[i]);                    
                    value = value + tVal;
                } else { //No elevation correction
                    value = value + (data[i] * weights[i]);
                }
            }
            //checking for minimum
            if (value < this.fixedMinimum.getValue()) {
                value = this.fixedMinimum.getValue();
            }
            if (value > this.fixedMaximum.getValue()) {
                value = this.fixedMaximum.getValue();
            }
        } else {
            if (!invalidDatasetReported) {     //only report once
                //in this case simulation should end, because it affects model behaviour seriously!
                getModel().getRuntime().sendHalt("Invalid dataset found while regionalizing data in component " + this.getInstanceName() + "."
                        + "\nThis might occur if all of the provided values are missing data values.");
                invalidDatasetReported = true;
            }
            value = JAMS.getMissingDataValue();
        }

        dataValue.setValue(value);
        
        //free data
        data = memPool.free(data);
        weights = memPool.free(weights);
        elev = memPool.free(elev);
    }
}
