/*
 * StandardLMArableParaReader.java
 * used to describe crop rotation
 * Created on 10. Dezember 2005, 12:56
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

package org.jams.j2k.s_n.crop;


import jams.data.*;
import jams.model.*;
import java.util.*;
import org.unijena.j2k.J2KFunctions;

/**
 *
 * @author S. Kralisch file edited by U. Bende-Michl
 */
public class StandardLMArableParaReader extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Arable crop rotation parameter file name"
            )
            public Attribute.String laFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() {
        
        //read crop rotation parameter
        Attribute.EntityCollection croprot = getModel().getRuntime().getDataFactory().createEntityCollection();
        croprot.setEntities(J2KFunctions.readParas(getModel().getWorkspaceDirectory().getPath()+"/"+laFileName.getValue(), getModel()));
        
        HashMap<Double, Attribute.Entity> ctMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity ct, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        
        Iterator<Attribute.Entity> ctIterator = croprot.getEntities().iterator();
        while (ctIterator.hasNext()) {
            ct = ctIterator.next();
            ctMap.put(ct.getDouble("ID"),  ct);//put all entities into a HashMap with their ID as key
                            }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            ct = ctMap.get(e.getDouble("croprotID"));
            e.setObject("croprot", ct);
            
            attrs = ct.getKeys();
            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], ct.getDouble((String) attrs[i]));
            }
            
        }
    }
      
}


