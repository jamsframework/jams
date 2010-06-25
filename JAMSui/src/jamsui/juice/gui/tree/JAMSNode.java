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
package jamsui.juice.gui.tree;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import jamsui.juice.ComponentDescriptor;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author S. Kralisch
 */
public class JAMSNode extends DefaultMutableTreeNode {

    public final static int MODEL_ROOT = 0;
    public final static int LIBRARY_ROOT = 1;
    public final static int CONTEXT_NODE = 2;
    public final static int COMPONENT_NODE = 3;
    public final static int PACKAGE_NODE = 4;
    public final static int ARCHIVE_NODE = 5;
    static int ICON_WIDTH = 16;
    static int ICON_HEIGHT = 16;
    static Icon[] NODE_ICON = {
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/World_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Component_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Folder_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Package_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH))
    };
    private int type = 0;
    private JAMSTree tree;
    private Observer observer;

    public JAMSNode(Object o, int type, JAMSTree tree) {
        this(o, tree);
        this.setType(type);
    }

    public JAMSNode(Object o, JAMSTree tree) {
        super(o);
        this.tree = tree;

        if (o instanceof ComponentDescriptor) {

            ComponentDescriptor cd = (ComponentDescriptor) o;
            observer = new Observer() {

                public void update(Observable o, Object arg) {
                    JAMSNode.this.tree.updateUI();
                }
            };
            cd.addObserver(observer);

        }
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
            this.removeObserver();
        }
        this.removeFromParent();
    }

    public void removeObserver() {

        if (observer == null) {
            return;
        }

        Object o = this.getUserObject();
        if (o instanceof ComponentDescriptor) {

            ComponentDescriptor cd = (ComponentDescriptor) o;
            cd.deleteObserver(observer);
            observer = null;

        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public JAMSNode clone(JAMSTree target) {

        ComponentDescriptor cd = ((ComponentDescriptor) this.getUserObject()).clone(target.getComponentCollection());
        JAMSNode clone = new JAMSNode(cd, this.getType(), target);
        return clone;
    }
}
