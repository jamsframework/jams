/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jamsui.juice.optimizer.wizard;

import jams.tools.JAMSTools;
import jams.tools.XMLTools;
import jams.model.JAMSVarDescription;
import jams.runtime.StandardRuntime;
import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import jamsui.juice.optimizer.wizard.step6Pane.AttributeDescription;
import jamsui.juice.optimizer.wizard.step6Pane.OptimizerDescription;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.JAMS;
import jams.io.XMLProcessor;
import jams.model.Model;
import jamsui.juice.optimizer.wizard.Tools.AttributeWrapper;
import jamsui.juice.optimizer.wizard.Tools.ComponentWrapper;
import jamsui.juice.optimizer.wizard.Tools.Efficiency;
import jamsui.juice.optimizer.wizard.Tools.Parameter;

/**
 *
 * @author Christian Fischer
 */
public class step6aPane extends stepPane {        
    
    final String optimizerContextName = "optimizer";
    
    JDialog parent = null;                
    Document doc = null;        
    Model model = null;
    JTree modelTree = new JTree();  
    
    JCheckBox outputModeEntityWriter = new JCheckBox(JAMS.resources.getString("output_by_entitywriter_component"));
    JCheckBox outputModeStandardized = new JCheckBox(JAMS.resources.getString("output_by_standardized_output_environment"));
    
    boolean removeNotUsedComponents = true;
    boolean removeGUIComponents = true;
    boolean optimizeModelStructure = true;
    
    StandardRuntime rt = null;
       
    String infoLog = "";
    OptimizerDescription desc;
    
    ArrayList<AttributeWrapper> selectedOutputAttributes = new ArrayList<AttributeWrapper>();
            
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
                                
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new AttributeWrapper(
                        name,
                        attr,
                        parent.getAttribute("name"),
                        context));
                model.insertNodeInto(childTreeNode, node, node.getChildCount());
                
            }
            if (child.getNodeName().equals("attribute")){                
                Element elem = (Element)child;                                                      
                String attr = elem.getAttribute("name");
                String context = parent.getAttribute("name");
                String clazz = elem.getAttribute("class");
                                
                //if (clazz.equals("jams.data.JAMSDouble")){
                    DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(new AttributeWrapper(
                            attr,
                            attr,
                            null,
                            context));
                    model.insertNodeInto(childTreeNode, node, node.getChildCount());
                //}
            }
        }
    }
                
    public void setDialog(JDialog parent){
        this.parent = parent;
    }
             
    
    
    @Override
    public String init(){   
                
        this.rt = (StandardRuntime)this.model.getRuntime();
        
        infoLog = "";
	//1. schritt
        //parameter relevante componenten verschieben                
        infoLog += JAMS.resources.getString("create_transitive_hull_of_dependency_graph") + "\n";
        Hashtable<String,HashSet<String>> dependencyGraph = jams.model.metaoptimizer.metaModelOptimizer.getDependencyGraph(doc.getDocumentElement(),model);
        Hashtable<String,HashSet<String>> transitiveClosureOfDependencyGraph = 
                jams.model.metaoptimizer.metaModelOptimizer.TransitiveClosure(dependencyGraph);
        
        doc = (Document)doc.cloneNode(true);
        Node root = (Node)doc.getDocumentElement();
        
        if (removeGUIComponents){
            infoLog = JAMS.resources.getString("removing_GUI_components")+ ":\n";
            ArrayList<String> removedGUIComponents = jams.model.metaoptimizer.metaModelOptimizer.RemoveGUIComponents(root);
            for (int i=0;i<removedGUIComponents.size();i++){
                infoLog += "    ***" + removedGUIComponents.get(i) + "\n";
            }
        }
        if (optimizeModelStructure){
            HashSet<String> effWritingComponents = new HashSet<String>();
            for (int i=0;i<desc.efficiencies.size();i++){                
                effWritingComponents.addAll(
                        jams.model.metaoptimizer.metaModelOptimizer.CollectAttributeWritingComponents(
                        (Node)this.doc.getDocumentElement(),
                        this.model,
                        desc.efficiencies.get(i).attributeName,
                        desc.efficiencies.get(i).contextName));
            }
            ArrayList<String> removedUnusedComponents = jams.model.metaoptimizer.metaModelOptimizer.RemoveNotListedComponents(root,
                    jams.model.metaoptimizer.metaModelOptimizer.GetRelevantComponentsList(transitiveClosureOfDependencyGraph,
                    effWritingComponents));
            
            infoLog += JAMS.resources.getString("removing_components_without_relevant_influence")+":\n";
            for (int i=0;i<removedUnusedComponents.size();i++){
                infoLog += "    ***" + removedUnusedComponents.get(i) + "\n";
            }
        }
                
        infoLog += JAMS.resources.getString("add_optimization_context") + "\n";                                                                              
        //optimierer bauen
        Element optimizerContext = doc.createElement("contextcomponent");
        optimizerContext.setAttribute("class", desc.optimizerClassName);
        optimizerContext.setAttribute("name", optimizerContextName);
                       
        
        Iterator<AttributeDescription> iter = desc.attributes.iterator();
        while(iter.hasNext()){
            AttributeDescription attr = iter.next();            
            jamsui.juice.optimizer.wizard.Tools.addAttribute(optimizerContext,attr.name,attr.value,attr.context,!attr.isAttribute);
        }
                               
        jamsui.juice.optimizer.wizard.Tools.addParameters(desc.parameters,root,this.optimizerContextName);
        jamsui.juice.optimizer.wizard.Tools.addEfficiencies(desc.efficiencies,root,this.optimizerContextName);
                                      
        infoLog += JAMS.resources.getString("find_a_position_to_place_optimizer") + "\n";
        //find place for optimization context
        Node firstComponent = XMLProcessor.getFirstComponent(root);
        if (firstComponent == null){
            return JAMS.resources.getString("Error_model_file_does_not_contain_any_components");
        }
        //collect all following siblings of firstComponent and add them to contextOptimizer
        Node currentNode = firstComponent;
        ArrayList<Node> followingNodes = new ArrayList<Node>();
        while( currentNode.getNextSibling() != null){
            followingNodes.add(currentNode);
            currentNode = currentNode.getNextSibling();
        }
        
        if (firstComponent.getParentNode() == null){
            return JAMS.resources.getString("Error_model_file_does_not_contain_a_model_context");
        }
        Node modelContext = firstComponent.getParentNode();
        for (int i=0;i<followingNodes.size();i++){
            modelContext.removeChild(followingNodes.get(i));
            optimizerContext.appendChild(followingNodes.get(i));
        }
        modelContext.appendChild(optimizerContext);
              
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(root);
        
        
        //show model graph
        modelTree.setRootVisible(true);
        root = XMLProcessor.getModelNode(this.doc.getDocumentElement());
        Element rootElement = (Element)root;
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new ComponentWrapper(rootElement.getAttribute("name")
                ,rootElement.getAttribute("name"),true)));
        modelTree.setModel(treeModel);
        modelTree.setCellRenderer(new Tools.ModelTreeRenderer());
        buildModelTree(root,(DefaultMutableTreeNode)treeModel.getRoot(),treeModel);
        
        panel.invalidate();        
        return null;
    }
    
    @Override 
    public JPanel getPanel(){
        return panel;
    }
            
    @Override
    public JPanel build(){                
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(JAMS.resources.getString("step6a_desc")), BorderLayout.NORTH); 
        
        /*
        JPanel outputOptions = new JPanel();        
        outputOptions.setBorder(BorderFactory.createLineBorder(Color.black));
        outputOptions.add(new JLabel(JAMS.resources.getString("output_mode")));
        
        JPanel subOutputOptions = new JPanel(new BorderLayout());        
        subOutputOptions.add(outputModeEntityWriter,BorderLayout.NORTH);
        subOutputOptions.add(outputModeStandardized,BorderLayout.SOUTH);
        outputOptions.add(subOutputOptions);*/
        
        JPanel subPanel = new JPanel();        
        subPanel.setLayout(new BoxLayout(subPanel,BoxLayout.Y_AXIS));        
        //subPanel.add(outputOptions);
        
        
        JScrollPane treeScroller = new JScrollPane(modelTree);        
        treeScroller.setVisible(true);
        subPanel.add(treeScroller);
                                    
        panel.add(subPanel, BorderLayout.SOUTH); 
        
        return panel;    
    } 
            
    public void setModelOptimizationProperties(boolean removeNotUsedComponents,boolean removeGUI,boolean optimizeStructure){
        this.removeNotUsedComponents = removeNotUsedComponents;
        this.removeGUIComponents = removeGUI;
        this.optimizeModelStructure = optimizeStructure;
    }
    public void setOptimizerDescription(OptimizerDescription desc){
        this.desc = desc;
    }
    public void setModel(Document doc,Model model){
        this.doc = doc;
        this.model = model;        
    }
    
    public String getInfoLog(){
        return this.infoLog;
    }
    
    public Document getDocument(){
        return doc;
    }
            
    @Override
    public String finish(){     
        Map<String,HashSet<String>> outputContexts = new HashMap<String,HashSet<String>>();
         
        //collect output attributes
        TreePath selections[] = modelTree.getSelectionPaths();
        if (selections == null){
            return JAMS.resources.getString("error_no_parameter");    
        }        
        selectedOutputAttributes.clear();
        for (int i=0;i<selections.length;i++){
            Object selectionPath[] = selections[i].getPath();
            if (selectionPath.length < 2)
                continue;
            DefaultMutableTreeNode attributeNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-1];
            
            if (attributeNode.getUserObject() instanceof AttributeWrapper ){
                AttributeWrapper selectedAttribute = (AttributeWrapper)attributeNode.getUserObject();                       
                selectedOutputAttributes.add(selectedAttribute);                                
            }
        }
        
        //parameter and efficiencies are set by default
        for (int i=0;i<this.desc.parameters.size();i++){
            Parameter attrDesc = desc.parameters.get(i);            
            selectedOutputAttributes.add(new AttributeWrapper(null,attrDesc.attributeName,null,optimizerContextName));
        }
        for (int i=0;i<this.desc.efficiencies.size();i++){
            Efficiency attrDesc = desc.efficiencies.get(i);
            selectedOutputAttributes.add(new AttributeWrapper(null,attrDesc.attributeName,null,optimizerContextName));
        }
        
        
        for (int i=0;i<selectedOutputAttributes.size();i++){
            String attr = selectedOutputAttributes.get(i).attributeName;
            if ( attr != null){
                String context = selectedOutputAttributes.get(i).contextName;
                if ( context != null){
                    if (!outputContexts.containsKey(context)){
                        outputContexts.put(context,new HashSet<String>());
                    }
                    outputContexts.get(context).add(attr);
                }
            }
        }
        
        //mode 1: xml
        Iterator<String> iter = outputContexts.keySet().iterator();
                
        while(iter.hasNext()){
            String context = iter.next();
            
            Document outputDoc = XMLTools.createDocument();
            Element root = outputDoc.createElement("outputdatastore"); 
            root.setAttribute("context", context);
            outputDoc.appendChild(root);
            
            Element trace = outputDoc.createElement("trace");             
            root.appendChild(trace);
            
            HashSet<String> attr = outputContexts.get(context);
            Iterator<String> attrIter = attr.iterator();
            while(attrIter.hasNext()){
                Element attrElement = outputDoc.createElement("attribute"); 
                attrElement.setAttribute("id",attrIter.next());
                trace.appendChild(attrElement);
            }
                        
            try{
                XMLTools.writeXmlFile(outputDoc, XMLProcessor.getWorkspacePath(doc) + File.separator + "output" + File.separator + "optimization_wizard_" + context + ".xml");
            }catch(Exception e){
                return JAMS.resources.getString("Error_cant_write_xml_file_because_") + e.toString();
            }            
        }        
        return null;
    }
}
