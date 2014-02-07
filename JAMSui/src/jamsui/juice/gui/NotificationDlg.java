/*
 * NotificationDlg.java
 * Created on 08.12.2011, 08:12:22
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class NotificationDlg extends JDialog {

    private JTextArea textArea = new JTextArea();

    public NotificationDlg(Frame owner, String title) {

        super(owner);
        setModal(false);
        this.setLocationByPlatform(true);
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                textArea.setText("");
            }
        });

        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationDlg.this.setVisible(false);
            }
        };
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);


        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Arial", 0, 10));
//        textArea.setLineWrap(true);

//        this.add(new JLabel("Message"), BorderLayout.NORTH);
        this.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationDlg.this.setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        JRootPane pane = getRootPane();
        InputMap inputMap = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "ENTER");
        Action cancelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NotificationDlg.this.setVisible(false);
            }
        };
        pane.getActionMap().put("ESCAPE", cancelAction);
        pane.getActionMap().put("ENTER", cancelAction);

        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(400, 500));
        this.pack();
    }

    public void addNotification(String text) {

        textArea.append(text);
        setVisible(true);
        requestFocus();

    }
}
