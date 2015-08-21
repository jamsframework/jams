/*
 * RainCorrectionSevruk.java
 * Created on 15. May 2008
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
        title="RainCorrectionSevruk",
        author="Peter Krause",
        description="Applies correction according to RICHTER 1985 and SEVRUK 1989" +
        "for measured daily precip sums. RICHTER is used for wetting and ET losses" +
        "whereas SEVRUK is used for the wind correction. This routine is thought" +
        "to produce better results in alpine regions than the normal RICHTER correction"
        )
        public class RainCorrectionSevruk extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.READ,
            description = "wind speed for the correction function"
            )
            public Attribute.DoubleArray wind;
    
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
            description = "Array of wind station elevations"
            )
            public Attribute.DoubleArray windElevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of wind station's x coordinate"
            )
            public Attribute.DoubleArray windXCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of wind station's y coordinate"
            )
            public Attribute.DoubleArray windYCoord;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Regression coefficients for wind"
            )
            public Attribute.DoubleArray windRegCoeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "number of temperature station for IDW"
            )
            public Attribute.Integer tempNIDW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "number of wind station for IDW"
            )
            public Attribute.Integer windNIDW;
    
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
            description = "tbase"
            )
            public Attribute.Double tbase;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
       
        //System.out.println("wind: " + this.windNIDW.getValue() );
        
            double[] precip = this.precip.getValue();
            double[] temperature = this.temperature.getValue();
            double[] wind = this.wind.getValue();
            double[] rcorr = new double[precip.length];
            
            double[] rainElev = new double[precip.length];
            double[] rainX = new double[precip.length];
            double[] rainY = new double[precip.length];
            
            //we need arrays for the temperature stations dist weight and id
            double[][] tempStatWeights = new double[precip.length][temperature.length];
             //we need arrays for the wind stations dist weight and id
            double[][] windStatWeights = new double[precip.length][wind.length];
            
            //parameterization of rain stations
            for(int i = 0; i < precip.length; i++){
                rainElev[i] = this.rainElevation.getValue()[i];
                rainX[i] = this.rainXCoord.getValue()[i];
                rainY[i] = this.rainYCoord.getValue()[i];
                
                
                for(int n = 0; n < this.tempNIDW.getValue(); n++){
                    tempStatWeights[i][n] = 0;
                }
                
                //System.out.println("wind: " + this.windNIDW.getValue() );
                for(int n = 0; n < this.windNIDW.getValue(); n++){
                    windStatWeights[i][n] = 0;
                }
            }
            
            for(int i = 0; i < rcorr.length; i++){
                //Calculating weights for nidw stations
                tempStatWeights[i] = org.unijena.j2k.statistics.IDW.calcNidwWeights(rainX[i], rainY[i], this.tempXCoord.getValue(), this.tempYCoord.getValue(), this.pIDW.getValue(), this.tempNIDW.getValue());
                windStatWeights[i] = org.unijena.j2k.statistics.IDW.calcNidwWeights(rainX[i], rainY[i], this.windXCoord.getValue(), this.windYCoord.getValue(), this.pIDW.getValue(), this.windNIDW.getValue());
            }
            double rsq_t = this.tempRegCoeff.getValue()[2];
            double grad_t = this.tempRegCoeff.getValue()[1];
            
            double rsq_w = this.windRegCoeff.getValue()[2];
            double grad_w = this.windRegCoeff.getValue()[1];
            
            //temperature and wind for each rain station
            double rainTemp;
            double rainWind;
            for (int r = 0; r < rcorr.length; r++) {
                rainTemp = 0;
                rainWind = 0;
                for(int t = 0; t < temperature.length; t++){
                    if(rsq_t >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rainElevation.getValue()[r] - this.tempElevation.getValue()[t];  //Elevation difference between unit and Station
                        rainTemp += ((deltaElev * grad_t + temperature[t]) * tempStatWeights[r][t]);
                    } else{ //No elevation correction
                        rainTemp  += (temperature[t] * tempStatWeights[r][t]);
                    }
                }
                for(int t = 0; t < wind.length; t++){
                    if(rsq_w >= this.regThres.getValue()) {  //Elevation correction is applied
                        double deltaElev = this.rainElevation.getValue()[r] - this.windElevation.getValue()[t];  //Elevation difference between unit and Station
                        rainWind += ((deltaElev * grad_w + wind[t]) * windStatWeights[r][t]);
                    } else{ //No elevation correction
                        rainWind  += (wind[t] * windStatWeights[r][t]);
                    }
                    if(rainWind < 0)
                        rainWind = 0;
                }
                
                //Calculating relative Winderror acc to SEVRUK 1989
                double windErr = 0;
                if(rainTemp < -27.0){
                    windErr = 1 + 0.550 * Math.pow(rainWind, 1.4);
                }
                else if((rainTemp >= -27.0)&&(rainTemp < -8.0)){
                    windErr = 1 + 0.280 * Math.pow(rainWind, 1.3);
                }
                else if((rainTemp >= -8.0)&&(rainTemp <= this.tbase.getValue())){
                    windErr = 1 + 0.150 * Math.pow(rainWind, 1.18);
                }
                else if(rainTemp >= this.tbase.getValue()){
                    windErr = 1 + 0.015 * rainWind;
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
                rcorr[r] = precip[r] + precip[r] * windErr + wetErr;
            }
            
            this.rcorr.setValue(rcorr);
        
    }
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
}
