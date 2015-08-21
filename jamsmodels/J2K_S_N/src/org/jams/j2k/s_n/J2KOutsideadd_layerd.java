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

package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KOutsideadd_layerd",
        author="Manfred Fink",
        description="Adds values to an existing layerd pool"
        )
        public class J2KOutsideadd_layerd extends JAMSComponent {
    
    /*
     *  Component variables
     */
  

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the inflow from the data file or another model",
            unit = "kg"
            )
            public Attribute.Double inflow;
      
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the actual pool storage of the additional inflow of the specific HRU",
            unit = "kg*ha^1"
            )
            public Attribute.DoubleArray actAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the array index (layer number) where the Inflow is added, counting starts with 0",
            defaultValue = "0"
            )
            public Attribute.Integer Layer;
    
      
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        
        double inflow = this.inflow.getValue() * 10000/(area.getValue()) ;
        double[] ActAddIn = actAddIn.getValue();
        
        //only active for receiver reach
        int i = 0;
        
        while (i < ActAddIn.length){
            
            if (i == Layer.getValue()){
              ActAddIn[i] = ActAddIn[i] + inflow;
            }
            
            i++;   
        }
         
            
            
        this.actAddIn.setValue(ActAddIn);
            
            
        
        
        
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
