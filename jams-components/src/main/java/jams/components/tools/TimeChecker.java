/*
 * TimeChecker.java
 * Created on 01.03.2019, 17:40:59
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
package jams.components.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
        title = "TimeChecker",
        author = "Sven Kralisch",
        description = "Checks if current model time matches given time",
        date = "2019-03-01",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class TimeChecker extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar currentTime;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time to look for"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Is current time the time to look for?"
    )
    public Attribute.Boolean result;

    
    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        if (currentTime.toString().equals(time.toString())) {
            result.setValue(true);
        } else {
            result.setValue(false);
        }
    }

    @Override
    public void cleanup() {
    }
}
