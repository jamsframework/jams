/*
 * AttributeEditDlg.java
 * Created on 12. Januar 2007, 11:41
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
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
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

/**
 *
 * @author S. Kralisch
 */
public class AttributeEditDlg extends JDialog {
    
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 0;
    
    private int result = CANCEL_OPTION;
    private JTextField nameText;
    private JComboBox typeCombo;
    private InputComponent valueInput;
    private JPanel mainPanel;
    private GridBagLayout mainLayout;
    
    /**
     * Creates a new instance of AttributeEditDlg
     */
    public AttributeEditDlg(Frame owner) {
        
        super(owner);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
                
        mainLayout = new GridBagLayout();
        mainPanel = new JPanel();
        mainPanel.setLayout(mainLayout);
        
        LHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("Name:"), 0, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("Type:"), 0, 2, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, new JLabel("Value:"), 0, 3, 1, 1, 0, 0);
        
        nameText = new JTextField();
        nameText.setColumns(40);
        valueInput = LHelper.createInputComponent("");
        
        typeCombo = new JComboBox();
        
        String[] typeNames = new String[JUICE.JAMS_DATA_TYPES.length];
        for (int i = 0; i < JUICE.JAMS_DATA_TYPES.length; i++) {
            typeNames[i] = JUICE.JAMS_DATA_TYPES[i].getName();
        }
        typeCombo.setModel(new DefaultComboBoxModel(typeNames));
        
        typeCombo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateInputComponent(e.getItem(), true);
                }
            }
        });
        
        LHelper.addGBComponent(mainPanel, mainLayout, nameText, 1, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, typeCombo, 1, 2, 1, 1, 0, 0);
        LHelper.addGBComponent(mainPanel, mainLayout, valueInput.getComponent(), 1, 3, 1, 1, 0, 0);
        
        LHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 4, 1, 1, 0, 0);
        
        this.getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                if (!valueInput.verify()) {
                    Color oldColor = valueInput.getComponent().getBackground();
                    valueInput.getComponent().setBackground(new Color(255, 0, 0));
                    LHelper.showErrorDlg(AttributeEditDlg.this, "Invalid data format!", "Format error");
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
    
    private void updateInputComponent(Object type, boolean doUpdate) {
        
        String shortType = (String) type;
        String oldValue = "";
        
        StringTokenizer tok = new StringTokenizer(shortType, ".");
        while (tok.hasMoreTokens()) {
            shortType = tok.nextToken();
        }
        if (valueInput != null) {
            LHelper.removeGBComponent(mainPanel, valueInput.getComponent());
            oldValue = valueInput.getValue();
        }
        valueInput = LHelper.createInputComponent(shortType);
        
        if (doUpdate) {
            valueInput.setValue(oldValue);
        }
        LHelper.addGBComponent(mainPanel, mainLayout, valueInput.getComponent(), 1, 3, 1, 1, 0, 0);
        
        pack();
    }
    
    public void show(String name, String type, String value) {
        
        this.setTitle("Attribute: " + name);
        updateInputComponent(type, false);
        this.valueInput.setValue(value);
        this.typeCombo.setSelectedItem(type);
        this.nameText.setText(name);
        
        pack();
        this.setVisible(true);
    }
    
    public String getAttributeName() {
        return nameText.getText();
    }
    
    public String getValue() {
        return valueInput.getValue();
    }
    
    public String getType() {
        return (String) typeCombo.getSelectedItem();
    }
    
    public int getResult() {
        return result;
    }
    
    
}
