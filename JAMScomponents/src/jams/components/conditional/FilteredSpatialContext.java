/*
 * JAMSSpatialContext.java
 * Created on 6. July 2012, 13:58
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
package jams.components.conditional;

import jams.data.*;
import jams.data.Attribute.Entity;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSSpatialContext;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS spatial context",
author = "Sven Kralisch",
date = "2012-07-06",
version = "1.0_0",
description = "This component represents a filtered JAMS context which can be "
+ "used to represent space in environmental models")
public class FilteredSpatialContext extends JAMSSpatialContext {
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Double attribute to filter")
    public Attribute.String attributeName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Attribute values to match")
    public Attribute.Double[] attributeValues;

    @Override
    public void init() {
        
        if (attributeName == null || attributeValues == null || attributeValues.length == 0) {
            super.init();
            return;
        }

        ArrayList<Entity> entityList = new ArrayList<Entity>();

        for (Entity e : entities.getEntities()) {
            try {
                if (e.existsAttribute(attributeName.getValue())) {
                    
                    double d = e.getDouble(attributeName.getValue());
                    boolean found = false;
                    
                    for (Attribute.Double value : attributeValues) {
                        if (d == value.getValue()) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) {
                        entityList.add(e);
                    }

                }
            } catch (NoSuchAttributeException ex) {
                getModel().getRuntime().handle(ex);
            }

        }
        entities = JAMSDataFactory.createEntityCollection();
        entities.setEntities(entityList);

        super.init();
    }

    @Override
    public Attribute.EntityCollection getEntities() {
        return entities;
    }

    @Override
    public void setEntities(Attribute.EntityCollection entities) {
        this.entities = entities;
    }
}
