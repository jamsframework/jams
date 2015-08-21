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
import jams.workspace.DataSetDefinition;
import jams.workspace.stores.InputDataStore;
import java.util.ArrayList;
import java.util.Arrays;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Peter Krause
 */
public class Regionalisation_ extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station elevations")
    public Attribute.DoubleArray statElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of station's weights")
    public Attribute.DoubleArray statWeights;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array position of weights")
    public Attribute.IntegerArray statOrder;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "regionalised data value")
    public Attribute.Double dataValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Attribute name elevation")
    public Attribute.Double entityElevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Number of IDW stations")
    public Attribute.Integer nidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Apply elevation correction to measured data")
    public Attribute.Boolean elevationCorrection;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Minimum r² value for elevation correction application")
    public Attribute.Double rsqThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible minimum value for data set")
    public Attribute.Double fixedMinimum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate")
    public Attribute.Double entityX;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate")
    public Attribute.Double entityY;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function")
    public Attribute.Double pidw;
//    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
//    description = "weights for IDW part of regionalisation")
//    public Attribute.DoubleArray statWeights;
//    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
//    description = "position array to determine best weights")
//    public Attribute.IntegerArray statOrder;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Weights for Thiessen polygons",
            defaultValue = "false")
    public Attribute.Boolean equalWeights;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates lat, long",
            defaultValue = "false")
    public Attribute.Boolean latLong;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "ID of the datastore to read station coordinates from")
    public Attribute.String dataStoreID;
    private double[] statX;
    private double[] statY;
    InputDataStore store = null;
    IDW idw = new IDW();

    boolean invalidDatasetReported = false;

    ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);

    @Override
    public void init() {
        if (dataStoreID != null) {
            store = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }

        // check if store exists
        if (store == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        DataSetDefinition dsDef = store.getDataSetDefinition();
        ArrayList<Object> xList = dsDef.getAttributeValues("X");
        ArrayList<Object> yList = dsDef.getAttributeValues("Y");

        if (xList == null || yList == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": x or y coordinates are not set!");
        }

        if (xList.size() != yList.size()) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + dataStoreID + "\" from " + getInstanceName() + ": Number of x and y coordinates differ!");
            return;
        }

        statX = listToDoubleArray(xList);
        statY = listToDoubleArray(yList);

        boolean isLatLon = latLong != null && latLong.getValue();
        if (isLatLon) {
            idw.init(statX, statY, null, (int) pidw.getValue(), IDW.Projection.LATLON);
        } else {
            idw.init(statX, statY, null, (int) pidw.getValue(), IDW.Projection.ANY);
        }
    }

    @Override
    public void initAll() {
        
        double weights[] = statWeights.getValue();
        int wA[] = statOrder.getValue();

        int n = statX.length;

        if (weights == null || weights.length != n) {
            weights = new double[n];
        }
        if (wA == null || wA.length != n) {
            wA = new int[n];
        }

        if (equalWeights == null || !equalWeights.getValue()) {
            idw.getIDW(entityX.getValue(), entityY.getValue(), null, 1);

            System.arraycopy(idw.getWeights(), 0, weights, 0, n);
            System.arraycopy(idw.getWeightOrder(), 0, wA, 0, n);
        } else if (equalWeights.getValue()) {
            for (int i = 0; i < n; i++) {
                weights[i] = 1. / (double) n;
                wA[i] = i;
            }
        }
        statWeights.setValue(weights);
        statOrder.setValue(wA);
    }

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
                    //checking for minimum
                    if (tVal < this.fixedMinimum.getValue()) {
                        tVal = this.fixedMinimum.getValue();
                    }
                    value = value + tVal;
                } else { //No elevation correction
                    value = value + (data[i] * weights[i]);
                }

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

    public void cleanup() {
        Arrays.fill(statWeights.getValue(), 0);
        if (store != null) {
            try {
                store.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private double[] listToDoubleArray(ArrayList<Object> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = ((Double) o).doubleValue();
            i++;
        }
        return result;
    }
}
