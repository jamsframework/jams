/*
 * JAMSTree.java
 * Created on 20. April 2006, 11:59
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.unijena.juice.gui.tree;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.unijena.juice.ComponentDescriptor;

public class JAMSTree extends JTree {
    
    private Insets autoscrollInsets = new Insets(20, 20, 20, 20); // insets
    
    public JAMSTree() {
        setAutoscrolls(true);
        setRootVisible(true);
        setShowsRootHandles(false);//to show the root icon
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); //set single selection for the Tree
        this.setSelectionRow(0);
        setCellRenderer(new JAMSTreeRenderer());
    }
    
    public void autoscroll(Point cursorLocation)  {
        Insets insets = getAutoscrollInsets();
        Rectangle outer = getVisibleRect();
        Rectangle inner = new Rectangle(outer.x+insets.left, outer.y+insets.top, outer.width-(insets.left+insets.right), outer.height-(insets.top+insets.bottom));
        if (!inner.contains(cursorLocation))  {
            Rectangle scrollRect = new Rectangle(cursorLocation.x-insets.left, cursorLocation.y-insets.top,	insets.left+insets.right, insets.top+insets.bottom);
            scrollRectToVisible(scrollRect);
        }
    }
    
    public boolean isPathEditable(TreePath path) {
        return false;
    }
    
    public Insets getAutoscrollInsets()  {
        return (autoscrollInsets);
    }
    
    public static JAMSNode makeDeepCopy(JAMSNode node, JAMSTree target) {
        
        JAMSNode copy = node.clone(target);
        ComponentDescriptor cd = (ComponentDescriptor) copy.getUserObject();
        for (Enumeration e = node.children(); e.hasMoreElements();) {
            copy.add(makeDeepCopy((JAMSNode)e.nextElement(), target));
        }
        return(copy);
    }
    
    class JAMSTreeRenderer extends DefaultTreeCellRenderer {
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            TreeNode node = (TreeNode) value;
            
            if (node instanceof JAMSNode) {
                JAMSNode jNode = (JAMSNode) node;
                setIcon(JAMSNode.NODE_ICON[jNode.getType()]);
            }
            return this;
        }
    }
    
    public void expandAll() {
        int row = 0;
        while (row < this.getRowCount()) {
            this.expandRow(row);
            row++;
        }
    }
    
    public void collapseAll() {
        int row = this.getRowCount()-1;
        while (row > 0) {
            this.collapseRow(row);
            row--;
        }
    }
}
