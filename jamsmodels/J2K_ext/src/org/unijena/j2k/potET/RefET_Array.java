/*
 * RefET.java
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

package org.unijena.j2k.potET;

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
description = "Calculates potential ETP after Penman-Monteith")
public class RefET_Array extends JAMSComponent {

    /*
     *  Component variables
     */
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | h | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable wind")
    public Attribute.DoubleArray wind;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable mean temperature")
    public Attribute.DoubleArray tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable relative humidity")
    public Attribute.DoubleArray rhum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable net radiation")
    public Attribute.DoubleArray netRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute elevation")
    public Attribute.DoubleArray elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "potential refET [mm/ timeUnit]")
    public Attribute.DoubleArray refET;


    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
       
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        
            double[] netRad = this.netRad.getValue();
            double[] temperature = this.tmean.getValue();
            double[] rhum = this.rhum.getValue();
            double[] wind = this.wind.getValue();
            double[] elevation = this.elevation.getValue();
            
            //refET standard parameters for short grass with effH 0.12 and LAI 2.88
            double rs = 70;
            double[] ra = new double[wind.length];
            double abs_temp[] = new double[wind.length];
            double delta_s[] = new double[wind.length];
            double pz[] = new double[wind.length];
            double est[] = new double[wind.length];
            double ea[] = new double[wind.length];
            double latH[] = new double[wind.length];
            double psy[] = new double[wind.length];
            double G[] = new double[wind.length];
            double pet[] = new double[wind.length];

            for(int i=0;i<wind.length;i++){
                ra[i] = 208. / wind[i];

                abs_temp[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature[i], "degC");
                delta_s[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature[i]);
                pz[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation[i], abs_temp[i]);
                est[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature[i]);
                ea[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum[i], est[i]);
                latH[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature[i]);
                psy[i] = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz[i], latH[i]);

                G[i] = this.calc_groundHeatFlux(netRad[i]);

                double tempFactor = 0;
                
                if (this.tempRes.getValue().equals("d")) {
                    tempFactor = 891;
                } else if (this.tempRes.getValue().equals("h")) {
                    tempFactor = 37;
                } else if (this.tempRes.getValue().equals("m")) {
                    tempFactor = 891;
                }
                pet[i] = (0.408 * delta_s[i] * (netRad[i] - G[i]) + psy[i] * (tempFactor / (temperature[i] + 273)) * wind[i] * (est[i] - ea[i])) / (delta_s[i] + psy[i] * (1 + 0.34 * wind[i]));




                //avoiding negative potETPs
                if (pet[i] < 0) {
                    pet[i] = 0;
                }
            }
            this.refET.setValue(pet);
            
    }

    public void cleanup() throws IOException {
        
    }

    private double calcETAllen(double ds, double netRad, double G, double pa, double CP, double est, double ea, double ra, double rs, double psy, double tempFactor) {
        ds = ds / 10;
        est = est / 10;
        ea = ea / 10;
        psy = psy / 10000;
        CP = CP / 1000;
        double Letp = (ds * (netRad - G) + ((pa * CP * (est - ea) / ra) * tempFactor)) / (ds + psy * (1 + rs / ra));

        return Letp;
    }

    private double calc_groundHeatFlux(double netRad) {
        double g = 0.1 * netRad;
        return g;
    }

    /*
    private double calc_raAllen(double veg_height, double windspeed){
    double w = Math.log((2 - 2. / 3. * veg_height)/(0.123 * veg_height));
    double r = Math.log((2 - 2. / 3. * veg_height)/(0.1 * 0.123 * veg_height));
    double v = Math.pow(0.41,2) * windspeed;
    
    double ra = (w*r) / v ;
    return ra;
    }
     */
    private static double calcRa(double eff_height, double wind_speed) {
        double ra;
        if (wind_speed <= 0) {
            wind_speed = 0.5;
        }
        if (eff_height < 10) {
            ra = (1.5 * Math.pow(Math.log(2 / (0.125 * eff_height)), 2)) / (Math.pow(0.41, 2) * wind_speed);
        } else {
            ra = 64 / (Math.pow(0.41, 2) * wind_speed);
        }
        return ra;
    }

    private double calcRs(double LAI, double rsc, double rss) {
        double A = Math.pow(0.7, LAI);
        double rs = 1. / (((1 - A) / rsc) + ((A / rss)));

        return rs;
    }
}
