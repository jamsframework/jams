/*
 * EntityDoubleAccess.java
 * Created on 13.06.2023, 23:03:52
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

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "EntityDoubleAccess",
        author = "Sven Kralisch",
        description = "Get double values by name from an entity and store it "
        + "in attributes.",
        date = "2023-06-13",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntityDoubleAccess extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity collection"
    )
    public Attribute.Entity entity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Names of the entitiy attributes to access"
    )
    public Attribute.String[] names;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The target attributes, number must match that of names"
    )
    public Attribute.Double[] attributes;


    /*
     *  Component run stages
     */

    @Override
    public void run() {
        
        if(!entity.isEmpty()){
            int i = 0;
            for (Attribute.String name : names) {
                attributes[i].setValue(entity.getDouble(name.getValue()));
                i++;
            }   
        }        
    }

}
