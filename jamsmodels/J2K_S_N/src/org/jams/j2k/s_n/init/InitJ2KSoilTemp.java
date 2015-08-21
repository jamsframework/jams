/*
 * initJ2KSoilTemp.java
 * Created on 23. November 2005, 16:40
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

package org.jams.j2k.s_n.init;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="initJ2KSoilTemp",
        author="Manfred Fink",
        description="Asinghs soil temperature initial states in diffrent depths"
        )
        public class InitJ2KSoilTemp extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "mean temperature of the simulation period in °C"
            )
            public Attribute.Double tmeanavg;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "soil temperature in layerdepth in °C"
            )
            public Attribute.Double Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "in °C *  Output soil surface temperature"
            )
            public Attribute.Double Surfacetemp;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() {
        
    
        Soil_Temp_Layer.setValue(tmeanavg.getValue());
        Surfacetemp.setValue(tmeanavg.getValue());
      
            
    }
}


