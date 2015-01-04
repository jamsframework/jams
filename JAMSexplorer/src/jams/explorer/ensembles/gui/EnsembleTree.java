/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import static jams.explorer.ensembles.gui.ClimateDataOverviewTab.logger;
import jams.explorer.ensembles.implementation.ClimateEnsemble.EnsembleTreeNode;
import jams.explorer.ensembles.implementation.ClimateEnsemble.OutputDirectoryTreeNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author christian
 */
public class EnsembleTree extends JTree {
    static final Logger logger = Logger.getLogger(EnsembleTree.class.getName());
        {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    public EnsembleTree() {
        setCellRenderer(new CheckBoxNodeRenderer());
        EnsembleTree.EnsembleTreeEditor editor = new EnsembleTreeEditor(this);
        setCellEditor(editor);

        init();
    }

    private void init() {
        logger.entering(getClass().getName(), "init");
        setEditable(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ((DefaultTreeCellRenderer) getCellRenderer()).setMinimumSize(new Dimension(300, 25));
        ((DefaultTreeCellRenderer) getCellRenderer()).setPreferredSize(new Dimension(300, 25));
        ((DefaultTreeCellRenderer) getCellRenderer()).setSize(300, 25);

        logger.exiting(getClass().getName(), "init");
    }

    public void expandAll() {
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
    }

    @Override
    public void setModel(TreeModel m) {
        logger.entering(getClass().getName(), "setModel");
        
        super.setModel(m);
        this.getModel().addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                expandAll();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                expandAll();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                expandAll();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                expandAll();
            }
        });
        expandAll();
        
        logger.exiting(getClass().getName(), "setModel");
    }

    static class CheckBoxNodeRenderer extends DefaultTreeCellRenderer {

        private final JCheckBox checkBoxRenderer = new JCheckBox();

        Color selectionBorderColor, selectionForeground, selectionBackground,
                textForeground, textBackground;

        public CheckBoxNodeRenderer() {
            Font fontValue;
            fontValue = UIManager.getFont("Tree.font");
            if (fontValue != null) {
                checkBoxRenderer.setFont(fontValue);
            }
            Boolean booleanValue = (Boolean) UIManager
                    .get("Tree.drawsFocusBorderAroundIcon");
            checkBoxRenderer.setFocusPainted((booleanValue != null)
                    && (booleanValue.booleanValue()));
            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
            checkBoxRenderer.setBackground(textBackground);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            Component returnValue = super.getTreeCellRendererComponent(tree,
                    value, selected, expanded, leaf, row, hasFocus);
            returnValue.setForeground(Color.black);

            if (value instanceof OutputDirectoryTreeNode) {
                if (selected) {
                    checkBoxRenderer.setForeground(selectionForeground);
                    checkBoxRenderer.setBackground(selectionBackground);
                } else {
                    checkBoxRenderer.setForeground(textForeground);
                    checkBoxRenderer.setBackground(textBackground);
                }
                OutputDirectoryTreeNode node = (OutputDirectoryTreeNode) value;
                boolean nodeSelection = node.getModel().isOutputSelected(node.getOutputDirectory());

                String stringValue = tree.convertValueToText(value, selected,
                        expanded, leaf, row, false);

                checkBoxRenderer.setText(stringValue);
                checkBoxRenderer.setSelected(nodeSelection);
                checkBoxRenderer.setEnabled(tree.isEnabled());

                returnValue = checkBoxRenderer;
            }

            return returnValue;
        }
    }

    private static class EnsembleTreeEditor extends AbstractCellEditor implements TreeCellEditor {

        JTree tree;
        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        JComponent editedComponent;
        JCheckBox checkBox = renderer.checkBoxRenderer;
        JTextField textfield = new JTextField();

        public EnsembleTreeEditor(JTree tree) {
            this.tree = tree;
            checkBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    editedComponent = (JComponent) e.getSource();
                    stopCellEditing();
                }
            });
            
            textfield.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    editedComponent = (JComponent) e.getSource();
                    stopCellEditing();
                }
            });
            
            textfield.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                    
                }

                @Override
                public void focusLost(FocusEvent e) {
                    editedComponent = (JComponent) e.getSource();
                    stopCellEditing();
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            logger.entering(getClass().getName(), "getCellEditorValue");
            
            if (editedComponent == checkBox) {               
                Object o = tree.getSelectionPath().getLastPathComponent();
                if (o instanceof OutputDirectoryTreeNode) {
                    OutputDirectoryTreeNode outputDirNode = (OutputDirectoryTreeNode) o;
                    outputDirNode.getModel().setOutputSelection(outputDirNode.getOutputDirectory(), checkBox.isSelected());
                    return outputDirNode.getUserObject();
                }
            }
            if (editedComponent == textfield) {
                Object o = tree.getSelectionPath().getLastPathComponent();
                if (o instanceof EnsembleTreeNode) {
                    EnsembleTreeNode ensembleTreeNode = (EnsembleTreeNode) o;
                    ensembleTreeNode.getEnsemble().setName(textfield.getText());
                    return ensembleTreeNode.getUserObject();
                }
            }
            logger.exiting(getClass().getName(), "getCellEditorValue");
            return null;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row) {

            if (value instanceof EnsembleTreeNode) {
                value = ((EnsembleTreeNode) value).getEnsemble().getName();
                if (value!=null)
                    textfield.setText(value.toString());
                else
                    textfield.setText("");
                return textfield;
            }

            if (value instanceof OutputDirectoryTreeNode) {
                OutputDirectoryTreeNode value2 = (OutputDirectoryTreeNode) value;
                checkBox.setText(value2.getOutputDirectory());
                checkBox.setSelected(value2.getModel().isOutputSelected(value2.getOutputDirectory()));
                return checkBox;
            }
            //default fallback
            Component editor = renderer.getTreeCellRendererComponent(tree, value,
                    true, expanded, leaf, row, true);

            return editor;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            boolean returnValue = false;

            if (event instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) event;
                TreePath path = tree.getPathForLocation(mouseEvent.getX(),
                        mouseEvent.getY());

                if (path != null) {
                    Object node = path.getLastPathComponent();
                    if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                        returnValue = (treeNode instanceof OutputDirectoryTreeNode);
                    }
                }

                if (!returnValue) {
                    returnValue = super.isCellEditable(event)
                            && path.getLastPathComponent() instanceof EnsembleTreeNode;
                }

            }

            //root is also editable
            return returnValue;
        }
    }
}
