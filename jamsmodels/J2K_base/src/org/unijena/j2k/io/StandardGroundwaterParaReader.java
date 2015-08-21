/*
 * StandardLUReader.java
 * Created on 10. November 2005, 10:53
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
package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.util.*;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardGroundwaterParaReader",
author = "Sven Kralisch",
description = "This component reads an ASCII file containing hydrogeology "
+ "information and adds them to model entities.",
date = "2005-11-10",
version = "1.1_0")
public class StandardGroundwaterParaReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Hydrogeology parameter file name")
    public Attribute.String gwFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;

    public void init() {

        //read gw parameter
        Attribute.EntityCollection gwTypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        gwTypes.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), gwFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> gwMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity gw, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> gwIterator = gwTypes.getEntities().iterator();
        while (gwIterator.hasNext()) {
            gw = gwIterator.next();
            gwMap.put(gw.getDouble("GID"), gw);
        }

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();

            gw = gwMap.get(e.getDouble("hgeoID"));
            e.setObject("hgeoType", gw);

            if (gw == null) {
                getModel().getRuntime().println("Groundwater unit defined in entity no. " + e.getDouble("ID") + " is not defined in geo parameter table", JAMS.VERBOSE);
            }
            attrs = gw.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = gw.getObject((String) attrs[i]);
                if (!(o instanceof Attribute.String)) {
                    e.setObject((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("Groundwater parameter file processed ...", JAMS.VERBOSE);
    }
}
