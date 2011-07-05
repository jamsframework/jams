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
import jams.JAMSVersion;
import jams.tools.FileTools;
import java.util.Properties;

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

        String gplText = "", contribText = "", versionText = "", contactText = "", vmText = "";
        try {
            URL textURL = ClassLoader.getSystemResource("resources/text/readme.txt");
            URL contribURL = ClassLoader.getSystemResource("resources/text/contribution.txt");
            if (textURL != null) {
                gplText = FileTools.streamToString(textURL.openStream());
                contribText = FileTools.streamToString(contribURL.openStream());
                versionText = JAMSVersion.getInstance().getVersionDateString();
                contactText = JAMSVersion.getInstance().getContactString();
            }
        } catch (IOException ioe) {
        }
        
        Properties prop = System.getProperties();
        vmText = vmText + list("java.version", ":\t\t");
        vmText = vmText + list("java.vendor", ":\t\t");
        vmText = vmText + list("java.home", ":\t\t");
        vmText = vmText + list("java.class.path", ":\t");
        vmText = vmText + list("java.library.path", ":\t");
        vmText = vmText + list("os.name", ":\t\t");
        vmText = vmText + list("os.arch", ":\t\t");
        vmText = vmText + list("os.version", ":\t\t");
        vmText = vmText + list("user.name", ":\t\t");
        vmText = vmText + list("user.home", ":\t\t");
        vmText = vmText + list("user.dir", ":\t\t");
        

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.white);
        contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        contentPanel.setPreferredSize(new Dimension(x + 10, y + 280));

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
        String text = JAMS.i18n("Version") + "\t" + versionText
                + "\n" + JAMS.i18n("Contact") + "\t" + contactText;

        JTextArea versionTextArea = new JTextArea();
        versionTextArea.setEditable(false);
        versionTextArea.setFont(new Font(Font.SANS_SERIF, 0, 11));
        versionTextArea.setColumns(20);
        versionTextArea.setRows(2);
        versionTextArea.setText(text);
        versionTextArea.setCaretPosition(0);
        versionTextArea.setPreferredSize(new Dimension(x, 30));

        contentPanel.add(versionTextArea);

        /*
         * license text
         */
        JTextArea gplTextArea = new JTextArea();
        gplTextArea.setEditable(false);
        gplTextArea.setFont(new Font(Font.SANS_SERIF, 0, 10));
        gplTextArea.setText(gplText);
        gplTextArea.setCaretPosition(0);
        JScrollPane gplScroll = new JScrollPane(gplTextArea);

        /*
         * contribution text
         */
        JTextArea contribTextArea = new JTextArea();
        contribTextArea.setEditable(false);
        contribTextArea.setFont(new Font(Font.SANS_SERIF, 0, 10));
        contribTextArea.setText(contribText);
        contribTextArea.setCaretPosition(0);
        JScrollPane contribScroll = new JScrollPane(contribTextArea);

        /*
         * vm info text
         */
        JTextArea vmTextArea = new JTextArea();
        vmTextArea.setEditable(false);
        vmTextArea.setFont(new Font(Font.SANS_SERIF, 0, 10));
        vmTextArea.setText(vmText);
        vmTextArea.setCaretPosition(0);
        JScrollPane vmScroll = new JScrollPane(vmTextArea);

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add(JAMS.i18n("Legal_notice"), gplScroll);
        tabPane.add(JAMS.i18n("Credits"), contribScroll);
        tabPane.add(JAMS.i18n("VM_INFO"), vmScroll);
        tabPane.setPreferredSize(new Dimension(x + 1, 200));

        contentPanel.add(tabPane);

        JButton closeButton = new JButton();
        closeButton.setText(JAMS.i18n("OK"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                AboutDlg.this.dispose();
            }
        });
        closeButton.setPreferredSize(new Dimension(60, 22));
        contentPanel.add(closeButton);

        //this.setAlwaysOnTop(true);
        //this.setUndecorated(true);
        //this.setModal(true);
        this.setTitle(JAMS.i18n("About"));

        pack();

        Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d2.width / 2 - getWidth() / 2, d2.height / 2 - getHeight() / 2);
    }
    
    private String list(String key, String space) {
        return key + space + System.getProperty(key) + "\n";
    }
}
