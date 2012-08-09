/*
 * ComponentAttributePanel.java
 * Created on 28. September 2007, 22:38
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import jams.gui.tools.GUIHelper;
import jams.gui.input.InputComponent;
import jams.gui.input.InputComponentFactory;
import jams.JAMS;
import jams.JAMSException;
import jams.data.JAMSDataFactory;
import jams.gui.input.*;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ComponentField.AttributeLinkException;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.tools.StringTools;
import jamsui.juice.JUICE;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.Position;

/**
 *
 * @author Sven Kralisch
 *
 * This panel provides GUI components for editing a component's attributes
 * connections
 */
public class ComponentAttributePanel extends JPanel {

    private JComboBox contextCombo;
    private InputComponent valueInput;
    private GridBagLayout infoLayout;
    private JTextField localNameText, linkText;
    private JButton customAttributeButton;
    private JPanel listPanel, infoPanel, valuePanel;
    private Class type;
    private JList attributeList;
    private JToggleButton linkButton, setButton;
    private ComponentField field;
    private TableModel tableModel;
    private int selectedRow;
    private ActionListener linkButtonListener, setButtonListener;
    private ItemListener contextComboListener;
    private DocumentListener customAttributeTextListener;
    private ListSelectionListener attributeListListener;
    private boolean adjusting = false;

    public ComponentAttributePanel() {

        this.setLayout(new BorderLayout());

        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(160, 200));
        
        infoPanel = new JPanel();
        infoLayout = new GridBagLayout();
//        infoPanel.setBackground(Color.green);
        infoPanel.setLayout(infoLayout);

        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBackground(Color.blue);

        this.add(infoPanel, BorderLayout.CENTER);
        this.add(listPanel, BorderLayout.EAST);

//        GUIHelper.addGBComponent(infoPanel, infoLayout, new JLabel(JAMS.i18n("Component:")), 0, 0, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
        GUIHelper.addGBComponent(infoPanel, infoLayout, new JLabel(JAMS.i18n("Local_name:")), 0, 10, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
        GUIHelper.addGBComponent(infoPanel, infoLayout, new JLabel(JAMS.i18n("Link:")), 0, 15, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
        GUIHelper.addGBComponent(infoPanel, infoLayout, new JLabel(JAMS.i18n("Value:")), 0, 20, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

        localNameText = new JTextField();
        localNameText.setEditable(false);
        GUIHelper.addGBComponent(infoPanel, infoLayout, localNameText, 1, 10, 1, 1, 0, 0);

        linkText = new JTextField();
        linkText.setEditable(false);
        linkText.setPreferredSize(new Dimension(320, 0));
        GUIHelper.addGBComponent(infoPanel, infoLayout, linkText, 1, 15, 1, 1, 1, 0);

        linkButton = new JToggleButton("LINK");
        linkButton.setMargin(new Insets(1, 1, 1, 1));
        linkButton.setFocusable(false);
        linkButton.setPreferredSize(new Dimension(40, 20));

        GUIHelper.addGBComponent(infoPanel, infoLayout, linkButton, 2, 15, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTH);

        valuePanel = new JPanel();
//        valuePanel.setBackground(Color.yellow);
        valuePanel.setLayout(new BorderLayout());
        valuePanel.setPreferredSize(new Dimension(320, 140));

        GUIHelper.addGBComponent(infoPanel, infoLayout, valuePanel, 1, 20, 1, 1, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

        setButton = new JToggleButton("SET");
        setButton.setMargin(new Insets(1, 1, 1, 1));
        setButton.setFocusable(false);
        setButton.setPreferredSize(new Dimension(40, 20));

        GUIHelper.addGBComponent(infoPanel, infoLayout, setButton, 2, 20, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTH);

        contextCombo = new JComboBox();
        listPanel.add(contextCombo, BorderLayout.NORTH);

        customAttributeButton = new JButton(JAMS.i18n("Custom_Attribute"));
        customAttributeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ContextDescriptor context = (ContextDescriptor) contextCombo.getSelectedItem();
                    String name = GUIHelper.showInputDlg(JUICE.getJuiceFrame(), JAMS.i18n("Custom_Attribute"), null);
                    if (name == null) {
                        return;
                    }
                    if (type.isArray()) {
                        context.addDynamicAttribute(name, JAMSDataFactory.getBelongingInterface(type.getComponentType()));
                    } else {
                        context.addDynamicAttribute(name, JAMSDataFactory.getBelongingInterface(type));
                    }
                    updateRepository();
                    updateAttributeLinkGUI();
                    DefaultListModel model = (DefaultListModel) attributeList.getModel();
                    int listIndex = attributeList.getNextMatch(name, 0, Position.Bias.Forward);
                    attributeList.scrollRectToVisible(attributeList.getCellBounds(listIndex, listIndex));
                } catch (JAMSException ex) {
                    JUICE.getExHandler().handle(ex);
                }
            }
        });

        listPanel.add(customAttributeButton, BorderLayout.SOUTH);

        attributeList = new JList();
        JScrollPane listScroll = new JScrollPane(attributeList);

        listPanel.add(listScroll, BorderLayout.CENTER);


        createListeners();
        addListeners();

        cleanup();

    }

    private void setAttributeLink() {

        if (adjusting) {
            return;
        }

        String attributeString = "";
        List<String> l = attributeList.getSelectedValuesList();
        if (!l.isEmpty()) {
            for (String s : l) {
                attributeString += ";" + s;
            }
            attributeString = attributeString.substring(1);
        }

        ContextDescriptor context = (ContextDescriptor) contextCombo.getSelectedItem();

        if (!attributeString.equals("") && context != null) {
            linkButton.setEnabled(true);
        } else {
            linkButton.setEnabled(false);
        }

        if (linkButton.isSelected() && !attributeString.equals("") && (context != null)) {
            try {
                //@TODO: proper handling
                field.linkToAttribute(context, attributeString);
            } catch (AttributeLinkException ex) {
                JUICE.getExHandler().handle(ex);
            }
            linkText.setText(field.getContext() + "." + field.getAttribute());
            tableModel.setValueAt(field.getContext() + "." + field.getAttribute(), selectedRow, 3);
        }

        if (!linkButton.isSelected()) {
            field.unlinkFromAttribute();
            linkText.setText("");
            tableModel.setValueAt("", selectedRow, 3);
        }

    }

    private void setAttributeValue() {

        if (adjusting) {
            return;
        }

        if (!valueInput.verify()) {
            return;
        }

        if (valueInput.getValue().equals("")) {
            setButton.setSelected(false);
            return;
        }

        if (setButton.isSelected()) {
            //valueInput.getComponent().setEnabled(false);
            field.setValue(valueInput.getValue());
        } else {
            //valueInput.getComponent().setEnabled(true);
            field.setValue("");
        }
        tableModel.setValueAt(field.getValue(), selectedRow, 4);
    }

    private void updateAttributeLinkGUI() {

        //adjust context input components according to context attributes
        if (field.getContextAttributes().isEmpty()) {
            linkButton.setSelected(false);
            linkText.setText("");

            if (contextCombo.getItemCount() > 0) {
                contextCombo.setSelectedIndex(0);
            }
            //contextCombo.setSelectedItem(null);
            attributeList.setSelectedValue(null, true);
        } else {
            linkButton.setSelected(true);
            linkText.setText(field.getContext() + "." + field.getAttribute());
            contextCombo.setSelectedItem(field.getContext());

            if (type.isArray()) {

                String[] values = field.getAttribute().split(";");
                ArrayList<String> valueList = new ArrayList<String>();
                for (String value : values) {
                    valueList.add(value);
                }

                int indices[] = new int[values.length];
                int c = 0;

                for (int i = 0; i < attributeList.getModel().getSize(); i++) {
                    String value = (String) attributeList.getModel().getElementAt(i);
                    if (valueList.contains(value)) {
                        indices[c++] = i;
                    }
                }

                attributeList.setSelectedIndices(indices);

//                for (String value : values) {
//                    attributeList.setSelectedValue(value, true);
//                }

            } else {
                // @todo: should stay empty if attribute not provided by some 
                // context -- workaround for errorneous model files

                attributeList.setSelectedValue(field.getAttribute().toString(), true);
            }
        }

        //adjust var input components according to var values
        if (StringTools.isEmptyString(field.getValue())) {
            setButton.setSelected(false);
            //valueInput.getComponent().setEnabled(true);
        } else {
            setButton.setSelected(true);
            //valueInput.getComponent().setEnabled(false);
        }
        valueInput.setValue(field.getValue());

        if (attributeList.isSelectionEmpty()) {
            linkButton.setEnabled(false);
        } else {
            linkButton.setEnabled(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void update(ComponentField var, ComponentDescriptor ancestorArray[],
            ComponentDescriptor component, TableModel tableModel, int selectedRow) {

        adjusting = true;

        this.field = var;
        this.type = var.getType();
        this.tableModel = tableModel;
        this.selectedRow = selectedRow;

        //set component's and var's name
        localNameText.setText(var.getName());
//        compNameText.setText(component.getInstanceName());

        //fill the context combo box
        this.contextCombo.setModel(new DefaultComboBoxModel(ancestorArray));
        updateRepository();

        //enable field for custom attribute name if !READ_ACCESS        
        if ((var.getAccessType() == ComponentField.READ_ACCESS) && !type.isArray()) {
            // @todo: this should be disabled since some other context must
            // provide this attribute -- workaround for incomplete attributes list
            customAttributeButton.setEnabled(true);
        } else {
            customAttributeButton.setEnabled(true);
        }

        //remove existing input component if necessary
        if (valueInput != null) {
            valuePanel.remove(valueInput.getComponent());
        }

        //create value input component
        valueInput = InputComponentFactory.createInputComponent(var.getType(), true);
        valuePanel.add(valueInput.getComponent(), BorderLayout.NORTH);
        valuePanel.updateUI();

        //enable set-value-button (disabled, when no var is displayed)
        setButton.setEnabled(true);

        //init gui according to the component's settings
        updateAttributeLinkGUI();

        valueInput.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChanged() {
                setAttributeValue();
            }
        });

        adjusting = false;
    }

    @SuppressWarnings("unchecked")
    private void updateRepository() {

        ContextDescriptor context = (ContextDescriptor) contextCombo.getSelectedItem();

        if (context == null) {
            return;
        }

        ArrayList<ContextAttribute> attributes = new ArrayList<ContextAttribute>();

        for (ContextAttribute attribute : context.getAttributes(type).values()) {
            attributes.add(attribute);
        }
        Collections.sort(attributes, new Comparator<ContextAttribute>() {

            @Override
            public int compare(ContextAttribute a1, ContextAttribute a2) {
                return a1.toString().compareTo(a2.toString());
            }
        });


        if (type.isArray()) {
            //attributes.addAll(repo.getUniqueAttributesByType(type.getComponentType()));
        }

        DefaultListModel lModel = new DefaultListModel();
        if (attributes != null) {

            //sort the list
            Collections.sort(attributes, new Comparator<ContextAttribute>() {

                @Override
                public int compare(ContextAttribute a1, ContextAttribute a2) {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            //add all elements to the list model
            for (int i = 0; i < attributes.size(); i++) {

                String attributeName = attributes.get(i).toString();

                if (false && attributeName.contains(";")) {
                    StringTokenizer tok = new StringTokenizer(attributeName, ";");
                    while (tok.hasMoreTokens()) {
                        lModel.addElement(tok.nextToken());
                    }
                } else {
                    lModel.addElement(attributeName);
                }

            }
        }

        if (type.isArray()) {
            attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            attributeList.setEnabled(true);
        } else {
            attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            attributeList.setEnabled(true);
        }


        attributeList.setModel(lModel);
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        adjusting = true;

        contextCombo.setModel(new DefaultComboBoxModel());
        attributeList.setModel(new DefaultListModel());
        localNameText.setText(null);
//        compNameText.setText(null);
        linkText.setText(null);
        if (valueInput != null) {
            valuePanel.remove(valueInput.getComponent());
            valuePanel.updateUI();
        }

        linkButton.setSelected(false);
        linkButton.setEnabled(false);
        setButton.setSelected(false);
        setButton.setEnabled(false);
        customAttributeButton.setEnabled(false);

        adjusting = false;
    }

    private void createListeners() {

        linkButtonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAttributeLink();
            }
        };

        setButtonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setAttributeValue();
            }
        };

        contextComboListener = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRepository();
                }
            }
        };

        customAttributeTextListener = new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                setAttributeLink();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setAttributeLink();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setAttributeLink();
            }
        };

        attributeListListener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    setAttributeLink();
                }
            }
        };
    }

    private void removeListeners_() {
        linkButton.removeActionListener(linkButtonListener);
        setButton.removeActionListener(setButtonListener);
        contextCombo.removeItemListener(contextComboListener);
        attributeList.removeListSelectionListener(attributeListListener);
    }

    private void addListeners() {
        linkButton.addActionListener(linkButtonListener);
        setButton.addActionListener(setButtonListener);
        contextCombo.addItemListener(contextComboListener);
        attributeList.addListSelectionListener(attributeListListener);

    }

}
