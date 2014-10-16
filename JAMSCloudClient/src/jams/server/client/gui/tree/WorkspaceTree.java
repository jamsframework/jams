/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui.tree;

import jams.server.client.gui.tree.JAMSServerTreeNodes.SortedMutableTreeNode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.WorkspaceNode;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.Workspaces;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author christian
 */
public class WorkspaceTree extends JAMSServerTree {

    /**
     *
     */
    public WorkspaceTree() {
        super();
    }
    
    private DefaultMutableTreeNode createWorkspaceNode(Workspace ws) {
        SortedMutableTreeNode top = JAMSServerTreeNodes.getNode(ws);

        for (WorkspaceFileAssociation wfa : ws.getFiles()) {
            attachWFAtoTree(wfa, top);
        }
        return top;
    }
        
    /**
     *
     * @return
     */
    public SortedMutableTreeNode getSelectedNode(){
        TreePath path = getSelectionPath();
        if (path == null)
            return null;
        Object o = path.getLastPathComponent();
        if (SortedMutableTreeNode.class.isAssignableFrom(o.getClass())) {
            return (SortedMutableTreeNode) o;
        }
        return null;
    }
        
    /**
     *
     * @return
     */
    public Workspace getSelectedWorkspace(){
        if (!isShowing())
            return null;
        
        Object o = getSelectedNode();
        if (o != null && o instanceof WorkspaceNode){
            return ((WorkspaceNode)o).getWorkspace();
        }
        return null;
    }
    
    /**
     *
     * @param root
     * @param object
     * @return
     */
    public WorkspaceNode findNode(SortedMutableTreeNode root, Workspace object){
        Object o = root.getUserObject();
        if (o.equals(object)){
            return (WorkspaceNode)root;
        }        
        for (int i=0;i<root.getChildCount();i++){
            SortedMutableTreeNode child = (SortedMutableTreeNode)root.getChildAt(i);
            SortedMutableTreeNode result = findNode(child, object);
            if (result != null)
                return (WorkspaceNode)result;
        }
        return null;
    }
           
    /**
     *
     * @param user
     * @param workspaces
     */
    public void generateModel(User user, Workspaces workspaces) {
        SortedMutableTreeNode root = JAMSServerTreeNodes.getNode(user);

        for (Workspace ws : workspaces.getWorkspaces()) {
            if (!ws.isReadOnly()) {
                root.add(createWorkspaceNode(ws));
            }
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
    }    
}
