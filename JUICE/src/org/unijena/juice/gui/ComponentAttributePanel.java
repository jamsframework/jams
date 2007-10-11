/*
 * ComponentAttributeConfigPanel.java
 * Created on 28. September 2007, 22:38
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.juice.*;
import org.unijena.juice.ComponentDescriptor;
import org.unijena.juice.ComponentDescriptor.ComponentAttribute;
import org.unijena.juice.ContextAttribute;

/**
 *
 * @author Sven Kralisch
 *
 * This panel provides GUI components for editing a component's attributes connections
 */
public class ComponentAttributePanel extends JPanel {
    
    private JComboBox contextCombo;
    private InputComponent valueInput;
    private GridBagLayout connectionLayout, infoLayout;
    private JTextField localNameText, compText, linkText, customAttributeText;
    private JPanel listPanel, infoPanel, valuePanel;
    private ModelView view;
    private Class type;
    private JList attributeList;
    private JToggleButton linkButton, setButton;
    private ComponentAttribute var;
    
    
    public ComponentAttributePanel(ModelView view) {
        
        this.view = view;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        connectionLayout = new GridBagLayout();
        
        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(100,245));
        
        infoPanel = new JPanel();
        //infoPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        //infoPanel.setPreferredSize(new Dimension(250,245));
        infoLayout = new GridBagLayout();
        infoLayout.preferredLayoutSize(infoPanel);
        infoPanel.setLayout(infoLayout);
        
        JPanel detailPanel = new JPanel();
        detailPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        detailPanel.setPreferredSize(new Dimension(420,245));
        detailPanel.add(infoPanel);
        
        this.add(detailPanel);
        this.add(listPanel);
        
        valuePanel = new JPanel();
        valuePanel.setBorder(BorderFactory.createEtchedBorder());
//        valuePanel.setLayout(new BorderLayout());
        valuePanel.setPreferredSize(new Dimension(250, 180));
        
        LHelper.addGBComponent(infoPanel, infoLayout, new JLabel("Component:"), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, infoLayout, new JLabel("Local name:"), 0, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, infoLayout, new JLabel("Linkage:"), 0, 15, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, infoLayout, new JPanel(), 0, 17, 1, 1, 0, 0);
        LHelper.addGBComponent(infoPanel, infoLayout, new JLabel("Value:"), 0, 20, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
        
        compText = new JTextField();
        compText.setEditable(false);
        compText.setBorder(BorderFactory.createEtchedBorder());
        compText.setPreferredSize(new Dimension(300, 20));
        LHelper.addGBComponent(infoPanel, infoLayout, compText, 1, 0, 1, 1, 0, 0);
        
        localNameText = new JTextField();
        localNameText.setEditable(false);
        localNameText.setBorder(BorderFactory.createEtchedBorder());
        localNameText.setPreferredSize(new Dimension(300, 20));
        LHelper.addGBComponent(infoPanel, infoLayout, localNameText, 1, 10, 1, 1, 0, 0);
        
        linkText = new JTextField();
        linkText.setEditable(false);
        linkText.setBorder(BorderFactory.createEtchedBorder());
        linkText.setPreferredSize(new Dimension(300, 20));
        LHelper.addGBComponent(infoPanel, infoLayout, linkText, 1, 15, 1, 1, 0, 0);
        
        linkButton = new JToggleButton("LINK");
        linkButton.setMargin(new Insets(1, 1, 1, 1));
        linkButton.setFocusable(false);
        linkButton.setPreferredSize(new Dimension(30,20));
        linkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAttributeLink();
            }
        });
        LHelper.addGBComponent(infoPanel, infoLayout, linkButton, 2, 15, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTH);
        
        setButton = new JToggleButton("SET");
        setButton.setMargin(new Insets(1, 1, 1, 1));
        setButton.setFocusable(false);
        setButton.setPreferredSize(new Dimension(30,20));
        LHelper.addGBComponent(infoPanel, infoLayout, setButton, 2, 20, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTH);
        
        
        contextCombo = new JComboBox();
        contextCombo.setBorder(BorderFactory.createEtchedBorder());
        contextCombo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRepository();
                }
            }
        });
        listPanel.add(contextCombo, BorderLayout.NORTH);
        
        JPanel customContextPanel = new JPanel();
        customContextPanel.setLayout(new BoxLayout(customContextPanel, BoxLayout.Y_AXIS));
        customAttributeText = new JTextField();
        customAttributeText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setAttributeLink();
            }
            public void insertUpdate(DocumentEvent e) {
                setAttributeLink();
            }
            public void removeUpdate(DocumentEvent e) {
                setAttributeLink();
            }
        });
        customAttributeText.setBorder(BorderFactory.createEtchedBorder());
        
        //customContextPanel.add(contextCombo);
        customContextPanel.add(new JLabel("Custom Attribute:"));
        customContextPanel.add(customAttributeText);
        listPanel.add(customContextPanel, BorderLayout.SOUTH);
        
        attributeList = new JList();
        attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(attributeList);
        attributeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Object o = attributeList.getSelectedValue();
                    if (o != null) {
                        customAttributeText.setText(o.toString());
                    } else {
                        customAttributeText.setText("");
                    }
                }
            }
        });
        listPanel.add(listScroll, BorderLayout.CENTER);
        
    }
    
    private void setAttributeLink() {
        if (linkButton.isSelected() && !customAttributeText.getText().equals("")) {

            
            linkText.setText(var.getContext() + " -> " + var.getContextAttribute());
            //linkText.setText(contextCombo.getSelectedItem() + " -> " + customAttributeText.getText());

        } else {
            linkText.setText("");
        }
    }
    
    
    public void update(ComponentAttribute var, String ancestorNames[], ComponentDescriptor component) {
        
        this.var = var;
        this.type = var.type;
        
        this.contextCombo.setModel(new DefaultComboBoxModel(ancestorNames));
        
        updateRepository();
        
        if (valueInput != null) {
            infoPanel.remove(valueInput.getComponent());
            infoPanel.updateUI();
        }
        
        valueInput = LHelper.createInputComponent(var.type.getSimpleName());
        LHelper.addGBComponent(infoPanel, infoLayout, valueInput.getComponent(), 1, 20, 1, 1, 0, 0);
        localNameText.setText(var.name);
        compText.setText(component.getName());
        
        this.valueInput.setValue(var.getValue());
        
        if (var.getContext() != null) {
            contextCombo.setSelectedItem(var.getContext().getName());
            attributeList.setSelectedValue(var.getAttribute().toString(), true);
            linkText.setText(var.getContext().getName() + " -> " + var.getAttribute().toString());
            linkButton.setSelected(true);
        } else {
            linkText.setText(null);
            linkButton.setSelected(false);
        }
        
        if (var.accessType == var.READ_ACCESS) {
            customAttributeText.setEnabled(false);
        } else {
            customAttributeText.setEnabled(true);
        }
        
        if (var.getValue() != "") {
            valueInput.getComponent().setEnabled(true);
            setButton.setSelected(true);
        } else {
            valueInput.getComponent().setEnabled(false);
            setButton.setSelected(false);
        }
    }
    
    private void updateRepository() {
        
        ComponentDescriptor context = this.getContext();
        
        AttributeRepository repo = context.getDataRepository();
        //ArrayList<Attribute> attributes = repo.getAttributesByType(type);
        ArrayList<ContextAttribute> attributes = repo.getUniqueAttributesByType(type);
        
        DefaultListModel lModel = new DefaultListModel();
        if (attributes != null) {
            
            //sort the list
            Collections.sort(attributes, new Comparator<ContextAttribute>() {
                public int compare(ContextAttribute a1, ContextAttribute a2) {
                    return a1.toString().compareTo(a2.toString());
                }
            });
            
            for (int i = 0; i < attributes.size(); i++) {
                lModel.addElement(attributes.get(i).toString());
            }
        }
        
        attributeList.setModel(lModel);
    }
    
    public ComponentDescriptor getContext() {
        return view.getComponentDescriptor((String) contextCombo.getSelectedItem());
    }
    
    public void cleanup() {
        contextCombo.setModel(new DefaultComboBoxModel());
        attributeList.setModel(new DefaultListModel());
        localNameText.setText(null);
        compText.setText(null);
        linkText.setText(null);
        if (valueInput != null) {
            infoPanel.remove(valueInput.getComponent());
            infoPanel.updateUI();
        }
    }
    
}
