/*
 * SoilWaterBalance.java
 * Created on 25. October 2006, 13:21
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="SoilWaterBalance",
        author="Peter Krause",
        description="Calculates a simplified soil water balance for each HRU"
        )
        public class SoilWaterBalance extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum MPS"
            )
            public Attribute.Double maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var actual MPS"
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var relative saturation of MPS"
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "direct runoff"
            )
            public Attribute.Double dirQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "groundwater recharge"
            )
            public Attribute.Double gwRecharge;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff"
            )
            public Attribute.Double totQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "potential ET"
            )
            public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual ET"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow melt"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "lateral-vertical distribution coefficient"
            )
            public Attribute.Double latVertDist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ET reduction factor"
            )
            public Attribute.Double linETRed;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum percolation"
            )
            public Attribute.Double maxPerc;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        double actMPS = this.actMPS.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        double maxMPS = this.maxMPS.getValue();
        
        
        //et out of the soil
        double actET = this.actET.getValue();
        double potET = this.potET.getValue();
        
        double deltaET = potET - actET;
        
        //first ET is saturated from input
        if(inflow >= deltaET){
            actET = potET;
            inflow = inflow - deltaET;
            deltaET = 0;
        }
        else{
            actET = actET + inflow;
            inflow = 0;
            deltaET = potET - actET;
        }
        //then form soilwater storage
        
        //evapotranspiration
        //reduction function here
        double linRed = this.linETRed.getValue();
        double reduceET = 1.0;
        if(actMPS < (linRed * maxMPS)){
            reduceET = actMPS / (linRed * maxMPS);
        }
        deltaET = deltaET * reduceET;
        
        if(actMPS >= deltaET){
            actET = actET + deltaET;
            actMPS = actMPS - deltaET;
            deltaET = 0;
        }
        else{
            actET = actET + actMPS;
            actMPS = 0;
        }
        
        //inflow goes into the soil
        double deltaMPS = maxMPS - actMPS;
        
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        
        
        //available water is put into soil
        deltaMPS = maxMPS - actMPS;
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        double dQ = 0;
        double gR = 0;
        
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if(slope_weight > 1)
            slope_weight = 1;
        
        dQ = slope_weight * inflow;
        gR = (1-slope_weight) * inflow;
        inflow = 0;
        
       //cross checking against maximum percolation
       if(gR > maxPerc.getValue()){
            double delta = gR - maxPerc.getValue();
            dQ = dQ + delta;
            gR = maxPerc.getValue();
        }
        
        
        //writing values back
        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
        this.satMPS.setValue(actMPS / maxMPS);
        this.gwRecharge.setValue(gR);
        this.dirQ.setValue(dQ);
        this.totQ.setValue(dQ + gR);
    }
    
    public void cleanup() {
        
    }
    
    
}
