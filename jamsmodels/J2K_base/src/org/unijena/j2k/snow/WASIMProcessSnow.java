/*
 * WASIMProcessSnow.java
 * Created on 17. August 2006, 17:33
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="WASIMProcessSnow",
        author="Joerg Schulla, implementation and minor adaptations Peter Krause",
        description="Adaptation of the WASIM module for snow calculation"
        )
        public class WASIMProcessSnow extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
//Entity attributes
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity area",
            unit = "m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity slope aspect correction factor"
            )
            public Attribute.Double sloAspCorrFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity latitude"
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Entity landuse albedo"
            )
            public Attribute.Double landAlbedo;
    
    //state variables
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Snow water equivalent",
            unit = "mm"
            )
            public Attribute.Double snowWaterEquivalent;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "liquid water in snowpack",
            unit = "mm"
            )
            public Attribute.Double freeWater;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "frozen water in snowpack",
            unit = "mm"
            )
            public Attribute.Double iceContent;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "radiation melt factor",
            unit = "mm / C*day"
            )
            public Attribute.Double radiationMeltFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the snow melt"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the albedo of the entity"
            )
            public Attribute.Double snowAlbedo;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual evapotranspiration"
            )
            public Attribute.Double aET;
    
    //calibration parameters
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "factor for calculating max. radiation melt factor"
            )
            public Attribute.Double rmf_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "min. radiation melt factor"
            )
            public Attribute.Double rmf_min;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "melt temperature: temperature at" +
            "which snow melt will start",
            unit = "C"
            )
            public Attribute.Double meltTemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "refreezing coefficient"
            )
            public Attribute.Double refCoeff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flag for selection of melt method" +
            "1 = temperature index approach" +
            "2 = tempWindAproach"
            )
            public Attribute.Integer meltMethod;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "factor c0 for potential melt rate"
            )
            public Attribute.Double c0Factor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "factor c1 for potential melt rate"
            )
            public Attribute.Double c1Factor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "factor c2 for potential melt rate"
            )
            public Attribute.Double c2Factor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Free-water holding capacity (decimal percent) of " +
            "snowpack expressed as decimal fraction of total " +
            "snowpack water equivalent"
            )
            public Attribute.Double freeWaterCapacity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "constant albedo of snow"
            )
            public Attribute.Double snowConstAlbedo;
    
    //driving data
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "rain amount"
            )
            public Attribute.Double rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow amount"
            )
            public Attribute.Double snow;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "precip amount"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "wind speed",
            unit = "m/s"
            )
            public Attribute.Double wind;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "minum temperature",
            unit = "C"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mean temperature",
            unit = "C"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "max temperature",
            unit = "C"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential evapotranspiration"
            )
            public Attribute.Double pET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "module active"
            )
            public Attribute.Boolean active;
    
    
    //run variables
    double runRain, runSnow, runIceContent, runFreeWater, runSnowWaterEquivalent, runMeltTemperature;
    double runRadiationMeltFactor, runWind, runPrecip, runSnowMelt, runSnowAlbedo, runAET;
    
    /*
     *  Component run stages
     */
    public void init() {
        
    }
    
    
    public void run() {
        if(this.active == null || this.active.getValue()){
            // map run variables
            // driving vars
            this.runRain = this.rain.getValue();
            this.runSnow = this.snow.getValue();
            this.runWind = this.wind.getValue();
            this.runPrecip = this.precip.getValue();
            
            // state vars
            this.runIceContent = this.iceContent.getValue();
            this.runFreeWater = this.freeWater.getValue();
            this.runSnowWaterEquivalent = this.snowWaterEquivalent.getValue();
            this.runSnowMelt = 0;
            this.runSnowAlbedo = this.snowAlbedo.getValue();
            this.runAET = this.aET.getValue();
            
            // calibration pars
            this.runMeltTemperature = this.meltTemp.getValue();
            
            int runMeltMethod = this.meltMethod.getValue();
            
            // day and night temperatures
            double tempDay = (this.tmax.getValue() + this.tmean.getValue()) / 2;
            double tempNight = (this.tmin.getValue() + this.tmean.getValue()) / 2;
            
            if (this.runSnow > 0 || this.runSnowWaterEquivalent > 0) {
                
                //very super simple snowET
                double deltaET = this.pET.getValue() - this.runAET;
                if(this.runIceContent <= deltaET){
                    this.runAET = this.runAET + this.runIceContent;
                    this.runIceContent = 0;
                }else{
                    this.runIceContent = this.runIceContent - deltaET;
                    this.runAET = this.pET.getValue();
                }
                
                int julDay = this.time.get(Attribute.Calendar.DAY_OF_YEAR);
                double dayFrac = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calcDayFraction(this.latitude.getValue(), julDay);
                this.runRadiationMeltFactor = this.calcRadiationMeltFactor2(julDay);
                
                //day part
                double potMeltRate = this.calcAccumulationAndMelt(tempDay, dayFrac, runMeltMethod);
                //night part
                potMeltRate = potMeltRate + this.calcAccumulationAndMelt(tempNight, (1-dayFrac), runMeltMethod);
                
                potMeltRate = potMeltRate * this.area.getValue();
                
                //snow and rain has been taken care of now!!
                this.runRain = 0;
                this.runSnow = 0;
                
                // balancing potMeltRate against iceContent
                if (potMeltRate > this.runIceContent) {
                    potMeltRate = this.runIceContent;
                }
                
                // transforming ice to liquid
                this.runIceContent = this.runIceContent - potMeltRate;
                this.runFreeWater = this.runFreeWater + potMeltRate;
                
                // computing maximum amount of free water
                double maxFreeWater = this.runIceContent
                        * this.freeWaterCapacity.getValue();
                
                // balancing free water against max free water
                if (maxFreeWater < this.runFreeWater) {
                    // snowmelt occurs
                    this.runSnowMelt = this.runFreeWater - maxFreeWater;
                    this.runFreeWater = maxFreeWater;
                }
                
                // if snow water equivalent < 5 mm the albedo is set to landcover
                // albedo
                // otherwise to a constant(!) snow albedo
                if ((this.runIceContent + this.runFreeWater) > (5.0 * this.area.getValue())) {
                    this.runSnowAlbedo = this.snowConstAlbedo.getValue();
                } else {
                    this.runSnowAlbedo = this.landAlbedo.getValue();
                }
            }
            //write vars back
            // state vars
            this.iceContent.setValue(this.runIceContent);
            this.freeWater.setValue(this.runFreeWater);
            this.snowWaterEquivalent.setValue(this.runIceContent + this.runFreeWater);
            this.snowMelt.setValue(this.runSnowMelt);
            this.snowAlbedo.setValue(this.runSnowAlbedo);
            this.aET.setValue(this.runAET);
            this.rain.setValue(this.runRain);
            this.snow.setValue(this.runSnow);
        }
    }
    
    
    public void cleanup() {
        
    }
    
    private double calcRadiationMeltFactor1(int julDay){
        double rmf = 0;
        double rmfMax = this.rmf_min.getValue() * this.rmf_max.getValue();
        //calculates the actual radiation melt factor for the specific julian day
        
        //Amplitude (absolute value only for random sampling)
        double r_b = (rmfMax - this.rmf_min.getValue()) / 2.0;
        //double r_b = ((this.rmf_max.getValue()) / 2.0);
        //Additative constant for shift of sin curve in x-direction
        double r_a = this.rmf_min.getValue() + r_b;
        //Adaptation of sin-curve period
        double r_c = 2 * Math.PI / 365;
        //Difference between Jan. 1. and Mar. 21 if sin(c*(d+day)) equals zero
        double r_d = -80.0;
        
        rmf = r_a + r_b * Math.sin(r_c * (r_d + julDay));
        
        //rmf = r_a + this.rmf_max.getValue() * Math.sin(r_c * (r_d + julDay));
        
        return rmf;
    }
    
    private double calcRadiationMeltFactor2(int julDay){
        double rmf = 0;
        double rmfMax = this.rmf_min.getValue() * this.rmf_max.getValue();
        //calculates the actual radiation melt factor for the specific julian day
        
        //Amplitude (absolute value only for random sampling)
        double r_b = (rmfMax - this.rmf_min.getValue()) / 2.0;
        //double r_b = ((this.rmf_max.getValue()) / 2.0);
        //Additative constant for shift of sin curve in x-direction
        double r_a = this.rmf_min.getValue() + r_b;
        //Adaptation of sin-curve period
        double r_c = 2 * Math.PI / 365;
        //Difference between Jan. 1. and Mar. 21 if sin(c*(d+day)) equals zero
        double r_d = -80.0;
        
        rmf = r_a + r_b * Math.sin(r_c * (r_d + julDay));
        
        //rmf = r_a + this.rmf_max.getValue() * Math.sin(r_c * (r_d + julDay));
        
        return rmf;
    }
    
    /**
     * calculates saturation vapour pressure over ice at the given temperature in hPa
     * @param temperature the air temperature in °C
     * @return the saturation vapour pressure at temperature T [hPa]
     */
    private double calcIceSaturationVapourPressure(double temperature){
        double est = 0;
        est = 6.1078 * Math.exp((21.874 * temperature)/(265.5 + temperature));
        return est;
    }
    
    private double calcAccumulationAndMelt(double temperature, double dayFraction, int runMeltMethod){
        this.runIceContent = this.runIceContent + this.runSnow * dayFraction;
        this.runFreeWater = this.runFreeWater + this.runRain * dayFraction;
        //this.runSnow = this.runSnow - (this.runSnow * dayFraction);
        //this.runRain = this.runRain - (this.runRain * dayFraction);
        
        //refreezing of free water
        double potMeltRate = 0.;
        if(temperature <= this.runMeltTemperature){
            //combined temperature and wind approach
            if(runMeltMethod == 3){
                double negMelt = Math.abs(this.refCoeff.getValue() * this.runRadiationMeltFactor * (temperature - this.runMeltTemperature));
                negMelt = negMelt * dayFraction;
                negMelt = negMelt * this.area.getValue();
                
                //part of the free water is refreezing
                if(negMelt <= this.runFreeWater){
                    this.runFreeWater = this.runFreeWater - negMelt;
                    this.runIceContent = this.runIceContent + negMelt;
                }
                //all free water refreezes
                else{
                    this.runIceContent = this.runIceContent + this.runFreeWater;
                    this.runFreeWater = 0;
                }
            }
        }
        //compute snow melt
        else{
            if(this.runIceContent > 0){
                //temperature index approach
                if(runMeltMethod == 1){
                    potMeltRate = this.c0Factor.getValue() * (temperature - this.runMeltTemperature);
                } else if(runMeltMethod == 2){
                    potMeltRate = (this.c1Factor.getValue() + this.c2Factor.getValue() * this.runWind) * (temperature - this.runMeltTemperature);
                } else if(runMeltMethod == 3){
                    //convective precip event produces latent heat
                    if(this.runPrecip >= 2.0){
                        double ms = 1.2 * temperature;
                        //sensible heat flux
                        double mh = (this.c1Factor.getValue() + this.c2Factor.getValue() * this.runWind) * (temperature);
                        //latent heat flux (0.662 = psychrometer constant)
                        double ml = (this.c1Factor.getValue() + this.c2Factor.getValue() * this.runWind) * (this.calcIceSaturationVapourPressure(temperature) - 6.11) / 0.662;
                        //melt by energy from precip
                        double mp = 0.0125 * this.runPrecip * temperature;
                        //summing it up
                        potMeltRate = ms + mh + ml + mp;
                    } else{
                        double factor = 0.5 * (1.0 + this.sloAspCorrFactor.getValue());
                        double rmfCorr = this.runRadiationMeltFactor * factor;
                        potMeltRate = rmfCorr * (temperature - this.runMeltTemperature);
                    }
                }
            }
        }
        return potMeltRate * dayFraction;
    }
}
