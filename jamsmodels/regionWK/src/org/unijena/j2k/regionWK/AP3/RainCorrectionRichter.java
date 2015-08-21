/*
 * CalcRelativeHumidity.java
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

package org.unijena.j2k.regionWK.AP3;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="RainCorrection_Richter",
        author="Peter Krause",
        description="Applies correction according to RICHTER 1985 for measured daily precip sums"
        )
        public class RainCorrectionRichter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the precip values"
            )
            public Attribute.DoubleArray precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temperature for the correction function"
            )
            public Attribute.DoubleArray temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "corrected precip values"
            )
            public Attribute.DoubleArray rcorr;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station elevations"
            )
            public Attribute.DoubleArray tempElevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station's x coordinate"
            )
            public Attribute.DoubleArray tempXCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of temperature station's y coordinate"
            )
            public Attribute.DoubleArray tempYCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Regression coefficients for temperature"
            )
            public Attribute.DoubleArray tempRegCoeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of precip station elevations"
            )
            public Attribute.DoubleArray rainElevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of precip station's x coordinate"
            )
            public Attribute.DoubleArray rainXCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of precip station's y coordinate"
            )
            public Attribute.DoubleArray rainYCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "number of temperature station for IDW"
            )
            public Attribute.Integer tempNIDW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "power for IDW function"
            )
            public Attribute.Double pIDW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "regression threshold"
            )
            public Attribute.Double regThres;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "snow_trs"
            )
            public Attribute.Double snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "snow_trans"
            )
            public Attribute.Double snow_trans;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
            defaultValue = "0"
            )
            public Attribute.Integer dataCaching;*/
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        
        //if(dataCaching.getValue() != 1){
            double[] precip = this.precip.getValue();
            double[] temperature = this.temperature.getValue();
            double[] rcorr = new double[precip.length];
            
            double[] rainElev = new double[precip.length];
            double[] rainX = new double[precip.length];
            double[] rainY = new double[precip.length];
            
            //we need arrays for the temperature stations dist weight and id
            double[][] statWeights = new double[precip.length][temperature.length];
            //double[][] statDists   = new double[precip.length][temperature.length];
            
            //parameterization of rain stations
            for(int i = 0; i < precip.length; i++){
                rainElev[i] = this.rainElevation.getValue()[i];
                rainX[i] = this.rainXCoord.getValue()[i];
                rainY[i] = this.rainYCoord.getValue()[i];
                
                
                for(int n = 0; n < this.tempNIDW.getValue(); n++){
                    statWeights[i][n] = 0;
                    //statDists[i][n]   = 0;
                }
            }
            
            for(int i = 0; i < rcorr.length; i++){
                //Calculating weights for nidw stations
                statWeights[i] = org.unijena.j2k.statistics.IDW.calcNidwWeights(rainX[i], rainY[i], this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.pIDW.getValue(), this.tempNIDW.getValue());
            }
            double rsq = this.tempRegCoeff.getValue()[2];
            double grad = this.tempRegCoeff.getValue()[1];
            
            //temperature for each rain station
            double rainTemp;
            for (int r = 0; r < rcorr.length; r++) {
                rainTemp = 0;
                for(int t = 0; t < temperature.length; t++){
                    if(rsq >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rainElevation.getValue()[r] - this.tempElevation.getValue()[t];  //Elevation difference between unit and Station
                        rainTemp += ((deltaElev * grad + temperature[t]) * statWeights[r][t]);
                    } else{ //No elevation correction
                        rainTemp  += (temperature[t] * statWeights[r][t]);
                    }
                }
                //determine rain and snow amount of precip
                double pSnow = (snow_trs.getValue() + snow_trans.getValue() - rainTemp) /
                        (2 * snow_trans.getValue());
                
                //fixing upper and lower bound for pSnow (has to be between 0 and 1
                if(pSnow > 1.0)
                    pSnow = 1.0;
                else if(pSnow < 0)
                    pSnow = 0;
                
                //dividing input precip into rain and snow
                double rain = (1 - pSnow) * precip[r];
                double snow = pSnow * precip[r];
                
                
                
                
                //Calculating relative Winderror acc to RICHTER 1995
                double windErr = 0;
                if(snow > 0){//if(pSnow >= 1.0){      //set to all snow (5/11/01), rechanged 1.03.02
                    if(snow <= 0.1)
                        snow = snow + (snow * 0.938);
                    else{
                        double relSnow = 0.5319 * Math.pow(snow, -0.197);
                        snow = snow + (snow * relSnow);
                    }
                }
                if(rain > 0){ //if(pSnow < 1.0){//
                    if(rain < 0.1)
                        rain += (rain * 0.492);
                    else
                        rain += (rain * (0.1349 * Math.pow(rain, -0.494)));
                }
                
                //Calculating error from evaporation and wetting acc. to Richter
                double wetErr = 0;
                if(precip[r] < 0.1)
                    wetErr = 0;
                else{
                    if(time.get(time.MONTH) >= 4 & time.get(time.MONTH) < 10){ //Summer half of the year
                        if(precip[r] >= 9.0)
                            wetErr = 0.47;
                        else
                            wetErr = 0.08 * Math.log(precip[r]) + 0.225;
                    } else{   //Winter half of the year
                        if(precip[r] >= 9.0)
                            wetErr = 0.3;
                        else
                            wetErr = 0.05 * Math.log(precip[r]) + 0.13;
                    }
                }
                
                //Calculating corrected rain_value
                rcorr[r] = rain + snow + wetErr;
                
               
            }
            
                 this.rcorr.setValue(rcorr);
            
                  
       // }
    }
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
}
