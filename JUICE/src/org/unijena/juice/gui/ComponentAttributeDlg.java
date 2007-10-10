/*
 * VarEditDlg.java
 * Created on 3. Januar 2007, 22:53
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
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.juice.*;
import org.unijena.juice.ComponentDescriptor;
import org.unijena.juice.ComponentDescriptor.ComponentAttribute;
import org.unijena.juice.ContextAttribute;

/**
 *
 * @author S. Kralisch
 *
 * Dialog with swing inputs for providing a component's attribute value
 * or linkage to a context attribute
 *
 */
public class ComponentAttributeDlg extends JDialog {
    
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 0;
    
    private int result = CANCEL_OPTION;
    private JComboBox contextCombo;
    private JTextField varNameText;
    private JComboBox varNameCombo;
    private InputComponent valueInput;
    private GridBagLayout mainLayout;
    private JPanel mainPanel, valuePanel;
    private ModelView view;
    private Class type;
    
    /**
     * Creates a new instance of VarEditDlg
     */
    public ComponentAttributeDlg(Frame owner, ModelView view) {
        
        super(owner);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        
        this.view = view;
        
        mainLayout = new GridBagLayout();
        mainPanel = new JPanel();
        mainPanel.setLayout(mainLayout);
        
        LHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("Context.Attribute:"), 0, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("Value:"), 0, 2, 1, 1, 0, 0);
        
        contextCombo = new JComboBox();
        LHelper.addGBComponent(mainPanel, mainLayout, contextCombo, 1, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("."), 2, 1, 1, 1, 0, 0);
        contextCombo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRepository();
                }
            }
        });
        
        varNameText = new JTextField();
        varNameText.setColumns(20);
        LHelper.addGBComponent(mainPanel, mainLayout, varNameText, 3, 1, 1, 1, 0, 0);
        
        varNameCombo = new JComboBox();
        varNameCombo.setEditable(true);
//        LHelper.addGBComponent(mainPanel, mainLayout, varNameCombo, 3, 1, 1, 1, 0, 0);
        
        
        LHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 3, 1, 1, 0, 0);
        
        this.getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                if (!valueInput.getValue().equals("") && !valueInput.verify()) {
                    Color oldColor = valueInput.getComponent().getBackground();
                    valueInput.getComponent().setBackground(new Color(255, 0, 0));
                    LHelper.showErrorDlg(ComponentAttributeDlg.this, "Invalid data format!", "Format error");
                    valueInput.getComponent().setBackground(oldColor);
                    return;
                }
                setVisible(false);
                result = ComponentAttributeDlg.APPROVE_OPTION;
            }
        });
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);
        
        JButton cancelButton = new JButton("Cancel");
        ActionListener cancelActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = ComponentAttributeDlg.CANCEL_OPTION;
            }
        };
        cancelButton.addActionListener(cancelActionListener);
        cancelButton.registerKeyboardAction(cancelActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JButton.WHEN_IN_FOCUSED_WINDOW);
        buttonPanel.add(cancelButton);
        
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateRepository() {
        
        AttributeRepository repo = this.getContext().getDataRepository();
        ArrayList<ContextAttribute> attributes = repo.getUniqueAttributesByType(type);
        
        String[] attrNames = {""};
        
        if (attributes != null) {
            
            //sort the list
            Collections.sort(attributes, new Comparator<ContextAttribute>() {
                public int compare(ContextAttribute a1, ContextAttribute a2) {
                    return a1.toString().compareTo(a2.toString());
                }
            });
            
            attrNames = new String[attributes.size()];
            for (int i = 0; i < attributes.size(); i++) {
                attrNames[i] = attributes.get(i).toString();
            }
        }
        
        varNameCombo.setModel(new DefaultComboBoxModel(attrNames));
        
    }
    
    public void show(ComponentAttribute var, String ancestorNames[]) {
        
        this.setTitle("Variable: " + var.name);
        
        this.type = var.type;
        
        contextCombo.setModel(new DefaultComboBoxModel(ancestorNames));
        
        if (valueInput != null) {
            LHelper.removeGBComponent(mainPanel, valueInput.getComponent());
        }
        valueInput = LHelper.createInputComponent(var.type.getSimpleName());
        LHelper.addGBComponent(mainPanel, mainLayout, valueInput.getComponent(), 1, 2, 3, 1, 0, 0);
        
        this.valueInput.setValue(var.getValue());
        
        this.varNameText.setText(var.getAttribute());
        if (var.getContext() != null) {
            this.contextCombo.setSelectedItem(var.getContext().getName());
            this.varNameCombo.setSelectedItem(var.getAttribute());
        }
        
        
        pack();
        this.setVisible(true);
    }
    
    public ComponentDescriptor getContext() {
        return view.getComponentDescriptor((String) contextCombo.getSelectedItem());
    }
    
    public String getAttributeName() {
        return varNameText.getText();
        //return varNameCombo.getSelectedItem().toString();
    }
    
    public String getValue() {
        return valueInput.getValue();
    }
    
    public int getResult() {
        return result;
    }
    
    
}
