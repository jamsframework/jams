/*
 * SnowLine.java
 * Created on 30.09.2021, 22:26:15
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.indices;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SnowLine",
        author = "Sven Kralisch",
        description = "Calculate whether or not an entity is part of the snow line",
        date = "2021-09-30",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SnowLine extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow water equivalent",
            defaultValue = "0",
            unit = "L"
    )
    public Attribute.Double swe;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity elevation",
            defaultValue = "0",
            unit = "m"
    )
    public Attribute.Double elevation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "snow water equivalent",
            defaultValue = "0",
            unit = "L"
    )
    public Attribute.Double oldSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "probability that this entity is part of the snow line",
            defaultValue = "0"
    )
    public Attribute.Double snowLine;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "probability that this entity is part of the snow line",
            defaultValue = "0"
    )
    public Attribute.Double snowLineElevation;    
    
    private int count = 0;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        
        double sl = 0;
        
        if ((oldSnow.getValue() == 0) && (swe.getValue() != 0)) {
            sl = 1;
        } else if ((oldSnow.getValue() != 0) && (swe.getValue() == 0)) {
            sl = 1;
        }
               
        if (sl == 1) {
            if (snowLineElevation.getValue() == 0) {
                count = 0;
            }
            snowLineElevation.setValue(((snowLineElevation.getValue() * count) + elevation.getValue()) / ++count);
        }

        snowLine.setValue(sl);
        
        oldSnow.setValue((swe.getValue()==0)?0:1);
        
    }
}
