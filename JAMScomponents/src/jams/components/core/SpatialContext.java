/*
 * SpatialContext.java
 * Created on 2. August 2005, 20:58
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.components.core;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS spatial context",
        author = "Sven Kralisch",
        date = "2005-08-02",
        version = "1.0_0",
        description = "This component represents a JAMS context which can be used to "
        + "represent space in environmental models")
public class SpatialContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "List of spatial entities")
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Current entity")
    public Attribute.Entity current;    

    @Override
    public Attribute.EntityCollection getEntities() {
        return entities;
    }

    @Override
    public void setEntities(Attribute.EntityCollection entities) {
        this.entities = entities;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(Attribute.Entity entity) {
        this.current.setId(entity.getId());
    }
}
