/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.juice.optimizer.wizard;

import jams.juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import java.awt.Component;
import java.awt.Image;
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
    
    static public Node getModelNode(Node root){
        if (root.getNodeName().equals("model")){
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
    
    static public String getWorkspacePath(Document model){
        Element root = model.getDocumentElement();
        Element modelElem = (Element)Tools.getModelNode(root);
        //modelNode.g
        
        NodeList childs = modelElem.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("var")){
                if ( ((Element)node).getAttribute("name").equals("workspaceDirectory") ){
                    return ((Element)node).getAttribute("value");
                }
            }
        }        
        return null;
    }
    
    static public Node getFirstComponent(Node root){
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            if ( childs.item(i).getNodeName().equals("component") || childs.item(i).getNodeName().equals("contextcomponent") ){
                return childs.item(i);
            }
            Node result = getFirstComponent(childs.item(i));
            if (result != null)
                return result;
        }
        return null;
    }
    
    static public Node findComponentNode(Node context, String name){
        NodeList childs = context.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            String node_name = node.getNodeName();
            if (node_name.equals("component") || node_name.equals("contextcomponent") || node_name.equals("model")){
                if (((Element)node).getAttribute("name").equals(name)){
                    return node;
                }
            }
            Node result = findComponentNode(node,name);
            if (result != null)
                return result;
        }
        return null;
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
}
