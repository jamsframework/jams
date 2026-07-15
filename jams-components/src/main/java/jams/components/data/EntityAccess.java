/*
 * EntityAccess.java
 * Created on 18.08.2022, 22:06:04
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
        title = "EntityAccess",
        author = "Sven Kralisch",
        description = "Find entities by ID in an entity collection. IDs are "
        + "assumed to be unique, i.e. the first match is returned.",
        date = "2022-08-18",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntityAccess extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity collection"
    )
    public Attribute.EntityCollection collection;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "IDs of the entities to access"
    )
    public Attribute.Long[] ids;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The entities"
    )
    public Attribute.Entity[] entities;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        for (int i = 0; i < ids.length; i++) {

            long id_ = ids[i].getValue();
            for (Attribute.Entity e : collection.getEntities()) {

                if (e.getId() == id_) {
                    entities[i].setValue(e);
                }

            }
        }

    }

}
