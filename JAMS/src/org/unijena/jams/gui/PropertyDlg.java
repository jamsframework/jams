/*
 * PropertyDlg.java
 * Created on 11. April 2006, 21:47
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import org.unijena.jams.*;
import org.unijena.jams.gui.input.BooleanInput;
import org.unijena.jams.gui.input.FileInput;
import org.unijena.jams.gui.input.TextInput;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.gui.input.FileListInput;

/**
 *
 * @author S. Kralisch
 */
public class PropertyDlg extends JDialog {

    private static final int JCOMP_HEIGHT = 20;
    private FileListInput list;
    private BooleanInput verboseCheck,  windowEnable,  windowOnTop,  errorDlg;
    private JSpinner debugSpinner;
    private FileInput infoFile,  errorFile;
    private TextInput windowHeight,  windowWidth, helpBaseURL;
    private JAMSProperties properties;
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 0;
    private int result = CANCEL_OPTION;

    public PropertyDlg(Frame owner, JAMSProperties properties) {

        super(owner);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.properties = properties;

        setTitle("JAMS Properties");
        setModal(true);

        JPanel contentPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        contentPanel.setLayout(gbl);
        //contentPanel.setPreferredSize(new Dimension(420, 250));

        int y = 0;

        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Libraries:"), 0, y, 1, 1, 0, 0);
        list = new FileListInput();
        list.setPreferredSize(new Dimension(295, 130));
        LHelper.addGBComponent(contentPanel, gbl, list, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Command line output:"), 0, y, 1, 1, 0, 0);
        verboseCheck = new BooleanInput();
        verboseCheck.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, verboseCheck, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Debug level:"), 0, y, 1, 1, 0, 0);
        debugSpinner = new JSpinner();
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.setLayout(new BorderLayout());
        spinnerPanel.add(debugSpinner, BorderLayout.WEST);
        ArrayList<Integer> vals = new ArrayList<Integer>();
        vals.add(0);
        vals.add(1);
        vals.add(2);
        vals.add(3);
        SpinnerListModel sModel = new SpinnerListModel(vals);
        debugSpinner.setModel(sModel);
        debugSpinner.setPreferredSize(new Dimension(35, 26));
        LHelper.addGBComponent(contentPanel, gbl, spinnerPanel, 1, y, 1, 1, 0, 0);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Info log file:"), 0, y, 1, 1, 0, 0);
        infoFile = new FileInput();
        infoFile.setPreferredSize(new Dimension(286, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, infoFile, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Error log file:"), 0, y, 1, 1, 0, 0);
        errorFile = new FileInput();
        errorFile.setPreferredSize(new Dimension(286, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, errorFile, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Model window visible:"), 0, y, 1, 1, 0, 0);
        windowEnable = new BooleanInput();
        windowEnable.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, windowEnable, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Show dialog on errors:"), 0, y, 1, 1, 0, 0);
        errorDlg = new BooleanInput();
        errorDlg.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, errorDlg, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Model window on top:"), 0, y, 1, 1, 0, 0);
        windowOnTop = new BooleanInput();
        windowOnTop.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, windowOnTop, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Model window width:"), 0, y, 1, 1, 0, 0);
        windowWidth = new TextInput();
        windowWidth.getComponent().setPreferredSize(new Dimension(100, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, windowWidth, 1, y, 1, 1, 1, 1);
        JPanel buttonPanel = new JPanel();

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Model window height:"), 0, y, 1, 1, 0, 0);
        windowHeight = new TextInput();
        windowHeight.getComponent().setPreferredSize(new Dimension(100, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, windowHeight, 1, y, 1, 1, 1, 1);

        y++;
        LHelper.addGBComponent(contentPanel, gbl, new JLabel("Help Base URL:"), 0, y, 1, 1, 0, 0);
        helpBaseURL = new TextInput();
        helpBaseURL.getComponent().setPreferredSize(new Dimension(300, JCOMP_HEIGHT));
        LHelper.addGBComponent(contentPanel, gbl, helpBaseURL, 1, y, 1, 1, 1, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.add(contentPanel);

        getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void cancel() {
        setVisible(false);
        result = CANCEL_OPTION;
    }

    private void ok() {
        setVisible(false);
        result = APPROVE_OPTION;
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane pane = super.createRootPane();
        Action cancelAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                cancel();
            }
        };
        Action okAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ok();
            }
        };
        InputMap inputMap = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "ENTER");
        pane.getActionMap().put("ESCAPE", cancelAction);
        pane.getActionMap().put("ENTER", okAction);

        return pane;
    }

    public void setProperties(JAMSProperties properties) {

        this.properties = properties;

        String[] libs = JAMSTools.toArray(properties.getProperty("libs"), ";");
        Vector<String> v = new Vector<String>();
        for (int i = 0; i < libs.length; i++) {
            v.add(libs[i]);
        }
        list.setListData(v);

        verboseCheck.setValue(properties.getProperty("verbose"));

        Integer debugLevel = 1;
        try {
            debugLevel = Integer.parseInt(properties.getProperty("debug"));
        } catch (NumberFormatException e) {
        }
        debugSpinner.setValue(debugLevel);

        errorFile.setFile(properties.getProperty("errorlog"));
        infoFile.setFile(properties.getProperty("infolog"));

        windowEnable.setValue(properties.getProperty("windowenable"));
        errorDlg.setValue(properties.getProperty("errordlg"));
        windowOnTop.setValue(properties.getProperty("windowontop"));

        windowHeight.setValue(properties.getProperty("windowheight"));
        windowWidth.setValue(properties.getProperty("windowwidth"));
        helpBaseURL.setValue(properties.getProperty("helpbaseurl"));
    }

    public void validateProperties() {

        Vector<String> v = list.getListData();
        String libs = "";
        if (v.size() > 0) {
            libs = v.get(0);
        }

        for (int i = 1; i < v.size(); i++) {
            libs += ";" + v.get(i);
        }
        properties.setProperty("libs", libs);
        properties.setProperty("debug", debugSpinner.getValue().toString());
        properties.setProperty("verbose", verboseCheck.getValue());
        properties.setProperty("errorlog", errorFile.getFileName());
        properties.setProperty("infolog", infoFile.getFileName());
        properties.setProperty("windowenable", windowEnable.getValue());
        properties.setProperty("errordlg", errorDlg.getValue());
        properties.setProperty("windowontop", windowOnTop.getValue());
        properties.setProperty("windowheight", windowHeight.getValue());
        properties.setProperty("windowwidth", windowWidth.getValue());
        properties.setProperty("helpbaseurl", helpBaseURL.getValue());
    }

    public JAMSProperties getProperties() {
        validateProperties();
        return properties;
    }

    public int getResult() {
        return result;
    }
}
