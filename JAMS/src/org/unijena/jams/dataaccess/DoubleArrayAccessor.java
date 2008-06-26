/*
 * DoubleArrayAccessor.java
 * Created on 19. März 2006, 14:07
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

package org.unijena.jams.dataaccess;

import org.unijena.jams.data.*;

/**
 *
 * @author S. Kralisch
 */
public class DoubleArrayAccessor implements JAMSEntityDataAccessor {
    
    JAMSDoubleArray componentObject;
    JAMSDoubleArray[] entityObject;
    int index;
    int accessType;
    
    public DoubleArrayAccessor(JAMSEntity[] entities, JAMSData dataObject, String attributeName, int accessType) throws JAMSEntity.NoSuchAttributeException {
        
        //get the entities' data objects
        entityObject = new JAMSDoubleArray[entities.length];
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].existsAttribute(attributeName)) {
                try {
                    entityObject[i] = (JAMSDoubleArray) entities[i].getObject(attributeName);
                } catch (JAMSEntity.NoSuchAttributeException nsae) {}
            } else {
                if (accessType != JAMSEntityDataAccessor.READ_ACCESS) {
                    entityObject[i] = new JAMSDoubleArray();
                    entities[i].setObject(attributeName, entityObject[i]);
                } else {
                    throw new JAMSEntity.NoSuchAttributeException("Attribute " + attributeName + " does not exist!");
                }
            }
        }
        
        this.accessType = accessType;
        this.componentObject = (JAMSDoubleArray) dataObject;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public void read() {
        componentObject.setValue(entityObject[index].getValue());
    }
    
    public void write() {
        entityObject[index].setValue(componentObject.getValue());
    }
    
    public int getAccessType() {
        return accessType;
    }  
    
    public Object getComponentObject(){
        return this.componentObject;
    }
}
