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
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.tools.FileTools;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardLUReader",
author = "Sven Kralisch",
description = "This component reads an ASCII file containing land use "
+ "information and adds them to model entities.",
date = "2005-11-10",
version = "1.1_0")
public class StandardLUReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Land use parameter file name"
            )
            public Attribute.String luFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() {
        //read lu parameter
        Attribute.EntityCollection lus = getModel().getRuntime().getDataFactory().createEntityCollection();
        
        lus.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),luFileName.getValue()), getModel()));
        
        HashMap<Double, Attribute.Entity> luMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity lu, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> luIterator = lus.getEntities().iterator();
        while (luIterator.hasNext()) {
            lu = luIterator.next();
            luMap.put(lu.getDouble("LID"),  lu);
        }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            lu = luMap.get(e.getDouble("landuseID"));
            e.setObject("landuse", lu);
            
            if (lu == null){
                getModel().getRuntime().sendHalt("Error landuse id " + e.getDouble("landuseID") + " of entity " + e.getId() + " not found!");
                return;
            }
            
            attrs = lu.getKeys();
            
            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = lu.getObject((String)attrs[i]);
                if(!(o instanceof Attribute.String))
                    e.setObject((String)attrs[i], o);
            }
            
        }
        getModel().getRuntime().println("Landuse parameter file processed ...", JAMS.VERBOSE);
    }
    
    
    
}
