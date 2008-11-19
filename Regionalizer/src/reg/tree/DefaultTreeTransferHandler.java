package reg.tree;

import java.awt.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.util.Enumeration;
import java.util.Vector;

public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler {

    public DefaultTreeTransferHandler(JAMSTree tree, int action) {
        super(tree, action, true);
    }

    public boolean canPerformAction(JAMSTree target, JAMSNode draggedNode, int action, Point location) {
        return false;
/*
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
            return (false);
        }

        /*        if (((JAMSNode)pathTarget.getLastPathComponent()).isLeaf()) { // or ((JAMSNode)pathTarget.getLastPathComponent()).getChildCount()==0
        target.setSelectionPath(null);
        return(false);
        }
         */
/*
        if (((JAMSNode) pathTarget.getLastPathComponent()).getType() == JAMSNode.COMPONENT_NODE) { // or ((JAMSNode)pathTarget.getLastPathComponent()).getChildCount()==0
//            target.setSelectionPath(null);
//            return(false);
        }
        if (action == DnDConstants.ACTION_COPY) {
            target.setSelectionPath(pathTarget);
            return (true);
        } else if (action == DnDConstants.ACTION_MOVE) {
            JAMSNode parentNode = (JAMSNode) pathTarget.getLastPathComponent();
            if (draggedNode.isRoot() || (parentNode == draggedNode.getParent()) || (draggedNode.isNodeDescendant(parentNode))) {
                target.setSelectionPath(null);
                return (false);
            } else {
                target.setSelectionPath(pathTarget);
                return (true);
            }
        } else {
            target.setSelectionPath(null);
            return (false);
        }*/
    }

    public boolean executeDrop(JAMSTree target, JAMSNode draggedNode, JAMSNode newParentNode, Vector expandedStates, int action) {
/*
        int position = 0;

        if (newParentNode.getType() == JAMSNode.COMPONENT_NODE) {
            JAMSNode siblingNode = newParentNode;
            newParentNode = (JAMSNode) newParentNode.getParent();
            position = newParentNode.getIndex(siblingNode);
            if (draggedNode.getParent().getIndex(draggedNode) < position) {
                position--;
            }
        } else {
            position = newParentNode.getChildCount();
        }

        if (action == DnDConstants.ACTION_MOVE) {

            target.saveExpandedState(new TreePath(target.getModel().getRoot()));

            draggedNode.removeFromParent();
            target.expandPath(new TreePath(newParentNode.getPath()));

            ((DefaultTreeModel) target.getModel()).insertNodeInto(draggedNode, newParentNode, position);

            TreePath treePath = new TreePath(draggedNode.getPath());

            int i = 0;
            for (Enumeration enumeration = draggedNode.depthFirstEnumeration(); enumeration.hasMoreElements(); i++) {
                JAMSNode element = (JAMSNode) enumeration.nextElement();
                TreePath path = new TreePath(element.getPath());
                if (((Boolean) expandedStates.get(i)).booleanValue()) {
                    target.expandPath(path);
                }
            }

            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);

            TreePath newtreePath = new TreePath(draggedNode.getPath());
            target.scrollPathToVisible(newtreePath);
            target.setSelectionPath(newtreePath);


            return true;
        }*/
        return false;
    }

}
