/*
 * EntityAccessor.java
 * Created on 22. März 2006, 08:33
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
package jams.dataaccess;

import jams.data.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class EntityAccessor implements DataAccessor {

    Attribute.Entity componentObject;
    Attribute.Entity[] entityObject;
    int index;
    int accessType;
    String attributeName;

    public EntityAccessor(DataFactory dataFactory,Attribute.Entity[] entities, JAMSData dataObject, String attributeName, int accessType) {

        //get the entities' data objects
        entityObject = new Attribute.Entity[entities.length];
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].existsAttribute(attributeName)) {
                try {
                    entityObject[i] = (Attribute.Entity) entities[i].getObject(attributeName);
                } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                }
            } else {
                if (accessType != DataAccessor.READ_ACCESS) {
                    entityObject[i] = dataFactory.createEntity();
                    entities[i].setObject(attributeName, entityObject[i]);
                } else {
                    throw new Attribute.Entity.NoSuchAttributeException(JAMS.i18n("Attribute_") + attributeName + JAMS.i18n("_does_not_exist!"));
                }
            }
        }

        this.accessType = accessType;
        this.componentObject = (Attribute.Entity) dataObject;
        this.attributeName = attributeName;
    }

    @Override
    public void initEntityData() {
        for (Attribute.Entity v : entityObject) {
            v.setValue(componentObject.getValue());
        }
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void read() {
        componentObject.setValue(entityObject[index].getValue());
    }

    @Override
    public void write() {
        entityObject[index].setValue(componentObject.getValue());
    }

    @Override
    public int getAccessType() {
        return accessType;
    }

    @Override
    public JAMSData getComponentObject() {
        return this.componentObject;
    }
}
