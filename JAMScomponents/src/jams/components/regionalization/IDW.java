/*
 * IDW.java
 * Created on 17. November 2005, 14:33
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.regionalization;

/**
 *
 * @author S. Kralisch
 */
public class IDW {
    
    /**
     * Calcs distances of specific climate stations from HRU
     * @param hru Instance of HRU-Class
     * @param stat Instances of relevant Climate Stations
     * @param pidw Power of IDW function
     * @return array of distances
     */
    public static double[] calcDistances(double entityX, double entityY, double[] statX, double[] statY, double pidw){
        double[] dist = new double[statX.length];
        //Calculating distances of each station to the entity
        for(int s = 0; s < statX.length; s++){
            double x = entityX - statX[s];
            double y = entityY - statY[s];
            //Phytagoras
            dist[s]  = Math.sqrt((Math.pow(x,2)+Math.pow(y,2)));
            //Power pidw, abs for positive values
            dist[s]  = Math.abs(Math.pow(dist[s],pidw));
        }
        return dist;
    }
    
    public static double[] equalWeights(int nStat){
    	double[] weights = new double[nStat];
    	for(int i = 0; i < nStat; i++)
    		weights[i] = 1. / (double)nStat;
    	return weights;
    }
    /**
     * Calcs weight for each Climate Station
     * @param dist the distance array
     * @param nstat number of Climate Stations
     * @return the weight array
     */
    public static double[] calcWeights(double[] dist){
        int nstat = dist.length;
        double[] weight = new double[nstat];
        double[] temp = new double[nstat];
        double distsum = 0;
        double tempsum = 0;
        //CALCULATING THE WEIGHTS
        for(int i = 0; i < nstat; i++)
            distsum += dist[i];
        for(int i = 0; i < nstat; i++){
            temp[i] = distsum / dist[i];
            tempsum += temp[i];
        }
        for(int s = 0; s < nstat; s++){
            //if station is identical this station get a weight of 1.0
            //and all others are set to 0.0
            if(dist[s] == 0){
                for(int j = 0; j < nstat; j++)
                    weight[j] = 0.0;
                weight[s] = 1.0;
                return weight;
            }
            else{
                weight[s] = temp[s] / tempsum;
            }
        }
        return weight;
    }
    
    /**
     * Changes the weight array in such a way, that only the weights of
     * "nidw"-stations are kept and the other weights are set to zero.
     * The nidw-weights are recalculated to provide again a sum of 1.0.
     * @param weight the weight array for all stations
     * @param nidw number of relevant stations
     * @return the changed weight array
     */
    public static double[] calcNidwWeights(double entityX, double entityY, double[] statX, double[] statY, double pidw, int nidw){
        
        double[] distances = calcDistances(entityX, entityY, statX, statY, pidw);
        double[] weights = calcWeights(distances);
        int counter = 0;
        int nstat = weights.length;
        int[] temp = new int[nstat];
        double weightsum = 0;
        int[] indicator = new int[nstat];
        for(int i = 0; i < nstat; i++){
            counter = 0;
            for(int k = 0; k < nstat; k++){
                if(weights[i] > weights[k])
                    counter++;
            }
            temp[i] = counter;
        }
        for(int i = 0; i < nstat; i++){
            if(temp[i] < (nstat - nidw)){
                weights[i] = 0;
            }
        }
        for(int i = 0; i < nstat; i++)
            weightsum += weights[i];
        for(int i = 0; i < nstat; i++)
            weights[i] = weights[i] / weightsum;
        
        return weights;
    }

    /**
     * Changes the weight array in such a way, that only the weights of
     * "nidw"-stations are kept and the other weights are set to zero.
     * The nidw-weights are recalculated to provide again a sum of 1.0.
     * @param weight the weight array for all stations
     * @param nidw number of relevant stations
     * @return the changed weight array
     */
    public static double[] calcIDWeights(double entityX, double entityY, double[] statX, double[] statY, double pidw){

        double[] distances = calcDistances(entityX, entityY, statX, statY, pidw);
        double[] weights = calcWeights(distances);
        int counter = 0;
        int nstat = weights.length;
        int[] temp = new int[nstat];
        double weightsum = 0;
        int[] indicator = new int[nstat];
        for(int i = 0; i < nstat; i++){
            counter = 0;
            for(int k = 0; k < nstat; k++){
                if(weights[i] > weights[k])
                    counter++;
            }
            temp[i] = counter;
        }
        
        for(int i = 0; i < nstat; i++)
            weightsum += weights[i];
        for(int i = 0; i < nstat; i++)
            weights[i] = weights[i] / weightsum;

        return weights;
    }
    
    /**
     * Computes an integer array (wArray) of same lenght as the weight array
     * in such a way that the first element of wArray contains the weight 
     * array position with the highest weight value. The second element of
     * wArray the second highest weight etc.
     * @param weight the weight array for all stations
     * @return the index of weights
     */
    public static int[] computeWeightArray(double[] weights){
        int counter = 0;
        int pos = 0;
        int nstat = weights.length;
        int[] wArray = new int[nstat];
        double[] tempWeight = new double[nstat];
        for(int i = 0; i < nstat; i++)
            tempWeight[i] = weights[i];
        
        double maxWeight = -9;
        
        for(int j = 0; j < nstat; j++){
            for(int i = 0; i < nstat; i++){
                counter = 0;
                if(tempWeight[i] > maxWeight){
                    maxWeight = weights[i];
                    pos = i;
                }
            }
            tempWeight[pos] = -9;
            maxWeight = -9;
            wArray[j] = pos;
        }
        return wArray;
    }
}
