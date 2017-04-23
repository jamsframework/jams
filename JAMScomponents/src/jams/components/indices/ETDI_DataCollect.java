/*
 * SMDI_DataCollect.java
 * Created on 18.04.2017, 22:56:45
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Evapotranspiration Deficit Index (ETDI) Data Collector",
        author = "Sven Kralisch",
        description = "This component collects required data to calculate the Evapotranspiration Deficit Index (ETDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ETDI_DataCollect extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Potential evapotranspiration",
            defaultValue = "0"
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual evapotranspiration",
            defaultValue = "0"
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current date"
    )
    public Attribute.Calendar date;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "List of collected water stress values"
    )
    public Attribute.Object wsValues;

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {
        List<Double> list = new ArrayList();
        wsValues.setValue(list);
    }

    @Override
    public void run() {
        List<Double> list = (List) wsValues.getValue();
        list.add((potET.getValue() - actET.getValue()) / potET.getValue());
    }
}
