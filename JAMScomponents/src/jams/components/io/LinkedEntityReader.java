/*
 * LinkedEntityReader.java
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
package jams.components.io;

//import org.unijena.j2k.*;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.Model;
import jams.tools.FileTools;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
version = "1.1_0")
public class LinkedEntityReader extends JAMSComponent {

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

    @Override
    public void init() throws Attribute.Entity.NoSuchAttributeException {

        //read hru parameter
        hrus.setEntities(readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        //assign IDs to all hru entities
        for (Attribute.Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(hruIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        reaches.setEntities(readParas(FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //assign IDs to all reach entities
        for (Attribute.Entity e : reaches.getEntityArray()) {
            try {
                e.setId((long) e.getDouble(reachIDAttribute.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, hru2hruAttribute.getValue());
        getModel().getRuntime().println("HRU entities read successfully", JAMS.STANDARD);
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, reach2reachAttribute.getValue());
        getModel().getRuntime().println("Reach entities read successfully", JAMS.STANDARD);
    }

    //do depth first search to find cycles
    protected boolean cycleCheck(Attribute.Entity node, Stack<Attribute.Entity> searchStack, HashSet<Long> closedList, HashSet<Long> visitedList) throws Attribute.Entity.NoSuchAttributeException {
        Attribute.Entity child_node;

        //current node allready in search stack -> circle found
        if (searchStack.indexOf(node) != -1) {
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
        if ((child_node != null) && (child_node.getValue() == null)) {
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

    protected boolean cycleCheck() throws Attribute.Entity.NoSuchAttributeException {
        Iterator<Attribute.Entity> hruIterator;

        HashSet<Long> closedList = new HashSet<Long>();
        HashSet<Long> visitedList = new HashSet<Long>();

        Attribute.Entity start_node;

        getModel().getRuntime().println("Cycle checking...");

        hruIterator = hrus.getEntities().iterator();

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

    protected void createTopology() throws Attribute.Entity.NoSuchAttributeException {

        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        HashMap<Double, Attribute.Entity> reachMap = new HashMap<Double, Attribute.Entity>();
        Iterator<Attribute.Entity> hruIterator;
        Iterator<Attribute.Entity> reachIterator;
        Attribute.Entity e, toPoly, toReach;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble(hruIDAttribute.getValue()), e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble(reachIDAttribute.getValue()), e);
        }

        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        Attribute.Entity nullEntity = JAMSDataFactory.createEntity();
        nullEntity.setValue((HashMap<String, Object>) null);
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);

        //associate the hru entities with their downstream entity
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            toPoly = hruMap.get(e.getDouble(hru2hruAttribute.getValue()));
            toReach = reachMap.get(e.getDouble(hru2reachAttribute.getValue()));

            if ((toPoly == null) || (toReach == null)) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for HRU with ID "
                        + e.getId() + " could not be found. This may cause errors!");
            }

            e.setObject(hru2hruAttribute.getValue(), toPoly);
            e.setObject(hru2reachAttribute.getValue(), toReach);

        }

        //associate the reach entities with their downstream entity
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();

            toReach = reachMap.get(e.getDouble(reach2reachAttribute.getValue()));

            if (toReach == null) {
                getModel().getRuntime().sendErrorMsg("Topological neighbour for reach with ID "
                        + e.getId() + " could not be found. This may cause errors!");
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

    protected void createOrderedList(Attribute.EntityCollection col, String asso) throws Attribute.Entity.NoSuchAttributeException {

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
                if ((f != null) && (f.getValue() == null)) {
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
    
    private ArrayList<Attribute.Entity> readParas(String fileName, Model model) {

        BufferedReader reader;
        ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
        StringTokenizer tokenizer;

        try {

            reader = new BufferedReader(new FileReader(fileName));

            String s = "#";

            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }

            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            tokenizer = new StringTokenizer(s, "\t");
            while (tokenizer.hasMoreTokens()) {
                String aName = tokenizer.nextToken();

                // this is just a bloody workaround for old J2000 reach parameter files
                if (aName.equals("to-reach")) {
                    aName = "to_reach";
                }
                attributeNames.add(aName);
            }

            //process lower boundaries
            reader.readLine();

            //process upper boundaries
            reader.readLine();
            //coment
            //process units
            reader.readLine();

            //get first line of hru data
            s = reader.readLine();

            while ((s != null) && !s.startsWith("#")) {

                Attribute.Entity e;

                e = JAMSDataFactory.createEntity();
                tokenizer = new StringTokenizer(s, "\t");

                String token;
                for (int i = 0; i < attributeNames.size(); i++) {
                    token = tokenizer.nextToken();
                    try {
                        //hopefully these are double values :-)
                        e.setDouble(attributeNames.get(i), Double.parseDouble(token));
                    } catch (NumberFormatException nfe) {
                        //most probably this happens because of string values within J2K parameter files
                        e.setObject(attributeNames.get(i), token);
                    }
                }

                entityList.add(e);

                s = reader.readLine();
            }

        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }

        return entityList;

    }    
}
