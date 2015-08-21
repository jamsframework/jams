/*
 * J2KTopologyCreator.java
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

import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class J2KTopologyCreator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Collection of reach objects"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Collection of reservoir objects"
            )
            public Attribute.EntityCollection reservoirs;
    
    public void init() {
        
        //create object associations from id attributes for hrus and reaches
        createTopology();
        
        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.STANDARD);
        createOrderedList(hrus, "to_poly");
        getModel().getRuntime().println("Create ordered reach-list", JAMS.STANDARD);
        createOrderedList(reaches, "to_reach");
        getModel().getRuntime().println("Entities read successfull!", JAMS.STANDARD);
        
    }
    
    //do depth first search to find cycles
    protected boolean cycleCheck(Attribute.Entity node,Stack<Attribute.Entity> searchStack,HashSet<Attribute.Double> closedList,HashSet<Attribute.Double> visitedList) {
        Attribute.Entity child_node;
        
        //current node allready in search stack -> circle found
        if ( searchStack.indexOf(node) != -1) {
            int index = searchStack.indexOf(node);
            
            String cyc_output = new String();
            for (int i = index; i < searchStack.size(); i++) {
                cyc_output += ((Attribute.Entity)searchStack.get(i)).getDouble("ID") + " ";
            }
            getModel().getRuntime().println("Found circle with ids:" + cyc_output);
            
            return true;
        }
        //node in closed list? -> then skip it
        if (closedList.contains(node.getObject("ID")) == true)
            return false;
        //now this node is visited
        visitedList.add((Attribute.Double)node.getObject("ID"));
        
        child_node = (Attribute.Entity)node.getObject("to_poly");
        
        if (child_node != null) {
            //push current node to search stack
            searchStack.push(node);
            
            boolean result = cycleCheck(child_node,searchStack,closedList,visitedList);
            
            searchStack.pop();
            
            return result;
        }
        return false;
    }
    
    protected boolean cycleCheck() {
        Iterator<Attribute.Entity> hruIterator;
        
        HashSet<Attribute.Double> closedList = new HashSet<Attribute.Double>();
        HashSet<Attribute.Double> visitedList = new HashSet<Attribute.Double>();
        
        Attribute.Entity start_node;
        
        getModel().getRuntime().println("Cycle checking...");
        
        hruIterator = hrus.getEntities().iterator();
        
        boolean result = false;
        
        while (hruIterator.hasNext()) {
            start_node = hruIterator.next();
            //connected component of start_node allready processed?
            if (closedList.contains(start_node.getObject("ID")) == false) {
                if ( cycleCheck(start_node,new Stack<Attribute.Entity>(),closedList,visitedList) == true) {
                    result = true;
                }
                closedList.addAll(visitedList);
                visitedList.clear();
            }
            
        }
        return result;
    }
    
    protected void createTopology() {
        
        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reservoirMap = null;
        if(this.reservoirs != null){
            reservoirMap = new HashMap<Double, Attribute.Entity>();
        }
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Iterator<Attribute.Entity> reservoirIterator;
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
        if(this.reservoirs != null){
            reservoirIterator = reservoirs.getEntities().iterator();
            while (reservoirIterator.hasNext()) {
                e = reservoirIterator.next();
                reservoirMap.put(e.getDouble("ID"),  e);
            }
        }
        
        //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            e.setObject("to_poly", hruMap.get(e.getDouble("to_poly")));
            e.setObject("to_reach", reachMap.get(e.getDouble("to_reach")));
            if(this.reservoirs != null){
                try {
                    e.setObject("to_reservoir", reservoirMap.get(e.getDouble("to_reservoir")));
                } catch (Attribute.Entity.NoSuchAttributeException ex) {
                    ex.printStackTrace();
                    //ex.printStackTrace();
                }
            }
        }
        
        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            e.setObject("to_reach", reachMap.get(e.getDouble("to-reach")));
            if(this.reservoirs != null){
                try {
                    e.setObject("to_reservoir", reservoirMap.get(e.getDouble("to-reservoir")));
                } catch (Attribute.Entity.NoSuchAttributeException ex) {
                    ex.printStackTrace();
                    //ex.printStackTrace();
                }
            }
        }
        
        //check for cycles
        if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
            if (cycleCheck() == true)
                getModel().getRuntime().println("HRUs --> cycle found ... :( ");
            else
                getModel().getRuntime().println("HRUs --> no cycle found");
        }
        
    }
    
    protected void createOrderedList(Attribute.EntityCollection col, String asso) {
        
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
        
        int numHRUs = col.getEntities().size();
        
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
                        //System.out.println("Processing entity: " + e.getDouble("ID"));
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
    }
}
