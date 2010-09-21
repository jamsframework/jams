/*
 * JAMSNode.java
 * Created on 7. April 2006, 21:55
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
package jams.meta;

import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
public class JAMSNode {

    public final static int MODEL_ROOT = 0;
    public final static int LIBRARY_ROOT = 1;
    public final static int CONTEXT_NODE = 2;
    public final static int COMPONENT_NODE = 3;
    public final static int PACKAGE_NODE = 4;
    public final static int ARCHIVE_NODE = 5;

    private Object userObject;
    private JAMSNode parent;
    private ArrayList<JAMSNode> children = new ArrayList<JAMSNode>();
    private int type;

    public JAMSNode(Object o) {
        this.userObject = o;
    }

    public void add(JAMSNode child) {
        children.add(child);
        child.setParent(this);
    }

    public int getChildCount() {
        return children.size();
    }

    public JAMSNode getChildAt(int i) {
        return children.get(i);
    }

    public void removeChild(JAMSNode child) {
        children.remove(child);
        child.setParent(null);
    }

    public void removeFromParent() {
        getParent().removeChild(this);
    }

    public Object getUserObject() {
        return userObject;
    }

    public void remove() {

        ArrayList<JAMSNode> children = new ArrayList<JAMSNode>();
        for (int i = 0; i < this.getChildCount(); i++) {
            children.add((JAMSNode) this.getChildAt(i));
        }

        for (JAMSNode child : children) {
            child.remove();
        }

        Object o = getUserObject();
        if (o instanceof ComponentDescriptor) {
            ComponentDescriptor cd = (ComponentDescriptor) o;
            cd.unregister();
        }
        this.removeFromParent();
    }

    /**
     * @return the parent
     */
    public JAMSNode getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(JAMSNode parent) {
        this.parent = parent;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
