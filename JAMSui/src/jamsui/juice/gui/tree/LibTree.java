/*
 * LibTree.java
 * Created on 19. April 2006, 17:58
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

import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jams.gui.tools.GUIHelper;
import jams.model.JAMSComponent;
import jams.model.JAMSContext;
import javax.swing.KeyStroke;
import jamsui.juice.JUICE;
import jams.JAMS;
import jams.JAMSException;
import jams.meta.ComponentDescriptor;
import jams.tools.StringTools;
import jams.meta.ComponentCollection;
import jams.meta.ContextDescriptor;
import jamsui.juice.gui.ComponentInfoDlg;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author S. Kralisch
 */
public class LibTree extends JAMSTree {

    private static final String ROOT_NAME = JAMS.i18n("Model_Components");
    private JPopupMenu popup;
    private String[] libsArray;
    private int contextCount, componentCount;
    private int i, maxClasses;

    public LibTree(ComponentCollection componentCollection, int maxClasses) {
        super(componentCollection);

        setEditable(false);
        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY);
        this.maxClasses = maxClasses;

        JMenuItem detailItem = new JMenuItem(JAMS.i18n("Show_Metadata..."));
        detailItem.setAccelerator(KeyStroke.getKeyStroke('M'));
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
        if ((node == null) || !(node.getUserObject() instanceof ComponentDescriptor)) {
            return;
        }
        ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
        ComponentInfoDlg.displayMetadataDlg((JFrame) this.getTopLevelAncestor(), cd.getClazz());

    }

    public void update(String libFileNames) {

        libsArray = StringTools.toArray(libFileNames, ";");

        contextCount = 0;
        componentCount = 0;
        JUICE.setStatusText(JAMS.i18n("Loading_Libraries"));
        this.setVisible(false);

        JAMSNode rootNode;

        if ((getModel() != null) && (getModel().getRoot() instanceof JAMSNode)) {
            rootNode = (JAMSNode) getModel().getRoot();
            rootNode.remove();
            setModel(null);
        }

        rootNode = createLibTree(libsArray);
        this.setModel(new DefaultTreeModel(rootNode));
        //this.collapseAll();
        this.setVisible(true);
        JUICE.setStatusText(JAMS.i18n("Contexts:") + contextCount + " " + JAMS.i18n("Components:") + componentCount);

    }

    private JAMSNode createLibTree(String[] libsArray) {

        JAMSNode root = new JAMSNode(ROOT_NAME, JAMSNode.LIBRARY_TYPE, this);
        JAMSNode jarNode;

        i = 1;

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
                            root.add((DefaultMutableTreeNode) jarNode);
                        }
                    }
                }
            } else {
                jarNode = createJARNode(file.toString(), JUICE.getLoader());
                if (jarNode != null) {
                    root.add((DefaultMutableTreeNode) jarNode);
                }
            }

        }

        return root;
    }

    private JAMSNode createJARNode(String jar, ClassLoader loader) {

        if (i >= maxClasses) {
            return null;
        }

        JAMSNode jarRoot = new JAMSNode(jar, JAMSNode.ARCHIVE_TYPE, this);
        ArrayList<Class> components = new ArrayList<Class>();
        JAMSNode compNode;
        String jarName = "", clazzName = "", clazzFullName = "";

        try {
            JarFile jfile = new JarFile(jar);
            File file = new File(jar);
            //URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
            jarName = file.getCanonicalFile().getName();
            //jarRoot = new JAMSNode(jarName, JAMSNode.PACKAGE_TYPE);

            Enumeration jarentries = jfile.entries();
            while (jarentries.hasMoreElements()) {
                String entry = jarentries.nextElement().toString();
//                if (entry.startsWith("org/geotools")) {
//                    continue;
//                }
                if ((entry.endsWith(".class"))) {
                    String classString = entry.substring(0, entry.length() - 6);
                    classString = classString.replaceAll("/", ".");

                    try {

                        if (i >= maxClasses) {
                            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), MessageFormat.format(JAMS.i18n("To_many_classes_error"), i), JAMS.i18n("Error_while_loading_archive"));
                            break;
                        }
                        i++;

                        // try to load the class and check if it's a subclass of JAMSComponent
                        Class<?> clazz = loader.loadClass(classString);

                        if (JAMSComponent.class.isAssignableFrom(clazz)) {
                            components.add(clazz);
                        }

                    } catch (ClassNotFoundException cnfe) {

                        JUICE.getLogger().log(Level.SEVERE, JAMS.i18n("Error_while_loading_archive_") + jarName + "\"" + JAMS.i18n("_(class_") + classString
                                + JAMS.i18n("_could_not_be_found)!"), cnfe);


                    } catch (NoClassDefFoundError ncdfe) {
                        // loading classes can cause a lot of NoClassDefFoundError
                        // exceptions, they are caught silently!
                    } catch (UnsupportedClassVersionError ucve) {
                        JUICE.getLogger().log(Level.SEVERE, MessageFormat.format(JAMS.i18n("ClassVersionErrorWhileLoadingComponentLib"), classString + " (" + jarName + ")") 
                                + "\n" + ucve.getMessage(), ucve);
                    } catch (Throwable e) {
                        // other exception like e.g. java.lang.SecurityException
                        // won't be handled since they hopefully don't occur
                        // while loading JARs containing JAMS components
                        JUICE.getLogger().log(Level.SEVERE, JAMS.i18n("Error_while_loading_archive_") + jarName + "\"" + JAMS.i18n("_(class_") + classString
                                + JAMS.i18n("_could_not_be_loaded)!") + "\n" + e.getMessage(), e);
                    }
                }
            }

            String oldPackage = "", newPackage = "";
            JAMSNode packageNode = null;
            for (Class clazz : components) {

                if (clazz.getPackage() != null) {
                    newPackage = clazz.getPackage().getName();
                } else {
                    newPackage = "default package";
                }

                if (!newPackage.equals(oldPackage)) {
                    packageNode = new JAMSNode(newPackage, JAMSNode.PACKAGE_TYPE, this);
                    oldPackage = newPackage;
                }

                clazzName = clazz.getSimpleName();
                clazzFullName = clazz.getName();

                if (!(clazzName.equals("JAMSComponent") || clazzName.equals("JAMSContext_") || clazzName.equals("JAMSGUIComponent") || clazzName.equals("JAMSModel"))) {

                    try {

                        ComponentDescriptor no;
                        if (JAMSContext.class.isAssignableFrom(clazz)) {
                            no = new ContextDescriptor(clazz, null, getComponentCollection());
                        } else {
                            no = new ComponentDescriptor(clazz, null, getComponentCollection());
                        }

                        no.addObserver(new Observer() {
                            public void update(Observable o, Object arg) {
                                LibTree.this.updateUI();
                            }
                        });

                        if (JAMSContext.class.isAssignableFrom(clazz)) {
                            compNode = new JAMSNode(no, JAMSNode.CONTEXT_TYPE, this);
                            contextCount++;
                        } else {
                            compNode = new JAMSNode(no, JAMSNode.COMPONENT_TYPE, this);
                            componentCount++;
                        }

                        no.setNode(compNode);

                        packageNode.add((DefaultMutableTreeNode) compNode);

                    } catch (NoClassDefFoundError ncdfe) {

                        GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("Missing_class_while_loading_component_") + clazzFullName
                                + JAMS.i18n("_in_archive_") + jarName + "\"!", JAMS.i18n("Error_while_loading_archive"));
                        Logger.getLogger(LibTree.class.getName()).log(Level.SEVERE, null, ncdfe);

                    } catch (JAMSException jex) {
                        GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), jex.getMessage(), jex.getHeader());
                        Logger.getLogger(LibTree.class.getName()).log(Level.SEVERE, null, jex);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

                if (packageNode.getChildCount() > 0) {
                    jarRoot.add((DefaultMutableTreeNode) packageNode);
                }
            }


        } catch (IOException ioe) {

            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("File_") + jar + JAMS.i18n("_could_not_be_loaded."), JAMS.i18n("Error_while_loading_archive"));
            jarRoot = null;

        }

        if (jarRoot.getChildCount() > 0) {
            return jarRoot;
        } else {
            return null;
        }
    }
}
