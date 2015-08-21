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
        author="Christian Fischer",
        description="A test function for optimizers which has a global and a local optimum"
        )
        
public class HosakiTestFunction extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X1"
            )
            public Attribute.Double paraX1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "parameter X2"
            )
            public Attribute.Double paraX2; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "function y"            
            )
            public Attribute.Double yVal;

    public void run() {
        double x1 = this.paraX1.getValue();
        double x2 = this.paraX2.getValue();
        
        double y = ((1 - (8 * x1) + (7 * Math.pow(x1,2)) - ((7./3.) * Math.pow(x1,3)) + ((1./4.) * Math.pow(x1,4)))) * Math.pow(x2,2) * Math.exp(-1 * x2);
        
        this.yVal.setValue(y);
    }
}
