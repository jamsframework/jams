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

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessRouting",
        author="Peter Krause",
        description="Passes the output of the entities as input to the respective reach or unit",
        version="1.0_0",
        date="2011-05-30"
        )
        public class J2KProcessRouting extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The spatial modelling entities"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reservoir objects"
            )
            public Attribute.EntityCollection reservoirs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow to modelling entity",
            unit = "L"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow to modelling entity",
            unit = "L"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow to modelling entity",
            unit = "L"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow to modelling entity",
            unit = "L"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "groundwater excess as input to modelling entity",
            unit = "L",
            defaultValue= "0"
            )
            public Attribute.Double inGWExcess;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 outflow from modellig entity",
            unit = "L"
            )
            public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Downstream hru entity"
            )
            public Attribute.Entity toPoly;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Downstream reach entity"
            )
            public Attribute.Entity toReach;
    
    /*
     *  Component run stages
     */
    
    public void init() {
    }

    public void run() {

        Attribute.Entity entity = entities.getCurrent();
        
        //receiving polygon
        //Attribute.Entity toPoly = (Attribute.Entity) entity.getObject("to_poly");
        
        //receiving reach
        //Attribute.Entity toReach = (Attribute.Entity) entity.getObject("to_reach");
                
        //receiving reservoir
        Attribute.Entity toReservoir = null;
        
        if (entity.existsAttribute("to_reservoir")) {
            toReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
        } else {
            toReservoir = null;
        }         

        double RD1out = outRD1.getValue();
        double RD2out = outRD2.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        
        
        if(!toPoly.isEmpty()){
            double elev = toPoly.getDouble("slope");
            double RD1in = toPoly.getDouble("inRD1");
            double RD2in = toPoly.getDouble("inRD2");
            double RG1in = toPoly.getDouble("inRG1");
            double RG2in = toPoly.getDouble("inRG2");
            
            RD1in = RD1in + RD1out;
            RD2in = RD2in + RD2out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
          
            RD2in += inGWExcess.getValue();
            
            RD1out = 0;
            RD2out = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            inGWExcess.setValue(0);
            
            
            
            toPoly.setDouble("inRD1", RD1in);
            toPoly.setDouble("inRD2", RD2in);
            toPoly.setDouble("inRG1", RG1in);
            toPoly.setDouble("inRG2", RG2in);
        } else if(!toReach.isEmpty()){
            double RD1in = toReach.getDouble("inRD1");
            double RD2in = toReach.getDouble("inRD2");
            double RG1in = toReach.getDouble("inRG1");
            double RG2in = toReach.getDouble("inRG2");
            
            RD1in = RD1in + RD1out;
            RD2in = RD2in + RD2out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;
            RD2in += inGWExcess.getValue();
            
            RD1out = 0;
            RD2out = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(RD1out);
            toReach.setDouble("inRD1", RD1in);
            outRD2.setValue(RD2out);
            toReach.setDouble("inRD2", RD2in);
            outRG1.setValue(RG1out);
            toReach.setDouble("inRG1", RG1in);
            outRG2.setValue(RG2out);
            inGWExcess.setValue(0);
            toReach.setDouble("inRG2", RG2in);
            
        }else if(toReservoir != null){
            double resRD1 = toReservoir.getDouble("compRD1");
            double resRD2 = toReservoir.getDouble("compRD2");
            double resRG1 = toReservoir.getDouble("compRG1");
            double resRG2 = toReservoir.getDouble("compRG2");
            
            resRD1 = resRD1 + RD1out;
            resRD2 = resRD2 + RD2out;
            resRG1 = resRG1 + RG1out;
            resRG2 = resRG2 + RG2out;
            
            RD1out = 0;
            RD2out = 0;
            RG1out = 0;
            RG2out = 0;
            
            outRD1.setValue(RD1out);
            toReservoir.setDouble("compRD1", resRD1);
            outRD2.setValue(RD2out);
            toReservoir.setDouble("compRD2", resRD2);
            outRG1.setValue(RG1out);
            toReservoir.setDouble("compRG1", resRG1);
            outRG2.setValue(RG2out);
            toReservoir.setDouble("compRG2", resRG2);
        } 
        else{
            getModel().getRuntime().println("Current entity ID: " + (int)entity.getDouble("ID") + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
}
