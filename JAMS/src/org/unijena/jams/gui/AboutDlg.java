/*
 * AboutDlg.java
 * Created on 5. April 2006, 10:49
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
package org.unijena.jams.gui;

import java.awt.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;

/**
 *
 * @author S. Kralisch
 */
public class AboutDlg extends JDialog {

    private Image img;

    /**
     * Creates a new instance of AboutDlg
     */
    public AboutDlg(Frame owner) {

        super(owner);

        getContentPane().setLayout(new java.awt.FlowLayout());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        URL imgURL = ClassLoader.getSystemResource("resources/images/JAMSsplash.png");
        if (imgURL != null) {
            img = new ImageIcon(imgURL).getImage();
        }
        int x = img.getWidth(null);
        int y = img.getHeight(null);

        URL textURL = ClassLoader.getSystemResource("resources/text/readme.txt");
        String text = "";
        try {
            if (textURL != null) {
                text = readContent(textURL.openStream());
            }
        } catch (IOException ioe) {
        }

        JPanel contentPanel = new JPanel();
//        contentPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentPanel.setLayout(new java.awt.FlowLayout());
        java.awt.Dimension d = new java.awt.Dimension(x + 10, y + 230);
        contentPanel.setPreferredSize(d);

        getContentPane().add(contentPanel);

        JPanel gfxPanel = new JPanel() {

            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(img, 0, 0, this);
            }
        };
        gfxPanel.setPreferredSize(new java.awt.Dimension(x, y));

        contentPanel.add(gfxPanel);

        JTextArea textArea = new javax.swing.JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Arial", 0, 10));
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setText(text);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new javax.swing.JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(x + 1, 180));
        scrollPane.setViewportView(textArea);
        contentPanel.add(scrollPane);

        JButton closeButton = new JButton();
        closeButton.setText("OK");
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutDlg.this.dispose();
            }
        });
        contentPanel.add(closeButton);

        //this.setAlwaysOnTop(true);
        //this.setUndecorated(true);
        //this.setModal(true);
        this.setTitle("JAMS: About");

        pack();

        Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d2.width / 2 - getWidth() / 2, d2.height / 2 - getHeight() / 2);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new AboutDlg(null).setVisible(true);
            }
        });
    }

    public static String readContent(InputStream in) {
        String content = "";
        InputStreamReader r = new InputStreamReader(in);

        try {
            char[] buffer = new char[in.available()];
            r.read(buffer);
            content = String.copyValueOf(buffer);
            r.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return content;
    }
}
