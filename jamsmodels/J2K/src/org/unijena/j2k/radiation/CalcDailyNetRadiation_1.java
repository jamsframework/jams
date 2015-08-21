/*
 * CalcDailyNetRadiation.java
 * Created on 24. November 2005, 13:32
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

package org.unijena.j2k.radiation;

import java.io.*;
//import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class CalcDailyNetRadiation_1 extends JAMSComponent {
    
    /*
     *  Component variables
     */

    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable sunshine hours"
            )
            public Attribute.Double sunh;
    */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity"
            )
            public Attribute.Double rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation"
            )
            public Attribute.Double extRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable albedo"
            )
            public Attribute.Double albedo;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation"
            )
            public Attribute.Double elevation;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute latitude"
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily net radiation [MJ/m²]"
            )
            public Attribute.Double netRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily shortwave radiation [MJ/m²]",
            defaultValue="0"            
            )
            public Attribute.Double swRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily longwave radiation [MJ/m²]",
            defaultValue="0"
            )
            public Attribute.Double lwRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily net radiation for refET [MJ/m²]",
            defaultValue="0"
            )
            public Attribute.Double refETNetRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Use caching of regionalised data?"
            )
            public Attribute.Boolean dataCaching; 
    
 
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
     
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException{
        
            double elev = elevation.getValue();
            double temp = tmean.getValue();
            double rh   = rhum.getValue();
            double sR   = solRad.getValue();
            double alb  = albedo.getValue();
            double extraTerrestialRad = extRad.getValue();
                    
            double sat_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temp);
            double act_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rh, sat_vapour_pressure);
            
            double clearSkyRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_ClearSkySolarRadiation(elev, extraTerrestialRad);
            double netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sR);
            double netRefETSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(0.23, sR);
            double netLWRadiation = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_DailyNetLongwaveRadiation(temp, act_vapour_pressure, sR, clearSkyRad, false);
            
            double nR_norm = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netSWRadiation, netLWRadiation);
            double nR_refET = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netRefETSWRadiation, netLWRadiation);
            netRad.setValue(nR_norm);
            refETNetRad.setValue(nR_refET);
            this.swRad.setValue(netSWRadiation);
            this.lwRad.setValue(netLWRadiation);
            
       
        
    }
    
    public void cleanup() throws IOException {

    }
}
