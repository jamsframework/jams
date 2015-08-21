package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.data.Attribute.Entity;
import jams.tools.JAMSTools;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch,C. Michel
 */
public class StandardEntityReaderRR extends JAMSComponent {
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "HRU parameter file name")
    public Attribute.String hruFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Reach parameter file name")
    public Attribute.String reachFileName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE, update = JAMSVarDescription.UpdateType.RUN, description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE, update = JAMSVarDescription.UpdateType.RUN, description = "Collection of reach objects")
    public Attribute.EntityCollection reaches;

    public void init() {
        //to handle relative path names        
        hrus.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), hruFileName.getValue()), getModel()));

        for (Entity e : hrus.getEntityArray()) {
            try {
                e.setId((long) e.getDouble("ID"));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"ID\" while reading J2K HRU parameter file (" + hruFileName.getValue() + ")!");
            }
        }

        //read reach parameter
        //reaches = new Attribute.EntityCollection();
        reaches.setEntities(J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), reachFileName.getValue()), getModel()));

        //create object associations from id attributes for hrus and reaches
        createTopology();

        //create total order on hrus and reaches that allows processing them subsequently
        getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
        createOrderedList(hrus, "to_poly");
        getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
        createOrderedList(reaches, "to_reach");
        getModel().getRuntime().println("Entities read successfull!", JAMS.VERBOSE);
        
        //aendert die Ordnungsreihenfolge der Reaches
        Entity[] Reaches= reaches.getEntityArray();
        int laenge=Reaches.length;
        Entity change;
        for (int i=0;i<laenge/2;i++)
        {
          change=Reaches[i];
          Reaches[i]=Reaches[laenge-1-i];
          Reaches[laenge-1-i]=change;
        }
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (int i=0;i<laenge;i++)
        {
        list.add(Reaches[i]);
        }

        reaches.setEntities(list);
        
    }

    //do depth first search to find cycles
    protected boolean cycleCheck(Entity node, Stack<Entity> searchStack, HashSet<Attribute.Double> closedList, HashSet<Attribute.Double> visitedList) {
        Attribute.Entity child_node;

        //current node allready in search stack -> circle found
        if (searchStack.indexOf(node) != -1) {
            int index = searchStack.indexOf(node);

            String cyc_output = new String();
            for (int i = index; i < searchStack.size(); i++) {
                cyc_output += ((Attribute.Entity) searchStack.get(i)).getDouble("ID") + " ";
            }
            getModel().getRuntime().println("Found circle with ids:" + cyc_output);

            return true;
        }
        //node in closed list? -> then skip it
        if (closedList.contains(node.getObject("ID")) == true) {
            return false;
        }
        //now this node is visited
        visitedList.add((Attribute.Double) node.getObject("ID"));

        child_node = (Attribute.Entity) node.getObject("to_poly");
        if (child_node.isEmpty()) {
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
        Iterator<Entity> hruIterator;

        HashSet<Attribute.Double> closedList = new HashSet<Attribute.Double>();
        HashSet<Attribute.Double> visitedList = new HashSet<Attribute.Double>();

        Entity start_node;

        getModel().getRuntime().println("Cycle checking...");

        hruIterator = hrus.getEntities().iterator();

        boolean result = false;

        while (hruIterator.hasNext()) {
            start_node = hruIterator.next();
            //connected component of start_node allready processed?
            if (closedList.contains(start_node.getObject("ID")) == false) {
                if (cycleCheck(start_node, new Stack<Entity>(), closedList, visitedList) == true) {
                    result = true;
                }
                closedList.addAll(visitedList);
                visitedList.clear();
            }

        }
        return result;
    }

    protected void createTopology() {

        HashMap<Double, Entity> hruMap = new HashMap<Double, Entity>();
        HashMap<Double, Entity> reachMap = new HashMap<Double, Entity>();
        Iterator<Entity> hruIterator;
        Iterator<Entity> reachIterator;
        Entity e;

        //put all entities into a HashMap with their ID as key
        hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            e = hruIterator.next();
            hruMap.put(e.getDouble("ID"), e);
        }
        reachIterator = reaches.getEntities().iterator();
        while (reachIterator.hasNext()) {
            e = reachIterator.next();
            reachMap.put(e.getDouble("ID"), e);
        }

        //create empty entities, i.e. those that are linked to in case there is no linkage ;-)
        Attribute.Entity nullEntity = getModel().getRuntime().getDataFactory().createEntity();
        hruMap.put(new Double(0), nullEntity);
        reachMap.put(new Double(0), nullEntity);

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

        //check for cycles
        if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
            if (cycleCheck() == true) {
                getModel().getRuntime().println("HRUs --> cycle found ... :( ");
            } else {
                getModel().getRuntime().println("HRUs --> no cycle found");
            }
        }

    }

    protected void createOrderedList(Attribute.EntityCollection col, String asso) {

        Iterator<Entity> hruIterator;
        Entity e, f;
        ArrayList<Entity> newList = new ArrayList<Entity>();
        HashMap<Entity, Integer> depthMap = new HashMap<Entity, Integer>();
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
                if (f.isEmpty()) {
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
        ArrayList<ArrayList<Entity>> alList = new ArrayList<ArrayList<Entity>>();
        for (int i = 0; i <= maxDepth; i++) {
            alList.add(new ArrayList<Entity>());
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
