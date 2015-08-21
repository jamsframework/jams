/*
 * LinearTestModel.java
 * Created on 08. December 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2k.testFunctions;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="Hosaki Test Function",
        author="Peter Krause",
        description="A test function for optimizers which has a global and a local optimum"
        )
        
public class ThreeFactorLinearTestFunction extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter c"
            )
            public Attribute.Double paraC;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter d"
            )
            public Attribute.Double paraD;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter e"
            )
            public Attribute.Double paraE;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input value"            
            )
            public Attribute.Double input;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the simulated response"            
            )
            public Attribute.Double simulation;
    
    public void init(){
        
    }
    
    public void run() {
        double c = this.paraC.getValue();
        double d = this.paraD.getValue();
        double e = this.paraE.getValue();
        
        //double y = (0.8 * c + 0.1 * d + 0.1 * e) * this.input.getValue();
        
        double y = (c + c*d + c*d*e) * this.input.getValue();
        
        this.simulation.setValue(y);
    }
    
    
    
    
}
