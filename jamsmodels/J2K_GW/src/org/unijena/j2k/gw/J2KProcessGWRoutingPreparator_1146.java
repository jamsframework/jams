/*
 * InitJ2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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
package org.unijena.j2k.gw;

import jams.data.*;
import jams.model.*;
import java.util.*;
import java.lang.Math.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class J2KProcessGWRoutingPreparator_1146 extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current hru entity")
    public JAMSEntityCollection hrus;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "The current reach entity")
    public JAMSEntityCollection reaches;

    /*
     *  Component run stages
     */
    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        Attribute.Entity e, f, r;
        HashMap<Attribute.Entity, ArrayList<Attribute.Entity>> routingMap = new HashMap<Attribute.Entity, ArrayList<Attribute.Entity>>();
        Iterator<Attribute.Entity> hruIterator, routingIterator;
        ArrayList<Attribute.Entity> senderPolys;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            if (e.getDouble("type") < 3) {
                f = (JAMSEntity) e.getObject("to_poly");

                //// getModel().getRuntime().println("Processing Entity: " + e.getDouble("ID"));

                if (!routingMap.containsKey(f)) {
                    routingMap.put(f, new ArrayList<Attribute.Entity>());
                }
                senderPolys = routingMap.get(f);
                senderPolys.add(e);

            }
        }
        routingIterator = hrus.getEntities().iterator();
        while (routingIterator.hasNext()) {
            r = routingIterator.next();
            //// getModel().getRuntime().println("Processing Entity: " + r.getDouble("ID"));
            senderPolys = routingMap.get(r);
            if (senderPolys != null) {

                //converting the ArrayLists into Arrays
                JAMSEntity[] from_poly_Array = senderPolys.toArray(new JAMSEntity[senderPolys.size()]);

                //creating new Objects for each entity
                r.setObject("from_poly", from_poly_Array);
            } else {
                r.setObject("from_poly", new JAMSEntity[0]);
            }
        }

         //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            if (e.getDouble("type") == 3) {
                f = (JAMSEntity) e.getObject("to_reach");

                //// getModel().getRuntime().println("HRU: " + e.getDouble("ID"));

                if (!routingMap.containsKey(f)) {
                    routingMap.put(f, new ArrayList<Attribute.Entity>());
                }
                senderPolys = routingMap.get(f);
                senderPolys.add(e);

            }
        }
        routingIterator = reaches.getEntities().iterator();
        while (routingIterator.hasNext()) {
            r = routingIterator.next();
            //// getModel().getRuntime().println("into Reach: " + r.getDouble("ID"));
            senderPolys = routingMap.get(r);
            if (senderPolys != null) {

                //converting the ArrayLists into Arrays
                JAMSEntity[] from_poly_Array = senderPolys.toArray(new JAMSEntity[senderPolys.size()]);

                //creating new Objects for each entity
                r.setObject("from_poly", from_poly_Array);
            } else {
                r.setObject("from_poly", new JAMSEntity[0]);
            }
        }

    }

    public void cleanup() {
    }
}
