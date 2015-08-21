/*
 * SEProvider.java
 *
 * Created on 8. September 2005, 16:32
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.jamstesting;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title = "SEProvider",
        author = "Sven Kralisch",
        description= ""
        )
        public class SEProvider extends JAMSComponent {
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Long max;
    
    public void init(){
        
        System.out.println("SEProvider init");
        
        Attribute.Entity e;
        
        ArrayList <Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
        Attribute.Double l;
        
        for (long i = 1; i <= max.getValue(); i++) {
            e = JAMSDataFactory.createEntity();
            e.setDouble("attr",  Math.pow(10,i));
            entityList.add(e);            
        }
        
        //entities = new Attribute.EntityCollection();
        entities.setEntities(entityList);
    }
    
    public void run(){
    }
    
    public void cleanup(){
    }
}
