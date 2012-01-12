/*
 * ContextAttributeDlg.java
 * Created on 12. Januar 2007, 11:41
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

import jams.data.JAMSString;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import jams.gui.tools.GUIHelper;
import jams.gui.input.InputComponent;
import jamsui.juice.*;
import jams.gui.input.InputComponentFactory;
import java.util.HashMap;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class ContextAttributeDlg extends JDialog {

    public static final int APPROVE_OPTION = 1;

    public static final int CANCEL_OPTION = 0;

    private int result = CANCEL_OPTION;

    private JTextField nameText;

    private JComboBox typeCombo;

    private InputComponent valueInput;

    private JPanel mainPanel;

    private GridBagLayout mainLayout;
    
    private HashMap<String, Class> classMap = new HashMap<String, Class>();

    /**
     * Creates a new instance of AttributeEditDlg
     * @param owner The parent frame of this JDialog
     */
    @SuppressWarnings("unchecked")    
    public ContextAttributeDlg(Frame owner) {

        super(owner);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);

        mainLayout = new GridBagLayout();
        mainPanel = new JPanel();
        mainPanel.setLayout(mainLayout);

        GUIHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(mainPanel, mainLayout, new JLabel(JAMS.i18n("Name:")), 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(mainPanel, mainLayout, new JLabel(JAMS.i18n("Type:")), 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(mainPanel, mainLayout, new JLabel(JAMS.i18n("Value:")), 0, 3, 1, 1, 0, 0);

        nameText = new JTextField();
        nameText.setColumns(40);
        valueInput = InputComponentFactory.createInputComponent(JAMSString.class, true);

        typeCombo = new JComboBox();

        String[] typeNames = new String[JUICE.JAMS_DATA_TYPES.length];
        for (int i = 0; i < JUICE.JAMS_DATA_TYPES.length; i++) {
            typeNames[i] = JUICE.JAMS_DATA_TYPES[i].getSimpleName();
            classMap.put(typeNames[i], JUICE.JAMS_DATA_TYPES[i]);
        }
        //typeCombo.setModel(new DefaultComboBoxModel(JUICE.JAMS_DATA_TYPES));
        typeCombo.setModel(new DefaultComboBoxModel(typeNames));

        typeCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateInputComponent(classMap.get((String) e.getItem()), true);
                }
            }
        });

        GUIHelper.addGBComponent(mainPanel, mainLayout, nameText, 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(mainPanel, mainLayout, typeCombo, 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(mainPanel, mainLayout, valueInput.getComponent(), 1, 3, 1, 1, 0, 0);

        GUIHelper.addGBComponent(mainPanel, mainLayout, new JPanel(), 0, 4, 1, 1, 0, 0);

        this.getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton okButton = new JButton(JAMS.i18n("OK"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!valueInput.verify()) {
                    valueInput.setMarked(true);
                    GUIHelper.showErrorDlg(ContextAttributeDlg.this, JAMS.i18n("Invalid_value!"), JAMS.i18n("Format_error"));
                    valueInput.setMarked(false);
                    return;
                }
                setVisible(false);
                result = ContextAttributeDlg.APPROVE_OPTION;
            }
        });
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        ActionListener cancelActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = ContextAttributeDlg.CANCEL_OPTION;
            }
        };
        cancelButton.addActionListener(cancelActionListener);
        cancelButton.registerKeyboardAction(cancelActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JButton.WHEN_IN_FOCUSED_WINDOW);
        buttonPanel.add(cancelButton);

        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateInputComponent(Class type, boolean doUpdate) {

        String oldValue = "";

        if (valueInput != null) {
            GUIHelper.removeGBComponent(mainPanel, valueInput.getComponent());
            oldValue = valueInput.getValue();
        }
        valueInput = InputComponentFactory.createInputComponent(type, true);

        if (doUpdate) {
            valueInput.setValue(oldValue);
        }
        GUIHelper.addGBComponent(mainPanel, mainLayout, valueInput.getComponent(), 1, 3, 1, 1, 0, 0);

        pack();
    }

    public void show(String name, Class type, String value) {

        this.setTitle(JAMS.i18n("Attribute:_") + name);
        updateInputComponent(type, false);
        this.valueInput.setValue(value);
        this.typeCombo.setSelectedItem(type.getSimpleName());
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

    public Class getAttributeType() {
        return classMap.get((String) typeCombo.getSelectedItem());
    }

    public int getResult() {
        return result;
    }
}
