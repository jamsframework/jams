/*
 * VariableAdder.java
 * Created on 23. May 2009, 20:40
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */

@JAMSComponentDescription(
        title="VariableAdder",
        author="Peter Krause",
        description="Adds a number of variables and return the result."
        )
        public class VariableAdder extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input variables to add"
            )
            public Attribute.Double[] inVars;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the result of the summation"
            )
            public Attribute.Double outVar;
    
    public void run() {
        
        double varSum = 0;
        for(int i = 0; i < inVars.length; i++)
            varSum = varSum + inVars[i].getValue();
        outVar.setValue(varSum);
        
    }
    
}
