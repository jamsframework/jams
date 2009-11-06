/*
 * metaModelOptimizer.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package jams.model.metaoptimizer;

import jams.tools.JAMSTools;
import jams.model.Component;
import jams.model.Context;
import jams.model.JAMSModel;
import jams.model.JAMSSpatialContext;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class metaModelOptimizer {
    static public class AttributeReadWriteSet{
        String contextName;
                
        HashMap<String,Set<String>> attrWritingComponents;
        HashMap<String,Set<String>> attrReadingComponents;
        
        AttributeReadWriteSet(){
            attrWritingComponents = new  HashMap<String,Set<String>>();
            attrReadingComponents = new  HashMap<String,Set<String>>();
        }
    };
    
    static private Hashtable<String,AttributeReadWriteSet> mergeTables(Hashtable<String,AttributeReadWriteSet> table,
                                                            Hashtable<String,AttributeReadWriteSet> subTable){
        Iterator<String> iter = subTable.keySet().iterator();
        //merge subtable with table
        while(iter.hasNext()){
            String context = iter.next();
            AttributeReadWriteSet set = subTable.get(context);
            AttributeReadWriteSet parent_set = table.get(context);
            if (parent_set == null){
                table.put(context, set);
            }else{                        
                Iterator<String> iterReadKey = set.attrReadingComponents.keySet().iterator();
                while(iterReadKey.hasNext()){
                    String key = iterReadKey.next();
                    Set<String> set1 = set.attrReadingComponents.get(key);
                    if (parent_set.attrReadingComponents.get(key) != null){
                        parent_set.attrReadingComponents.get(key).addAll(set1);
                    }else{
                        parent_set.attrReadingComponents.put(key, set1);
                    }
                }
                Iterator<String> iterWriteKey = set.attrWritingComponents.keySet().iterator();
                while(iterWriteKey.hasNext()){
                    String key = iterWriteKey.next();
                    Set<String> set1 = set.attrWritingComponents.get(key);
                    if (parent_set.attrWritingComponents.get(key) != null){
                        parent_set.attrWritingComponents.get(key).addAll(set1);
                    }else{
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
    static private Hashtable<String,AttributeReadWriteSet> unifyContexts(Hashtable<String,AttributeReadWriteSet> table,Hashtable<String,String> contextEntityAttributes){        
        Hashtable<String,AttributeReadWriteSet> mergedCDGs = new Hashtable<String,AttributeReadWriteSet>();

        Iterator<String> iter = table.keySet().iterator();
        while (iter.hasNext()) {   
            String key = iter.next();
            AttributeReadWriteSet e = table.get(key);
            
            Iterator<String> iter2 = mergedCDGs.keySet().iterator();
            boolean EntityAdded = false;
            while (iter2.hasNext()) {
                AttributeReadWriteSet e2 = mergedCDGs.get(iter2.next());
                
                if (contextEntityAttributes.get(e.contextName) != null && contextEntityAttributes.get(e2.contextName) != null){
                    if (contextEntityAttributes.get(e.contextName).equals(contextEntityAttributes.get(e2.contextName))) {                                                
                        Iterator<String> iterReadKey = e.attrReadingComponents.keySet().iterator();
                        while(iterReadKey.hasNext()){
                            String key1 = iterReadKey.next();
                            Set<String> set1 = e.attrReadingComponents.get(key1);
                            if (e2.attrReadingComponents.get(key1) != null){
                                e2.attrReadingComponents.get(key1).addAll(set1);
                            }else{
                                e2.attrReadingComponents.put(key1, set1);
                            }
                        }
                        Iterator<String> iterWriteKey = e.attrWritingComponents.keySet().iterator();
                        while(iterWriteKey.hasNext()){
                            String key1 = iterWriteKey.next();
                            Set<String> set1 = e.attrWritingComponents.get(key1);
                            if (e2.attrWritingComponents.get(key1) != null){
                                e2.attrWritingComponents.get(key1).addAll(set1);
                            }else{
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
                    
    static public Set<String> CollectAttributeWritingComponents(Node root,JAMSModel model,String attribute,String context){        
        Hashtable<String,String> contextEntityAttributes = new Hashtable<String,String>();
        Hashtable<String,AttributeReadWriteSet> result = getAttributeReadWriteSet(root,model,model.getName(),contextEntityAttributes);
        
        return result.get(context).attrWritingComponents.get(attribute);
    }
    
    //this function return an attribute r/w set for each context
    //the r/w set contains a list for every attribute which components are reading and writing that attr.
    static public Hashtable<String,AttributeReadWriteSet> getAttributeReadWriteSet(Node root,Component parent,String currentContext,Hashtable<String,String> contextEntityAttributes) {                                
        NodeList childs = root.getChildNodes();
        
        Hashtable<String,AttributeReadWriteSet> table = new Hashtable<String,AttributeReadWriteSet>();        
        //process each child
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            //child ist context
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("model")) {                
                Element elem = (Element)node;
                String name = elem.getAttribute("name");
                Component comp = ((Context)parent).getComponent(name);
                //recursive procedure
                Hashtable<String,AttributeReadWriteSet> subTable = getAttributeReadWriteSet(node,comp,name,contextEntityAttributes);
                table = mergeTables(subTable,table);
            }
            if (node.getNodeName().equals("component")) {  
                Element elem = (Element)node;
                String name = elem.getAttribute("name");
                Component comp = ((Context)parent).getComponent(name);
                Hashtable<String,AttributeReadWriteSet> subTable = getAttributeReadWriteSet(node,comp,currentContext,contextEntityAttributes);
                table = mergeTables(subTable,table);                
            }
            if (node.getNodeName().equals("var")) { 
                Element elem = (Element)node;
                String name = elem.getAttribute("name");
                String attr = elem.getAttribute("attribute");
                String context = null;
                if (!attr.equals("")){
                    context = elem.getAttribute("context");
                    if (context.equals("")){
                        context = currentContext;
                    }                
                    if (parent instanceof JAMSSpatialContext){
                        contextEntityAttributes.put(currentContext, attr);                        
                    }
                    Field f = null;
                    try{
                        f = JAMSTools.getField(parent.getClass(), name);
                        AttributeReadWriteSet attrRWSet = table.get(context);
                        if (attrRWSet == null){
                            attrRWSet = new AttributeReadWriteSet();
                            table.put(context,attrRWSet);
                            attrRWSet.contextName = context;
                        }
                                                
                        if (f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READ ||
                                f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READWRITE ||
                                 f.getType().getName().equals("jams.data.JAMSEntity") || 
                                f.getType().getName().equals("jams.data.JAMSEntityCollection")){
                            if (attrRWSet.attrReadingComponents.get(attr) != null){
                                attrRWSet.attrReadingComponents.get(attr).add(parent.getInstanceName());
                            }else{
                                HashSet<String> attrSet = new HashSet<String>();
                                attrSet.add(parent.getInstanceName());
                                attrRWSet.attrReadingComponents.put(attr, attrSet);
                            }
                        }
                        if (f.getAnnotation(JAMSVarDescription.class).access() == AccessType.WRITE ||
                                f.getAnnotation(JAMSVarDescription.class).access() == AccessType.READWRITE || 
                                f.getType().getName().equals("jams.data.JAMSEntity") || 
                                f.getType().getName().equals("jams.data.JAMSEntityCollection")
                                ){
                            if (attrRWSet.attrWritingComponents.get(attr) != null){
                                attrRWSet.attrWritingComponents.get(attr).add(parent.getInstanceName());
                            }else{
                                HashSet<String> attrSet = new HashSet<String>();
                                attrSet.add(parent.getInstanceName());
                                attrRWSet.attrWritingComponents.put(attr, attrSet);
                            }
                        }                        
                    }catch(Exception e){
                        System.out.println(e.toString()); e.printStackTrace();
                    }                                
                }
            }
        }                          
        return table;     
    }
            
    static public void ExportGDLFile(Hashtable<String, HashSet<String>> edges,String fileName){
        //collect node
        HashSet<String> nodes = new HashSet<String>();
        Iterator<String> keyIter = edges.keySet().iterator();
        while(keyIter.hasNext()){
            String key = keyIter.next();
            nodes.add(key);
            HashSet<String> endNodes = edges.get(key);
            nodes.addAll(endNodes);
        }
        //add nodes to file
        String GDLContext = new String();
        GDLContext = "graph {";
        Iterator<String> nodeIter = nodes.iterator();
        while(nodeIter.hasNext()){
            GDLContext += "node: { title: \"" + nodeIter.next() + "\"}\n";
        }
        //add edges
        Iterator<String> sourceIter = edges.keySet().iterator();
        while(sourceIter.hasNext()){
            String key = sourceIter.next();
            Iterator<String> endNodesIter = edges.get(key).iterator();
            while(endNodesIter.hasNext()){
                String dest = endNodesIter.next();
                GDLContext += "edge: { source: \""+key+"\" target:\""+dest+"\" }";
            }
        }
        GDLContext += "}";
                
        try{
            BufferedWriter GDLFileWriter = new BufferedWriter(new FileWriter(fileName));
            GDLFileWriter.write(GDLContext);
            GDLFileWriter.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }        
    }
    
    static public Hashtable<String, HashSet<String>> getDependencyGraph(Node root,JAMSModel model) {
        Hashtable<String, HashSet<String>> edges = new Hashtable<String, HashSet<String>>();
        Hashtable<String,String> contextEntityAttributes = new Hashtable<String,String>();
        //for each independed context, get read/write access components
        //and create an edge from read to write component!               
        Hashtable<String,AttributeReadWriteSet> CAttrRWSet = unifyContexts(getAttributeReadWriteSet(root,model,model.getInstanceName(),contextEntityAttributes),contextEntityAttributes);
                        
        HashMap<String,Set<String>> writeAccessComponents = null;
        HashMap<String,Set<String>> readAccessComponents = null;

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
                Set<String> readAccess  = readAccessComponents.get(attr);

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
                            HashSet<String> list = new HashSet<String>();
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
    public static Hashtable<String,HashSet<String>> TransitiveClosure (Hashtable<String,HashSet<String>> graph){        
        Hashtable<String,HashSet<String>> TransitiveClosure = new Hashtable();
        
        Set<String> keys = graph.keySet();
        Iterator<String> iter = keys.iterator();
        while(iter.hasNext()){
            String key = iter.next();
            HashSet<String> value = graph.get(key);
            TransitiveClosure.put(key, (HashSet<String>)value.clone());
        }
        //find structures like a <-- b <-- c            
        boolean change = true;
        while (change){
            change = false;
            Set<String> aSet = TransitiveClosure.keySet();
            Iterator<String> a_iter = keys.iterator();
            while(a_iter.hasNext()){
                String a = a_iter.next();
                HashSet<String> bSet = TransitiveClosure.get(a);
                if (bSet == null)
                    continue;
                Iterator<String> b_iter = bSet.iterator();
                HashSet<String> modification = new HashSet();
                while(b_iter.hasNext()){
                    String b = b_iter.next();
                    HashSet<String> cSet = TransitiveClosure.get(b);
                    if (cSet == null)
                        continue;
                    Iterator<String> c_iter = cSet.iterator();
                    while (c_iter.hasNext()){
                        String c = c_iter.next();
                        if (!bSet.contains(c)){
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
    
    public static ArrayList<String> RemoveGUIComponents(Node root){
        ArrayList<String> removedComponents = new ArrayList<String>();
        
        NodeList childs = root.getChildNodes();
        Node mainRoot = root.getOwnerDocument();
        
        ArrayList<Node> childsToRemove = new ArrayList<Node>();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent")) {
                removedComponents.addAll(RemoveGUIComponents(node));
            }else if (node.getNodeName().equals("component")) {
                Element comp = (Element)node;
                if ( comp.getAttribute("class").contains("jams.components.gui") ){
                    childsToRemove.add(node);                
                    RemoveProperty(mainRoot,null,comp.getAttribute("name"));
                }
            }                
        }
                            
        for (int i=0;i<childsToRemove.size();i++){
            Element elem = (Element)childsToRemove.get(i);                                    
            removedComponents.add(elem.getAttribute("name"));            
            root.removeChild(childsToRemove.get(i));            
        }
        //delete empty contexts
        removedComponents.addAll(RemoveEmptyContextes(root));
        
        return removedComponents;        
    }
    
    static public ArrayList<String> RemoveEmptyContextes(Node root){
        NodeList childs = root.getChildNodes();
        ArrayList<String> removedNodes = new ArrayList<String>();
        ArrayList<Node> childsToRemove = new ArrayList<Node>();
        
        //find empty contextes
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent")) {
                NodeList subChilds = node.getChildNodes();
                boolean isEmpty = true;
                for (int subIndex = 0; subIndex < subChilds.getLength(); subIndex++) {
                    if (subChilds.item(subIndex).getNodeName().equals("component") || subChilds.item(subIndex).getNodeName().equals("contextcomponent")){
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty)
                    childsToRemove.add(node);                    
                
                removedNodes.addAll(RemoveEmptyContextes(node));                
            }
        }
        //remove childs on list
        for (int i=0;i<childsToRemove.size();i++){
            Element elem = (Element)childsToRemove.get(i);                                    
            removedNodes.add(elem.getAttribute("name"));            
            root.removeChild(childsToRemove.get(i));            
        }
        //recursive iteration until no change occurs
        if (removedNodes.size()>0)
            removedNodes.addAll(RemoveEmptyContextes(root));
        //list of deleted nodes
        return removedNodes;
    }
    
    static public ArrayList<String> RemoveNotListedComponents(Node root,Set<String> list){
        NodeList childs = root.getChildNodes();
        
        ArrayList<Node> childsToRemove = new ArrayList<Node>();
        ArrayList<String> removedNodes = new ArrayList<String>();
        
        Node mainRoot = root.getOwnerDocument();
        
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent")) {
                removedNodes.addAll(RemoveNotListedComponents(node,list));
            }else if (node.getNodeName().equals("component")) {
                Element comp = (Element)node;
                if ( !list.contains(comp.getAttribute("name")) ){
                    RemoveProperty(mainRoot,null,comp.getAttribute("name"));
                    childsToRemove.add(node);                       
                }
            }                
        }
        
        //remove childs on list
        for (int i=0;i<childsToRemove.size();i++){
            Element elem = (Element)childsToRemove.get(i);                                    
            removedNodes.add(elem.getAttribute("name"));
            root.removeChild(childsToRemove.get(i));            
        }
        //delete empty contexts
        removedNodes.addAll(RemoveEmptyContextes(root));
        
        return removedNodes;
    }
           
    static public void RemoveProperty(Node root,String attrName,String component){        
        ArrayList<Node> nodesToRemove = new ArrayList<Node>();
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("property")){
                Element elem = (Element)node;
                if (elem.hasAttribute("attribute") && elem.hasAttribute("component")){
                    String attr = elem.getAttribute("attribute");
                    String comp = elem.getAttribute("component");
                    if ( (attrName == null || attr.equals(attrName)) && comp.equals(component)){
                        nodesToRemove.add(node);
                    }
                }
            }else{
                RemoveProperty(node,attrName,component);
            }
        }
        for (int i=0;i<nodesToRemove.size();i++){
            root.removeChild(nodesToRemove.get(i));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Set<String> GetRelevantComponentsList(Hashtable<String,HashSet<String>> dependencyGraph,Set<String> EffWritingComponentsList){
        HashSet<String> compList = new HashSet();
        Iterator<String> iter = EffWritingComponentsList.iterator();
        while(iter.hasNext()){
            String wr_comp = iter.next();
            HashSet<String> set = dependencyGraph.get(wr_comp);
            if (set != null){
                compList.addAll(set);
                compList.add(wr_comp);
            }
        }
        return compList;
    }
    
}
