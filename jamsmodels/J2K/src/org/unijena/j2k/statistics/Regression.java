/*
 * Regression.java
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
import java.util.Arrays;

/**
 *
 * @author S. Kralisch
 */
public class Regression {

    public static double[] calcLinReg(double[] xData, double[] yData){
        double weight[] = new double[xData.length];
        Arrays.fill(weight, 1.0);
        return calcLinReg(xData, yData, weight);
    }
    /**
     * Calcs coefficients of linear regression between x, y data
     * @param xData the independent data array (x)
     * @param yData the dependent data array (y)
     * @return (intercept, gradient, r²)
     */
    public static double[] calcLinReg(double[] xData, double[] yData, double[] weight){
        double sumYValue = 0;
        double meanYValue = 0;
        double sumXValue = 0;
        double meanXValue = 0;
        double sumX = 0;
        double sumY = 0;
        double prod = 0;
        double NODATA = JAMS.getMissingDataValue();
        int nstat = xData.length;
        double[] regCoef = new double[3]; //(intercept, gradient, r²)
        int counter = 0;
        //calculating sums
        for(int i = 0; i < nstat; i++){
            if((yData[i] != NODATA) && (xData[i] != NODATA)){
                sumYValue += weight[i]*yData[i];
                sumXValue += weight[i]*xData[i];
                counter++;
            }
        }
        //calculating means
        meanYValue = sumYValue / counter;
        meanXValue = sumXValue / counter;
        
        //calculating regression coefficients
        for(int i = 0; i < nstat; i++){
            if((yData[i] != NODATA) && (xData[i] != NODATA)){
                sumX += Math.pow((xData[i] - meanXValue), 2);
                sumY += Math.pow((yData[i] - meanYValue), 2);
                prod += ((xData[i] - meanXValue)*(yData[i] - meanYValue));
            }
        }
        if(sumX > 0 && sumY > 0){
            regCoef[1] = prod / sumX;  //gradient
            regCoef[0] = meanYValue - regCoef[1] * meanXValue; //intercept
            regCoef[2] = Math.pow((prod / Math.sqrt(sumX * sumY)), 2); //r²
        }
        else{
            regCoef[1] = 0;
            regCoef[0] = 0;
            regCoef[2] = 0;
        }
        
        return regCoef;
    }
}
