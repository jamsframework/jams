/*
 * VarSummation.java
 * Created on 22. Februar 2005, 15:01
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

package org.unijena.j2000g;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
public class ProcessRouting extends JAMSComponent {
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection subBasins;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input values"
            )
            public Attribute.Double dirQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input values"
            )
            public Attribute.Double gwRecharge;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input values"
            )
            public Attribute.Double totQ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input values"
            )
            public Attribute.Double subBasinID;
    
                
    @Override
    public void init() {
        
    }

    @Override
    public void run() {
        int id = (int)subBasinID.getValue();
        Attribute.Entity entity = subBasins.getEntity(id);
        
        double subBasin_dirQ = entity.getDouble("dirQ");
        double subBasin_gwRecharge = entity.getDouble("gwRecharge");
        double subBasin_totQ = entity.getDouble("totQ");
                
        entity.setDouble("dirQ", subBasin_dirQ + dirQ.getValue());
        entity.setDouble("gwRecharge", subBasin_gwRecharge + gwRecharge.getValue());
        entity.setDouble("totQ", subBasin_totQ + totQ.getValue());
    }
    
    @Override
    public void cleanup(){

    }
    
}
