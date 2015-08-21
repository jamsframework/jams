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
import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import jams.tools.FileTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.unijena.j2k.J2KFunctions;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "StandardEntityReader",
        author = "Sven Kralisch & Christian Fischer",
        description = "This component reads two ASCII files containing data of hru and "
        + "reach entities and creates two collections of entities accordingly. "
        + "1:n topologies between different entities are created based on provided "
        + "attribute names. Additionally, the topologies are checked for cycles.",
        date = "2010-01-29",
        version = "1.2")
@VersionComments(entries
        = @VersionComments.Entry(
                version = "1.2", comment = "Added function to use only a subset "
                + "of all entities. This is defined by a reach ID "
                + "(subcatchmentReachID) which represents the outlet of "
                + "the catchment.")
)
public class StandardEntityReader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "HRU parameter file name")
    public Attribute.String hruFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Reach parameter file name")
    public Attribute.String reachFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute containing the HRU identifiers",
            defaultValue = "ID")
    public Attribute.String hruIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute containing the reach identifiers",
            defaultValue = "ID")
    public Attribute.String reachIDAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to HRU relation in the input file",
            defaultValue = "to_poly")
    public Attribute.String hru2hruAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the HRU to reach relation in the input file",
            defaultValue = "to_reach")
    public Attribute.String hru2reachAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of the attribute describing the reach to reach relation in the input file",
            defaultValue = "to_reach")
    public Attribute.String reach2reachAttribute;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "ID of a reach defining a sub-catchment. Only hrus/reaches draining to this reach "
            + "will be used on the resulting entity collections",
            defaultValue = "-1")
    public Attribute.Double subcatchmentReachID;

    ArrayList<Attribute.Entity> hruList, reachList;
    HashMap<Double, Attribute.Entity> hruMap, reachMap;
    Attribute.Entity nullEntity, defaultRootReach;
    HashMap<Attribute.Entity, List<Attribute.Entity>> children;

    @Override
    public void init() {

        defaultRootReach = null;
        getModel().getRuntime().println("Reading spatial model entities...", JAMS.VERBOSE);

        //read hru parameter
        hruList = J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel());

        //assign IDs to all hru entities
        for (Attribute.Entity e : hruList) {
            try {
                e.setId((long) e.getDouble(hruIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reachList = J2KFunctions.readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel());

        //assign IDs to all reach entities
        for (Attribute.Entity e : reachList) {
            try {
                e.setId((long) e.getDouble(reachIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K reach parameter file (" + reachFileName.getValue() + ")!");
            }
        }

        //create enttiy maps
        createEntityMaps();

        //create object associations from id attributes for hrus and reaches
        createTopology();

        // create a map containing enties and lists of source entites
        createChildrenMap();

        getModel().getRuntime().println("Model entities read successfully. This resulted in "
                + hruList.size() + " HRUs / " + reachList.size() + " reaches overall.", JAMS.STANDARD);

        //create ordered lists and subsets if needed
        createEntityCollections();

        getModel().getRuntime().println("Model entities ordered and subsetted successfully. This resulted in "
                + hrus.getEntities().size() + " HRUs / " + reaches.getEntities().size() + " reaches.", JAMS.STANDARD);

        //create total order on hrus and reaches that allows processing them subsequently
//        createOrderedList(hrus, hru2hruAttribute.getValue());
//        createOrderedList(reaches, reach2reachAttribute.getValue());
    }

    //do depth first search to find cycles
    protected boolean cycleCheck(Attribute.Entity node, Stack<Attribute.Entity> searchStack, HashSet<Long> closedList, HashSet<Long> visitedList) {
        Attribute.Entity child_node;

        //current node allready in search stack -> circle found
        if (searchStack.contains(node)) {
            int index = searchStack.indexOf(node);

            String cyc_output = new String();
            for (int i = index; i < searchStack.size(); i++) {
                cyc_output += ((Attribute.Entity) searchStack.get(i)).getId() + " ";
            }
            getModel().getRuntime().println("Found circle with ids:" + cyc_output);

            return true;
        }
        //node in closed list? -> then skip it
        if (closedList.contains(node.getId()) == true) {
            return false;
        }
        //now this node is visited
        visitedList.add(node.getId());

        child_node = (Attribute.Entity) node.getObject(hru2hruAttribute.getValue());
        if ((child_node != null) && (child_node.isEmpty())) {
            child_node = null;
        }

        if (child_node != null) {
            //push current node to search stack
            searchStack.push(node);

            boolean result = cycleCheck(child_node, searchStack, closedList, visitedList);

            searchStack.pop();

            return result;
        }
        return false;
    }

    protected boolean cycleCheck() {
        Iterator<Attribute.Entity> hruIterator;

        HashSet<Long> closedList = new HashSet<Long>();
        HashSet<Long> visitedList = new HashSet<Long>();

        Attribute.Entity start_node;

        getModel().getRuntime().println("Cycle checking...");

        hruIterator = hruList.iterator();

        boolean result = false;

        while (hruIterator.hasNext()) {
            start_node = hruIterator.next();
            //connected component of start_node allready processed?
            if (closedList.contains(start_node.getId()) == false) {
                if (cycleCheck(start_node, new Stack<Attribute.Entity>(), closedList, visitedList) == true) {
                    result = true;
                }
                closedList.addAll(visitedList);
                visitedList.clear();
            }

        }
        return result;
    }

    private void createChildrenMap() {

        children = new HashMap();
        Attribute.Entity node, parentNode;
        List<Attribute.Entity> l;

        // create an upstream mapping        
        Iterator<Attribute.Entity> hruIterator = hruList.iterator();
        while (hruIterator.hasNext()) {
            node = hruIterator.next();

            // hru -> hru 
            parentNode = (Attribute.Entity) node.getObject(hru2hruAttribute.getValue());
            if (parentNode != nullEntity) {
                // put node into parentNode's list
                l = children.get(parentNode);
                if (l == null) {
                    l = new ArrayList();
                    children.put(parentNode, l);
                }
                l.add(node);
            }

            // hru -> reach
            parentNode = (Attribute.Entity) node.getObject(hru2reachAttribute.getValue());
            if (parentNode != nullEntity) {
                // put node into parentNode's list
                l = children.get(parentNode);
                if (l == null) {
                    l = new ArrayList();
                    children.put(parentNode, l);
                }
                l.add(node);
            }
        }

        Iterator<Attribute.Entity> reachIterator = reachList.iterator();
        while (reachIterator.hasNext()) {
            node = reachIterator.next();

            // reach -> reach
            parentNode = (Attribute.Entity) node.getObject(reach2reachAttribute.getValue());
            if (parentNode != nullEntity) {
                // put node into parentNode's list
                l = children.get(parentNode);
                if (l == null) {
                    l = new ArrayList();
                    children.put(parentNode, l);
                }
                l.add(node);
            }
        }
    }

    public static void main(String[] args) {

        Set<Double> s = new HashSet();
        s.add(new Double(12));
        long x = 12;
        System.out.println(s.contains((double) x));

    }

    private void createEntityCollections() {

        // create a node list of the graph starting at the root reach (breath first enumeration)
        Attribute.Entity root;
        if (subcatchmentReachID.getValue() != -1) {
            root = reachMap.get(subcatchmentReachID.getValue());
            if (root != null) {
                root.setObject(reach2reachAttribute.getValue(), nullEntity);
            } else {
                root = defaultRootReach;
                getModel().getRuntime().println("Subbasin with id " + subcatchmentReachID.getValue() + " does not exist! Using default outlet.");
            }
        } else {
            root = defaultRootReach;
        }
        List<Attribute.Entity> catchmentList = getSubtreeList(root, children);

        // create two new lists for hrus and reaches 
        List<Attribute.Entity> hl = new ArrayList();
        List<Attribute.Entity> rl = new ArrayList();

        // put reaches into hash set for faster search
        Set<Attribute.Entity> reachSet = new HashSet();
        for (Attribute.Entity e : reachMap.values()) {
            reachSet.add(e);
        }

        for (int i = catchmentList.size() - 1; i >= 0; i--) {
            Attribute.Entity e = catchmentList.get(i);
            if (reachSet.contains(e)) {
                rl.add(e);
            } else {
                hl.add(e);
            }
        }

        hrus.setEntities(hl);
        reaches.setEntities(rl);
    }

    private List getSubtreeList(Attribute.Entity node, HashMap<Attribute.Entity, List<Attribute.Entity>> children) {

        List<Attribute.Entity> treeList = new ArrayList();

        treeList.add(node);

        List<Attribute.Entity> l = children.get(node);
        if (l != null) {
            for (Attribute.Entity e : l) {
                treeList.addAll(getSubtreeList(e, children));
            }
        }

        return treeList;
    }

    private void createEntityMaps() {

        //put all entities into a HashMap with their ID as key
        hruMap = new HashMap();
        reachMap = new HashMap();
        Attribute.Entity e;

        Iterator<Attribute.Entity> hruIterator = hruList.iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            double id = (double) e.getId();
            if (!hruMap.containsKey(id)) {
                hruMap.put(id, e);
            } else {
                getModel().getRuntime().sendErrorMsg("Found duplicate HRU-ID (" + e.getId() + "). This may cause errors!");
            }
        }
        Iterator<Attribute.Entity> reachIterator = reachList.iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            double id = (double) e.getId();
            if (!reachMap.containsKey(id)) {
                reachMap.put(id, e);
            } else {
                getModel().getRuntime().sendErrorMsg("Found duplicate reach-ID (" + e.getId() + "). This may cause errors!");
            }
        }

        //create an empty entity, i.e. the one that is linked to in case there is no linkage ;-)
        nullEntity = getModel().getRuntime().getDataFactory().createEntity();
        hruMap.put(0d, nullEntity);
        reachMap.put(0d, nullEntity);
    }

    protected void createTopology() {

        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Attribute.Entity e, toPoly, toReach;

        //associate the hru entities with their downstream entity
        hruIterator = hruList.iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            toPoly = hruMap.get(e.getDouble(hru2hruAttribute.getValue()));
            toReach = reachMap.get(e.getDouble(hru2reachAttribute.getValue()));

            if ((toPoly == null) || (toReach == null)) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for HRU with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            } else {
                if (toPoly == nullEntity && toReach == nullEntity) {
                    getModel().getRuntime().sendInfoMsg("The HRU with ID " + e.getId() + " drains nowhere. This may cause errors!");
                }
            }

            e.setObject(hru2hruAttribute.getValue(), toPoly);
            e.setObject(hru2reachAttribute.getValue(), toReach);

        }

        //associate the reach entities with their downstream entity
        reachIterator = reachList.iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            toReach = reachMap.get(e.getDouble(reach2reachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            } else {
                if (toReach == nullEntity) {
                    if (defaultRootReach != null) {
                        getModel().getRuntime().sendInfoMsg("The river network has more than one outlet! This may cause errors! ID of first outlet is: " + defaultRootReach.getId() + " and the second outlet is: " + toReach.getId());
                    }
                    defaultRootReach = e;
                }
            }

            e.setObject(reach2reachAttribute.getValue(), toReach);

        }

        //check for cycles
        if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
            if (cycleCheck() == true) {
                getModel().getRuntime().sendHalt("HRUs --> cycle found ... :( ");
            } else {
                getModel().getRuntime().println("HRUs --> no cycle found");
            }
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

        //put all collection elements (keys) and their depth (values) into a HashMap
        int maxDepth = 0;
        while (mapChanged) {
            mapChanged = false;
            hruIterator = col.getEntities().iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();

                f = (Attribute.Entity) e.getObject(asso);
                if (f == null) {
                    this.getModel().getRuntime().println("warning hru with id:" + e.getId() + " has no receiver");
                }
                if ((f != null) && (f.isEmpty())) {
                    f = null;
                }

                if (f != null) {
                    eDepth = depthMap.get(e);
                    fDepth = depthMap.get(f);
                    if (fDepth.intValue() <= eDepth.intValue()) {
                        depthMap.put(f, new Integer(eDepth.intValue() + 1));
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
        for (int i = 0; i <= maxDepth; i++) {
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
        for (int i = 0; i <= maxDepth; i++) {
            hruIterator = alList.get(i).iterator();
            while (hruIterator.hasNext()) {
                e = hruIterator.next();
                newList.add(e);
            }
        }
        col.setEntities(newList);
    }
}
