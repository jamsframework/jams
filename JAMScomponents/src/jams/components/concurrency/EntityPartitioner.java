/*
 * Context.java
 * Created on 30.01.2012, 22:13:18
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.concurrency;

import jams.data.*;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
 @JAMSComponentDescription(
        title="EntityPartitioner",
        author="Sven Kralisch",
        description="Creates a partitioning of entities in an entity collection"
         + " in such a way that entities within one partition are not"
         + " interdependent. The maximum number of partitions can be configured"
         + " by a component attribute.",
        date = "2012-01-30",
        version = "1.0_0")
public class EntityPartitioner extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of partitions",
            defaultValue = "1"
            )
            public Attribute.Integer nPartitions;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Input entity collection"
            )
            public Attribute.EntityCollection inEntities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Resulting entity collections"
            )
            public Attribute.EntityCollection[] outEntities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to HRU relation in the input file",
            defaultValue = "to_poly")
            public Attribute.String associationAttribute;    
    
    @Override
    public void init() {
        
        ArrayList<ArrayList<Attribute.Entity>> partitioning = createPartitioning(inEntities, associationAttribute.getValue());
        
        outEntities = new Attribute.EntityCollection[partitioning.size()];
        int i = 0;
        
        for (Attribute.EntityCollection col : outEntities) {
            col = JAMSDataFactory.createEntityCollection();
            col.setValue(partitioning.get(i));
            i++;
        }
    }

    /**
     * Creates a partitioning of an entity collection
     * @param col The entitiy collection
     * @param asso The name of the association attribute
     * @return A list containing lists of entities. The n-th element
     * contains a list with all entities of order n. The order is the depth in 
     * the depency graph that results from the associations identified by asso,
     * i.e. all entities in the 0-th list are independent from other entities, 
     * while entities in the n-th list are directly depending on entities of 
     * the (n-1)-th list.
     */
    protected ArrayList<ArrayList<Attribute.Entity>> createPartitioning(Attribute.EntityCollection col, String asso) {

        Attribute.Entity f;
        ArrayList<Attribute.Entity> newList = new ArrayList<>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<>();
        Integer eDepth, fDepth;
        boolean mapChanged = true;

        for (Attribute.Entity e : col.getEntities()) {
            depthMap.put(e, new Integer(0));
        }

        //put all collection elements (keys) and their depth (values) into a HashMap
        int maxDepth;
        while (mapChanged) {
            mapChanged = false;
            for (Attribute.Entity e : col.getEntities()) {
                
                try {
                    
                    f = (Attribute.Entity) e.getObject(asso);
                    if ((f != null) && (f.getValue() != null)) {
                        eDepth = depthMap.get(e);
                        fDepth = depthMap.get(f);
                        if (fDepth <= eDepth) {
                            depthMap.put(f, eDepth + 1);
                            mapChanged = true;
                        }
                    }
                                    
                } catch (NoSuchAttributeException ex) {
                    getModel().getRuntime().sendErrorMsg("Component attribute " + asso + " is not exising!");
                }
            }
        }

        //find out which is the max depth of all entities
        maxDepth = 0;
        for (Attribute.Entity e : col.getEntities()) {
            maxDepth = Math.max(maxDepth, depthMap.get(e).intValue());
        }

        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<>();
        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Attribute.Entity>());
        }

        //fill the ArrayList objects within the ArrayList with entity objects
        for (Attribute.Entity e : col.getEntities()) {
            int depth = depthMap.get(e).intValue();
            alList.get(depth).add(e);
        }

        return alList;
    }
}