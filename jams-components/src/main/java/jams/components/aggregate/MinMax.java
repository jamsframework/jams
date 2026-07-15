/*
 * MinMax.java
 * Created on 10.02.2022, 20:52:39
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
package jams.components.aggregate;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "MinMax",
        author = "Sven Kralisch",
        description = "Simple calculator for minimum/maximum",
        date = "2022-02-10",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class MinMax extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The value to calculate min/max of"
    )
    public Attribute.Double value;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum value"
    )
    public Attribute.Double maximum;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "minimum value"
    )
    public Attribute.Double minimum;

    /*
     *  Component run stages
     */

    
    @Override
    public void initAll() {
        if (maximum != null) {
            maximum.setValue(Double.MIN_VALUE);
        }
        if (minimum != null) {
            minimum.setValue(Double.MAX_VALUE);
        }
    }

    @Override
    public void run() {
        if (maximum != null) {
            maximum.setValue(Math.max(maximum.getValue(), value.getValue()));
        }
        if (minimum != null) {
            minimum.setValue(Math.min(minimum.getValue(), value.getValue()));
        }
    }

}
