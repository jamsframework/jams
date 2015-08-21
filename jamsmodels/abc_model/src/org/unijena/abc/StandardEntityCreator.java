/*
 * StandardEntityCreator.java
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

package org.unijena.abc;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityCreator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of spatial objects"
            )
            public Attribute.EntityCollection spObj;
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        //read hru parameter
        spObj = getModel().getRuntime().getDataFactory().createEntityCollection();
        ArrayList <Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
        for(int i = 0; i < 3; i++){
            Attribute.Entity e = getModel().getRuntime().getDataFactory().createEntity();
            entityList.add(e);
        }
        spObj.setEntities(entityList);        
    }
    
    /*private void createTopology() throws Attribute.Entity.NoSuchAttributeException {
        
        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Attribute.Entity e;
        
        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"),  e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"),  e);
        }
        
        //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            e.setObject("to_poly", hruMap.get(e.getDouble("to_poly")));
            e.setObject("to_reach", reachMap.get(e.getDouble("to_reach")));
        }
        
        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            e.setObject("to_reach", reachMap.get(e.getDouble("to-reach")));
        }
        
    }
    
    private void createOrderedList(Attribute.EntityCollection col, String asso) throws Attribute.Entity.NoSuchAttributeException {
        
        Iterator<Attribute.Entity> hruIterator;
        Attribute.Entity e, f;
        ArrayList<Attribute.Entity> newList = new ArrayList<Attribute.Entity>();
        HashMap<Attribute.Entity, Integer> depthMap = new HashMap<Attribute.Entity, Integer>();
        Integer eDepth, fDepth;
        boolean mapChanged = true;
        
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            depthMap.put(hruIterator.next(), new Integer(0));
        }
        
        //put all collection elements (keys) and their depth (values) into a HashMap
        int maxDepth = 0;
        while (mapChanged) {
            mapChanged = false;
            hruIterator = col.getEntities().iterator();
            while (hruIterator.hasNext()) {
                
                e = hruIterator.next();
                f = (Attribute.Entity) e.getObject(asso);
                if (f != null) {
                    eDepth = depthMap.get(e);
                    fDepth = depthMap.get(f);
                    if (fDepth.intValue() <= eDepth.intValue()) {
                        depthMap.put(f, new Integer(fDepth.intValue()+1));
                        mapChanged = true;
                    }
                }
            }
        }
        
        
        //find out which is the max depth of all entities
        maxDepth = 0;
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            maxDepth = Math.max(maxDepth, depthMap.get(e).intValue());
        }
        
        //create ArrayList of ArrayList objects, each element keeping the entities of one level
        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<ArrayList<Attribute.Entity>>();
        for (int i=0; i<=maxDepth; i++) {
            alList.add(new ArrayList<Attribute.Entity>());
        }
        
        //fill the ArrayList objects within the ArrayList with entity objects
        hruIterator = col.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            int depth = depthMap.get(e).intValue();                
            alList.get(depth).add(e);
        }
        
        //put the entities
        for (int i=0; i<=maxDepth; i++) {
            hruIterator = alList.get(i).iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }*/
}
