/*
 * DoubleArrayMultiply.java
 * Created on 14.01.2020, 15:51:19
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
import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DoubleArrayMultiply",
        author = "Sven Kralisch",
        description = "Multiply double array with one or many double values and return the result",
        date = "2020-01-14",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleArrayMultiply extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First operand"
    )
    public Attribute.DoubleArray da;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Second operand"
    )
    public Attribute.Double[] d;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Result of d1*d2 (element-wise)"
    )
    public Attribute.DoubleArray result;

    transient Runnable job;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        
        if (da.getValue().length == d.length) {

                    double[] d1 = da.getValue();
                    double[] r = new double[d1.length];
                    for (int i = 0; i < d1.length; i++) {
                        r[i] = d1[i] * d[i].getValue();
                    }
                    result.setValue(r);

        } else if (d.length == 1) {

                    double[] d1 = da.getValue();
                    double[] r = new double[d1.length];
                    for (int i = 0; i < d1.length; i++) {
                        r[i] = d1[i] * d[0].getValue();
                    }
                    result.setValue(r);

        } else {

            getModel().getRuntime().sendHalt("Attribute d2 has wrong length, should be 1 or length of d1");

        }
    }

}
