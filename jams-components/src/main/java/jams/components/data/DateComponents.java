/*
 * DateComponents.java
 * Created on 23.10.2019, 02:00:44
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
        title = "DateComponents",
        author = "Sven Kralisch",
        description = "Extract components from a date/time value",
        date = "2019-10-23",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DateComponents extends JAMSComponent {

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
            description = "the date/time component ID\n"
                    + "(year:1, month:2, week_of_year:3, day_of_year:6, day_of_month:5, day_of_week:7"
    )
    public Attribute.Integer componentID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "offSet to componentValue",
            defaultValue = "0"
    )
    public Attribute.Integer offSet;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the date/time component value"
    )
    public Attribute.Integer componentValue;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        componentValue.setValue(dateTime.get(componentID.getValue()) + offSet.getValue());
    }
}
