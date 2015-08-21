/*
 * SubbasinFlooding.java
 * Created on 11.07.2015, 00:45:46
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
package flooding;

import jams.data.*;
import jams.model.*;
import java.util.Collections;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SubbasinInnundation",
        author = "Sven Kralisch",
        description = "Distribute reach water to neighbouring HRUs",
        date = "2015-07-10",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SubbasinFlooding extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The HRUs that drain into the reach"
    )
    public Attribute.EntityCollection subbasinHRUs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's elevation attribute",
            defaultValue = "elevation"
    )
    public Attribute.String elevationAttributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's elevation attribute",
            defaultValue = "elevation"
    )
    public Attribute.String RD1AttributeName;   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the HRU's elevation attribute",
            defaultValue = "elevation"
    )
    public Attribute.String areaAttributeName;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Runoff components of the reach that should be used for flooding"
    )
    public Attribute.Double[] runoffComponents;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach width"
    )
    public Attribute.Double reachWidth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach length"
    )
    public Attribute.Double reachLength;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach height"
    )
    public Attribute.Double reachHeight;

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        // sort subbasin HRUs according to their height
        Collections.sort(subbasinHRUs.getEntities(), (Attribute.Entity e1, Attribute.Entity e2)
                -> (int) (e1.getDouble(elevationAttributeName.getValue())
                - e2.getDouble(elevationAttributeName.getValue())));
    }

    @Override
    public void run() {

        // calc overall sum and store proportions
        double sum = 0;
        double[] proportion = new double[runoffComponents.length];

        for (Attribute.Double d : runoffComponents) {
            sum += d.getValue();
        }

        int i = 0;
        for (Attribute.Double d : runoffComponents) {
            proportion[i++] = d.getValue() / sum;
        }

        // calc initial water height in reach
        double waterHeight = sum / 1000 / reachLength.getValue() / reachWidth.getValue() + reachHeight.getValue();
        
        // iterate over (elevation-sorted) HRUs and distribute water...
        for (Attribute.Entity e : subbasinHRUs.getEntities()) {
            double hruHeight = e.getDouble(elevationAttributeName.getValue());
            if (waterHeight > hruHeight) {
                // flood hru, taking areas into account
                double heightDiff = waterHeight - hruHeight;
                //...
            }
        }

    }

    @Override
    public void cleanup() {
    }
}
