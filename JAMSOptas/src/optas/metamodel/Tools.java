/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.metamodel;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class Tools {
    static public class Range{
        public double lowerBound;
        public double upperBound;

        public Range(double a,double b){
            lowerBound = a;
            upperBound = b;
        }

        @Override
        public String toString(){
            return lowerBound + "\t" + upperBound;
        }
    }
            
    static public ArrayList<Element> getNodeByType(Node root, String name){
        ArrayList<Element> list = new ArrayList<Element>();
        if (root.getNodeName().equals(name)){
            list.add((Element)root);
            return list;
        }
        
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            list.addAll(getNodeByType(childs.item(i), name));
        }
        return list;
    }
    
    static public Node getModelNode(Node root){
        ArrayList<Element> list = getNodeByType(root,"model");
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    static public ArrayList<Element> getNodeByAttribute(Node root, String attribute, String key){
        ArrayList<Element> set = new ArrayList<Element>();

        if (root.getNodeName().equals("contextcomponent")){
            Element elem = (Element)root;
            if (elem.getAttribute(attribute).equals(key))
                set.add(elem);
        }
        if (root.getNodeName().equals("component")){
            Element elem = (Element)root;
            if (elem.getAttribute(attribute).equals(key))
                set.add(elem);
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            set.addAll(getNodeByAttribute(childs.item(i),attribute, key));
        }
        return set;
    }

    static public ArrayList<Element> getVariable(Node root, String name) {
        ArrayList<Element> set = new ArrayList<Element>();

        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeName().equals("var")) {
                Element elem = (Element) node;
                if (elem.getAttribute("name").equals(name)) {
                    set.add(elem);
                }
            }
        }
        return set;
    }

    static public ArrayList<Element> getNodeByAttributeContent(Node root, String attribute, String key){
        ArrayList<Element> set = new ArrayList<Element>();

        if (root.getNodeName().equals("contextcomponent")){
            Element elem = (Element)root;
            if (elem.getAttribute(attribute).contains(key))
                set.add(elem);
        }
        if (root.getNodeName().equals("component")){
            Element elem = (Element)root;
            if (elem.getAttribute(attribute).contains(key))
                set.add(elem);
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            set.addAll(getNodeByAttribute(childs.item(i),attribute, key));
        }
        return set;
    }

    static public ArrayList<Element> getNodeByName(Node root, String key){
        return getNodeByAttribute(root,"name",key);
    }

    static public ArrayList<Element> getNodeByClass(Node root, String clazz){
        return getNodeByAttribute(root,"class",clazz);
    }
    
    static public String getTypeFromNodeName(Node root,String name){
        if (root.getNodeName().equals("contextcomponent") || root.getNodeName().equals("model")){
            Element elem = (Element)root;
            if (elem.getAttribute("name").equals(name))
                return "jams.model.contextcomponent";
        }
        if (root.getNodeName().equals("component")){
            Element elem = (Element)root;
            if (elem.getAttribute("name").equals(name))
                return "jams.model.component";
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            String value = getTypeFromNodeName(childs.item(i),name);
            if (value != null)
                return value;
        }
        return null;
    }
    
    static public boolean isAttribute(Node root,String name){
        if (root.getNodeName().equals("context")){
            Element elem = (Element)root;
            if (elem.getAttribute("name").equals(name))
                return true;
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            if (isAttribute(childs.item(i),name))
                return true;
        }
        return false;
    }
    
    public static void replaceAttribute(Node root,AttributeWrapper attribute,String newAttributeName,String newAttributeContext) throws ModelModifier.WizardException{
        NodeList childs = root.getChildNodes();
        ArrayList<Node> nodesToRemove = new ArrayList<Node>();
        
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("model") || node.getNodeName().equals("contextcomponent")){
                replaceAttribute(node, attribute, newAttributeName, newAttributeContext);                
            }
            if (node.getNodeName().equals("component")){
                replaceAttribute(node, attribute, newAttributeName, newAttributeContext);
            }
            
            //something like that 
            //(a)<var name="pidw" value="2.0"/>
            //(b)<var attribute="x" context="HRUInit" name="entityX"/>
            if (node.getNodeName().equals("var")){
                Element varNode = (Element)node;
                //case (b)
                if (attribute.getAttributeName() != null){
                    String node_attr = varNode.getAttribute("attribute");
                    String node_context = varNode.getAttribute("context");
                    if (node_attr == null)
                        continue;
                    if (node_context == null)
                        node_context = ((Element)root.getParentNode()).getAttribute("name");
                    if (node_attr.equals(attribute.getAttributeName()) && node_context.equals(attribute.getContextName())){
                        varNode.setAttribute("attribute", newAttributeName);
                        varNode.setAttribute("context", newAttributeContext);
                    }
                //case (a)
                }else{
                    String node_attr = varNode.getAttribute("name");                                        
                    String node_component = ((Element)varNode.getParentNode()).getAttribute("name");
                    if (node_component == null)
                        continue;
                    if (node_attr.equals(attribute.getVariableName()) && node_component.equals(attribute.getComponentName())){
                        varNode.setAttribute("attribute", newAttributeName);
                        varNode.setAttribute("context", newAttributeContext);
                        varNode.removeAttribute("value");                        
                    }
                }
            }
            if (node.getNodeName().equals("attribute")){
                Element varNode = (Element)node;
                if (attribute.getAttributeName() != null){
                    String node_attr = varNode.getAttribute("name");
                    String node_context = ((Element)root).getAttribute("name");
                                            
                    if (node_attr.equals(attribute.getAttributeName()) && node_context.equals(attribute.getContextName())){
                        //remove broken links                        
                        nodesToRemove.add(node);
                    }
                }                
            }
        }
        for (int i=0;i<nodesToRemove.size();i++){
            root.removeChild(nodesToRemove.get(i));
        }
        if (root instanceof Document)
            metaModelOptimizer.removeUnlinkedProperties(root);
        else
            metaModelOptimizer.removeUnlinkedProperties(root.getOwnerDocument());
    }
                        
    public static void addAttribute(Element parent,String name,String value,String context,boolean isValue){
        Element newElement = parent.getOwnerDocument().createElement("var");
        newElement.setAttribute("name", name);
        if (isValue)
            newElement.setAttribute("value", value);
        else
            newElement.setAttribute("attribute", value);
        if (context != null)
            newElement.setAttribute("context", context);
        
        if (parent.getFirstChild() != null)
            parent.insertBefore(newElement, parent.getFirstChild());       
        else
            parent.appendChild(newElement);
    }
            
    final static String fileVarList[] = {"reachFileName","hruFileName","luFileName","stFileName","gwFileName","shapeFileName","shapeFileName1",
    "stylesFileName","shapeFileName1","heightMap","luFileName","entityFileName","stFileName","gwFileName"};

    static void doAdjustments(Node root){
        if (root.getNodeName().equals("var") || root.getNodeName().equals("attribute")) {
            Element elem = (Element) root;
            if (elem.hasAttribute("name") && elem.hasAttribute("value")){
                String name  = elem.getAttribute("name");
                String value = elem.getAttribute("value");

                if (name.equals("data_caching")){
                    elem.setAttribute("value", "2");
                }

                for (int i=0;i<fileVarList.length;i++){
                    if (fileVarList[i].equals(name)){
                        elem.setAttribute("value", value.replace("\\", "/"));
                    }
                }
            }
        }

        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            doAdjustments(childs.item(i));
        }
    }

    static boolean changeWorkspace(Node root, String newWorkspace) {
        if (root.getNodeName().equals("var")) {
            Element elem = (Element) root;
            if (elem.hasAttribute("name")) {
                String varName = elem.getAttribute("name");
                if (varName.equals("workspaceDirectory")) {
                    elem.setAttribute("value", newWorkspace);
                    return true;
                }
            }
        }
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            if (changeWorkspace(childs.item(i), newWorkspace)) {
                return true;
            }
        }
        return false;
    }

    static String getWorkspace(Node root) {
        if (root.getNodeName().equals("var")) {
            Element elem = (Element) root;
            if (elem.hasAttribute("name")) {
                String varName = elem.getAttribute("name");
                if (varName.equals("workspaceDirectory")) {
                    return elem.getAttribute("value");
                }
            }
        }
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            String value = getWorkspace(childs.item(i));
            if (value!=null) {
                return value;
            }
        }
        return null;
    }
}
