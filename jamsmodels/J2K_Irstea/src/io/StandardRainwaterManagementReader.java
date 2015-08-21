/*
 * StandardLUReader.java
 * Created on 13. January 2013, 16:39
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
package io;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.util.*;
import jams.tools.JAMSTools;


/**
 *
 * @author M. Labbas
 */
@JAMSComponentDescription(title = "StandardRainwaterManagementReader",
author = "Mériem Labbas",
description = "This component reads an ASCII file containing rainwater management "
+ "information and adds them to model entities.",
date = "2013-01-13",
version = "1.1_0")
public class StandardRainwaterManagementReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Rainwater management parameter file name")
    public Attribute.String rwmFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;

    public void init() {

        //read rwm parameter
        Attribute.EntityCollection rwmTypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        rwmTypes.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), rwmFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> rwmMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity rwm, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> rwmIterator = rwmTypes.getEntities().iterator();
        while (rwmIterator.hasNext()) {
            rwm = rwmIterator.next();
            rwmMap.put(rwm.getDouble("RMID"), rwm);
        }

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();

            rwm = rwmMap.get(e.getDouble("rwmID"));
            e.setObject("rwmType", rwm);

            if (rwm == null) {
                getModel().getRuntime().println("Rainwater management unit defined in entity no. " + e.getDouble("ID") + " is not defined in rwm parameter table", JAMS.VERBOSE);
            }
            attrs = rwm.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = rwm.getObject((String) attrs[i]);
                if (!(o instanceof Attribute.String)) {
                    e.setObject((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("Rainwater management parameter file processed ...", JAMS.VERBOSE);
    }
}
