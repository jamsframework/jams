/*
 * LauncherPanel.java
 * Created on 5. Januar 2007, 10:44
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.juice.ModelProperties.ModelProperty;

/**
 *
 * @author S. Kralisch
 */
public class LauncherPanel extends JPanel {
    
    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    private static final Dimension BUTTON_DIMENSION = new Dimension(130,20);
    
    private JTabbedPane tabbedPane = new JTabbedPane();
    private HashMap<ModelProperty, InputComponent> inputMap = new HashMap<ModelProperty, InputComponent>();
    private ModelPropertyDlg propertyDlg = new ModelPropertyDlg(JUICE.getJuiceFrame());
    private GroupEditDlg groupEditDlg = new GroupEditDlg(JUICE.getJuiceFrame());
    private HashMap<ModelProperties.Group, JPanel> groupPanels;
    private HashMap<ModelProperties.Group, JScrollPane> groupPanes;
    private JPanel mainButtonPanel = new JPanel();
    private ModelView view;
    
    
    
    public LauncherPanel(ModelView view) {
        
        this.view = view;
        
        //setBorder(BorderFactory.createTitledBorder("Model Parameters"));
        setLayout(new BorderLayout());
        
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(mainButtonPanel, BorderLayout.NORTH);
        
        JButton addPropertyButton = new JButton("Add Property");
        addPropertyButton.setPreferredSize(BUTTON_DIMENSION);
        addPropertyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProperty();
            }
        });
        
        JButton addGroupButton = new JButton("Add Group");
        addGroupButton.setPreferredSize(BUTTON_DIMENSION);
        addGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addGroup();
            }
        });
        
        JButton moveupGroupButton = new JButton("Move up Group");
        moveupGroupButton.setPreferredSize(BUTTON_DIMENSION);
        moveupGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveupGroup();
            }
        });
        
        JButton movedownGroupButton = new JButton("Move down Group");
        movedownGroupButton.setPreferredSize(BUTTON_DIMENSION);
        movedownGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                movedownGroup();
            }
        });
        
        JButton editGroupButton = new JButton("Edit Group");
        editGroupButton.setPreferredSize(BUTTON_DIMENSION);
        editGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editGroup();
            }
        });
        
        JButton delGroupButton = new JButton("Remove Group");
        delGroupButton.setPreferredSize(BUTTON_DIMENSION);
        delGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteGroup();
            }
        });
        
/*
        mainButtonPanel.setPreferredSize(new Dimension(BUTTON_DIMENSION.width+20,60));
        mainButtonPanel.add(addPropertyButton);
        mainButtonPanel.add(addGroupButton);
        mainButtonPanel.add(moveupGroupButton);
        mainButtonPanel.add(movedownGroupButton);
        mainButtonPanel.add(editGroupButton);
        mainButtonPanel.add(delGroupButton);
 */
        
        GridBagLayout gblButton = new GridBagLayout();
        JPanel innerButtonPanel = new JPanel();
        innerButtonPanel.setLayout(gblButton);
        innerButtonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        
        LHelper.addGBComponent(innerButtonPanel, gblButton, addPropertyButton, 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, addGroupButton, 0, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, moveupGroupButton, 1, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, movedownGroupButton, 1, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, editGroupButton, 2, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, delGroupButton, 2, 1, 1, 1, 0, 0);
        
        mainButtonPanel.add(innerButtonPanel);
        
/*
        modelProperties.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                LauncherPanel.this.setValue((ModelProperties) o);
            }
        });
 */
        
    }
    
    private void moveupGroup() {
        int index = tabbedPane.getSelectedIndex();
        if (index > 0) {
            
            ModelProperties.Group group = view.getModelProperties().getGroup(index);
            view.getModelProperties().removeGroup(group);
            Component comp = tabbedPane.getComponentAt(index);
            tabbedPane.removeTabAt(index);
            
            index--;
            
            view.getModelProperties().insertGroup(group, index);
            tabbedPane.add(comp, index);
            tabbedPane.setTitleAt(index, group.getName());
            tabbedPane.setSelectedIndex(index);
        }
    }
    
    private void movedownGroup() {
        int index = tabbedPane.getSelectedIndex();
        if (index < tabbedPane.getTabCount()-1) {
            
            ModelProperties.Group group = view.getModelProperties().getGroup(index);
            view.getModelProperties().removeGroup(group);
            Component comp = tabbedPane.getComponentAt(index);
            tabbedPane.removeTabAt(index);
            
            index++;
            
            view.getModelProperties().insertGroup(group, index);
            tabbedPane.add(comp, index);
            tabbedPane.setTitleAt(index, group.getName());
            tabbedPane.setSelectedIndex(index);
        }
    }
    
    private void editGroup() {
        
        // get selected group
        
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        ModelProperties.Group group = view.getModelProperties().getGroup(index);
        
        // query new name
        
        String groupName = group.getName();
        String newGroupName = JOptionPane.showInputDialog(this, "Group name:", groupName);
        if ((newGroupName == null) || newGroupName.equals("") || newGroupName.equals(groupName)) {
            return;
        }
        
        //  try to set group's name
        
        if (!view.getModelProperties().setGroupName(group, newGroupName)) {
            LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Group name already in use!", "Error");
            return;
        }
        
        // update tabbedpane title
        
        tabbedPane.setTitleAt(index, newGroupName);
        
    }
    
    private void addGroup() {
        
        // get name
        
        String groupName = JOptionPane.showInputDialog(this, "Group name:");
        
        if ((groupName == null) || groupName.equals("")) {
            return;
        }
                
        // add group
        
        if (!view.getModelProperties().addGroup(groupName)) {
            LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Group name already in use!", "Error");
            return;
        }
        
        ModelProperties.Group group = view.getModelProperties().getGroup(groupName);
        
        // create panels and scrollpanes
        
        JPanel contentPanel = new JPanel();
        JPanel scrollPanel = new JPanel();
        scrollPanel.add(contentPanel);
        
        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        
        tabbedPane.addTab(groupName, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
        
        // add group and panel to groupPanels map
        
        groupPanels.put(group, contentPanel);
        groupPanes.put(group, scrollPane);
    }
    
    private void deleteGroup() {
        
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        
        if (LHelper.showYesNoDlg(JUICE.getJuiceFrame(), "Really delete this Group and all of its properties?", "Delete Group") != JOptionPane.YES_OPTION) {
            return;
        }
        
        // get group
        
        ModelProperties.Group group = view.getModelProperties().getGroup(index);
        
        // remove group
        
        view.getModelProperties().removeGroup(group);
        
        // remove tabbedPane and entry from groupPanels
        
        tabbedPane.removeTabAt(index);
        groupPanels.remove(group);
    }
    
    public void updateGroup(ModelProperties.Group group) {
        
        updateProperties();
        
        InputComponent ic;
        GridBagLayout gbl = new GridBagLayout();
        
        JPanel contentPanel = groupPanels.get(group);
        
        contentPanel.removeAll();
        contentPanel.setLayout(gbl);
        
        int y = 0;
        
        ArrayList<ModelProperty> properties = group.getProperties();
        
        for (int j = 0; j < properties.size(); j++) {
            ModelProperty property = properties.get(j);
            
            LHelper.addGBComponent(contentPanel, gbl, new JLabel(property.name), 0, y, 1, 1, 0, 0);
            if (property.var != null) {
                ic = LHelper.createInputComponent(property.var.type.getSimpleName());
            } else if (property.attribute != null) {
                ic = LHelper.createInputComponent(property.attribute.getType().getSimpleName());
            } else {
                ic = LHelper.createInputComponent(JUICE.JAMS_DATA_TYPES[0].getSimpleName());
            }
            ic.setRange(property.lowerBound, property.upperBound);
            ic.getComponent().setToolTipText(property.description);
            ic.setValue(property.value);
            
            inputMap.put(property, ic);
            LHelper.addGBComponent(contentPanel, gbl, (Component) ic, 1, y, 2, 1, 1, 1);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEmptyBorder());
            
            PropertyButton downButton = new PropertyButton(property);
            downButton.setToolTipText("Move down");
            downButton.setIcon(DOWN_ICON);
            downButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertyButton button = (PropertyButton) e.getSource();
                    moveDownProperty(button.property);
                }
            });
            buttonPanel.add(downButton);
            
            PropertyButton upButton = new PropertyButton(property);
            upButton.setToolTipText("Move up");
            upButton.setIcon(UP_ICON);
            upButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertyButton button = (PropertyButton) e.getSource();
                    moveUpProperty(button.property);
                }
            });
            buttonPanel.add(upButton);
            
            PropertyButton delButton = new PropertyButton(property);
            delButton.setToolTipText("Delete");
            delButton.setText("-");
            delButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertyButton button = (PropertyButton) e.getSource();
                    deleteProperty(button.property);
                }
            });
            buttonPanel.add(delButton);
            
            PropertyButton editButton = new PropertyButton(property);
            editButton.setToolTipText("Edit");
            editButton.setText("...");
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertyButton button = (PropertyButton) e.getSource();
                    editProperty(button.property);
                }
            });
            buttonPanel.add(editButton);
            
            LHelper.addGBComponent(contentPanel, gbl, buttonPanel, 3, y, 1, 1, 1, 1);
            
            y++;
        }
        
        contentPanel.updateUI();
    }
    
    public void updatePanel() {
        
        ArrayList<ModelProperties.Group> groups = view.getModelProperties().getGroupList();
        
        groupPanels = new HashMap<ModelProperties.Group, JPanel>();
        groupPanes = new HashMap<ModelProperties.Group, JScrollPane>();
        
        tabbedPane.removeAll();
        
        JPanel contentPanel, scrollPanel;
        JScrollPane scrollPane;
        
        for (ModelProperties.Group group : groups) {
            
            contentPanel = new JPanel();
            
            scrollPanel = new JPanel();
            scrollPanel.add(contentPanel);
            scrollPane = new JScrollPane(scrollPanel);
            
            groupPanels.put(group, contentPanel);
            groupPanes.put(group, scrollPane);
            
            updateGroup(group);
            
            tabbedPane.addTab(group.getName(), scrollPane);
        }
    }
    
    private void moveDownProperty(ModelProperty property) {
        
        JPanel contentPanel = groupPanels.get(property.getGroup());
        ArrayList<ModelProperty> list = property.getGroup().getProperties();
        
        int index = list.indexOf(property);
        if (index < list.size()-1) {
            list.remove(index);
            list.add(index+1, property);
        }
        
        updateGroup(property.getGroup());
        contentPanel.updateUI();
    }
    
    private void moveUpProperty(ModelProperty property) {
        
        JPanel contentPanel = groupPanels.get(property.getGroup());
        ArrayList<ModelProperty> list = property.getGroup().getProperties();
        
        int index = list.indexOf(property);
        if (index > 0) {
            list.remove(index);
            list.add(index-1, property);
        }
        
        updateGroup(property.getGroup());
        contentPanel.updateUI();
    }
    
    private void deleteProperty(ModelProperty property) {
        
        JPanel contentPanel = groupPanels.get(property.getGroup());
        ArrayList<ModelProperty> list = property.getGroup().getProperties();
        
        int result = LHelper.showYesNoDlg(JUICE.getJuiceFrame(), "Really delete this property?", "Delete property");
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        list.remove(property);
        
        updateGroup(property.getGroup());
        contentPanel.updateUI();
    }
    
    private void addProperty() {
        
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String groupName = tabbedPane.getTitleAt(index);
        
        propertyDlg.update(view.getModelProperties().getGroupNames(), view.getComponentDescriptors(), null, groupName);
        propertyDlg.setVisible(true);
        
        if (propertyDlg.getResult() == ModelPropertyDlg.OK_RESULT) {
            
            ModelProperties.ModelProperty property = view.getModelProperties().createProperty();
            
            String newGroupName = propertyDlg.getGroup();
            
            property.name = propertyDlg.getName();
            property.description = propertyDlg.getDescription();
            property.lowerBound = propertyDlg.getLowerBound();
            property.upperBound = propertyDlg.getUpperBound();
            property.component = propertyDlg.getComponent();
            property.attribute = propertyDlg.getAttribute();
            property.var = propertyDlg.getVar();
            
            ModelProperties.Group group = view.getModelProperties().getGroup(newGroupName);
            view.getModelProperties().addProperty(group, property);
            
            updateGroup(group);
            groupPanels.get(group).updateUI();
        }
    }
    
    private void editProperty(ModelProperty property) {
        
        ModelProperties.Group group = property.getGroup();
        
        propertyDlg.update(view.getModelProperties().getGroupNames(), view.getComponentDescriptors(), property, group.getName());
        propertyDlg.setVisible(true);
        
        if (propertyDlg.getResult() == ModelPropertyDlg.OK_RESULT) {
            String newGroupName = propertyDlg.getGroup();
            
            property.name = propertyDlg.getName();
            property.description = propertyDlg.getDescription();
            property.lowerBound = propertyDlg.getLowerBound();
            property.upperBound = propertyDlg.getUpperBound();
            property.component = propertyDlg.getComponent();
            property.attribute = propertyDlg.getAttribute();
            property.var = propertyDlg.getVar();
            
            ModelProperties.Group newGroup = view.getModelProperties().getGroup(newGroupName);
            
            if (!newGroup.equals(group)) {
                view.getModelProperties().removePropertyFromGroup(group, property);
                view.getModelProperties().addPropertyToGroup(newGroup, property);
                
                updateGroup(newGroup);
            }
            
            updateGroup(group);
        }
    }
    
    public boolean verifyInputs() {
        
        // verify all provided values
        for (ModelProperty p : inputMap.keySet()) {
            InputComponent ic = inputMap.get(p);
            if (!ic.verify()) {
                
                // find containing scroll pane and select it
                JScrollPane scrollPane = groupPanes.get(p.getGroup());
                tabbedPane.setSelectedComponent(scrollPane);
                
                Color oldColor = ic.getComponent().getBackground();
                ic.getComponent().setBackground(new Color(255, 0, 0));
                
                if (ic.getErrorCode() == InputComponent.INPUT_OUT_OF_RANGE) {
                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Selected value out of range!", "Range error");
                } else {
                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Invalid data format!", "Format error");
                }
                
                ic.getComponent().setBackground(oldColor);
                return false;
            }
        }
        return true;
    }
    
    public void updateProperties() {
        
        // set values of properties to provided
        for (ModelProperty property : inputMap.keySet()) {
            InputComponent ic = inputMap.get(property);
            property.value = ic.getValue();
        }
    }
    
    class PropertyButton extends JButton {
        
        ModelProperty property;
        
        public PropertyButton(ModelProperty property) {
            super();
            this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            this.setPreferredSize(new Dimension(20,14));
            this.property = property;
        }
    }
}
