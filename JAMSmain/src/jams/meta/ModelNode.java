/*
 * ModelNode.java
 * Created on 03.11.2010, 18:46:47
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

import jams.JAMSException;
import jams.JAMSLogging;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        JAMSLogging.registerLogger(Logger.getLogger(this.getClass().getName()));
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

    @SuppressWarnings("unchecked")
    public ModelNode clone(ComponentCollection target, boolean deepCopy, Map<ContextDescriptor, ContextDescriptor> contextMap) {

        ModelNode nodeCopy = null;
//        try {
        //@TODO: proper handling
        ComponentDescriptor cd = (ComponentDescriptor) this.getUserObject();
        ComponentDescriptor cdCopy = cd.cloneNode();
        nodeCopy = new ModelNode(cdCopy);
        nodeCopy.setType(this.getType());

        cdCopy.register(target);

        if (cd instanceof ContextDescriptor) {
            contextMap.put((ContextDescriptor) cd, (ContextDescriptor) cdCopy);
        }

//        } catch (JAMSException ex) {
//            Logger.getLogger(ModelNode.class.getName()).logger(Level.SEVERE, ex.getMessage(), ex);
//        }

        if (deepCopy && (nodeCopy != null)) {
            for (Enumeration<ModelNode> e = this.children(); e.hasMoreElements();) {

                ModelNode childNode = e.nextElement().clone(target, true, contextMap);
                nodeCopy.add(childNode);

                ComponentDescriptor childCd = (ComponentDescriptor) childNode.getUserObject();
                for (ComponentField field : childCd.getComponentFields().values()) {
                    ContextDescriptor context = field.getContext();
                    if ((context != null) && (contextMap.containsKey(context))) {
                        try {
                            ContextDescriptor newContext = contextMap.get(context);
                            field.linkToAttribute(newContext, field.getAttribute());
                        } catch (JAMSException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }

                }

            }
        }

        return nodeCopy;
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
