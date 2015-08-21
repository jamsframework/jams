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
import jams.tools.JAMSTools;
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
        public class CalcHourlyNetRadiation extends JAMSComponent {
    
    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
            defaultValue = "0")
            public Attribute.Integer dataCaching;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable temperature"
            )
            public Attribute.Double temp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity"
            )
            public Attribute.Double rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation"
            )
            public Attribute.Double extRad;
    
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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute latitude"
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "calc refET with fixed values"
            )
            public Attribute.Boolean refET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily net radiation [MJ/m²]"
            )
            public Attribute.Double netRad;
    
    private File cacheFile;
    private boolean useCache = false;
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
            //           getModel().getRuntime().sendHalt(this.getInstanceName() + ": dataCaching is true but no cache file available!");
        }
        if (dataCaching.getValue() == 1) {
            reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
        } else if (dataCaching.getValue() == 0) {
            writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
        }
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException{
        if (dataCaching.getValue() == 1) {
            netRad.setValue(reader.readDouble());
        }
        else {
            int julDay = time.get(Attribute.Calendar.DAY_OF_YEAR);
            int idx = (julDay - 1) * 24 + time.get(Attribute.Calendar.HOUR_OF_DAY);
            double extraterrRadiation = extRad.getValue();
            double elev = elevation.getValue();
            double alb;
            if(refET.getValue())
                alb = 0.23;
            else
                alb = albedo.getValue();
            
            double temperature = temp.getValue();
            double rh = rhum.getValue();
            double sRad = solRad.getValue();
            
            double sat_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            double act_vapour_pressure = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(rh, sat_vapour_pressure);
            
            //double extraTerrestialRad = sRad /(0.25 + 0.5 * sunh);
            double clearSkyRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_ClearSkySolarRadiation(elev, extraterrRadiation);
            double netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sRad);
            double netLWRadiation = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyNetLongwaveRadiation(temperature, act_vapour_pressure, sRad, clearSkyRad, 0.3, false);
            
            double nRad = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetRadiation(netSWRadiation, netLWRadiation);
            
            netRad.setValue(nRad);
        } 
        
    }
    
    public void cleanup() throws IOException {
        if (dataCaching.getValue() == 0) {
            writer.flush();
            writer.close();
        } else if(dataCaching.getValue() == 1) {
            reader.close();
        }
    }
}
