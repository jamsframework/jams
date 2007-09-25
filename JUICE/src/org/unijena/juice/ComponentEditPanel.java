/*
 * CompEditPanel.java
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

package org.unijena.juice;

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
import javax.swing.table.TableColumn;
import org.unijena.jams.gui.LHelper;
import org.unijena.juice.tree.ComponentDescriptor;
import org.unijena.juice.tree.JAMSNode;
import org.unijena.juice.tree.ContextAttribute;

/**
 *
 * @author S. Kralisch
 */
public class ComponentEditPanel extends JPanel {
    
    private static final String DEFAULT_STRING = "[none]";
    private static final Dimension BUTTON_DIMENSION = new Dimension(70,20);
    private static final Dimension TABLE_DIMENSION = new Dimension(500,250);
    
    private ComponentDescriptor componentDescriptor = null;
    private HashMap<String, JTextField> textFields = new HashMap<String, JTextField>();
    private JPanel componentPanel;
    private Vector<JPanel> varPanels = new Vector<JPanel>();
    private JTable varTable, attributeTable;
    private Vector<String> varTableColumnIds = new Vector<String>(), attributeTableColumnIds = new Vector<String>();
    private DefaultTableModel varTableModel, attributeTableModel;
    private List<String> varNameList, attrNameList;
    private int selectedVarRow, selectedAttrRow;
    private JButton varEditButton, varResetButton, attributeEditButton, attributeAddButton, attributeDeleteButton;
    private ComponentAttributeDlg varEditDlg;
    private ContextAttributeDlg attrEditDlg;
    private ModelView view;
    private JAMSNode node;
    private JTabbedPane tabPane;
    
    public ComponentEditPanel(ModelView view) {
        super();
        this.view = view;
        init();
    }
    
    private void init() {
        
        componentPanel = new JPanel();
        //setBorder(BorderFactory.createTitledBorder("Component Properties"));
        
        GridBagLayout mainLayout = new GridBagLayout();
        componentPanel.setLayout(mainLayout);
        
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Name:"), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Type:"), 0, 1, 1, 1, 0, 0);
        
        JButton nameEditButton = new JButton("...");
        nameEditButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nameEditButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String oldName = textFields.get("name").getText();
                String newName = LHelper.showInputDlg(JUICE.getJuiceFrame(), "New model name", oldName);
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
        varTable = new JTable(){
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        varTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel varRowSM = varTable.getSelectionModel();
        varRowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    ComponentEditPanel.this.selectedVarRow = lsm.getMinSelectionIndex();
                    ComponentEditPanel.this.varEditButton.setEnabled(true);
                    ComponentEditPanel.this.varResetButton.setEnabled(true);
                } else {
                    ComponentEditPanel.this.selectedVarRow = -1;
                    ComponentEditPanel.this.varEditButton.setEnabled(false);
                    ComponentEditPanel.this.varResetButton.setEnabled(false);
                }
            }
        });
        varTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if (e.getClickCount() == 2){
                    showVarEditDlg();
                }
            }
        });
        varTableColumnIds.add("Name");
        varTableColumnIds.add("Type (Access)");
        varTableColumnIds.add("R/W");
        varTableColumnIds.add("Context attribute");
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
        
        varEditButton = new JButton("Edit");
        varEditButton.setEnabled(false);
        varEditButton.setPreferredSize(BUTTON_DIMENSION);
        varEditButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showVarEditDlg();
            }
        });
        
        varResetButton = new JButton("Reset");
        varResetButton.setEnabled(false);
        varResetButton.setPreferredSize(BUTTON_DIMENSION);
        varResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                varReset();
            }
        });
        
        JPanel varButtonPanel = new JPanel();
        varButtonPanel.setPreferredSize(new Dimension(BUTTON_DIMENSION.width+10,20));
        varButtonPanel.add(varEditButton);
        varButtonPanel.add(varResetButton);
        varPanel.add(varButtonPanel, BorderLayout.EAST);
        
        //create attribute table
        attributeTable = new JTable(){
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel attrRowSM = attributeTable.getSelectionModel();
        attrRowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    ComponentEditPanel.this.selectedAttrRow = lsm.getMinSelectionIndex();
                    ComponentEditPanel.this.attributeEditButton.setEnabled(true);
                    ComponentEditPanel.this.attributeDeleteButton.setEnabled(true);
                } else {
                    ComponentEditPanel.this.selectedAttrRow = -1;
                    ComponentEditPanel.this.attributeEditButton.setEnabled(false);
                    ComponentEditPanel.this.attributeDeleteButton.setEnabled(false);
                }
            }
        });
        attributeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if (e.getClickCount() == 2){
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
            public void actionPerformed(ActionEvent e) {
                showAttributeEditDlg();
            }
        });
        attributeAddButton = new JButton("Add");
        attributeAddButton.setEnabled(true);
        attributeAddButton.setPreferredSize(BUTTON_DIMENSION);
        attributeAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAttributeAddDlg();
            }
        });
        attributeDeleteButton = new JButton("Delete");
        attributeDeleteButton.setEnabled(false);
        attributeDeleteButton.setPreferredSize(BUTTON_DIMENSION);
        attributeDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAttributeDeleteDlg();
            }
        });
        
        JPanel attributeButtonPanel = new JPanel();
        attributeButtonPanel.setPreferredSize(new Dimension(BUTTON_DIMENSION.width+10,20));
        attributeButtonPanel.add(attributeAddButton);
        attributeButtonPanel.add(attributeDeleteButton);
        attributeButtonPanel.add(attributeEditButton);
        attributePanel.add(attributeButtonPanel, BorderLayout.EAST);
        
        
        //fill the tabbed pane
        tabPane = new JTabbedPane();
        
        tabPane.add("Component attributes", varPanel);
        tabPane.add("Context attributes (local)", attributePanel);
        tabPane.setEnabledAt(1, false);
        
        LHelper.addGBComponent(componentPanel, mainLayout, new JPanel(), 0, 2, 4, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Attributes:"), 0, 10, 4, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, tabPane, 0, 20, 4, 1, 1.0, 1.0);
        
        reset(DEFAULT_STRING);
        add(componentPanel);
    }
    
    private void showAttributeEditDlg() {
        
        int tmpSelectedAttrRow = selectedAttrRow;
        
        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }
        
        String componentName = attrNameList.get(selectedAttrRow);
        ContextAttribute attr = componentDescriptor.getContextAttributes().get(componentName);
        attrEditDlg.show(attr.name, attr.type.getName(), attr.value);
        
        if (attrEditDlg.getResult() == attrEditDlg.APPROVE_OPTION) {
            attr.value = attrEditDlg.getValue();
            try {
                attr.type = Class.forName(attrEditDlg.getType());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            attr.name = attrEditDlg.getAttributeName();
            this.updateCtxtAttrs();
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }
    }
    
    private void showAttributeAddDlg() {
        //create the dialog if it not yet existing
        if (attrEditDlg == null) {
            attrEditDlg = new ContextAttributeDlg(JUICE.getJuiceFrame());
        }
        attrEditDlg.show("", JUICE.JAMS_DATA_TYPES[13].getName(), "");
        
        if (attrEditDlg.getResult() == attrEditDlg.APPROVE_OPTION) {
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
        if (result == JOptionPane.NO_OPTION)
            return;
        
        componentDescriptor.removeContextAttribute(attrName);
        this.updateCtxtAttrs();
        
        if (tmpSelectedAttrRow > attributeTable.getRowCount()-1) {
            tmpSelectedAttrRow--;
        }
        
        if (tmpSelectedAttrRow >= 0) {
            attributeTable.setRowSelectionInterval(tmpSelectedAttrRow, tmpSelectedAttrRow);
        }
        
    }
    
    private void showVarEditDlg() {
        int tmpSelectedVarRow = selectedVarRow;
        String componentName = varNameList.get(selectedVarRow);
        ComponentDescriptor.ComponentVar var = componentDescriptor.getCVars().get(componentName);
        
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
                var.attribute = varEditDlg.getAttributeName();
                //var.context = view.getComponentDescriptor(varEditDlg.getContext());
                var.context = varEditDlg.getContext();
            }
            var.value = varEditDlg.getValue();
            this.updateCmpAttrs();
            varTable.setRowSelectionInterval(tmpSelectedVarRow, tmpSelectedVarRow);
        }
    }
    
    private void varReset() {
        int tmpSelectedVarRow = selectedVarRow;
        String componentName = varNameList.get(selectedVarRow);
        ComponentDescriptor.ComponentVar var = componentDescriptor.getCVars().get(componentName);
        var.value = "";
        var.context = null;
        var.attribute = "";
        this.updateCmpAttrs();
        varTable.setRowSelectionInterval(tmpSelectedVarRow, tmpSelectedVarRow);
    }
    
    public JTextField getTextField(String key, String value, boolean editable) {
        JTextField text = new JTextField();
        text.setEditable(editable);
        text.setText(value);
        text.setColumns(30);
        textFields.put(key, text);
        return text;
    }
    
    public void setComponentDescriptor(JAMSNode node) {
        this.node = node;
        this.componentDescriptor = (ComponentDescriptor) node.getUserObject();
        
        if (node.getType() == JAMSNode.COMPONENT_NODE) {
            tabPane.setEnabledAt(1, false);
            tabPane.setSelectedIndex(0);
        } else {
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
            rowData.add(attr.name);
            rowData.add(attr.type.getSimpleName());
            rowData.add(attr.value);
            
            tableData.add(rowData);
        }
        
        attributeTableModel.setDataVector(tableData, attributeTableColumnIds);
        
        attributeTable.getColumnModel().getColumn(0).setMaxWidth(100);
        attributeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        attributeTable.getColumnModel().getColumn(1).setMaxWidth(100);
        attributeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
    }
    
    private void updateCmpAttrs() {
        
        selectedVarRow = -1;
        varEditButton.setEnabled(false);
        
        varNameList = new ArrayList<String>(componentDescriptor.getCVars().keySet());
        //Collections.sort(varNameList);
        
        Vector<Vector<String>> tableData = new Vector<Vector<String>>();
        Vector<String> rowData;
        for (String name : varNameList) {
            ComponentDescriptor.ComponentVar var = componentDescriptor.getCVars().get(name);
            
            //create a vector with table data from var properties
            rowData = new Vector<String>();
            rowData.add(var.name);
            
            String type = var.type.getSimpleName();
            rowData.add(type);
            
            String accessType = "";
            if (var.accessType == ComponentDescriptor.ComponentVar.READ_ACCESS)
                accessType = "R";
            if (var.accessType == ComponentDescriptor.ComponentVar.WRITE_ACCESS)
                accessType = "W";
            if (var.accessType == ComponentDescriptor.ComponentVar.READWRITE_ACCESS)
                accessType = "R/W";
            
            rowData.add(accessType);
            
            if (!var.attribute.equals(""))
                rowData.add(var.context+"."+var.attribute);
            else
                rowData.add("");
            rowData.add(var.value);
            
            tableData.add(rowData);
//            System.out.println(name + " : " + var.context + " : " + var.attribute + " : " + var.value);
        }
        varTableModel.setDataVector(tableData, varTableColumnIds);
        
        varTable.getColumnModel().getColumn(0).setMaxWidth(100);
        varTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        varTable.getColumnModel().getColumn(1).setMaxWidth(100);
        varTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        varTable.getColumnModel().getColumn(2).setMaxWidth(30);
        varTable.getColumnModel().getColumn(2).setPreferredWidth(30);
        
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
    
}
