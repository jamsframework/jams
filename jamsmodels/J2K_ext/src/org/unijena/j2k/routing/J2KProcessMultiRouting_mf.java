/*
 * J2KProcessMultiRouting.java
 *
 * Created on 5. Juni 2008, 14:27
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

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="J2KProcessRouting_mf",
        author="Peter Krause",
        description="Passes the output of the entities as input to the respective reach or unit"
        )
        
        public class J2KProcessMultiRouting_mf extends JAMSComponent {
    
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
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reservoir objects"
            )
            public Attribute.EntityCollection reservoirs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD1 inflow"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD2 inflow"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar groundwater excess"
            )
            public Attribute.Double inGWExcess;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD1 outflow"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RD2 outflow"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar RG2 outflow"
            )
            public Attribute.Double outRG2;

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
        Attribute.Entity[] toPolyArray = (Attribute.Entity[]) entity.getObject("to_poly");
        //receiving reach
        Attribute.Entity[] toReachArray = (Attribute.Entity[]) entity.getObject("to_reach");
        //receiving reservoir
        Attribute.Entity toReservoir = null;
        
        Double[] polyWeightsArray = (Double[]) entity.getObject("to_poly_weights");
        Double[] reachWeightsArray = (Double[]) entity.getObject("to_reach_weights");
        
        Attribute.Entity toPoly;
        double polyWeight, reachWeight;
        
        try{
            toReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
        }
        catch(Attribute.Entity.NoSuchAttributeException e){
            toReservoir = null;
        }
        double RD1out = outRD1.getValue();
        double RD2out = outRD2.getValue();
        double RG1out = outRG1.getValue();
        double RG2out = outRG2.getValue();
        
        boolean keinziel = false;
        
        if(toPolyArray.length > 0){
            
            for (int a = 0; a < toPolyArray.length; a++){
                
                if (toPolyArray[a] != null){
                    
                    toPoly = toPolyArray[a];
                    polyWeight = polyWeightsArray[a];
                    
                    double RD1in = toPoly.getDouble("inRD1");
                    double RD2in = toPoly.getDouble("inRD2");
                    double RG1in = toPoly.getDouble("inRG1");
                    double RG2in = toPoly.getDouble("inRG2");
                    
                    RD1in = RD1in + RD1out * polyWeight;
                    RD2in = RD2in + RD2out * polyWeight;
                    RG1in = RG1in + RG1out * polyWeight;
                    RG2in = RG2in + RG2out * polyWeight;                    
                    RD2in += inGWExcess.getValue();
                    
                    toPoly.setDouble("inRD1", RD1in);
                    toPoly.setDouble("inRD2", RD2in);
                    toPoly.setDouble("inRG1", RG1in);
                    toPoly.setDouble("inRG2", RG2in);
                    
                    keinziel = true;
                }
            }
        }
        
        if (toReachArray.length > 0){
            
            for (int a = 0; a < toReachArray.length; a++){
                
                toReach = toReachArray[a];
                reachWeight = reachWeightsArray[a];
                
                double RD1in = toReach.getDouble("inRD1");
                double RD2in = toReach.getDouble("inRD2");
                double RG1in = toReach.getDouble("inRG1");
                double RG2in = toReach.getDouble("inRG2");
                
                RD1in = RD1in + RD1out * reachWeight;
                RD2in = RD2in + RD2out * reachWeight;
                RG1in = RG1in + RG1out * reachWeight;
                RG2in = RG2in + RG2out * reachWeight;
                RD2in += inGWExcess.getValue();
                
                toReach.setDouble("inRD1", RD1in);
                toReach.setDouble("inRD2", RD2in);
                toReach.setDouble("inRG1", RG1in);
                toReach.setDouble("inRG2", RG2in);
                
                keinziel = true;
            }
        }        
        
        RD1out = 0;
        RD2out = 0;
        RG1out = 0;
        RG2out = 0;
        
        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);
        inGWExcess.setValue(0);        
        
        if (keinziel == false){
            getModel().getRuntime().println("Current entity ID: " + (int)entity.getDouble("ID") + " has no receiver.");
        }
    }
    public void cleanup() {
        
    }
}