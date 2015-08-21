/*
 * PenmanMonteith.java
 * Created on 24. November 2005, 13:57
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
package org.unijena.j2k.regionWK.AP2;

import java.io.*;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcDailyETP_PenmanMonteith",
author = "Peter Krause",
description = "Calculates potential ETP according Penman-Monteith")
public class Penman extends JAMSComponent {

       /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | h | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable wind")
    public Attribute.Double wind;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable mean temperature")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable relative humidity")
    public Attribute.Double rhum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable net radiation")
    public Attribute.Double netRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute elevation")
    public Attribute.Double elevation;
    
       @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute latitude [deg]")
    public Attribute.Double latitude;

     @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "potential ET [mm/ timeUnit]")
    public Attribute.Double potET;


   
      /*
     *  Component run stages
     */
    
    int[] monthMean = {15, 45, 74, 105, 135, 166, 196, 227, 258, 288, 319, 349};
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
        int julDay = time.get(time.DAY_OF_YEAR);
        int month = time.get(time.MONTH);
        
        double wind = this.wind.getValue();
        double temperature = this.tmean.getValue();
        double elevation = this.elevation.getValue();
        double rhum = this.rhum.getValue();
        double netRad = this.netRad.getValue();
        double area = this.area.getValue();
        
        
        double lati = latitude.getValue();
        
         double abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
        
        double pz = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation, abs_temp);
        double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature);
        
        double delta_s = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature);
        double psy = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz, latH);
        
        double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
        double ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum, est);
        
        double G = this.calc_groundHeatFlux(netRad);
        
       
        
        
        double declination = 0;
            if (this.tempRes == null) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
            } else if (this.tempRes.getValue().equals("d")) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
            } else if (this.tempRes.getValue().equals("m")) {
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(this.monthMean[month]);
            }
        
        double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lati);
        double sunsetHourAngle = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_SunsetHourAngle(latRad, declination);
        double maximumSunshine = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_maximumSunshineHours(sunsetHourAngle);
                
        double sr = maximumSunshine/12;   
        
            double tempFactor = 0;
                       
            if (this.tempRes.getValue().equals("d")) {
                tempFactor = 86400;
            } else if (this.tempRes.getValue().equals("h")) {
                tempFactor = 3600;
            } else if (this.tempRes.getValue().equals("m")) {
                tempFactor = 86400;
            }
            
            double pET = calcETPenm(delta_s, G, est, ea, psy, latH, netRad, wind, sr);

            //converting mm to litres
            //pET = pET * area;

            //aggregation to monthly values
            if (this.time != null) {
                if (this.tempRes.getValue().equals("m")) {
                    int daysInMonth = this.time.getActualMaximum(time.DATE);
                    pET = pET * daysInMonth;
                }
            }
            //avoiding negative potETPs
            if (pET < 0) {
                pET = 0;
            }

            this.potET.setValue(pET);
                    
            
        }
   

    public void cleanup() throws IOException {
        
    }

    private double calcETPenm(double ds, double G, double est, double ea, double psy, double L, double netRad, double wind, double sr){

        double pET = (ds/(ds+psy) * (netRad - G)/L + psy/(ds+psy)*(0.063 * (1+1.08*wind)*(est-ea)*sr));

        return pET;
    }

    private double calc_groundHeatFlux(double netRad) {
        double g = 0.1 * netRad;
        return g;
    }

}
