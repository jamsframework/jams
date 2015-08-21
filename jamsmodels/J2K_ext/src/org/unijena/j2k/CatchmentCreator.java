/*
 * CatchmentCreator.java
 * Created on 22. Februar 2005, 14:15
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

package org.unijena.j2k;

import java.util.ArrayList;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */
public class CatchmentCreator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection with one (catchment) entity object"
            )
            public Attribute.EntityCollection catchment;
    
    
    /*
     *  Component runstages
     */
        
    public void init() {
        ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();
        Attribute.Entity e = getModel().getRuntime().getDataFactory().createEntity();
        e.setDouble("ID", 1);
        list.add(e);
        catchment.setEntities(list);
    }
    
}
