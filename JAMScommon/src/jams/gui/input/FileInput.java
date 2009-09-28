/*
 * FileInput.java
 * Created on 11. April 2006, 20:46
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
package jams.gui.input;

import jams.gui.tools.GUIHelper;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author S. Kralisch
 */
public class FileInput extends JPanel implements InputComponent {

    private static final int BUTTON_SIZE = 20;
    private JTextField textField;
    private JButton addButton;
    private JFileChooser jfc;
    private ValueChangeListener l;

    public FileInput(boolean dirsOnly) {
        this();
        if (dirsOnly) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
    }

    // constructor of main frame
    public FileInput() {
        // create a panel to hold all other components
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);

        textField = new JTextField();
        textField.setBorder(BorderFactory.createEtchedBorder());
        add(textField, BorderLayout.CENTER);

        jfc = GUIHelper.getJFileChooser();

        addButton = new JButton("...");
        addButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                int result = jfc.showOpenDialog(FileInput.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    textField.setText(stringValue);
                }
            }
        });
        add(addButton, BorderLayout.EAST);

    }

    public void setFile(String fileName) {
        if (fileName == null) {
            fileName = "";
        }
        textField.setText(fileName);
        File file = new File(fileName);
        if (file.exists()) {
            // try to change to that dir
            if (file.isDirectory()) {
                jfc.setCurrentDirectory(file);
            } else {
                jfc.setCurrentDirectory(file.getParentFile());
            }
        }
    }

    public String getFileName() {
        return textField.getText();
    }

    public String getValue() {
        return getFileName();
    }

    public void setValue(String value) {
        setFile(value);
    }

    public JComponent getComponent() {
        return this;
    }

    public void setRange(double lower, double upper) {
    }

    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        addButton.setEnabled(enabled);
    }

    public boolean verify() {
        File file = new File(getFileName());
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public int getErrorCode() {
        return INPUT_OK;
    }

    public void setLength(int length) {
        textField.setColumns(length);
    }

    public void addValueChangeListener(ValueChangeListener l) {
        this.l = l;
        this.textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                FileInput.this.l.valueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                FileInput.this.l.valueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                FileInput.this.l.valueChanged();
            }
        });
    }
    private Color oldColor;

    public void setMarked(boolean marked) {
        if (marked == true) {
            oldColor = textField.getBackground();
            textField.setBackground(new Color(255, 0, 0));
        } else {
            textField.setBackground(oldColor);
        }
    }

    public void setHelpText(String text) {
        text = "<html>" + text + "</html>";
        this.textField.setToolTipText(text);
    }
}
