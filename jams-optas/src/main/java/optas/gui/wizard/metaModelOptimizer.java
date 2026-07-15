/*
 * metaModelOptimizer.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package optas.gui.wizard;

import jams.data.Attribute;
import jams.tools.JAMSTools;
import jams.model.Component;
import jams.model.Context;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import jams.model.Model;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class metaModelOptimizer {

    static public class AttributeReadWriteSet {

        String contextName;
        HashMap<String, Set<String>> attrWritingComponents;
        HashMap<String, Set<String>> attrReadingComponents;

        AttributeReadWriteSet() {
            attrWritingComponents = new HashMap<String, Set<String>>();
            attrReadingComponents = new HashMap<String, Set<String>>();
        }
    };

    static private Hashtable<String, AttributeReadWriteSet> mergeTables(Hashtable<String, AttributeReadWriteSet> table,
            Hashtable<String, AttributeReadWriteSet> subTable) {
        Iterator<String> iter = subTable.keySet().iterator();
        //merge subtable with table
        while (iter.hasNext()) {
            String context = iter.next();
            AttributeReadWriteSet set = subTable.get(context);
            AttributeReadWriteSet parent_set = table.get(context);
            if (parent_set == null) {
                table.put(context, set);
            } else {
                Iterator<String> iterReadKey = set.attrReadingComponents.keySet().iterator();
                while (iterReadKey.hasNext()) {
                    String key = iterReadKey.next();
                    Set<String> set1 = set.attrReadingComponents.get(key);
                    if (parent_set.attrReadingComponents.get(key) != null) {
                        parent_set.attrReadingComponents.get(key).addAll(set1);
                    } else {
                        parent_set.attrReadingComponents.put(key, set1);
                    }
                }
                Iterator<String> iterWriteKey = set.attrWritingComponents.keySet().iterator();
                while (iterWriteKey.hasNext()) {
                    String key = iterWriteKey.next();
                    Set<String> set1 = set.attrWritingComponents.get(key);
                    if (parent_set.attrWritingComponents.get(key) != null) {
                        parent_set.attrWritingComponents.get(key).addAll(set1);
                    } else {
                        parent_set.attrWritingComponents.put(key, set1);
                    }
                }
            }
        }
        return table;
    }
    //it is possible, that two contexts with different name are working on the
    //same data set iff the entity ids are equal
    //in this case the read/write sets have to be unified
    static private Hashtable<String, AttributeReadWriteSet> unifyContexts(Hashtable<String, AttributeReadWriteSet> table, Hashtable<String, String> contextEntityAttributes) {
        Hashtable<String, AttributeReadWriteSet> mergedCDGs = new Hashtable<String, AttributeReadWriteSet>();

        Iterator<String> iter = table.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            AttributeReadWriteSet e = table.get(key);

            Iterator<String> iter2 = mergedCDGs.keySet().iterator();
            boolean EntityAdded = false;
            while (iter2.hasNext()) {
                AttributeReadWriteSet e2 = mergedCDGs.get(iter2.next());

                if (contextEntityAttributes.get(e.contextName) != null && contextEntityAttributes.get(e2.contextName) != null) {
                    if (contextEntityAttributes.get(e.contextName).equals(contextEntityAttributes.get(e2.contextName))) {
                        Iterator<String> iterReadKey = e.attrReadingComponents.keySet().iterator();
                        while (iterReadKey.hasNext()) {
                            String key1 = iterReadKey.next();
                            Set<String> set1 = e.attrReadingComponents.get(key1);
                            if (e2.attrReadingComponents.get(key1) != null) {
                                e2.attrReadingComponents.get(key1).addAll(set1);
                            } else {
                                e2.attrReadingComponents.put(key1, set1);
                            }
                        }
                        Iterator<String> iterWriteKey = e.attrWritingComponents.keySet().iterator();
                        while (iterWriteKey.hasNext()) {
                            String key1 = iterWriteKey.next();
                            Set<String> set1 = e.attrWritingComponents.get(key1);
                            if (e2.attrWritingComponents.get(key1) != null) {
                                e2.attrWritingComponents.get(key1).addAll(set1);
                            } else {
                                e2.attrWritingComponents.put(key1, set1);
                            }
                        }
                        EntityAdded = true;
                    }
                }
            }
            if (!EntityAdded) {
                mergedCDGs.put(key, e);
            }
        }
        return mergedCDGs;
    }

    static public Set<String> CollectAttributeWritingComponents(Node root, Model model, String attribute, String context) {
        if (context == null || attribute == null) {
            System.err.println("error: no context or attribute for:" + context + "." + attribute);
            return null;
        }
        Hashtable<String, String> contextEntityAttributes = new Hashtable<String, String>();
        Hashtable<String, AttributeReadWriteSet> result = getAttributeReadWriteSet(root, model, model.getName(), contextEntityAttributes);
        if (result == null) {
            System.err.println("error: could not get attribute read write set!");
            return null;
        }
        AttributeReadWriteSet attrRwSet = result.get(context);
        if (attrRwSet == null) {
            System.err.println("error: no rw set for context: " + context + "and attribute:" + attribute);
            return null;
        }
        return result.get(context).attrWritingComponents.get(attribute);
    }
    //this function return an attribute r/w set for each context
    //the r/w set contains a list for every attribute which components are reading and writing that attr.
    static public Hashtable<String, AttributeReadWriteSet> getAttributeReadWriteSet(Node root, Component parent, String currentContext, Hashtable<String, String> contextEntityAttributes) {
        NodeList childs = root.getChildNodes();

        Hashtable<String, AttributeReadWriteSet> table = new Hashtable<String, AttributeReadWriteSet>();
        //process each child
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            //child ist context
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("model")) {
                Element elem = (Element) node;
                String name = elem.getAttribute("name");
                Component comp = ((Context) parent).getComponent(name);
                //recursive procedure
                Hashtable<String, AttributeReadWriteSet> subTable = getAttributeReadWriteSet(node, comp, name, contextEntityAttributes);
                table = mergeTables(subTable, table);
            }
            if (node.getNodeName().equals("component")) {
                Element elem = (Element) node;
                String name = elem.getAttribute("name");
                Component comp = ((Context) parent).getComponent(name);
                Hashtable<String, AttributeReadWriteSet> subTable = getAttributeReadWriteSet(node, comp, currentContext, contextEntityAttributes);
                table = mergeTables(subTable, table);
            }
            if (node.getNodeName().equals("var")) {
                Element elem = (Element) node;
                String name = elem.getAttribute("name");
                String attr = elem.getAttribute("attribute");
                String context = null;
                if (!attr.equals("")) {
                    context = elem.getAttribute("context");
                    if (context.equals("")) {
                        context = currentContext;
                    }
                    if (parent instanceof JAMSContext) {
                        contextEntityAttributes.put(currentContext, attr);
                    }
                    Field f = null;
                    try {
                        f = JAMSTools.getField(parent.getClass(), name);
                        AttributeReadWriteSet attrRWSet = table.get(context);
                        if (attrRWSet == null) {
                            attrRWSet = new AttributeReadWriteSet();
                            table.put(context, attrRWSet);
                            attrRWSet.contextName = context;
                        }
                        //handle multiple attributes in array
                        StringTokenizer tok = new StringTokenizer(attr, ";");
                        while (tok.hasMoreTokens()) {
                            String singleAttr = tok.nextToken();

                            if (Attribute.Entity.class.isAssignableFrom(f.getType()) ||
                                    Attribute.EntityCollection.class.isAssignableFrom(f.getType())){
                                if (attrRWSet.attrReadingComponents.get(singleAttr) != null) {
                                    attrRWSet.attrReadingComponents.get(singleAttr).add(parent.getInstanceName());
                                } else {
                                    Set<String> attrSet = new TreeSet<String>();
                                    attrSet.add(parent.getInstanceName());
                                    attrRWSet.attrReadingComponents.put(singleAttr, attrSet);
                                }
                                if (attrRWSet.attrWritingComponents.get(singleAttr) != null) {
                                    attrRWSet.attrWritingComponents.get(singleAttr).add(parent.getInstanceName());
                                } else {
                                    Set<String> attrSet = new TreeSet<String>();
                                    attrSet.add(parent.getInstanceName());
                                    attrRWSet.attrWritingComponents.put(singleAttr, attrSet);
                                }
                            }else if(f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READ ||
                                    f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READWRITE) {
                                if (attrRWSet.attrReadingComponents.get(singleAttr) != null) {
                                    attrRWSet.attrReadingComponents.get(singleAttr).add(parent.getInstanceName());
                                } else {
                                    Set<String> attrSet = new TreeSet<String>();
                                    attrSet.add(parent.getInstanceName());
                                    attrRWSet.attrReadingComponents.put(singleAttr, attrSet);
                                }
                            }
                            if (f.getAnnotation(JAMSVarDescription.class).access() == AccessType.WRITE ||
                                    f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READWRITE ||
                                    Attribute.Entity.class.isAssignableFrom(f.getType()) ||
                                    Attribute.EntityCollection.class.isAssignableFrom(f.getType())) {
                                if (attrRWSet.attrWritingComponents.get(singleAttr) != null) {
                                    attrRWSet.attrWritingComponents.get(singleAttr).add(parent.getInstanceName());
                                } else {
                                    Set<String> attrSet = new TreeSet<String>();
                                    attrSet.add(parent.getInstanceName());
                                    attrRWSet.attrWritingComponents.put(singleAttr, attrSet);
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.out.println(e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }
        return table;
    }

    static public void ExportGDLFile(Hashtable<String, Set<String>> edges, Set<String> removedNodes, String fileName) {
        if (removedNodes == null) {
            removedNodes = new TreeSet<String>();
        //collect node
        }
        Set<String> nodes = new TreeSet<String>();
        Iterator<String> keyIter = edges.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            nodes.add(key);
            Set<String> endNodes = edges.get(key);
            nodes.addAll(endNodes);
        }
        //add nodes to file
        String GDLContext = new String();
        GDLContext = "graph: {layoutalgorithm : forcedir\n"+
                     "colorentry 32: 219 44 44\n"+
                     "finetuning: yes\n"+
                     "orientation: left_to_right\n"+
                     "colorentry 1: 210 218 255\n"+
                     "node.color      : 1\n"+
                     "node.textcolor  : black\n"+
                     "smanhattanedges: yes\n"+
                     "arrowmode:free\n"+
                     "node.fontname   : \"Times New Roman\"\n"+
                     "node.borderwidth: 1\n"+
                     "edge.thickness  : 1\n"+
                     "edge.color      : black\n"+
                     "edge.arrowstyle : solid\n"+
                     "edge.arrowsize :  10\n"+
                     "arrowmode       : free\n"+
                     "portsharing     : yes\n"+
                     "node.shape: ellipse\n"+
                     "attraction   : 80\n"+
                     "repulsion    : 1000\n"+
                     "energetic : yes\n"+
                     "energetic overlapping : 10000\n"+
                     "energetic border : 0\n"+
                     "energetic attraction: 0\n"+
                     "energetic crossing:0\n"+
                     "energetic repulsion:0\n"+
                     "energetic gravity:0\n"+
                     "gravity      : 10.0\n"+
                     "fdmax        : 50\n"+
                     "tempmax      : 254\n"+
                     "temptreshold : 3\n"+
                     "tempscheme   : 2\n"+
                     "tempfactor   : 1.08\n"+
                     "randomfactor : 100\n";
        
        Iterator<String> nodeIter = nodes.iterator();
        while (nodeIter.hasNext()) {
            String name = nodeIter.next();
            if (removedNodes.contains(name)) {
                GDLContext += "node: {color:red title: \"" + name + "\"}\n";
            } else {
                GDLContext += "node: {title: \"" + name + "\"}\n";
            }
        }
        //add edges
        Iterator<String> sourceIter = edges.keySet().iterator();
        while (sourceIter.hasNext()) {
            String key = sourceIter.next();
            Iterator<String> endNodesIter = edges.get(key).iterator();
            while (endNodesIter.hasNext()) {
                String dest = endNodesIter.next();
                GDLContext += "edge: { source: \"" + key + "\" target:\"" + dest + "\" }";
            }
        }
        GDLContext += "}";

        try {
            BufferedWriter GDLFileWriter = new BufferedWriter(new FileWriter(fileName));
            GDLFileWriter.write(GDLContext);
            GDLFileWriter.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static public Hashtable<String, Set<String>> getDependencyGraph(Node root, Model model) {
        Hashtable<String, Set<String>> edges = new Hashtable<String, Set<String>>();
        Hashtable<String, String> contextEntityAttributes = new Hashtable<String, String>();
        //for each independed context, get read/write access components
        //and create an edge from read to write component!               
        Hashtable<String, AttributeReadWriteSet> CAttrRWSet = unifyContexts(getAttributeReadWriteSet(root, model, model.getInstanceName(), contextEntityAttributes), contextEntityAttributes);

        Map<String, Set<String>> writeAccessComponents = null;
        Map<String, Set<String>> readAccessComponents = null;

        //process all independent contexts
        Iterator<String> iter = CAttrRWSet.keySet().iterator();
        while (iter.hasNext()) {
            AttributeReadWriteSet e = CAttrRWSet.get(iter.next());
            writeAccessComponents = e.attrWritingComponents;
            readAccessComponents = e.attrReadingComponents;
            Iterator<String> writeAccessKeysIter = writeAccessComponents.keySet().iterator();

            while (writeAccessKeysIter.hasNext()) {
                String attr = writeAccessKeysIter.next();
                //all components which have write/read access to this attrib
                Set<String> writeAccess = writeAccessComponents.get(attr);
                Set<String> readAccess = readAccessComponents.get(attr);

                if (readAccess == null || writeAccess == null) {
                    continue;
                }
                Iterator<String> writerIterator = writeAccess.iterator();
                //iterate through write access components
                while (writerIterator.hasNext()) {
                    String writeAccessComponent = writerIterator.next();
                    //iterate through read access components
                    Iterator<String> readerIterator = readAccess.iterator();
                    while (readerIterator.hasNext()) {
                        String readAccessComponent = readerIterator.next();
                        //add edge from read to write access component
                        if (edges.containsKey(readAccessComponent)) {
                            edges.get(readAccessComponent).add(writeAccessComponent);
                        } else {
                            Set<String> list = new TreeSet<String>();
                            list.add(writeAccessComponent);
                            edges.put(readAccessComponent, list);
                        }
                    }
                }
            }
        }
        //ExportGDLFile(edges,"C:\\Arbeit\\gehlberg.gdl");
        return edges;
    }

    @SuppressWarnings("unchecked")
    public static Hashtable<String, Set<String>> TransitiveClosure(Hashtable<String, Set<String>> graph) {
        Hashtable<String, Set<String>> TransitiveClosure = new Hashtable();

        Set<String> keys = graph.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Set<String> value = graph.get(key);
            Set<String> set = new TreeSet<String>();
            set.addAll(value);
            TransitiveClosure.put(key, set);
        }
        //find structures like a <-- b <-- c            
        boolean change = true;
        while (change) {
            change = false;
            Set<String> aSet = TransitiveClosure.keySet();
            Iterator<String> a_iter = keys.iterator();
            while (a_iter.hasNext()) {
                String a = a_iter.next();
                Set<String> bSet = TransitiveClosure.get(a);
                if (bSet == null) {
                    continue;
                }
                Iterator<String> b_iter = bSet.iterator();
                Set<String> modification = new TreeSet();
                while (b_iter.hasNext()) {
                    String b = b_iter.next();
                    Set<String> cSet = TransitiveClosure.get(b);
                    if (cSet == null) {
                        continue;
                    }
                    Iterator<String> c_iter = cSet.iterator();
                    while (c_iter.hasNext()) {
                        String c = c_iter.next();
                        if (!bSet.contains(c)) {
                            modification.add(c);
                            change = true;
                        }
                    }
                }
                bSet.addAll(modification);
            }
        }
        return TransitiveClosure;
    }

    static public Node getNodeByName(Node root, String name){
        if (root.getNodeName().equals(name)){
            return root;
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node model = getNodeByName(childs.item(i), name);
            if (model != null)
                return model;
        }
        return null;
    }

    public static ArrayList<String> RemoveGUIComponents(Node root) throws OPTASWizardException{
        ArrayList<String> removedComponents = new ArrayList<String>();

        NodeList childs = root.getChildNodes();
        Node mainRoot = root.getOwnerDocument();

        ArrayList<Node> childsToRemove = new ArrayList<Node>();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent")) {
                removedComponents.addAll(RemoveGUIComponents(node));
            } else if (node.getNodeName().equals("component")) {
                Element comp = (Element) node;
                if (comp.getAttribute("class").contains("jams.components.gui")) {
                    childsToRemove.add(node);
                    //RemoveProperty(mainRoot, null, comp.getAttribute("name"));
                }
            }
        }

        for (int i = 0; i < childsToRemove.size(); i++) {
            Element elem = (Element) childsToRemove.get(i);
            removedComponents.add(elem.getAttribute("name"));
            root.removeChild(childsToRemove.get(i));
        }
        if (getNodeByName(mainRoot,"launcher")!=null)
            removeUnlinkedProperties(root);
        //delete empty contexts
        removedComponents.addAll(RemoveEmptyContextes(root));

        return removedComponents;
    }

    static public ArrayList<String> RemoveEmptyContextes(Node root) {
        NodeList childs = root.getChildNodes();
        ArrayList<String> removedNodes = new ArrayList<String>();
        ArrayList<Node> childsToRemove = new ArrayList<Node>();

        //find empty contextes
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("subgroup")) {
                NodeList subChilds = node.getChildNodes();
                boolean isEmpty = true;
                for (int subIndex = 0; subIndex < subChilds.getLength(); subIndex++) {
                    if (subChilds.item(subIndex).getNodeName().equals("property")) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    childsToRemove.add(node);
                }
            }
            if (node.getNodeName().equals("group")) {
                NodeList subChilds = node.getChildNodes();
                boolean isEmpty = true;
                for (int subIndex = 0; subIndex < subChilds.getLength(); subIndex++) {
                    if (subChilds.item(subIndex).getNodeName().equals("subgroup") ||
                            subChilds.item(subIndex).getNodeName().equals("property")) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    childsToRemove.add(node);
                }
            }
            if (node.getNodeName().equals("contextcomponent")) {
                NodeList subChilds = node.getChildNodes();
                boolean isEmpty = true;
                for (int subIndex = 0; subIndex < subChilds.getLength(); subIndex++) {
                    if (subChilds.item(subIndex).getNodeName().equals("component") || subChilds.item(subIndex).getNodeName().equals("contextcomponent")) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    childsToRemove.add(node);
                }                
            }
            removedNodes.addAll(RemoveEmptyContextes(node));
        }
        //remove childs on list
        for (int i = 0; i < childsToRemove.size(); i++) {
            Element elem = (Element) childsToRemove.get(i);
            removedNodes.add(elem.getAttribute("name"));
            root.removeChild(elem);
        }
        //recursive iteration until no change occurs
        if (removedNodes.size() > 0) {
            removedNodes.addAll(RemoveEmptyContextes(root));
        //list of deleted nodes
        }
        return removedNodes;
    }

    static public ArrayList<String> RemoveNotListedComponents(Node root, Set<String> list) throws OPTASWizardException{
        NodeList childs = root.getChildNodes();

        ArrayList<Node> childsToRemove = new ArrayList<Node>();
        ArrayList<String> removedNodes = new ArrayList<String>();

        Node mainRoot = root.getOwnerDocument();

        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent")) {
                removedNodes.addAll(RemoveNotListedComponents(node, list));
            } else if (node.getNodeName().equals("component")) {
                Element comp = (Element) node;
                if (!list.contains(comp.getAttribute("name"))) {
                    //RemoveProperty(mainRoot, null, comp.getAttribute("name"));
                    childsToRemove.add(node);
                }
            }
        }

        //remove childs on list
        for (int i = 0; i < childsToRemove.size(); i++) {
            Element elem = (Element) childsToRemove.get(i);
            removedNodes.add(elem.getAttribute("name"));
            root.removeChild(childsToRemove.get(i));
        }
        if (getNodeByName(mainRoot,"launcher")!=null)
            removeUnlinkedProperties(root);
        //delete empty contexts
        removedNodes.addAll(RemoveEmptyContextes(root));

        return removedNodes;
    }

     static public Node getNode(Node root, String owner){
        if (root.getNodeName().equals("context")){
            Element elem = (Element)root;
            if (elem.getAttribute("name").equals(owner))
                return root;
        }
        if (root.getNodeName().equals("component")){
            Element elem = (Element)root;
            if (elem.getAttribute("name").equals(owner))
                return root;
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node value = getNode(childs.item(i),owner);
            if (value != null)
                return value;
        }
        return null;
    }

    public static void removeUnlinkedProperties(Node root) throws OPTASWizardException {
        Element launcherNode = (Element)getNodeByName(root,"launcher");
        if (launcherNode == null){
            throw new OPTASWizardException("model does not contain launcher node!");
        }
        ArrayList<Node> nodesToRemove = new ArrayList<Node>();

        Node node;

        NodeList groupNodes = launcherNode.getElementsByTagName("group");
        for (int gindex = 0; gindex < groupNodes.getLength(); gindex++) {
            node = groupNodes.item(gindex);
            Element groupElement = (Element) node;
            // @todo subgroups and properties recursive
            NodeList groupChildNodes = groupElement.getChildNodes();
            for (int pindex = 0; pindex < groupChildNodes.getLength(); pindex++) {
                node = groupChildNodes.item(pindex);
                if (node.getNodeName().equalsIgnoreCase("property")) {
                    Element propertyElement = (Element) node;
                    String comp = propertyElement.getAttribute("component");
                    if (getNode(root, comp)==null){
                        nodesToRemove.add(propertyElement);
                    }
                }
                if (node.getNodeName().equalsIgnoreCase("subgroup")) {
                    Element subgroupElement = (Element) node;
                    NodeList propertyNodes = subgroupElement.getElementsByTagName("property");
                    for (int kindex = 0; kindex < propertyNodes.getLength(); kindex++) {
                        Element propertyElement = (Element) propertyNodes.item(kindex);
                        String comp = propertyElement.getAttribute("component");
                        if (getNode(root, comp)==null){
                            nodesToRemove.add(propertyElement);
                        }
                    }
                }
            }
        }
        for (Node p : nodesToRemove)
            p.getParentNode().removeChild(p);
        return;
    }
    
    @SuppressWarnings("unchecked")
    public static Set<String> GetRelevantComponentsList(Hashtable<String, Set<String>> dependencyGraph, Set<String> EffWritingComponentsList) {
        Set<String> compList = new TreeSet();
        Iterator<String> iter = EffWritingComponentsList.iterator();
        while (iter.hasNext()) {
            String wr_comp = iter.next();
            Set<String> set = dependencyGraph.get(wr_comp);
            if (set != null) {
                compList.addAll(set);                
            }
            compList.add(wr_comp);
        }
        return compList;
    }
}
