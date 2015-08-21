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

package org.unijena.j2k;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="LinearTestModel",
        author="Peter Krause",
        description="A linear regression model, which can be used to test optimizer and efficiency calculation"
        )
        
public class LinearTestModel extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "known intercept"
            )
            public Attribute.Double paraAgoal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "known gradient"
            )
            public Attribute.Double paraBgoal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "known intercept"
            )
            public Attribute.Double paraCgoal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "known gradient"
            )
            public Attribute.Double paraDgoal; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unknown intercept"
            )
            public Attribute.Double paraA;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unknown gradient"
            )
            public Attribute.Double paraB; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unknown intercept"
            )
            public Attribute.Double paraC;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unknown gradient"
            )
            public Attribute.Double paraD; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "model output y"
            )
            public Attribute.DoubleArray yVal;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "real output y"
            )
            public Attribute.DoubleArray yValKnown;
    
    public void run() {
        double a = this.paraA.getValue();
        double b = this.paraB.getValue();
        double c = this.paraC.getValue();
        double d = this.paraD.getValue();
        
        double aFin = this.paraAgoal.getValue();
        double bFin = this.paraBgoal.getValue();
        double cFin = this.paraCgoal.getValue();
        double dFin = this.paraDgoal.getValue();
        
        double[] x = {1,2,3,4,5,6,7,8,9,10};
        double[] y = new double[x.length];
        double[] yFin = new double[x.length];
        
        for(int i = 0; i < x.length; i++){
            y[i] = a * Math.pow(x[i], 4) + b * Math.pow(x[i], 3) + c * x[i] + d;
            yFin[i] = aFin * Math.pow(x[i], 4) + bFin * Math.pow(x[i], 3) + cFin * x[i] + dFin;
            //y[i] = a + b * x[i];
            //yFin[i] = aFin + bFin * x[i];
        }
        
        this.yVal.setValue(y);
        this.yValKnown.setValue(yFin);
    }
}
