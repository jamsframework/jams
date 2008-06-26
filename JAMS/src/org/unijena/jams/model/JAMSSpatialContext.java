/*
 * JAMSSpatialContext.java
 * Created on 2. August 2005, 20:58
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.jams.model;

import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS spatial context",
author = "Sven Kralisch",
date = "2. August 2005",
description = "This component represents a JAMS context which can be used to " +
"represent spatial contexts in environmental models")
public class JAMSSpatialContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of spatial entities")
    public JAMSEntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Current spatial entity")
    public JAMSEntity currentEntity;

    public JAMSEntityCollection getEntities() {
        return entities;
    }

    public void setEntities(JAMSEntityCollection entities) {
        this.entities = entities;
    }

    public JAMSEntity getCurrentEntity() {
        return currentEntity;
    }

    public void setCurrentEntity(JAMSEntity currentEntity) {
        this.currentEntity = currentEntity;
    }
}
