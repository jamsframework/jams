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

package org.unijena.j2000g;

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
        public class CapillarRising extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "groundwater storage",
            unit = "L"
            )
            public Attribute.Double gwStorage;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maxMPS",
            unit = "L"
            )
            public Attribute.Double maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actMPS",
            unit = "L"
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "satMPS",
            unit = "-"
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "gwCapRise",
            defaultValue="1.0",
            unit = "-"
            )
            public Attribute.Double gwCapRise;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "gwCapRiseAdaptation",
            unit = "-"
            )
            public Attribute.Double gwCapRiseAdaptation;
            
    /*
     *  Component run stages
     */
        
    @Override
    public void run() {
        double actMPS = this.actMPS.getValue();
        double maxMPS = this.maxMPS.getValue();
        
        if (maxMPS == 0){
            getModel().getRuntime().println("Olala there is a HRU with zero Field Capacity .. ");
            return;
        }
        double satMPS = actMPS / maxMPS;
        double gwStorage = this.gwStorage.getValue();
                
        double deltaMPS = maxMPS - actMPS;
        double alpha = gwCapRise.getValue()*this.gwCapRiseAdaptation.getValue();
        double inMPS = 0;
        if (satMPS != 0){
            inMPS = deltaMPS * (1.0-satMPS) * alpha;
        }else{
            inMPS = deltaMPS * alpha;
        }
                                
        if (gwStorage < inMPS){
            inMPS = gwStorage;
        }
        
        actMPS += inMPS;
        gwStorage -= inMPS;
                          
        this.gwStorage.setValue(gwStorage);
        this.actMPS.setValue(actMPS);
        this.satMPS.setValue(actMPS / maxMPS);
    }       
}
