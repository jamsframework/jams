/*
 * JAMSNode.java
 * Created on 7. April 2006, 21:55
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui.tree;

import jams.JAMSException;
import jams.meta.ComponentDescriptor;
import jams.meta.ModelNode;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author S. Kralisch
 */
public class JAMSNode extends ModelNode {

    public final static int LIBRARY_TYPE = 3;
    public final static int PACKAGE_TYPE = 4;
    public final static int ARCHIVE_TYPE = 5;
    static final int ICON_WIDTH = 16;
    static final int ICON_HEIGHT = 16;
    static Icon[] NODE_ICON = {
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Component_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/Context_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
        new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/World_s.png")).getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)),
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

        JAMSNode clone = null;
        try {
            //@TODO: proper handling
            ComponentDescriptor cd = ((ComponentDescriptor) this.getUserObject()).cloneNode();
            clone = new JAMSNode(cd, this.getType(), target);
            cd.register(target.getComponentCollection());
        } catch (JAMSException ex) {
            Logger.getLogger(JAMSNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clone;
    }
}
