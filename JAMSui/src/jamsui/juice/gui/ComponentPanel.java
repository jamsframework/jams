/*
 * ComponentPanel.java
 * Created on 12. Dezember 2006, 22:43
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
package jamsui.juice.gui;

import jams.JAMSException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import jams.gui.tools.GUIHelper;
import java.awt.Font;
import javax.swing.UIManager;
import jamsui.juice.*;

import jams.JAMS;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelNode;
import jamsui.juice.gui.tree.JAMSNode;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

/**
 *
 * @author S. Kralisch
 */
public class ComponentPanel extends JPanel {

    private static final String DEFAULT_STRING = JAMS.i18n("[none]");
//    ATTR_CONFIG_STRING = JAMS.i18n("Attribute_configuration:"), MODEL_CONFIG_STRING = JAMS.i18n("Model_configuration:"), ATTR_OVERVIEW_STRING = JAMS.i18n("Attribute_overview:");
    private static final Dimension TABLE_DIMENSION = new Dimension(500, 200);
    private ComponentDescriptor componentDescriptor = null;
    private HashMap<String, JTextField> textFields = new HashMap<String, JTextField>();
    private JPanel componentPanel;
    private JTable varTable, attributeTable;
    private Vector<String> varTableColumnIds = new Vector<String>(), attributeTableColumnIds = new Vector<String>();
    private DefaultTableModel varTableModel, attributeTableModel;
    private List<String> varNameList, attrNameList;
    private int selectedVarRow, selectedAttrRow;
    private JButton attributeEditButton, attributeAddButton, attributeDeleteButton;
    private ContextAttributeDlg attrEditDlg;
    private ModelView view;
    private JTabbedPane tabPane;
    private ComponentAttributePanel attributeConfigPanel;
    private JPanel switchPanel;
//    private JLabel configLabel;

    public ComponentPanel(ModelView view) {
        super();
        this.view = view;
        init();
    }

    private void init() {

        componentPanel = new JPanel();

        // create some bold font for the labels
        Font labelFont = (Font) UIManager.getDefaults().get("Label.font");
        labelFont = new Font(labelFont.getName(), Font.BOLD, labelFont.getSize() );

        GridBagLayout mainLayout = new GridBagLayout();
        componentPanel.setLayout(mainLayout);

        JLabel nameLabel = new JLabel(JAMS.i18n("Name:"));
        nameLabel.setFont(labelFont);
        JLabel typeLabel = new JLabel(JAMS.i18n("Type:"));
        typeLabel.setFont(labelFont);

        GUIHelper.addGBComponent(componentPanel, mainLayout, nameLabel, 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(componentPanel, mainLayout, typeLabel, 0, 1, 1, 1, 0, 0);

        JButton nameEditButton = new JButton(JAMS.i18n("..."));
        nameEditButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nameEditButton.setPreferredSize(new Dimension(20, 20));
        nameEditButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String oldName = textFields.get("name").getText();
                String newName = GUIHelper.showInputDlg(JUICE.getJuiceFrame(), JAMS.i18n("New_component_name"), oldName);
                if ((newName != null) && !newName.equals(oldName)) {
                    textFields.get("name").setText(newName);
                    setComponentName();
                }
            }
        });
        
        JPanel namePanel = new JPanel();
        ((FlowLayout)namePanel.getLayout()).setVgap(0);
        namePanel.add(getTextField("name", "", false));
        namePanel.add(nameEditButton);

        JPanel typePanel = new JPanel();
        ((FlowLayout)typePanel.getLayout()).setVgap(0);
        typePanel.add(getTextField("type", "", false));

        GUIHelper.addGBComponent(componentPanel, mainLayout, namePanel, 1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
        GUIHelper.addGBComponent(componentPanel, mainLayout, typePanel, 1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);


        //create var table
        varTable = new JTable() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        varTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel varRowSM = varTable.getSelectionModel();
        varRowSM.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    ComponentPanel.this.selectedVarRow = lsm.getMinSelectionIndex();
                } else {
                    ComponentPanel.this.selectedVarRow = -1;
                }
                updateAttributeConfigPanel();
            }
        });

        varTableColumnIds.add(JAMS.i18n("Name"));
        varTableColumnIds.add(JAMS.i18n("Type"));
        varTableColumnIds.add("R/W");
        varTableColumnIds.add(JAMS.i18n("Context_Attribute"));
        varTableColumnIds.add(JAMS.i18n("Value"));

        varTableModel = new DefaultTableModel(varTableColumnIds, 0);
        varTable.setModel(varTableModel);
        varTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane varTableScroll = new JScrollPane(varTable);
        varTableScroll.setPreferredSize(TABLE_DIMENSION);

        //create panel that holds all contents of the var tab
        JPanel varPanel = new JPanel();
        varPanel.setLayout(new BorderLayout());

        varPanel.add(varTableScroll, BorderLayout.CENTER);

        //create attribute table
        attributeTable = new JTable() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel attrRowSM = attributeTable.getSelectionModel();
        attrRowSM.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    ComponentPanel.this.selectedAttrRow = lsm.getMinSelectionIndex();
                    ComponentPanel.this.attributeEditButton.setEnabled(true);
                    ComponentPanel.this.attributeDeleteButton.setEnabled(true);
                } else {
                    ComponentPanel.this.selectedAttrRow = -1;
                    ComponentPanel.this.attributeEditButton.setEnabled(false);
                    ComponentPanel.this.attributeDeleteButton.setEnabled(false);
                }
            }
        });
        attributeTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showAttributeEditDlg();
                }
            }
        });

        attributeTableColumnIds.add(JAMS.i18n("Name"));
        attributeTableColumnIds.add(JAMS.i18n("Type"));
        attributeTableColumnIds.add(JAMS.i18n("Value"));
        attributeTableModel = new DefaultTableModel(attributeTableColumnIds, 0);
        attributeTable.setModel(attributeTableModel);
        attributeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);


        JScrollPane attributeTableScroll = new JScrollPane(attributeTable);
        attributeTableScroll.setPreferredSize(TABLE_DIMENSION);

        //create panel that holds all contents of the attribute tab
        JPanel attributePanel = new JPanel();
        attributePanel.setLayout(new BorderLayout());

        attributePanel.add(attributeTableScroll, BorderLayout.CENTER);

        attributeEditButton = new JButton(JAMS.i18n("Edit"));
        attributeEditButton.setEnabled(false);
        attributeEditButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeEditDlg();
            }
        });
        attributeAddButton = new JButton(JAMS.i18n("Add"));
        attributeAddButton.setEnabled(true);
        attributeAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeAddDlg();
            }
        });
        attributeDeleteButton = new JButton(JAMS.i18n("Delete"));
        attributeDeleteButton.setEnabled(false);
        attributeDeleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeDeleteDlg();
            }
        });

        JPanel attributeButtonPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        attributeButtonPanel.setLayout(gbl);
        GUIHelper.addGBComponent(attributeButtonPanel, gbl, attributeAddButton, 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(attributeButtonPanel, gbl, attributeDeleteButton, 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(attributeButtonPanel, gbl, attributeEditButton, 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(attributeButtonPanel, gbl, new JPanel(), 0, 3, 1, 1, 1, 1);
        attributePanel.add(attributeButtonPanel, BorderLayout.EAST);


        //fill the tabbed pane
        tabPane = new JTabbedPane();

        tabPane.add(JAMS.i18n("Component_attributes"), varPanel);
        tabPane.add(JAMS.i18n("Context_attributes"), attributePanel);
        tabPane.setEnabledAt(1, false);

        attributeConfigPanel = new ComponentAttributePanel();
        
//        configLabel = new JLabel(MODEL_CONFIG_STRING);
//        configLabel.setFont(labelFont);

        switchPanel = new JPanel();
        switchPanel.setBackground(Color.red);

//        JLabel attrOverviewLabel = new JLabel(ATTR_OVERVIEW_STRING);
//        attrOverviewLabel.setFont(labelFont);
        
//        GUIHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 2, 4, 1, 1.0, 1.0);
//        GUIHelper.addGBComponent(componentPanel, mainLayout, attrOverviewLabel, 0, 10, 4, 1, 0, 0);
        GUIHelper.addGBComponent(componentPanel, mainLayout, tabPane, 0, 20, 4, 1, 1.0, 1.0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
//        GUIHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 25, 4, 1, 1.0, 1.0);
//        GUIHelper.addGBComponent(componentPanel, mainLayout, configLabel, 0, 27, 4, 1, 0, 0);
        GUIHelper.addGBComponent(componentPanel, mainLayout, switchPanel, 0, 30, 4, 1, 1.0, 1.0);

        switchPanel.add(attributeConfigPanel);
//        switchPanel.add(new JPanel());
        ((FlowLayout)switchPanel.getLayout()).setHgap(0);
        ((FlowLayout)switchPanel.getLayout()).setVgap(0);
        
        reset(DEFAULT_STRING);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(componentPanel);
    }

    private void showAttributeEditDlg() {

        int tmpSelectedAttrRow = selectedAttrRow;

        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }

        String attributeName = attrNameList.get(selectedAttrRow);
        ContextAttribute attr = ((ContextDescriptor) componentDescriptor).getStaticAttributes().get(attributeName);
        attrEditDlg.show(attr.getName(), attr.getType(), attr.getValue());

        if (attrEditDlg.getResult() == ContextAttributeDlg.APPROVE_OPTION) {
            attr.setValue(attrEditDlg.getValue());
            attr.setType(attrEditDlg.getAttributeType());
            try {
                attr.setName(attrEditDlg.getAttributeName());
            } catch (JAMSException ex) {
                GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), ex.getMessage(), ex.getHeader());
                Logger.getLogger(ComponentPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.updateCtxtAttrs();
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }
    }

    private void showAttributeAddDlg() {
        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }
        attrEditDlg.show("", JUICE.JAMS_DATA_TYPES[10], "");

        if (attrEditDlg.getResult() == ContextAttributeDlg.APPROVE_OPTION) {
            try {
                ((ContextDescriptor) componentDescriptor).addStaticAttribute(attrEditDlg.getAttributeName(), attrEditDlg.getAttributeType(), attrEditDlg.getValue());
                this.updateCtxtAttrs();
            } catch (JAMSException ex) {
                GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), ex.getMessage(), ex.getHeader());
                Logger.getLogger(ComponentPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void showAttributeDeleteDlg() {

        int tmpSelectedAttrRow = selectedAttrRow;

        String attrName = attrNameList.get(selectedAttrRow);
        int result = GUIHelper.showYesNoDlg(JUICE.getJuiceFrame(), JAMS.i18n("Delete_Attribute_") + attrName + "\"?", JAMS.i18n("Confirm"));
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        ((ContextDescriptor) componentDescriptor).removeStaticAttribute(attrName);
        this.updateCtxtAttrs();

        if (tmpSelectedAttrRow > attributeTable.getRowCount() - 1) {
            tmpSelectedAttrRow--;
        }

        if (tmpSelectedAttrRow >= 0) {
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }
    }

    public JTextField getTextField(String key, String value, boolean editable) {
        JTextField text = new JTextField();
        text.setBorder(BorderFactory.createEtchedBorder());
        text.setEditable(editable);
        text.setText(value);
        text.setColumns(30);
        textFields.put(key, text);
        return text;
    }

    public void setComponentDescriptor(ComponentDescriptor cd) {
        this.componentDescriptor = cd;

        if (componentDescriptor.getType() == JAMSNode.MODEL_TYPE) {
            if (switchPanel.getComponents()[0] != view.getModelEditPanel()) {
                switchPanel.remove(switchPanel.getComponents()[0]);
                view.getModelEditPanel().setPreferredSize(switchPanel.getSize());
                switchPanel.add(view.getModelEditPanel());
//                configLabel.setText(MODEL_CONFIG_STRING);
                switchPanel.updateUI();
            }
        } else {
            if (switchPanel.getComponents()[0] != attributeConfigPanel) {
                switchPanel.remove(switchPanel.getComponents()[0]);
                attributeConfigPanel.setPreferredSize(switchPanel.getSize());
                switchPanel.add(attributeConfigPanel);
//                configLabel.setText(ATTR_CONFIG_STRING);
                this.updateUI();
            }
        }


        if (componentDescriptor.getType() == JAMSNode.COMPONENT_TYPE) {
            tabPane.setEnabledAt(1, false);
            tabPane.setEnabledAt(0, true);
            tabPane.setSelectedIndex(0);
        } else if (componentDescriptor.getType() == JAMSNode.MODEL_TYPE) {
            tabPane.setEnabledAt(0, false);
            tabPane.setEnabledAt(1, true);
            tabPane.setSelectedIndex(1);
        } else {
            tabPane.setEnabledAt(0, true);
            tabPane.setEnabledAt(1, true);
        }

        textFields.get("type").setText(componentDescriptor.getClazz().getCanonicalName());
        textFields.get("name").setText(componentDescriptor.getInstanceName());

        updateCmpAttrs();
        updateCtxtAttrs();
    }

    private void updateCtxtAttrs() {

        if (!(componentDescriptor instanceof ContextDescriptor)) {
            return;
        }

        selectedAttrRow = -1;

        HashMap<String, ContextAttribute> attributes = ((ContextDescriptor) componentDescriptor).getStaticAttributes();

        attrNameList = new ArrayList<String>(attributes.keySet());
        Collections.sort(attrNameList);

        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        Vector<String> rowData;
        for (String name : attrNameList) {
            ContextAttribute attr = attributes.get(name);

            //create a vector with table data from attr properties
            rowData = new Vector<String>();
            rowData.add(attr.getName());
            rowData.add(attr.getType().getSimpleName());
            rowData.add(attr.getValue());

            tableData.add(rowData);
        }

        attributeTableModel.setDataVector(tableData, attributeTableColumnIds);

        attributeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        attributeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        attributeTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    private void updateCmpAttrs() {

        selectedVarRow = -1;

        varNameList = componentDescriptor.getComponentFieldList();

        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        Vector<String> rowData;
        for (String name : varNameList) {
            ComponentField var = componentDescriptor.getComponentFields().get(name);

            //create a vector with table data from var properties
            rowData = new Vector<String>();
            rowData.add(var.getName());

            String type = var.getType().getSimpleName();
            rowData.add(type);

            String accessType = "";
            if (var.getAccessType() == ComponentField.READ_ACCESS) {
                accessType = "R";
            }
            if (var.getAccessType() == ComponentField.WRITE_ACCESS) {
                accessType = "W";
            }
            if (var.getAccessType() == ComponentField.READWRITE_ACCESS) {
                accessType = "R/W";
            }
            rowData.add(accessType);

            if (!var.getAttribute().equals("")) {
                rowData.add(var.getContext() + "." + var.getAttribute());
            } else {
                rowData.add("");
            }

            rowData.add(var.getValue());

            tableData.add(rowData);
        }
        varTableModel.setDataVector(tableData, varTableColumnIds);

        varTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        varTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        varTable.getColumnModel().getColumn(2).setMaxWidth(35);
        varTable.getColumnModel().getColumn(3).setPreferredWidth(150);

    }

    public void reset(String clazz) {
        for (JTextField text : textFields.values()) {
            text.setText(DEFAULT_STRING);
        }
        textFields.get("type").setText(clazz);
    }

    private void setComponentName() {
        String name = textFields.get("name").getText();
        if (componentDescriptor != null) {
            try {
                componentDescriptor.setInstanceName(name);
            } catch (JAMSException ex) {
                GUIHelper.showInfoDlg(JUICE.getJuiceFrame(), ex.getMessage(), ex.getHeader());
                Logger.getLogger(ComponentPanel.class.getName()).log(Level.WARNING, null, ex);
            }

            textFields.get("name").setText(componentDescriptor.getInstanceName());
        }
    }

    private void updateAttributeConfigPanel() {

        if (selectedVarRow < 0) {
            attributeConfigPanel.cleanup();
            return;
        }
        String attributeName = varNameList.get(selectedVarRow);
        ComponentField attr = componentDescriptor.getComponentFields().get(attributeName);

        ArrayList<ComponentDescriptor> ancestors = new ArrayList<ComponentDescriptor>();

        ModelNode ancestor = (ModelNode) componentDescriptor.getNode().getParent();
        while (ancestor != null) {
            ancestors.add((ComponentDescriptor) ancestor.getUserObject());
            ancestor = (ModelNode) ancestor.getParent();
        }

        ComponentDescriptor ancestorArray[] = ancestors.toArray(new ComponentDescriptor[ancestors.size()]);
        attributeConfigPanel.update(attr, ancestorArray, componentDescriptor, varTable.getModel(), selectedVarRow);
    }
}
