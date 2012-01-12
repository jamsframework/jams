/*
 * PropertyDlg.java
 * Created on 11. April 2006, 21:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import jams.*;
import jams.gui.input.BooleanInput;
import jams.gui.input.FileInput;
import jams.gui.input.TextInput;
import jams.gui.input.FileListInput;
import jams.tools.StringTools;

/**
 *
 * @author S. Kralisch
 */
public class PropertyDlg extends JDialog {

    private static final int JCOMP_HEIGHT = 20;
    private FileListInput list;
    private BooleanInput verboseCheck,  windowEnable,  windowOnTop,  errorDlg, profiling, defaultWSPath;
    private JSpinner debugSpinner;
    private FileInput infoFile,  errorFile;
    private TextInput windowHeight,  windowWidth, helpBaseURL, userName, forceLocale, charset;
    private SystemProperties properties;
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 0;
    private int result = CANCEL_OPTION;

    public PropertyDlg(Frame owner, SystemProperties properties) {

        super(owner);
        this.setLayout(new BorderLayout());
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.properties = properties;

        setTitle(JAMS.i18n("JAMS_Preferences"));
        setModal(true);

        JPanel contentPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        contentPanel.setLayout(gbl);
        //contentPanel.setPreferredSize(new Dimension(420, 250));

        int y = 0;

        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Libraries:")), 0, y, 1, 1, 0, 0);
        list = new FileListInput();
        list.setPreferredSize(new Dimension(295, 130));
        GUIHelper.addGBComponent(contentPanel, gbl, list, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Command_line_output:")), 0, y, 1, 1, 0, 0);
        verboseCheck = new BooleanInput();
        verboseCheck.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, verboseCheck, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Verbosity_level:")), 0, y, 1, 1, 0, 0);
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
        GUIHelper.addGBComponent(contentPanel, gbl, spinnerPanel, 1, y, 1, 1, 0, 0);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Info_log_file:")), 0, y, 1, 1, 0, 0);
        infoFile = new FileInput();
        infoFile.setPreferredSize(new Dimension(286, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, infoFile, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Error_log_file:")), 0, y, 1, 1, 0, 0);
        errorFile = new FileInput();
        errorFile.setPreferredSize(new Dimension(286, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, errorFile, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Model_window_visible:")), 0, y, 1, 1, 0, 0);
        windowEnable = new BooleanInput();
        windowEnable.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, windowEnable, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Show_dialog_on_errors:")), 0, y, 1, 1, 0, 0);
        errorDlg = new BooleanInput();
        errorDlg.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, errorDlg, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Model_window_on_top:")), 0, y, 1, 1, 0, 0);
        windowOnTop = new BooleanInput();
        windowOnTop.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, windowOnTop, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Create_profile_info:")), 0, y, 1, 1, 0, 0);
        profiling = new BooleanInput();
        profiling.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, profiling, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Default_WS_path:")), 0, y, 1, 1, 0, 0);
        defaultWSPath = new BooleanInput();
        defaultWSPath.setPreferredSize(new Dimension(295, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, defaultWSPath, 1, y, 1, 1, 1, 1);
        
        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Force_Localization:")), 0, y, 1, 1, 0, 0);
        forceLocale = new TextInput();
        forceLocale.getComponent().setPreferredSize(new Dimension(40, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, forceLocale, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Character_Set:")), 0, y, 1, 1, 0, 0);
        charset = new TextInput();
        charset.getComponent().setPreferredSize(new Dimension(100, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, charset, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Model_window_width:")), 0, y, 1, 1, 0, 0);
        windowWidth = new TextInput();
        windowWidth.getComponent().setPreferredSize(new Dimension(100, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, windowWidth, 1, y, 1, 1, 1, 1);
        JPanel buttonPanel = new JPanel();

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Model_window_height:")), 0, y, 1, 1, 0, 0);
        windowHeight = new TextInput();
        windowHeight.getComponent().setPreferredSize(new Dimension(100, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, windowHeight, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("User_name:")), 0, y, 1, 1, 0, 0);
        userName = new TextInput();
        userName.getComponent().setPreferredSize(new Dimension(300, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, userName, 1, y, 1, 1, 1, 1);

        y++;
        GUIHelper.addGBComponent(contentPanel, gbl, new JLabel(JAMS.i18n("Help_Base_URL:")), 0, y, 1, 1, 0, 0);
        helpBaseURL = new TextInput();
        helpBaseURL.getComponent().setPreferredSize(new Dimension(300, JCOMP_HEIGHT));
        GUIHelper.addGBComponent(contentPanel, gbl, helpBaseURL, 1, y, 1, 1, 1, 1);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);


        JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPanel.add(cancelButton);

        getContentPane().add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
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

    public void setProperties(SystemProperties properties) {

        this.properties = properties;

        String[] libs = StringTools.toArray(properties.getProperty(SystemProperties.LIBS_IDENTIFIER), ";");
        Vector<Object> v = new Vector<Object>();
        for (int i = 0; i < libs.length; i++) {
            v.add(libs[i]);
        }
        list.setListData(v);

        verboseCheck.setValue(properties.getProperty(SystemProperties.VERBOSITY_IDENTIFIER));

        Integer debugLevel = 1;
        try {
            debugLevel = Integer.parseInt(properties.getProperty(SystemProperties.DEBUG_IDENTIFIER));
        } catch (NumberFormatException e) {
        }
        debugSpinner.setValue(debugLevel);

        errorFile.setFile(properties.getProperty(SystemProperties.ERRORLOG_IDENTIFIER));
        infoFile.setFile(properties.getProperty(SystemProperties.INFOLOG_IDENTIFIER));

        windowEnable.setValue(properties.getProperty(SystemProperties.WINDOWENABLE_IDENTIFIER));
        errorDlg.setValue(properties.getProperty(SystemProperties.ERRORDLG_IDENTIFIER));
        windowOnTop.setValue(properties.getProperty(SystemProperties.WINDOWONTOP_IDENTIFIER));
        profiling.setValue(properties.getProperty(SystemProperties.PROFILE_IDENTIFIER));
        defaultWSPath.setValue(properties.getProperty(SystemProperties.USE_DEFAULT_WS_PATH));
        forceLocale.setValue(properties.getProperty(SystemProperties.LOCALE_IDENTIFIER));
        charset.setValue(properties.getProperty(SystemProperties.CHARSET_IDENTIFIER));

        windowHeight.setValue(properties.getProperty(SystemProperties.WINDOWHEIGHT_IDENTIFIER));
        windowWidth.setValue(properties.getProperty(SystemProperties.WINDOWWIDTH_IDENTIFIER));
        userName.setValue(properties.getProperty(SystemProperties.USERNAME_IDENTIFIER));
        helpBaseURL.setValue(properties.getProperty(SystemProperties.HELPBASEURL_IDENTIFIER));
    }

    public void validateProperties() {

        Vector<Object> v = list.getListData();
        String libs = "";
        if (v.size() > 0) {
            libs = v.get(0).toString();
        }

        for (int i = 1; i < v.size(); i++) {
            libs += ";" + v.get(i);
        }
        properties.setProperty(SystemProperties.LIBS_IDENTIFIER, libs);
        properties.setProperty(SystemProperties.DEBUG_IDENTIFIER, debugSpinner.getValue().toString());
        properties.setProperty(SystemProperties.VERBOSITY_IDENTIFIER, verboseCheck.getValue());
        properties.setProperty(SystemProperties.ERRORLOG_IDENTIFIER, errorFile.getFileName());
        properties.setProperty(SystemProperties.INFOLOG_IDENTIFIER, infoFile.getFileName());
        properties.setProperty(SystemProperties.WINDOWENABLE_IDENTIFIER, windowEnable.getValue());
        properties.setProperty(SystemProperties.ERRORDLG_IDENTIFIER, errorDlg.getValue());
        properties.setProperty(SystemProperties.WINDOWONTOP_IDENTIFIER, windowOnTop.getValue());
        properties.setProperty(SystemProperties.PROFILE_IDENTIFIER, profiling.getValue());
        properties.setProperty(SystemProperties.USE_DEFAULT_WS_PATH, defaultWSPath.getValue());
        properties.setProperty(SystemProperties.LOCALE_IDENTIFIER, forceLocale.getValue());
        properties.setProperty(SystemProperties.CHARSET_IDENTIFIER, charset.getValue());
        properties.setProperty(SystemProperties.WINDOWHEIGHT_IDENTIFIER, windowHeight.getValue());
        properties.setProperty(SystemProperties.WINDOWWIDTH_IDENTIFIER, windowWidth.getValue());
        properties.setProperty(SystemProperties.USERNAME_IDENTIFIER, userName.getValue());
        properties.setProperty(SystemProperties.HELPBASEURL_IDENTIFIER, helpBaseURL.getValue());
    }

    public SystemProperties getProperties() {
        validateProperties();
        return properties;
    }

    public int getResult() {
        return result;
    }
}
