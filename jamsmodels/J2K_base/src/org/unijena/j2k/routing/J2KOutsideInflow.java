/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class J2KOutsideInflow extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The receiver reach ID"
            )
            public Attribute.Integer reachID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the inflow from the data file or another model"
            )
            public Attribute.Double inflow;
        
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "additional inflow to the specific reach"
            )
            public Attribute.Double inAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "outflow of the additional inflow from the specific reach"
            )
            public Attribute.Double outAddIn;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the actual channel storage of the additional inflow of the specific reach"
            )
            public Attribute.Double actAddIn;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment additional input outlet storage"
            )
            public Attribute.Double catchmentAddIn;*/
    
      
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        Attribute.Entity entity = entities.getCurrent();
        double inflow = this.inflow.getValue();
        
        //only active for receiver reach
        if(entity.getDouble("ID") == reachID.getValue()){
            double actAddIn = this.actAddIn.getValue();
            
            this.actAddIn.setValue(actAddIn + inflow);
            
        }    
        
        
        
    }
    
    public void cleanup() {
        
    }
    
    /**
     * Calculates flow velocity in specific reach
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    /*public static double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        
        /**
         *transfering liter/d to m³/s
         **/
       /* double q_m = q / (1000 * secondsOfTimeStep);
        double rh = calcHydraulicRadius(afv, q_m, width);
        boolean cont = true;
        while(cont){
            veloc = (rough) * Math.pow(rh, (2.0/3.0)) * Math.sqrt(slope);
            if((Math.abs(veloc - afv)) > 0.001){
                afv = veloc;
                rh = calcHydraulicRadius(afv, q_m, width);
            } else{
                cont = false;
                afv = veloc;
            }
        }
        return afv;
    }
    
    /**
     * Calculates the hydraulic radius of a rectangular
     * stream bed depending on daily runoff and flow_velocity
     * @param v the flow velocity
     * @param q the daily runoff
     * @param width the width of reach
     * @return hydraulic radius in m
     */
   /* public static double calcHydraulicRadius(double v, double q, double width){
        double A = (q / v);
        
        double rh = A / (width + 2*(A / width));
        
        return rh;
    }*/
}
