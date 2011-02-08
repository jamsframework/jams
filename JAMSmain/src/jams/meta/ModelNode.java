/*
 * ModelNode.java
 * Created on 03.11.2010, 18:46:47
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
//public interface ModelNode extends TreeNode {
public class ModelNode extends DefaultMutableTreeNode {

    public static final int COMPONENT_TYPE = 0, CONTEXT_TYPE = 1, MODEL_TYPE = 2;
    private int type;

    public ModelNode(Object o) {
        super(o);
        if (ComponentDescriptor.class.isAssignableFrom(o.getClass())) {
            ((ComponentDescriptor) o).setNode(this);
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


//    void add(ModelNode child);
//
//    void add(int index, ModelNode child);
//
//    Enumeration<ModelNode> breathFirstEnum();
//
//    ModelNode getChildAt(int i);
//
//    int getChildCount();
//
//    ModelNode getParent();
//
//    void remove();
//
//    void removeChild(ModelNode child);
//
//    void setParent(ModelNode parent);
//
//    boolean isLeaf();
//
//    boolean isRoot();
//
//    int getType();
//
//    void setType(int type);
//
//    Object getUserObject();
}
