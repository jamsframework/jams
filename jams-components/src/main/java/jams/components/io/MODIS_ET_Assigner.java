/*
 * MODIS_ET_Assigner.java
 * Created on 14.04.2016, 11:39:06
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
package jams.components.io;

import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "MODIS_ET_Regionalizer",
        author = "Sven Kralisch",
        description = "Distribute monthly MODIS ET data to model entities",
        date = "2016-04-14",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2016-04-14", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", date = "2016-04-20", comment = "Fixed handling of missing data values")
})
public class MODIS_ET_Assigner extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "MODIS ET values of current time step"
    )
    public Attribute.DoubleArray modisETArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of HRU IDs from MODIS file"
    )
    public Attribute.IntegerArray hruIDArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID"
    )
    public Attribute.Double hruID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU area",
            unit = "m²"
    )
    public Attribute.Double hruArea;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "MODIS ET value",
            unit = "L"
    )
    public Attribute.Double modisET;

    Map<Integer, Integer> hru2idMap;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        if (hru2idMap == null) {
            hru2idMap = new HashMap();
            int[] hruID = hruIDArray.getValue();
            for (int i = 0; i < hruID.length; i++) {
                hru2idMap.put(hruID[i], i);
            }
        }

        double value = modisETArray.getValue()[hru2idMap.get((int) hruID.getValue())];
        int days = time.getActualMaximum(Attribute.Calendar.DAY_OF_MONTH);

        if (value < 0) {
            value = modisET.getValue();
        }

        modisET.setValue(value * hruArea.getValue() / days);

    }

}
