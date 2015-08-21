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
public class WeightedDistributedSumAggregator extends JAMSComponent {
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of entity objects"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "id of dst entity"
            )
            public Attribute.Double entityID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute name of sum"
            )
            public Attribute.String[] dstAttributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = ""
            )
            public Attribute.Double[] srcAttribute;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "weight value"
            )
            public Attribute.Double weight;
                                        
    @Override
    public void run() {
        int id = (int)entityID.getValue();
        Attribute.Entity entity = entities.getEntity(id);
        
        //check preconditions
        if (entity == null){
            getModel().getRuntime().sendHalt("Entity " + id + " is not existing!");
            return;
        }
        
        if (srcAttribute.length != dstAttributeName.length){
            getModel().getRuntime().sendHalt("Lengths of srcAttribute and dstAttribute differs!");
            return;
        }
                        
        for (int i=0;i<srcAttribute.length;i++){
            double value1 = entity.getDouble(dstAttributeName[i].getValue());
            double value2 = srcAttribute[i].getValue() / weight.getValue();
            entity.setDouble(dstAttributeName[i].getValue(), value1 + value2);
        }                
    }    
}
