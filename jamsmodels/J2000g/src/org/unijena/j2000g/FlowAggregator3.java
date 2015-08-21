/*
 * FlowAggregator.java
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
        description="Converts cumulative runoff volumes into flows"
        )
        public class FlowAggregator3 extends JAMSComponent {
    
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
            description = "the input variables to convert"
            )
            public Attribute.Double[] inVars;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the converted output variables"
            )
            public Attribute.Double[] outVars;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the actual time step"
            )
            public Attribute.Calendar timeStep;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        long days = 1;
        if(this.timeStep != null)
            days = timeStep.getActualMaximum(Attribute.Calendar.DAY_OF_MONTH);
        //double totOut = 0;

        for(int i = 0; i < this.inVars.length; i++){
            this.outVars[i].setValue((this.inVars[i].getValue() * this.cArea.getValue()) / (86400 * 1000 * days));
        }

    }
    
    public void cleanup() {
        
    }
    
    
}
