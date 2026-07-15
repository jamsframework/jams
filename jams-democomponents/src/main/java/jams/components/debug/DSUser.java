/*
 * DSUser.java
 * Created on 16. Oktober 2008, 22:07
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.debug;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription (title = "Title",
                           author = "Author",
                           description = "Description")
public class DSUser extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Description")
    public JAMSDoubleArray doubleValues;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Description")
    public JAMSDouble sum;
    /*
     *  Component run stages
     */

    double d;

    public void run() {
        d = 0;
        for (double value : doubleValues.getValue()) {
            d += value;
        }
        sum.setValue(d);
    }
}
