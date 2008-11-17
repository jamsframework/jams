/*
 * LogViewDlg.java
 * Created on 12. November 2006, 15:50
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
package jams.gui;

import java.awt.*;
import javax.swing.*;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class LogViewDlg extends JDialog {

    private JTextArea textArea;

    public LogViewDlg(Frame owner, int width, int height, String title) {

        super(owner);

        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(true);

        textArea = new javax.swing.JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Arial", 0, 10));


        JScrollPane scrollPane = new javax.swing.JScrollPane();
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.setViewportView(textArea);
        //contentPanel.add(scrollPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton();
        closeButton.setText(JAMS.resources.getString("Close"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogViewDlg.this.setVisible(false);
            }
        });
        JButton clearButton = new JButton();
        clearButton.setText(JAMS.resources.getString("Clear"));
        clearButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogViewDlg.this.setText("");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.setTitle(title);

        pack();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return textArea.getText();
    }

    public void appendText(String text) {
        textArea.append(text);
    }
}
