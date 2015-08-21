/*
 * InitJ2KNSoil.java
 * Created on 13. February 2006, 09:03
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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
        title="InitJ2KGroundwaterN",
        author="Manfred Fink",
        description="intitiallizing groundwater N module with two different N-Pools"
        )
        public class InitJ2KGroundwaterN extends JAMSComponent  {
    
    /*
     *  Component variables
     */

    
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage"
            )
            public Attribute.Double maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG2 storage"
            )
            public Attribute.Double maxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG1 N storage in kgN"
            )
            public Attribute.Double NActRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG2 N storage in kgN"
            )
            public Attribute.Double NActRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU Concentration in mgN/l for RG1"
            )
            public Attribute.Double N_concRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU Concentration in mgN/l for RG2"
            )
            public Attribute.Double N_concRG2;
   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relativ size of the groundwaterN damping tank RG1 0 - 10 to calibrate in -"
            )
            public Attribute.Double N_delay_RG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relativ size of the groundwaterN damping tank RG2 0 - 10 to calibrate in -"
            )
            public Attribute.Double N_delay_RG2;
    
    
    
    // constants and calibration parameter

    
    
    /*
     *  Component run stages
     */
    
    
    
 
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
            
              
            double iNActRG1 = (maxRG1.getValue() * N_concRG1.getValue() / 1000000) * N_delay_RG1.getValue();
            double iNActRG2 = (maxRG2.getValue() * N_concRG2.getValue() / 1000000) * N_delay_RG2.getValue();
            
            NActRG1.setValue(iNActRG1);
            NActRG2.setValue(iNActRG2);
    }
    
 
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
        
    }
}
