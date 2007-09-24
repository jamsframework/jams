package org.unijena.juice.tree;
import java.awt.*;
import java.util.Collections;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import org.unijena.jams.model.JAMSContext;
import org.unijena.juice.ContextReplaceDlg;
import org.unijena.juice.JUICE;

public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {
    
    private ContextReplaceDlg dlg = new ContextReplaceDlg(JUICE.getJuiceFrame());
    
    public DefaultTreeTransferHandler(JAMSTree tree, int action) {
        super(tree, action, true);
    }
    
    public boolean canPerformAction(JAMSTree target, JAMSNode draggedNode, int action, Point location) {
        
        JAMSNode targetRoot = (JAMSNode) target.getModel().getRoot();
        
        //nothing can be moved to a tree with lib root
        if (targetRoot.getType() == JAMSNode.LIBRARY_ROOT) {
            return false;
        }
        
        //package and library nodes can't be moved
        if (draggedNode.getType() == JAMSNode.PACKAGE_NODE || draggedNode.getType() == JAMSNode.LIBRARY_ROOT) {
            return false;
        }
        
        TreePath pathTarget = target.getPathForLocation(location.x, location.y);
        if (pathTarget == null) {
            target.setSelectionPath(null);
            return(false);
        }
        
/*        if (((JAMSNode)pathTarget.getLastPathComponent()).isLeaf()) { // or ((JAMSNode)pathTarget.getLastPathComponent()).getChildCount()==0
            target.setSelectionPath(null);
            return(false);
        }
 */
        if (((JAMSNode)pathTarget.getLastPathComponent()).getType() == JAMSNode.COMPONENT_NODE) { // or ((JAMSNode)pathTarget.getLastPathComponent()).getChildCount()==0
            target.setSelectionPath(null);
            return(false);
        }
        if(action == DnDConstants.ACTION_COPY) {
            target.setSelectionPath(pathTarget);
            return(true);
        } else if (action == DnDConstants.ACTION_MOVE) {
            JAMSNode parentNode =(JAMSNode)pathTarget.getLastPathComponent();
            if (draggedNode.isRoot() || (parentNode == draggedNode.getParent()) || (draggedNode.isNodeDescendant(parentNode))) {
                target.setSelectionPath(null);
                return(false);
            } else {
                target.setSelectionPath(pathTarget);
                return(true);
            }
        } else {
            target.setSelectionPath(null);
            return(false);
        }
    }
    
    public boolean executeDrop(JAMSTree target, JAMSNode draggedNode, JAMSNode newParentNode, Vector expandedStates, int action) {
        
        HashMap<String, HashSet<ComponentDescriptor>> pendingContexts = null;
        
        if (action == DnDConstants.ACTION_COPY) {
            
            JAMSNode newNode = target.makeDeepCopy(draggedNode, target);
            newNode.setType(draggedNode.getType());
            
            if (target instanceof ModelTree) {
                if (!fixPendingContexts(newNode, newParentNode)) {
                    return false;
                }
            }
            
            target.expandPath(new TreePath(newParentNode.getPath()));
            ((DefaultTreeModel)target.getModel()).insertNodeInto(newNode, newParentNode, newParentNode.getChildCount());
            TreePath treePath = new TreePath(newNode.getPath());
            int i = 0;
            for (Enumeration enumeration = newNode.depthFirstEnumeration(); enumeration.hasMoreElements(); i++) {
                JAMSNode element = (JAMSNode)enumeration.nextElement();
                TreePath path = new TreePath(element.getPath());
                if (((Boolean)expandedStates.get(i)).booleanValue()) {
                    target.expandPath(path);
                }
            }
            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);
            
            return true;
        }
        if (action == DnDConstants.ACTION_MOVE) {
            
            if (target instanceof ModelTree) {
                if (!fixPendingContexts(draggedNode, newParentNode)) {
                    return false;
                }
            }
            
            TreePath oldParentPath = new TreePath(((JAMSNode)draggedNode.getParent()).getPath());
            draggedNode.removeFromParent();
            target.expandPath(new TreePath(newParentNode.getPath()));
            ((DefaultTreeModel)target.getModel()).insertNodeInto(draggedNode, newParentNode, newParentNode.getChildCount());
            TreePath treePath = new TreePath(draggedNode.getPath());
            int i = 0;
            for (Enumeration enumeration = draggedNode.depthFirstEnumeration(); enumeration.hasMoreElements(); i++) {
                JAMSNode element = (JAMSNode)enumeration.nextElement();
                TreePath path = new TreePath(element.getPath());
                if (((Boolean)expandedStates.get(i)).booleanValue()) {
                    target.expandPath(path);
                }
            }
            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);
            return true;
        }
        return false;
    }
    
    private boolean fixPendingContexts(JAMSNode rootNode, JAMSNode parentNode) {
        
        JAMSNode node;
        ComponentDescriptor cd;
        HashSet<String> contexts = new HashSet<String>();
        HashMap<String, HashSet<ComponentDescriptor>> pendingContexts = new HashMap<String, HashSet<ComponentDescriptor>>();
        
        cd = (ComponentDescriptor) rootNode.getUserObject();
        
        Enumeration nodeEnum = rootNode.breadthFirstEnumeration();
        
        while (nodeEnum.hasMoreElements()) {
            node = (JAMSNode) nodeEnum.nextElement();
            cd = (ComponentDescriptor) node.getUserObject();
            
            if (JAMSContext.class.isAssignableFrom(cd.getClazz())) {
                contexts.add(cd.getName());
            }
            
            for (ComponentDescriptor.ComponentVar var : cd.getCVars().values()) {
                if (var.context != null) {
                    String contextName = var.context.getName();
                    if (!contexts.contains(contextName)) {
                        HashSet<ComponentDescriptor> components = pendingContexts.get(contextName);
                        if (components == null) {
                            components = new HashSet<ComponentDescriptor>();
                            pendingContexts.put(contextName, components);
                        }
                        components.add(cd);
                        //System.out.println(var.name + " references " + contextName);
                    }
                }
            }
        }
        
        //put new parent and all of its ancestors into vector for creating a select box
        Vector<String> ancestorNames = new Vector<String>();
        
        //put new parent and all of its ancestors into a hashmap for access by name
        HashMap<String, ComponentDescriptor> ancestors = new HashMap<String, ComponentDescriptor>();
        
        cd = (ComponentDescriptor) parentNode.getUserObject();
        ancestorNames.add(cd.toString());
        ancestors.put(cd.toString(), cd);
        
        JAMSNode ancestor = (JAMSNode) parentNode.getParent();
        while (ancestor != null) {
            cd = (ComponentDescriptor) ancestor.getUserObject();
            ancestorNames.add(cd.toString());
            ancestors.put(cd.toString(), cd);
            ancestor = (JAMSNode) ancestor.getParent();
        }
        
        String ancestorNameArray[] = ancestorNames.toArray(new String[ancestorNames.size()]);
        
        //sort pending contexts
        ArrayList<String> pendingContextList = new ArrayList<String>(pendingContexts.keySet());
        Collections.sort(pendingContextList);
        
        //iterate over all pending contexts
        for (String oldContextName : pendingContextList) {
            HashSet<ComponentDescriptor> components = pendingContexts.get(oldContextName);
            
            //open a dialog for specification of new context and get new context
            if (dlg.show(oldContextName, ancestorNameArray, components) == ContextReplaceDlg.CANCEL_OPTION) {
                return false;
            }
            ComponentDescriptor newContext = ancestors.get(dlg.getContext());
            
            //iterate over all components referencing pending contexts
            for (ComponentDescriptor component : components) {
                //iterate over all vars
                for (ComponentDescriptor.ComponentVar var : component.getCVars().values()) {
                    if (var.context != null) {
                        //again select vars that reference this pending context and connect to new (selected) context
                        if (var.context.getName().equals(oldContextName)) {
                            var.context = newContext;
                        }
                    }
                }
            }
        }
        return true;
    }
}
