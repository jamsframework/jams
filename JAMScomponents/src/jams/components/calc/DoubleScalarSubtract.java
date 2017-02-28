/*
 * DoubleDivide.java
 * Created on 18.05.2016, 15:51:19
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
import static ucar.unidata.util.Format.d;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DoubleScalarSubtract",
        author = "Annika Kuenne",
        description = "subtracts double value operands and returns the result",
        date = "2017-01-24",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleScalarSubtract extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Operand1"
    )
    public Attribute.Double[] d1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Operand2"
    )
    public Attribute.Double[] d2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Subtract of operands"
    )
    public Attribute.Double[] result;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        if (d1.length != d2.length || d1.length != result.length) {
            getModel().getRuntime().sendHalt("Manno! Check number of attributes for d1, d2, result!");
        }
        double s = 0;
        for (int i = 0; i < d1.length; i++) {
            s = d1[i].getValue() - d2[i].getValue();
            result[i].setValue(s);
        }
    }
}
