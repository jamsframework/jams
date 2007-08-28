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

package org.unijena.jams.gui.input;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import org.unijena.jams.gui.*;

/**
 *
 * @author S. Kralisch
 */
public class FileInput extends JPanel implements InputComponent {
    
    private static final long serialVersionUID = -2171698332789577245L;
    
    static final int BUTTON_SIZE = 21;
    
    private JTextField textField;
    private JFileChooser jfc;
    
    public FileInput(boolean dirsOnly) {
        this();
        if (dirsOnly)
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
    
    // constructor of main frame
    public FileInput() {
        
        // create a panel to hold all other components
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
        
        textField = new JTextField();
        add(textField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        buttonPanel.setPreferredSize(new Dimension(BUTTON_SIZE+5,BUTTON_SIZE+5));
        buttonPanel.setLayout( new FlowLayout() );
        add( buttonPanel, BorderLayout.EAST );
        
        
        JButton addButton = new JButton("...");
        addButton.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
        addButton.setMargin(new java.awt.Insets(10, 10, 10, 10));
        
        jfc = new JFileChooser();
        
        buttonPanel.add(addButton);
        add(addButton, BorderLayout.EAST);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent event ) {
                
                int result = jfc.showOpenDialog(FileInput.this);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    textField.setText(stringValue);
                }
            }
        });
    }
    
    public void setFile(String fileName) {
        if (fileName == null)
            fileName = "";
        textField.setText(fileName);
        File file = new File(fileName).getParentFile();
        jfc.setCurrentDirectory(file);
    }
    
    public String getFile() {
        return textField.getText();
    }
    
    public String getValue() {
        return getFile();
    }
    
    public void setValue(String value) {
        setFile(value);
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    public void setRange(double lower, double upper){};
    
    public boolean verify() {
        return true;
    }
    
    public int getErrorCode() {
        return INPUT_OK;
    }
    
}