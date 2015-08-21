package org.jams.j2k.s_n.wq;

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


import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcDailyETP_PenmanMonteith",
author = "Peter Krause",
description = "Calculates potential ETP according Penman-Monteith")
public class PenmanWQ extends JAMSComponent {

    public final double CP = 1.031E-3; //MJ/kg°C

    public final double RSS = 150;

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "temporal resolution [d | h | m]")
    public JAMSString tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable wind")
    public JAMSDouble wind;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable mean temperature")
    public JAMSDouble tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable relative humidity")
    public JAMSDouble rhum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "state variable net radiation")
    public JAMSDouble netRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute elevation")
    public JAMSDouble elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "attribute area")
    public JAMSDouble area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "potential ET [mm/ timeUnit]")
    public JAMSDouble potET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual ET [mm/ timeUnit]")
    public JAMSDouble actET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "et calibration parameter")
    public JAMSDouble et_cal;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
    defaultValue = "0")
    public JAMSInteger dataCaching;

    private File cacheFile;
    //todo .. handle transient
    transient private ObjectOutputStream writer;

    transient private ObjectInputStream reader;

    /*
     *  Component run stages
     */
    public void init() throws JAMSEntity.NoSuchAttributeException, IOException {
        //first, check if cached data are available
        //cacheFile = new File(dirName.getValue() + "/$" + this.getInstanceName() + ".cache");
        cacheFile = new File(getModel().getWorkspace().getTempDirectory(), this.getInstanceName() + ".cache");
        if (!cacheFile.exists() && (dataCaching.getValue() == 1)) {
            getModel().getRuntime().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
        }

        if (dataCaching.getValue() == 1) {
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));//new FileInputStream(cacheFile));
        } else if (dataCaching.getValue() == 0) {
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
    }

    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {

        if (dataCaching.getValue() == 1) {
            this.potET.setValue(reader.readDouble());
            this.actET.setValue(0.0);
        } else {
            double netRad = this.netRad.getValue();
            double temperature = this.tmean.getValue();
            double rhum = this.rhum.getValue();
            double wind = this.wind.getValue();
            double elevation = this.elevation.getValue();
            double area = this.area.getValue();

            double abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
            double delta_s = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_slopeOfSaturationPressureCurve(temperature);
            double pz = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_atmosphericPressure(elevation, abs_temp);
            double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            double ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rhum, est);

            double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(temperature);
            double psy = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_psyConst(pz, latH);

            double G = this.calc_groundHeatFlux(netRad);
            double vT = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_VirtualTemperature(abs_temp, pz, ea);
            double pa = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_AirDensityAtConstantPressure(vT, pz);

            double tempFactor = 0;
            double pET = 0;
            double aET = 0;


            if (this.tempRes.getValue().equals("d")) {
                tempFactor = 86400;
            } else if (this.tempRes.getValue().equals("h")) {
                tempFactor = 3600;
            } else if (this.tempRes.getValue().equals("m")) {
                tempFactor = 86400;
            }
            double Letp = 0;
            Letp = this.calcPM(delta_s, netRad, G, pa, CP, est, ea, psy, tempFactor, wind);

            pET = Letp / latH;
            aET = 0;

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

            this.potET.setValue(pET*et_cal.getValue());
            this.actET.setValue(aET);
            
            if (dataCaching.getValue() == 0) {
                writer.writeDouble(pET*et_cal.getValue());
            }
        }
    }

    public void cleanup() throws IOException {
        if (dataCaching.getValue() == 0) {
            writer.flush();
            writer.close();
        } else if (dataCaching.getValue() == 1) {
            reader.close();
        }
    }

    private double calcPM(double ds, double netRad, double G, double pa, double CP, double est, double ea, double psy, double tempFactor, double wind){
        double fu = (0.27 + 0.2333 * wind);
        //double Letp = (ds*(netRad-G) + ((pa*CP*fu*(est-ea))))/(delta_s+psy);
        //double Letp = (ds * (netRad - G) + (pa * CP * (est - ea) * fu * tempFactor)) / (ds + psy);
        double Letp = (ds * (netRad - G) + (pa * CP * (est - ea) * fu)) / (ds + psy);
        return Letp;
    }
    

    private double calc_groundHeatFlux(double netRad) {
        double g = 0.1 * netRad;
        return g;
    }

    
}
