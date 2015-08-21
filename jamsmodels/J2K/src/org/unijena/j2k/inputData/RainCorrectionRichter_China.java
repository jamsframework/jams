/*
 * RainCorrectionRichter.java
 * Created on 24. November 2005, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title = "RainCorrection_Richter",
author = "Peter Krause",
description = "Applies correction according to RICHTER 1985 for measured daily precip sums",
version = "1.0_0",
date = "2011-05-30")
public class RainCorrectionRichter_China extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the precip values",
    unit = "mm")
    public Attribute.DoubleArray precip;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temperature for the correction function",
    unit = "°C")
    public Attribute.DoubleArray temperature;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "wind for the correction function")
    public Attribute.DoubleArray wind;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "corrected precip values",
    unit = "mm")
    public Attribute.DoubleArray rcorr;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of temperature station elevations",
    unit = "m")
    public Attribute.DoubleArray tempElevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of temperature station's x coordinate")
    public Attribute.DoubleArray tempXCoord;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of temperature station's y coordinate")
    public Attribute.DoubleArray tempYCoord;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Regression coefficients for temperature")
    public Attribute.DoubleArray tempRegCoeff;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Regression coefficients for wind")
    public Attribute.DoubleArray windRegCoeff;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of temperature station elevations",
    unit = "m")
    public Attribute.DoubleArray windElevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Array of wind station's x coordinate")
    public Attribute.DoubleArray windXCoord;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Array of wind station's y coordinate")
    public Attribute.DoubleArray windYCoord;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of precip station elevations",
    unit = "m")
    public Attribute.DoubleArray rainElevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of precip station's x coordinate")
    public Attribute.DoubleArray rainXCoord;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "array of precip station's y coordinate")
    public Attribute.DoubleArray rainYCoord;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "number of temperature station for IDW")
    public Attribute.Integer tempNIDW;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "number of wind station for IDW")
    public Attribute.Integer windNIDW;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "power for IDW function")
    public Attribute.Double pIDW;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "regression threshold")
    public Attribute.Double regThres;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "snow_trs",
    upperBound = 5.0,
    lowerBound = -5.0,
    unit = "°C")
    public Attribute.Double snow_trs;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "snow_trans",
    upperBound = 10,
    lowerBound = 0,
    unit = "K")
    public Attribute.Double snow_trans;
    
    ArrayPool<double[]> memPool = new ArrayPool<double[]>(double.class);
    IDW idwTemp = new IDW();
    IDW idwWind = new IDW();

    @Override
    public void run() {

        double[] precip = this.precip.getValue();
        double[] temperature = this.temperature.getValue();
        double[] wind = this.wind.getValue();
        
        int n = precip.length;

        double[] rcorr = this.rcorr.getValue();
        if (rcorr == null || rcorr.length != n) {
            rcorr = memPool.alloc(n);
        }
        //parameterization of rain stations        
        double[] rainElev = rainElevation.getValue();
        double[] rainX = rainXCoord.getValue();
        double[] rainY = rainYCoord.getValue();

        double rsqTemp = this.tempRegCoeff.getValue()[2];
        double gradTemp = this.tempRegCoeff.getValue()[1];
        
        double rsqWind = this.tempRegCoeff.getValue()[2];
        double gradWind = this.tempRegCoeff.getValue()[1];

        idwTemp.init(this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.tempElevation.getValue(), (int) this.pIDW.getValue(), IDW.Projection.ANY);
        idwWind.init(this.windXCoord.getValue(), this.windYCoord.getValue(), this.windElevation.getValue(), (int) this.pIDW.getValue(), IDW.Projection.ANY);

        //temperature for each rain station
        for (int r = 0; r < n; r++) {
            //use IDW to calc rainTemp
            double rainTemp,rainWind;
            if (rsqTemp > this.regThres.getValue()) {
                rainTemp = idwTemp.getElevationCorrectedIDW(rainX[r], rainY[r], rainElev[r], gradTemp, temperature, tempNIDW.getValue());
            } else {
                rainTemp = idwTemp.getIDW(rainX[r], rainY[r], temperature, tempNIDW.getValue());
            }
            
            if (rsqWind > this.regThres.getValue()) {
                rainWind = idwWind.getElevationCorrectedIDW(rainX[r], rainY[r], rainElev[r], gradWind, wind, windNIDW.getValue());
            } else {
                rainWind = idwWind.getIDW(rainX[r], rainY[r], temperature, windNIDW.getValue());
            }
            
            //determine rain and snow amount of precip
            double pSnow = (snow_trs.getValue() + snow_trans.getValue() - rainTemp)
                    / (2 * snow_trans.getValue());

            //fixing upper and lower bound for pSnow (has to be between 0 and 1
            if (pSnow > 1.0) {
                pSnow = 1.0;
            } else if (pSnow < 0) {
                pSnow = 0;                //dividing input precip into rain and snow
            }
            double rain = (1 - pSnow) * precip[r];
            double snow = pSnow * precip[r];


            //Calculating relative Winderror acc to RICHTER 1995            
            if (snow > 0) {//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                if (snow <= 0.1) {
                    snow = snow + (snow * 0.938);
                } else {
                    double relSnow = 0.5319 * Math.pow(snow, -0.197);
                    snow = snow + (snow * relSnow);
                }
            }
            if (rain > 0) {
                if (rain < 0.1) {
                    rain += (rain * 0.492);
                } else {
                    rain += (rain * (0.1349 * Math.pow(rain, -0.494)));
                }
            }
            
            //Calculating relative Winderror acc to Baishen Ye 2004
            if (snow > 0) {//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                if (snow <= 0.1) {
                    snow = snow + (snow * 0.938);
                } else {
                    if (rainWind < 6.2) {
                        double CR_snow = Math.exp(-0.056 * rainWind);
                        snow = snow / CR_snow;
                    } else if (rainWind >= 6.2) {
                        double CR_snow = Math.exp(-0.056 * 6.2);
                        snow = snow / CR_snow;
                    }
                }
            }
    
            //Calculating error from evaporation and wetting acc. to Richter             
            double wetErr;
            if (precip[r] < 0.1) {
                wetErr = 0;
            } else {
                if (time.get(Attribute.Calendar.MONTH) >= 4 & time.get(Attribute.Calendar.MONTH) < 10) { //Summer half of the year
                    if (precip[r] >= 9.0) {
                        wetErr = 0.47;
                    } else {
                        wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                    }
                } else {   //Winter half of the year
                    if (precip[r] >= 9.0) {
                        wetErr = 0.3;
                    } else {
                        wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                    }
                }
            }
            //Calculating corrected rain_value
            if (precip[r] == JAMS.getMissingDataValue()) {
                rcorr[r] = JAMS.getMissingDataValue();
            } else {
                rcorr[r] = rain + snow + wetErr;
            }
        }        
        this.rcorr.setValue(rcorr);
    }
}
