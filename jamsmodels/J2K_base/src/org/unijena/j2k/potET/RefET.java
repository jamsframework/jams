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
description = "Calculates FAO grass reference ET",
version="1.0_0",
date="2011-05-30")
public class RefET extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | h | m]")
    public Attribute.String tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable wind",
            unit="m/s"
            )
            public Attribute.Double wind;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit="°C"
            )
            public Attribute.Double tmean;
    
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

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "potential refET [mm/ timeUnit]")
    public Attribute.Double refET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "actual ET [mm/ timeUnit]")
    public Attribute.Double actET;

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        double netRad = this.netRad.getValue();
        double temperature = this.tmean.getValue();
        double rhum = this.rhum.getValue();
        double wind = this.wind.getValue();
        double elevation = this.elevation.getValue();
        double area = this.area.getValue();

        //refET standard parameters for short grass with effH 0.12 and LAI 2.88
        double rs = 70;
        double ra = 208. / wind;

        double abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
        double delta_s = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature);
        double pz = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation, abs_temp);
        double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
        double ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum, est);
        double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature);
        double psy = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz, latH);

        double G = this.calc_groundHeatFlux(netRad);

        double tempFactor = 0;
        double pET = 0;
        double aET = 0;

        if (this.tempRes.getValue().equals("d")) {
            tempFactor = 891;
        } else if (this.tempRes.getValue().equals("h")) {
            tempFactor = 37;
        } else if (this.tempRes.getValue().equals("m")) {
            tempFactor = 891;
        }
        pET = (0.408 * delta_s * (netRad - G) + psy * (tempFactor / (temperature + 273)) * wind * (est - ea)) / (delta_s + psy * (1 + 0.34 * wind));


        //converting mm to litres
        pET = pET * area;

        //aggregation to monthly values
        if (this.time != null) {
            if (this.tempRes.getValue().equals("m")) {
                int daysInMonth = this.time.getActualMaximum(Attribute.Calendar.DATE);
                pET = pET * daysInMonth;
            }
        }

        //avoiding negative potETPs
        if (pET < 0) {
            pET = 0;
        }
        this.refET.setValue(pET);
        this.actET.setValue(aET);
    }

    private double calc_groundHeatFlux(double netRad) {
        double g = 0.1 * netRad;
        return g;
    }
}
