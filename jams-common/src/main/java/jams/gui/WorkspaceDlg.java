/*
 * WorkspaceDlg.java
 * Created on 18. Juni 2009, 16:03
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
import jams.gui.input.BooleanInput;
import jams.gui.input.InputComponent;
import jams.gui.input.IntegerInput;
import jams.gui.input.TextInput;
import jams.workspace.JAMSWorkspace;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class WorkspaceDlg extends JDialog {

    private JAMSWorkspace ws;

    private InputComponent titleInput;
    
    private IntegerInput idInput;

    private JTextArea descriptionInput;

    private InputComponent persistenceInput;

    public WorkspaceDlg(Frame parent) {

        super(parent);
        this.setLocationByPlatform(true);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        GridBagLayout gbl = new GridBagLayout();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(gbl);

        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Title"), 10, 10, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Description"), 10, 20, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("ID"), 10, 30, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Persistent"), 10, 40, 1, 1, 1, 1);

        titleInput = new TextInput();
        titleInput.setLength(40);
        
        idInput = new IntegerInput();
        
        
        descriptionInput = new JTextArea();
        JScrollPane descriptionScroll = new JScrollPane(descriptionInput);
        descriptionScroll.setPreferredSize(new Dimension(200, 100));
        persistenceInput = new BooleanInput();

        GUIHelper.addGBComponent(mainPanel, gbl, titleInput.getComponent(), 20, 10, 2, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, descriptionScroll, 20, 20, 2, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, idInput.getComponent(), 20, 30, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel(" (Leave empty to auto-generate a new one)"), 21, 30, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, persistenceInput.getComponent(), 20, 40, 1, 1, 1, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ws.setTitle(titleInput.getValue());
                ws.setDescription(descriptionInput.getText());
                int id;
                try {
                    id = Integer.parseInt(idInput.getValue());
                } catch (NumberFormatException ex) {
                    id = -1;
                }
                ws.setID(id);
                if (persistenceInput.getValue().equalsIgnoreCase("true")) {
                    ws.setPersistent(true);
                } else {
                    ws.setPersistent(false);
                }
                ws.saveConfig();
                setVisible(false);
            }
        });
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        ActionListener cancelListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        cancelButton.addActionListener(cancelListener);
        cancelButton.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JButton.WHEN_IN_FOCUSED_WINDOW);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JScrollPane scroll = new JScrollPane(mainPanel);

        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    public void setVisible(JAMSWorkspace ws) {
        this.ws = ws;
        this.setTitle("Workspace Properties" + " [" + ws.getDirectory().getPath() + "]");
        this.titleInput.setValue(ws.getTitle());
        this.idInput.setValue(Integer.toString(ws.getID()));
        this.descriptionInput.setText(ws.getDescription());
        this.persistenceInput.setValue(Boolean.toString(ws.isPersistent()));
        setVisible(true);
    }
}
