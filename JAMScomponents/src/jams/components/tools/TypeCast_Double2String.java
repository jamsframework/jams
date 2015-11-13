/*
 * TypeCast_Double2String.java
 * Created on 08.07.2015, 18:19:13
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

package jams.components.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
    title="TypeCast",
    author="Sven Kralisch",
    description="Convert attribute types",
    date = "2015-07-08",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class TypeCast_Double2String extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input attribuite"       
            )
            public Attribute.Double doubleAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output attribuite"       
            )
            public Attribute.String stringAttribute;
    
    /*
     *  Component run stages
     */
    
    @Override
    public void initAll() {
        stringAttribute.setValue(doubleAttribute.toString());
    }

    @Override
    public void run() {
    }

    @Override
    public void cleanup() {
    }
}