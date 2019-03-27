/*
 * ArrayIterator.java
 * Created on 19.07.2016, 17:02:35
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
package jams.components.datatransfer;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "ArrayIterator",
        author = "Sven Kralisch",
        description = "This component takes a (list of) double array(s) as "
                + "input and returns the next array element at each invocation"
                + "of the run methods, this way iterating over the array(s).",
        date = "2019-03-26",
        version = "1.1_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Fixed buggy behaviour in case of repeated use (e.g. calibration)")
})
public class ArrayIterator extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Input double array")
    public Attribute.DoubleArray[] array;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Output double scalar")
    public Attribute.Double[] value;

    int arrayIndex;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        arrayIndex = 0;
    }

    @Override
    public void run() {

        for (int i = 0; i < array.length; i++) {

            value[i].setValue(array[i].getValue()[arrayIndex]);

        }
        arrayIndex++;

    }

    @Override
    public void cleanup() {
    }
}
