/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package juice.optimizer.wizard;

import jams.JAMSProperties;
import juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import juice.optimizer.wizard.OptimizationWizard.Parameter;
import juice.*;
import jams.model.JAMSComponent;
import jams.model.JAMSContext;
import jams.model.JAMSModel;
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
import org.w3c.dom.Document;

/**
 *
 * @author Christian Fischer
 */
public class step2Pane extends stepPane {        
    Document loadedModel = null;
    JAMSProperties properties = null;
    JAMSModel model;
    JTree modelTree = new JTree();    
    ArrayList<Parameter> selectedParameters = new ArrayList<Parameter>();
    
    public void setModelDoc(Document model){
        loadedModel = model;
    }
    
    public void setProperties(JAMSProperties properties){
        this.properties = properties;
    }
    
    static class MyRenderer extends DefaultTreeCellRenderer {
        Icon contextIcon, componentIcon, attributeIcon;
            
        int ICON_WIDTH = 16;
        int ICON_HEIGHT = 16;
    
        public MyRenderer() {
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
                    if (wrapper.content instanceof JAMSContext)
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

    public boolean IsVariableOverWritten(String varName,String compName){
        String attr = model.getAttributeToVariable(varName, compName);
        if (attr == null)
            return false;
        if (model.CollectAttributeWritingComponents(attr).size()!=0)
            return true;
        //if attribute is declared in model context directly in jam file
        //then this result is not correct!!
        return true;
    }
    
    void buildModelTree(JAMSContext context,DefaultMutableTreeNode node,DefaultTreeModel model){
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
        }
    }
    
    @Override
    public String init(){
        StandardRuntime rt = new StandardRuntime();
        if (this.loadedModel == null){
            return JUICE.resources.getString("error_no_model_loaded");            
        }
        if (this.properties == null){
            return JUICE.resources.getString("error_no_property_file");            
        }
        
        rt.loadModel(this.loadedModel, properties);
        selectedParameters.clear();
        model = rt.getModel();
        modelTree.setRootVisible(true);
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new ComponentWrapper(model)));
        modelTree.setModel(treeModel);
        modelTree.setCellRenderer(new MyRenderer());
        buildModelTree(model,(DefaultMutableTreeNode)treeModel.getRoot(),treeModel);        
        return null;
    }

    public JAMSModel getModel(){
        return this.model;
    }
    
    @Override    
    public String finish(){
        TreePath selections[] = modelTree.getSelectionPaths();
        if (selections == null){
            return JUICE.resources.getString("error_no_parameter");    
        }        
        selectedParameters.clear();
        for (int i=0;i<selections.length;i++){
            Object selectionPath[] = selections[i].getPath();
            if (selectionPath.length < 2)
                continue;
            DefaultMutableTreeNode attributeNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-1];
            DefaultMutableTreeNode componentNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-2];
            if (attributeNode.getUserObject() instanceof String &&
                componentNode.getUserObject() instanceof ComponentWrapper){
                Parameter selectedParameter = new Parameter();
                selectedParameter.name = (String)attributeNode.getUserObject();              
                selectedParameter.component = ((ComponentWrapper)componentNode.getUserObject()).content;              
                selectedParameters.add(selectedParameter);
            }
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
        panel.add(new JLabel(JUICE.resources.getString("step1_desc")), BorderLayout.NORTH);               
        
        JScrollPane treeScroller = new JScrollPane(modelTree);        
        treeScroller.setVisible(true);
        panel.add(treeScroller);
        
        return panel;
    }  
    
    public ArrayList<Parameter> getSelection(){
        return this.selectedParameters;
    }
}
