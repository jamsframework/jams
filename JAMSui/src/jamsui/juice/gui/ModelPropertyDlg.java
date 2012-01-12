/*
 * ModelPropertyDlg.java
 * Created on 9. März 2007, 23:21
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.WindowConstants;
import jams.gui.tools.GUIHelper;
import jams.gui.input.FloatInput;
import jams.gui.input.IntegerInput;
import jams.JAMS;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelProperties.ModelProperty;

/**
 *
 * @author Sven Kralisch
 */
public class ModelPropertyDlg extends JDialog {

    private static final Dimension TEXT_FIELD_DIM = new Dimension(200, 20), TEXT_AREA_DIM = new Dimension(300, 100);
    public final static int OK_RESULT = 0;
    public final static int CANCEL_RESULT = -1;
    private JComboBox groupCombo, componentCombo, varCombo;
    private HashMap<String, ComponentDescriptor> componentDescriptors;
    private JTextField nameField, descriptionField, helpURLField;
    private JTextArea helpTextField;
    private FloatInput lowField, upField;
    private IntegerInput lengthField;
    private int result = CANCEL_RESULT;

    public ModelPropertyDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        init();
    }

    private void init() {

        setModal(true);
        this.setTitle(JAMS.i18n("Model_property_editor"));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.setLayout(new BorderLayout());
        GridBagLayout gbl = new GridBagLayout();
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(gbl);

        GUIHelper.addGBComponent(contentPanel, gbl, new JPanel(), 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Group:")), 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Component:")), 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Variable/Attribute:")), 0, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Name:")), 0, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Description:")), 0, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Lower_Boundary:")), 0, 6, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Upper_Boundary:")), 0, 7, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Length:")), 0, 8, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Help_URL:")), 0, 9, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Help_Text:")), 0, 10, 1, 1, 0, 0);

        groupCombo = new JComboBox();
        GUIHelper.addGBComponent(contentPanel, gbl, groupCombo, 1, 1, 1, 1, 0, 0);

        componentCombo = new JComboBox();
        componentCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateComponentVars(e.getItem());
                }
            }
        });
        GUIHelper.addGBComponent(contentPanel, gbl, componentCombo, 1, 2, 1, 1, 0, 0);

        varCombo = new JComboBox();
        GUIHelper.addGBComponent(contentPanel, gbl, varCombo, 1, 3, 1, 1, 0, 0);

        nameField = new JTextField();
        nameField.setPreferredSize(TEXT_FIELD_DIM);
        descriptionField = new JTextField();
        descriptionField.setPreferredSize(TEXT_FIELD_DIM);
        lowField = new FloatInput();
        upField = new FloatInput();
        lengthField = new IntegerInput();
        helpURLField = new JTextField();

        GUIHelper.addGBComponent(contentPanel, gbl, nameField, 1, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, descriptionField, 1, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, lowField.getComponent(), 1, 6, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, upField.getComponent(), 1, 7, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, gbl, lengthField.getComponent(), 1, 8, 1, 1, 0, 0);

        GUIHelper.addGBComponent(contentPanel, gbl, helpURLField, 1, 9, 2, 1, 0, 0);

        helpTextField = new JTextArea();
        JScrollPane textScroll = new JScrollPane(helpTextField);
        textScroll.setPreferredSize(TEXT_AREA_DIM);
        GUIHelper.addGBComponent(contentPanel, gbl, textScroll, 1, 10, 2, 1, 0, 0);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        ActionListener okListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = OK_RESULT;
            }
        };
        okButton.addActionListener(okListener);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        ActionListener cancelListener = new ActionListener() {

            @Override
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
        setResizable(false);
    }

    @SuppressWarnings("unchecked")
    private void updateComponentVars(Object item) {
        ComponentDescriptor cd = this.componentDescriptors.get((String) item);
        HashMap<String, ComponentField> vars = cd.getComponentFields();

        ArrayList<String> varNames = new ArrayList<String>();
        for (String name : vars.keySet()) {
            ComponentField var = vars.get(name);
            if (var.getContext() == null) {
                varNames.add(name);
            }
        }

        if (cd instanceof ContextDescriptor) {
            HashMap<String, ContextAttribute> attrs = ((ContextDescriptor) cd).getStaticAttributes();
            for (String name : attrs.keySet()) {
                varNames.add(name);
            }
        }

        Collections.sort(varNames);

        varNames.add(0, JAMS.i18n("[enable_component]"));

        String[] varNameArray = varNames.toArray(new String[varNames.size()]);
        varCombo.setModel(new DefaultComboBoxModel(varNameArray));
    }

    @SuppressWarnings("unchecked")
    public void update(String[] groupNames, HashMap<String, ComponentDescriptor> componentDescriptors, ModelProperty property, String currentGroup) {

        groupCombo.setModel(new DefaultComboBoxModel(groupNames));
        groupCombo.setSelectedItem(currentGroup);

        this.componentDescriptors = componentDescriptors;
        ArrayList<String> componentNames = new ArrayList<String>();
        for (String name : componentDescriptors.keySet()) {
            componentNames.add(name);
        }
        Collections.sort(componentNames);
        String[] compNameArray = componentNames.toArray(new String[componentNames.size()]);
        componentCombo.setModel(new DefaultComboBoxModel(compNameArray));

        if (property != null) {
            componentCombo.setSelectedItem(property.component.getName());

            if (property.var != null) {
                varCombo.setSelectedItem(property.var.getName());
            } else if (property.attribute != null) {
                varCombo.setSelectedItem(property.attribute.getName());
            }

            nameField.setText(property.name);
            nameField.setCaretPosition(0);
            descriptionField.setText(property.description);
            descriptionField.setCaretPosition(0);

            lowField.setValue("" + property.lowerBound);
            upField.setValue("" + property.upperBound);
            lengthField.setValue("" + property.length);
            helpURLField.setText(property.getHelpComponent().getHelpURL());
            helpURLField.setCaretPosition(0);
            helpTextField.setText(property.getHelpComponent().getHelpText());
            helpTextField.setCaretPosition(0);
        } else {
            nameField.setText("");
            descriptionField.setText("");
            lowField.setValue("");
            upField.setValue("");
            lengthField.setValue("");
            helpURLField.setText("");
            helpTextField.setText("");

            updateComponentVars(componentCombo.getSelectedItem());
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

    public String getDescription() {
        return descriptionField.getText();
    }

    public double getLowerBound() {
        double lowBound = 0;
        try {
            lowBound = Double.parseDouble(lowField.getValue());
        } catch (NumberFormatException nfe) {
        }
        return lowBound;
    }

    public double getUpperBound() {
        double upBound = 0;
        try {
            upBound = Double.parseDouble(upField.getValue());
        } catch (NumberFormatException nfe) {
        }
        return upBound;
    }

    public int getLength() {
        int length = 0;
        try {
            length = Integer.parseInt(lengthField.getValue());
        } catch (NumberFormatException nfe) {
        }
        return length;
    }

    public String getHelpURL() {
        return helpURLField.getText();
    }

    public String getHelpText() {
        return helpTextField.getText();
    }

    public ComponentDescriptor getComponent() {
        return componentDescriptors.get(componentCombo.getSelectedItem());
    }

    public ComponentField getVar() {
        return getComponent().getComponentFields().get(varCombo.getSelectedItem());
    }

    public ContextAttribute getAttribute() {
        ComponentDescriptor cd = getComponent();
        if (cd instanceof ContextDescriptor) {
            return ((ContextDescriptor) cd).getStaticAttributes().get(varCombo.getSelectedItem());
        } else {
            return null;
        }
    }
}
