/*
 * LauncherFrame.java
 * Created on 27. August 2006, 21:55
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.jams.gui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.unijena.jams.*;
import org.unijena.jams.io.XMLIO;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch
 */
public class GUILauncher extends LauncherPane {

    private JMenuBar mainMenu;
    private JMenu logsMenu;
    private JMenuItem saveItem,  saveAsItem;
    private JFileChooser jfc;
    private PropertyDlg propertyDlg;
    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, "Info Log");
    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, "Error Log");
    private String modelFilename;


    /*
    //public static final int APPROVE_OPTION = 1;
    //public static final int EXIT_OPTION = 0;
    private static final String baseTitle = "JAMS Launcher";
    //private int result = EXIT_OPTION;
    private Map<InputComponent, Element> inputMap;
    private Map<InputComponent, JScrollPane> groupMap;
    
    private Document modelDocument = null;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JAMSProperties properties;
    private JButton runButton;
    
    
    
    private HelpDlg helpDlg;
    
    private String initialModelDocString = "";
    private JAMSRuntime runtime;
    private Runnable modelLoading;
    private WorkerDlg setupModelDlg;
    private Font titledBorderFont;
     */
    public GUILauncher(JAMSProperties properties) {
        super(properties);
    }

    public GUILauncher(JAMSProperties properties, String modelFilename, String cmdLineArgs) {
        //super(properties, modelFilename, cmdLineArgs);
        this(properties);
        this.modelFilename = modelFilename;
        loadModelDefinition(modelFilename, JAMSTools.toArray(cmdLineArgs, ";"));
    }

    protected void loadModelDefinition(String modelFilename, String[] args) {

        // first close any already opened models
        if (!closeModel()) {
            return;
        }
        super.loadModelDefinition(modelFilename, args);
    }

    protected void init() throws HeadlessException, DOMException, NumberFormatException {

        // create additional dialogs
        this.propertyDlg = new PropertyDlg(this, getProperties());
        jfc = LHelper.getJFileChooser();

        // menu stuff
        mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load Model");
        loadItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                File file = null;
                if (modelFilename != null) {
                    file = new File(modelFilename);
                } else {
                    file = new File(System.getProperty("user.dir"));
                }
                jfc.setCurrentDirectory(file);
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (jfc.showOpenDialog(GUILauncher.this) == JFileChooser.APPROVE_OPTION) {

                    String modelFilename = jfc.getSelectedFile().getAbsolutePath();
                    loadModelDefinition(modelFilename, null);

                }
            }
        });
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(loadItem);

        saveItem = new JMenuItem("Save Model");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                saveModel();
            }
        });
        saveItem.setEnabled(false);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveItem);

        saveAsItem = new JMenuItem("Save Model As...");
        saveAsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                File file = null;
                if (modelFilename != null) {
                    file = new File(modelFilename);
                } else {
                    file = new File(System.getProperty("user.dir"));
                }

                jfc.setCurrentDirectory(file);

                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (jfc.showSaveDialog(GUILauncher.this) == JFileChooser.APPROVE_OPTION) {
                    modelFilename = jfc.getSelectedFile().getAbsolutePath();
                    saveModel();
                }
            }
        });
        saveAsItem.setEnabled(false);
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                exit();
            }
        });
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);
        getMainMenu().add(fileMenu);

        JMenu editMenu = new JMenu("Extras");
        JMenuItem editOptionsItem = new JMenuItem("Edit Options");
        editOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }

            }
        });
        editMenu.add(editOptionsItem);

        JMenuItem loadOptionsItem = new JMenuItem("Load Options");
        loadOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showOpenDialog(GUILauncher.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().load(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
            }
        });
        editMenu.add(loadOptionsItem);

        JMenuItem saveOptionsItem = new JMenuItem("Save Options as...");
        saveOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showSaveDialog(GUILauncher.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().save(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
            }
        });
        editMenu.add(saveOptionsItem);
        getMainMenu().add(editMenu);

        logsMenu = new JMenu("Logs");
        JMenuItem infoLogItem = new JMenuItem("Model info log");
        infoLogItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getInfoDlg().setVisible(true);
            }
        });
        getLogsMenu().add(infoLogItem);
        JMenuItem errorLogItem = new JMenuItem("Model error log");
        errorLogItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getErrorDlg().setVisible(true);
            }
        });
        getLogsMenu().add(errorLogItem);
        getMainMenu().add(getLogsMenu());

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                new AboutDlg(GUILauncher.this).setVisible(true);
            }
        });
        helpMenu.add(aboutItem);
        getMainMenu().add(helpMenu);

        setJMenuBar(getMainMenu());

        super.init();

    }

    protected void processInfoLog(String logText) {
        GUILauncher.this.getInfoDlg().appendText(logText);
    }

    protected void processErrorLog(String logText) {
        GUILauncher.this.getErrorDlg().appendText(logText);
    }

    protected void exit() {
        //close the current model
        if (!closeModel()) {
            return;
        }

        // finally write property file to default location
        try {
            String defaultFile = getProperties().getDefaultFilename();
            getProperties().save(defaultFile);
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }

        super.exit();
        System.exit(0);
    }

    private boolean closeModel() {

        if (getModelDocument() == null) {
            return true;
        }

        // check for invalid parameter values
        if (!verifyInputs()) {
            int result = LHelper.showYesNoDlg(this, "Found invalid parameter values " +
                    "which won't be saved. Proceed anyway?", "Invalid parameter values");
            if (result == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        // update all properties
        updateProperties();

        if (getModelDocument() != null) {
            String modelDocString = XMLIO.getStringFromDocument(getModelDocument());
            if (!getInitialModelDocString().equals(modelDocString)) {
                int result = LHelper.showYesNoCancelDlg(this, "Save modifications in " + modelFilename + "?", "JAMS Launcher: unsaved modifications");
                if (result == JOptionPane.CANCEL_OPTION) {
                    return false;
                } else if (result == JOptionPane.OK_OPTION) {
                    saveModel();
                }
            }
        }
        return true;
    }

    private void saveModel() {

        // update all properties
        updateProperties();

        try {
            XMLIO.writeXmlFile(getModelDocument(), modelFilename);
        } catch (IOException ioe) {
            LHelper.showErrorDlg(GUILauncher.this, "Error saving configuration to " + modelFilename, "Error");
            return;
        }
    //LHelper.showInfoDlg(LauncherFrame.this, "Configuration has been saved to " + LauncherFrame.this.modelFilename, "Info");
    }

    protected void fillAttributes(final Document doc) {

        // extract some model information
        Element root = doc.getDocumentElement();
        setTitle(BASE_TITLE + ": " + root.getAttribute("name") + " [" + modelFilename + "]");
        setHelpBaseUrl(root.getAttribute("helpbaseurl"));

    }

    public JMenuBar getMainMenu() {
        return mainMenu;
    }

    protected JMenu getLogsMenu() {
        return logsMenu;
    }

    protected LogViewDlg getInfoDlg() {
        return infoDlg;
    }

    protected LogViewDlg getErrorDlg() {
        return errorDlg;
    }
}
