/*
 * J2KGroundwaterN_lumped.java
 * Created on 5. Januar 2006, 10:31
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, c8fima
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
        title="J2KGroundwaterN_lumped",
        author="Manfred Fink",
        description="Groundwater N module"
        )
        public class J2KGroundwaterN_lumped extends JAMSComponent {
    
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
            description = "actual RG1 storage"
            )
            public Attribute.String aNameActRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual RG2 storage"
            )
            public Attribute.String aNameActRG2;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual RG N storage in kgN"
            )
            public Attribute.String aNameNActRG;
    
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
            description = "RG1 N inflow in kgN"
            )
            public Attribute.String aNameN_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 N inflow in kgN"
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
            description = "N Percolation out of the soil profile in kgN"
            )
            public Attribute.String aNamePercoNabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU Concentration in mgN/l for RG1"
            )
            public Attribute.String aNameN_concRG;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage"
            )
            public Attribute.String aNameMaxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG2 storage"
            )
            public Attribute.String aNameMaxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N Grounwater buffer storage in kgN"
            )
            public Attribute.String aNameNGWbuffer;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relativ size of the groundwaterN damping tank 0 - 10 to calibrate in -"
            )
            public Attribute.Double N_delay;
    
    
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
            
            entities[i].setDouble(aNameN_RG1_in.getValue(), 0);
            entities[i].setDouble(aNameN_RG2_in.getValue(), 0);
            entities[i].setDouble(aNameN_concRG.getValue(), 3.8);
            double iN_concRG =  entities[i].getDouble(aNameN_concRG.getValue());
            double iActRG1 =  entities[i].getDouble(aNameActRG1.getValue());
            double iActRG2 =  entities[i].getDouble(aNameActRG2.getValue());
   //         double iNActRG = (iN_concRG * (iActRG1 + iActRG2)) / 1000000;
            
            double iNActRG = ((entities[i].getDouble(aNameMaxRG1.getValue()) + 
                    entities[i].getDouble(aNameMaxRG2.getValue())) * iN_concRG / 1000000) * N_delay.getValue(); 
            entities[i].setDouble(aNameNActRG.getValue(), iNActRG);

            //           entities[i].setDouble(aNameNGWbuffer.getValue(), NGWbuffer);
        }
    }
    
    public void run() {
        
        double RG1_out = entity.getDouble(aNameOutRG1.getValue());
        double RG2_out = entity.getDouble(aNameOutRG2.getValue());
        double ActRG1 = entity.getDouble(aNameActRG1.getValue());
        double MaxRG1 = entity.getDouble(aNameMaxRG1.getValue());
        double MaxRG2 = entity.getDouble(aNameMaxRG2.getValue());
        double ActRG2 = entity.getDouble(aNameActRG2.getValue());
        double N_RG1_in = entity.getDouble(aNameN_RG1_in.getValue());
        double N_RG2_in = entity.getDouble(aNameN_RG2_in.getValue());
        double NActRG = entity.getDouble(aNameNActRG.getValue());
        double N_concRG = entity.getDouble(aNameN_concRG.getValue());
        double percoN = entity.getDouble(aNamePercoNabs.getValue());
        double RGNretentinon = 1;
        
        NActRG  = N_RG1_in + N_RG2_in + percoN + NActRG;

        double watersum = ActRG1 + ActRG2 + RG1_out + RG2_out + ((MaxRG1 + MaxRG2) * N_delay.getValue());
        
 /*       
        if (watersum > 0){
        N_concRG = ((NActRG * RGNretentinon + (NActRG * (1 - RGNretentinon))) * 1000000) / watersum;
        
        } else {
        N_concRG = 0;  
        }
 */     
        if (watersum > 0){
        N_concRG = NActRG * 1000000 / watersum;
         } else {
        N_concRG = 0;  
        }     

        N_RG1_out = (RG1_out * N_concRG) / 1000000; // from mg/l to kg/l
        N_RG2_out = (RG2_out * N_concRG) / 1000000; // from mg/l to kg/l
        
        if (N_RG1_out + N_RG2_out > NActRG){
        
            double rg1part = N_RG1_out /(N_RG1_out + N_RG2_out);
            double rg2part = N_RG2_out /(N_RG1_out + N_RG2_out);
            N_RG1_out = rg1part * NActRG;
            N_RG2_out = rg2part * NActRG;
        }  
        NActRG = NActRG - (N_RG1_out + N_RG2_out);
        
//       System.out.println("N_RG1_out = " + N_RG1_out +" RG1_out =  "+ RG1_out);
//       System.out.println("N_RG2_out = " + N_RG2_out +" RG2_out =  "+ RG2_out);
        
        
        entity.setDouble((aNameN_RG1_in.getValue()), N_RG1_in);
        entity.setDouble((aNameN_RG2_in.getValue()), N_RG2_in);
        entity.setDouble((aNameN_RG1_out.getValue()), N_RG1_out);
        entity.setDouble((aNameN_RG2_out.getValue()), N_RG2_out);
        entity.setDouble((aNameNActRG.getValue()), NActRG);
        entity.setDouble((aNameN_concRG.getValue()), N_concRG);
    }
    
    public void cleanup() {
        
    }
}
