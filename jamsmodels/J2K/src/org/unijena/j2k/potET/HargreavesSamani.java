/*
 * HargreavesSamani.java
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
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcPotentialETSamani",
author = "Peter Krause",
description = "Calculates potential ET according to Hargreaves Samani")
public class HargreavesSamani extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "temporal resolution [d | m]")
    public Attribute.String tempRes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable minimum air temperature",
            unit = "degC")
    public Attribute.Double tmin;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable mean temperature",
            unit = "degC")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable maximum air temperature",
            unit = "degC")
    public Attribute.Double tmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable extraterrestrial radiation",
            unit = "MJ m^-2 day^-1")
    public Attribute.Double extRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area")
    public Attribute.Double area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "potential ET",
            unit = "mm day^-1")
    public Attribute.Double potET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "actual ET",
            unit = "mm day^-1")
    public Attribute.Double actET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
    defaultValue = "0")
    public Attribute.Integer dataCaching;

    private File cacheFile;

    transient private ObjectOutputStream writer;

    transient private ObjectInputStream reader;

    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
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

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        if (dataCaching.getValue() == 1) {
            this.potET.setValue(reader.readDouble());
        } else {
            double extRad = this.extRad.getValue();
            double tmin = this.tmin.getValue();
            double tavg = this.tmean.getValue();
            double tmax = this.tmax.getValue();
            double area = this.area.getValue();

            double latH = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_latentHeatOfVaporization(tavg);
            
            double pET = 0;
            double aET = 0;
            
            pET = (0.0023 * extRad * Math.sqrt(tmax - tmin) * (tavg + 17.8)) / latH;

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
            this.potET.setValue(pET);
            this.actET.setValue(aET);

            if (dataCaching.getValue() == 0) {
                writer.writeDouble(pET);
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

    
}
