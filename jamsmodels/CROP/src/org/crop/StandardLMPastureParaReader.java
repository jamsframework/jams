/*
 * StandardLMPastureParaReader.java
 * Created on 10. Dezember 2005, 13:08
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c5ulbe
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

package org.crop;


import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author S. Kralisch file edited by U. Bende-Michl
 */
public class StandardLMPastureParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Data file directory name"
            )
            public Attribute.String dirName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Pasture management parameter file name"
            )
            public Attribute.String lpFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        //read pasture management parameter
        Attribute.EntityCollection lmpas = new Attribute.EntityCollection();
        lmpas.setEntities(J2KFunctions.readParas(dirName.getValue()+"/"+lpFileName.getValue()));
        
        HashMap<Double, Attribute.Entity> lpMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity lp, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        
        Iterator<Attribute.Entity> lpIterator = lmpas.getEntities().iterator();
        while (lpIterator.hasNext()) {
            lp = lpIterator.next();
            lpMap.put(lp.getDouble("ID"),  lp);//put all entities into a HashMap with their ID as key
                            }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            lp = lpMap.get(e.getDouble("lmpasID"));
            e.setObject("lmPas", lp);
            
            attrs = lp.getDoubleKeys();
            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], lp.getDouble((String) attrs[i]));
            }
            
        }
    }
      
}

