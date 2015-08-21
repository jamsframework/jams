/*
 * J2KNRouting.java
 * Created on 08. December 2005, 09:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe & Manfred Fink
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
 * @author Peter Krause & Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KNRouting",
        author="Peter Krause & Manfred Fink",
        description="Passes the N output of the entities as N input to the respective reach or unit"
        )
        public class J2KNRouting extends JAMSComponent {
    
    /*
     *  Component variables
     */

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 N inflow in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow in kgN"
            )
            public Attribute.Double InterflowN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N inflow in kgN"
            )
            public Attribute.Double N_RG1_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N inflow in kgN"
            )
            public Attribute.Double N_RG2_in;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD1 N outflow in kgN"
            )
            public Attribute.Double SurfaceNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RD2 N outflow in kgN"
            )
            public Attribute.Double InterflowNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N outflow in kgN"
            )
            public Attribute.Double N_RG1_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N outflow in kgN"
            )
            public Attribute.Double N_RG2_out;
    
    /*
     *  Component run stages
     */
    
    public void init() {

    }
    
    public void run() {
        //receiving polygon
        Attribute.Entity toPoly = (Attribute.Entity) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity toReach = (Attribute.Entity) entity.getObject("to_reach");
        
        double NRD1out = SurfaceNabs.getValue();
        double NRD2out = InterflowNabs.getValue();
        double NRG1out = N_RG1_out.getValue();
        double NRG2out = N_RG2_out.getValue();
//        System.out.println("NRD2out: " + NRD2out);
        
        if(toPoly != null){
            double NRD1in = toPoly.getDouble("SurfaceN_in");
            double NRD2in = toPoly.getDouble("InterflowN_in");
            double NRG1in = toPoly.getDouble("N_RG1_in");
            double NRG2in = toPoly.getDouble("N_RG2_in");
//            double NRG1in = 0;
//            double NRG2in = 0;
            
            NRD1in = NRD1in + NRD1out;
            NRD2in = NRD2in + NRD2out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
                  
            NRD1out = 0;
            NRD2out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            
            SurfaceN_in.setValue(0);
            InterflowN_in.setValue(0);
            N_RG1_in.setValue(0);
            N_RG2_in.setValue(0);
                    
            toPoly.setDouble("SurfaceN_in",NRD1in);
            toPoly.setDouble("InterflowN_in", NRD2in);
            toPoly.setDouble("N_RG1_in", NRG1in);
            toPoly.setDouble("N_RG2_in", NRG2in);
        } else if(toReach != null){
                       
            double NRD1in = toReach.getDouble("SurfaceN_in");
            double NRD2in = toReach.getDouble("InterflowN_in");
            double NRG1in = toReach.getDouble("N_RG1_in");
            double NRG2in = toReach.getDouble("N_RG2_in");
           
            NRD1in = NRD1in + NRD1out;
            NRD2in = NRD2in + NRD2out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            NRD2out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            
            SurfaceNabs.setValue(NRD1out);
            toReach.setDouble("SurfaceN_in", NRD1in);
            InterflowNabs.setValue(NRD2out);
            toReach.setDouble("InterflowN_in", NRD2in);
            N_RG1_out.setValue(NRG1out);
            toReach.setDouble("N_RG1_in", NRG1in);
            N_RG2_out.setValue(NRG2out);
            toReach.setDouble("N_RG2_in", NRG2in);
            
        } else{
            System.out.println("Current entity ID: " + entity.getInt("ID") + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
}
