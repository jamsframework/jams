/*
 * DoubleAdd.java
 * Created on 11.07.2018, 15:51:19
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
        title = "DoubleAdd",
        author = "Sven Kralisch",
        description = "Add two double values and return the result",
        date = "2018-07-11",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleAdd extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First operand"
    )
    public Attribute.Double[] d1;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Second operand"
    )
    public Attribute.Double[] d2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Result of d1+d2 (element-wise)"
    )
    public Attribute.Double[] result;

    transient Runnable job;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        if (d1.length != result.length) {
            getModel().getRuntime().sendHalt("Attribute result has wrong length, should be length of d1");
        }

        if (d1.length == d2.length) {

            job = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < d1.length; i++) {
                        result[i].setValue(d1[i].getValue() + d2[i].getValue());
                    }
                }
            };

        } else if (d2.length == 1) {

            job = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < d1.length; i++) {
                        result[i].setValue(d1[i].getValue() + d2[0].getValue());
                    }
                }
            };

        } else {

            getModel().getRuntime().sendHalt("Attribute d2 has wrong length, should be 1 or length of d1");

        }
    }

    /*
     * This method makes sure that the "job" object is reinitialized after 
     * serialization/deserialization.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init();
    }

    @Override
    public void run() {
        job.run();
    }
}
