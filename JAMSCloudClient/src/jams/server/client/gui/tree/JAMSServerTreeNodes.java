/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui.tree;

import jams.server.entities.Job;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author christian
 */
public class JAMSServerTreeNodes {

    /**
     *
     * @author christian
     */
    public static class SortedMutableTreeNode extends DefaultMutableTreeNode implements Comparable<SortedMutableTreeNode> {

        public SortedMutableTreeNode(Object o) {
            super(o);
        }

        public void add(DefaultMutableTreeNode newChild) {
            super.add(newChild);
            Collections.sort(this.children, nodeComparator);
        }

        public void insert(DefaultMutableTreeNode newChild, int childIndex) {
            super.insert(newChild, childIndex);
            Collections.sort(this.children, nodeComparator);
        }
        
        protected Comparator nodeComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof SortedMutableTreeNode && o2 instanceof SortedMutableTreeNode) {
                    SortedMutableTreeNode s1 = (SortedMutableTreeNode) o1;
                    SortedMutableTreeNode s2 = (SortedMutableTreeNode) o2;
                    if (!s1.isLeaf() && s2.isLeaf()) {
                        return -1;
                    }
                    if (s1.isLeaf() && !s2.isLeaf()) {
                        return 1;
                    }                    
                    return s1.compareTo(s2);
                }
                return o1.toString().compareToIgnoreCase(o2.toString());                                
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        };

        @Override
        public int compareTo(SortedMutableTreeNode o) {
            return this.toString().compareToIgnoreCase(o.toString());
        }
    }

    static public class UserNode extends SortedMutableTreeNode {        
        private UserNode(User user) {
            super(user);
        }

        @Override
        public String toString() {
            return ((User)userObject).getName();
        }                
    }
        
    static public class WorkspaceNode extends SortedMutableTreeNode {        
        
        public WorkspaceNode(Workspace ws) {
            super(ws);
        }
        
        public Workspace getWorkspace(){
            return (Workspace)userObject;
        }

        @Override
        public String toString() {
            Workspace ws = getWorkspace();
            if (ws.getName()!=null)
                return String.format("Workspace %05d - %s", ws.getId(), ws.getName());
            else
                return String.format("Workspace %05d", ws.getId());
        }        
        
        @Override
        public int compareTo(SortedMutableTreeNode o) {
            if (o instanceof WorkspaceNode){
                WorkspaceNode o1 = this;
                WorkspaceNode o2 = (WorkspaceNode)o;
                return o1.getWorkspace().getId().compareTo(o2.getWorkspace().getId());
            }
            return this.toString().compareToIgnoreCase(o.toString());
        }
    }

    static public class JobNode extends SortedMutableTreeNode {    

        public JobNode(Job job) {
            super(job);
        }

        public Job getJob(){
            return (Job)getUserObject();
        }
        
        @Override
        public String toString() {
            Job job = getJob();
            if (job.getWorkspace().getName() != null){
                return String.format("Job %05d - %s", job.getId(), job.getWorkspace().getName());
            }else{
                return String.format("Job %05d", job.getId());
            }
        }
        
        @Override
        public int compareTo(SortedMutableTreeNode o) {
            if (o instanceof JobNode){
                JobNode o1 = this;
                JobNode o2 = (JobNode)o;
                return o1.getJob().getId().compareTo(o2.getJob().getId());
            }
            return this.toString().compareToIgnoreCase(o.toString());
        }
    }

    static public class WFANode extends SortedMutableTreeNode {    

        WorkspaceFileAssociation wfa;
        String fileName;
        String subdirs[];

        public WFANode(WorkspaceFileAssociation wfa) {
            super(wfa);
            
            this.wfa = wfa;
            String path = wfa.getPath().replace("\\\\", "\\");
            path = path.replace("//", "/");
            path = path.replaceAll("^/", "");
            path = path.replaceAll("^\\\\", "");
            subdirs = path.split("[/\\\\]");
            if (subdirs.length != 0) {
                fileName = subdirs[subdirs.length - 1];
            } else {
                fileName = "";
            }
        }

        public WorkspaceFileAssociation getWFA(){
            return wfa;
        }
        
        public String[] getSubdirs() {
            return subdirs;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public String toString() {
            return fileName;
        }
    }
    
    static public SortedMutableTreeNode getNode(User user){
        return new UserNode(user);
    }
    static public SortedMutableTreeNode getNode(Workspace ws){
        return new WorkspaceNode(ws);
    }
    static public SortedMutableTreeNode getNode(WorkspaceFileAssociation wfa){
        return new WFANode(wfa);
    }
    static public SortedMutableTreeNode getNode(Job job){
        return new JobNode(job);
    }
    static public SortedMutableTreeNode getNode(String string){
        return new SortedMutableTreeNode(string);
    }
}
