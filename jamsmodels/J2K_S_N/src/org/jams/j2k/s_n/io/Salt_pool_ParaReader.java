/*
 * StandardManagementParaReader.java
 *
 * Created on 6. März 2006, 13:35
 *
 * * This file is part of JAMS
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

package org.jams.j2k.s_n.io;

import org.unijena.j2k.*;
import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.util.*;
import jams.tools.JAMSTools;

/**
 *
 * @author c8fima
 */
public class Salt_pool_ParaReader extends JAMSComponent {
    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Salt_pool parameter file name")
    public Attribute.String hruSaltPoolFileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of hru objects")
    public Attribute.EntityCollection hrus;
    

  
    public void init() {

        //read saltpools
        Attribute.EntityCollection gwTypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        gwTypes.setEntities(J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruSaltPoolFileName.getValue()), getModel()));

        HashMap<Double, Attribute.Entity> saltMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity salt, e;
        Object[] attrs;

        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> gwIterator = gwTypes.getEntities().iterator();
        while (gwIterator.hasNext()) {
            salt = gwIterator.next();
            saltMap.put(salt.getDouble("HRU_ID"), salt);
        }

        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();

            salt = saltMap.get(e.getDouble("saltid"));
            e.setObject("SaltPool", salt);

            if (salt == null) {
                getModel().getRuntime().println("Salt Pool unit defined in entity no. " + e.getDouble("ID") + " is not defined in salt parameter table", JAMS.VERBOSE);
            }
            attrs = salt.getKeys();

            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = salt.getObject((String) attrs[i]);
                if (!(o instanceof Attribute.String)) {
                    e.setObject((String) attrs[i], o);
                }
            }
        }
        getModel().getRuntime().println("Salt Pool parameter file processed ...", JAMS.VERBOSE);
    }
   
        
    
}