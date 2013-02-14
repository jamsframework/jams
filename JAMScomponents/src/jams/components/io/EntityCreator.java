/*
 * EntityCreator.java
 * Created on 21. November 2005, 11:46
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
package jams.components.io;

import java.util.ArrayList;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "EntityCreator",
author = "Sven Kralisch",
description = "Creates a number of empty (holding no attributes) Attribute.Entity " +
        "objects and stores them in a Attribute.EntityCollection object")
public class EntityCreator extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Entities being created")
    public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Number of entities to be created")
    public Attribute.Double count;

    /*
     *  Component runstages
     */
    public void init() {

        ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();

        for (int i = 0; i < count.getValue(); i++) {
            list.add(getModel().getRuntime().getDataFactory().createEntity());
        }

        entities.setEntities(list);
    }
}
