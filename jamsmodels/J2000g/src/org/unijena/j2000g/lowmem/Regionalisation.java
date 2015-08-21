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
import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 * @author Peter Krause
 */
public class Regionalisation extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "use only pidw stations for elevation correction",
            defaultValue = "false")
    public Attribute.Boolean localElevationCorrection;
    
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
    //stat data
    Projection proj = null;
        
    //tmp data
    private double[] data, elev, weights, dist;
            
    int n = 0, nIDW=0;
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
        nIDW = Math.min(nidw.getValue(),statX.getValue().length);
        pIDW = pidw.getValue();
                
        dist = new double[n];
        data = new double[nIDW];
        elev = new double[nIDW];
        weights = new double[nIDW];
    }

    private double rad(double decDeg) {
        return (decDeg * Math.PI / 180.);
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
    
    private int minIndex(double []v){
        double min = Double.POSITIVE_INFINITY;
        int p = 0;
        for(int i=0;i<v.length;i++){
            if (v[i]<min){
                min = v[i];
                p = i;
            }            
        }
        return p;
    }
        
    @Override
    public void initAll() {        
        double weights[] = statWeights.getValue();
        int wA[] = statOrder.getValue();
        double distsum = 0;
               
        if (weights == null || weights.length != n) {
            weights = new double[nIDW];
        }
        if (wA == null || wA.length != nIDW) {
            wA = new int[nIDW];
        }
                
        calcDistances(this.entityX.getValue(), this.entityY.getValue(), this.statX.getValue(), this.statY.getValue(), this.pidw.getValue());
         
        int p=0;
        while (p<this.nIDW){
            int min = minIndex(dist);
            weights[p] = dist[min];
            wA[p] = min;
            
            if (dist[min] == 0){
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
        for(int i = 0; i < this.nIDW; i++) {
            distsum += weights[i];
        }
        
        for(int i = 0; i < this.nIDW; i++){
            if (weights[i]!=0.0){
                weights[i] = distsum / weights[i];    
            }            
        }        
            
        statWeights.setValue(weights);
        statOrder.setValue(wA);
        //@DEBUG
        calcDistances(this.entityX.getValue(), this.entityY.getValue(), this.statX.getValue(), this.statY.getValue(), this.pidw.getValue());
    }

    @Override
    public void run() {
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

        if (this.localElevationCorrection.getValue()) {
        //bestimme regression
            //x -> elev
            //y -> data
            double xq = 0, yq = 0;
            int counter = 0, element = 0;
            //for (int i = 0; i < n; i++) {
            while (element < nIDW) {
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
            //for (int i = 0; i < n; i++) {
            element = 0;
            while (element < nIDW) {
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
            if (covx == 0 || covy == 0){
                rsq = 0;
            }else{
                rsq = (covxy*covxy) / (covx * covy);
            }
        }
        Arrays.fill(data,0.0);
        Arrays.fill(weights,0.0);
        Arrays.fill(elev,0.0);

//@TODO: Recheck this for correct calculation, the Doug Boyle Problem!!
        int counter = 0, element = 0;
        boolean valid = false;

        //normalising weights
        double weightsum = 0;
                
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
                weights[counter] = sourceWeights[element];
                elev[counter] = sourceElevations[t];

                weightsum += weights[counter];
                
                counter++;
                element++;
                if (element >= wA.length) {
                    break;
                }
            }
        }
        
        if (valid) {
            for (int i = 0; i < counter; i++) {
                weights[i] /= weightsum;
                
                if ((rsq >= rsqThreshold.getValue()) && (elevationCorrection.getValue())) {  //Elevation correction is applied
                    deltaElev = targetElevation - elev[i];  //Elevation difference between unit and Station
                    double tVal = ((deltaElev * gradient + data[i]) * weights[i]);     
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

        //checking for minimum
        value = Math.max(this.fixedMinimum.getValue(), value);
        value = Math.min(this.fixedMaximum.getValue(), value);

        dataValue.setValue(value);
    }
    
    public static void main(String[] args) {
        Regionalisation reg = new Regionalisation();
        
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
        reg.regCoeff = JAMSDataFactory.createDoubleArray();
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
        
        reg.statElevation.setValue(new double[]{100,200,300,400});
        reg.statX.setValue(new double[]{0,0,1,1});
        reg.statY.setValue(new double[]{0,1,0,1});
        reg.regCoeff.setValue(new double[3]);
        reg.rsqThreshold.setValue(-1.0);
        reg.init();
        
        double entityX[] = new double[]{0.5,0,1,0,1,0.25,0.5 ,0.75,0.25,0.5};
        double entityY[] = new double[]{0.5,1,0,0,1,0.25,0.25,0.25,0.75,0.75};
        double entityElevation[] = new double[]{100,100,100,100,100,100,100,100,100,100};
        
        double dataArray[][] = new double[][]{
            {1,2,1,2},
            {2,3,4,5},
            {0,0,0,1},
            {2,0,0,0},
            {4,2,1,0},
            {5,5,5,5},
            {1,2,1,2},
            {0,0,0,0},
        };
        
        for (int i=0;i<10;i++){
            System.out.println("Entity " + i );
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
