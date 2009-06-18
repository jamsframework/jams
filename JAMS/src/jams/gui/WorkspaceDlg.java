/*
 * WorkspaceDlg.java
 * Created on 18. Juni 2009, 16:03
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.gui.input.BooleanInput;
import jams.gui.input.InputComponent;
import jams.gui.input.TextInput;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.workspace.JAMSWorkspace;
import jams.workspace.JAMSWorkspace.InvalidWorkspaceException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class WorkspaceDlg extends JDialog {
    
    private JAMSWorkspace ws;

    private InputComponent titleInput;

    private JTextArea descriptionInput;

    private InputComponent persistenceInput;

    public WorkspaceDlg() {

        super();
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        GridBagLayout gbl = new GridBagLayout();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(gbl);

        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Title"), 10, 10, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Description"), 10, 20, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, new JLabel("Persistent"), 10, 30, 1, 1, 1, 1);

        titleInput = new TextInput();
        titleInput.setLength(40);
        descriptionInput = new JTextArea();
        JScrollPane descriptionScroll = new JScrollPane(descriptionInput);
        descriptionScroll.setPreferredSize(new Dimension(200, 100));
        persistenceInput = new BooleanInput();

        GUIHelper.addGBComponent(mainPanel, gbl, titleInput.getComponent(), 20, 10, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, descriptionScroll, 20, 20, 1, 1, 1, 1);
        GUIHelper.addGBComponent(mainPanel, gbl, persistenceInput.getComponent(), 20, 30, 1, 1, 1, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ws.setTitle(titleInput.getValue());
                ws.setDescription(descriptionInput.getText());
                ws.setPersistent(Boolean.parseBoolean(persistenceInput.getValue()));
                ws.saveConfig();
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
        this.setTitle("Workspace properties" + " [" + ws.getDirectory().getPath() + "]");
        this.titleInput.setValue(ws.getTitle());
        this.descriptionInput.setText(ws.getDescription());
        this.persistenceInput.setValue(Boolean.toString(ws.isPersistent()));
        setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception lnfe) {
        }

        JAMSRuntime runtime = new StandardRuntime();
        runtime.setDebugLevel(JAMS.VERBOSE);
        runtime.addErrorLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });
        runtime.addInfoLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });

        JAMSProperties properties = JAMSProperties.createJAMSProperties();
        properties.load("D:/jamsapplication/nsk.jap");
        String[] libs = JAMSTools.toArray(properties.getProperty("libs", ""), ";");


        JAMSWorkspace ws;
        try {
            ws = new JAMSWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg"), runtime, true);
        } catch (InvalidWorkspaceException iwe) {
            System.out.println(iwe.getMessage());
            return;
        }

        WorkspaceDlg wsdlg = new WorkspaceDlg();
        wsdlg.setVisible(ws);
    }
}
