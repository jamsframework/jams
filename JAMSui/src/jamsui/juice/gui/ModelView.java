/*
 * ModelView.java
 * Created on 12. Mai 2006, 08:25
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
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import jams.JAMS;
import jams.SystemProperties;
import jams.tools.JAMSTools;
import jams.gui.JAMSLauncher;
import jams.gui.tools.GUIHelper;
import jams.gui.WorkerDlg;
import jams.io.ParameterProcessor;
import jams.tools.XMLTools;
import jams.io.XMLProcessor;
import jams.meta.ModelDescriptor;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.StringTools;
import jams.workspace.InvalidWorkspaceException;
import jamsui.juice.*;
import jamsui.juice.gui.tree.ModelTree;
import java.awt.Desktop;
import java.net.URI;
import org.w3c.dom.Document;
import reg.JAMSExplorer;

/**
 *
 * @author S. Kralisch
 */
public class ModelView {

    private static final int TREE_PANE_WIDTH = 250;
    private JInternalFrame frame;
    private File savePath;
    private Document initialDoc;
    private ModelTree tree;
    private ComponentPanel compEditPanel;
    //private HashMap<ComponentDescriptor, DataRepository> dataRepositories = new HashMap<ComponentDescriptor, DataRepository>();
    private ModelGUIPanel launcherPanel;
    private ModelEditPanel modelEditPanel;
    private TreePanel modelTreePanel;
    private JDesktopPane parentPanel;
    private WorkerDlg loadModelDlg;
    private Runnable modelLoading;
    private static int viewCounter = 0;
    public static ViewList viewList = new ViewList();
    private JAMSRuntime runtime;
    private static JAMSExplorer theExplorer;
    private ModelDescriptor modelDescriptor = new ModelDescriptor();
    private OutputDSDlg outputDSDlg;
//    private PanelDlg launcherPanelDlg;

    public ModelView(JDesktopPane parentPanel) {
        this(getNextViewName(), parentPanel);
    }

    public ModelView(String title, JDesktopPane parentPanel) {

        this.parentPanel = parentPanel;
        modelEditPanel = new ModelEditPanel(this);
        compEditPanel = new ComponentPanel(this);
        launcherPanel = new ModelGUIPanel(this);

        modelTreePanel = new TreePanel();

        modelLoading = new Runnable() {

            @Override
            public void run() {
                try {
                    // create the runtime
                    runtime = new StandardRuntime();

                    // add info and error log output
                    runtime.addInfoLogObserver(new Observer() {

                        @Override
                        public void update(Observable obs, Object obj) {
                            JUICE.getJuiceFrame().getInfoDlg().appendText(obj.toString());
                        }
                    });
                    runtime.addErrorLogObserver(new Observer() {

                        @Override
                        public void update(Observable obs, Object obj) {
                            JUICE.getJuiceFrame().getErrorDlg().appendText(obj.toString());
                        }
                    });

                    // load the model
                    Document modelDoc = getModelDoc();
                    if (modelDoc != null) {
                        runtime.loadModel(modelDoc, JUICE.getJamsProperties());

                        // if workspace has not been provided, check if the document has been
                        // read from file and try to use parent directory instead
                        if (StringTools.isEmptyString(runtime.getModel().getWorkspacePath())
                                && (getSavePath() != null)) {
                            String dir = getSavePath().getParent();
                            runtime.getModel().setWorkspacePath(dir);
                            runtime.sendInfoMsg(JAMS.i18n("no_workspace_defined_use_loadpath") + dir);
                        }

                    } else {
                        runtime = null;
                    }

                } catch (Exception e) {
                    runtime.handle(e);
                }
            }
        };
        // create worker dialog for model setup
        loadModelDlg = new WorkerDlg(JUICE.getJuiceFrame(), JAMS.i18n("Model_Setup"));

        // create the internal frame
        frame = new JInternalFrame();

        frame.setClosable(true);
        frame.setIconifiable(true);
        frame.setMaximizable(true);
        frame.setResizable(true);
        frame.setTitle(title);
        frame.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/Context_si.png")));
        //frame.setVisible(true);
        frame.setBounds(0, 0, 600, 600);
        frame.addInternalFrameListener(new InternalFrameListener() {

            @Override
            public void internalFrameActivated(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent evt) {
                exit();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent evt) {
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        /*
         * create the toolbar
         */
//        JToolBar toolBar = new JToolBar();
        //toolBar.setPreferredSize(new Dimension(0, JAMS.TOOLBAR_HEIGHT));

//        modelRunButton = new JButton(JUICE.getJuiceFrame().getRunModelAction());
//        modelRunButton.setText("");
//        modelRunButton.setToolTipText(JAMS.i18n("Run_Model"));
//        modelRunButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
//        toolBar.add(modelRunButton);
//
//        modelGUIRunButton = new JButton(JUICE.getJuiceFrame().getRunModelFromLauncherAction());
//        modelGUIRunButton.setText("");
//        modelGUIRunButton.setToolTipText(JAMS.i18n("Run_model_from_JAMS_Launcher"));
//        modelGUIRunButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRunLauncher.png")));
//        toolBar.add(modelGUIRunButton);
//
//        JButton copyGUIButton = new JButton(JUICE.getJuiceFrame().getCopyModelGUIAction());
//        copyGUIButton.setText("");
//        copyGUIButton.setToolTipText(JAMS.i18n("Copy_Model_GUI"));
//        copyGUIButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Copy.png")));
//        toolBar.add(copyGUIButton);
//
//        JButton pasteGUIButton = new JButton(JUICE.getJuiceFrame().getPasteModelGUIAction());
//        pasteGUIButton.setText("");
//        pasteGUIButton.setToolTipText(JAMS.i18n("Paste_Model_GUI"));
//        pasteGUIButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Paste.png")));
//        toolBar.add(pasteGUIButton);

        /*
         * create the splitpane
         */
        JSplitPane modelSplitPane = new JSplitPane();
        modelSplitPane.setAutoscrolls(true);
        modelSplitPane.setContinuousLayout(true);

        JTabbedPane tabPane = new JTabbedPane();
        //tabPane.addTab("Model configuration", new JScrollPane(modelEditPanel));
        tabPane.addTab(JAMS.i18n("Component_configuration"), new JScrollPane(compEditPanel));
        tabPane.addTab(JAMS.i18n("GUI_Builder"), new JScrollPane(launcherPanel));

        modelSplitPane.setLeftComponent(modelTreePanel);
        modelSplitPane.setRightComponent(tabPane);
        modelSplitPane.setDividerLocation(TREE_PANE_WIDTH);
        //modelSplitPane.setOneTouchExpandable(true);
        //modelSplitPane.setDividerSize(DIVIDER_WIDTH);


        /*
         * add everything to the frame
         */
//        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(modelSplitPane, BorderLayout.CENTER);

        ModelView.viewList.addView(frame, this);

        // check if there already was a view opened
        ModelView currentView = JUICE.getJuiceFrame().getCurrentView();

        // add the current frame to the JDesktopPane
        parentPanel.add(frame, JLayeredPane.DEFAULT_LAYER);

        try {
            if (currentView == null) {
                frame.setMaximum(true);
            }
        } catch (PropertyVetoException pve) {
            JAMSTools.handle(pve);
        }
    }

    /**
     * This method will create a JAMSLauncher window with the current model 
     * loaded
     */
    public void runModelFromLauncher() {
        launcherPanel.updateProperties();
        JAMSLauncher launcher = new JAMSLauncher(JUICE.getJuiceFrame(), JUICE.getJamsProperties(), getModelDoc(), getSavePath());
        launcher.setVisible(true);
    }

    public void runModel() {

        // first check if provided parameter values are valid
        /*if (!launcherPanel.verifyInputs()) {
        return;
        }*/

        // then load the model via the modelLoading runnable
        loadModelDlg.setTask(modelLoading);
        loadModelDlg.execute();

        // check if runtime has been created successfully
        if (runtime == null) {
            return;
        }

        // then execute it
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    // start the model
                    runtime.runModel();
                } catch (Exception ex) {
                    runtime.handle(ex);
                }

                JUICE.getJuiceFrame().getInfoDlg().appendText("\n\n");
                JUICE.getJuiceFrame().getErrorDlg().appendText("\n\n");

                //dump the runtime and clean up
                runtime = null;
                Runtime.getRuntime().gc();
            }
        };
        try {
            t.start();
        } catch (Exception e) {
            runtime.handle(e);
        }
    }

    public static String getNextViewName() {
        viewCounter++;
        return JAMS.i18n("Model") + viewCounter;
    }

    public boolean save() {

        boolean result = false;

        Document doc = getModelDoc();

        try {
            result = XMLTools.writeXmlFile(doc, savePath);
        } catch (IOException ioe) {
            return false;
        }

        return result;
    }

    public ModelTree getTree() {
        return tree;
    }

    public void setTree(ModelTree tree) {
        this.tree = tree;
        modelTreePanel.setTree(tree);
        updateLauncherPanel();
    }

    public void updateLauncherPanel() {
        this.launcherPanel.updatePanel();
    }

    public TreePanel getModelTreePanel() {
        return modelTreePanel;
    }

    public void setModelTreePanel(TreePanel modelTreePanel) {
        this.modelTreePanel = modelTreePanel;
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File savePath) {

        if (savePath != null) {
            if (!(savePath.getAbsolutePath().endsWith(".jam") || savePath.getAbsolutePath().endsWith(".xml"))) {
                savePath = new File(savePath.getAbsolutePath() + ".jam");
            }
            frame.setTitle(savePath.getAbsolutePath());
        }
        this.savePath = savePath;
    }

    public void loadParams(File paramsFile) {
        try {
            Document doc = ParameterProcessor.loadParams(getModelDoc(), paramsFile);
            modelDescriptor = new ModelDescriptor();
            this.setTree(new ModelTree(this, doc));
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("File_") + paramsFile.getName() + JAMS.i18n("_could_not_be_loaded."), JAMS.i18n("File_open_error"));
        }
    }

    public void saveParams(File paramsFile) {
        try {
            String path = null;
            if (getSavePath() != null) {
                path = getSavePath().getAbsolutePath();
            }
            ParameterProcessor.saveParams(getModelDoc(), paramsFile,
                    JUICE.getJamsProperties().getProperty(SystemProperties.USERNAME_IDENTIFIER), path);
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("File_") + paramsFile.getName() + JAMS.i18n("_could_not_be_saved."), JAMS.i18n("File_saving_error"));
        }
    }

    public boolean exit() {

        boolean returnValue = false;

        String newXMLString = XMLTools.getStringFromDocument(getModelDoc());
        String oldXMLString = XMLTools.getStringFromDocument(initialDoc);

        if (newXMLString.compareTo(oldXMLString) != 0) {
            int result = GUIHelper.showYesNoCancelDlg(JUICE.getJuiceFrame(), JAMS.i18n("Save_modifications_in_") + this.getFrame().getTitle() + JAMS.i18n("_?"), JAMS.i18n("Unsaved_modifications"));
            if (result == JOptionPane.OK_OPTION) {
                JUICE.getJuiceFrame().saveModel(this);
                closeView();
                returnValue = true;
            } else if (result == JOptionPane.NO_OPTION) {
                closeView();
                returnValue = true;
            }
        } else {
            closeView();
            returnValue = true;
        }
        return returnValue;
    }

    private void closeView() {
        boolean maximized = this.getFrame().isMaximum();

        ModelView.viewList.removeView(this.getFrame());
        parentPanel.remove(this.getFrame());
        this.getFrame().dispose();
        parentPanel.updateUI();

        if (maximized) {
            try {
                ModelView currentView = JUICE.getJuiceFrame().getCurrentView();
                if (currentView != null) {
                    currentView.getFrame().setMaximum(true);
                }
            } catch (PropertyVetoException pve) {
                JAMSTools.handle(pve);
            }
        }
    }

    /**
     * Return an XML document describing the model.
     * @return The XML document describing the model.
     */
    public Document getModelDoc() {
        if (tree == null) {
            return null;
        }

        launcherPanel.updateProperties();
        return tree.getModelDocument(getModelDescriptor());
    }

    /**
     * Loads a JAMS model from file
     * @param fileName The file containing the models XML document.
     */
    public void loadModel(String fileName) {
        try {
            // first do search&replace on the input xml file
            String newModelFilename = XMLProcessor.modelDocConverter(fileName);
            if (!newModelFilename.equalsIgnoreCase(fileName)) {
                GUIHelper.showInfoDlg(JUICE.getJuiceFrame(),
                        JAMS.i18n("The_model_definition_in_") + fileName + JAMS.i18n("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.i18n("_while_your_original_file_was_left_untouched."), JAMS.i18n("Info"));
            }
            fileName = newModelFilename;

            this.setSavePath(new File(fileName));
            this.setTree(new ModelTree(this, XMLTools.getDocument(fileName)));

            this.setInitialState();

        } catch (FileNotFoundException fnfe) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("File_") + fileName + JAMS.i18n("_could_not_be_loaded."), JAMS.i18n("File_open_error"));
        } catch (Exception e) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.i18n("Unknown_error_during_Model_loading"), JAMS.i18n("Model_loading_error"));
            e.printStackTrace();
        }
    }

    public JInternalFrame getFrame() {
        return frame;
    }

    public ComponentPanel getCompEditPanel() {
        return compEditPanel;
    }

    public ModelEditPanel getModelEditPanel() {
        return modelEditPanel;
    }

    public void setCompEditPanel(ComponentPanel compEditPanel) {
        this.compEditPanel = compEditPanel;
    }

    /*
    public DataRepository getDataRepository(ComponentDescriptor context) {
    DataRepository repo = dataRepositories.get(context);
    if (repo == null) {
    repo = new DataRepository(context);
    dataRepositories.put(context, repo);
    }
    return repo;
    }
    
    public HashMap<ComponentDescriptor, DataRepository> getDataRepositories() {
    return dataRepositories;
    }
     */
    public void setInitialState() {
        this.initialDoc = tree.getModelDocument(getModelDescriptor());
    }

    public ModelDescriptor getModelDescriptor() {
        return modelDescriptor;
    }

    public void setModelDescriptor(ModelDescriptor md) {
        this.modelDescriptor = md;
    }

    public void openExplorer() {

        File workspaceFile = new File(getModelDescriptor().getWorkspacePath());

        if (!workspaceFile.isDirectory()) {
            if (getSavePath() != null) {
                workspaceFile = getSavePath().getParentFile();
            } else {
                GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
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
            GUIHelper.showInfoDlg(JUICE.getJuiceFrame(), jams.JAMS.i18n("ExplorerDisabled"), jams.JAMS.i18n("Info"));
        } catch (InvalidWorkspaceException ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), "\"" + workspaceFile + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
        }
    }

    public void openWSBrowser() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        File workspacePath = new File(XMLProcessor.getWorkspacePath(getModelDoc()));
        if (!workspacePath.isDirectory()) {
            if (savePath != null) {
                workspacePath = savePath.getParentFile();
            } else {
                return;
            }
        }

        // try to open file browser in workspace dir
        try {
            URI workspaceURI = workspacePath.toURI();
            Desktop.getDesktop().browse(workspaceURI);
        } catch (IOException ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), "\"" + workspacePath + "\"" + JAMS.i18n("Invalid_Workspace"), JAMS.i18n("Error"));
        }

    }

    /**
     * @return the outputDSDlg
     */
    public OutputDSDlg getOutputDSDlg() {
        if (outputDSDlg == null) {
            outputDSDlg = new OutputDSDlg(JUICE.getJuiceFrame(), this.getModelDescriptor());
        }
        return outputDSDlg;
    }
}
