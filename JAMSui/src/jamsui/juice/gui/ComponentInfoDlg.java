/*
 * ComponentInfoDlg.java
 * Created on 24. April 2006, 09:45
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jamsui.juice.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.lang.reflect.Field;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import jams.gui.tools.GUIHelper;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.JAMS;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author S. Kralisch
 */
public class ComponentInfoDlg extends JDialog {

    private static final String DEFAULT_STRING = JAMS.i18n("[none]");
    private static final int TEXTAREA_WIDTH = 295;
    private static final int GRIDBAG_MAX_Y = 3;
    private HashMap<String, JTextField> textFields = new HashMap<>();
    private HashMap<String, JTextPane> textAreas = new HashMap<>();
    private SimpleAttributeSet descriptionText;
    private JPanel contentPanel;
    private GridBagLayout mainLayout;
    private ArrayList<JPanel> varPanels = new ArrayList<>();
    private JLabel varLabel = new JLabel(JAMS.i18n("Variables:"));
    private static HashMap<Class, JDialog> compViewDlgs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ComponentInfoDlg(Frame owner, Class clazz) {

        super(owner);
        this.setLocationByPlatform(true);
        this.setTitle(clazz.getCanonicalName());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        contentPanel = new JPanel();
        mainLayout = new GridBagLayout();
        contentPanel.setLayout(mainLayout);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Type:")), 0, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Author:")), 0, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Date:")), 0, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Description:")), 0, 3, 1, 1, 0, 0);

        GUIHelper.addGBComponent(contentPanel, mainLayout, getTextField("type", ""), 1, 0, 1, 1, 1.0, 1.0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, getTextField("author", ""), 1, 1, 1, 1, 1.0, 1.0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, getTextField("date", ""), 1, 2, 1, 1, 1.0, 1.0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, getTextPane("description", "", 140), 1, 3, 1, 1, 1.0, 1.0);

        reset(DEFAULT_STRING);

        this.add(new JScrollPane(contentPanel));
        JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
        if (jcd != null) {
            update(clazz.getCanonicalName(), jcd);
        } else {
            reset(clazz.getCanonicalName());
        }

        update(clazz.getFields());

        setPreferredSize(new Dimension(450, 600));
        pack();
    }

    public JScrollPane getTextPane(String key, String value, int height) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/plain");
        textPane.setEditable(false);
        textPane.setText(value);
        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(TEXTAREA_WIDTH, height));
        textAreas.put(key, textPane);
        return scroll;
    }

    public JTextField getTextField(String key, String value) {
        JTextField text = new JTextField();
        text.setEditable(false);
        text.setText(value);
        textFields.put(key, text);
        return text;
    }

    private void update(String clazz, JAMSComponentDescription jcd) {
        textFields.get("type").setText(clazz);
        textFields.get("author").setText(jcd.author());
        textFields.get("date").setText(jcd.date());
        textAreas.get("description").setText(jcd.description());
    }

    private void update(Field compFields[]) {

        int pos = GRIDBAG_MAX_Y + 1;

        //get rid of current var components
        for (JPanel p : varPanels) {
            contentPanel.remove(p);
        }
        contentPanel.remove(varLabel);

        //create new components
        if (compFields.length > 0) {
            GUIHelper.addGBComponent(contentPanel, mainLayout, varLabel, 0, pos++, 1, 1, 0, 0);
        }

        for (Field field : compFields) {
            JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);

            //check if there actually is a jvd, else this is some other field and we're not interested
            if (jvd != null) {

                JPanel fieldPanel = new JPanel();
                varPanels.add(fieldPanel);
                fieldPanel.setBorder(BorderFactory.createTitledBorder(field.getName()));
                GUIHelper.addGBComponent(contentPanel, mainLayout, fieldPanel, 0, pos++, 2, 1, 0, 0);

                GridBagLayout fieldLayout = new GridBagLayout();
                fieldPanel.setLayout(fieldLayout);

                GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel(JAMS.i18n("Type:")), 0, 0, 1, 1, 0, 0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel(JAMS.i18n("Access:")), 0, 1, 1, 1, 0, 0);
                //GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel("Update:"), 0, 2, 1, 1, 0, 0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel(JAMS.i18n("Description:")), 0, 3, 1, 1, 0, 0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel(JAMS.i18n("Unit:")), 0, 4, 1, 1, 0, 0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, new JLabel(JAMS.i18n("Default:")), 0, 5, 1, 1, 0, 0);

                GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextField("", field.getType().getName()), 1, 0, 1, 1, 1.0, 1.0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextField("", jvd.access().toString()), 1, 1, 1, 1, 1.0, 1.0);
                //GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextField("", jvd.update().toString()), 1, 2, 1, 1, 1.0, 1.0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextPane("", jvd.description(), 70), 1, 3, 1, 1, 1.0, 1.0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextField("", jvd.unit()), 1, 4, 1, 1, 1.0, 1.0);
                GUIHelper.addGBComponent(fieldPanel, fieldLayout, getTextField("", jvd.defaultValue().equals(JAMSVarDescription.NULL_VALUE) ? "" : jvd.defaultValue()), 1, 5, 1, 1, 1.0, 1.0);

//                this.getParent().validate();
            }
        }
    }

    public void reset(String clazz) {
        for (JTextField text : textFields.values()) {
            text.setText(DEFAULT_STRING);
        }
        for (JEditorPane text : textAreas.values()) {
            text.setText(DEFAULT_STRING);
        }
        textFields.get("type").setText(clazz);
    }

    public static void displayMetadataDlg(Frame owner, Class clazz) {

        if (clazz != null) {

            if (compViewDlgs.containsKey(clazz)) {
                compViewDlgs.get(clazz).setVisible(true);
                return;
            }

            ComponentInfoDlg compViewDlg = new ComponentInfoDlg(owner, clazz);
            compViewDlgs.put(clazz, compViewDlg);
            compViewDlg.setVisible(true);

        }
    }
}
