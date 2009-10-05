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
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.JAMSVersion;
import jams.gui.tools.GUIHelper;

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

        String gplText = "", versionText = "", contactText = "";
        try {
            URL textURL = ClassLoader.getSystemResource("resources/text/readme.txt");
            if (textURL != null) {
                gplText = JAMSTools.streamToString(textURL.openStream());
            }
            versionText = JAMSVersion.getInstance().getVersionDateString();
            contactText = JAMSVersion.getInstance().getContactString();
        } catch (IOException ioe) {
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.white);
//        contentPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        java.awt.Dimension d = new Dimension(x + 10, y + 280);
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
         * text areas
         */
        JTextField versionTextField = new JTextField();
        versionTextField.setEditable(false);
        versionTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        versionTextField.setBackground(Color.white);
        versionTextField.setBorder(null);
        //int versionTextHeight = 16;
        versionTextField.setPreferredSize(new Dimension(x - 160, 16));
        versionTextField.setText(versionText);
        versionTextField.setCaretPosition(0);

        JTextField contactTextField = new JTextField();
        contactTextField.setEditable(false);
        contactTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        contactTextField.setBackground(Color.white);
        contactTextField.setBorder(null);
        //int versionTextHeight = 16;
        contactTextField.setPreferredSize(new Dimension(x - 160, 16));
        contactTextField.setText(contactText);
        contactTextField.setCaretPosition(0);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(Color.white);
        GridBagLayout gbl = new GridBagLayout();
        textPanel.setLayout(gbl);
        textPanel.getInsets().set(0, 0, 0, 0);

        JLabel vLabel = new JLabel(JAMS.resources.getString("Version"));
        vLabel.setVerticalTextPosition(JLabel.BOTTOM);
        vLabel.setHorizontalAlignment(JLabel.RIGHT);
        GUIHelper.addGBComponent(textPanel, gbl, vLabel, 0, 0, 1, 1, 1, 1);
        GUIHelper.addGBComponent(textPanel, gbl, versionTextField, 1, 0, 1, 1, 1, 1);

        JLabel cLabel = new JLabel(JAMS.resources.getString("Contact"));
        cLabel.setVerticalTextPosition(JLabel.BOTTOM);
        cLabel.setHorizontalAlignment(JLabel.RIGHT);
        GUIHelper.addGBComponent(textPanel, gbl, cLabel, 0, 1, 1, 1, 1, 1);
        GUIHelper.addGBComponent(textPanel, gbl, contactTextField, 1, 1, 1, 1, 1, 1);

        contentPanel.add(textPanel);

//        JPanel versionPanel = new JPanel();
//        versionPanel.setBackground(Color.white);
//        versionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//        //versionPanel.setBorder(BorderFactory.createEtchedBorder());
//        versionPanel.setPreferredSize(new java.awt.Dimension(x + 1, 22));
//        versionPanel.getInsets().set(0, 0, 0, 0);
//        JLabel vLabel = new JLabel(JAMS.resources.getString("Version"));
//        vLabel.setVerticalTextPosition(JLabel.BOTTOM);
//        versionPanel.add(vLabel);
//        versionPanel.add(versionTextField);
//        contentPanel.add(versionPanel);
//
//        JPanel contactPanel = new JPanel();
//        contactPanel.setBackground(Color.white);
//        //contactPanel.setBorder(BorderFactory.createEtchedBorder());
//        contactPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//        contactPanel.setPreferredSize(new java.awt.Dimension(x + 1, 22));
//        contactPanel.getInsets().set(0, 0, 0, 0);
//        JLabel cLabel = new JLabel(JAMS.resources.getString("Contact"));
//        cLabel.setVerticalTextPosition(JLabel.BOTTOM);
//        contactPanel.add(cLabel);
//        contactPanel.add(contactTextField);
//        contentPanel.add(contactPanel);

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
        closeButton.setText(JAMS.resources.getString("OK"));
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
        this.setTitle(JAMS.resources.getString("About"));

        pack();

        Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d2.width / 2 - getWidth() / 2, d2.height / 2 - getHeight() / 2);
    }
}
