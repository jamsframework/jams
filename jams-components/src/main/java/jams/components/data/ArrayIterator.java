/*
 * ArrayIterator.java
 * Created on 19.07.2023, 10:28:01
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
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "ArrayIterator",
        author = "Sven Kralisch",
        description = "Iterates over arrays of double values, providing list of double values.",
        date = "2023-07-19",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ArrayIterator extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "array of values to extract from (all dataArrays must have the same length)"
    )
    public Attribute.DoubleArray[] dataArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the index of the value to be extracted"
    )
    public Attribute.Integer arrayIndex;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "extracted value"
    )
    public Attribute.Double[] dataValue;
    

    @Override
    public void run() {
        for (int i = 0; i < dataArray.length; i++) {
            dataValue[i].setValue(dataArray[i].getValue()[arrayIndex.getValue()]);
        }
        arrayIndex.setValue(arrayIndex.getValue()+1);
    }

}
