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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "DoubleScalarSum",
        author = "Sven Kralisch",
        description = "Summarizes double value operands and returns the result",
        date = "2017-01-24",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleScalarSum extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Operands"
    )
    public Attribute.Double[] d;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sum of operands"
    )
    public Attribute.Double result;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        double s = 0; 
        for (int i = 0; i < d.length; i++) {
            s = s + d[i].getValue();
        }
        result.setValue(s);
    }
}
