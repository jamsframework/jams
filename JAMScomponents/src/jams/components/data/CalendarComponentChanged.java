/*
 * CalendarComponentChanged.java
 * Created on 18.03.2020, 10:18:36
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
package jams.components.data;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "CalendarComponentChanged",
        author = "Sven Kralisch",
        description = "Checks whether there is a change in a given component of a "
        + "calendar object",
        date = "2020-03-18",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class CalendarComponentChanged extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input date/time"
    )
    public Attribute.Calendar dateTime;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of the date/time component to be checked\n"
            + "(year:1, month:2, week_of_year:3, day_of_year:6, day_of_month:5, day_of_week:7"
    )
    public Attribute.Integer componentID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "result indicating whether the date/time component has changed or not"
    )
    public Attribute.Boolean changed;

    private int old = Integer.MIN_VALUE;

    @Override
    public void run() {
        int current = dateTime.get(componentID.getValue());
        if (current != old) {
            old = current;
            changed.setValue(true);
        } else {
            changed.setValue(false);
        }
    }

}
