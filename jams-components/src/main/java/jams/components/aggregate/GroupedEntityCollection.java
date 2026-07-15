/*
 * GroupedEntityCollection.java
 * Created on 29.06.2025, 22:32:30
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
package jams.components.aggregate;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "GroupedEntityCollection",
        author = "Sven Kralisch",
        description = "Create a new entity collection based on unique attribute "
        + "values of another entity collection",
        date = "2025-06-29",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class GroupedEntityCollection extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.EntityCollection inputCollection;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.String attributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Description"
    )
    public Attribute.EntityCollection outputCollection;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        
        List<Attribute.Entity> outList = new ArrayList();
        Set<String> uniqueVals = new HashSet();
        
        for (Attribute.Entity e : inputCollection.getEntities()) {
            
            String val = e.getObject(attributeName.getValue()).toString();
            System.out.println(val);
            
        }
        
    }

}
