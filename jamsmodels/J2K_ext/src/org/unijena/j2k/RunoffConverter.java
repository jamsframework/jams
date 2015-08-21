/*
 * CalcAreaWeight.java
 * Created on 23. Februar 2006, 17:15
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
        title="Runoff Converter",
        author="Peter Krause",
        description="Converts m³/s to mm/day"
        )
        
public class RunoffConverter extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Catchment area attribute"
            )
            public Attribute.Double catchmentArea;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input"
            )
            public Attribute.Double inVar;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the output"
            )
            public Attribute.Double outVar;
    
    public void run() {
        double out = this.inVar.getValue() * 1000 * 86400 / catchmentArea.getValue();
        outVar.setValue(out);
    }
}
