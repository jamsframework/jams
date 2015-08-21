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

package org.unijena.j2k.regionWK.AP0;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.data.Attribute.EntityCollection;
import jams.tools.JAMSTools;

/**
 *
 * @author S. Kralisch
 */
public class StandardLUReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Land use parameter file name"
            )
            public Attribute.String luFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        //read lu parameter
        EntityCollection lus = JAMSDataFactory.createEntityCollection();
        
        lus.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),luFileName.getValue()), getModel()));
        
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
