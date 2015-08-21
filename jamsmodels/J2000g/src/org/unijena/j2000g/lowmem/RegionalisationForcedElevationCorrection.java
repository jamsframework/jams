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
package org.unijena.j2000g.lowmem;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;

/**
 *
 * @author Peter Krause
 */
public class RegionalisationForcedElevationCorrection extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "gradient for static elevation correction",
            defaultValue = "0")
    public Attribute.Double gradient;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "gradient for static elevation correction",
            defaultValue = "0")
    public Attribute.Double v0;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station elevations")
    public Attribute.DoubleArray statX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station elevations")
    public Attribute.DoubleArray statY;

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
            description = "Number of IDW stations",
            defaultValue = "3")
    public Attribute.Integer nidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Apply elevation correction to measured data")
    public Attribute.Boolean elevationCorrection;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Minimum r² value for elevation correction application")
    public Attribute.Double rsqThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible minimum value for data set",
            defaultValue = "-Infinity")
    public Attribute.Double fixedMinimum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Absolute possible minimum value for data set",
            defaultValue = "Infinity")
    public Attribute.Double fixedMaximum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate")
    public Attribute.Double entityX;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate")
    public Attribute.Double entityY;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function")
    public Attribute.Double pidw;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates lat, long",
            defaultValue = "false")
    public Attribute.Boolean latLong;

    public enum Projection {

        LATLON, ANY
    };

    private class Station {

        int index;
        double elevation;
        double value;
        double weight;
    }

    //stat data
    Projection proj = null;

    //tmp data
    private double[] data, elev, weights, dist;
    private Station stationCache[];

    int n = 0, nIDW = 0, nIDWAvailable;
    double pIDW = 0;
    boolean invalidDatasetReported = false;

    @Override
    public void init() {
        boolean isLatLon = latLong != null && latLong.getValue();
        if (isLatLon) {
            proj = Projection.LATLON;
        } else {
            proj = Projection.ANY;
        }

        n = statX.getValue().length;
        nIDW = Math.min(nidw.getValue(), n);
        pIDW = pidw.getValue();

        dist = new double[n];
        data = new double[n];
        elev = new double[n];
        weights = new double[n];

        stationCache = new Station[nIDW];
        for (int i = 0; i < nIDW; i++) {
            stationCache[i] = new Station();
        }
    }

    private double rad(double decDeg) {
        return (decDeg * Math.PI / 180.);
    }

    private void initWeights(int nSearch) {
        double weights[] = statWeights.getValue();
        int wA[] = statOrder.getValue();
        double distsum = 0;

        if (weights == null || weights.length != nSearch) {
            weights = new double[nSearch];
        }
        if (wA == null || wA.length != nSearch) {
            wA = new int[nSearch];
        }

        calcDistances(this.entityX.getValue(), this.entityY.getValue(), this.statX.getValue(), this.statY.getValue(), this.pidw.getValue());

        int p = 0;
        while (p < nSearch) {
            int min = minIndex(dist);
            weights[p] = dist[min];
            wA[p] = min;

            if (dist[min] == 0) {
                Arrays.fill(weights, 0.0);
                Arrays.fill(wA, 0);
                wA[p] = min;
                weights[p] = 1.0;
                break;
            }
            dist[min] = Double.MAX_VALUE;
            p++;
        }

        //CALCULATING THE WEIGHTS
        for (int i = 0; i < nSearch; i++) {
            distsum += weights[i];
        }

        for (int i = 0; i < nSearch; i++) {
            if (weights[i] != 0.0) {
                weights[i] = distsum / weights[i];
            }
        }

        statWeights.setValue(weights);
        statOrder.setValue(wA);
        //@DEBUG
        //calcDistances(this.entityX.getValue(), this.entityY.getValue(), this.statX.getValue(), this.statY.getValue(), this.pidw.getValue());
    }

    public void calcDistances(double entityX, double entityY, double[] statX, double[] statY, double pidw) {
        final double R = 6378137.0;

        if (this.proj == Projection.ANY) {
            //Calculating distances of each station to the entity
            for (int s = 0; s < statX.length; s++) {
                double x = entityX - statX[s];
                double y = entityY - statY[s];
                //Phytagoras
                dist[s] = x * x + y * y;//Math.sqrt(());
                //calc the root and power to pidw/2, abs for positive values
                if (pidw != 2) {
                    dist[s] = Math.pow(dist[s], pidw / 2.0);
                }
            }
        } else {
            //Calculating distances of each station to the entity
            for (int s = 0; s < statX.length; s++) {
                dist[s] = R * Math.acos(Math.sin(rad(entityY)) * Math.sin(rad(statY[s]))
                        + Math.cos(rad(entityY)) * Math.cos(rad(statY[s]))
                        * Math.cos(rad(statX[s]) - rad(entityX)));
                //Power pidw, abs for positive values
                dist[s] = Math.abs(Math.pow(dist[s], pidw));
            }
        }
    }

    private int minIndex(double[] v) {
        double min = Double.POSITIVE_INFINITY;
        int p = 0;
        for (int i = 0; i < v.length; i++) {
            if (v[i] < min) {
                min = v[i];
                p = i;
            }
        }
        return p;
    }

    @Override
    public void initAll() {
        int nSearch = Math.min(2 * nIDW, n);
        initWeights(nSearch);
    }

    private boolean fillStationCache() {
        int[] wA = this.statOrder.getValue();
        double[] sourceData = dataArray.getValue();
        double[] sourceWeights = statWeights.getValue();
        double[] sourceElevations = statElevation.getValue();

        int counter = 0, element = 0, nSearch = wA.length;
        while (counter < nIDW) {
            if (element >= nSearch) {
                if (nSearch == n) {
                    break;
                }
                nSearch = Math.min(2 * nSearch, n);
                initWeights(nSearch);
                wA = this.statOrder.getValue();
                sourceData = dataArray.getValue();
                sourceWeights = statWeights.getValue();
                sourceElevations = statElevation.getValue();
            }
            int t = wA[element];
            if (sourceData[t] != JAMS.getMissingDataValue()) {
                stationCache[counter].elevation = sourceElevations[t];
                stationCache[counter].weight = sourceWeights[element];
                stationCache[counter].value = sourceData[t];
                stationCache[counter].index = t;
                counter++;
            }
            element++;
        }
        nIDWAvailable = counter;

        if (nIDWAvailable == 0 && !invalidDatasetReported) {     //only report once
            //in this case simulation should end, because it affects model behaviour seriously!
            getModel().getRuntime().sendHalt("Invalid dataset found while regionalizing data in component " + this.getInstanceName() + "."
                    + "\nThis might occur if all of the provided values are missing data values.");
            invalidDatasetReported = true;
            return false;
        }
        return true;
    }

    //int counter = 0;
    int mycounter = 0;
    double TOTAL1 = 0, TOTAL2 = 0;

    @Override
    public void run() {
        double targetElevation = entityElevation.getValue();

        double value = 0;
        double deltaElev = 0;

        if (!fillStationCache()) {
            value = JAMS.getMissingDataValue();
            return;
        }
        
        Arrays.fill(data, 0.0);
        Arrays.fill(weights, 0.0);
        Arrays.fill(elev, 0.0);

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
        //normalising weights
        double weightsum = 0;

        for (int i = 0; i < nIDWAvailable; i++) {
            data[i] = stationCache[i].value;
            weights[i] = stationCache[i].weight;
            elev[i] = stationCache[i].elevation;

            weightsum += weights[i];
        }

        //double totalTVAL = 0;
        for (int i = 0; i < nIDWAvailable; i++) {
            weights[i] /= weightsum;

            if (elevationCorrection.getValue()) {  //Elevation correction is applied
                deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                double a = v0.getValue() + elev[i]*gradient.getValue();
                double b = v0.getValue() + targetElevation*gradient.getValue();
                double correctionFactor = b/a;
                
             
                double tVal = (data[i] * correctionFactor) * weights[i];
                //totalTVAL += tVal;
                value = value + tVal;
            } else { //No elevation correction
                value = value + (data[i] * weights[i]);
            }
        }
        /*output += "\tTVAL:" + totalTVAL;
         if (getInstanceName().contains("Precip")) {
         TOTAL1 += totalTVAL;
         TOTAL2 += value;
         output += "\t" + TOTAL1 / TOTAL2;
         }*/

        //checking for minimum
        value = Math.max(this.fixedMinimum.getValue(), value);
        value = Math.min(this.fixedMaximum.getValue(), value);

        dataValue.setValue(value);
        /*if (this.getInstanceName().contains("Precip")) {
         mycounter++;
         System.out.println(output);
         }*/
    }

    public static void main(String[] args) {
        RegionalisationForcedElevationCorrection reg = new RegionalisationForcedElevationCorrection();

        reg.dataValue = JAMSDataFactory.createDouble();
        reg.elevationCorrection = JAMSDataFactory.createBoolean();
        reg.entityElevation = JAMSDataFactory.createDouble();
        reg.entityX = JAMSDataFactory.createDouble();
        reg.entityY = JAMSDataFactory.createDouble();
        reg.fixedMaximum = JAMSDataFactory.createDouble();
        reg.fixedMinimum = JAMSDataFactory.createDouble();
        reg.latLong = JAMSDataFactory.createBoolean();
        reg.nidw = JAMSDataFactory.createInteger();
        reg.pidw = JAMSDataFactory.createDouble();
        reg.rsqThreshold = JAMSDataFactory.createDouble();
        reg.statElevation = JAMSDataFactory.createDoubleArray();
        reg.statOrder = JAMSDataFactory.createIntegerArray();
        reg.statWeights = JAMSDataFactory.createDoubleArray();
        reg.statX = JAMSDataFactory.createDoubleArray();
        reg.statY = JAMSDataFactory.createDoubleArray();
        reg.dataArray = JAMSDataFactory.createDoubleArray();
        reg.elevationCorrection.setValue(true);
        reg.fixedMinimum.setValue(-999999);
        reg.fixedMaximum.setValue(999999);
        reg.pidw.setValue(2.0);
        reg.nidw.setValue(4);
        reg.latLong.setValue(false);

        reg.statElevation.setValue(new double[]{100, 200, 300, 400});
        reg.statX.setValue(new double[]{0, 0, 1, 1});
        reg.statY.setValue(new double[]{0, 1, 0, 1});
        reg.rsqThreshold.setValue(-1.0);
        reg.init();

        double entityX[] = new double[]{0.5, 0, 1, 0, 1, 0.25, 0.5, 0.75, 0.25, 0.5};
        double entityY[] = new double[]{0.5, 1, 0, 0, 1, 0.25, 0.25, 0.25, 0.75, 0.75};
        double entityElevation[] = new double[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100};

        double dataArray[][] = new double[][]{
            {1, 2, 1, 2},
            {2, 3, 4, 5},
            {0, 0, 0, 1},
            {2, 0, 0, 0},
            {4, 2, 1, 0},
            {5, 5, 5, 5},
            {1, 2, 1, 2},
            {0, 0, 0, 0},};

        for (int i = 0; i < 10; i++) {
            System.out.println("Entity " + i);
            reg.entityX.setValue(entityX[i]);
            reg.entityY.setValue(entityY[i]);
            reg.entityElevation.setValue(entityElevation[i]);
            reg.initAll();
            System.out.println(Arrays.toString(reg.statOrder.getValue()));
            System.out.println(Arrays.toString(reg.statWeights.getValue()));
            for (int j = 0; j < 8; j++) {
                reg.dataArray.setValue(dataArray[j]);
                reg.run();
                System.out.println("Timestep: " + j + "->" + reg.dataValue.getValue());
            }
        }
    }

}
