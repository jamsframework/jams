/*
 * SimpleNode.java
 * Created on 05.11.2010, 15:12:14
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SimpleNode {

//    private Object userObject;
//    private ModelNode parent;
//    protected ArrayList<ModelNode> children = new ArrayList<ModelNode>();
//    private int type;
//
//    public SimpleNode(ComponentDescriptor cd) {
//        this.userObject = cd;
//        cd.setNode(this);
//    }
//
//    public void add(ModelNode child) {
//        children.add(child);
//        child.setParent(this);
//    }
//
//    public void add(int index, ModelNode child) {
//        children.add(index, child);
//        child.setParent(this);
//    }
//
//    public Enumeration<ModelNode> breathFirstEnum() {
//        return Collections.enumeration(breathFirstEnum(this));
//    }
//
//    private ArrayList<ModelNode> breathFirstEnum(ModelNode root) {
//
//        ArrayList<ModelNode> nodes = new ArrayList<ModelNode>();
//
//        nodes.add(root);
//        for (int i = 0; i < root.getChildCount(); i++) {
//            ModelNode child = root.getChildAt(i);
//            nodes.addAll(breathFirstEnum(child));
//        }
//        return nodes;
//    }
//
//    public ModelNode getChildAt(int i) {
//        return children.get(i);
//    }
//
//    public int getChildCount() {
//        return children.size();
//    }
//
//    public ModelNode getParent() {
//        return parent;
//    }
//
//    public Object getUserObject() {
//        return userObject;
//    }
//
//    public void remove() {
//
//        for (ModelNode child : children) {
//            child.remove();
//        }
//
//        Object o = getUserObject();
//        if (o instanceof ComponentDescriptor) {
//            ComponentDescriptor cd = (ComponentDescriptor) o;
//            cd.unregister();
//        }
//        getParent().removeChild(this);
//    }
//
//    public void removeChild(ModelNode child) {
//        children.remove(child);
//        child.setParent(null);
//    }
//
//    public void setParent(ModelNode parent) {
//        this.parent = parent;
//    }
//
//    public boolean isLeaf() {
//        if (getChildCount() == 0) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean isRoot() {
//        if (getParent() == null) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public int getType() {
//        return type;
//    }
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public int getIndex(TreeNode node) {
//        return children.indexOf(node);
//    }
//
//    public boolean getAllowsChildren() {
//        if (type == COMPONENT_TYPE) {
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    public Enumeration children() {
//        return Collections.enumeration(children);
//    }

}