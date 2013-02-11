/*
 * DoubleArrayAccessor.java
 * Created on 19. März 2006, 14:07
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
public class DoubleArrayAccessor implements DataAccessor {

    Attribute.DoubleArray componentObject;

    Attribute.DoubleArray[] entityObject;

    int index;

    int accessType;

    public DoubleArrayAccessor(DataFactory dataFactory,Attribute.Entity[] entities, JAMSData dataObject, String attributeName, int accessType) throws JAMSEntity.NoSuchAttributeException {

        //get the entities' data objects
        entityObject = new Attribute.DoubleArray[entities.length];
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].existsAttribute(attributeName)) {
                try {
                    entityObject[i] = (Attribute.DoubleArray) entities[i].getObject(attributeName);
                } catch (JAMSEntity.NoSuchAttributeException nsae) {
                }
            } else {
                if (accessType != DataAccessor.READ_ACCESS) {
                    entityObject[i] = dataFactory.createDoubleArray();
                    entities[i].setObject(attributeName, entityObject[i]);
                } else {
                    throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + attributeName + JAMS.i18n("_does_not_exist!"));
                }
            }
        }

        this.accessType = accessType;
        this.componentObject = (Attribute.DoubleArray) dataObject;
    }

    @Override
    public void initEntityData() {
        for (Attribute.DoubleArray array : entityObject) {
            if (componentObject.getValue() != null) {
                array.setValue((double[]) componentObject.getValue().clone());
            }
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
