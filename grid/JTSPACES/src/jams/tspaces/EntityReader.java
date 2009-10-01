package jams.tspaces;

import org.unijena.j2k.*;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class EntityReader extends JAMSComponent {   
	
	
            public  JAMSString dirName;
            public  JAMSString hruFileName;
            public  JAMSString reachFileName;
            public  JAMSEntityCollection hrus;
            public  JAMSEntityCollection reaches;
    
            public EntityReader(String dir, String hru, String reach){
            	dirName=new JAMSString(dir);
            	hruFileName=new JAMSString(hru);
            	reachFileName=new JAMSString(reach);
            	hrus=new JAMSEntityCollection() ;
                reaches=new JAMSEntityCollection();
            }            
    
            public void init() throws JAMSEntity.NoSuchAttributeException {
                
                //read hru parameter
                //hrus = new JAMSEntityCollection();
                //hrus.setEntities(J2KFunctions.readParas(dirName.getValue() + "/" + hruFileName.getValue(), getModel()));
            	String fileName=dirName.getValue() + "/" + hruFileName.getValue();
                BufferedReader reader;
                ArrayList <JAMSEntity> entityList = new ArrayList<JAMSEntity>();
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
                        attributeNames.add(tokenizer.nextToken());
                    }
                    
                    //process lower boundaries
                    reader.readLine();
                    
                    //process upper boundaries
                    reader.readLine();
                    
                    //process units
                    reader.readLine();
                    
                    //get first line of hru data
                    s = reader.readLine();
                    
                    while ((s != null) && !s.startsWith("#"))  {
                        
                        JAMSEntity e = JAMSDataFactory.createEntity();
                        tokenizer = new StringTokenizer(s, "\t");
                        
                        String token;
                        for (int i = 0; i < attributeNames.size(); i++) {
                            token = tokenizer.nextToken();
                            try {
                                //hopefully these are double values :-)
                                e.setDouble(attributeNames.get(i), Double.parseDouble(token));
                                //model.getRuntime().println(attributeNames.get(i) + ": " + token, 4);
                            } catch (NumberFormatException nfe) {
                                //most probably this happens because of string values within J2K parameter files
                                e.setObject(attributeNames.get(i), token);
                            }
                        }
                        
                        entityList.add(e);
                        s = reader.readLine();
                    }
                    
                
                hrus.setEntities(entityList);
                
                
                
                //read reach parameter
                //reaches = new JAMSEntityCollection();
                //reaches.setEntities(J2KFunctions.readParas(dirName.getValue() + "/" + reachFileName.getValue(), getModel()));
            	fileName=dirName.getValue() + "/" + reachFileName.getValue();
                entityList = new ArrayList<JAMSEntity>();                 
                    reader = new BufferedReader(new FileReader(fileName));
                    
                    s = "#";
                    
                    // get rid of comments
                    while (s.startsWith("#")) {
                        s = reader.readLine();
                    }
                    
                    //put the attribure names into a vector
                    attributeNames = new Vector<String>();
                    tokenizer = new StringTokenizer(s, "\t");
                    while (tokenizer.hasMoreTokens()) {
                        attributeNames.add(tokenizer.nextToken());
                    }
                    
                    //process lower boundaries
                    reader.readLine();
                    
                    //process upper boundaries
                    reader.readLine();
                    
                    //process units
                    reader.readLine();
                    
                    //get first line of hru data
                    s = reader.readLine();
                    
                    while ((s != null) && !s.startsWith("#"))  {
                        
                        JAMSEntity e = JAMSDataFactory.createEntity();
                        tokenizer = new StringTokenizer(s, "\t");
                        
                        String token;
                        for (int i = 0; i < attributeNames.size(); i++) {
                            token = tokenizer.nextToken();
                            try {
                                //hopefully these are double values :-)
                                e.setDouble(attributeNames.get(i), Double.parseDouble(token));
                                //model.getRuntime().println(attributeNames.get(i) + ": " + token, 4);
                            } catch (NumberFormatException nfe) {
                                //most probably this happens because of string values within J2K parameter files
                                e.setObject(attributeNames.get(i), token);
                            }
                        }
                        
                        entityList.add(e);
                        s = reader.readLine();
                    }
                
                reaches.setEntities(entityList);
                } catch (IOException ioe) {                    
                }
                
                
                
                //create object associations from id attributes for hrus and reaches
                createTopology();
                
                //create total order on hrus and reaches that allows processing them subsequently
                //getModel().getRuntime().println("Create ordered hru-list", JAMS.VERBOSE);
                createOrderedList(hrus, "to_poly");
                //getModel().getRuntime().println("Create ordered reach-list", JAMS.VERBOSE);
                createOrderedList(reaches, "to_reach");
                //getModel().getRuntime().println("Entities read successfull!", JAMS.VERBOSE);
                
            }
            
            //do depth first search to find cycles
            protected boolean cycleCheck(JAMSEntity node,Stack<JAMSEntity> searchStack,HashSet<JAMSDouble> closedList,HashSet<JAMSDouble> visitedList) throws JAMSEntity.NoSuchAttributeException {
                JAMSEntity child_node;
                
                //current node allready in search stack -> circle found
                if ( searchStack.indexOf(node) != -1) {
                    int index = searchStack.indexOf(node);
                    
                    String cyc_output = new String();
                    for (int i = index; i < searchStack.size(); i++) {
                        cyc_output += ((JAMSEntity)searchStack.get(i)).getDouble("ID") + " ";
                    }
                    getModel().getRuntime().println("Found circle with ids:" + cyc_output);
                    
                    return true;
                }
                //node in closed list? -> then skip it
                if (closedList.contains(node.getObject("ID")) == true)
                    return false;
                //now this node is visited
                visitedList.add((JAMSDouble)node.getObject("ID"));
                
                child_node = (JAMSEntity)node.getObject("to_poly");
                
                if (child_node != null) {
                    //push current node to search stack
                    searchStack.push(node);
                    
                    boolean result = cycleCheck(child_node,searchStack,closedList,visitedList);
                    
                    searchStack.pop();
                    
                    return result;
                }
                return false;
            }
            
            protected boolean cycleCheck() throws JAMSEntity.NoSuchAttributeException {
                Iterator<JAMSEntity> hruIterator;
                
                HashSet<JAMSDouble> closedList = new HashSet<JAMSDouble>();
                HashSet<JAMSDouble> visitedList = new HashSet<JAMSDouble>();
                
                JAMSEntity start_node;
                
                getModel().getRuntime().println("Cycle checking...");
                
                hruIterator = hrus.getEntities().iterator();
                
                boolean result = false;
                
                while (hruIterator.hasNext()) {
                    start_node = hruIterator.next();
                    //connected component of start_node allready processed?
                    if (closedList.contains(start_node.getObject("ID")) == false) {
                        if ( cycleCheck(start_node,new Stack<JAMSEntity>(),closedList,visitedList) == true) {
                            result = true;
                        }
                        closedList.addAll(visitedList);
                        visitedList.clear();
                    }
                    
                }
                return result;
            }
            
            protected void createTopology() throws JAMSEntity.NoSuchAttributeException {
                
                HashMap<Double, JAMSEntity> hruMap = new HashMap<Double, JAMSEntity>();
                HashMap<Double, JAMSEntity> reachMap = new HashMap<Double, JAMSEntity>();
                Iterator<JAMSEntity> hruIterator;
                Iterator<JAMSEntity> reachIterator;
                JAMSEntity e;
                
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
                    e.setObject("to_reach", reachMap.get(e.getDouble("to_reach")));
                }
                
                //check for cycles
                /*if (this.getModel().getRuntime().getDebugLevel() >= JAMS.VVERBOSE) {
                    if (cycleCheck() == true)
                        getModel().getRuntime().println("HRUs --> cycle found ... :( ");
                    else
                        getModel().getRuntime().println("HRUs --> no cycle found");
                }*/
                
            }
            
            protected void createOrderedList(JAMSEntityCollection col, String asso) throws JAMSEntity.NoSuchAttributeException {
                
                Iterator<JAMSEntity> hruIterator;
                JAMSEntity e, f;
                ArrayList<JAMSEntity> newList = new ArrayList<JAMSEntity>();
                HashMap<JAMSEntity, Integer> depthMap = new HashMap<JAMSEntity, Integer>();
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
                        
                        f = (JAMSEntity) e.getObject(asso);
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
                ArrayList<ArrayList<JAMSEntity>> alList = new ArrayList<ArrayList<JAMSEntity>>();
                for (int i=0; i<=maxDepth; i++) {
                    alList.add(new ArrayList<JAMSEntity>());
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
