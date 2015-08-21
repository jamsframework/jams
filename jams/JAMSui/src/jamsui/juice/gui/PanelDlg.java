/*
 * PanelDlg.java
 * Created on 12. Juli 2008, 01:44
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
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jams.JAMS;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public abstract class PanelDlg extends JDialog {

    private JPanel panel;
    
    /**
     * Create a JDialog that contains a JPanel object and OK/Cancel buttons
     * @param owner The parent window.
     * @param title The dialog window title.
     * @param panel The panel to be displayed.
     */
    public PanelDlg(Frame owner, String title, JPanel panel) {
        super(owner, title);
        this.panel = panel;
        setLocationRelativeTo(owner);
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processOK();
            }
        });
        JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processCancel();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
    }
    
    public abstract void processOK();

    public abstract void processCancel();

    public JPanel getPanel() {
        return panel;
    }

}
