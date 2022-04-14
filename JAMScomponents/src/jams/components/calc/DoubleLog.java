/*
 * DoubleLog.java
 * Created on 14.04.2022, 23:06:57
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

package jams.components.calc;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="DoubleLog",
    author="Sven Kralisch",
    description="Calc log of a double value",
    date = "2022-04-14",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleLog extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input"      
            )
            public Attribute.Double[] d;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "output"
            )
            public Attribute.Double[] log_d;
                
    /*
     *  Component run stages
     */
    
    @Override
    public void run() {
        for (int i = 0; i < d.length; i++) {
            log_d[i].setValue(Math.log(d[i].getValue()));
        }
    }

}