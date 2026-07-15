/*
 * TSColumnToEntityValue.java
 * Created on 19.06.2014, 23:44:07
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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "TSColumnToEntityValue",
        author = "Sven Kralisch",
        description = "Take a line from from an input datastore as input and "
                + "extract one value matching a single entity. The proper "
                + "value is identified by matching the entity ID (or any other "
                + "unique attribute) with the datastore columns ID.",
        date = "2014-06-20",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2014-06-20", comment = "Initial version")
})
public class TSColumnToEntityValue extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of double values from a datastore. Order "
            + "according to datastore")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Array of column IDs")
    public Attribute.StringArray columnID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity's ID to match with the column ID")
    public Attribute.Double entityID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The position of the entity's value within the data "
                    + "array (written once at the beginning and read afterwards")
    public Attribute.Integer columnNumber;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The entity's value extracted from the data array")
    public Attribute.Double dataValue;

    private double[] _columnID;
    /*
     *  Component run stages
     */

    @Override
    public void init() {
        _columnID = new double[columnID.getValue().length];
        int i = 0;
        for (String s : columnID.getValue()) {
            _columnID[i++] = Double.parseDouble(s);
        }
    }

    /*
     *  Component run stages
     */
    @Override
    public void initAll() {

        columnNumber.setValue(-1);
        for (int i = 0; i < _columnID.length; i++) {
            if (entityID.getValue() == _columnID[i]) {
                columnNumber.setValue(i);
            }
        }
    }

    @Override
    public void run() {

        int i = columnNumber.getValue();
        if (i >= 0) {
            dataValue.setValue(dataArray.getValue()[i]);
        }

    }

}
