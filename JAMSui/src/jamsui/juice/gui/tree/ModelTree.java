/*
 * ModelTree.java
 * Created on 20. April 2006, 11:53
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

import jams.JAMS;
import jams.JAMSException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jams.gui.tools.GUIHelper;
import jams.meta.ComponentDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.ModelIO;
import jams.meta.ModelNode;
import jams.meta.NodeFactory;
import jams.model.JAMSContext;
import jamsui.juice.JUICE;
import jamsui.juice.gui.ComponentInfoDlg;
import javax.swing.JFrame;
import jamsui.juice.gui.ModelView;
import org.w3c.dom.Document;

/**
 *
 * @author S. Kralisch
 */
public class ModelTree extends JAMSTree {

    private ModelView view;
    private JPopupMenu popup;
    private boolean smartExpand = true;
    private ModelIO modelIO;

    public ModelTree(ModelView view, Document modelDoc) {
        super(view.getModelDescriptor());

        setEditable(true);

        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.view = view;
        this.modelIO = new ModelIO(new NodeFactory() {

            @Override
            public ModelNode createNode(ComponentDescriptor cd) {
                JAMSNode node = new JAMSNode(cd, ModelTree.this);
//                cd.setNode(node);
                return node;
            }
        });
        updateModelTree(modelDoc);

        addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                displayComponentInfo();
            }
        });

        JMenuItem toggleEnabledItem = new JMenuItem(JAMS.i18n("Toggle_enable"));
        toggleEnabledItem.setAccelerator(KeyStroke.getKeyStroke('E'));
        toggleEnabledItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                toggleEnable();
            }
        });

        JMenuItem showMetadataItem = new JMenuItem(JAMS.i18n("Show_Metadata..."));
        showMetadataItem.setAccelerator(KeyStroke.getKeyStroke('M'));
        showMetadataItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showMetaData();
            }
        });

        JMenuItem deleteItem = new JMenuItem(JAMS.i18n("Delete"));
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteNode();
            }
        });

        JMenuItem moveUpItem = new JMenuItem(JAMS.i18n("Move_up"));
        moveUpItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        moveUpItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                moveUpNode();
            }
        });
        JMenuItem moveDownItem = new JMenuItem(JAMS.i18n("Move_down"));
        moveDownItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        moveDownItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                moveDownNode();
            }
        });

        popup = new JPopupMenu();
        popup.add(toggleEnabledItem);
        popup.add(showMetadataItem);
        popup.add(deleteItem);
        popup.add(moveUpItem);
        popup.add(moveDownItem);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (!evt.isControlDown()) {
                    ModelTree.this.setSelectionPath(null);
                }                    
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    showPopup(evt);
                }
            }
        });

        addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_DELETE:
                        deleteNode();
                        break;
                    case '-':
                        moveUpNode();
                        break;
                    case '+':
                        moveDownNode();
                        break;
                    case 'E':
                        toggleEnable();
                        break;
                }
            }
        });
    }

    private void deleteNode() {

        if (this.getSelectionPaths() == null) {
            return;
        }

        for (TreePath path : this.getSelectionPaths()) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();

            int result = GUIHelper.showYesNoDlg(JUICE.getJuiceFrame(),
                    JAMS.i18n("Really_delete_component_")
                    + node.getUserObject().toString()
                    + JAMS.i18n("Really_delete_component_2"), JAMS.i18n("Deleting_component"));
            if (result == JOptionPane.YES_OPTION) {
                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
                node.remove();
                this.updateUI();
            }
        }

        this.setSelectionPath(null);
    }

    private void toggleEnable() {
        if (this.getSelectionPaths() == null) {
            return;
        }

        for (TreePath path : this.getSelectionPaths()) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
            cd.setEnabled(!cd.isEnabled());
            this.updateUI();
        }
    }

    private void showMetaData() {
        if (this.getSelectionPaths() == null) {
            return;
        }

        for (TreePath path : this.getSelectionPaths()) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
            ComponentInfoDlg.displayMetadataDlg((JFrame) this.getTopLevelAncestor(), cd.getClazz());
        }
    }

    private void moveUpNode() {

        int i, j;
        TreePath[] paths = this.getSelectionPaths();
        if (paths == null) {
            return;
        }

        int[] index = new int[paths.length];

        i = 0;
        for (TreePath path : paths) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();
            index[i] = parent.getIndex(node);
            i++;
        }

        i = index.length - 1;
        for (int k = paths.length - 1; k >= 0; k--) {

            JAMSNode node = (JAMSNode) paths[k].getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();

            index[i]--;
            j = i - 1;
            while ((j >= 0) && (index[j] == index[i])) {
                index[i]--;
                j--;
            }

            if (index[i] >= 0) {
                parent.insert(node, index[i]);
            }
            i--;
        }

        this.updateUI();
    }

    private void moveDownNode() {

        int i, j;
        TreePath[] paths = this.getSelectionPaths();
        int[] index = new int[paths.length];

        i = 0;
        for (TreePath path : paths) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();
            if (parent != null) {
                index[i] = parent.getIndex(node);
            }
            i++;
        }

        i = -1;
        for (TreePath path : paths) {

            i++;
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();

            if (parent == null) {
                continue;
            }

            index[i]++;
            j = i + 1;
            while ((j < index.length) && (index[j] == index[i])) {
                index[i]++;
                j++;
            }

            if (index[i] < parent.getChildCount()) {
                parent.insert(node, index[i]);
            }
        }

        this.updateUI();
    }

    private void showPopup(MouseEvent evt) {

        TreePath p = this.getClosestPathForLocation(evt.getX(), evt.getY());
        this.addSelectionPath(p);

        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();

        if (node.getType() == JAMSNode.MODEL_TYPE) {
            //return;
        }

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

    private void displayComponentInfo() {

        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();
        if (node != null) {
            view.getCompEditPanel().setComponentDescriptor((ComponentDescriptor) node.getUserObject());
        }
    }

    public void updateModelTree(Document modelDoc) {

        ModelNode rootNode = null;

        if (modelDoc == null) {
            try {
                ModelDescriptor md = modelIO.createModel(JUICE.getExHandler());
                view.setModelDescriptor(md);
                this.setComponentCollection(md);
                rootNode = md.getRootNode();


            } catch (JAMSException ex) {
                GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), ex.getMessage(), ex.getHeader());
            }
        } else {
            try {
                ModelDescriptor md = modelIO.loadModel(modelDoc, JUICE.getLoader(), true, JUICE.getExHandler());
                view.setModelDescriptor(md);
                this.setComponentCollection(md);
                rootNode = md.getRootNode();
            } catch (JAMSException ex) {
                GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), ex.getMessage(), ex.getHeader());
            }

        }

        view.getModelEditPanel().updatePanel();

        TreeModel model = new DefaultTreeModel(rootNode);
//        TreeModel model = new DefaultTreeModel(getModelTree(modelDoc));
        setModel(model);

        this.setSelectionRow(0);
        this.displayComponentInfo();
        this.expandAll();
        smartExpand = false;
    }

    // Create a XML document from the model tree
    public Document getModelDocument(ModelDescriptor md) {
        return modelIO.getModelDocument(md);
    }

    public ModelView getView() {
        return view;
    }

    @Override
    protected void setExpandedState(TreePath path, boolean state) {

        // If smartExpand is true, expand only nodes that do not represent 
        // simple JAMSContext objects. Nodes representing subclasses of 
        // JAMSContext will be expanded
        if (smartExpand) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            if (node.getType() == JAMSNode.CONTEXT_TYPE) {
                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
                if (cd.getClazz() == JAMSContext.class) {
                    return;
                }
            }
        }
        super.setExpandedState(path, state);
    }

    class ModelLoadException extends Exception {

        private String className, componentName;

        public ModelLoadException(String className, String componentName) {
            super();
            this.className = className;
            this.componentName = componentName;
        }

        public String getClassName() {
            return className;
        }

        public String getComponentName() {
            return componentName;
        }
    }
}
