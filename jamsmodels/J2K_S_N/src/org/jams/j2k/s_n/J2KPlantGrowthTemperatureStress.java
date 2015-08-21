
/*
 * J2KPlantGrowthTemperatureStress.java
 * Created on 15. Februar 2006, 15:35
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
        title="J2KPlantGrothTemperatureStress",
        author="Manfred Fink",
        description="Calculation of the plant groth temperature factor after SWAT"
        )
        public class J2KPlantGrowthTemperatureStress extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C dayly mean temperature"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C plant groth base temperature"
            )
            public Attribute.Double tbase;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C plant groth optimal temperature"
            )
            public Attribute.Double topti;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "in [-] plant groth temperature stress factor"
            )
            public Attribute.Double tstrs;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        double run_tstrs = 0;
        double run_tmean = tmean.getValue();
        double run_topti = topti.getValue();
        double run_tbase = tbase.getValue();
        
        if (run_tmean <= run_tbase){
            
            run_tstrs = 1;
        
            
        }else if (run_tmean > run_tbase && run_tmean <= run_topti){
         
            run_tstrs = 1 - (Math.exp(((-0.1054 * Math.pow((run_topti - run_tmean) , 2))) / Math.pow((run_tmean - run_tbase) , 2)));
            
   //         run_tstrs = 0;
            
        }else if (run_tmean > run_topti && run_tmean <= ((2 * run_topti) - run_tbase)){
            
            
           run_tstrs = 1 - (Math.exp(((-0.1054 * Math.pow((run_topti - run_tmean) , 2))) / Math.pow(((2 * run_topti) - run_tmean - run_tbase) , 2)));
        
     //      run_tstrs = 0;
           
        }else if (run_tmean > ((2 * run_topti) - run_tbase)){
            
           run_tstrs = 1; 
        
        }
      
        tstrs.setValue(run_tstrs);
        
    }
    
    public void cleanup() {
        
    }
}
