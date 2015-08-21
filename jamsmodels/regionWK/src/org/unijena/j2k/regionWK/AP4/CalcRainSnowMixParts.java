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

package org.unijena.j2k.regionWK.AP4;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcRainSnowParts",
        author="Peter Krause",
        description="Divides precip into rain and snow based on mean temperature"
        )
        public class CalcRainSnowMixParts extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRS"
            )
            public Attribute.Double snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRANS"
            )
            public Attribute.Double snow_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable min temperature or mean"
            )
            public Attribute.Double tmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable precipitation"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable rain"
            )
            public Attribute.Double rain_mix;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable snow"
            )
            public Attribute.Double snow_mix;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "mix rain, snow (yes/no)"
            )
            public Attribute.Integer mix;
      
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        double temperature = (this.tmin.getValue() + this.tmean.getValue()) / 2.0;
        //determinining relative snow amount of total precip depending on temperature
        double pSnow = 0;
        int mixrs = 0;
        
        if((temperature > snow_trs.getValue() + snow_trans.getValue()) || (temperature < snow_trs.getValue() - snow_trans.getValue())) {
            pSnow = (snow_trs.getValue() + snow_trans.getValue() - temperature) /
                (2 * snow_trans.getValue());
             mixrs = 1;
        }
        
        
        //System.out.println("davor" + mixrs);
        this.mix.setValue(mixrs);
        //System.out.println("nachher" + mixrs);
        
        //fixing upper and lower bound for pSnow (has to be between 0 and 1
        if(pSnow > 1.0)
            pSnow = 1.0;
        else if(pSnow < 0)
            pSnow = 0;
        
        //converting mm/m² to absolute litres
        double precip = this.precip.getValue() * this.area.getValue();
        if (precip < 0){
           precip = 0; 
        }
        //dividing input precip into rain and snow
        double rain = (1 - pSnow) * precip;
        double snow = pSnow * precip;
        
        this.snow_mix.setValue(snow);
        this.rain_mix.setValue(rain);
        
        
        
        
     }
    
   
    public void cleanup() {
        
    }
}
