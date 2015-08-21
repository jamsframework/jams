/*
 * J2KGroundwaterNswat.java
 * Created on 8. Dezember 2005, 15:51
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
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KGroundwaterNswat",
        author="Manfred Fink",
        description="Groundwater N module after SWAT (very Simple)"
        )
        public class J2KGroundwaterNswat extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.String aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 inflow"
            )
            public Attribute.String aNameInRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 inflow"
            )
            public Attribute.String aNameInRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow"
            )
            public Attribute.String aNameOutRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow"
            )
            public Attribute.String aNameOutRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 N outflow in kgN"
            )
            public Attribute.String aNameN_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 N outflow in kgN"
            )
            public Attribute.String aNameN_RG2_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 N outflow in kgN"
            )
            public Attribute.String aNameN_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 N outflow in kgN"
            )
            public Attribute.String aNameN_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU Concentration Factor in mgN/l for RG1"
            )
            public Attribute.String aNameN_concRG1;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU Concentration Factor in mgN/l for RG2"
            )
            public Attribute.String aNameN_concRG2;        
    
    double N_RG1_in = 0; 
    double N_RG2_in = 0;
    double N_RG1_out = 0; 
    double N_RG2_out = 0;
     
           
    
    /*
     *  Component run stages
     */
    
    public void init()  {
        EntityEnumerator eEnum = hrus.getEntityEnumerator();
        Attribute.Entity[] entities = hrus.getEntityArray();
        
        for (int i = 0; i < entities.length; i++) {
            entities[i].setDouble(aNameN_concRG1.getValue(), 2);
            entities[i].setDouble(aNameN_concRG2.getValue(), 2);
        }
    }
    
    public void run() {
     
       double RG1_out = entity.getDouble(aNameOutRG1.getValue());
       double RG2_out = entity.getDouble(aNameOutRG2.getValue());
       double N_concRG1 = entity.getDouble(aNameN_concRG1.getValue());
       double N_concRG2 = entity.getDouble(aNameN_concRG2.getValue()); 
       
       this.N_RG1_out = (RG1_out * N_concRG1) / 1000000; // from mg/l to kg/l
       this.N_RG2_out = (RG2_out * N_concRG1) / 1000000; // from mg/l to kg/l
       
//       System.out.println("N_RG1_out = " + N_RG1_out +" RG1_out =  "+ RG1_out);
//       System.out.println("N_RG2_out = " + N_RG2_out +" RG2_out =  "+ RG2_out);
       
       entity.setDouble((aNameN_RG1_in.getValue()), N_RG1_in);
       entity.setDouble((aNameN_RG2_in.getValue()), N_RG2_in);
       entity.setDouble((aNameN_RG1_out.getValue()), N_RG1_out);
       entity.setDouble((aNameN_RG2_out.getValue()), N_RG2_out);
       
       
    }
    
    public void cleanup() {
        
    }
}
