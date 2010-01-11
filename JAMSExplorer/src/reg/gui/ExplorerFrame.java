/*
 * ExplorerFrame.java
 * Created on 18. November 2008, 21:40
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
package reg.gui;

import jams.tools.JAMSTools;
import jams.gui.tools.GUIHelper;
import jams.gui.PropertyDlg;
import jams.gui.WorkerDlg;
import jams.gui.WorkspaceDlg;
import jams.tools.XMLIO;
import jams.gui.JAMSLauncher;
import jams.io.ParameterProcessor;
import jams.io.XMLProcessor;
import jams.workspace.InvalidWorkspaceException;
import jams.workspace.JAMSWorkspace;
import jams.workspace.stores.ShapeFileDataStore;
import reg.JAMSExplorer;
import java.io.FileNotFoundException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.w3c.dom.Document;
import reg.spreadsheet.JAMSSpreadSheet;
import reg.spreadsheet.STPConfigurator;
import reg.viewer.Viewer;
import reg.wizard.tlug.ExplorerWizard;
import reg.wizard.tlug.panels.BaseDataPanel;
import reg.wizard.tlug.panels.DataDecisionPanel;
import reg.wizard.tlug.panels.StationParamsPanel;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ExplorerFrame extends JFrame {

    private static final int INOUT_PANE_WIDTH = 250, INOUT_PANE_HEIGHT = 450;

    private static final int DIVIDER_WIDTH = 6;

    private JFileChooser jfc = GUIHelper.getJFileChooser();

    private WorkerDlg openWSDlg;

    private Action openWSAction, openSTPAction, exitAction, editWSAction,
            sensitivityAnalysisAction, launchModelAction, editPrefsAction,
            reloadWSAction, launchWizardAction;

    private JLabel statusLabel;

    private JSplitPane mainSplitPane;

    private JTabbedPane tPane;

    private JAMSExplorer explorer;

    private PropertyDlg propertyDlg;

    private WorkspaceDlg wsDlg;

    private Document modelDoc = null;

    private MCAT5Toolbar mcat5ToolBar = null;

    public ExplorerFrame(JAMSExplorer explorer) {
        this.explorer = explorer;
        mcat5ToolBar = new MCAT5Toolbar(this);
        init();
    }

    private void init() {

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        wsDlg = new WorkspaceDlg(this);

        createListener();

        exitAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CLOSE")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        openWSAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OPEN_WORKSPACE...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        };

        editWSAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("EDIT_WORKSPACE...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                wsDlg.setVisible(explorer.getWorkspace());
            }
        };

        sensitivityAnalysisAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MCAT5...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mcat5ToolBar.setVisible(!mcat5ToolBar.isVisible());
            }
        };

        launchModelAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("START_MODEL")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                launchModel();
            }
        };

        launchWizardAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("START_WIZARD...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                launchWizard();
                launchModel();
            }
        };


        editPrefsAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("EDIT_PREFERENCES...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(explorer.getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };


        openSTPAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("STACKED_TIME_PLOT")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                STPConfigurator stp = new STPConfigurator(explorer);
            }
        };


        reloadWSAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("RELOAD_WORKSPACE")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        };

        update();

        propertyDlg = new PropertyDlg(this, explorer.getProperties());

        openWSDlg = new WorkerDlg(this, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OPENING_WORKSPACE"));

        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        setTitle(JAMSExplorer.APP_TITLE);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setSelectedFile(new File(System.getProperty("user.dir")));

        tPane = new JTabbedPane();

        mainSplitPane = new JSplitPane();
        JSplitPane inoutSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(inoutSplitPane);
        mainSplitPane.setRightComponent(tPane);
        mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        mainSplitPane.setDividerSize(DIVIDER_WIDTH);
        mainSplitPane.setOneTouchExpandable(false);

        inoutSplitPane.setAutoscrolls(true);
        inoutSplitPane.setContinuousLayout(true);
        inoutSplitPane.setLeftComponent(explorer.getDisplayManager().getTreePanel());
        inoutSplitPane.setRightComponent(explorer.getDisplayManager().getInputDSInfoPanel());
        inoutSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        inoutSplitPane.setDividerLocation(INOUT_PANE_HEIGHT);
        inoutSplitPane.setDividerSize(DIVIDER_WIDTH);
        inoutSplitPane.setOneTouchExpandable(false);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();

        JButton wsOpenButton = new JButton(openWSAction);
        wsOpenButton.setText("");
        wsOpenButton.setToolTipText((String) openWSAction.getValue(Action.NAME));
        wsOpenButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelOpen.png")));
        toolBar.add(wsOpenButton);

        JButton reloadWSButton = new JButton(reloadWSAction);
        reloadWSButton.setText("");
        reloadWSButton.setToolTipText((String) reloadWSAction.getValue(Action.NAME));
        reloadWSButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Reload.png")));
        toolBar.add(reloadWSButton);

        JButton wsEditButton = new JButton(editWSAction);
        JButton stpButton = new JButton(openSTPAction);

        wsEditButton.setText("");
        wsEditButton.setToolTipText((String) editWSAction.getValue(Action.NAME));
        wsEditButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Preferences.png")));
        toolBar.add(wsEditButton);

        stpButton.setText("");
        stpButton.setToolTipText((String) openSTPAction.getValue(Action.NAME));
        stpButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/stp.png")));
        toolBar.add(stpButton);

        if (explorer.isTlugized()) {
            JButton launchModelButton = new JButton(launchModelAction);
            launchModelButton.setText("");
            launchModelButton.setToolTipText((String) launchModelAction.getValue(Action.NAME));
            launchModelButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
            toolBar.add(launchModelButton);

            JButton launchWizardButton = new JButton(launchWizardAction);
            launchWizardButton.setText("");
            launchWizardButton.setToolTipText((String) launchWizardAction.getValue(Action.NAME));
            launchWizardButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRunLauncher.png")));
            toolBar.add(launchWizardButton);
        }

        JButton sensitivityAnalysisButton = new JButton(sensitivityAnalysisAction);
        sensitivityAnalysisButton.setText("");
        sensitivityAnalysisButton.setToolTipText((String) sensitivityAnalysisAction.getValue(Action.NAME));
        sensitivityAnalysisButton.setIcon(new ImageIcon(getClass().getResource("/reg/resources/images/gold.png")));
        toolBar.add(sensitivityAnalysisButton);

        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);
        toolBarPanel.add(mcat5ToolBar);

        getContentPane().add(toolBarPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText(JAMSExplorer.APP_TITLE + " " + JAMSExplorer.APP_VERSION);
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("FILE"));
        mainMenu.add(fileMenu);
        JMenu prefsMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PREFERENCES"));
        mainMenu.add(prefsMenu);
        JMenu plotMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PLOT"));
        mainMenu.add(plotMenu);

        JMenuItem openWSItem = new JMenuItem(openWSAction);
        openWSItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(openWSItem);

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);

        JMenuItem stpItem = new JMenuItem(openSTPAction);
//        stpIcon.setAccelerator()
        plotMenu.add(stpItem);

        JMenuItem editWSItem = new JMenuItem(editWSAction);
        prefsMenu.add(editWSItem);

        JMenuItem editPrefsItem = new JMenuItem(editPrefsAction);
        prefsMenu.add(editPrefsItem);

        setJMenuBar(mainMenu);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(d.width, JAMSExplorer.SCREEN_WIDTH), Math.min(d.height, JAMSExplorer.SCREEN_HEIGHT));

        // init the model document
        if (explorer.isTlugized()) {
            this.initModelDoc();
        }
    }

    private void initModelDoc() {
        try {
            JAMSWorkspace workspace = explorer.getWorkspace();
            if (workspace != null) {
                String directoryName = workspace.getDirectory().getPath();
                String completeFileName = directoryName + File.separator + workspace.getModelFilename();
                System.out.println("initModelDoc: try to get document from file " + completeFileName);
                /*
                try {
                File[] dirFiles = JAMSTools.getFiles(directoryName, null);
                for (File dirFile : dirFiles) {
                System.out.println("found file " + dirFile.getName());
                }
                } catch (Exception e) {
                }
                 */
                File modelFile = new File(completeFileName);
                if (modelFile == null || !modelFile.exists()) {
                    System.out.println("Datei nicht gefunden !!");
                } else {
                    this.modelDoc = XMLIO.getDocument(completeFileName);
                }
            }

        } catch (FileNotFoundException ex) {
            explorer.getRuntime().handle(ex);
        }
    }

    public void open(File workspaceFile) {
        try {
            String[] libs = JAMSTools.toArray(explorer.getProperties().getProperty("libs", ""), ";");
            JAMSWorkspace workspace = new JAMSWorkspace(workspaceFile, explorer.getRuntime(), false);
            workspace.setLibs(libs);
            explorer.getDisplayManager().removeAllDisplays();
            explorer.setWorkspace(workspace);
            if (explorer.isTlugized()) {
                this.initModelDoc();
            }
            this.update();

        } catch (InvalidWorkspaceException iwe) {
            explorer.getRuntime().handle(iwe);
        }
    }

    private void open() {

        int result = jfc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            openWSDlg.setTask(new Runnable() {

                public void run() {
                    open(jfc.getSelectedFile());
                }
            });
            openWSDlg.execute();
        }
    }

    public void reset() {
        explorer.setWorkspace(null);
        update();
    }

    public void update() {
        JAMSWorkspace workspace = explorer.getWorkspace();

        if (workspace == null) {
            editWSAction.setEnabled(false);
            launchModelAction.setEnabled(false);
            launchWizardAction.setEnabled(false);
            reloadWSAction.setEnabled(false);
        } else {
            jfc.setSelectedFile(workspace.getDirectory());
            setTitle(JAMSExplorer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
//            updateMainPanel(new JPanel());
            editWSAction.setEnabled(true);
            reloadWSAction.setEnabled(true);
            launchWizardAction.setEnabled(true);

            // check if the default model is existing
            File modelFile = new File(workspace.getDirectory(), workspace.getModelFilename());
            if (modelFile.exists()) {
                launchModelAction.setEnabled(true);
            } else {
                launchModelAction.setEnabled(false);
            }
            explorer.getDisplayManager().getTreePanel().update();
            mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        }
    }

    private void launchModel() {

        JAMSLauncher launcher = new JAMSLauncher(null, explorer.getProperties(), modelDoc);
        launcher.setVisible(true);
    }

    private void launchWizard() {

        JAMSWorkspace ws = explorer.getWorkspace();
        Wizard explorerWizard = new ExplorerWizard().createWizard();

        // init data -> get shape from workspace
        Map initialData = new HashMap<String, String>();
        ShapeFileDataStore shapeDataStore = ws.getFirstShapeInputDataStore();
        if (shapeDataStore != null) {
            String shapeFileName = (new File(shapeDataStore.getUri()).getPath());
            System.out.println("init wizard with shape " + shapeFileName);
            initialData.put(BaseDataPanel.KEY_SHAPE_FILENAME, shapeFileName);
        }

        try {

            Map wizardSettings = (Map) WizardDisplayer.showWizard(explorerWizard,
                    new Rectangle(20, 20, 850, 530), null, initialData);
            if (wizardSettings != null) {
                Set keys = wizardSettings.keySet();
                System.out.println("settings coming from wizard:");
                for (Object key : keys) {
                    System.out.println(key + "=" + wizardSettings.get(key));
                }
                String workSpaceDir = ws.getDirectory().getCanonicalPath();

                // station data -> copy the wished files
                String dataDecision = (String) wizardSettings.get(DataDecisionPanel.KEY_DATA);
                if (dataDecision != null && dataDecision.equals(DataDecisionPanel.VALUE_STATION)) {
                    String computation = (String) wizardSettings.get(StationParamsPanel.KEY_COMPUTATION);
                    // look into directory &computation and get model + output files
                    String sourceDir = workSpaceDir
                            + File.separator + "variants"
                            + File.separator + computation;
                    System.out.println("sourceDir: " + sourceDir);

                    // copy model file
                    String modelFileName = this.copyModelFiles(sourceDir);
                    if (!JAMSTools.isEmptyString(modelFileName)) {
                        // activate the new model
                        ws.setModelFile(modelFileName);
                        this.initModelDoc();
                        this.update();
                        setWorkSpace2Model();
                    }

                } // dataDecision = station

                if (dataDecision != null && dataDecision.equals(DataDecisionPanel.VALUE_SPATIAL)) {


                    String sourceDir = workSpaceDir
                            + File.separator + "variants"
                            + File.separator + "regionalizer";
                    System.out.println("sourceDir: " + sourceDir);

                    // copy model file
                    String modelFileName = this.copyModelFiles(sourceDir);
                    if (!JAMSTools.isEmptyString(modelFileName)) {

                        // activate the new model
                        ws.setModelFile(modelFileName);
                        this.initModelDoc();
                        this.update();

                        Properties properties = new Properties();

                        //additional shape file?
                        String shapeFileName = (String) wizardSettings.get(BaseDataPanel.KEY_SHAPE_FILENAME);
                        if (!JAMSTools.isEmptyString(shapeFileName)) {
                            File theShapeFile = new File(shapeFileName);

                            String fileName = theShapeFile.getName();
                            String id = JAMSTools.getPartOfToken(fileName, 1, "."); // get rid of suffix;
                            ShapeFileDataStore addShapeStore = new ShapeFileDataStore(ws, id, theShapeFile.toURI().toString(), fileName, null);
                            ws.addDataStore(addShapeStore);

                            // put shape to model
                            properties.put("EntityReader.shapeFileName", shapeFileName);
                            properties.put("EntityReader.idName", id);
                        }


                        // put parameter to model
                        Set<String> wizardKeys = ExplorerWizard.KEY_MODEL_MAPPING.keySet();
                        for (String wizardKey : wizardKeys) {
                            String value = (String) wizardSettings.get(wizardKey);
                            String[] modelKeys = ExplorerWizard.KEY_MODEL_MAPPING.get(wizardKey);
                            for (String modelKey : modelKeys) {
                                properties.put(modelKey, value);
                            }
                        }

                        ParameterProcessor.loadParams(modelDoc, properties);
                        setWorkSpace2Model();

                        update();
                        JAMSSpreadSheet spreadSheet = explorer.getDisplayManager().getSpreadSheet();
                        if (spreadSheet != null) {
                            spreadSheet.updateGUI();
                        }
                    }
                } // dataDecision = spatial
            }

        } catch (Exception ex) {
            explorer.getRuntime().handle(ex);
        }
    }

    private void setWorkSpace2Model() {
        JAMSWorkspace workspace = explorer.getWorkspace();
        try {
            String directoryName = workspace.getDirectory().getCanonicalPath();
            System.out.println("setWorkSpace2Model " + directoryName);
            XMLProcessor.setWorkspacePath(modelDoc, directoryName);
        } catch (Exception e) {
            explorer.getRuntime().handle(e);

        }
    }

    /**
     * copy model and related files to workspace
     * @param sourceDir
     * @return name of model file or null
     * @throws IOException
     */
    private String copyModelFiles(String sourceDir)
            throws IOException {
        String modelSourceDir = sourceDir + File.separator + "workspace";
        File[] modelFiles = JAMSTools.getFiles(modelSourceDir, null);
        if (modelFiles == null || modelFiles.length == 0) {
            System.out.println("no model files found ind " + modelSourceDir);
        } else {
            File modelFile = modelFiles[0];

            String workSpaceDir = explorer.getWorkspace().getDirectory().getCanonicalPath();

            String completeModelFileName = modelFile.getAbsolutePath();
            String modelFileName = modelFile.getName();
            String targetFileName = workSpaceDir + File.separator + modelFileName;
            JAMSTools.copyFile(completeModelFileName, targetFileName);


            // copy output files
            String outputSourceDir = sourceDir + File.separator + "output";
            String outputTargetDir = workSpaceDir + File.separator + "output";
            File[] outputFiles = JAMSTools.getFiles(outputSourceDir, "xml");
            if (outputFiles == null || outputFiles.length == 0) {
                System.out.println("no output files found in " + outputSourceDir);
            } else {
                for (File outputFile : outputFiles) {
                    String outputFileName = outputFile.getAbsolutePath();
                    targetFileName = outputTargetDir + File.separator + outputFile.getName();
                    System.out.println("outputFile file found: " + outputFileName);
                    JAMSTools.copyFile(outputFileName, targetFileName);
                }
            }
            return modelFileName;
        }
        return null;

    }

    private void createListener() {
        this.addWindowListener(new WindowListener() {

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowOpened(WindowEvent e) {
            }
        });
    }

    /**
     * @return the tPane
     */
    public JTabbedPane getTPane() {
        return tPane;
    }

    private void exit() {

        Viewer.destroy();

        for (Window window : explorer.getChildWindows()) {
            window.dispose();
        }

        this.setVisible(false);
        this.dispose();

        explorer.exit();
    }
}
