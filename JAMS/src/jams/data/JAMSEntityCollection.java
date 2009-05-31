/*
 * JAMSEntityCollection.java
 * Created on 2. August 2005, 21:03
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
package jams.data;

import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSEntityCollection implements Attribute.EntityCollection {

    protected ArrayList<JAMSEntity> entities = new ArrayList<JAMSEntity>();

    protected JAMSEntity[] entityArray;

    protected JAMSEntity current;

    JAMSEntityCollection() {
    }

    @Override
    public JAMSEntity[] getEntityArray() {
        return this.entityArray;
    }

    @Override
    public JAMSEntityEnumerator getEntityEnumerator() {
        return new EntityEnumerator();
    }

    class EntityEnumerator implements JAMSEntityEnumerator {

        JAMSEntity[] entityArray = getEntityArray();

        int index = 0;

        @Override
        public boolean hasNext() {
            return (index + 1 < entityArray.length);
        }

        @Override
        public JAMSEntity next() {
            index++;
            JAMSEntityCollection.this.current = entityArray[index];
            return entityArray[index];
        }

        @Override
        public void reset() {
            index = 0;
            JAMSEntityCollection.this.current = entityArray[index];
        }
    }

    @Override
    public ArrayList<JAMSEntity> getEntities() {
        return entities;
    }

    @Override
    public void setEntities(ArrayList<JAMSEntity> entities) {
        this.entities = entities;
        this.entityArray = entities.toArray(new JAMSEntity[entities.size()]);
        if (entityArray.length > 0) {
            this.current = entityArray[0];
        } else {
            this.current = null;
        }
    }

    @Override
    public JAMSEntity getCurrent() {
        return current;
    }

    @Override
    public void setValue(String data) {
        //this makes no sense!
    }

    @Override
    public void setValue(ArrayList<JAMSEntity> entities) {
        setEntities(entities);
    }

    @Override
    public ArrayList<JAMSEntity> getValue() {
        return getEntities();
    }
}
