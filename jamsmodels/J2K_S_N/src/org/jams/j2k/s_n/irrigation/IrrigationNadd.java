/*
 * IrrigationNadd.java
 * Created on 18.10.2011, 11:36:15
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.jams.j2k.s_n.irrigation;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
 @JAMSComponentDescription(
        title="IrrigationNadd",
        author="Manfred Fink",
        description="Adds the calculated Nitrogen of the irrigation water to the surface runoff"
        )
public class IrrigationNadd extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N-amount of the irrigation water in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double irrigationN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Nitrate in surface runoff added to HRU layer in N",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
            )
            public Attribute.Double SurfaceN_in;
                
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
    }

    @Override
    public void run() {
        SurfaceN_in.setValue(SurfaceN_in.getValue() + irrigationN.getValue());
    }

    @Override
    public void cleanup() {
    }
}