/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package juice.optimizer.wizard;

import juice.optimizer.wizard.OptimizationWizard.ComponentWrapper;
import juice.optimizer.wizard.OptimizationWizard.Efficiency;
import jams.model.JAMSComponent;
import jams.model.JAMSContext;
import jams.model.JAMSModel;
import jams.model.JAMSVarDescription;
import jams.model.JAMSVarDescription.AccessType;
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
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Christian Fischer
 */
public class step4Pane extends stepPane{          
    JAMSModel model = null;
    JTree modelTree = new JTree();    
    ArrayList<Efficiency> selectedEfficiencies = new ArrayList<Efficiency>();
    
    static class MyRenderer extends DefaultTreeCellRenderer {
        Icon contextIcon, componentIcon, attributeIcon;
            
        int ICON_WIDTH = 16;
        int ICON_HEIGHT = 16;
    
        public MyRenderer() {
            contextIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));
            componentIcon = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/ContextComponent.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH));            
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
    
    private boolean IsVariableOverWritten(String varName,String compName){
        String attr = model.getAttributeToVariable(varName, compName);
        if (attr == null)
            return false;
        if (model.CollectAttributeWritingComponents(attr).size()!=0)
            return true;
        //if attribute is declared in model context directly in jam file
        //then this result is not correct!!
        return true;
    }
    
    public ArrayList<Efficiency> getSelectedEfficiencies(){
        return selectedEfficiencies;
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
                    if (jvd.access() != AccessType.READ ){
                        Class type = fields[j].getType();                                        
                        if (type.getName().equals("jams.data.JAMSDouble")){
                            if (IsVariableOverWritten(fields[j].getName(),comp.getInstanceName())){
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
    public JPanel build() {        
        panel.setLayout(new BorderLayout());
        panel.setBorder(null);
        panel.add(new JLabel(java.util.ResourceBundle.getBundle("resources/Bundle").getString("step3_desc")), BorderLayout.NORTH);               
        
        JScrollPane treeScroller = new JScrollPane(modelTree);        
        treeScroller.setVisible(true);
        panel.add(treeScroller);
        
        return panel;                
    }
    
    @Override    
    public JPanel getPanel(){
        return panel;
    }
    
    public void setModel(JAMSModel model){
        this.model = model;
    }
    
    @Override   
    public String init(){
        if (model == null){
            return java.util.ResourceBundle.getBundle("resources/Bundle").getString("error_no_model_loaded");
        }
        this.selectedEfficiencies.clear();
        modelTree.setRootVisible(true);
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(new ComponentWrapper(model)));
        modelTree.setModel(treeModel);
        modelTree.setCellRenderer(new MyRenderer());
        buildModelTree(model,(DefaultMutableTreeNode)treeModel.getRoot(),treeModel);        
        return null;
    }
    
    @Override
    public String finish(){        
        TreePath selections[] = modelTree.getSelectionPaths();
        if (selections == null){
            return java.util.ResourceBundle.getBundle("resources/Bundle").getString("error_no_parameter");
        }        
        selectedEfficiencies.clear();
        for (int i=0;i<selections.length;i++){
            Object selectionPath[] = selections[i].getPath();
            if (selectionPath.length < 2)
                continue;
            DefaultMutableTreeNode attributeNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-1];
            DefaultMutableTreeNode componentNode = (DefaultMutableTreeNode)selectionPath[selectionPath.length-2];
            if (attributeNode.getUserObject() instanceof String &&
                componentNode.getUserObject() instanceof ComponentWrapper){
                Efficiency selectedEfficiency = new Efficiency();
                selectedEfficiency.name = (String)attributeNode.getUserObject();              
                selectedEfficiency.component = ((ComponentWrapper)componentNode.getUserObject()).content;              
                selectedEfficiencies.add(selectedEfficiency);
            }
        }
        return null;
    }
}
