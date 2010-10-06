/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jams.JAMSProperties;
import jamsui.juice.gui.JUICEFrame;
import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class Tools {
    public static class ComponentWrapper{
        public String componentName;
        public String componentContext;
        public boolean contextComponent;
        
        public ComponentWrapper(String componentName,String componentContext,boolean contextComponent){
            this.componentContext = componentContext;
            this.componentName = componentName;
            this.contextComponent = contextComponent;
        }
        public String toString(){
            if (contextComponent)
                return componentName;
            return /*componentContext + "." + */componentName;
        }
    }
    
    public static class AttributeWrapper implements Comparable{
        public String attributeName;
        public String variableName;
        public String componentName;
        public String contextName;
        public boolean isSetByValue;
                
        public AttributeWrapper(){
            
        }
        public AttributeWrapper(String variableName,String attributeName,String componentName,String contextName){
            this.variableName = variableName;
            this.attributeName = attributeName;
            this.componentName = componentName;
            this.contextName = contextName;            
        }
        @Override
        public boolean equals(Object a){
            if (! (a instanceof AttributeWrapper))
                return false;
            return ((AttributeWrapper)a).extendedtoString2().equals(extendedtoString2());
        }

        @Override
        public int hashCode() {
            int hash = 4;
            hash = 97 * hash + (this.attributeName != null ? this.attributeName.hashCode() : 0);
            hash = 97 * hash + (this.variableName != null ? this.variableName.hashCode() : 0);
            hash = 97 * hash + (this.componentName != null ? this.componentName.hashCode() : 0);
            hash = 97 * hash + (this.contextName != null ? this.contextName.hashCode() : 0);
            return hash;
        }
        public String extendedtoString(){
            return componentName + "." + variableName +"="+ contextName + "." + attributeName;
        }
        public String extendedtoString2() {
            if (contextName == null) {
                if (attributeName != null) {
                    return componentName + "." + attributeName;
                } else {
                    return componentName + "." + variableName;
                }
            } else {
                if (attributeName != null) {
                    return contextName + "." + attributeName;
                } else {
                    return contextName + "." + variableName;
                }
            }
        }
        @Override
        public String toString(){
            return extendedtoString2();
            /*if (variableName!=null)
                return variableName;
            return attributeName;*/
        }

        public int compareTo(Object a){
            return this.extendedtoString2().compareTo( ((AttributeWrapper)a).extendedtoString2());
        }
    }
    
    public static class Parameter extends AttributeWrapper{        
        public double lowerBound;
        public double upperBound;
        public boolean startValueValid;
        public double startValue;        
        
        public Parameter(AttributeWrapper attr){
            startValueValid = false;
            this.attributeName = attr.attributeName;
            this.componentName = attr.componentName;
            this.contextName = attr.contextName;
            this.variableName = attr.variableName;
            this.isSetByValue = attr.isSetByValue;            
        }

        public Parameter(AttributeWrapper attr, Range range){
            startValueValid = false;
            this.attributeName = attr.attributeName;
            this.componentName = attr.componentName;
            this.contextName = attr.contextName;
            this.variableName = attr.variableName;
            this.isSetByValue = attr.isSetByValue;
            if (range != null){
                this.lowerBound = range.lowerBound;
                this.upperBound = range.upperBound;
            }
        }
    }

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

    public static class Efficiency extends AttributeWrapper{              
        public int mode;
        
        public Efficiency(AttributeWrapper attr){
            this.attributeName = attr.attributeName;
            this.componentName = attr.componentName;
            this.contextName = attr.contextName;
            this.variableName = attr.variableName;
            this.isSetByValue = attr.isSetByValue;            
        }
    }
    
    public static class ModelData{
        public Document modelDoc;
        public JAMSProperties properties;
        public JUICEFrame frame;
    }
    
    static class ModelTreeRenderer extends DefaultTreeCellRenderer {
        Icon contextIcon, componentIcon, attributeIcon;

        int ICON_WIDTH = 16;
        int ICON_HEIGHT = 16;

        public ModelTreeRenderer() {
            componentIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Component_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
            contextIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
            attributeIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/attribute.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
            if (value instanceof DefaultMutableTreeNode){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                if (node.getUserObject() instanceof ComponentWrapper) {
                    ComponentWrapper wrapper = (ComponentWrapper)node.getUserObject();
                    if (wrapper.contextComponent)
                        setIcon(contextIcon);
                    else
                        setIcon(componentIcon);
                }else{
                    setIcon(attributeIcon);
                }
            }
            return this;
        }
    }
    
    static public Node getNodeByName(Node root, String name){
        if (root.getNodeName().equals(name)){
            return root;
        }
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node model = getModelNode(childs.item(i));
            if (model != null)
                return model;
        }
        return null;
    }
    
    static public Node getModelNode(Node root){
        return getNodeByName(root,"model");
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
    
    public static void replaceAttribute(Node root,AttributeWrapper attribute,String newAttributeName,String newAttributeContext){
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
                if (attribute.attributeName != null){
                    String node_attr = varNode.getAttribute("attribute");
                    String node_context = varNode.getAttribute("context");
                    if (node_attr == null)
                        continue;
                    if (node_context == null)
                        node_context = ((Element)root.getParentNode()).getAttribute("name");
                    if (node_attr.equals(attribute.attributeName) && node_context.equals(attribute.contextName)){                        
                        varNode.setAttribute("attribute", newAttributeName);
                        varNode.setAttribute("context", newAttributeContext);
                    }
                //case (a)
                }else{
                    String node_attr = varNode.getAttribute("name");                                        
                    String node_component = ((Element)varNode.getParentNode()).getAttribute("name");
                    if (node_component == null)
                        continue;
                    if (node_attr.equals(attribute.variableName) && node_component.equals(attribute.componentName)){
                        jams.model.metaoptimizer.metaModelOptimizer.RemoveProperty(root.getOwnerDocument().getDocumentElement(), attribute.variableName, attribute.componentName);
                        varNode.setAttribute("attribute", newAttributeName);
                        varNode.setAttribute("context", newAttributeContext);
                        varNode.removeAttribute("value");                        
                    }
                }
            }
            if (node.getNodeName().equals("attribute")){
                Element varNode = (Element)node;
                if (attribute.attributeName != null){
                    String node_attr = varNode.getAttribute("name");
                    String node_context = ((Element)root).getAttribute("name");
                                            
                    if (node_attr.equals(attribute.attributeName) && node_context.equals(attribute.contextName)){
                        //remove broken links
                        jams.model.metaoptimizer.metaModelOptimizer.RemoveProperty(root.getOwnerDocument().getDocumentElement(), attribute.attributeName, attribute.contextName);
                        nodesToRemove.add(node);
                    }
                }                
            }
        }
        for (int i=0;i<nodesToRemove.size();i++){
            root.removeChild(nodesToRemove.get(i));
        }
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
    
    public static void addParameters(ArrayList<Parameter> list,Node root, String optimizerContextName){
        for (int i=0;i<list.size();i++){            
            if (list.get(i).attributeName != null)
                replaceAttribute(root, list.get(i), list.get(i).attributeName, optimizerContextName);
            else
                replaceAttribute(root, list.get(i), list.get(i).variableName, optimizerContextName);
        }
    }
    public static void addEfficiencies(ArrayList<Efficiency> list,Node root, String optimizerContextName){
        for (int i=0;i<list.size();i++){            
            if (list.get(i).attributeName != null)
                replaceAttribute(root, list.get(i), list.get(i).attributeName, optimizerContextName);
            else
                replaceAttribute(root, list.get(i), list.get(i).variableName, optimizerContextName);
        }
        
    }
}
