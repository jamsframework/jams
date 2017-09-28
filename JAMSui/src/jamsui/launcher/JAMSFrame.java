/*
 * JAMSFrame.java
 * Created on 27. August 2006, 21:55
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.launcher;

import jams.gui.*;
import jams.JAMS;
import jams.SystemProperties;
import jams.tools.JAMSTools;
import jams.gui.tools.GUIHelper;
import jams.JAMSFileFilter;
import jams.JAMSLogging;
import jams.JAMSLogging.LogOption;
import jams.io.ParameterProcessor;
import jams.tools.XMLTools;
import jams.io.XMLProcessor;
import jams.model.JAMSFullModelState;
import jams.model.Model;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jams.workspace.InvalidWorkspaceException;
import jamsui.juice.JUICE;
import java.awt.Desktop;
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
import java.net.URI;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import jams.explorer.JAMSExplorer;
import jams.explorer.ensembles.gui.EnsembleControlPanel;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSFrame extends JAMSLauncher {

    private JMenuBar mainMenu;
    private JMenu logsMenu, modelMenu, recentMenu;
    private JMenuItem saveItem, saveAsItem, startEnsembleManagerItem;
    private JFileChooser jfcProps, jfcSer, jfcModel, jfcParam;
    private JDialog rtManagerDlg;
    private PropertyDlg propertyDlg;
    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, JAMS.i18n("Info_Log"));
    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, JAMS.i18n("Error_Log"));
    private String modelFilename;
    private Action editPrefsAction, loadPrefsAction, savePrefsAction,
            loadModelAction, saveModelAction, saveAsModelAction, exitAction,
            aboutAction, loadModelParamAction, saveModelParamAction,
            loadModelExecutionStateAction, rtManagerAction, infoLogAction,
            errorLogAction, onlineAction, explorerAction, browserAction,
            editModelAction, startEnsembleManagerAction;
    private static JAMSExplorer theExplorer;

    public JAMSFrame(Frame parent, SystemProperties properties) {
        super(parent, properties);

//        MsgBoxLogHandler.getInstance().setParent(this);
//        NotificationLogHandler.getInstance().setParent(this);

        boolean os64 = JAMSTools.is64Bit();
        boolean vm64 = System.getProperty("os.arch").endsWith("64");

        if (os64 != vm64) {
            String osArch = (os64 ? "64 bit" : "32 bit");
            String vmArch = (vm64 ? "64 bit" : "32 bit");
            int result = GUIHelper.showYesNoDlg(null, String.format("Architectures of OS (%s) and Java VM (%s) used with JAMS seem to differ.\n"
                    + "You should update your Java VM to a matching version to avoid strange model behaviour.\n"
                    + "Continue anyway?", osArch, vmArch), "Warning");
            if (result == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }

        // take care of loggers
        JAMSLogging.getInstance().addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                List loggers = JAMSLogging.getLoggers();
                Logger logger = (Logger) arg;
                LogOption option = JAMSLogging.getLogOption(logger);
                if (loggers.contains(logger)) {
                    JAMSui.registerLogger(option, logger);
                } else {
                    JAMSui.unregisterLogger(option, logger);
                }
            }
        });
    }

    public JAMSFrame(Frame parent, SystemProperties properties, String modelFilename, String cmdLineArgs, Properties jmpParameters) {
        //super(properties, modelFilename, cmdLineArgs);
        this(parent, properties);

        loadModelDefinition(modelFilename, StringTools.toArray(cmdLineArgs, ";"), jmpParameters);
        loadPath = new File(modelFilename);
    }

    protected void loadModelDefinition(String fileName, String[] args, Properties jmpParameters) {

        // first close any already opened models
        if (!closeModel()) {
            return;
        }

        try {

            //check if file exists
            File file = new File(fileName);
            if (!file.exists()) {
                GUIHelper.showErrorDlg(this, JAMS.i18n("Model_file_") + fileName + JAMS.i18n("_could_not_be_found!"), JAMS.i18n("File_Open_Error"));
                return;
            } else {
                loadPath = file;
            }

            // first do search&replace on the input xml file
            String newFilename = XMLProcessor.modelDocConverter(fileName);
            if (!newFilename.equalsIgnoreCase(fileName)) {
                fileName = newFilename;
                GUIHelper.showInfoDlg(JAMSFrame.this,
                        JAMS.i18n("The_model_definition_in_") + fileName + JAMS.i18n("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newFilename + JAMS.i18n("_while_your_original_file_was_left_untouched."), JAMS.i18n("Info"));
            }

            // create string from input model definition file and replace "%x" occurences by cmd line data
            String xmlString = FileTools.fileToString(fileName);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%" + i + "%", args[i]);
                }
            }

            // finally, create the model document from the string
            this.modelDocument = XMLTools.getDocumentFromString(xmlString);

            if (jmpParameters != null) {
                modelDocument = ParameterProcessor.loadParams(modelDocument, jmpParameters);
            }

            ParameterProcessor.preProcess(modelDocument);
            this.initialModelDocString = XMLTools.getStringFromDocument(this.modelDocument);

        } catch (IOException ioe) {
            GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("The_specified_model_configuration_file_") + fileName + JAMS.i18n("_could_not_be_found!"), JAMS.i18n("Error"));
        } catch (SAXException se) {
            if (se instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException) se;
                GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("The_specified_model_configuration_file_") + fileName + JAMS.i18n("_contains_errors!") + "\n[Fatal Error] :" + spe.getLineNumber() + ":" + spe.getColumnNumber() + ":" + spe.getMessage(), JAMS.i18n("Error"));
            } else {
                GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("The_specified_model_configuration_file_") + fileName + JAMS.i18n("_contains_errors!"), JAMS.i18n("Error"));
            }
        }

        this.modelFilename = fileName;

        fillAttributes(this.getModelDocument());
        fillTabbedPane(this.getModelDocument());

        saveModelAction.setEnabled(true);
        saveAsModelAction.setEnabled(true);
        modelMenu.setEnabled(true);
        editModelAction.setEnabled(true);
        explorerAction.setEnabled(true);
        getRunModelAction().setEnabled(true);

        JAMSTools.addToRecentFiles(getProperties(), SystemProperties.RECENT_FILES, fileName);
        updateRecentMenu();

        //GUIHelper.showInfoDlg(JAMSLauncher.this, "Model has been successfully loaded!", "Info");
    }
    
    @Override
    protected void init() throws HeadlessException, DOMException, NumberFormatException {

        super.init();

        getRunModelAction().setEnabled(false);

        // define some actions
        editPrefsAction = new AbstractAction(JAMS.i18n("Edit_Preferences...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };

        loadPrefsAction = new AbstractAction(JAMS.i18n("Load_Preferences...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                jfcProps.setSelectedFile(new File(""));
                //jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfcProps.showOpenDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfcProps.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().load(stringValue);
                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        savePrefsAction = new AbstractAction(JAMS.i18n("Save_Preferences...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                jfcProps.setSelectedFile(new File(""));
                //jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfcProps.showSaveDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfcProps.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().save(stringValue);
                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        loadModelExecutionStateAction = new AbstractAction(JAMS.i18n("Resume_Model_Execution")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jfcSer == null) {
                    jfcSer = new JFileChooser();
                }
                jfcSer.setSelectedFile(new File(""));
                int result = jfcSer.showOpenDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        state = new JAMSFullModelState(jfcSer.getSelectedFile());
                        Model model = state.getModel();
                        Document doc = model.getRuntime().getModelDocument();
                        loadModelDefinition(doc);
                        modelFilename = model.getWorkspacePath() + "/" + model.getName();
                        saveModelAction.setEnabled(true);
                        explorerAction.setEnabled(true);
                        saveAsModelAction.setEnabled(true);
                        modelMenu.setEnabled(true);
                        editModelAction.setEnabled(true);
                        getRunModelAction().setEnabled(true);
                    } catch (IOException ioe) {
                        GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("Could_not_resume_model_execution_because") + ioe, JAMS.i18n("Resume_error"));
                    } catch (ClassNotFoundException cnfe) {
                        GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("Could_not_resume_model_execution_because") + cnfe, JAMS.i18n("Resume_error"));
                    }
                }
            }
        };

        loadModelAction = new AbstractAction(JAMS.i18n("Open_Model...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!StringTools.isEmptyString(modelFilename)) {
                    jfcModel.setSelectedFile(new File(modelFilename));
                } else {
                    jfcModel.setSelectedFile(new File(""));
                }
                if (jfcModel.showOpenDialog(JAMSFrame.this) == JFileChooser.APPROVE_OPTION) {
                    state = null;
                    loadModelDefinition(jfcModel.getSelectedFile().getAbsolutePath(), null, null);

                }
            }
        };

        saveModelAction = new AbstractAction(JAMS.i18n("Save_Model")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveModel();
            }
        };
        saveModelAction.setEnabled(false);

        saveAsModelAction = new AbstractAction(JAMS.i18n("Save_Model_As...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!StringTools.isEmptyString(modelFilename)) {
                    jfcModel.setSelectedFile(new File(modelFilename));
                } else {
                    jfcModel.setSelectedFile(new File(""));
                }
                if (jfcModel.showSaveDialog(JAMSFrame.this) == JFileChooser.APPROVE_OPTION) {
                    modelFilename = jfcModel.getSelectedFile().getAbsolutePath();
                    saveModel();
                }
            }
        };
        saveAsModelAction.setEnabled(false);

        startEnsembleManagerAction = new AbstractAction(JAMS.i18n("Start_Ensemble_Manager")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEnsembleManager();
            }
        };

        editModelAction = new AbstractAction(JAMS.i18n("Edit_Model...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!closeModel()) {
                    return;
                }
                JAMSFrame.this.dispose();
                if (JUICE.getJuiceFrame() == null) {
                    JUICE.createJUICEFrame(getProperties());
                }
                if (JAMSFrame.this.modelFilename != null) {
                    JUICE.getJuiceFrame().loadModel(JAMSFrame.this.modelFilename);
                }
            }
        };
        editModelAction.setEnabled(true);

        exitAction = new AbstractAction(JAMS.i18n("Exit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        aboutAction = new AbstractAction(JAMS.i18n("About")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDlg(JAMSFrame.this).setVisible(true);
            }
        };

        onlineAction = new AbstractAction(JAMS.i18n("JAMS_online...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIHelper.openURL(JAMS.i18n("JAMS_URL"));
            }
        };

        loadModelParamAction = new AbstractAction(JAMS.i18n("Load_Model_Parameter...")) {
            @Override
            public void actionPerformed(ActionEvent e) {

                jfcParam.setSelectedFile(new File(""));
                int result = jfcParam.showOpenDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfcParam.getSelectedFile().getAbsolutePath();
                    loadParams(new File(path));
                }
            }
        };

        saveModelParamAction = new AbstractAction(JAMS.i18n("Save_Model_Parameter...")) {
            @Override
            public void actionPerformed(ActionEvent e) {

                jfcParam.setSelectedFile(new File(""));
                int result = jfcParam.showSaveDialog(JAMSFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfcParam.getSelectedFile().getAbsolutePath();
                    saveParams(new File(path));
                }
            }
        };

        rtManagerAction = new AbstractAction(JAMS.i18n("Show_Runtime_Manager...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                rtManagerDlg.setVisible(true);
            }
        };

        infoLogAction = new AbstractAction(JAMS.i18n("Info_Log...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getInfoDlg().setVisible(true);
            }
        };

        errorLogAction = new AbstractAction(JAMS.i18n("Error_Log...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getErrorDlg().setVisible(true);
            }
        };

        explorerAction = new AbstractAction(JAMS.i18n("DATA_EXPLORER")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openExplorer();
            }
        };

        browserAction = new AbstractAction(JAMS.i18n("Browse_WS_Dir")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseWSDir();
            }
        };

        // create additional dialogs
        this.propertyDlg = new PropertyDlg(this, getProperties());

        jfcProps = GUIHelper.getJFileChooser(JAMSFileFilter.getPropertyFilter());
        jfcProps.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfcProps.setCurrentDirectory(JAMS.getBaseDir());

        jfcModel = GUIHelper.getJFileChooser(JAMSFileFilter.getModelFilter());
        jfcModel.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfcModel.setCurrentDirectory(JAMS.getBaseDir());

        jfcParam = GUIHelper.getJFileChooser(JAMSFileFilter.getParameterFilter());
        jfcParam.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfcParam.setCurrentDirectory(JAMS.getBaseDir());

        // runtime manager dlg
        rtManagerDlg = new JDialog(this, JAMS.i18n("Runtime_Manager"));
        rtManagerDlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        rtManagerDlg.setLocationByPlatform(true);
        RuntimeManagerPanel rtManagerPanel = new RuntimeManagerPanel();
        rtManagerDlg.getContentPane().add(rtManagerPanel);
        rtManagerDlg.pack();

        JButton closeButton = new JButton(JAMS.i18n("Close"));
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
        JMenu fileMenu = new JMenu(JAMS.i18n("File"));

        JMenuItem loadItem = new JMenuItem(loadModelAction);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add(loadItem);

        recentMenu = new JMenu(JAMS.i18n("Recent_Files"));
        updateRecentMenu();
        fileMenu.add(recentMenu);

        saveItem = new JMenuItem(saveModelAction);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveItem);

        saveAsItem = new JMenuItem(saveAsModelAction);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        fileMenu.add(saveAsItem);

        if (isEnsembleManagerEnabled) {
            startEnsembleManagerItem = new JMenuItem(startEnsembleManagerAction);
            startEnsembleManagerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
            fileMenu.add(new JSeparator());
            fileMenu.add(startEnsembleManagerItem);
            fileMenu.add(new JSeparator());
        }

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);
        getMainMenu().add(fileMenu);

        // extras menu
        JMenu editMenu = new JMenu(JAMS.i18n("Edit"));

        JMenuItem editOptionsItem = new JMenuItem(editPrefsAction);
        editOptionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        editMenu.add(editOptionsItem);

        JMenuItem loadOptionsItem = new JMenuItem(loadPrefsAction);
        editMenu.add(loadOptionsItem);

        JMenuItem saveOptionsItem = new JMenuItem(savePrefsAction);
        editMenu.add(saveOptionsItem);

        getMainMenu().add(editMenu);

        // model menu
        modelMenu = new JMenu(JAMS.i18n("Model"));
        modelMenu.setEnabled(false);
        mainMenu.add(modelMenu);

        JMenuItem runModelItem = new JMenuItem(getRunModelAction());
        runModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(runModelItem);

        JMenuItem explorerItem = new JMenuItem(explorerAction);
        explorerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        modelMenu.add(explorerItem);

        JMenuItem fileBrowserItem = new JMenuItem(browserAction);
        fileBrowserItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        modelMenu.add(fileBrowserItem);

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

        JMenuItem loadModelExecutionStateItem = new JMenuItem(loadModelExecutionStateAction);
        modelMenu.add(loadModelExecutionStateItem);

        // logs menu
        logsMenu = new JMenu(JAMS.i18n("Logs"));

        JMenuItem infoLogItem = new JMenuItem(infoLogAction);
        getLogsMenu().add(infoLogItem);

        JMenuItem errorLogItem = new JMenuItem(errorLogAction);
        getLogsMenu().add(errorLogItem);
        getMainMenu().add(getLogsMenu());

        // help menu
        JMenu helpMenu = new JMenu(JAMS.i18n("Help"));

        JMenuItem onlineItem = new JMenuItem(onlineAction);
        helpMenu.add(onlineItem);

        JMenuItem aboutItem = new JMenuItem(aboutAction);
        helpMenu.add(aboutItem);

        getMainMenu().add(helpMenu);

        setJMenuBar(getMainMenu());
        
        getToolBar().remove(1);

        JButton loadButton = new JButton(loadModelAction);
        loadButton.setText("");
        loadButton.setToolTipText(JAMS.i18n("Open_Model..."));
        loadButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelOpen.png")));
        getToolBar().add(loadButton, 0);

        JButton saveButton = new JButton(saveModelAction);
        saveButton.setText("");
        saveButton.setToolTipText(JAMS.i18n("Save_Model"));
        saveButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelSave.png")));
        getToolBar().add(saveButton, 1);

        JButton prefsButton = new JButton(editPrefsAction);
        prefsButton.setText("");
        prefsButton.setToolTipText(JAMS.i18n("Edit_Preferences..."));
        prefsButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Preferences.png")));
        getToolBar().add(prefsButton, 2);

        getToolBar().add(new JToolBar.Separator(null), 3);

        JButton explorerButton = new JButton(explorerAction);
        explorerButton.setText("");
        explorerButton.setToolTipText(JAMS.i18n("JADE"));
        explorerButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Layers_small.png")));
        explorerAction.setEnabled(false);
        getToolBar().add(explorerButton);

        JButton juiceButton = new JButton(editModelAction);
        juiceButton.setText("");
        juiceButton.setToolTipText(JAMS.i18n("Edit_Model..."));
        juiceButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/JUICEicon32.png")));
        getToolBar().add(juiceButton);

        getToolBar().addSeparator();

        JButton infoLogButton = new JButton(infoLogAction);
        infoLogButton.setText("");
        infoLogButton.setToolTipText(JAMS.i18n("Show_Info_Log..."));
        infoLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/InfoLog.png")));
        getToolBar().add(infoLogButton);

        JButton errorLogButton = new JButton(errorLogAction);
        errorLogButton.setText("");
        errorLogButton.setToolTipText(JAMS.i18n("Show_Error_Log..."));
        errorLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ErrorLog.png")));
        getToolBar().add(errorLogButton);

        JButton exitButton = new JButton(exitAction);
        exitButton.setText("");
        exitButton.setToolTipText(JAMS.i18n("Exit"));
        exitButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/system-shutdown.png")));
        getToolBar().add(exitButton);

    }

    public void loadParams(File paramsFile) {
        try {
            ParameterProcessor.loadParams(getModelDocument(), paramsFile);
            loadModelDefinition(getModelDocument());
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(this, JAMS.i18n("File_") + paramsFile.getName() + JAMS.i18n("_could_not_be_loaded."), JAMS.i18n("File_Open_Error"));
        }
    }

    public void saveParams(File paramsFile) {
        try {
            ParameterProcessor.saveParams(getModelDocument(), paramsFile,
                    getProperties().getProperty(SystemProperties.USERNAME_IDENTIFIER), modelFilename);
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(this, JAMS.i18n("File_") + paramsFile.getName() + JAMS.i18n("_could_not_be_saved."), JAMS.i18n("File_saving_error"));
        }
    }

    @Override
    protected void processInfoLog(String logText) {
        JAMSFrame.this.getInfoDlg().appendText(logText);
    }

    @Override
    protected void processErrorLog(String logText) {
        JAMSFrame.this.getErrorDlg().appendText(logText);
    }

    @Override
    protected void exit() {
        //close the current model
        if (!closeModel()) {
            return;
        }

        // finally write property file to default location
        try {
            getProperties().save();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }

        super.exit();
        System.exit(0);
    }

    private boolean closeModel() {
        this.state = null;
        if (getModelDocument() == null) {
            return true;
        }

        // check for invalid parameter values
        if (!verifyInputs(false)) {
            int result = GUIHelper.showYesNoDlg(this, JAMS.i18n("Found_invalid_parameter_values_which_won't_be_saved._Proceed_anyway?"), JAMS.i18n("Invalid_parameter_values"));
            if (result == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        // update all properties
        updateProperties();

        if (getModelDocument() != null) {
            String modelDocString = XMLTools.getStringFromDocument(getModelDocument());
            if (!getInitialModelDocString().equals(modelDocString)) {
                int result = GUIHelper.showYesNoCancelDlg(this, JAMS.i18n("Save_modifications_in_") + modelFilename + JAMS.i18n("_?"), JAMS.i18n("JAMS_Launcher:_unsaved_modifications"));
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
            GUIHelper.showErrorDlg(JAMSFrame.this, JAMS.i18n("Error_saving_configuration_to_") + modelFilename, JAMS.i18n("Error"));
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

    private void browseWSDir() {

        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Document modelDoc = getModelDocument();

        File workspaceFile = new File(XMLProcessor.getWorkspacePath(modelDoc));
        if (!workspaceFile.isDirectory()) {
            if (loadPath != null) {
                workspaceFile = loadPath.getParentFile();
            } else {
                GUIHelper.showErrorDlg(this, "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
                return;
            }
        }

        // try to open file browser in workspace dir
        try {
            URI workspaceURI = workspaceFile.toURI();
            Desktop.getDesktop().browse(workspaceURI);
        } catch (IOException ex) {
            GUIHelper.showErrorDlg(JAMSFrame.this, "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
        }
    }

    private void openEnsembleManager() {
        SwingWorker worker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                EnsembleControlPanel ecp = new EnsembleControlPanel(JAMSFrame.this);
                JFrame frame = new JFrame("Ensemble Manager");

                //setCurrentDirectory("C:/Arbeit/Projekte/J2000Klima/JAMS/data/Ensembe Hasel");
                frame.add(ecp);
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                return null;
            }
        };
        worker.run();

    }

    private void openExplorer() {

        // check if provided values are valid
        if (!verifyInputs()) {
            return;
        }
        updateProperties();

        Document modelDoc = getModelDocument();

        File workspaceFile = new File(XMLProcessor.getWorkspacePath(modelDoc));
        if (!workspaceFile.isDirectory()) {
            if (loadPath != null) {
                workspaceFile = loadPath.getParentFile();
            } else {
                GUIHelper.showErrorDlg(this, "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
                return;
            }
        }

        try {

            if (theExplorer == null) {
                theExplorer = new JAMSExplorer(null, false);
            }

            if ((theExplorer.getWorkspace() == null) || (!theExplorer.getWorkspace().getDirectory().equals(workspaceFile))) {
                theExplorer.getExplorerFrame().open(workspaceFile);
            }

            theExplorer.getExplorerFrame().setVisible(true);

        } catch (NoClassDefFoundError ncdfe) {
            GUIHelper.showInfoDlg(this, JAMS.i18n("ExplorerDisabled"), JAMS.i18n("Info"));
        } catch (InvalidWorkspaceException ex) {
            GUIHelper.showErrorDlg(this, "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
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

    private void updateRecentMenu() {
        recentMenu.removeAll();
        String[] recentFiles = JAMSTools.getRecentFiles(getProperties(), SystemProperties.RECENT_FILES);
        for (String fileName : recentFiles) {
            Action openAction = new AbstractAction(fileName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadModelDefinition(this.getValue(Action.NAME).toString(), null, null);
                }
            };
            JMenuItem recentItem = new JMenuItem(openAction);
            recentMenu.add(recentItem);
        }
    }
}
