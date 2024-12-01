/*
 * EntityCollectionFromList.java
 * Created on 17.04.2024, 22:32:18
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
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="Title",
    author="Author",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class EntityCollectionFromList extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
            )
            public Attribute.Object entityList;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description"
            )
            public Attribute.EntityCollection entities;
                
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
        entities.setValue((List<Attribute.Entity>)entityList);
    }

    @Override
    public void run() {
    }

    @Override
    public void cleanup() {
    }
}