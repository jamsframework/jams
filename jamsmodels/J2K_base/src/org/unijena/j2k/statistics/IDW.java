/*
 * IDW.java
 * Created on 17. November 2005, 14:33
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
package org.unijena.j2k.statistics;

import jams.JAMS;
import jams.data.ArrayPool;
import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author S. Kralisch
 */
public class IDW implements Serializable {

    public enum Projection {

        LATLON, ANY
    };
    /**
     * Calcs distances of specific climate stations from HRU for projected
     * coordinates
     *
     * @param hru Instance of HRU-Class
     * @param stat Instances of relevant Climate Stations
     * @param pidw Power of IDW function
     * @return array of distances
     */
    ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);
    
    double dist[] = null;
    double weights[] = null;
    int wArray[] = null;
    int n = 0;
    double statX[], statY[], statElevation[];
    double pidw;
    Projection proj;

    public void init(double[] statX, double[] statY, double[] statElevation, int pidw, Projection proj) {
        n = statX.length;
        if (dist == null || dist.length != n) {
            dist = new double[n];
        }

        if (weights == null || weights.length != n) {
            weights = new double[n];
        }

        if (wArray == null || wArray.length != n) {
            wArray = new int[n];
        }
        this.proj = proj;
        this.statX = statX;
        this.statY = statY;
        this.pidw = pidw;
        this.statElevation = statElevation;
    }

    public double getElevationCorrectedIDW(double x, double y, double elevation, double w, double data[], int p) {
        if (this.proj == Projection.ANY){            
            this.calcDistances(x, y, statX, statY, pidw);
        }else{            
            this.calcLatLongDistances(x, y, statX, statY, pidw);
        }
        if (data == null){
            this.calcWeights();
        }else{
            this.calcWeights(data);
        }
        this.computeWeightArray();

        if (data == null)
            return 0;
        
        double[] idw_data = memPool.alloc(p);
        double[] idw_weights = memPool.alloc(p);
        double[] idw_elev = memPool.alloc(p);

        //selecting the nidw closest temperature stations and avoiding no data values
        int counter = 0;
        int element = 0;
        while (counter < p && element < wArray.length) {
            int t = wArray[element];
            //check if data is valid or no data
            if (data[t] != JAMS.getMissingDataValue()) {
                idw_data[counter] = data[t];
                idw_weights[counter] = weights[t];
                if (statElevation != null) {
                    idw_elev[counter] = statElevation[t];
                }

                counter++;
            } 
            element++;
        }

        //normalising weights
        double weightsum = 0;
        for (int i = 0; i < counter; i++) {
            weightsum += idw_weights[i];
        }
        for (int i = 0; i < counter; i++) {
            idw_weights[i] = idw_weights[i] / weightsum;
        }

        double result = 0;
        for (int t = 0; t < counter; t++) {
            double deltaElev = elevation - idw_elev[t];
            result += ((deltaElev * w + idw_data[t]) * idw_weights[t]);
        }

        return result;
    }

    public double getIDW(double x, double y, double data[], int p) {
        return getElevationCorrectedIDW(x, y, 0, 0, data, p);
    }

    public double[] getWeights() {       
        return this.weights;//this.calcWeights();
    }

    public int[] getWeightOrder() {
        return this.wArray;//this.computeWeightArray();
    }

    public void calcDistances(double entityX, double entityY, double[] statX, double[] statY, double pidw) {
        //double[] dist = memPool.alloc(statX.length);
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
    }

    /**
     * Calcs distances of specific climate stations from HRU for geographical
     * coordinates
     *
     * @param hru Instance of HRU-Class
     * @param stat Instances of relevant Climate Stations
     * @param pidw Power of IDW function
     * @return array of distances
     */
    private double[] calcLatLongDistances(double entityX, double entityY, double[] statX, double[] statY, double pidw) {
        //radius at the equator in meter
        final double R = 6378137.0;
        //double[] dist = new double[statX.length];

        //Calculating distances of each station to the entity
        for (int s = 0; s < statX.length; s++) {
            dist[s] = R * Math.acos(Math.sin(rad(entityY)) * Math.sin(rad(statY[s]))
                    + Math.cos(rad(entityY)) * Math.cos(rad(statY[s]))
                    * Math.cos(rad(statX[s]) - rad(entityX)));
            //Power pidw, abs for positive values
            dist[s] = Math.abs(Math.pow(dist[s], pidw));
        }
        return dist;
    }

    /**
     * Calcs weight for each Climate Station
     *
     * @param dist the distance array
     * @param nstat number of Climate Stations
     * @return the weight array
     */
    private double[] calcWeights(){
        
        //double[] weight = memPool.alloc(nstat);//new double[nstat];
        double[] temp = memPool.alloc(n);
        double distsum = 0;
        double tempsum = 0;
        //CALCULATING THE WEIGHTS
        for(int i = 0; i < n; i++) {
            distsum += dist[i];
        }
        for(int i = 0; i < n; i++){
            temp[i] = distsum / dist[i];
            tempsum += temp[i];
        }
        for(int s = 0; s < n; s++){
            //if station is identical this station get a weight of 1.0
            //and all others are set to 0.0
            if(dist[s] == 0){
                Arrays.fill(weights, 0.0);                
                weights[s] = 1.0;
                return weights;
            }
            else{
                weights[s] = temp[s] / tempsum;
            }
        }
        memPool.free(temp);
        return weights;
    }

    /**
     * Calcs weight for each Climate Station
     *
     * @param dist the distance array
     * @param nstat number of Climate Stations
     * @return the weight array
     */
    private double[] calcWeights(double[] data) {
        double distsum = 0;
        double tempsum = 0;

        for (int i = 0; i < n; i++) {
            distsum += dist[i];
        }
        for (int i = 0; i < n; i++) {
            if (dist[i] > 0) {
                tempsum += distsum / dist[i];
            }
        }
        for (int s = 0; s < n; s++) {
            //if station is identical this station get a weight of 1.0
            //and all others are set to 0.0
            if (dist[s] == 0 && data[s] != JAMS.getMissingDataValue()) {
                Arrays.fill(weights, 0.0);
                weights[s] = 1.0;
                return weights;
            } else if (dist[s] == 0 && data[s] == JAMS.getMissingDataValue()) {
                weights[s] = 0.0;
            } else {
                weights[s] = (distsum / dist[s]) / tempsum; //temp[s] / tempsum;
            }
        }
        return weights;
    }

    /**
     * Changes the weight array in such a way, that only the weights of
     * "nidw"-stations are kept and the other weights are set to zero. The
     * nidw-weights are recalculated to provide again a sum of 1.0.
     *
     * @param weight the weight array for all stations
     * @param nidw number of relevant stations
     * @return the changed weight array
     */
    /*public double[] calcNidwWeights(double entityX, double entityY, double[] statX, double[] statY, double pidw, int nidw) {
     calcDistances(entityX, entityY, statX, statY, pidw);
     calcWeights();

     int counter = 0;
     int nstat = weights.length;
     int[] temp = new int[nstat];
     double weightsum = 0;

     for (int i = 0; i < nstat; i++) {
     counter = 0;
     for (int k = 0; k < nstat; k++) {
     if (weights[i] > weights[k]) {
     counter++;
     }
     }
     temp[i] = counter;
     }
     for (int i = 0; i < nstat; i++) {
     if (temp[i] < (nstat - nidw)) {
     weights[i] = 0;
     }
     }
     for (int i = 0; i < nstat; i++) {
     weightsum += weights[i];
     }
     for (int i = 0; i < nstat; i++) {
     weights[i] = weights[i] / weightsum;
     }

     return weights;
     }*/
    /**
     * Computes an integer array (wArray) of same lenght as the weight array in
     * such a way that the first element of wArray contains the weight array
     * position with the highest weight value. The second element of wArray the
     * second highest weight etc.
     *
     * @param weight the weight array for all stations
     * @return the index of weights
     */
    //TODO improve the sorting algorithm
    private int[] computeWeightArray() {
        int pos = 0;

        double[] tempWeight = memPool.alloc(n);

        System.arraycopy(weights, 0, tempWeight, 0, n);

        for (int j = 0; j < n; j++) {
            double maxWeight = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < n; i++) {
                if (tempWeight[i] > maxWeight) {
                    maxWeight = weights[i];
                    pos = i;
                }
            }
            tempWeight[pos] = Double.NEGATIVE_INFINITY;
            wArray[j] = pos;
        }
        tempWeight = memPool.free(tempWeight);

        return wArray;
    }

    private static double rad(double decDeg) {
        return (decDeg * Math.PI / 180.);
    }
}
