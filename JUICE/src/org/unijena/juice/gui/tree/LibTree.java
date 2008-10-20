/*
 * LibTree.java
 * Created on 19. April 2006, 17:58
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

import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jams.JAMSTools;
import jams.gui.LHelper;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import org.unijena.juice.ComponentDescriptor;
import org.unijena.juice.gui.ComponentInfoPanel;
import org.unijena.juice.JUICE;

/**
 *
 * @author S. Kralisch
 */
public class LibTree extends JAMSTree {

    private static final String ROOT_NAME = "Model Components";
    private HashMap<Class, JDialog> compViewDlgs = new HashMap<Class, JDialog>();
    private JPopupMenu popup;
    private String[] libsArray;
    private int contextCount,  componentCount;

    public LibTree() {
        super();

        setEditable(false);
        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY);

        JMenuItem detailItem = new JMenuItem("Show details");
        detailItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                displayComponentDlg();
            }
        });
        popup = new JPopupMenu();
        popup.add(detailItem);

        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    showPopup(evt);
                }
            }

            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    displayComponentDlg();
                }
            }
        });
    }

    private void showPopup(MouseEvent evt) {
        TreePath p = this.getClosestPathForLocation(evt.getX(), evt.getY());
        this.setSelectionPath(p);
        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();
        if (node != null) {

            try {
                Class<?> clazz = ((ComponentDescriptor) node.getUserObject()).getClazz();
                if (clazz != null) {
                    popup.show(this, evt.getX(), evt.getY());
                }
            } catch (ClassCastException cce) {
            }
        }
    }

    private void displayComponentDlg() {

        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        try {
            Class<?> clazz = ((ComponentDescriptor) node.getUserObject()).getClazz();
            if (clazz != null) {

                if (compViewDlgs.containsKey(clazz)) {
                    compViewDlgs.get(clazz).setVisible(true);
                    return;
                }

                JDialog compViewDlg = new JDialog((JFrame) this.getTopLevelAncestor());
                compViewDlg.setLocationByPlatform(true);
                compViewDlg.setTitle(clazz.getCanonicalName());

                compViewDlgs.put(clazz, compViewDlg);
                compViewDlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                ComponentInfoPanel compView = new ComponentInfoPanel();
                compViewDlg.add(new JScrollPane(compView));

                JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
                if (jcd != null) {
                    compView.update(clazz.getCanonicalName(), jcd);
                } else {
                    compView.reset(clazz.getCanonicalName());
                }

                compView.update(clazz.getFields());

                compViewDlg.setPreferredSize(new Dimension(450, 600));
                compViewDlg.pack();
                compViewDlg.setVisible(true);
            }
        } catch (ClassCastException cce) {
        }

    }

    public void update(String libFileNames) {

        libsArray = JAMSTools.toArray(libFileNames, ";");
        this.setModel(null);

        contextCount = 0;
        componentCount = 0;
        JUICE.setStatusText("Loading libraries...");
        this.setVisible(false);
        JAMSNode root = LibTree.this.createLibTree(LibTree.this.libsArray);
        this.setModel(new DefaultTreeModel(root));
        this.collapseAll();
        this.setVisible(true);
        JUICE.setStatusText("Contexts:" + contextCount + " Components:" + componentCount);

    }

    private JAMSNode createLibTree(String[] libsArray) {

        JAMSNode root = new JAMSNode(ROOT_NAME, JAMSNode.LIBRARY_ROOT);
        JAMSNode jarNode;

        for (int i = 0; i < libsArray.length; i++) {
            File file = new File(libsArray[i]);

            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                File[] f = file.listFiles();
                for (int j = 0; j < f.length; j++) {
                    if (f[j].getName().endsWith(".jar")) {
                        jarNode = createJARNode(f[j].toString(), JUICE.getLoader());
                        if (jarNode != null) {
                            root.add(jarNode);
                        }
                    }
                }
            } else {
                jarNode = createJARNode(file.toString(), JUICE.getLoader());
                if (jarNode != null) {
                    root.add(jarNode);
                }
            }

        }

        /*
        
        
        while (stok.hasMoreTokens()) {
        
        jar = stok.nextToken();
        jarNode = createJARNode(jar, loader);
        if (jarNode != null)
        root.add(jarNode);
        }
         */
        return root;
    }

    private JAMSNode createJARNode(String jar, ClassLoader loader) {

        //System.out.println("loading " + jar);
        JAMSNode jarRoot = new JAMSNode(jar, JAMSNode.PACKAGE_NODE);
        ArrayList<Class> components = new ArrayList<Class>();
        JAMSNode compNode;
        String jarName = "", clazzName = "", clazzFullName = "";

        try {
            JarFile jfile = new JarFile(jar);
            File file = new File(jar);
            //URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
            jarName = file.getCanonicalFile().getName();
            //jarRoot = new JAMSNode(jarName, JAMSNode.PACKAGE_NODE);

            Enumeration jarentries = jfile.entries();
            while (jarentries.hasMoreElements()) {
                String entry = jarentries.nextElement().toString();
                if ((entry.endsWith(".class"))) {
                    String classString = entry.substring(0, entry.length() - 6);
                    classString = classString.replaceAll("/", ".");

                    try {

                        // try to load the class and check if it's a subclass of JAMSComponent
                        Class<?> clazz = loader.loadClass(classString);

                        if (JAMSComponent.class.isAssignableFrom(clazz)) {
                            components.add(clazz);
                        }

                    } catch (ClassNotFoundException cnfe) {

                        LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Error while loading archive " + jarName + " (class " + classString +
                                " could not be found)!", "Error while loading archive");

                    } catch (NoClassDefFoundError ncdfe) {
                        //System.out.println("failed: " + classString);
                        //LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Missing class while loading component " + clazzFullName +
                        //        " in archive " + jarName + "!", "Error while loading archive");
                    }
                }
            }

            String oldPackage = "", newPackage;
            JAMSNode packageNode = null;
            for (Class clazz : components) {
                newPackage = clazz.getPackage().getName();
                if (!newPackage.equals(oldPackage)) {
                    packageNode = new JAMSNode(newPackage, JAMSNode.PACKAGE_NODE);
                    jarRoot.add(packageNode);
                    oldPackage = newPackage;
                }

                clazzName = clazz.getSimpleName();
                clazzFullName = clazz.getName();

                if (!(clazzName.equals("JAMSComponent") || clazzName.equals("JAMSContext_") || clazzName.equals("JAMSGUIComponent") || clazzName.equals("JAMSModel"))) {

                    try {

                        ComponentDescriptor no = new ComponentDescriptor(clazz, this);

                        if (JAMSContext.class.isAssignableFrom(clazz)) {
                            compNode = new JAMSNode(no, JAMSNode.CONTEXT_NODE);
                            contextCount++;
                        } else {
                            compNode = new JAMSNode(no, JAMSNode.COMPONENT_NODE);
                            componentCount++;
                        }

                        packageNode.add(compNode);

                    } catch (NoClassDefFoundError ncdfe) {

                        LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Missing class while loading component " + clazzFullName +
                                " in archive " + jarName + "!", "Error while loading archive");

                    }
                }
            }


        } catch (IOException ioe) {

            LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Could not open file " + jar + "!", "Error while loading archive");
            jarRoot = null;

        }

        if (components.size() > 0) {
            return jarRoot;
        } else {
            return null;
        }
    }
}
