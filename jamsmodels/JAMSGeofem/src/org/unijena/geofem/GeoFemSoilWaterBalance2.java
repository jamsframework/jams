/*
 * GeoFemSoilWaterBalance.java
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

package org.unijena.geofem;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="GeoFemSoilWaterBalance",
        author="Peter Krause",
        description="Calculates a simplified soil water balance for each HRU"
        )
        public class GeoFemSoilWaterBalance2 extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum LPS"
            )
            public Attribute.Double maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var actual MPS"
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var actual LPS"
            )
            public Attribute.Double actLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "subsurface runoff storage"
            )
            public Attribute.Double ssroStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var relative saturation of MPS"
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var relative saturation of LPS"
            )
            public Attribute.Double satLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "surface runoff"
            )
            public Attribute.Double sro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sub-surface runoff"
            )
            public Attribute.Double ssro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "groundwater recharge"
            )
            public Attribute.Double gwRecharge;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff"
            )
            public Attribute.Double totQ;*/
    
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
            description = "ssro recession factor"
            )
            public Attribute.Double k_factor;

    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        //double k_factor = 1;
        //double maxExcStor = 100000.0;
    
        
        double actMPS = this.actMPS.getValue();
        double actLPS = 0;//this.actLPS.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        double maxMPS = this.maxMPS.getValue();
        double maxLPS = this.maxLPS.getValue();
        double ssroStor = this.ssroStorage.getValue();
        
        //inflow goes into the soil
        double deltaMPS = maxMPS - actMPS;
        
        //inflow can go into fc
        if(inflow <= deltaMPS){
            actMPS = actMPS + inflow;
            inflow = 0;
        }
        else{
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        //remaining inflow goes into ac
        
        double deltaLPS = maxLPS - actLPS;
        if(inflow <= deltaLPS){
            actLPS = actLPS + inflow;
            inflow = 0;
        }
        else{
            actLPS = maxLPS;
            inflow = inflow - deltaLPS;
        }
        
        //et out of the soil
        double actET = this.actET.getValue();
        double potET = this.potET.getValue();
        
        double deltaET = potET - actET;
        
        //excess water is used first
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
        //soilwater storage is used next
        
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
        
        double sro = inflow;
        inflow = 0;
        double gwRecharge = 0;
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if(slope_weight > 1)
            slope_weight = 1;
        
        this.actLPS.setValue(actLPS);
        
        //latVert distribution
        double latPart = slope_weight * actLPS;
        double vertPart = (1 - slope_weight) * actLPS;
        
        ssroStor = ssroStor + latPart;
        
        double ssro = ssroStor * (1.0 / k_factor.getValue());
        ssroStor = ssroStor - ssro;
        
        gwRecharge = vertPart;
        
        //writing values back
        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
        
        this.satMPS.setValue(actMPS / maxMPS);
        this.satLPS.setValue(actLPS/maxLPS);
        this.ssroStorage.setValue(ssroStor);
        this.gwRecharge.setValue(gwRecharge);
        this.sro.setValue(sro);
        this.ssro.setValue(ssro);
    }
    
    public void cleanup() {
        
    }
    
    
}
