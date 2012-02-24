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
import jams.data.Attribute.Entity;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "EntityPartitioner",
author = "Sven Kralisch",
description = "Creates a partitioning of entities in an entity collection"
+ " in such a way that entities within one partition are not"
+ " interdependent. The maximum number of partitions can be configured"
+ " by a component attribute.",
date = "2012-01-30",
version = "1.0_0")
public class EntityPartitioner extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Input entity collection")
    public Attribute.EntityCollection inEntities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Resulting entity collections, length of this array defines the number of partitions")
    public Attribute.EntityCollection[] outEntities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of the attribute describing the HRU to HRU relation in the input file",
    defaultValue = "to_poly")
    public Attribute.String associationAttribute;
    private HashMap<Attribute.Entity, List<Attribute.Entity>> linkMap = new HashMap<>();

    @Override
    public void init() {

        // create a partitioning, i.e. a list of all sublists
        ArrayList<ArrayList<Attribute.Entity>> alList = createDepthPartitioning(inEntities, associationAttribute.getValue());

        // distribute the sublists over the targeted number of entity collections
        ArrayList<Attribute.Entity>[] listArray = distributeEntities(alList, outEntities.length);
        
        getModel().getRuntime().println("");
        getModel().getRuntime().println("Partitioning of " + inEntities.getValue().size() + " entities resultet in " + listArray.length + " partitions:");
        
        // assign entity partitions to component attribute
        for (int i = 0; i < outEntities.length; i++) {
            getModel().getRuntime().println(String.format("    partition %02d: %d entities" , i, listArray[i].size()));
            outEntities[i].setValue(listArray[i]);
        }
        getModel().getRuntime().println("");
    }

    /**
     * Creates a partitioning of an entity collection
     *
     * @param col The entitiy collection
     * @param asso The name of the association attribute
     * @return A list containing lists of entities. The n-th element contains a
     * list with all entities that form a subtree defined by the associations
     * identified by asso, i.e. all entities that are ultimately associated to
     * one root element of that subtree that is not associated to any other
     * element
     */
    protected ArrayList<ArrayList<Attribute.Entity>> createDepthPartitioning(Attribute.EntityCollection col, String asso) {

        ArrayList<ArrayList<Attribute.Entity>> alList = new ArrayList<>();

        // create a reverse link mapping of all entities
        for (Attribute.Entity e : col.getEntities()) {
            try {
                Attribute.Entity f = (Attribute.Entity) e.getObject(asso);
                List<Attribute.Entity> linkList = linkMap.get(f);
                if (linkList == null) {
                    linkList = new ArrayList<>();
                    linkMap.put(f, linkList);
                }
                linkList.add(e);

            } catch (NoSuchAttributeException ex) {
                getModel().getRuntime().sendErrorMsg("Component attribute " + asso + " is not exising in entity " + e.getId() + "!");
            }
        }

        // put all subtrees into an array list
        for (Attribute.Entity e : col.getEntities()) {

            try {
                Attribute.Entity f = (Attribute.Entity) e.getObject(asso);
                if (f.getValue() == null) {
                    alList.add(listSubTree(e));
                }
            } catch (NoSuchAttributeException ex) {
                getModel().getRuntime().sendErrorMsg("Component attribute " + asso + " is not exising in entity " + e.getId() + "!");
            }
        }

        // sort the array list according to the number of elements in the subtrees
        Collections.sort(alList, new Comparator<ArrayList<Attribute.Entity>>() {

            @Override
            public int compare(ArrayList<Entity> o1, ArrayList<Entity> o2) {
                return o1.size() - o2.size();
            }
        });

        return alList;
    }

    private ArrayList<Attribute.Entity>[] distributeEntities(ArrayList<ArrayList<Attribute.Entity>> alList, int numberOfLists) {

        ArrayList<Attribute.Entity> listArray[] = new ArrayList[numberOfLists];
        for (int i = 0; i < listArray.length; i++) {
            listArray[i] = new ArrayList<>();
        }

        while (alList.size() > 0) {

            // calc avg size of lists
            int sum = 0;
            for (ArrayList list : listArray) {
                sum += list.size();
            }
            double avgSize = sum / listArray.length;

            // distribute lists
            for (ArrayList list : listArray) {

                while ((list.size() <= avgSize) && (alList.size() > 0)) {
                    ArrayList<Attribute.Entity> largestList = alList.remove(alList.size() - 1);
                    list.addAll(largestList);
                }

            }
        }

        return listArray;

    }

    private ArrayList<Attribute.Entity> listSubTree(Attribute.Entity root) {

        ArrayList<Attribute.Entity> list = new ArrayList<>();

        // get all direct children of root
        List<Entity> children = linkMap.get(root);
        if (children != null) {
            for (Attribute.Entity child : children) {
                list.addAll(listSubTree(child));
            }
        }

        // finally add the root element
        list.add(root);

        return list;
    }

    /**
     * Creates a partitioning of an entity collection
     *
     * @param col The entitiy collection
     * @param asso The name of the association attribute
     * @return A list containing lists of entities. The n-th element contains a
     * list with all entities of order n. The order is the depth in the depency
     * graph that results from the associations identified by asso, i.e. all
     * entities in the 0-th list are independent from other entities, while
     * entities in the n-th list are directly depending on entities of the
     * (n-1)-th list.
     */
    protected ArrayList<ArrayList<Attribute.Entity>> createBreadthPartitioning(Attribute.EntityCollection col, String asso) {

        Attribute.Entity f;
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