/*
 * ScalarToArray.java
 * Created on 06.11.2019, 17:23:31
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
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "ScalarToArray",
        author = "Sven Kralisch",
        description = "Collect double scalar(s) into double array(s)",
        date = "2019-11-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ScalarToArray extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input value(s)"
    )
    public Attribute.Double[] dataValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "array(s) of values"
    )
    public Attribute.DoubleArray[] dataArray;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        for (int i = 0; i < dataArray.length; i++) {

            if (dataArray[i].getValue() == null) {
                dataArray[i].setValue(new double[0]);
            }
        }

    }

    @Override
    public void run() {

        for (int i = 0; i < dataValue.length; i++) {

            if (dataArray[i].getValue() == null) {
                dataArray[i].setValue(new double[0]);
            }
            
            double[] a = dataArray[i].getValue();
            double[] b = new double[a.length + 1];
            System.arraycopy(a, 0, b, 0, a.length);
            b[b.length - 1] = dataValue[i].getValue();
            dataArray[i].setValue(b);
        }
    }

}
