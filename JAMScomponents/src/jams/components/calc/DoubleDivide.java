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
        title = "DoubleDivide",
        author = "Sven Kralisch",
        description = "Divide a double value by another one and return the result",
        date = "2016-05-18",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DoubleDivide extends JAMSComponent {

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
    public Attribute.Double d2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Result of d1/d2"
    )
    public Attribute.Double[] result;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        for (int i = 0; i < d1.length; i++) {
            result[i].setValue(d1[i].getValue() / d2.getValue());
        }
    }
}
