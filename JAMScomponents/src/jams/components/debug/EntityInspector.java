/*
 * EntityInspector.java
 * Created on 09.07.2015, 11:23:00
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
package jams.components.debug;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "EntityInspector",
        author = "Sven Kralisch",
        description = "Entity inspector",
        date = "2015-07-09",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntityInspector extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
    )
    public Attribute.EntityCollection entities;                // for a list of attribute types, see jams.data.Attribute  

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {
        Attribute.Entity e = entities.getCurrent();
//        Attribute.EntityCollection subbasins = (Attribute.EntityCollection) e.getObject("upstreamCatchment");
//        getModel().getRuntime().println(String.valueOf(e.getDouble("length")), JAMS.VERBOSE);
    }

    @Override
    public void cleanup() {
    }
}
