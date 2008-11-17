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
package jams.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
     * @param owner
     */
    public AboutDlg(Frame owner) {

        super(owner);

        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        URL imgURL = ClassLoader.getSystemResource("resources/images/JAMSsplash.png");
        if (imgURL != null) {
            img = new ImageIcon(imgURL).getImage();
        }
        int x = img.getWidth(null);
        int y = img.getHeight(null);

        String gplText = "", versionText = "";
        try {
            URL textURL = ClassLoader.getSystemResource("resources/text/readme.txt");
            if (textURL != null) {
                gplText = readContent(textURL.openStream());
            }
            textURL = ClassLoader.getSystemResource("resources/text/version.txt");
            if (textURL != null) {
                versionText = readContent(textURL.openStream());
            }
        } catch (IOException ioe) {
        }

        JPanel contentPanel = new JPanel();
//        contentPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentPanel.setLayout(new FlowLayout());
        java.awt.Dimension d = new Dimension(x + 10, y + 250);
        contentPanel.setPreferredSize(d);

        getContentPane().add(contentPanel);

        JPanel gfxPanel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(img, 0, 0, this);
            }
        };
        gfxPanel.setPreferredSize(new Dimension(x, y));
        contentPanel.add(gfxPanel);

        /*
         * version text
         */
        JTextArea versionTextArea = new JTextArea();
        versionTextArea.setEditable(false);
        versionTextArea.setFont(new Font("Arial", 0, 10));
        int versionTextHeight = 16;
        versionTextArea.setPreferredSize(new Dimension(x - 3, versionTextHeight));
        versionTextArea.setText(versionText);
        versionTextArea.setCaretPosition(0);

        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        versionPanel.setBorder(BorderFactory.createEtchedBorder());
        versionPanel.setPreferredSize(new java.awt.Dimension(x + 1, versionTextHeight + 4));
        versionPanel.getInsets().set(0, 0, 0, 0);
        versionPanel.add(versionTextArea);
        contentPanel.add(versionPanel);

        /*
         * license text
         */
        JTextArea gplTextArea = new JTextArea();
        gplTextArea.setEditable(false);
        gplTextArea.setFont(new Font("Arial", 0, 10));
        gplTextArea.setColumns(20);
        gplTextArea.setRows(5);
        gplTextArea.setText(gplText);
        gplTextArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(x + 1, 180));
        scrollPane.setViewportView(gplTextArea);
        contentPanel.add(scrollPane);

        /*JLabel versionTextLabel = new JLabel();
        versionTextLabel.setFont(new java.awt.Font("Arial", 0, 10));
        versionTextLabel.setText("Test");*/

        JButton closeButton = new JButton();
        closeButton.setText(java.util.ResourceBundle.getBundle("resources/Bundle").getString("OK"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                AboutDlg.this.dispose();
            }
        });
        contentPanel.add(closeButton);

        //this.setAlwaysOnTop(true);
        //this.setUndecorated(true);
        //this.setModal(true);
        this.setTitle(java.util.ResourceBundle.getBundle("resources/Bundle").getString("About"));

        pack();

        Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d2.width / 2 - getWidth() / 2, d2.height / 2 - getHeight() / 2);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new  

              Runnable() {

                 @Override
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
