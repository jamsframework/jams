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
        title="FlowAggregator",
        author="Peter Krause",
        description="Recalculates outflows from reaches into user defined flow values."
        )
        public class FlowAggregator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target entity keeping the aggregated data"
            )
            public Attribute.Entity targetEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Attribute name of flow component"
            )
            public Attribute.String aNameFlowComponent;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach entity last = 1, all = 0"
            )
            public Attribute.Integer reachEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Area of the target entity"
            )
            public Attribute.String aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Aggregation type"
            )
            public Attribute.Integer aggregationType;
    
    final int NO_TRANSFORMATION = 0;
    final int LITRES_2_CUBIC_METERS = 1;
    final int LITRES_2_CUBIC_METERS_PER_SECOND = 2;
    final int MILLIMETERS = 3;
    final int LITRES_PER_SECOND = 4;
    
    final int ALL = 0;
    final int LAST = 1;
    
    public void run() {
        
        //eigentlich bräuchten wir hier einen ReachOutputWriter
        //jetzt aber erstmal quick and dirty nur den outflow reach!
        Attribute.Entity[] entities = reaches.getEntityArray();
        double flow = 0;
        double out_flow = 0;
        if(this.reachEntity.getValue() == this.ALL){
            for(int i = 0; i < entities.length; i++){
                flow += entities[i].getDouble(aNameFlowComponent.getValue());
            }
        }else if(this.reachEntity.getValue() == this.LAST){
            flow = entities[entities.length - 1].getDouble(aNameFlowComponent.getValue());
        }
        if (aggregationType.getValue() == this.NO_TRANSFORMATION) {
            out_flow = flow;
        } else if(aggregationType.getValue() == this.LITRES_2_CUBIC_METERS){
            out_flow = flow / 1000;
        } else if(aggregationType.getValue() == this.LITRES_2_CUBIC_METERS_PER_SECOND){
            out_flow = flow / (1000 * 86400);
        } else if(aggregationType.getValue() == this.MILLIMETERS){
            out_flow = flow / this.targetEntity.getDouble(aNameArea.getValue());
        } else if(aggregationType.getValue() == this.LITRES_PER_SECOND){
            out_flow = flow / (3600);
        }
        
        
        targetEntity.setDouble(aNameFlowComponent.getValue(), out_flow);
        //}
        
        
    }
    
}
