
/*
 * J2KPlantGrowthStress.java
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
        title="J2KPlantGrowthStress",
        author="Manfred Fink",
        description="Calculation of the plant growth stress factor after SWAT"
        )
        public class J2KPlantGrowthStress extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in [-] plant growth nitrogen stress factor"
            )
            public Attribute.Double nstrs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in [-] plant growth phosphorous stress factor"
            )
            public Attribute.Double pstrs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in [-] plant growth temperature stress factor"
            )
            public Attribute.Double tstrs;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in [-] plant growth water stress factor"
            )
            public Attribute.Double wstrs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Biomass sum produced for a given day [kg/ha] drymass"
            )
            public Attribute.Double BioAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Plants daily biomass increase [kg/ha]"
            )
            public Attribute.Double BioOpt_delta;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        
        double stressfactor = 1 - Math.max(wstrs.getValue(),(Math.max(tstrs.getValue(),Math.max(nstrs.getValue(),pstrs.getValue()))));
       // stressfactor = 1 - Math.max(wstrs.getValue(),tstrs.getValue());
       // stressfactor = 1 - nstrs.getValue();
        if (stressfactor > 1){
            System.out.println("Stress "+  stressfactor);
            stressfactor = 1;
        }
        
        if (stressfactor < 0){
//            System.out.println("Stress "+  stressfactor);
            stressfactor = 0;
        }
        
        
        double bioact = (stressfactor * BioOpt_delta.getValue()) + BioAct.getValue();    
        
         
        
        BioAct.setValue(bioact);
        
        
       
       
    
    }
    
    public void cleanup() {
        
    }
}
