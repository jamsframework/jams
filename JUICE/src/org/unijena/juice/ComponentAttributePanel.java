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

package org.unijena.juice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.juice.DataRepository.Attribute;
import org.unijena.juice.tree.ComponentDescriptor;

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
    private JTextField localNameText, compText, linkText;
    private JPanel listPanel, infoPanel, valuePanel;
    private ModelView view;
    private Class type;
    private JList attributeList;
    private JToggleButton linkButton, setButton;
    
    
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
        LHelper.addGBComponent(infoPanel, infoLayout, new JLabel("Value:"), 0, 20, 1, 1, 0, 0);
        
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
        JPanel linkBtnPanel = new JPanel();
        linkBtnPanel.add(linkButton);
        LHelper.addGBComponent(infoPanel, infoLayout, linkBtnPanel, 2, 15, 1, 1, 0, 0);
        
        setButton = new JToggleButton("SET");
        setButton.setMargin(new Insets(1, 1, 1, 1));
        setButton.setFocusable(false);
        setButton.setPreferredSize(new Dimension(30,20));
        JPanel setBtnPanel = new JPanel();
        setBtnPanel.add(setButton);
        LHelper.addGBComponent(infoPanel, infoLayout, setBtnPanel, 2, 20, 1, 1, 0, 0);
        
        
        contextCombo = new JComboBox();
        contextCombo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRepository();
                }
            }
        });
        listPanel.add(contextCombo, BorderLayout.NORTH);
        
        attributeList = new JList();
        attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(attributeList);
        listPanel.add(listScroll, BorderLayout.CENTER);
        
    }
    
    public void update(ComponentDescriptor.ComponentAttribute var, String ancestorNames[], ComponentDescriptor component) {
        
        this.type = var.type;
        
        this.contextCombo.setModel(new DefaultComboBoxModel(ancestorNames));
        
        if (valueInput != null) {
            infoPanel.remove(valueInput.getComponent());
            infoPanel.updateUI();
        }
        
        valueInput = LHelper.createInputComponent(var.type.getSimpleName());
        LHelper.addGBComponent(infoPanel, infoLayout, valueInput.getComponent(), 1, 20, 1, 1, 0, 0);
        //valuePanel.add(valueInput.getComponent());
        localNameText.setText(var.name);
        compText.setText(component.getName());
        
        this.valueInput.setValue(var.value);
        
        if (var.context != null) {
            contextCombo.setSelectedItem(var.context.getName());
            attributeList.setSelectedValue(var.attribute.toString(), true);
            linkText.setText(var.context.getName() + " -> " + var.attribute.toString());
        } else {
            linkText.setText(null);
            attributeList.setModel(new DefaultListModel());
        }
        
    }
    
    private void updateRepository() {
        
        DataRepository repo = view.getDataRepository(this.getContext());
        ArrayList<Attribute> attributes = repo.getAttributesByType(type);
        
        DefaultListModel lModel = new DefaultListModel();
        if (attributes != null) {
            
            //sort the list
            Collections.sort(attributes, new Comparator<Attribute>() {
                public int compare(Attribute a1, Attribute a2) {
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
