/*
 * ComponentPanel.java
 * Created on 12. Dezember 2006, 22:43
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
package org.unijena.juice.gui;

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
import jams.gui.LHelper;
import jams.model.JAMSModel;
import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.unijena.juice.*;
import org.unijena.juice.ComponentDescriptor;
import org.unijena.juice.ComponentDescriptor.ComponentAttribute;
import org.unijena.juice.gui.tree.JAMSNode;
import org.unijena.juice.ContextAttribute;

/**
 *
 * @author S. Kralisch
 */
public class ComponentPanel extends JPanel {

    private static final String DEFAULT_STRING = "[none]",
            ATTR_CONFIG_STRING = "Attribute configuration:", 
            MODEL_CONFIG_STRING = "Model configuration:",
            ATTR_OVERVIEW_STRING = "Attribute overview:";
    private static final Dimension BUTTON_DIMENSION = new Dimension(70, 20);
    private static final Dimension TABLE_DIMENSION = new Dimension(500, 200);
    private ComponentDescriptor componentDescriptor = null;
    private HashMap<String, JTextField> textFields = new HashMap<String, JTextField>();
    private JPanel componentPanel;
    private Vector<JPanel> varPanels = new Vector<JPanel>();
    private JTable varTable,  attributeTable;
    private Vector<String> varTableColumnIds = new Vector<String>(),  attributeTableColumnIds = new Vector<String>();
    private DefaultTableModel varTableModel,  attributeTableModel;
    private List<String> varNameList,  attrNameList;
    private int selectedVarRow,  selectedAttrRow;
    private JButton attributeEditButton,  attributeAddButton,  attributeDeleteButton;
    private ComponentAttributeDlg varEditDlg;
    private ContextAttributeDlg attrEditDlg;
    private ModelView view;
    private JAMSNode node;
    private JTabbedPane tabPane;
    private ComponentAttributePanel attributeConfigPanel;
    private JPanel switchPanel;
    private JLabel configLabel;

    public ComponentPanel(ModelView view) {
        super();
        this.view = view;
        init();
    }

    private void init() {

        componentPanel = new JPanel();
        //setBorder(BorderFactory.createTitledBorder("Component Properties"));

        // create some bold font for the labels
        Font labelFont = (Font) UIManager.getDefaults().get("Label.font");
        labelFont = new Font(labelFont.getName(), Font.BOLD, labelFont.getSize()+1);                
        
        GridBagLayout mainLayout = new GridBagLayout();
        componentPanel.setLayout(mainLayout);
        
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(labelFont);
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(labelFont);

        LHelper.addGBComponent(componentPanel, mainLayout, nameLabel, 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, typeLabel, 0, 1, 1, 1, 0, 0);

        JButton nameEditButton = new JButton("...");
        nameEditButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nameEditButton.setPreferredSize(new Dimension(20, 20));
        nameEditButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String oldName = textFields.get("name").getText();
                String newName = LHelper.showInputDlg(JUICE.getJuiceFrame(), "New component name", oldName);
                if ((newName != null) && !newName.equals(oldName)) {
                    textFields.get("name").setText(newName);
                    setComponentName();
                }
            }
        });
        JPanel nameEditButtonPanel = new JPanel();
        nameEditButtonPanel.setLayout(new BorderLayout());
        nameEditButtonPanel.add(nameEditButton, BorderLayout.WEST);

        LHelper.addGBComponent(componentPanel, mainLayout, getTextField("name", "", false), 1, 0, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, nameEditButtonPanel, 2, 0, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, getTextField("type", "", false), 1, 1, 1, 1, 1.0, 1.0);

        //LHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 0, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 3, 0, 1, 1, 1.0, 1.0);

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

        /*
        varTable.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e){
        if (e.getClickCount() == 2){
        showVarEditDlg();
        }
        }
        });
         */

        varTableColumnIds.add("Name");
        varTableColumnIds.add("Type");
        varTableColumnIds.add("R/W");
        varTableColumnIds.add("Context Attribute");
        varTableColumnIds.add("Value");

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

        attributeTableColumnIds.add("Name");
        attributeTableColumnIds.add("Type");
        attributeTableColumnIds.add("Value");
        attributeTableModel = new DefaultTableModel(attributeTableColumnIds, 0);
        attributeTable.setModel(attributeTableModel);
        attributeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);


        JScrollPane attributeTableScroll = new JScrollPane(attributeTable);
        attributeTableScroll.setPreferredSize(TABLE_DIMENSION);

        //create panel that holds all contents of the attribute tab
        JPanel attributePanel = new JPanel();
        attributePanel.setLayout(new BorderLayout());

        attributePanel.add(attributeTableScroll, BorderLayout.CENTER);

        attributeEditButton = new JButton("Edit");
        attributeEditButton.setEnabled(false);
        attributeEditButton.setPreferredSize(BUTTON_DIMENSION);
        attributeEditButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeEditDlg();
            }
        });
        attributeAddButton = new JButton("Add");
        attributeAddButton.setEnabled(true);
        attributeAddButton.setPreferredSize(BUTTON_DIMENSION);
        attributeAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeAddDlg();
            }
        });
        attributeDeleteButton = new JButton("Delete");
        attributeDeleteButton.setEnabled(false);
        attributeDeleteButton.setPreferredSize(BUTTON_DIMENSION);
        attributeDeleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributeDeleteDlg();
            }
        });

        JPanel attributeButtonPanel = new JPanel();
        attributeButtonPanel.setPreferredSize(new Dimension(BUTTON_DIMENSION.width + 10, 20));
        attributeButtonPanel.add(attributeAddButton);
        attributeButtonPanel.add(attributeDeleteButton);
        attributeButtonPanel.add(attributeEditButton);
        attributePanel.add(attributeButtonPanel, BorderLayout.EAST);


        //fill the tabbed pane
        tabPane = new JTabbedPane();

        tabPane.add("Component attributes", varPanel);
        tabPane.add("Context attributes (local)", attributePanel);
        tabPane.setEnabledAt(1, false);

        attributeConfigPanel = new ComponentAttributePanel(view);
        configLabel = new JLabel(MODEL_CONFIG_STRING);
        configLabel.setFont(labelFont);
        
        switchPanel = new JPanel();
        
        JLabel attrOverviewLabel = new JLabel(ATTR_OVERVIEW_STRING);
        attrOverviewLabel.setFont(labelFont);

        LHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 2, 4, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, attrOverviewLabel, 0, 10, 4, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, tabPane, 0, 20, 4, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 25, 4, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, configLabel, 0, 27, 4, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, switchPanel, 0, 30, 4, 1, 1.0, 1.0);

        switchPanel.add(attributeConfigPanel);

        reset(DEFAULT_STRING);
        add(componentPanel);
    }

    private void showAttributeEditDlg() {

        int tmpSelectedAttrRow = selectedAttrRow;

        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }

        String attributeName = attrNameList.get(selectedAttrRow);
        ContextAttribute attr = componentDescriptor.getContextAttributes().get(attributeName);
        attrEditDlg.show(attr.getName(), attr.getType().getName(), attr.getValue());

        if (attrEditDlg.getResult() == ContextAttributeDlg.APPROVE_OPTION) {
            attr.setValue(attrEditDlg.getValue());
            try {
                attr.setType(Class.forName(attrEditDlg.getType()));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            attr.setName(attrEditDlg.getAttributeName());
            this.updateCtxtAttrs();
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }
    }

    private void showAttributeAddDlg() {
        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }
        attrEditDlg.show("", JUICE.JAMS_DATA_TYPES[10].getName(), "");

        if (attrEditDlg.getResult() == ContextAttributeDlg.APPROVE_OPTION) {
            try {
                componentDescriptor.addContextAttribute(attrEditDlg.getAttributeName(), Class.forName(attrEditDlg.getType()), attrEditDlg.getValue());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            this.updateCtxtAttrs();
        }
    }

    private void showAttributeDeleteDlg() {

        int tmpSelectedAttrRow = selectedAttrRow;

        String attrName = attrNameList.get(selectedAttrRow);
        int result = LHelper.showYesNoDlg(JUICE.getJuiceFrame(), "Delete Attribute \"" + attrName + "\"?", "Confirm");
        if (result == JOptionPane.NO_OPTION) {
            return;
        }
        componentDescriptor.removeContextAttribute(attrName);
        this.updateCtxtAttrs();

        if (tmpSelectedAttrRow > attributeTable.getRowCount() - 1) {
            tmpSelectedAttrRow--;
        }

        if (tmpSelectedAttrRow >= 0) {
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }

    }

    /*    
    private void showVarEditDlg() {
    int tmpSelectedVarRow = selectedVarRow;
    String attributeName = varNameList.get(selectedVarRow);
    ComponentAttribute var = componentDescriptor.getComponentAttributes().get(attributeName);
    
    //create the dialog if it not yet existing
    if (varEditDlg == null) {
    varEditDlg = new ComponentAttributeDlg(JUICE.getJuiceFrame(), view);
    }
    
    Vector<String> ancestors = new Vector<String>();
    ancestors.add("");
    
    JAMSNode ancestor = (JAMSNode) node.getParent();
    while (ancestor != null) {
    ancestors.add(ancestor.getUserObject().toString());
    ancestor = (JAMSNode) ancestor.getParent();
    }
    
    String ancestorNames[] = ancestors.toArray(new String[ancestors.size()]);
    
    varEditDlg.show(var, ancestorNames);
    
    if (varEditDlg.getResult() == ComponentAttributeDlg.APPROVE_OPTION) {
    if ((varEditDlg.getAttributeName().equals("") && varEditDlg.getContext() == null) ||
    (!varEditDlg.getAttributeName().equals("") && varEditDlg.getContext() != null)) {
    
    ComponentDescriptor.ComponentAttribute ca = componentDescriptor.getComponentAttributes().get(attributeName);
    ca.linkToAttribute(varEditDlg.getContext(), varEditDlg.getAttributeName());
    //componentDescriptor.linkComponentAttribute(attributeName, varEditDlg.getContext(), varEditDlg.getAttributeName());
    
    }
    //componentDescriptor.setComponentAttribute(attributeName, varEditDlg.getValue());
    componentDescriptor.getComponentAttributes().get(attributeName).setValue(varEditDlg.getValue());
    
    this.updateCmpAttrs();
    varTable.setRowSelectionInterval(tmpSelectedVarRow, tmpSelectedVarRow);
    }
    }
     */
    public JTextField getTextField(String key, String value, boolean editable) {
        JTextField text = new JTextField();
        text.setBorder(BorderFactory.createEtchedBorder());
        text.setEditable(editable);
        text.setText(value);
        text.setColumns(30);
        textFields.put(key, text);
        return text;
    }

    public void setComponentDescriptor(JAMSNode node) {
        this.node = node;
        this.componentDescriptor = (ComponentDescriptor) node.getUserObject();

        if (componentDescriptor.getClazz() == JAMSModel.class) {
            if (switchPanel.getComponents()[0] != view.getModelEditPanel()) {
                switchPanel.remove(switchPanel.getComponents()[0]);
                switchPanel.add(view.getModelEditPanel());
                configLabel.setText(MODEL_CONFIG_STRING);
                switchPanel.updateUI();
            }
        } else {
            if (switchPanel.getComponents()[0] != attributeConfigPanel) {
                switchPanel.remove(switchPanel.getComponents()[0]);
                switchPanel.add(attributeConfigPanel);
                configLabel.setText(ATTR_CONFIG_STRING);
                this.updateUI();
            }
        }


        if (node.getType() == JAMSNode.COMPONENT_NODE) {
            tabPane.setEnabledAt(1, false);
            tabPane.setEnabledAt(0, true);
            tabPane.setSelectedIndex(0);
        } else if (node.getType() == JAMSNode.MODEL_ROOT) {
            tabPane.setEnabledAt(0, false);
            tabPane.setEnabledAt(1, true);
            tabPane.setSelectedIndex(1);
        } else {
            tabPane.setEnabledAt(0, true);
            tabPane.setEnabledAt(1, true);
        }

        textFields.get("type").setText(componentDescriptor.getClazz().getCanonicalName());
        textFields.get("name").setText(componentDescriptor.getName());

        updateCmpAttrs();
        updateCtxtAttrs();
    }

    private void updateCtxtAttrs() {
        selectedAttrRow = -1;

        attrNameList = new ArrayList<String>(componentDescriptor.getContextAttributes().keySet());
        Collections.sort(attrNameList);

        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        Vector<String> rowData;
        for (String name : attrNameList) {
            ContextAttribute attr = componentDescriptor.getContextAttributes().get(name);

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

        varNameList = new ArrayList<String>(componentDescriptor.getComponentAttributes().keySet());
        //Collections.sort(varNameList);

        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        Vector<String> rowData;
        for (String name : varNameList) {
            ComponentAttribute var = componentDescriptor.getComponentAttributes().get(name);

            //create a vector with table data from var properties
            rowData = new Vector<String>();
            rowData.add(var.name);

            String type = var.type.getSimpleName();
            rowData.add(type);

            String accessType = "";
            if (var.accessType == ComponentAttribute.READ_ACCESS) {
                accessType = "R";
            }
            if (var.accessType == ComponentAttribute.WRITE_ACCESS) {
                accessType = "W";
            }
            if (var.accessType == ComponentAttribute.READWRITE_ACCESS) {
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
            } catch (JUICEException.NameAlreadyUsedException ex) {
                LHelper.showInfoDlg(this, "Name " + name + " is already in use. Renamed component to " +
                        componentDescriptor.getName() + "!", "Component name");
                textFields.get("name").setText(componentDescriptor.getName());
            }
        }
    }

    private void updateAttributeConfigPanel() {

        if (selectedVarRow < 0) {
            attributeConfigPanel.cleanup();
            return;
        }
        String attributeName = varNameList.get(selectedVarRow);
        ComponentAttribute attr = componentDescriptor.getComponentAttributes().get(attributeName);

        Vector<ComponentDescriptor> ancestors = new Vector<ComponentDescriptor>();

        JAMSNode ancestor = (JAMSNode) node.getParent();
        while (ancestor != null) {
            ancestors.add((ComponentDescriptor) ancestor.getUserObject());
            ancestor = (JAMSNode) ancestor.getParent();
        }

        ComponentDescriptor ancestorArray[] = ancestors.toArray(new ComponentDescriptor[ancestors.size()]);
        attributeConfigPanel.update(attr, ancestorArray, componentDescriptor, varTable.getModel(), selectedVarRow);
    }
}
