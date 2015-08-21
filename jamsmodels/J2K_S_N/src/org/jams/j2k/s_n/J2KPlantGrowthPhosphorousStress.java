
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
        title="J2KPlantGrowthPhosphorousStress",
        author="Manfred Fink",
        description="Calculation of the plant groth Phosphorous factor after SWAT"
        )
        public class J2KPlantGrowthPhosphorousStress extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "optimal nitrogen content in Biomass in (kgN/ha)"
            )
            public Attribute.Double optibioP;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual nitrogen content in Biomass in (kgN/ha)"
            )
            public Attribute.Double actbioP;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "in [-] plant groth nitrogen stress factor"
            )
            public Attribute.Double pstrs;
    
   
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        double run_pstrs = 0;
        double run_optibioP = optibioP.getValue();
        double run_actbioP = actbioP.getValue();
        //double runactN_up = actN_up.getValue();
        double phi_nit = 0; // scaling factor for nitrogen stress [-]
        
        
        phi_nit = 200 * (((run_actbioP + 0.01) / (run_optibioP + 0.01)) - 0.5);
        
        run_pstrs = 1 - (phi_nit / (phi_nit + Math.exp(3.535 - (0.02597 * phi_nit))));
        //actbioN.setValue(run_actbioN);
        pstrs.setValue(run_pstrs);
       
    
    }
    
    public void cleanup() {
        
    }
}
