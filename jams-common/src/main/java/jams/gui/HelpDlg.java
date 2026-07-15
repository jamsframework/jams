/*
 * HelpDlg.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

import jams.gui.tools.GUIHelper;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jams.JAMS;
import jams.meta.HelpComponent;
import jams.tools.StringTools;

/**
 *
 * @author Heiko Busch
 */
public class HelpDlg extends JDialog {

    public final static int OK_RESULT = 0;
    public final static int CANCEL_RESULT = -1;
    private HelpComponent helpComponent;
    /**
     * the base url coming from outside 
     */
    private String baseUrl = "";
    /**
     *  the pane to be filled with content
     */
    private JTextPane webPagePane;

    public HelpDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        init();
    }

    public HelpComponent getHelpComponent() {
        return helpComponent;
    }

    public void setHelpComponent(HelpComponent helpComponent) {
        this.helpComponent = helpComponent;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * init the help dialog without filling content
     */
    public void init() {
        setModal(false);
        this.setTitle(JAMS.i18n("Help"));

        this.setLayout(new BorderLayout());
        GridBagLayout gbl = new GridBagLayout();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(gbl);

        GUIHelper.addGBComponent(contentPanel, gbl, new JPanel(), 0, 0, 1, 1, 0, 0);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        ActionListener okListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        okButton.addActionListener(okListener);
        getRootPane().setDefaultButton(okButton);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        webPagePane = new JTextPane();
        webPagePane.setEditable(false); // Start read-only
        JScrollPane scrollPane = new JScrollPane(webPagePane);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * load the content of helpComponent into webPagePane
     * content could be text or url-page
     * 
     * @param helpComponent
     */
    public void load(HelpComponent helpComponent) {

        setHelpComponent(helpComponent);

        if (this.helpComponent.hasHelpText()) {
            webPagePane.setContentType("text/html");
            webPagePane.setText(this.helpComponent.getHelpText());
            this.setVisible(true);
        }
        if (this.helpComponent.hasHelpURL()) {
            String url = this.baseUrl;
            if (!StringTools.isEmptyString(url)) {
                url += "/";
            }
            url += this.helpComponent.getHelpURL();

            try {
//                webPagePane.setContentType("text/html");
//                webPagePane.setPage(url);
                GUIHelper.openURL(url);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(webPagePane, new String[]{
                            JAMS.i18n("Unable_to_open_file"), url
                        }, JAMS.i18n("File_Open_Error"),
                        JOptionPane.ERROR_MESSAGE);
                setCursor(Cursor.getDefaultCursor());
            }
        }

        pack();
    }
}
