/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2k.tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="Entity ID writer",
        author="Manfred Fink",
        description="Writes the entity ID in a JAMS double Attribute",
        version="1.0",
        date="2014-07-01"
        )

        public class J2KEntityID extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The entity collection"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Entity ID",
            unit = "-"
            )
            public Attribute.Double EntityID;
    
    
     
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        Attribute.Entity entity = entities.getCurrent();
        
        Double EntityID_run = entity.getDouble("ID");
        
        EntityID.setValue(EntityID_run);
        
        
        
    }
    
    public void cleanup() {
        
    }
}
    
    /**
     * Calculates flow velocity in specific reach
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    