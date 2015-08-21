/*
 * CalcRainSnowParts.java
 * Created on 23. November 2005, 17:33
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

package org.unijena.j2k.snow;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcRainSnowParts",
        author="Peter Krause",
        description="Distributes precipitation into rain and snow based on air temperature",
        version="1.0_0",
        date="2011-05-30"
        )
        public class CalcRainSnowParts extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area",
            unit = "m"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRS",
            lowerBound = -10.0,
            upperBound = 10.0,
            defaultValue = "0.0",
            unit = "°C"
            )
            public Attribute.Double snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRANS",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "2.0",
            unit = "K"
            )
            public Attribute.Double snow_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable min temperature or mean",
            unit = "°C"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit = "°C"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable precipitation",
            unit = "mm"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable rain",
            unit = "L"
            )
            public Attribute.Double rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable snow",
            unit = "L"
            )
            public Attribute.Double snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "save variable rain",
            defaultValue= "0",
            unit = "L"
            )
            public Attribute.Double svRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "save variable snow",
            defaultValue= "0",
            unit = "L"
            )
            public Attribute.Double svSnow;
    
    
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        double temperature = (this.tmin.getValue() + this.tmean.getValue()) / 2.0;
        //determinining relative snow amount of total precip depending on temperature
        double pSnow = (snow_trs.getValue() + snow_trans.getValue() - temperature) /
                (2 * snow_trans.getValue());
        
        //fixing upper and lower bound for pSnow (has to be between 0 and 1
        if(pSnow > 1.0)
            pSnow = 1.0;
        else
            pSnow = 0;
        
        //converting mm/m² to absolute litres
        double precip = this.precip.getValue() * this.area.getValue();
        if (precip < 0){
           precip = 0; 
        }
        //dividing input precip into rain and snow
        double rain = (1 - pSnow) * precip;
        double snow = pSnow * precip;
        
        this.snow.setValue(snow);
        this.rain.setValue(rain);
        
        this.svSnow.setValue(snow);
        this.svRain.setValue(rain);
    }
}
