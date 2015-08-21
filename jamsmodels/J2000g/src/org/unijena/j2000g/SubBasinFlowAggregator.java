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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class SubBasinFlowAggregator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection subBasins;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ids of subbasins"
            )
            public Attribute.Double[] subbasinIDs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum"
            )
            public Attribute.Double[] sum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attribute name",
            defaultValue = "totQ"
            )
            public Attribute.String[] attributeName;
    /*
     *  Component run stages
     */

    @Override
    public void run() {
        for (int i = 0; i < sum.length; i++) {
            double sum = 0;
            for (Attribute.Double id : subbasinIDs) {
                int iid = (int) id.getValue();
                Attribute.Entity entity = subBasins.getEntity(iid);
                sum += entity.getDouble(attributeName[i].getValue());
            }
            this.sum[i].setValue(sum);
        }
    }
}
