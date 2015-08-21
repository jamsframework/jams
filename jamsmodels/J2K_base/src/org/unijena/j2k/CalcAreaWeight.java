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
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcAreaWeight",
        author="Peter Krause",
        description="Calculates a relative area weight as a fraction between "
        + "the spatial entitie's area and the sum of the area of all spatial"
        + "entities (i.e. the area of the catchment)",
        version="1.0_0",
        date="2011-05-30"
        )
        
public class CalcAreaWeight extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the area of the single entity",
            unit = "m^2"
            )
            public Attribute.Double entityArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the area of the catchment",
            unit = "m^2"
            )
            public Attribute.Double catchmentArea;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the relative area weight of the entity",
            unit = "n/a"
            )
            public Attribute.Double areaWeight;
    
    public void run() {
        areaWeight.setValue(catchmentArea.getValue() / entityArea.getValue());
    }
}
