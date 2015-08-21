/*
 * SpatialAggregator.java
 * Created on 18. November 2005, 20:40
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="SpatialAggregator",
        author="Sven Kralisch",
        description="Aggregates spatial parameters values of all entities in an entity collection." +
        "These can be optionally weighted by the entities area."
        )
        public class SpatialAggregator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target entity keeping the aggregated data"
            )
            public Attribute.Entity targetEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Attribute name of regionalised data"
            )
            public Attribute.String aNameData;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Attribute name of entity area"
            )
            public Attribute.String aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Aggregation type"
            )
            public Attribute.Integer aggregationType;
    
    
    public void run() {
        
        Attribute.Entity[] entities = hrus.getEntityArray();
        
        double dataSum = 0, areaSum = 0, area = 0, data;
        
        if (aggregationType.getValue() == 1) {
            for (int i = 0; i < entities.length; i++) {
                area = entities[i].getDouble(aNameArea.getValue());
                areaSum += area;
                dataSum += entities[i].getDouble(aNameData.getValue())*area;
            }
            dataSum /= areaSum;
        } else if(aggregationType.getValue() == 0){
            for (int i = 0; i < entities.length; i++) {
                dataSum += entities[i].getDouble(aNameData.getValue());
            }
        } else if(aggregationType.getValue() == 3){
            for (int i = 0; i < entities.length; i++) {
                area = entities[i].getDouble(aNameArea.getValue());
                areaSum += area;
                dataSum += entities[i].getDouble(aNameData.getValue());
            }
            
            dataSum = dataSum / areaSum;
        } else if(aggregationType.getValue() == 4){ //from kg/ha to average kg/ha
            for (int i = 0; i < entities.length; i++) {
                area = entities[i].getDouble(aNameArea.getValue());
                areaSum += area;
                data = entities[i].getDouble(aNameData.getValue());
                data = data * area / 10000;
                           
                dataSum += data;
            }
            
            dataSum = dataSum / (areaSum /10000) ;
        } else if(aggregationType.getValue() == 5){ //area weighted (accurate)
            for (int i = 0; i < entities.length; i++) {
                area = entities[i].getDouble(aNameArea.getValue());
                areaSum += area;
                data = entities[i].getDouble(aNameData.getValue());
                data = data * area;
                           
                dataSum += data;
            }
            
            dataSum = dataSum / areaSum ;
        } else if(aggregationType.getValue() == 6){ // average 
            for (int i = 0; i < entities.length; i++) {
                
                dataSum += entities[i].getDouble(aNameData.getValue());
                area = i;
                
            
            }
            
        }
        
        
        targetEntity.setDouble(aNameData.getValue(), dataSum);
        
    }
    
}
