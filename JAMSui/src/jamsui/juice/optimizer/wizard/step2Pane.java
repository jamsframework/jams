/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jams.JAMSProperties;
import jams.tools.JAMSTools;
import jamsui.juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import jamsui.juice.optimizer.wizard.OptimizationWizard.Parameter;
import jams.model.JAMSModel;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
import jams.runtime.StandardRuntime;
import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilderFactory;
import jamsui.juice.optimizer.wizard.OptimizationWizard.AttributeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class step2Pane extends stepPane {        
    Document loadedModel = null;
    JAMSProperties properties = null;
    JAMSModel model;
    StandardRuntime rt = null;
    JTree modelTree = new JTree();    
    ArrayList<Parameter> selectedParameters = new ArrayList<Parameter>();
    
    public void setModelDoc(Document model){
        loadedModel = model;
    }
    
    public void setProperties(JAMSProperties properties){
        this.properties = properties;
    }
                         
    boolean isPotentialParameter(JAMSVarDescription jvd, String attrName, String contextName){
        if (jvd.access() != AccessType.READ)
            return false;        
        if (attrName==null)
            return true;
        else{
            //these are potential parameters, but they are allready listed in the context node
            /*
            Node contextNode = Tools.findComponentNode(getDocument(), contextName);
            if (contextNode==null)
                return false;
            
            NodeList list = contextNode.getChildNodes();
            
            for (int i=0;i<list.getLength();i++){
                Node node = list.item(i);
                if ( node.getNodeName().equals("attribute") && ((Element)node).hasAttribute("name") && ((Element)node).hasAttribute("value") ){                    
                    if (((Element)node).getAttribute("name").equals(attrName)){
                        return true;
                    }                    
                }
            }*/
            return false;
        }
        
    }
    
    //todo .. component attributes, which are not used in the jam file, will not
    //be shown in this list
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
                if (attr.equals(""))    attr = null;
                if (context.equals("")) context = null;
                if (name.equals(""))    name = null;
                                
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
                    //System.out.println(e.toString() + parent.getAttribute("class"));
                    continue;
                }                                
                if (field==null){
                    //System.out.println("field is null" + clazz);
                    continue;
                }
                field.getAnnotation(JAMSVarDescription.class);
                Class type = field.getType();                                        
                if (!type.getName().equals("jams.data.JAMSDouble")){
                    isDouble = false;                    
                }                
                JAMSVarDescription jvd = field.getAnnotation(JAMSVarDescription.class);
                
                if (isPotentialParameter(jvd,attr,context) && isDouble){                    
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
                            null,
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
    
    private Node getModelNode(Node root){
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
    
    public Document getDocument(){
        return this.loadedModel;
        
    }
    
    public boolean RemoveLauncher(Node root){
        NodeList childs = root.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("launcher")){
                root.removeChild(node);
                return true;
            }else
               if (RemoveLauncher(node))
                   return true;
        }        
        return false;
    }
    
    @Override
    public String init(){
        rt = new StandardRuntime();
        if (this.loadedModel == null){
            return JAMS.resources.getString("error_no_model_loaded");            
        }
        if (this.properties == null){
            return JAMS.resources.getString("error_no_property_file");            
        }
        //copy doc, because loadModel will alter document sometimes
        
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        Document CopyDoc = null;
        try{
            Node oldDocument = this.loadedModel.cloneNode(true);
            CopyDoc = (f.newDocumentBuilder()).newDocument();            
            CopyDoc.appendChild(CopyDoc.importNode(oldDocument.getFirstChild(),true));        
            RemoveLauncher(CopyDoc);
        }catch(Exception e){System.out.println(e); e.printStackTrace();}
        
        rt.loadModel(CopyDoc, properties);
        if (rt.getDebugLevel() >= 3){
            System.out.println(rt.getErrorLog());
            System.out.println(rt.getInfoLog());
        }
        selectedParameters.clear();
        model = rt.getModel();
        modelTree.setRootVisible(true);
        Node root = getModelNode(loadedModel);
        Element rootElement = (Element)root;
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new ComponentWrapper(rootElement.getAttribute("name")
                ,rootElement.getAttribute("name"),true)));
        modelTree.setModel(treeModel);
        modelTree.setCellRenderer(new Tools.ModelTreeRenderer());
        buildModelTree(root,(DefaultMutableTreeNode)treeModel.getRoot(),treeModel);        
        return null;
    }

    public JAMSModel getModel(){
        return this.model;
    }
    public StandardRuntime getRuntime(){
        return rt;
    }
    
    @Override    
    public String finish(){
        TreePath selections[] = modelTree.getSelectionPaths();
        if (selections == null){
            return JAMS.resources.getString("error_no_parameter");    
        }        
        selectedParameters.clear();
        for (int i=0;i<selections.length;i++){
            Object selectionPath[] = selections[i].getPath();
            if (selectionPath.length < 2)
                continue;
            DefaultMutableTreeNode attributeNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-1];
            
            if (attributeNode.getUserObject() instanceof AttributeWrapper ){
                Parameter selectedParameter = new Parameter((AttributeWrapper)attributeNode.getUserObject());                       
                selectedParameters.add(selectedParameter);
            }
        }
        if (selectedParameters.size()==0){          
            return JAMS.resources.getString("error_no_parameter");    
        }
        return null;
    }
    
    @Override
    public JPanel getPanel(){
        return null;
    }
                
    @Override
    public JPanel build(){                
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(JAMS.resources.getString("step1_desc")), BorderLayout.NORTH);               
        
        JScrollPane treeScroller = new JScrollPane(modelTree);        
        treeScroller.setVisible(true);
        panel.add(treeScroller);
        
        return panel;
    }  
    
    public ArrayList<Parameter> getSelection(){
        return this.selectedParameters;
    }
}
