/*
 * CalendarAccessor.java
 * Created on 03. October 2005, 10:58
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

package jams.dataaccess;

import jams.data.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class CalendarAccessor implements DataAccessor {
    
    JAMSCalendar componentObject;
    JAMSCalendar[] entityObject;
    int index;
    int accessType;
    
    public CalendarAccessor(JAMSEntity[] entities, JAMSData dataObject, String attributeName, int accessType) throws JAMSEntity.NoSuchAttributeException {
        
        //get the entities' data objects
        entityObject = new JAMSCalendar[entities.length];
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].existsAttribute(attributeName)) {
                try {
                    entityObject[i] = (JAMSCalendar) entities[i].getObject(attributeName);
                } catch (JAMSEntity.NoSuchAttributeException nsae) {}
            } else {
                if (accessType != DataAccessor.READ_ACCESS) {
                    entityObject[i] = new JAMSCalendar();
                    entities[i].setObject(attributeName, entityObject[i]);
                } else {
                    throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + attributeName + JAMS.resources.getString("_does_not_exist!"));
                }
            }
        }
        
        this.accessType = accessType;
        this.componentObject = (JAMSCalendar) dataObject;
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
    
    public JAMSData getComponentObject(){
        return this.componentObject;
    }
}
