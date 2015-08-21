/*
 * j2000gGroundwaterParaReader.java
 * Created on 10. November 2007, 10:53
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

package org.unijena.j2000g;

import org.unijena.j2k.*;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author P. Krause
 */
public class j2000gGroundwaterParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hydrogeology parameter file name"
            )
            public Attribute.String gwFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() {
        
        //read gw parameter
        Attribute.EntityCollection gwTypes = getModel().getRuntime().getDataFactory().createEntityCollection();

        gwTypes.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),gwFileName.getValue()), getModel()));
        
        HashMap<Double, Attribute.Entity> gwMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity gw, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> gwIterator = gwTypes.getEntities().iterator();
        while (gwIterator.hasNext()) {
            gw = gwIterator.next();
            gwMap.put(gw.getDouble("GID"),  gw);
        }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            //System.out.println("proc hru " + e.getDouble("ID"));
            gw = gwMap.get(e.getDouble("hgeoID"));
            e.setObject("hgeoType", gw);
            
            attrs = gw.getKeys();
            
            for (int i = 0; i < attrs.length; i++) {
                //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                Object o = gw.getObject((String)attrs[i]);
                if(!(o instanceof Attribute.String))
                    e.setObject((String)attrs[i], o);
            }
        }
        getModel().getRuntime().println("Groundwater parameter file processed ...", JAMS.VERBOSE);
    }
}
