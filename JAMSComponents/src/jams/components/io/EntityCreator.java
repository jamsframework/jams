/*
 * EntityCreator.java
 * Created on 21. November 2005, 11:46
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

package jams.components.io;

import java.util.ArrayList;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
public class EntityCreator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Entities being created"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of entities to be created"
            )
            public JAMSDouble count;
    
    
    /*
     *  Component runstages
     */
    
    public void init() {
        
        ArrayList<JAMSEntity> list = new ArrayList<JAMSEntity>();
        
        for (int i = 0; i < count.getValue(); i++) {
            list.add(JAMSDataFactory.createEntity());
        }
        
        entities.setEntities(list);
    }
    
}
