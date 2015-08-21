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
        title="J2KAreaWeigth",
        author="Manfred Fink",
        description="Calculates product of two variables",
        version="1.0_0",
        date="2012-03-19"
        )
        public class J2KMultiplication extends JAMSComponent {
    
    /*
     *  Component variables
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "multiplier 1",
            unit="-"
            )
            public Attribute.Double mult1;
    
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "multiplier 2",
            unit="-"
            )
            public Attribute.Double mult2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Product",
            unit="-"
            )
            public Attribute.Double Product;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
    
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
 
        Product.setValue(mult1.getValue() * mult2.getValue());
        }
        
        
    
    public void cleanup() {
        }
    
}
