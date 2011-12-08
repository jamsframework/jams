/*
 * NotificationDlg.java
 * Created on 08.12.2011, 08:12:22
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui;

import jams.JAMSException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class NotificationDlg extends JDialog {

    private JTextArea textArea = new JTextArea();

    public NotificationDlg(Frame owner, String title) {

        super(owner);
        this.setLocationByPlatform(true);
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());

//        this.add(new JLabel("Message"), BorderLayout.NORTH);
        this.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationDlg.this.setVisible(false);
                textArea.setText("");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(400, 500));
        this.pack();
    }

    public void addNotification(String text) {

        append(Color.black, text + "\n\n");
        setVisible(true);

    }

    public void addNotification(JAMSException ex) {

        append(Color.red, ex.getHeader() + ":\n");
        append(Color.black, ex.getMessage() + "\n\n");

        if (!isVisible()) {
            setVisible(true);
        }

    }

    private void append(Color c, String s) {
        textArea.append(s);
    }

//    private void append(Color c, String s) {
//        StyleContext sc = StyleContext.getDefaultStyleContext();
//        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
//                StyleConstants.Foreground, c);
//
//        int len = textPane.getDocument().getLength(); // same value as
//        // getText().length();
//        textPane.setCaretPosition(len); // place caret at the end (with no selection)
//        textPane.setCharacterAttributes(aset, false);
//        textPane.replaceSelection(s); // there is no selection, so inserts at caret
//    }
    
    public static void main(String[] args) {
        NotificationDlg dlg = new NotificationDlg(null, "test");
        dlg.addNotification("TEST");
        dlg.addNotification(new JAMSException("BLA", "ERROR"));
    }
}
