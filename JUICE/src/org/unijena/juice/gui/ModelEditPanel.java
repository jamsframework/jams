/*
 * ModelEditPanel.java
 * Created on 12. Dezember 2006, 22:43
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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jams.gui.LHelper;
import jams.gui.input.FileInput;
import jams.gui.input.ValueChangeListener;

/**
 *
 * @author S. Kralisch
 *
 * Panel that provides swing components for defining model author,
 * date, description and help-baseURL
 *
 */
public class ModelEditPanel extends JPanel {
    
    private static final int TEXTAREA_WIDTH = 450, TEXTAREA_HEIGHT = 150;
    
    private HashMap<String, JTextField> textFields = new HashMap<String, JTextField>();
    private HashMap<String, JTextPane> textAreas = new HashMap<String, JTextPane>();
    private JPanel componentPanel;
    private GridBagLayout mainLayout;
    private ModelView view;
    private FileInput workspaceInput;
    
    public ModelEditPanel(ModelView view) {
        super();
        this.view = view;
        init();
    }
    
    private void init() {
        
        componentPanel = new JPanel();
        //setBorder(BorderFactory.createTitledBorder("Model Properties"));
        
        mainLayout = new GridBagLayout();
        componentPanel.setLayout(mainLayout);
        
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Workspace:"), 1, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Author:"), 1, 1, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Date:"), 1, 2, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Help Base URL:"), 1, 3, 1, 1, 0, 0);
        LHelper.addGBComponent(componentPanel, mainLayout, new JLabel("Description:"), 1, 4, 1, 1, 0, 0);
        
        workspaceInput = new FileInput(true);
        
        LHelper.addGBComponent(componentPanel, mainLayout, workspaceInput, 2, 0, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, getTextField("author", "", true), 2, 1, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, getTextField("date", "", true), 2, 2, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, getTextField("helpBaseUrl", "", true), 2, 3, 1, 1, 1.0, 1.0);
        LHelper.addGBComponent(componentPanel, mainLayout, getTextPane("description", "", true), 2, 4, 2, 1, 1.0, 1.0);

        workspaceInput.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChanged() {
                view.setWorkspace(workspaceInput.getValue());
            }
        });

        textFields.get("author").getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateAuthor();
            }
            public void insertUpdate(DocumentEvent e) {
                updateAuthor();
            }
            public void removeUpdate(DocumentEvent e) {
                updateAuthor();
            }
        });
        textFields.get("date").getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateDate();
            }
            public void insertUpdate(DocumentEvent e) {
                updateDate();
            }
            public void removeUpdate(DocumentEvent e) {
                updateDate();
            }
        });
        textFields.get("helpBaseUrl").getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateHelpBaseUrl();
            }
            public void insertUpdate(DocumentEvent e) {
                updateHelpBaseUrl();
            }
            public void removeUpdate(DocumentEvent e) {
                updateHelpBaseUrl();
            }
        });
        textAreas.get("description").getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateDescription();
            }
            public void insertUpdate(DocumentEvent e) {
                updateDescription();
            }
            public void removeUpdate(DocumentEvent e) {
                updateDescription();
            }
        });
        
/*
        textFields.get("author").addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateAuthor();
            }
        });
        textFields.get("date").addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDate();
            }
        });
        textAreas.get("description").addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDescription();
            }
        });
*/      
        add(componentPanel);
    }
    
    public void update() {
        textFields.get("author").setText(view.getAuthor());
        textFields.get("date").setText(view.getDate());
        textFields.get("helpBaseUrl").setText(view.getHelpBaseUrl());
        textAreas.get("description").setText(view.getDescription());
        workspaceInput.setValue(view.getWorkspace());
    }
    
    public JTextField getTextField(String key, String value, boolean editable) {
        JTextField text = new JTextField();
        text.setBorder(BorderFactory.createEtchedBorder());
        text.setEditable(editable);
        text.setText(value);
        text.setColumns(30);
        textFields.put(key, text);
        return text;
    }
    
    public JScrollPane getTextPane(String key, String value, boolean editable) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/plain");
        textPane.setEditable(editable);
        textPane.setText(value);
        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(TEXTAREA_WIDTH, TEXTAREA_HEIGHT));
        textAreas.put(key, textPane);
        return scroll;
    }
    
    private void updateAuthor() {
        
        String author = textFields.get("author").getText();
        view.setAuthor(author);
    }
    
    private void updateDate() {
        
        String date = textFields.get("date").getText();
        view.setDate(date);
    }
    
    private void updateDescription() {
        
        String description = textAreas.get("description").getText();
        view.setDescription(description);
    }
    
    private void updateHelpBaseUrl() {
        
        String helpBaseUrl = textFields.get("helpBaseUrl").getText();
        view.setHelpBaseUrl(helpBaseUrl);
    }
}
