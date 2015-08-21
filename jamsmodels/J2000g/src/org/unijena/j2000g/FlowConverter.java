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
        public class FlowConverter extends JAMSComponent {
    
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
            description = "input"
            )
            public Attribute.Double inQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output in cms"
            )
            public Attribute.Double outQcbm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output in mm"
            )
            public Attribute.Double outQmm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output in litres"
            )
            public Attribute.Double outQl;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "temporal resolution [h;d;m]"
            )
            public Attribute.String tempRes;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        //conversion from liters to m^3/time
        if(tempRes.getValue().equalsIgnoreCase("h")){
            this.outQcbm.setValue((inQ.getValue()) / (3600. * 1000));
            this.outQl.setValue((inQ.getValue()) / (3600.));
            this.outQmm.setValue(inQ.getValue() / cArea.getValue());
        }
        else if(tempRes.getValue().equalsIgnoreCase("d")){
            this.outQcbm.setValue((inQ.getValue()) / (86400. * 1000));
            this.outQl.setValue((inQ.getValue()) / (86400.));
            this.outQmm.setValue(inQ.getValue() / cArea.getValue());
        }else if(tempRes.getValue().equalsIgnoreCase("m")){
            this.outQcbm.setValue((inQ.getValue()) / (30.*86400 * 1000));
            this.outQl.setValue((inQ.getValue()) / (30.*86400));
            this.outQmm.setValue(inQ.getValue() / cArea.getValue());
        }
    }
    
    public void cleanup() {
        
    }
    
    
}
