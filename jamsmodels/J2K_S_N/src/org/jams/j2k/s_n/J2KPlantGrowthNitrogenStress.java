
/*
 * J2KPlantGrowthNitrogenStress.java
 * Created on 16. Februar 2006, 09:18
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
        title="J2KPlantGrowthNitrogenStress",
        author="Manfred Fink",
        description="Calculation of the plant groth nitrogen factor after SWAT"
        )
        public class J2KPlantGrowthNitrogenStress extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "optimal nitrogen content in Biomass in (kgN/ha)"
            )
            public Attribute.Double optibioN;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual nitrogen content in Biomass in (kgN/ha)"
            )
            public Attribute.Double actbioN;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "in [-] plant groth nitrogen stress factor"
            )
            public Attribute.Double nstrs;
    
   
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        double run_nstrs = 0;
        double run_optibioN = optibioN.getValue();
        double run_actbioN = actbioN.getValue();
        //double runactN_up = actN_up.getValue();
        double phi_nit = 0; // scaling factor for nitrogen stress [-]
        
        
        if ((run_actbioN + 0.01) / (run_optibioN + 0.01) < 0.5){
            nstrs.setValue(1.0);
        }else{
        
        phi_nit = 200 * (((run_actbioN + 0.01) / (run_optibioN + 0.01)) - 0.5);
        
        run_nstrs = 1 - (phi_nit / (phi_nit + Math.exp(3.535 - (0.02597 * phi_nit))));
        //actbioN.setValue(run_actbioN);
        nstrs.setValue(run_nstrs);
        }
    
    }
    
    public void cleanup() {
        
    }
}
