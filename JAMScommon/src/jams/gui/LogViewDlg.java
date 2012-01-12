/*
 * LogViewDlg.java
 * Created on 12. November 2006, 15:50
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

    /**
     * Creates a new LogViewDlg object
     * @param owner Parent frame
     * @param width Dialog width
     * @param height Dialog height
     * @param title Dialog title
     */
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
        closeButton.setText(JAMS.i18n("Close"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogViewDlg.this.setVisible(false);
            }
        });
        JButton clearButton = new JButton();
        clearButton.setText(JAMS.i18n("Clear"));
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

    /**
     * Sets the text to be displayed by the dialog
     * @param text The text to be displayed by the dialog
     */
    public void setText(String text) {
        textArea.setText(text);
    }

    /**
     * 
     * @return The text displayed by the dialog
     */
    public String getText() {
        return textArea.getText();
    }

    /**
     * Appends some text to the dialog
     * @param text The text to be appended
     */
    public void appendText(String text) {
        textArea.append(text);
    }
}
