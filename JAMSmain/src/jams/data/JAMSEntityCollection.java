/*
 * JAMSEntityCollection.java
 * Created on 2. August 2005, 21:03
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
package jams.data;

import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSEntityCollection implements Attribute.EntityCollection {

    protected List<Attribute.Entity> entities = new ArrayList<Attribute.Entity>();
    protected Attribute.Entity[] entityArray;
    protected Attribute.Entity current;
    protected EntityEnumerator ee = null;
    protected HashMap<Long, Attribute.Entity> idMap;

    @Override
    public Attribute.Entity[] getEntityArray() {
        return this.entityArray;
    }

    @Override
    public String toString() {
        String result = "";
        int i = 0;
        for (Attribute.Entity entity : entities) {
            if (i++ != 0) {
                result += "\n";
            }
            result += entity.toString();
        }
        return result;
    }

    @Override
    public EntityEnumerator getEntityEnumerator() {

//        if (ee == null) {
        ee = new EntityEnumerator() {
            Attribute.Entity[] entityArray = getEntityArray();
            int index = 0;

            @Override
            public boolean hasNext() {
                return (index + 1 < entityArray.length);
            }

            @Override
            public boolean hasPrevious() {
                return index > 0;
            }

            @Override
            public Attribute.Entity next() {
                index++;
                JAMSEntityCollection.this.current = entityArray[index];
                return JAMSEntityCollection.this.current;
            }

            @Override
            public Attribute.Entity previous() {
                index--;
                JAMSEntityCollection.this.current = entityArray[index];
                return JAMSEntityCollection.this.current;
            }

            @Override
            public void reset() {
                index = 0;
                JAMSEntityCollection.this.current = entityArray[index];
            }
        };
//        }

        return ee;
    }

    @Override
    public List<Attribute.Entity> getEntities() {
        return entities;
    }

    @Override
    public void setEntities(List<Attribute.Entity> entities) {
        this.entities = entities;
        this.entityArray = entities.toArray(new JAMSEntity[entities.size()]);
        if (entityArray.length > 0) {
            this.current = entityArray[0];
        } else {
            this.current = null;
        }
        //attention id changes after this point will do not have any effect
        //do it now or never .. (late initialization can cause problem with multiple iterations .. )        
        idMap = new HashMap<Long, Attribute.Entity>();
        for (Attribute.Entity e : entities) {
            idMap.put(e.getId(), e);
        }

    }

    @Override
    public Attribute.Entity getCurrent() {
        return current;
    }

    @Override
    public void setValue(String data) {
        //this makes no sense!
    }

    @Override
    public void setValue(List<Attribute.Entity> entities) {
        setEntities(entities);
    }

    @Override
    public List<Attribute.Entity> getValue() {
        return getEntities();
    }

    @Override
    public Attribute.Entity getEntity(long id) {
        return idMap.get(id);
    }
}
