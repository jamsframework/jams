/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui.tree;

import jams.server.client.JobController;
import jams.server.client.gui.tree.JAMSServerTreeNodes.JobNode;
import jams.server.client.gui.tree.JAMSServerTreeNodes.SortedMutableTreeNode;
import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Jobs;
import jams.server.entities.User;
import jams.server.entities.WorkspaceFileAssociation;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author christian
 */
public class JobsTree extends JAMSServerTree {
     
    /**
     *
     */
    public JobsTree() {
        super();
    }
        
    private DefaultMutableTreeNode createJobNode(Job job, JobState state) {
        SortedMutableTreeNode top = JAMSServerTreeNodes.getNode(job, state);

        for (WorkspaceFileAssociation wfa : job.getWorkspace().getFiles()) {            
            attachWFAtoTree(wfa, top);
        }
        return top;
    }
    
    /**
     *
     * @param job
     */
    public void updateNode(Job job){
        DefaultMutableTreeNode newNode = createJobNode(job, null);        
        DefaultMutableTreeNode oldNode = findNode(getRoot(), job);
        if (oldNode == null)
            return;
        
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)oldNode.getParent();
        int index = parent.getIndex(oldNode);        
        oldNode.removeFromParent();
        parent.insert(newNode, index);        
    }
                    
    /**
     *
     * @return
     */
    public Job getSelectedJob() {
        if (!this.isShowing())
            return null;
        
        Object o = this.getSelectedNode();
        if (o != null && o instanceof JobNode){
            return ((JobNode)o).getJob();
        }
        return null;
    }
            
    /**
     *
     * @param root
     * @param object
     * @return
     */
    public JobNode findNode(SortedMutableTreeNode root, Job object){
        Object o = root.getUserObject();
        if (o.equals(object)){
            return (JobNode)root;
        }        
        for (int i=0;i<root.getChildCount();i++){
            SortedMutableTreeNode child = (SortedMutableTreeNode)root.getChildAt(i);
            SortedMutableTreeNode result = findNode(child, object);
            if (result != null)
                return (JobNode)result;
        }
        return null;
    }
        
    JobController c;
    /**
     *
     * @param user
     * @param jobs
     * @param c
     */
    public void generateModel(User user, Jobs jobs, JobController c) {
        SortedMutableTreeNode root = JAMSServerTreeNodes.getNode(user);
        this.c = c;
        
        for (Job job : jobs.getJobs()) {
            JobState state = null;
            if (job.getPID() > 0) {
                state = c.getState(job);
            }
            root.add(createJobNode(job, state));
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
    }
}
