/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
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

//import org.unijena.j2k.*;
import jams.data.Attribute;
import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.workspace.stores.InputDataStore;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StationEntityCreator",
author = "Peter Krause",
description = "This component creates a set of entities which equals climate or" +
        "precipitation stations used as input.",
date = "2010-05-10",
version = "1.0")
public class StationEntityCreator extends JAMSComponent {

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Collection of entities")
        public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Input climates station data file"
            )
            public Attribute.String inFile;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "ID of the datastore to read station coordinates from")
    public Attribute.String dataStoreID;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "the station names")
    public Attribute.StringArray statNames;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "the station Ids")
    public Attribute.IntegerArray statId;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "the station elevation")
    public Attribute.DoubleArray statElev;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "the station x-coordinates")
    public Attribute.DoubleArray statX;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "the station y-coordinates")
    public Attribute.DoubleArray statY;
    
    ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();

    @Override
    public void init() {
        InputDataStore store = null;
        if (dataStoreID != null) {
            store = getModel().getWorkspace().getInputDataStore(dataStoreID.getValue());
        }
        this.statNames.setValue(listToStringArray(store.getDataSetDefinition().getAttributeValues("NAME")));
        this.statId.setValue(listToIntegerArray(store.getDataSetDefinition().getAttributeValues("ID")));
        this.statElev.setValue(listToDoubleArray(store.getDataSetDefinition().getAttributeValues("ELEVATION")));
        this.statX.setValue(listToDoubleArray(store.getDataSetDefinition().getAttributeValues("X")));
        this.statY.setValue(listToDoubleArray(store.getDataSetDefinition().getAttributeValues("Y")));
        
        int nEntities = statNames.getValue().length;
        Attribute.EntityCollection ents = getModel().getRuntime().getDataFactory().createEntityCollection();
        for(int i = 0; i < nEntities; i++){
           Attribute.Entity e;
                try {
                    e = (Attribute.Entity) getModel().getRuntime().getDataFactory().createInstance(Attribute.Entity.class);
                    e.setObject("NAME", statNames.getValue()[i]);
                    e.setInt("ID", this.statId.getValue()[i]);
                    e.setDouble("ELEVATION", this.statElev.getValue()[i]);
                    e.setDouble("X", this.statX.getValue()[i]);
                    e.setDouble("Y", this.statY.getValue()[i]);
                    
                    entityList.add(e);
                } catch (InstantiationException ex) {
                } catch (IllegalAccessException ex) {
                } 
        }
        this.entities.setEntities(entityList);
        
        
        
    }
     private double[] listToDoubleArray(ArrayList<Object> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = ((Double) o).doubleValue();
            i++;
        }
        return result;
    }

      private int[] listToIntegerArray(ArrayList<Object> list) {
        int[] result = new int[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = Integer.parseInt(o.toString());
            i++;
        }
        return result;
    }

      private String[] listToStringArray(ArrayList<Object> list) {
        String[] result = new String[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = (String)o.toString();
            i++;
        }
        return result;
    }
    
}
