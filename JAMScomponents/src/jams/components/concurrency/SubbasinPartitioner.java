/*
 * EntityPartitioner.java
 * Created on 08.04.2013, 22:05:12
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
@JAMSComponentDescription(title = "SubbasinPartitioner",
        author = "Sven Kralisch",
        description = "Creates a partitioning of entities in an entity collection"
        + " in such a way that two entities from different partitions are not"
        + " interdependent. The entities' sub-basin membership is used for this "
        + "purpose. The maximum number of partitions can be configured"
        + " by a component attribute.",
        date = "2013-04-08",
        version = "1.0_0")
public class SubbasinPartitioner extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Input entity collection")
    public Attribute.EntityCollection inEntities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Resulting entity collections, length of this array defines the number of partitions")
    public Attribute.EntityCollection[] outEntities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU's subbasin membership in the input file",
            defaultValue = "subbasin")
    public Attribute.String subbasinAttribute;

    @Override
    public void init() {

        // create a partitioning, i.e. a list of all sublists
        List<List<Attribute.Entity>> alList = createSubbasinPartitioning(inEntities, subbasinAttribute.getValue());

        // distribute the sublists over the targeted number of entity collections
        ArrayList<Attribute.Entity>[] listArray = distributeEntities(alList, outEntities.length);

        getModel().getRuntime().println("");
        getModel().getRuntime().println("Partitioning of " + inEntities.getValue().size() + " entities resulted in " + listArray.length + " partitions:");

        // assign entity partitions to component attribute
        for (int i = 0; i < outEntities.length; i++) {
            getModel().getRuntime().println(String.format("    partition %02d: %d entities", i, listArray[i].size()));
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
    protected List<List<Attribute.Entity>> createSubbasinPartitioning(Attribute.EntityCollection col, String asso) {

        ArrayList<List<Attribute.Entity>> alList = new ArrayList();
        HashMap<Double, List<Attribute.Entity>> subbasinMap = new HashMap();

        // create a subbasin mapping
        for (Attribute.Entity e : col.getEntities()) {
            try {
                double subbasinID = e.getDouble(asso);
                List<Attribute.Entity> entityList = subbasinMap.get(subbasinID);
                if (entityList == null) {
                    entityList = new ArrayList<Attribute.Entity>();
                    subbasinMap.put(subbasinID, entityList);
                }
                entityList.add(e);

            } catch (NoSuchAttributeException ex) {
                getModel().getRuntime().sendErrorMsg("Component attribute " + asso + " is not exising in entity " + e.getId() + "!");
            }
        }

        // put all subbasins into an array list
        for (List subbasin : subbasinMap.values()) {
            alList.add(subbasin);
        }

        // sort the array list according to the number of elements in the subbasins
        Collections.sort(alList, new Comparator<List<Attribute.Entity>>() {
            @Override
            public int compare(List<Attribute.Entity> o1, List<Attribute.Entity> o2) {
                return o1.size() - o2.size();
            }
        });

        return alList;
    }

    private ArrayList<Attribute.Entity>[] distributeEntities(List<List<Attribute.Entity>> alList, int numberOfLists) {

        ArrayList<Attribute.Entity> listArray[] = new ArrayList[numberOfLists];
        for (int i = 0; i < listArray.length; i++) {
            listArray[i] = new ArrayList<Attribute.Entity>();
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
                    List<Attribute.Entity> largestList = alList.remove(alList.size() - 1);
                    list.addAll(largestList);
                }

            }
        }

        return listArray;

    }

}