/*
 * StandardCropParaReader.java
 * used to describe crop parameters
 * Created on 9. Dezember 2005, 15:30
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
public class StandardCropParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Crop parameter file name"
            )
            public Attribute.String crFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() {
        
        //read crop parameter
        Attribute.EntityCollection crops = getModel().getRuntime().getDataFactory().createEntityCollection();
        crops.setEntities(J2KFunctions.readParas(getModel().getWorkspaceDirectory().getPath()+"/"+crFileName.getValue(), getModel()));
        
        HashMap<Double, Attribute.Entity> crMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity cr, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        
        Iterator<Attribute.Entity> crIterator = crops.getEntities().iterator();
        while (crIterator.hasNext()) {
            cr = crIterator.next();
            crMap.put(cr.getDouble("ID"),  cr);//put all entities into a HashMap with their ID as key
                            }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            cr = crMap.get(e.getDouble("cropID"));
            e.setObject("crop", cr);
            
            attrs = cr.getKeys();
            for (int i = 0; i < attrs.length; i++) {
                e.setDouble((String) attrs[i], cr.getDouble((String) attrs[i]));
            }
            
        }
    }
   
    
    
}

