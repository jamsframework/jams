/*
 * LayeredSoilParaReader.java
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
import jams.tools.JAMSTools;

/**
 *
 * @author P. Krause
 */
public class LayeredSoilParaReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil types parameter file name"
            )
            public Attribute.String stFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    
    
    public void init() {
        
        //read soil parameters
        Attribute.EntityCollection soilTypes = getModel().getRuntime().getDataFactory().createEntityCollection();
        soilTypes.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),stFileName.getValue()), getModel()));
        
        HashMap<Double, Attribute.Entity> stMap = new HashMap<Double, Attribute.Entity>();
        Attribute.Entity st, e;
        Object[] attrs;
        
        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> stIterator = soilTypes.getEntities().iterator();
        while (stIterator.hasNext()) {
            st = stIterator.next();
            stMap.put(st.getDouble("SID"),  st);
        }
        
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            e.setDouble("depth", 100);
         
            
            //here we have to deal with horizons
            //first we lump them together
            double hruSID = (e.getDouble("soilID") * 100) + 1;
            int horizon = 0;
            while(stMap.containsKey(hruSID)){
                Double hSID = new Double(""+hruSID);
                st = stMap.get(hSID);
                e.setObject("soilHorizon", st);
                attrs = st.getKeys();
                
                for (int i = 0; i < attrs.length; i++) {
                    //e.setDouble((String) attrs[i], lu.getDouble((String) attrs[i]));
                    Object o = st.getObject((String)attrs[i]);
                    if(!(o instanceof Attribute.String))
                        e.setObject((String)attrs[i]+"_h"+horizon, o);
                }
                hruSID++;
                horizon++;
            }
            
            e.setDouble("horizons", horizon);
        }
        getModel().getRuntime().println("Layered soil parameter file processed ...");
    }
    
    
    
}
