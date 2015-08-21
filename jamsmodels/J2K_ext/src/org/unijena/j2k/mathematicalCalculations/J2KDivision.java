/*
 * J2KProcessInterception.java
 * Created on 24. November 2005, 10:52
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

package org.unijena.j2k.mathematicalCalculations;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KDivision",
        author="Manfred Fink",
        description="Calculates quotient of two variables",
        version="1.0_0",
        date="2014-05-30"
        )
        public class J2KDivision extends JAMSComponent {
    
    /*
     *  Component variables
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "dividend",
            unit="-"
            )
            public Attribute.Double div1;
    
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "divisor",
            unit="-"
            )
            public Attribute.Double div2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "quotient",
            unit="-"
            )
            public Attribute.Double quotient;
    
    /*
     *  Component run stages
     */

    public void init() {
        div1.setValue(0.0);
        div2.setValue(0.0);
    }
    
    public void run() {
        
        
            
        if (div2.getValue()==0){
      //   getModel().getRuntime().sendErrorMsg(getModel().getComponent("J2KDivision") + " Do not divide with zero, dividend: " + div1.getValue() + ", divisor: " + div2.getValue());
     //    getModel().getRuntime().sendHalt(getModel().getComponent("J2KDivision") + " Do not divide with zero, dividend: " + div1.getValue() + ", divisor: " + div2.getValue());
            quotient.setValue(0.0); // neccessary option for calculation of substance concentrations
      //      quotient.setValue(null); 
        }else{
            quotient.setValue(div1.getValue() / div2.getValue());
        }
    }

}
