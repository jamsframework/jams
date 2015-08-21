/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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

import java.util.Vector;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="GeoFemGroundwater",
        author="Peter Krause",
        description="Description"
        )
        public class GeoFemGroundwater extends JAMSComponent {
    
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
            description = "recision coefficient k"
            )
            public Attribute.Double k;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "groundwater recharge"
            )
            public Attribute.Double gwRecharge;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "groundwater storages"
            )
            public Attribute.Double storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "baseflow basQ"
            )
            public Attribute.Double basQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "baseflow recession coefficient"
            )
            public Attribute.Double gwRecCoeff;
    
   
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
       double storage = this.storage.getValue();
       double input = this.gwRecharge.getValue();
       
       double recTanSlope = 1 - (Math.tan(this.slope.getValue() * (Math.PI / 180.)));
        if(recTanSlope < 0)
            recTanSlope = 0;
       
       storage = storage + input;
       
       double gwRec = 1. / (this.k.getValue() * recTanSlope);
       //double outflow = (1. / this.k.getValue()) * storage;
       double outflow = gwRec * storage;
       storage = storage - outflow;
       
       this.storage.setValue(storage);
       this.basQ.setValue(outflow);
       this.gwRecCoeff.setValue(this.k.getValue() * recTanSlope);
       
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
