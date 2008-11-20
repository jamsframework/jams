/*
 * DSTree.java
 * Created on 19. November 2008, 17:58
 *
 * This file is part of JAMS
 * Copyright (C) 2008 FSU Jena
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
package reg.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import jams.model.JAMSComponent;
import jams.workspace.VirtualWorkspace;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author S. Kralisch
 */
public class DSTree extends JAMSTree {

    private static final String ROOT_NAME = "Datenspeicher",  INPUT_NAME = "Eingabedaten",  OUTPUT_NAME = "Ausgabedaten";
    private JPopupMenu popup;
    private VirtualWorkspace workspace;
    private DSTreeNode root;
    private NodeObservable nodeObservable = new NodeObservable();

    public DSTree() {
        super();

        setEditable(false);
        root = new DSTreeNode(ROOT_NAME, DSTreeNode.IO_ROOT);
        this.setModel(new DefaultTreeModel(root));

        JMenuItem detailItem = new JMenuItem("Zeige Daten");
        detailItem.setAccelerator(KeyStroke.getKeyStroke('D'));
        detailItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                displayDSData();
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
                    displayDSData();
                }
            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                displayDSInfo();
            }
        });

        this.setVisible(false);
    }

    private void showPopup(MouseEvent evt) {
        TreePath p = this.getClosestPathForLocation(evt.getX(), evt.getY());
        this.setSelectionPath(p);
        DSTreeNode node = (DSTreeNode) this.getLastSelectedPathComponent();
        if ((node != null) && ((node.getType() == DSTreeNode.INPUT_DS) || (node.getType() == DSTreeNode.OUTPUT_DS))){
            popup.show(this, evt.getX(), evt.getY());
        }
    }

    private void displayDSInfo() {
        System.out.println("Show Info");
        nodeObservable.setNode((DSTreeNode) getSelectionPath().getLastPathComponent());
    }

    private void displayDSData() {
        System.out.println("Show Data");
    }

    public void update(VirtualWorkspace workspace) {

        this.setVisible(false);
        this.workspace = workspace;
        createIOTree();
        //this.expandRow(0);
        this.expandAll();
        this.setVisible(true);

    /*
    libsArray = JAMSTools.toArray(libFileNames, ";");
    this.setModel(null);

    contextCount = 0;
    componentCount = 0;
    JUICE.setStatusText(JUICE.resources.getString("Loading_Libraries"));
    this.setVisible(false);
    DSTreeNode root = LibTree.this.createLibTree(LibTree.this.libsArray);
    this.setModel(new DefaultTreeModel(root));
    this.collapseAll();
    this.setVisible(true);
    JUICE.setStatusText(JUICE.resources.getString("Contexts:") + contextCount + " " + JUICE.resources.getString("Components:") + componentCount);
     */

    }

    private DSTreeNode createIOTree() {

        DSTreeNode inputRoot = new DSTreeNode(INPUT_NAME, DSTreeNode.INPUT_ROOT);
        Set<String> inIDs = workspace.getInputDataStoreIDs();
        List<String> inIDList = new ArrayList<String>(inIDs);
        Collections.sort(inIDList);
        for (String id : inIDList) {
            DSTreeNode dsNode = new DSTreeNode(id, DSTreeNode.INPUT_DS);
            inputRoot.add(dsNode);
        }

        DSTreeNode outputRoot = new DSTreeNode(OUTPUT_NAME, DSTreeNode.OUTPUT_ROOT);
        Set<String> outIDs = workspace.getOutputDataStoreIDs();
        List<String> outIDList = new ArrayList<String>(outIDs);
        Collections.sort(outIDList);
        for (String id : outIDList) {
            DSTreeNode dsNode = new DSTreeNode(id, DSTreeNode.OUTPUT_DS);
            outputRoot.add(dsNode);
        }
        root.removeAllChildren();
        root.add(inputRoot);
        root.add(outputRoot);

        /*
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
         */

        return root;
    }

    private DSTreeNode createJARNode(String jar, ClassLoader loader) {

        //System.out.println("loading " + jar);
        DSTreeNode jarRoot = new DSTreeNode(jar, DSTreeNode.INPUT_DS);
        ArrayList<Class> components = new ArrayList<Class>();
        DSTreeNode compNode;
        String jarName = "", clazzName = "", clazzFullName = "";

        try {
            JarFile jfile = new JarFile(jar);
            File file = new File(jar);
            //URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
            jarName = file.getCanonicalFile().getName();
            //jarRoot = new DSTreeNode(jarName, DSTreeNode.PACKAGE_NODE);

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
                    } catch (NoClassDefFoundError ncdfe) {
                        //LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Missing class while loading component " + clazzFullName +
                        //        " in archive " + jarName + "!", "Error while loading archive");
                    } catch (Exception e) {
                        // other exception like e.g. java.lang.SecurityException
                        // won't be handled since they hopefully don't occur
                        // while loading JARs containing JAMS components
                    }
                }
            }

            String oldPackage = "", newPackage;
            DSTreeNode packageNode = null;
            for (Class clazz : components) {
                newPackage = clazz.getPackage().getName();
                if (!newPackage.equals(oldPackage)) {
                    packageNode = new DSTreeNode(newPackage, DSTreeNode.INPUT_DS);
                    jarRoot.add(packageNode);
                    oldPackage = newPackage;
                }

                clazzName = clazz.getSimpleName();
                clazzFullName = clazz.getName();

                if (!(clazzName.equals("JAMSComponent") || clazzName.equals("JAMSContext_") || clazzName.equals("JAMSGUIComponent") || clazzName.equals("JAMSModel"))) {
                    /*
                    try {

                    ComponentDescriptor no = new ComponentDescriptor(clazz, this);

                    if (JAMSContext.class.isAssignableFrom(clazz)) {
                    compNode = new DSTreeNode(no, DSTreeNode.CONTEXT_NODE);
                    contextCount++;
                    } else {
                    compNode = new DSTreeNode(no, DSTreeNode.COMPONENT_NODE);
                    componentCount++;
                    }

                    packageNode.add(compNode);

                    } catch (NoClassDefFoundError ncdfe) {

                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Missing_class_while_loading_component_") + clazzFullName +
                    JUICE.resources.getString("_in_archive_") + jarName + "\"!", JUICE.resources.getString("Error_while_loading_archive"));

                    }*/
                }
            }


        } catch (IOException ioe) {

            //LHelper.showErrorDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("File_") + jar + JUICE.resources.getString("_could_not_be_loaded."), JUICE.resources.getString("Error_while_loading_archive"));
            jarRoot = null;

        }

        if (components.size() > 0) {
            return jarRoot;
        } else {
            return null;
        }
    }

    public void addObserver(Observer o) {
        nodeObservable.addObserver(o);
    }

    /**
     * @return the workspace
     */
    public VirtualWorkspace getWorkspace() {
        return workspace;
    }

    private class NodeObservable extends Observable {
        DSTreeNode node;

        public void setNode(DSTreeNode node) {
            this.node = node;
            this.setChanged();
            notifyObservers();
        }

        @Override
        public void notifyObservers(Object arg) {
            super.notifyObservers(node);
        }

    }
}
