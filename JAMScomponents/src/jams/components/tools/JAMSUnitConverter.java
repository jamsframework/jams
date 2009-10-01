/*
 * JAMSUnitConverter.java
 * Created on 23. Februar 2006, 23:22
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.components.tools;

import jams.data.*;
import jams.model.*;
import org.jscience.physics.units.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="JScience Unit Converter",
        author="Sven Kralisch",
        date="1. December 2005",
        description="This component converts an input value having a certain unit into an output value " +
        "having another unit. The units are represented by SI compliant unit strings, e.g. m^3/s.")
public class JAMSUnitConverter extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Unit of input value"
            )
            public JAMSString inUnit;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Unit of output value"
            )
            public JAMSString outUnit;   
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Input value"
            )
            public JAMSDouble inValue;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Output value"
            )
            public JAMSDouble outValue;
     
     transient Unit in, out;
     transient Converter conv;
     
     public void init() {
         in = Unit.valueOf(this.inUnit.getValue());
         out = Unit.valueOf(this.outUnit.getValue());
         if (!in.isCompatible(out)) {
             getModel().getRuntime().sendHalt("Incompatible units: " + inUnit + " <-> " + outUnit);
         }
         conv = in.getConverterTo(out);
     }
     
     public void run() {
         outValue.setValue(conv.convert(inValue.getValue()));
     }
         
}
