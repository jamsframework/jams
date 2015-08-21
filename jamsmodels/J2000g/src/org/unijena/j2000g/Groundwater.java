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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Groundwater",
        author="Peter Krause",
        description="Description"
        )
        public class Groundwater extends JAMSComponent {
    
    /*
     *  Component variables
     */
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
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
       double storage = this.storage.getValue();
       double input = this.gwRecharge.getValue();
       
       storage = storage + input;
       
       double outflow = (1. / this.k.getValue()) * storage;
       storage = storage - outflow;
       
       this.storage.setValue(storage);
       this.basQ.setValue(outflow);
       
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
