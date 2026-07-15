/*
 * RegionalisationLocalKriging.java
 * Created on 20. June 2016, 13:15
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
package jams.components.interpolation;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrasstools.gears.utils.math.matrixes.ColumnVector;
import org.jgrasstools.gears.utils.math.matrixes.LinearSystem;
import org.jgrasstools.gears.utils.math.matrixes.MatrixException;

/**
 *
 * @author Raphael Knevels
 */
public class RegionalisationLocalKriging extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "use only stations for elevation correction",
            defaultValue = "false")
    public Attribute.Boolean localElevationCorrection;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station x coordinates")
    public Attribute.DoubleArray statX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of station y coordinates")
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
            description = "Number of neighbor stations",
            defaultValue = "3")
    public Attribute.Integer nNs;

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
            description = "Absolute possible maximum value for data set",
            defaultValue = "Infinity")
    public Attribute.Double fixedMaximum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate")
    public Attribute.Double entityX;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate")
    public Attribute.Double entityY;
    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates lat, long",
            defaultValue = "false")
    public Attribute.Boolean latLong;

    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Type of semivariogram: Spherical Model (0), Exponential Model (1), Gaussian Model (2), Sillian Model (3)",
            defaultValue = "0")
    public Attribute.Integer pSemivariogramType;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Value of nugget")
    public Attribute.Double pNug;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Value of range")
    public Attribute.Double pA;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Value of sill")
    public Attribute.Double pS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The interpolation mode (0 = interpolate on irregular grid, 1 = interpolate on regular grid)",
            defaultValue = "0")
    public Attribute.Integer pMode;

    public enum Projection {
        LATLON, ANY
    };
    
    //stat data
    Projection proj = null;

    //tmp data
    private double[] data, elev, weights, dist;
    

    int n = 0, nNS = 0;
    boolean invalidDatasetReported = false, entityIsStation;

    
    
    @Override
    public void init() {
        
        // check projection
        boolean isLatLon = latLong != null && latLong.getValue();
        if (isLatLon) {
            proj = Projection.LATLON;
        } else {
            proj = Projection.ANY;
        }

        n = statX.getValue().length; // read out number of input stations
        nNS = Math.min(nNs.getValue(), statX.getValue().length); // set number of neighbors for regionalisation
    
        dist = new double[n]; // initialize array which takes the distances to all station points
        data = new double[nNS]; // initialize array which takes the data values of neighbor stations
        elev = new double[nNS]; // initialize array which takes the elevation of neighbor stations
        weights = new double[nNS+1]; // initialize array which takes the weights of neighbor stations  

    }

    /* fuction to convert degree to radiant */
    private double rad(double decDeg) {
        return (decDeg * Math.PI / 180.);
    }

    /* fuction to calculate the distances between entity and stations */
    public void calcDistances(double entityX, double entityY, double[] statX, double[] statY) {
        final double R = 6378137.0;

        if (this.proj == Projection.ANY) {
            
            //Calculating distances of each station to the entity
            for (int s = 0; s < statX.length; s++) {
                double x = entityX - statX[s];
                double y = entityY - statY[s];
                
                //Phytagoras
                dist[s] = Math.sqrt(x * x + y * y); //Math.sqrt(());

            }
        } else {
            
            //Calculating distances of each station to the entity
            for (int s = 0; s < statX.length; s++) {
                dist[s] = R * Math.acos(Math.sin(rad(entityY)) * Math.sin(rad(statY[s]))
                        + Math.cos(rad(entityY)) * Math.cos(rad(statY[s]))
                        * Math.cos(rad(statX[s]) - rad(entityX)));
                
                //Power pidw, abs for positive values
                dist[s] = Math.abs(Math.pow(dist[s], 2.0));
            }
        }
    }

    /* fuction to get the index of the (remaining) station with the minimal distance to entity */
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

    /* fuction to create arrays for kriging operation */
    public double[][] fillStationKrigingArrays(double entityX, double entityY, double entityZ, double[] statX, double[] statY, double[] statZ, int nn, int[] neighborStations) {

        // n + 1 because in last position the entitiy coordinate (x|y|z) is added      
        double[] xStation = new double[nn + 1];
        double[] yStation = new double[nn + 1];
        double[] zStation = new double[nn + 1];
        

        for (int i = 0; i < nn; i++) {
            int t = neighborStations[i]; // neighborStations contains indices from nearest next neighbor stations

            xStation[i] = statX[t]; // x coordinate from neighbor station
            yStation[i] = statY[t]; // y coordinate from neighbor station
            zStation[i] = statZ[t]; // z coordinate from neighbor station
                    }

        // adding entity values to last position of array
        xStation[nn] = entityX;
        yStation[nn] = entityY;
        zStation[nn] = entityZ;
       
        // double[][] filledArrays = {xStation, yStation, zStation, vStation};
        double[][] filledArrays = {xStation, yStation, zStation};

        return filledArrays;
    }

    
    /* fuction was taken from jgrasstools: OmsKriging.java by moovida  */
    private double[][] covMatrixCalculating(double[] x, double[] y, double[] z, int nn) {

        double[][] ap = new double[nn + 1][nn + 1]; // + 1 because of entity value

        for (int j = 0; j < nn; j++) {
            for (int i = 0; i < nn; i++) {
                double rx = x[i] - x[j];
                double ry = y[i] - y[j];
                double rz = 0;
                
                if (this.pMode.getValue() == 0) {
                    rz = z[i] - z[j];
                }
                double tmp = variogram(this.pNug.getValue(), this.pA.getValue(), this.pS.getValue(), rx, ry, rz);

                ap[j][i] = tmp;
                ap[i][j] = tmp;
            }
        }

        for (int i = 0; i < nn; i++) {
            ap[i][nn] = 1.0;
            ap[nn][i] = 1.0;
        }
        
        ap[nn][nn] = 0;
        return ap;
    }

    
    /* fuction was taken from jgrasstools: OmsKriging.java by moovida  */
    private double[] knownTermsCalculation(double[] x, double[] y, double[] z, int nn) {

        double[] gamma = new double[nn + 1]; // last place for entity value which will be 1

        for (int i = 0; i < nn; i++) {
            double rx = x[i] - x[nn];
            double ry = y[i] - y[nn];
            double rz = z[i] - z[nn];

            gamma[i] = variogram(this.pNug.getValue(), this.pA.getValue(), this.pS.getValue(), rx, ry, rz);
        }

        gamma[nn] = 1.0; // value for the entity itself

        return gamma;
    }

    /* fuction was taken from jgrasstools: OmsKriging.java by moovida  */
    private double variogram(double nugget, double range, double sill, double rx, double ry, double rz) {
        
        if (this.pMode.getValue() == 0) {
            rz = rz;
        } else {
            rz = 0;
        }

        double valueVariogram = 0;
        double h2 = Math.sqrt(rx * rx + rz * rz + ry * ry);


        // aus h_kriging.java by MOOVIDA (jgrasstools)         
        double scale = sill - nugget;

        switch (this.pSemivariogramType.getValue()) {
            case 0: // Spherical Model
                if (h2 >= range) {
                    valueVariogram = nugget + scale;
                } else {
                    valueVariogram = nugget + scale * (3 * h2 / (2 * range) - h2 * h2 * h2 / (2 * range * range * range));
                }
                break;
                
            case 1: // Exponential Model
                valueVariogram = nugget + scale * (1 - Math.exp(-3 * h2 / range));
                break;
                
            case 2: // Gaussian Model
                // d = 1 - Math.exp(-3 * d / (range * range));
                // d = nugget + scale * d * d;
                valueVariogram = nugget + scale * (1 - Math.exp(-3 * Math.pow(h2, 2)) / (range * range));
                break;
                
            case 3: // Sillian Model
                valueVariogram = 1 - Math.exp(-3 * h2 / (range * range));
                valueVariogram = nugget + scale * valueVariogram * valueVariogram;
                break;
        }

        return valueVariogram;
    }

    @Override
    public void initAll() {
        int wA[] = statOrder.getValue(); // order of importance of station
        double weights[] = statWeights.getValue(); // get station weights
        this.entityIsStation = false; // boolean to check if entity is station 


        if (weights == null || weights.length != (nNS+1)) {
            weights = new double[nNS+1];
        }
        
        if (wA == null || wA.length != nNS) {
            wA = new int[nNS];
        }

        // calculate distance from entity to all 
        calcDistances(this.entityX.getValue(), this.entityY.getValue(), this.statX.getValue(), this.statY.getValue());

        int p = 0;
        while (p < this.nNS) { // loop to search nearest neighbors 
            
            int min = minIndex(dist); 
            wA[p] = min;

            if (dist[min] == 0) { // if entity equals station
                Arrays.fill(wA, 0);
                Arrays.fill(weights, 0);
                wA[p] = min; 
                weights[p] = 1.0;
                this.entityIsStation = true;
                break;
            }
            
            dist[min] = Double.MAX_VALUE;
            p++;
        }
        
            // CALCULATE WEIGHTS
        
             // fill arrays for kriging procedure
            double[][] filledArrays = fillStationKrigingArrays(this.entityX.getValue(), this.entityY.getValue(), this.entityElevation.getValue(), this.statX.getValue(), this.statY.getValue(), this.statElevation.getValue(), this.nNS, wA);
            double[] xStation = filledArrays[0]; // x coordinates of stations + entity
            double[] yStation = filledArrays[1]; // y coordinates of stations + entity
            double[] zStation = filledArrays[2]; // z coordinates of stations + entity
         
            
            if (!this.entityIsStation) { // check if entity is station
               
                /* START KRIGING PROCESS: GETTING WEIGHTS - OMSKriging from JGRASSTOOLS by MOOVIDA */
                
                try {
                    // calculating the covariance matrix.

                    double[][] covarianceMatrix = covMatrixCalculating(xStation, yStation, zStation, this.nNS);

                     // extract the coordinate of the points where interpolated.
                     // initialize the solution and its variance vector.
                     // calculating the right hand side of the kriging linear system.
                    double[] knownTerm = knownTermsCalculation(xStation, yStation, zStation, this.nNS);

                    // solve the linear system, where the result is the weight.                 
                    ColumnVector knownTermColumn = new ColumnVector(knownTerm);
                    LinearSystem linearSystem = new LinearSystem(covarianceMatrix);
                    ColumnVector solution = linearSystem.solve(knownTermColumn, true);

                    // contains values with kriging weights
                    weights = solution.copyValues1D();
                    
                } catch (MatrixException ex) {
                    Logger.getLogger(RegionalisationLocalKriging.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        statWeights.setValue(weights);
        statOrder.setValue(wA);
    }

    int counter = 0;

    @Override
    public void run() {
        
        //Retreiving data, elevations and weights
        double[] regCoeff = this.regCoeff.getValue();
        double gradient = regCoeff[1];
        double rsq = regCoeff[2];

        double[] sourceElevations = statElevation.getValue();
        double[] sourceData = dataArray.getValue(); // station data
        double[] sourceWeights = statWeights.getValue(); // station weights for special entity
        double targetElevation = entityElevation.getValue();
        int[] wA = this.statOrder.getValue();

        double value = 0; // final interpolated value
        double deltaElev = 0; //... is used when elevation correction is applied
        
        
        /* CALCULATION FOR LOCAL ELEVATION CORRECTION */        
        if (this.localElevationCorrection.getValue()) { // calculate regression if local elevation correction is wished
            //x -> elev
            //y -> data
            double xq = 0, yq = 0;
            int counter = 0, element = 0;

            while (element < nNS) {
                int t = wA[element];
                if (sourceData[t] != JAMS.getMissingDataValue()) {
                    xq += sourceElevations[t];
                    yq += sourceData[t];
                    counter++;
                }
                element++;
            }
            xq /= counter;
            yq /= counter;

            double covxy = 0, covx = 0, covy = 0;
            
            element = 0;
            while (element < nNS) {
                int t = wA[element];
                if (sourceData[t] != JAMS.getMissingDataValue()) {
                    double p = (sourceElevations[t] - xq);
                    double q = (sourceData[t] - yq);
                    covxy += p * q;
                    covx += p * p;
                    covy += q * q;
                }
                element++;
            }
            gradient = covxy / covx;
            
            //rsq = covxy / Math.sqrt(covx * covy);
            //rsq *= rsq;
            
            rsq = (covxy * covxy) / (covx * covy);
        } // end of regression calculation
        
        Arrays.fill(data, 0.0);
        Arrays.fill(elev, 0.0);
        Arrays.fill(weights,0.0);

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
        int counter = 0, element = 0;
        boolean valid = false;


        /* DATA CHECK AND ALLOCATION */ 
        while (counter < nNS) { // DATA CHECK
            
            int t = wA[element]; // t is index of neighbor station: first t is the most close station
            
            //check if data is valid or no data
            if (sourceData[t] == JAMS.getMissingDataValue()) {
                element++;
                if (element >= wA.length) {
                    //getModel().getRuntime().println("BREAK1: too less data NIDW had been reduced!");
                    break;
                }
            } else { // data is valid
                
                valid = true;
                data[counter] = sourceData[t]; // data contains data of valid neighbor stations
                elev[counter] = sourceElevations[t]; // elev contains elevation of valid neighbor stations
                weights[counter] = sourceWeights[element]; // get station weight, if one station is invalid, next will receive its weight, therefore station with less weight will be jumped over
                
                counter++;
                element++;
                if (element >= wA.length) {
                    break;
                }
            }
        } // end DATA CHECK

        //   counter is now the amount of valid stations
        if (valid) { // interpolation of entity can start

                       // calculate value of interpolated point/entity
            for (int i = 0; i < counter; i++) {
                if ((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                    deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + data[i]) * weights[i]);      
                    value = value + tVal;
                } else { //No elevation correction
                    value = value + (data[i] * weights[i]);
                }
            }
        // end of "if(valid)" and thus interpolation procedures
        } else { // if data is not valid...)
            if (!invalidDatasetReported) {     //only report once
                //in this case simulation should end, because it affects model behaviour seriously!
                getModel().getRuntime().sendHalt("Invalid dataset found while regionalizing data in component " + this.getInstanceName() + "."
                        + "\nThis might occur if all of the provided values are missing data values.");
                invalidDatasetReported = true;
            }
            value = JAMS.getMissingDataValue();
        }
       
        
        //checking for minimum
        value = Math.max(this.fixedMinimum.getValue(), value);
        value = Math.min(this.fixedMaximum.getValue(), value);

        dataValue.setValue(value); // write interpolated value
    }

    public static void main(String[] args) {
        RegionalisationLocalKriging reg = new RegionalisationLocalKriging();

        reg.dataValue = JAMSDataFactory.createDouble();
        reg.elevationCorrection = JAMSDataFactory.createBoolean();
        reg.entityElevation = JAMSDataFactory.createDouble();
        reg.entityX = JAMSDataFactory.createDouble();
        reg.entityY = JAMSDataFactory.createDouble();
        reg.fixedMaximum = JAMSDataFactory.createDouble();
        reg.fixedMinimum = JAMSDataFactory.createDouble();
        reg.latLong = JAMSDataFactory.createBoolean();
        reg.nNs = JAMSDataFactory.createInteger();
        reg.rsqThreshold = JAMSDataFactory.createDouble();
        reg.regCoeff = JAMSDataFactory.createDoubleArray();
        reg.statElevation = JAMSDataFactory.createDoubleArray();
        reg.statOrder = JAMSDataFactory.createIntegerArray();
        reg.statWeights = JAMSDataFactory.createDoubleArray();
        reg.statX = JAMSDataFactory.createDoubleArray();
        reg.statY = JAMSDataFactory.createDoubleArray();
        reg.dataArray = JAMSDataFactory.createDoubleArray();
        reg.localElevationCorrection = JAMSDataFactory.createBoolean();
        reg.elevationCorrection.setValue(true);
        reg.fixedMinimum.setValue(-999999);
        reg.fixedMaximum.setValue(999999);
        reg.nNs.setValue(4);
        reg.latLong.setValue(false);
        reg.localElevationCorrection.setValue(true);

        reg.statElevation.setValue(new double[]{100, 200, 300, 400});
        reg.statX.setValue(new double[]{0, 0, 1, 1});
        reg.statY.setValue(new double[]{0, 1, 0, 1});
        reg.regCoeff.setValue(new double[3]);
        reg.rsqThreshold.setValue(-1.0);

        // KRIGING
        reg.pNug = JAMSDataFactory.createDouble();
        reg.pS = JAMSDataFactory.createDouble();
        reg.pA = JAMSDataFactory.createDouble();
        reg.pMode = JAMSDataFactory.createInteger();
        reg.pSemivariogramType = JAMSDataFactory.createInteger();
        reg.pNug.setValue(0.0);
        reg.pS.setValue(1.0);
        reg.pA.setValue(5.0);
        reg.pSemivariogramType.setValue(0);
        reg.pMode.setValue(1); // regular grid

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

        
        for (int i=0;i < 10;i++){
            System.out.println("Entity " + i + "---------------------------------------");
            reg.entityX.setValue(entityX[i]);
            reg.entityY.setValue(entityY[i]);
            reg.entityElevation.setValue(entityElevation[i]);            
            reg.initAll();
            System.out.print("Station Order: ");
            System.out.println(Arrays.toString(reg.statOrder.getValue()));
            System.out.print("Local Kriging Weights: ");
            System.out.println(Arrays.toString(reg.statWeights.getValue()));
            for (int j = 0; j < 8; j++) {
                reg.dataArray.setValue(dataArray[j]);                               
                reg.run();            
                System.out.println("Timestep: " + j + " | Local Kriging -> " + reg.dataValue.getValue());
            }
            
            System.out.println();
        }
    }

}
