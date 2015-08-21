/*
 * CalcRefET.java
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

package org.unijena.j2k.refET;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="CalcDailyETP_PenmanMonteith",
        author="Peter Krause",
        description="Calculates potential ETP after Penman-Monteith",
        version="1.0_0",
        date="2011-05-30"
        )
    
    public class CalcRefET extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d or h]"
            )
            public Attribute.String tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable wind",
            unit="m/s"
            )
            public Attribute.Double wind;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable minimum temperature",
            unit="°C"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit="°C"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable maximum temperature",
            unit="°C"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity",
            unit="%"
            )
            public Attribute.Double rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable net radiation",
            unit="MJ m^-2 d^-1"
            )
            public Attribute.Double netRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state extraterrestric radiation",
            unit="MJ m^-2 d^-1"
            )
            public Attribute.Double extRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation",
            unit="MJ m^-2 d^-1"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation",
            unit="m"
            )
            public Attribute.Double elevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m²"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "reference potential ETP [mm]",
            unit="mm d^-1"
            )
            public Attribute.Double refET;
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
      
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
            final double CP = 0.001031; //Specific heat of air [MJ kg-1 ï¿½C-1]
            final double albedo = 0.23; //according to Allen et al.
            final double rc = 70; //according to Allen et al.
            final double clearSkyTrans = 0.75; //according to Donatelli
            double extRad      = this.extRad.getValue();
            double solRad      = this.solRad.getValue();
            double tmin        = this.tmin.getValue();
            double tmean       = this.tmean.getValue();
            double tmax        = this.tmax.getValue();
            double rhum        = this.rhum.getValue();
            double wind        = this.wind.getValue();
            double elevation   = this.elevation.getValue();
            double area        = this.area.getValue();
            
            double latHeat = CalcVariables.calc_latentHeatOfVaporization(tmean);
            double airPressure = CalcVariables.calcAirPressure(elevation);
            double atmDens = CalcVariables.calcAtmosphericDensity(tmean, airPressure);
            double esT = CalcVariables.calcSaturationVapourPressure(tmean);
            double ea = CalcVariables.calcActualVapourPressure(esT, rhum);
            double vapPresDef = esT - ea;
            double slopSPC = CalcVariables.calc_slopeOfSaturationPressureCurve(tmean);
            double psyConst = CalcVariables.calc_psyConst(airPressure, latHeat);
            double ra = CalcVariables.calcAerodynamicResistance(wind);
            
            double nRad = this.netRad.getValue();//CalcVariables.calcNetRadiation(tmax,tmin,solRad,extRad,ea,albedo,clearSkyTrans,tempRes.getValue());
            double kt = 0; //unit conversion [s d|h-1]
            if(tempRes.getValue().equals("d"))
                kt = 86400;
            else if(tempRes.getValue().equals("h"))
                kt = 3600;
            
            
            double pETP = (1/latHeat) * (slopSPC * (nRad-0) + kt * (vapPresDef * atmDens * CP) / ra) / (slopSPC + psyConst * (1 + rc/ra));

            //converting mm to litres
            pETP = pETP * area;
            
            //avoiding negative potETPs
            if(pETP < 0){
                pETP = 0;
            }
            this.refET.setValue(pETP);
            this.netRad.setValue(nRad);
            
        
    }
    
    public void cleanup()  throws IOException {
        
    }
    
    private double calc_groundHeatFlux(double netRad){
        double g = 0.1 * netRad;
        return g;
    }
    
    private double calc_raAllen(double veg_height, double windspeed){
        double w = Math.log((2 - 2. / 3. * veg_height)/(0.123 * veg_height));
        double r = Math.log((2 - 2. / 3. * veg_height)/(0.1 * 0.123 * veg_height));
        double v = Math.pow(0.41,2) * windspeed;
        
        double ra = (w*r) / v ;
        return ra;
    }
    
    private static double calc_raSchulla(double eff_height, double wind_speed){
        double ra;
        if(eff_height < 10){
            ra = (4.72 * Math.pow(Math.log(2/(0.125 * eff_height)),2)) / (1 + 0.54 * wind_speed);
        } else{
            ra = 64 / (1 + 0.54 * wind_speed);
        }
        return ra;
    }
    
    private double calc_rs(double LAI, double rsc, double rss){
        double A = Math.pow(0.7, LAI);
        double rs = 1. / (((1-A)/rsc)+((A / rss)));
        
        return rs;
    }
}
