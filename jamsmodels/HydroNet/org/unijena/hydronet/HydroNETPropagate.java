/*
 * HydroNETPropagate.java
 * Created on 24. Mai 2006, 15:44
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

package org.unijena.hydronet;

import jams.data.*;
import jams.data.Attribute.Entity;
import jams.model.*;
import java.util.*;

/**
 *
 * @author Christian Fischer
 */
public class HydroNETPropagate extends JAMSComponent {
    @JAMSComponentDescription(
        title="HydroNETPropagate",
        author="Christian Fischer",
        description=""
        )
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of spatial entities"
            )
            public Attribute.EntityCollection entities;
        
    @Override
    public void run() throws Attribute.Entity.NoSuchAttributeException { 
	Iterator<Entity> Iterator = entities.getEntities().iterator();
	while (Iterator.hasNext()) {
	    Entity entity = Iterator.next();
	    ((DistNeuron)entity.getObject("DIST_NEURON")).propagate();
	    ((NONeuron)entity.getObject("NITROGEN_NEURON")).propagate();
	    ((CostNeuron)entity.getObject("COST_NEURON")).propagate();
	}
    }
}    

