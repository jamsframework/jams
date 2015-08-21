/*
 * J2KGlacier2ReachRouting.java
 * Created on 10. April 2008, 09:21
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

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KGlacier2ReachRouting",
        author="Peter Krause",
        description="Passes the melt of a glacier HRU to a corresponding" +
        "reach as component RD1"
        )
        public class J2KGlacier2ReachRouting extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU statevar glacier melt outflow"
            )
            public Attribute.Double glacierRunoff;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU RD1 from upper HRUs"
            )
            public Attribute.Double inRD1;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the receiving reach"
            )
            public Attribute.Entity toReach;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the receiving glacier HRU"
            )
            public Attribute.Entity toPoly;

     /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;*/

    /*
     *  Component run stages
     */

    public void init() {
    }

    public void run() {
        double gm = glacierRunoff.getValue();
        //Attribute.Entity ent = entities.getCurrent();
        //long id = ent.getId();
        //System.out.println("ID: " + ent.getId());
        if (toPoly.getValue() != null) {
            double RD1inside = this.inRD1.getValue();
            double RD1in = toPoly.getDouble("inRD1");
            RD1in = RD1in + gm + RD1inside;
            inRD1.setValue(0);
            toPoly.setDouble("inRD1", RD1in);
        } else if(toReach.getValue() != null){
            double RD1inside = this.inRD1.getValue();
            double RD1in = toReach.getDouble("inRD1");
            RD1in = RD1in + gm + RD1inside;
            toReach.setDouble("inRD1", RD1in);
        }
    }

    public void cleanup() {

    }
}

