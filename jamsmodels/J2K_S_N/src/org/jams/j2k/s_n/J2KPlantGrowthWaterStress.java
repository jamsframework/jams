
/*
 * J2KPlantGrothTemperatureStress.java
 * Created on 16. Februar 2006, 09:05
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
package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="J2KPlantGrothWaterStress",
        author="Manfred Fink",
        description="Calculation of the plant groth water factor after SWAT"
        )
        public class J2KPlantGrowthWaterStress extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU actual Transpiration in mm"
            )
            public Attribute.Double aTP;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU potential Transpiration in mm"
            )
            public Attribute.Double pTP;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "in [-] plant groth water stress factor"
            )
            public Attribute.Double wstrs;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        double run_wstrs = 0;
        double run_aTP = aTP.getValue();
        double run_pTP = pTP.getValue();
        
        run_wstrs = 1 - ((run_aTP + 0.000001) / (run_pTP + 0.000001)); //orginal SWAT
        
        //run_wstrs = 1 - (Math.sqrt((run_aTP + 0.000001) / (run_pTP + 0.000001)) );
        
        //run_wstrs = 0;
        
        wstrs.setValue(run_wstrs);
    
    }
    
    public void cleanup() {
        
    }
}
