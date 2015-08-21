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
        public class FlowAggregator2 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "catchment area"
            )
            public Attribute.Double cArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "direct runoff"
            )
            public Attribute.Double dirQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "base flow"
            )
            public Attribute.Double basQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "direct runoff cbm"
            )
            public Attribute.Double dirQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "baseflow cbm"
            )
            public Attribute.Double basQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total outflow cbm"
            )
            public Attribute.Double totQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total outflow mm"
            )
            public Attribute.Double totQmm;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        double totOut = this.dirQ.getValue() + this.basQ.getValue();
        
        this.totQmm.setValue(totOut);
        //conversion from mm to m^3/time
        totOut = (totOut * cArea.getValue()) / (86400 * 1000);
        
        this.totQcbm.setValue(totOut);
        this.dirQcbm.setValue((dirQ.getValue() * cArea.getValue()) / (86400 * 1000));
        this.basQcbm.setValue((basQ.getValue() * cArea.getValue()) / (86400 * 1000));
    }
    
    public void cleanup() {
        
    }
    
    
}
