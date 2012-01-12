/*
 * ModelSubgroupDlg.java
 * Created on 11. Mai 2008, 06:37
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import jams.gui.tools.GUIHelper;
import jams.JAMS;
import jams.meta.ModelProperties.Group;

/**
 *
 * @author Heiko Busch
 */
public class ModelSubgroupDlg extends JDialog {
    
    public final static int OK_RESULT = 0;
    public final static int CANCEL_RESULT = -1;
    
    private JComboBox groupCombo;
    private JTextField nameField;
    private JTextField helpURLField;
    private JTextArea helpTextField;
    private int result = CANCEL_RESULT;
    
    public ModelSubgroupDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        init();
    }
    
    private void init() {
        
        setModal(true);
        this.setTitle(JAMS.i18n("Subgroup_editor"));
        
        this.setLayout(new BorderLayout());
        GridBagLayout gbl = new GridBagLayout();
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(gbl);
        
        GUIHelper.addGBComponent(contentPanel, gbl, new JPanel(), 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Group:")), 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Name:")), 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Help_URL:")), 0, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Help_Text:")), 0, 4, 1, 1, 0, 0);
        
        groupCombo = new JComboBox();
        nameField = new JTextField();
        helpURLField = new JTextField();
        helpTextField = new JTextArea();
        
        GUIHelper.addGBComponent(contentPanel, gbl, groupCombo, 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, nameField, 1, 2, 1, 1, 30, 0);

        helpURLField.setPreferredSize(new Dimension(200, 20));
        GUIHelper.addGBComponent(contentPanel, gbl, helpURLField, 1, 3, 2, 1, 0, 0);
        
        helpTextField.setColumns(30);
        helpTextField.setRows(5);
        GUIHelper.addGBComponent(contentPanel, gbl, helpTextField, 1, 4, 2, 1, 0, 0);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = OK_RESULT;
            }
        };
        okButton.addActionListener(okListener);
        getRootPane().setDefaultButton(okButton);        
        
        JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = CANCEL_RESULT;
            }
        };
        cancelButton.addActionListener(cancelListener);
        cancelButton.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JButton.WHEN_IN_FOCUSED_WINDOW);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }
    
    @SuppressWarnings("unchecked")
    public void update(String[] groupNames, Group subgroup, String currentGroup) {

        groupCombo.setModel(new DefaultComboBoxModel(groupNames));
        groupCombo.setSelectedItem(currentGroup);
        
        
        if (subgroup != null) {
            nameField.setText(subgroup.name);
            helpURLField.setText(subgroup.getHelpComponent().getHelpURL());
            helpTextField.setText(subgroup.getHelpComponent().getHelpText());
        } else {
            nameField.setText("");
            helpURLField.setText("");
            helpTextField.setText("");
        }
        
        pack();
    }
    
    public String getGroup() {
        return (String) groupCombo.getSelectedItem();
    }
    
    public int getResult() {
        return result;
    }
    
    public String getName() {
        return nameField.getText();
    }

    public String getHelpURL() {
        return helpURLField.getText();
    }

    public String getHelpText() {
        return helpTextField.getText();
    }
    
}
