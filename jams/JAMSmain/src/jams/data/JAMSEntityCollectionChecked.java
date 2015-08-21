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
public class JAMSEntityCollectionChecked extends JAMSEntityCollection {
    
    @Override
    public void setEntities(List<Attribute.Entity> entities) {
        this.entities = entities;
        this.entityArray = entities.toArray(new JAMSEntityChecked[entities.size()]);
        if (entityArray.length > 0) {
            this.current = entityArray[0];
        } else {
            this.current = null;
        }
        //attention id changes after this point will do not have any effect
        idMap = new HashMap<Long, Attribute.Entity>();
        for (Attribute.Entity e : entities) {
            idMap.put(e.getId(), e);
        }

    }
}
