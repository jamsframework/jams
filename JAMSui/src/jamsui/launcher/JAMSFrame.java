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
package jamsui.launcher;

import jams.gui.*;
import jams.JAMS;
import jams.SystemProperties;
import jams.tools.JAMSTools;
import jams.gui.tools.GUIHelper;
import jams.JAMSFileFilter;
import jams.io.ParameterProcessor;
import jams.tools.XMLTools;
import jams.io.XMLProcessor;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jamsui.juice.JUICE;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import java.awt.Frame;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import reg.JAMSExplorer;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSFrame extends JAMSLauncher {

    private JMenuBar mainMenu;

    private JMenu logsMenu, modelMenu;

    private JMenuItem saveItem, saveAsItem;

    private JFileChooser jfc;

    private JDialog rtManagerDlg;

    private PropertyDlg propertyDlg;

    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, JAMS.resources.getString("Info_Log"));

    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, JAMS.resources.getString("Error_Log"));

    private String modelFilename;

    private Action editPrefsAction, loadPrefsAction, savePrefsAction,
            loadModelAction, saveModelAction, saveAsModelAction, exitAction,
            aboutAction, loadModelParamAction, saveModelParamAction,
            rtManagerAction, infoLogAction, errorLogAction, onlineAction,
            explorerAction, editModelAction;

    public JAMSFrame(Frame parent, SystemProperties properties) {
        super(parent, properties);
    }

    public JAMSFrame(Frame parent, SystemProperties properties, String modelFilename, String cmdLineArgs) {
        //super(properties, modelFilename, cmdLineArgs);
        this(parent, properties);
        loadModelDefinition(modelFilename, StringTools.toArray(cmdLineArgs, ";"));
        loadPath = new File(modelFilename);
    }

    protected void loadModelDefinition(String fileName, String[] args) {

        // first close any already opened models
        if (!closeModel()) {
            return;
        }

        try {

            //check if file exists
            File file = new File(fileName);
            if (!file.exists()) {
                GUIHelper.showErrorDlg(this, JAMS.resources.getString("Model_file_") + fileName + JAMS.resources.getString("_could_not_be_found!"), JAMS.resources.getString("File_Open_Error"));
                return;
            }

            // first do search&replace on the input xml file
            String newFilename = XMLProcessor.modelDocConverter(fileName);
            if (!newFilename.equalsIgnoreCase(fileName)) {
                fileName = newFilename;
                GUIHelper.showInfoDlg(JAMSFrame.this,
                        JAMS.resources.getString("The_model_definition_in_") + fileName + JAMS.resources.getString("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newFilename + JAMS.resources.getString("_while_your_original_file_was_left_untouched."), JAMS.resources.getString("Info"));
            }

            // create string from input model definition file and replace "%x" occurences by cmd line data
            String xmlString = FileTools.fileToString(fileName);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%" + i, args[i]);
                }
            }

            // finally, create the model document from the string
            this.modelDocument = XMLTools.getDocumentFromString(xmlString);
            this.initialModelDocString = XMLTools.getStringFromDocument(this.modelDocument);

        } catch (IOException ioe) {
            GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.resources.getString("The_specified_model_configuration_file_") + fileName + JAMS.resources.getString("_could_not_be_found!"), JAMS.resources.getString("Error"));
        } catch (SAXException se) {
            GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.resources.getString("The_specified_model_configuration_file_") + fileName + JAMS.resources.getString("_contains_errors!"), JAMS.resources.getString("Error"));
        }

        this.modelFilename = fileName;

        fillAttributes(this.getModelDocument());
        fillTabbedPane(this.getModelDocument());

        saveModelAction.setEnabled(true);
        saveAsModelAction.setEnabled(true);
        modelMenu.setEnabled(true);
        editModelAction.setEnabled(true);
        getRunModelAction().setEnabled(true);

        //GUIHelper.showInfoDlg(JAMSLauncher.this, "Model has been successfully loaded!", "Info");
    }

    protected void init() throws HeadlessException, DOMException, NumberFormatException {

        super.init();

        getRunModelAction().setEnabled(false);

        // define some actions
        editPrefsAction = new AbstractAction(JAMS.resources.getString("Edit_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };

        loadPrefsAction = new AbstractAction(JAMS.resources.getString("Load_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(""));
                //jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showOpenDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().load(stringValue);
                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        savePrefsAction = new AbstractAction(JAMS.resources.getString("Save_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(""));
                //jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showSaveDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().save(stringValue);
                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        loadModelAction = new AbstractAction(JAMS.resources.getString("Open_Model...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (!StringTools.isEmptyString(modelFilename)) {
                    jfc.setSelectedFile(new File(modelFilename));
                } else {
                    jfc.setSelectedFile(new File(""));
                }
                if (jfc.showOpenDialog(JAMSFrame.this) == JFileChooser.APPROVE_OPTION) {

                    loadModelDefinition(jfc.getSelectedFile().getAbsolutePath(), null);

                }
            }
        };

        saveModelAction = new AbstractAction(JAMS.resources.getString("Save_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveModel();
            }
        };
        saveModelAction.setEnabled(false);

        saveAsModelAction = new AbstractAction(JAMS.resources.getString("Save_Model_As...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (!StringTools.isEmptyString(modelFilename)) {
                    jfc.setSelectedFile(new File(modelFilename));
                } else {
                    jfc.setSelectedFile(new File(""));
                }
                if (jfc.showSaveDialog(JAMSFrame.this) == JFileChooser.APPROVE_OPTION) {
                    modelFilename = jfc.getSelectedFile().getAbsolutePath();
                    saveModel();
                }
            }
        };
        saveAsModelAction.setEnabled(false);

        editModelAction = new AbstractAction(JAMS.resources.getString("Edit_Model...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!closeModel()) {
                    return;
                }
                JAMSFrame.this.dispose();
                if (JUICE.getJuiceFrame() == null) {
                    JUICE.createJUICEFrame();
                }
                JUICE.getJuiceFrame().loadModel(JAMSFrame.this.modelFilename);
            }
        };
        editModelAction.setEnabled(false);

        exitAction = new AbstractAction(JAMS.resources.getString("Exit")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        aboutAction = new AbstractAction(JAMS.resources.getString("About")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDlg(JAMSFrame.this).setVisible(true);
            }
        };

        onlineAction = new AbstractAction(JAMS.resources.getString("JAMS_online...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIHelper.openURL(JAMS.resources.getString("JAMS_URL"));
            }
        };

        loadModelParamAction = new AbstractAction(JAMS.resources.getString("Load_Model_Parameter...")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getParameterFilter());
                jfc.setSelectedFile(new File(""));
                int result = jfc.showOpenDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfc.getSelectedFile().getAbsolutePath();
                    loadParams(new File(path));
                }
            }
        };

        saveModelParamAction = new AbstractAction(JAMS.resources.getString("Save_Model_Parameter...")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getParameterFilter());
                jfc.setSelectedFile(new File(""));
                int result = jfc.showSaveDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfc.getSelectedFile().getAbsolutePath();
                    saveParams(new File(path));
                }
            }
        };

        rtManagerAction = new AbstractAction(JAMS.resources.getString("Show_Runtime_Manager...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                rtManagerDlg.setVisible(true);
            }
        };

        infoLogAction = new AbstractAction(JAMS.resources.getString("Info_Log...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                getInfoDlg().setVisible(true);
            }
        };

        errorLogAction = new AbstractAction(JAMS.resources.getString("Error_Log...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                getErrorDlg().setVisible(true);
            }
        };

        explorerAction = new AbstractAction(JAMS.resources.getString("JEDI")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                openExplorer();
            }
        };

        // create additional dialogs
        this.propertyDlg = new PropertyDlg(this, getProperties());
        jfc = GUIHelper.getJFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setCurrentDirectory(JAMSui.getBaseDir());

        // runtime manager dlg
        rtManagerDlg = new JDialog(this, JAMS.resources.getString("Runtime_Manager"));
        rtManagerDlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        rtManagerDlg.setLocationByPlatform(true);
        RuntimeManagerPanel rtManagerPanel = new RuntimeManagerPanel();
        rtManagerDlg.getContentPane().add(rtManagerPanel);
        rtManagerDlg.pack();

        JButton closeButton = new JButton(JAMS.resources.getString("Close"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                rtManagerDlg.setVisible(false);
            }
        });
        rtManagerPanel.getButtonPanel().add(closeButton);


        // menu stuff
        mainMenu = new JMenuBar();

        // file menu
        JMenu fileMenu = new JMenu(JAMS.resources.getString("File"));

        JMenuItem loadItem = new JMenuItem(loadModelAction);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add(loadItem);

        saveItem = new JMenuItem(saveModelAction);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveItem);

        saveAsItem = new JMenuItem(saveAsModelAction);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);
        getMainMenu().add(fileMenu);

        // extras menu
        JMenu editMenu = new JMenu(JAMS.resources.getString("Extras"));

        JMenuItem editOptionsItem = new JMenuItem(editPrefsAction);
        editOptionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        editMenu.add(editOptionsItem);

        JMenuItem loadOptionsItem = new JMenuItem(loadPrefsAction);
        editMenu.add(loadOptionsItem);

        JMenuItem saveOptionsItem = new JMenuItem(savePrefsAction);
        editMenu.add(saveOptionsItem);
        getMainMenu().add(editMenu);

        // model menu
        modelMenu = new JMenu(JAMS.resources.getString("Model"));
        modelMenu.setEnabled(false);
        mainMenu.add(modelMenu);

        JMenuItem runModelItem = new JMenuItem(getRunModelAction());
        runModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(runModelItem);

        JMenuItem explorerItem = new JMenuItem(explorerAction);
        explorerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        modelMenu.add(explorerItem);

        JMenuItem editModelItem = new JMenuItem(editModelAction);
        editModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK));
        modelMenu.add(editModelItem);

        modelMenu.add(new JSeparator());

        JMenuItem loadModelParamItem = new JMenuItem(loadModelParamAction);
        //loadModelParamItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(loadModelParamItem);

        JMenuItem saveModelParamItem = new JMenuItem(saveModelParamAction);
        //loadModelParamItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(saveModelParamItem);

        modelMenu.add(new JSeparator());

        JMenuItem rtManagerItem = new JMenuItem(rtManagerAction);
        modelMenu.add(rtManagerItem);

        // logs menu
        logsMenu = new JMenu(JAMS.resources.getString("Logs"));

        JMenuItem infoLogItem = new JMenuItem(infoLogAction);
        getLogsMenu().add(infoLogItem);

        JMenuItem errorLogItem = new JMenuItem(errorLogAction);
        getLogsMenu().add(errorLogItem);
        getMainMenu().add(getLogsMenu());

        // help menu
        JMenu helpMenu = new JMenu(JAMS.resources.getString("Help"));

        JMenuItem onlineItem = new JMenuItem(onlineAction);
        helpMenu.add(onlineItem);

        JMenuItem aboutItem = new JMenuItem(aboutAction);
        helpMenu.add(aboutItem);

        getMainMenu().add(helpMenu);

        setJMenuBar(getMainMenu());

        JButton loadButton = new JButton(loadModelAction);
        loadButton.setText("");
        loadButton.setToolTipText(JAMS.resources.getString("Open_Model..."));
        loadButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelOpen.png")));
        getToolBar().add(loadButton);

        JButton saveButton = new JButton(saveModelAction);
        saveButton.setText("");
        saveButton.setToolTipText(JAMS.resources.getString("Save_Model"));
        saveButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelSave.png")));
        getToolBar().add(saveButton);

        JButton infoLogButton = new JButton(infoLogAction);
        infoLogButton.setText("");
        infoLogButton.setToolTipText(JAMS.resources.getString("Show_Info_Log..."));
        infoLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/InfoLog.png")));
        getToolBar().add(infoLogButton);

        JButton errorLogButton = new JButton(errorLogAction);
        errorLogButton.setText("");
        errorLogButton.setToolTipText(JAMS.resources.getString("Show_Error_Log..."));
        errorLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ErrorLog.png")));
        getToolBar().add(errorLogButton);

        JButton exitButton = new JButton(exitAction);
        exitButton.setText("");
        exitButton.setToolTipText(JAMS.resources.getString("Exit"));
        exitButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/system-shutdown.png")));
        getToolBar().add(exitButton);


    }

    public void loadParams(File paramsFile) {
        try {
            ParameterProcessor.loadParams(getModelDocument(), paramsFile);
            loadModelDefinition(getModelDocument());
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(this, JAMS.resources.getString("File_") + paramsFile.getName() + JAMS.resources.getString("_could_not_be_loaded."), JAMS.resources.getString("File_Open_Error"));
        }
    }

    public void saveParams(File paramsFile) {
        try {
            ParameterProcessor.saveParams(getModelDocument(), paramsFile,
                    getProperties().getProperty(SystemProperties.USERNAME_IDENTIFIER), modelFilename);
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(this, JAMS.resources.getString("File_") + paramsFile.getName() + JAMS.resources.getString("_could_not_be_saved."), JAMS.resources.getString("File_saving_error"));
        }
    }

    protected void processInfoLog(String logText) {
        JAMSFrame.this.getInfoDlg().appendText(logText);
    }

    protected void processErrorLog(String logText) {
        JAMSFrame.this.getErrorDlg().appendText(logText);
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
            JAMSTools.handle(ioe);
        }

        super.exit();
        System.exit(0);
    }

    private boolean closeModel() {

        if (getModelDocument() == null) {
            return true;
        }

        // check for invalid parameter values
        if (!verifyInputs(false)) {
            int result = GUIHelper.showYesNoDlg(this, JAMS.resources.getString("Found_invalid_parameter_values_which_won't_be_saved._Proceed_anyway?"), JAMS.resources.getString("Invalid_parameter_values"));
            if (result == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        // update all properties
        updateProperties();

        if (getModelDocument() != null) {
            String modelDocString = XMLTools.getStringFromDocument(getModelDocument());
            if (!getInitialModelDocString().equals(modelDocString)) {
                int result = GUIHelper.showYesNoCancelDlg(this, JAMS.resources.getString("Save_modifications_in_") + modelFilename + JAMS.resources.getString("_?"), JAMS.resources.getString("JAMS_Launcher:_unsaved_modifications"));
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
            XMLTools.writeXmlFile(getModelDocument(), modelFilename);
            this.initialModelDocString = XMLTools.getStringFromDocument(this.modelDocument);
            fillAttributes(getModelDocument());
        } catch (IOException ioe) {
            GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.resources.getString("Error_saving_configuration_to_") + modelFilename, JAMS.resources.getString("Error"));
            return;
        }
    }

    @Override
    protected void fillAttributes(final Document doc) {

        // extract some model information
        Element root = doc.getDocumentElement();
        setTitle(BASE_TITLE + ": " + root.getAttribute("name") + " [" + modelFilename + "]");
        setHelpBaseUrl(root.getAttribute("helpbaseurl"));

    }

    private void openExplorer() {

        // check if provided values are valid
        if (!verifyInputs()) {
            return;
        }
        updateProperties();

        Document modelDoc = getModelDocument();

        NodeList varList = modelDoc.getDocumentElement().getElementsByTagName("var");
        Element varNode = (Element) varList.item(0);
        String workspacePath = varNode.getAttribute("value");

        try {
            File workspaceFile = new File(workspacePath);
            if (!workspaceFile.exists()) {
                GUIHelper.showErrorDlg(this, "\"" + workspaceFile + "\"" + JAMS.resources.getString("Invalid_Workspace"), JAMS.resources.getString("Error"));
                return;
            }
            JAMSExplorer explorer = new JAMSExplorer(null, false, false);
            explorer.getExplorerFrame().setVisible(true);
            explorer.getExplorerFrame().open(workspaceFile);
        } catch (NoClassDefFoundError ncdfe) {
            GUIHelper.showInfoDlg(this, JAMS.resources.getString("ExplorerDisabled"), JAMS.resources.getString("Info"));
            explorerAction.setEnabled(false);
        }

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
