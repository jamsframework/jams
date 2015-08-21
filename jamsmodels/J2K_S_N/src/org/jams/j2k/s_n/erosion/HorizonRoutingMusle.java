/*
 * J2KProcessRouting.java
 * Created on 28. November 2005, 09:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.jams.j2k.s_n.erosion;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessRouting",
        author="Peter Krause",
        description="Passes the output of the entities as input to the respective reach or unit"
        )
        public class HorizonRoutingMusle extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar sediment inflow")
    public Attribute.Double insed;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar sediment outflow")
    public Attribute.Double outsed;

    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        //receiving polygon
        Attribute.Entity toPoly = (Attribute.Entity) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity toReach = (Attribute.Entity) entity.getObject("to_reach");
        
        double sedout = outsed.getValue();
                
        
        if(!toPoly.isEmpty()){
            
            
            sedout = 0;
           
            outsed.setValue(0);

            double sedin = toPoly.getDouble("insed");
           
            toPoly.setDouble("insed", sedin);
            
        } else if(!toReach.isEmpty()){
           
            
            double sedin = toReach.getDouble("insed");
            
            
            sedin = sedin + sedout;
            
            sedout = 0;
            
            outsed.setValue(sedout);
            toReach.setDouble("insed", sedin);
           
            
                
            
        } else{
            getModel().getRuntime().println("Current entity ID: " + entity.getDouble("ID") + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
    
   
}
