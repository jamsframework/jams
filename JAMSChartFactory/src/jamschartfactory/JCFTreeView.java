/*
 * JCFTreeView.java
 *
 * Created on 20. April 2006, 11:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author c0krpe
 */
public class JCFTreeView extends javax.swing.JPanel implements Serializable, MouseListener{
    DefaultTreeModel treeModel;
    JTree tree;
    MainFrame frame;
    boolean newPlot = false;
    boolean addPlot = false;
    /**
     * Creates a new instance of JCFTreeView
     */
    public JCFTreeView(String fileName, String[] dataCols, MainFrame mf) {
        super(new java.awt.GridLayout(1,0));
        frame = mf;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Data files");//new DefaultMutableTreeNode(fileName);
        treeModel = new DefaultTreeModel(rootNode);
        
        this.addRootLeaf(treeModel, fileName, dataCols);
        //DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(fileName);
        //for(int i = 0; i < dataCols.length; i++)
        //    rootNode.add(new DataColumnTreeNode(dataCols[i]));
        treeModel.addTreeModelListener(new MyTreeModelListener());
        
        tree = new JTree(treeModel);
        tree.setFont(new java.awt.Font("Arial", 0, 11));
        //initialize treeView
        
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
        
        tree.addMouseListener(this);
 
    }
    
    public DefaultTreeModel getModel(){
        return this.treeModel;
    }
    
    /**
     * adds a root node to the j2kTree. Function is called during
     * model startup.
     * @param j2kTree the J2kTree on the left pane
     * @param leaf the display name of the root node
     */
    public void addRootLeaf(DefaultTreeModel treeModel, String leaf, String[] dataCols){
        
        DefaultMutableTreeNode Leaf = new DefaultMutableTreeNode(leaf);
        for(int i = 0; i < dataCols.length; i++)
            Leaf.add(new DataColumnTreeNode(dataCols[i]));
        
        ((DefaultMutableTreeNode)treeModel.getRoot()).add(Leaf);
        treeModel.reload();
    }
    
    /**
     * mouse event of the treeView
     * @param e the mouse event
     */
    public void mouseReleased(MouseEvent e){
        if(e.getButton() == e.BUTTON3){
            //System.out.println("Mouse event at component: "+e);
            System.out.println("Button 3 has been realeased.");
        }
    }
    /**
     * mouse event of the treeView
     * @param e the mouse event
     */
    public void mouseExited(MouseEvent e){
        //System.out.println("Mouse exited e: " + e);
    }
    /**
     * mouse event of the treeView
     * @param e the mouse event
     */
    public void mouseEntered(MouseEvent e){
        //System.out.println("Mouse entered e: " + e);
    }
    /**
     * mouse event of the treeView
     * @param e the mouse event
     */
    public void mousePressed(MouseEvent e){
        //System.out.println("Mouse pressed e: " + e);
    }
    
    
    /**
     * mouse event of the treeView
     * @param e the mouse event
     */
    public void mouseClicked(MouseEvent e){
        if(e.getButton() == e.BUTTON3){
            //System.out.println("Mouse event at component: "+e);
            System.out.println("Button 3 has been clicked.");
            javax.swing.JTree tree = (javax.swing.JTree)e.getComponent();
            
            javax.swing.tree.TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
            
            if(selectedPath == null){
                System.err.println("Selected Path is null!");
            }
            else{
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
                if(node.isRoot()){
                    System.out.println((String)node.getUserObject() + " file can be changed");
                }
                else{
                   int level = node.getRoot().getIndex(node);
                   System.out.println((String)node.getUserObject() + " data can be plotted. Node Nr. "+level);
                   DataColumnTreeNode dcNode = (DataColumnTreeNode)node;
                   dcNode.popupMenu.show(this.tree, e.getX(), e.getY());
                   System.out.println("nodeAction: " + dcNode.selectedAction);
                   
                }
            }
            /*
            javax.swing.tree.TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
            
            if(selectedPath == null){
                System.err.println("Selected Path is null!");
            }
            else{
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
                tree.setSelectionPath(new javax.swing.tree.TreePath(node.getPath()));
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
                System.out.println("Selected node: "+parentNode.getUserObject() + "-" + node.getUserObject());
                if(parentNode.getUserObject().equals("SpatialObjects")){
                    spatialObjectTreeNode soNode = (spatialObjectTreeNode)node;
                    soNode.popupMenu.show(this.j2kTree, e.getX(), e.getY());
                }
                else if(parentNode.getUserObject().equals("TimeSeriesData")){
                    stationDataSetTreeNode sdNode = (stationDataSetTreeNode)node;
                    sdNode.popupMenu.show(this.j2kTree, e.getX(), e.getY());
                }
                else if(parentNode.getUserObject().equals("ProcessModules")){
                    processModuleTreeNode pmNode = (processModuleTreeNode)node;
                    pmNode.popupMenu.show(this.j2kTree, e.getX(), e.getY());
                }
                else if(parentNode.getUserObject().equals("ParameterSets")){
                    parameterSetTreeNode pmNode = (parameterSetTreeNode)node;
                    pmNode.popupMenu.show(this.j2kTree, e.getX(), e.getY());
                }
            }*/
            //tree.getModel().get;
            //DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getComponentAt(e.getX(), e.getY());
        }
    }
    
}

class MyTreeModelListener implements javax.swing.event.TreeModelListener {
    public void treeNodesChanged(javax.swing.event.TreeModelEvent e) {
        System.out.println("treeNodesChanged was called");
        DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode)
        (e.getTreePath().getLastPathComponent());
        
            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */
        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)
            (node.getChildAt(index));
        } catch (NullPointerException exc) {}
        
        System.out.println("The user has finished editing the node.");
        System.out.println("New value: " + node.getUserObject());
    }
    public void treeNodesInserted(javax.swing.event.TreeModelEvent e) {
        System.out.println("treeNodesInserted");
    }
    public void treeNodesRemoved(javax.swing.event.TreeModelEvent e) {
    }
    public void treeStructureChanged(javax.swing.event.TreeModelEvent e) {
        System.out.println("treeStructureChanged");
    }
}
