/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package juice.optimizer.wizard;

import juice.optimizer.wizard.OptimizationWizard.Efficiency;
import juice.optimizer.wizard.OptimizationWizard.Parameter;
import juice.optimizer.wizard.step6Pane.AttributeDescription;
import juice.optimizer.wizard.step6Pane.OptimizerDescription;
import juice.*;
import jams.io.XMLIO;
import jams.model.JAMSModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christian Fischer
 */
public class step7Pane extends stepPane {
    final JTextField selectedOutputFile = new JTextField(25);
    JScrollPane logScroller = null;
    JDialog parent = null;
    JTextArea infoLogField = new JTextArea();
    
    OptimizerDescription desc;
    
    Document doc = null;        
    JAMSModel model = null;
    
    boolean removeNotUsedComponents = true;
    boolean removeGUIComponents = true;
    boolean optimizeModelStructure = true;
    
    String infoLog = null;
    
    public String getOutputPath(){
        return this.selectedOutputFile.getText();
    }
    public Document getModifiedDocument(){
        return this.doc;
    }
    
    public void setDialog(JDialog parent){
        this.parent = parent;
    }
    
    public void setModelOptimizationProperties(boolean removeNotUsedComponents,boolean removeGUI,boolean optimizeStructure){
        this.removeNotUsedComponents = removeNotUsedComponents;
        this.removeGUIComponents = removeGUI;
        this.optimizeModelStructure = optimizeStructure;
    }
    public void setOptimizerDescription(OptimizerDescription desc){
        this.desc = desc;
    }
    public void setModel(Document doc,JAMSModel model){
        this.doc = doc;
        this.model = model;        
    }
    
    private void AddAttribute(Element parent,String name,String value,String context,boolean isValue){
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
    
    private Node findComponentNode(Node context, String name){
        NodeList childs = context.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            String node_name = node.getNodeName();
            if (node_name.equals("component") || node_name.equals("contextcomponent")){
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
    
    private void removeAttribute(Node component, String name){
        NodeList childs = component.getChildNodes();
        for (int i=0;i<childs.getLength();i++){
            Node node = childs.item(i);
            if (node.getNodeName().equals("var")){
                if (((Element)node).getAttribute("name").equals(name)){
                    component.removeChild(node);
                }
            }            
        }        
    }
    
    private Node getFirstComponent(Node root){
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
            
    private void addParameters(ArrayList<Parameter> list,Node root){
        for (int i=0;i<list.size();i++){
            Node component = findComponentNode(root,list.get(i).component.getInstanceName());
            if (component == null)
                return;
            removeAttribute(component,list.get(i).name);
            AddAttribute((Element)component,list.get(i).name,list.get(i).name,"optimizer",false);
        }
    }
    private void addEfficiencies(ArrayList<Efficiency> list,Node root){
        for (int i=0;i<list.size();i++){
            Node component = findComponentNode(root,list.get(i).component.getInstanceName());
            removeAttribute(component,list.get(i).name);
            AddAttribute((Element)component,list.get(i).name,list.get(i).name,"optimizer",false);
        }
    }
    
    @Override
    public String init(){   
        infoLog = "";
	//1. schritt
        //parameter relevante componenten verschieben                
        infoLog += JUICE.resources.getString("create_transitive_hull_of_dependency_graph") + "\n";
        Hashtable<String,HashSet<String>> dependencyGraph = jams.components.metaOptimizer.modelOptimizer.getDependencyGraph(doc.getDocumentElement(),model);
        Hashtable<String,HashSet<String>> transitiveClosureOfDependencyGraph = 
                jams.components.metaOptimizer.modelOptimizer.TransitiveClosure(dependencyGraph);
        
        doc = (Document)doc.cloneNode(true);
        Node root = (Node)doc.getDocumentElement();
        
        if (removeGUIComponents){
            infoLog = JUICE.resources.getString("removing_GUI_components")+ ":\n";
            ArrayList<String> removedGUIComponents = jams.components.metaOptimizer.modelOptimizer.RemoveGUIComponents(root);
            for (int i=0;i<removedGUIComponents.size();i++){
                infoLog += "    ***" + removedGUIComponents.get(i) + "\n";
            }
        }
        if (optimizeModelStructure){
            HashSet<String> effWritingComponents = new HashSet<String>();
            for (int i=0;i<desc.efficiencies.size();i++){                
                effWritingComponents.addAll(model.CollectAttributeWritingComponents(desc.efficiencies.get(i).name));
            }
            ArrayList<String> removedUnusedComponents = jams.components.metaOptimizer.modelOptimizer.RemoveNotListedComponents(root,
                    jams.components.metaOptimizer.modelOptimizer.GetRelevantComponentsList(transitiveClosureOfDependencyGraph,
                    effWritingComponents));
            
            infoLog += JUICE.resources.getString("removing_components_without_relevant_influence")+":\n";
            for (int i=0;i<removedUnusedComponents.size();i++){
                infoLog += "    ***" + removedUnusedComponents.get(i) + "\n";
            }
        }
        infoLog += JUICE.resources.getString("add_optimization_context") + "\n";                                                                              
        //optimierer bauen
        Element optimizerContext = doc.createElement("contextcomponent");
        optimizerContext.setAttribute("class", desc.optimizerClassName);
        optimizerContext.setAttribute("name", "optimizer");
                       
        
        Iterator<AttributeDescription> iter = desc.attributes.iterator();
        while(iter.hasNext()){
            AttributeDescription attr = iter.next();            
            AddAttribute(optimizerContext,attr.name,attr.value,attr.context,!attr.isAttribute);
        }
                               
        addParameters(desc.parameters,root);
        addEfficiencies(desc.efficiencies,root);
                                      
        infoLog += JUICE.resources.getString("find_a_position_to_place_optimizer") + "\n";
        //find place for optimization context
        Node firstComponent = getFirstComponent(root);
        if (firstComponent == null){
            return JUICE.resources.getString("Error_model_file_does_not_contain_any_components");
        }
        //collect all following siblings of firstComponent and add them to contextOptimizer
        Node currentNode = firstComponent;
        ArrayList<Node> followingNodes = new ArrayList<Node>();
        while( currentNode.getNextSibling() != null){
            followingNodes.add(currentNode);
            currentNode = currentNode.getNextSibling();
        }
        
        if (firstComponent.getParentNode() == null){
            return JUICE.resources.getString("Error_model_file_does_not_contain_a_model_context");
        }
        Node modelContext = firstComponent.getParentNode();
        for (int i=0;i<followingNodes.size();i++){
            modelContext.removeChild(followingNodes.get(i));
            optimizerContext.appendChild(followingNodes.get(i));
        }
        modelContext.appendChild(optimizerContext);
              
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(root);
        
        infoLogField.setPreferredSize(new Dimension(300,200));        
        infoLogField.setAutoscrolls(true);
        infoLogField.setBorder(new LineBorder(Color.BLACK));
        infoLogField.setEditable(true);
        
        infoLogField.append(infoLog);
        infoLogField.invalidate();
        panel.invalidate();        
        return null;
    }
    
    @Override 
    public JPanel getPanel(){
        return panel;
    }
    
    
    
    @Override
    public JPanel build(){
        panel.setLayout(new GridBagLayout());
        
        JButton chooseModelFile = new JButton(JUICE.resources.getString("Save"));        
        JPanel saveModelFilePanel = new JPanel(new GridBagLayout());    
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        saveModelFilePanel.add(selectedOutputFile,c);
        c.gridx = 1;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        saveModelFilePanel.add(chooseModelFile,c);
                
        JPanel modelFilePanel = new JPanel(new BorderLayout());
        modelFilePanel.add(new JLabel(JUICE.resources.getString("output_file_path")), BorderLayout.NORTH);
        modelFilePanel.add(saveModelFilePanel,BorderLayout.CENTER);

        c.gridx = 0;    c.gridy = 0;    c.fill = GridBagConstraints.NONE;
        panel.add(modelFilePanel,c);   
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(new JLabel("infoLog"),BorderLayout.NORTH);
        logScroller = new JScrollPane();
        logScroller.setViewportView(infoLogField);       
        logScroller.setVisible(true);
        infoPanel.add(logScroller,BorderLayout.SOUTH);
        c.gridx = 0;    c.gridy = 1;    c.fill = GridBagConstraints.NONE;
        panel.add(infoPanel,c);                
                        
        chooseModelFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle(JUICE.resources.getString("Choose_a_model_file"));
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".jam") || f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
                    }
                    @Override
                    public String getDescription() {
                        return "model file filter";
                    }
                });

                if (fc.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File fileFromDialog = fc.getSelectedFile();
                selectedOutputFile.setText(fileFromDialog.getAbsolutePath());
            }
        });      
        
        infoLogField.setCaretPosition(infoLogField.getDocument().getLength());
        
        return panel;    
    }                
}
