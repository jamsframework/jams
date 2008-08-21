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
package org.unijena.juice.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.jams.io.ParameterProcessor;
import org.unijena.juice.*;
import org.unijena.juice.ModelProperties.Group;
import org.unijena.juice.ModelProperties.ModelElement;
import org.unijena.juice.ModelProperties.ModelProperty;

/**
 *
 * @author S. Kralisch
 */
public class LauncherPanel extends JPanel {

    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(9, 5, Image.SCALE_SMOOTH));
    private static final Dimension BUTTON_DIMENSION = new Dimension(110, 20);
    private JTabbedPane tabbedPane = new JTabbedPane();
    private HashMap<ModelProperty, InputComponent> inputMap = new HashMap<ModelProperty, InputComponent>();
    private ModelPropertyDlg propertyDlg = new ModelPropertyDlg(JUICE.getJuiceFrame());
    private ModelSubgroupDlg subgroupDlg = new ModelSubgroupDlg(JUICE.getJuiceFrame());
//    private GroupEditDlg groupEditDlg = new GroupEditDlg(JUICE.getJuiceFrame());
    private HashMap<ModelProperties.Group, JPanel> groupPanels;
    private HashMap<ModelProperties.Group, JScrollPane> groupPanes;
    private JPanel mainButtonPanel = new JPanel();
    private ModelView view;
    private Font titledBorderFont;

    /**
     * JPanel providing visual builder for defining a JAMS models GUI
     * 
     * @param view the ModelView object this LauncherPanel is associated to
     */
    public LauncherPanel(ModelView view) {

        this.view = view;
        this.tabbedPane.setTabPlacement(JTabbedPane.LEFT);

        //setBorder(BorderFactory.createTitledBorder("Model Parameters"));
        setLayout(new BorderLayout());

        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(mainButtonPanel, BorderLayout.NORTH);

        // create some nice font for the border title
        titledBorderFont = (Font) UIManager.getDefaults().get("TitledBorder.font");
        int fontSize = titledBorderFont.getSize();
        if (titledBorderFont.getStyle() == Font.BOLD) {
            fontSize += 2;
        }
        titledBorderFont = new Font(titledBorderFont.getName(), Font.BOLD, fontSize);

        JButton addPropertyButton = new JButton("Add Property");
        addPropertyButton.setPreferredSize(BUTTON_DIMENSION);
        addPropertyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addProperty();
            }
        });

        JButton addSubgroupButton = new JButton("Add Subgroup");
        addSubgroupButton.setPreferredSize(BUTTON_DIMENSION);
        addSubgroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addSubgroup();
            }
        });

        JButton addGroupButton = new JButton("Add Group");
        addGroupButton.setPreferredSize(BUTTON_DIMENSION);
        addGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addGroup();
            }
        });

        JButton moveupGroupButton = new JButton("Group up");
        moveupGroupButton.setPreferredSize(BUTTON_DIMENSION);
        moveupGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                moveupGroup();
            }
        });

        JButton movedownGroupButton = new JButton("Group down");
        movedownGroupButton.setPreferredSize(BUTTON_DIMENSION);
        movedownGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                movedownGroup();
            }
        });

        JButton editGroupButton = new JButton("Edit Group");
        editGroupButton.setPreferredSize(BUTTON_DIMENSION);
        editGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editGroup();
            }
        });

        JButton delGroupButton = new JButton("Remove Group");
        delGroupButton.setPreferredSize(BUTTON_DIMENSION);
        delGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteGroup();
            }
        });

        GridBagLayout gblButton = new GridBagLayout();
        JPanel innerButtonPanel = new JPanel();
        innerButtonPanel.setLayout(gblButton);
        //innerButtonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        LHelper.addGBComponent(innerButtonPanel, gblButton, addPropertyButton, 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, addSubgroupButton, 0, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, addGroupButton, 1, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, editGroupButton, 1, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, moveupGroupButton, 2, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, movedownGroupButton, 2, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(innerButtonPanel, gblButton, delGroupButton, 3, 0, 1, 1, 0, 0);

        mainButtonPanel.add(innerButtonPanel);

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
        if (index < tabbedPane.getTabCount() - 1) {

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

    public void updateGroup(Group group) {

        if (group.isSubGroup()) {
            updateGroup(group.getGroup());
            return;
        }
        updateProperties();

        GridBagLayout gbl = new GridBagLayout();

        JPanel contentPanel = groupPanels.get(group);

        contentPanel.removeAll();
        contentPanel.setLayout(gbl);

        int y = 1;

        Vector properties = group.getProperties();
        for (int j = 0; j < properties.size(); j++) {
            Object modelElement = properties.get(j);

            // <@todo> groups consists of subgroups and properties,
            //          subgroups consists of properties
            //          this could be recursive too
            if (modelElement instanceof ModelProperty) {

                ModelProperty property = (ModelProperty) modelElement;
                JPanel buttonPanel = createPropertyButtonPanel(contentPanel, gbl, property, y);
                LHelper.addGBComponent(contentPanel, gbl, buttonPanel, 3, y, 1, 1, 1, 1);
            }
            if (modelElement instanceof Group) {
                Group subgroup = (Group) modelElement;
                Vector subgroupProperties = subgroup.getProperties();

                int height = subgroupProperties.size() + 3;

                // create the subgroup panel
                JPanel subgroupPanel = new JPanel(gbl);

                // create and set the border
                subgroupPanel.setBorder(BorderFactory.createTitledBorder(null, subgroup.getCanonicalName(),
                        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, titledBorderFont));

                // add the subgroup panel
                LHelper.addGBComponent(contentPanel, gbl, subgroupPanel,
                        0, y, 3, height,
                        6, 2, 6, 2,
                        1, 1);

                JPanel sgButtonPanel = createSubgroupButtonPanel(subgroup);
                LHelper.addGBComponent(contentPanel, gbl, sgButtonPanel, 3, y, 3, 1, 1, 1);
                int row = y + 1;
                for (int k = 0; k < subgroupProperties.size(); k++) {
                    Object subgroupElement = subgroupProperties.get(k);

                    if (subgroupElement instanceof ModelProperty) {
                        row++;

                        ModelProperty subgroupProperty = (ModelProperty) subgroupElement;
                        JPanel buttonPanel = createPropertyButtonPanel(subgroupPanel, gbl, subgroupProperty, row);
                        LHelper.addGBComponent(subgroupPanel, gbl, buttonPanel, 3, row, 3, 1, 1, 1);
                    }
                }
                row = row + 2;
                y = row;

            }
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

    /**
     * createPropertyButtonPanel
     * @param contentPanel - the content panel
     * @param gbl - the layout
     * @param property - the property
     * @param row - row number of actual row
     * @return buttonPanel
     */
    private JPanel createPropertyButtonPanel(JPanel contentPanel, GridBagLayout gbl, ModelProperty property, int row) {

        JPanel buttonPanel = new JPanel();
        InputComponent ic;

        // create a label with the property's name and some space in front of it
        JLabel nameLabel = new JLabel(property.name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        LHelper.addGBComponent(contentPanel, gbl, nameLabel, 0, row, 1, 1, 0, 0);

        if (property.var != null) {
            ic = LHelper.createInputComponent(property.var.type.getSimpleName());
        } else if (property.attribute != null) {
            ic = LHelper.createInputComponent(property.attribute.getType().getSimpleName());
        } else {
            ic = LHelper.createInputComponent(JUICE.JAMS_DATA_TYPES[0].getSimpleName());
        }
        ic.setRange(property.lowerBound, property.upperBound);
        ic.setLength(property.length);
        ic.getComponent().setToolTipText(property.description);
        ic.setValue(property.value);

        if ((property.attribute == null) && (property.var == null)) {
            ic.getComponent().setEnabled(true);            
        } else {
            ic.getComponent().setEnabled(false);
        }

        inputMap.put(property, ic);
        LHelper.addGBComponent(contentPanel, gbl, (Component) ic, 1, row, 2, 1, 1, 1);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());

        ElementButton downButton = new ElementButton(property);
        downButton.setToolTipText("Move down");
        downButton.setIcon(DOWN_ICON);
        downButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                moveDownElement(button.element);
            }
        });
        buttonPanel.add(downButton);

        ElementButton upButton = new ElementButton(property);
        upButton.setToolTipText("Move up");
        upButton.setIcon(UP_ICON);
        upButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                moveUpElement(button.element);
            }
        });
        buttonPanel.add(upButton);

        ElementButton delButton = new ElementButton(property);
        delButton.setToolTipText("Delete");
        delButton.setText("-");
        delButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                deleteElement(button.element);
            }
        });
        buttonPanel.add(delButton);

        ElementButton editButton = new ElementButton(property);
        editButton.setToolTipText("Edit");
        editButton.setText("...");
        editButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                editProperty((ModelProperty) button.element);
            }
        });
        buttonPanel.add(editButton);

        return buttonPanel;
    }

    private JPanel createSubgroupButtonPanel(Group subgroup) {

        JPanel buttonPanel = new JPanel();

        buttonPanel.setBorder(BorderFactory.createEmptyBorder());

        ElementButton downButton = new ElementButton(subgroup);
        downButton.setToolTipText("Move down");
        downButton.setIcon(DOWN_ICON);
        downButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                moveDownElement(button.element);
            }
        });
        buttonPanel.add(downButton);

        ElementButton upButton = new ElementButton(subgroup);
        upButton.setToolTipText("Move up");
        upButton.setIcon(UP_ICON);
        upButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                moveUpElement(button.element);
            }
        });
        buttonPanel.add(upButton);

        ElementButton delButton = new ElementButton(subgroup);
        delButton.setToolTipText("Delete");
        delButton.setText("-");
        delButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                deleteElement(button.element);
            }
        });
        buttonPanel.add(delButton);

        ElementButton editButton = new ElementButton(subgroup);
        editButton.setToolTipText("Edit");
        editButton.setText("...");
        editButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ElementButton button = (ElementButton) e.getSource();
                editSubgroup((Group) button.element);
            }
        });
        buttonPanel.add(editButton);

        return buttonPanel;
    }

    private void moveDownElement(ModelElement element) {

        Vector<Object> list = element.getGroup().getProperties();
        int index = list.indexOf(element);
        if (index < list.size() - 1) {
            list.remove(index);
            list.add(index + 1, element);
        }

        Group mainGroup = element.getMainGroup();
        updateGroup(mainGroup);
        JPanel contentPanel = groupPanels.get(mainGroup);
        contentPanel.updateUI();
    }

    private void moveUpElement(ModelElement element) {

        Vector<Object> list = element.getGroup().getProperties();
        int index = list.indexOf(element);
        if (index > 0) {
            list.remove(index);
            list.add(index - 1, element);
        }

        Group mainGroup = element.getMainGroup();
        updateGroup(mainGroup);
        JPanel contentPanel = groupPanels.get(mainGroup);
        contentPanel.updateUI();
    }

    private void deleteElement(ModelElement element) {

        Vector list = element.getGroup().getProperties();

        // subgroup has to be empty
        if (element instanceof Group) {
            Vector test = ((Group) element).getProperties();
            if (test.size() > 0) {
                LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Subgroup has to be empty.", "Deletion not possible");
                return;
            }
        }

        int result = LHelper.showYesNoDlg(JUICE.getJuiceFrame(), "Really delete this property?", "Delete property");
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        list.remove(element);

        Group mainGroup = element.getMainGroup();
        JPanel contentPanel = groupPanels.get(mainGroup);
        updateGroup(mainGroup);
        contentPanel.updateUI();
    }

    private void addProperty() {

        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String groupName = tabbedPane.getTitleAt(index);

        propertyDlg.update(view.getModelProperties().getAllGroupNames(), view.getComponentDescriptors(), null, groupName);
        propertyDlg.setVisible(true);

        if (propertyDlg.getResult() == ModelPropertyDlg.OK_RESULT) {

            ModelProperties.ModelProperty property = view.getModelProperties().createProperty();

            String newGroupName = propertyDlg.getGroup();

            property.name = propertyDlg.getName();
            property.description = propertyDlg.getDescription();
            property.lowerBound = propertyDlg.getLowerBound();
            property.upperBound = propertyDlg.getUpperBound();
            property.length = propertyDlg.getLength();
            property.getHelpComponent().setHelpURL(propertyDlg.getHelpURL());
            property.getHelpComponent().setHelpText(propertyDlg.getHelpText());

            property.component = propertyDlg.getComponent();
            property.attribute = propertyDlg.getAttribute();
            property.var = propertyDlg.getVar();

            ModelProperties.Group group = view.getModelProperties().getGroup(newGroupName);
            view.getModelProperties().addProperty(group, property);
            if (group.isSubGroup()) {
                group = group.getGroup();
            }
            updateGroup(group);
            groupPanels.get(group).updateUI();
        }
    }

    private void addSubgroup() {
        int index = tabbedPane.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String groupName = tabbedPane.getTitleAt(index);

        subgroupDlg.update(view.getModelProperties().getGroupNames(), null, groupName);
        subgroupDlg.setVisible(true);

        if (subgroupDlg.getResult() == ModelSubgroupDlg.OK_RESULT) {

            groupName = subgroupDlg.getGroup();
            ModelProperties.Group group = view.getModelProperties().getGroup(groupName);

            String subgroubName = subgroupDlg.getName();

            ModelProperties.Group subgroup = view.getModelProperties().createSubgroup(group, subgroubName);
            subgroup.getHelpComponent().setHelpURL(subgroupDlg.getHelpURL());
            subgroup.getHelpComponent().setHelpText(subgroupDlg.getHelpText());

            updateGroup(group);
            groupPanels.get(group).updateUI();
        }
    }

    private void editProperty(ModelProperty property) {

        ModelProperties.Group group = property.getGroup();

        propertyDlg.update(view.getModelProperties().getAllGroupNames(), view.getComponentDescriptors(), property, group.getName());
        propertyDlg.setVisible(true);

        if (propertyDlg.getResult() == ModelPropertyDlg.OK_RESULT) {
            String newGroupName = propertyDlg.getGroup();

            property.name = propertyDlg.getName();
            property.description = propertyDlg.getDescription();
            property.lowerBound = propertyDlg.getLowerBound();
            property.upperBound = propertyDlg.getUpperBound();
            property.length = propertyDlg.getLength();
            property.getHelpComponent().setHelpURL(propertyDlg.getHelpURL());
            property.getHelpComponent().setHelpText(propertyDlg.getHelpText());
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

    private void editSubgroup(Group subgroup) {

        ModelProperties.Group group = subgroup.getGroup();

        subgroupDlg.update(view.getModelProperties().getGroupNames(), subgroup, group.getName());
        subgroupDlg.setVisible(true);

        if (subgroupDlg.getResult() == ModelSubgroupDlg.OK_RESULT) {
            String newGroupName = subgroupDlg.getGroup();

            subgroup.name = subgroupDlg.getName();
            subgroup.getHelpComponent().setHelpURL(subgroupDlg.getHelpURL());
            subgroup.getHelpComponent().setHelpText(subgroupDlg.getHelpText());

            ModelProperties.Group newGroup = view.getModelProperties().getGroup(newGroupName);

            if (!newGroup.equals(group)) {
                view.getModelProperties().removePropertyFromGroup(group, subgroup);
                view.getModelProperties().addPropertyToGroup(newGroup, subgroup);

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
                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Invalid value!", "Format error");
                }

                ic.getComponent().setBackground(oldColor);
                return false;
            }
        }
        updateProperties();
        return true;
    }

    public void updateProperties() {

        // set values of properties to provided
        for (ModelProperty property : inputMap.keySet()) {
            InputComponent ic = inputMap.get(property);
            property.value = ic.getValue();
        }
    }

    class ElementButton extends JButton {

        ModelElement element;

        public ElementButton(ModelElement element) {
            super();
            this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            this.setPreferredSize(new Dimension(20, 14));
            this.element = element;
        }
    }
}
