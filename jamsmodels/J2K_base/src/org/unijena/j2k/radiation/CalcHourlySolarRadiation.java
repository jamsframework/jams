/*
 * CalcHourlySolarRadiation.java
 * Created on 13. Januar 2006, 11:57
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
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="HourlySolarRadiation",
        author="Peter Krause",
        description="Calculates hourly solar radiation from standard climatological measurements"
        )
        public class CalcHourlySolarRadiation extends JAMSComponent {
    
    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
            defaultValue = "0")
            public Attribute.Integer dataCaching;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "latitude of entity"
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable slope aspect correction factor"
            )
            public Attribute.Double actSlAsCf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable fraction of sunshine in one hour"
            )
            public Attribute.Double sunFrac;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Angstrom factor a"
            )
            public Attribute.Double angstrom_a;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Angstrom factor b"
            )
            public Attribute.Double angstrom_b;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "hourly solar radiation [MJ/m²]"
            )
            public Attribute.Double solRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "extraterrestic radiation [MJ/m²]"
            )
            public Attribute.Double actExtRad;
    
    
    
    private File cacheFile;
    //private boolean useCache = false;
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
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        if (dataCaching.getValue() == 1) {
            solRad.setValue(reader.readDouble());
        }
        else {
            int oldjulDay = 0;
            int julDay = time.get(Attribute.Calendar.DAY_OF_YEAR);
            int idx = (julDay - 1) * 24 + time.get(Attribute.Calendar.HOUR_OF_DAY);
            
            double lat = latitude.getValue();
            double radLat = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lat);
            //double longi = longitude.getValue();
            //double radLon =  org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(longi);
            //double elev = elevation.getValue();
            //double slo = slope.getValue();
            //double asp = aspect.getValue();
            double SAC = actSlAsCf.getValue();
            double declination = 0;
            double invRelDistEarthSun = 0;
            double solarConstant = 0;
            
            double sun_frac = sunFrac.getValue();
            
            if(julDay != oldjulDay){
                //sun's declination in rad
                declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
                //inverse relative dist. Earth Sun
                invRelDistEarthSun = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_InverseRelativeDistanceEarthSun(julDay);
                //the solar constant MJ /m² min
                solarConstant = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarConstant(julDay);
                oldjulDay = julDay;
            }
            double longTZ = 15;
            double maximumSunshine = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyMaxSunshine(this.actExtRad.getValue());
            double solRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SolarRadiation(sun_frac, maximumSunshine, this.actExtRad.getValue(), angstrom_a.getValue(), angstrom_b.getValue());
            
            solRadiation = solRadiation * SAC;
            
            solRad.setValue(solRadiation);
            
        } 
    }
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException, IOException {
        if (dataCaching.getValue() == 0) {
            writer.flush();
            writer.close();
        } else if(dataCaching.getValue() == 1) {
            reader.close();
        }
        
    }
}
