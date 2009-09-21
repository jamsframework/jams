/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.juice.optimizer.wizard;

import jams.tools.JAMSTools;
import jams.juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import jams.juice.optimizer.wizard.OptimizationWizard.Efficiency;
import jams.juice.*;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import jams.runtime.StandardRuntime;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jams.juice.optimizer.wizard.OptimizationWizard.AttributeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class step4Pane extends stepPane{          
    //JAMSModel model = null;
    StandardRuntime rt = null;
    Document loadedModel = null;
    JTree modelTree = new JTree();    
    ArrayList<Efficiency> selectedEfficiencies = new ArrayList<Efficiency>();
            
/*    private boolean IsVariableOverWritten(String varName,String compName){
        String attr = model.getAttributeToVariable(varName, compName);
        if (attr == null)
            return false;
        if (model.CollectAttributeWritingComponents(attr).size()!=0)
            return true;
        //if attribute is declared in model context directly in jam file
        //then this result is not correct!!
        return true;
    }*/
    
    public ArrayList<Efficiency> getSelectedEfficiencies(){
        return selectedEfficiencies;
    }
    
    void buildModelTree(Node root,DefaultMutableTreeNode node,DefaultTreeModel model){                        
        NodeList childs = root.getChildNodes();
        Element parent = (Element)root;
        for (int i=0;i<childs.getLength();i++){
            Node child = childs.item(i);
            if (child.getNodeName().equals("contextcomponent")){
                Element elem = (Element)child;                
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new ComponentWrapper(
                        elem.getAttribute("name"),
                        elem.getAttribute("name"),
                        true));
                model.insertNodeInto(childTreeNode, node, node.getChildCount());
                
                buildModelTree(child,childTreeNode,model);
            }
            if (child.getNodeName().equals("component")){               
                Element elem = (Element)child;                
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new ComponentWrapper(
                        elem.getAttribute("name"),
                        parent.getAttribute("name"),
                        false));
                model.insertNodeInto(childTreeNode, node, node.getChildCount());
                buildModelTree(child,childTreeNode,model);
            }
            if (child.getNodeName().equals("var")){                
                Element elem = (Element)child;                      
                String context = elem.getAttribute("context");
                String name = elem.getAttribute("name");
                String attr = elem.getAttribute("attribute");
                if (context == null && attr != null){
                    ComponentWrapper component = (ComponentWrapper)node.getUserObject();
                    context = component.componentContext;
                }                
                Class clazz = null;
                Field field = null;
                boolean isDouble = true;
                try{
                    clazz = rt.getClassLoader().loadClass(parent.getAttribute("class"));
                    if (clazz != null)
                        field = JAMSTools.getField(clazz, name);
                }catch(Exception e){
                    continue;
                }                                
                if (field==null)
                    continue;
                field.getAnnotation(JAMSVarDescription.class);
                Class type = field.getType();                                        
                if (!type.getName().equals("jams.data.JAMSDouble")){
                    isDouble = false;                    
                }                
                JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);
                if ((jvd == null ||jvd.access() == AccessType.WRITE || jvd.access() == AccessType.READWRITE ) && isDouble){
                    DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new AttributeWrapper(
                            name,
                            attr,
                            parent.getAttribute("name"),
                            context));
                    model.insertNodeInto(childTreeNode, node, node.getChildCount());
                }
            }
            if (child.getNodeName().equals("attribute")){                
                Element elem = (Element)child;                                                      
                String attr = elem.getAttribute("name");
                String context = parent.getAttribute("name");
                String clazz = elem.getAttribute("class");
                                
                if (clazz.equals("jams.data.JAMSDouble")){
                    DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new AttributeWrapper(
                            attr,
                            attr,
                            null,
                            context));
                    model.insertNodeInto(childTreeNode, node, node.getChildCount());
                }
            }
        }
        
        /*
        ArrayList<AttributeSpec> specs = context.getAttributes();
        for (int i=0;i<specs.size();i++){
            AttributeSpec attr = specs.get(i);
            if (attr.className.equals("jams.data.JAMSDouble")){
                DefaultMutableTreeNode attrNode = new DefaultMutableTreeNode(attr.attributeName);
                model.insertNodeInto( attrNode,node, 0); 
            }
        }
        for (int i=0;i<context.getComponents().size();i++){
            JAMSComponent comp = (JAMSComponent)context.getComponents().get(i);
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new ComponentWrapper(comp));
            Field[] fields = comp.getClass().getFields();
            model.insertNodeInto(childNode, node, node.getChildCount());
            for (int j=0;j<fields.length;j++){
                if (fields[j].isAnnotationPresent(JAMSVarDescription.class)){
                    JAMSVarDescription jvd = fields[j].getAnnotation(JAMSVarDescription.class);
                    if (jvd.access() == AccessType.READ){
                        Class type = fields[j].getType();                                        
                        if (type.getName().equals("jams.data.JAMSDouble")){
                            if (!IsVariableOverWritten(fields[j].getName(),comp.getInstanceName())){
                                DefaultMutableTreeNode attrNode = new DefaultMutableTreeNode(fields[j].getName());
                                model.insertNodeInto( attrNode,childNode, childNode.getChildCount());                            
                            }
                        }
                    }
                }
            }            
            if (comp instanceof JAMSContext)
                buildModelTree((JAMSContext)comp,childNode,model);
        }*/
    }
    
    @Override
    public JPanel build() {        
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(JUICE.resources.getString("step3_desc")), BorderLayout.NORTH);               
        
        JScrollPane treeScroller = new JScrollPane(modelTree);        
        treeScroller.setVisible(true);
        panel.add(treeScroller);
        
        return panel;                
    }
    
    @Override    
    public JPanel getPanel(){
        return panel;
    }
    
    public void setRuntime(StandardRuntime rt){
        this.rt = rt;
    }
    
    public void setModelDocument(Document doc){
        this.loadedModel = doc;
    }
    
    public Node getModelNode(Node root){
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
    
    @Override   
    public String init(){
        if (rt == null){
            return JUICE.resources.getString("error_no_model_loaded");
        }
        this.selectedEfficiencies.clear();
        modelTree.setRootVisible(true);
        Node root = getModelNode(loadedModel.getDocumentElement());
        Element rootElement = (Element)root;
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new ComponentWrapper(rootElement.getAttribute("name")
                ,rootElement.getAttribute("name"),true)));
        modelTree.setModel(treeModel);
        modelTree.setCellRenderer(new Tools.ModelTreeRenderer());
        buildModelTree(root,(DefaultMutableTreeNode)treeModel.getRoot(),treeModel);      
        return null;
    }
    
    @Override
    public String finish(){        
        TreePath selections[] = modelTree.getSelectionPaths();
        if (selections == null){
            return JUICE.resources.getString("error_no_parameter");
        }        
        selectedEfficiencies.clear();
        for (int i=0;i<selections.length;i++){
            Object selectionPath[] = selections[i].getPath();
            if (selectionPath.length < 2)
                continue;
            DefaultMutableTreeNode attributeNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-1];            
            if (attributeNode.getUserObject() instanceof AttributeWrapper){
                Efficiency selectedEfficiency = new Efficiency((AttributeWrapper)attributeNode.getUserObject());                
                selectedEfficiencies.add(selectedEfficiency);
            }
        }
        if (selectedEfficiencies.size()==0){
            return JUICE.resources.getString("error_no_parameter");
        }
        return null;
    }
}
