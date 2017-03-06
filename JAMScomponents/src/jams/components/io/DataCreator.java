/*
 * DataCreator.java
 * Created on 02.03.2017, 17:54:53
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
package jams.components.io;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "EntityCreator",
        author = "Sven Kralisch",
        description = "Creates a number of empty (holding no attributes) Attribute.Entity "
        + "objects",
        date = "2017-03-02",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class DataCreator extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Entity[] entities;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Double[] doubles;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Integer[] integers;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Float[] floats;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Long[] longs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Calendar[] calendars;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.TimeInterval[] timeIntervals;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Boolean[] booleans;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Geometry[] geometries;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attributes to be created"
    )
    public Attribute.Object[] objects;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
    }

    @Override
    public void cleanup() {
    }
}
