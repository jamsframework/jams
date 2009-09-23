/*
 * JUICEFrame.java
 * Created on 4. April 2006, 14:18
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
package jams.juice.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import jams.JAMSConstants;
import jams.io.JAMSFileFilter;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.gui.AboutDlg;
import jams.gui.GUIHelper;
import jams.gui.LogViewDlg;
import jams.gui.PropertyDlg;
import jams.gui.RuntimeManagerPanel;
import jams.gui.WorkerDlg;
import jams.juice.*;
import jams.juice.gui.tree.LibTree;
import jams.juice.gui.tree.ModelTree;
import jams.juice.optimizer.wizard.OptimizationWizard;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author  S. Kralisch
 */
public class JUICEFrame extends JFrame {

    private static final int TREE_PANE_WIDTH = 250,  RT_MANAGER_HEIGHT = 600;

    private static final int DIVIDER_WIDTH = 8;

    private PropertyDlg propertyDlg;

    private JFileChooser jfc = GUIHelper.getJFileChooser();

    private TreePanel libTreePanel;

    private JDesktopPane modelPanel = new JDesktopPane();

    private JMenu windowMenu,  modelMenu;

    private JMenuItem pasteModelParameterItem,  copyModelParameterItem,  searchModelItem,  OptimizationWizardItem;

    private JLabel statusLabel;

    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, JUICE.resources.getString("Info_Log"));

    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, JUICE.resources.getString("Error_Log"));

    private Node modelProperties;

    private WorkerDlg loadModelDlg;

    private SearchDlg searchDlg;

    private String modelPath;

    private Action editPrefsAction,  reloadLibsAction,  newModelAction,  loadPrefsAction,  savePrefsAction,  loadModelAction,  saveModelAction,  saveAsModelAction,  exitAction,  aboutAction,  searchAction,  copyModelGUIAction,  pasteModelGUIAction,  OptimizationWizardGUIAction,  loadModelParamAction,  saveModelParamAction,  runModelAction,  runModelFromLauncherAction,  infoLogAction,  errorLogAction,  onlineAction;

    public JUICEFrame() {
        init();
    }

    private void init() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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

        editPrefsAction = new AbstractAction(JUICE.resources.getString("Edit_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(JUICE.getJamsProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };

        reloadLibsAction = new AbstractAction(JUICE.resources.getString("Reload")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                JUICE.updateLibs();
            }
        };

        newModelAction = new AbstractAction(JUICE.resources.getString("New_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                newModel();
            }
        };

        loadPrefsAction = new AbstractAction(JUICE.resources.getString("Load_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                int result = jfc.showOpenDialog(JUICEFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        JAMSProperties properties = JUICE.getJamsProperties();
                        properties.load(stringValue);

                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        savePrefsAction = new AbstractAction(JUICE.resources.getString("Save_Preferences...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                int result = jfc.showSaveDialog(JUICEFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        JAMSProperties properties = JUICE.getJamsProperties();
                        properties.save(stringValue);
                    } catch (IOException ioe) {
                        JAMSTools.handle(ioe);
                    }
                }
            }
        };

        loadModelAction = new AbstractAction(JUICE.resources.getString("Open_Model...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                int result = jfc.showOpenDialog(JUICEFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    JUICEFrame.this.loadModel(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        };

        saveModelAction = new AbstractAction(JUICE.resources.getString("Save_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveModel(getCurrentView());
            }
        };
        saveModelAction.setEnabled(false);

        saveAsModelAction = new AbstractAction(JUICE.resources.getString("Save_Model_As...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveModelAs(getCurrentView());
            }
        };
        saveAsModelAction.setEnabled(false);

        exitAction = new AbstractAction(JAMSConstants.resources.getString("Exit")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        aboutAction = new AbstractAction(JAMSConstants.resources.getString("About")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDlg(JUICEFrame.this).setVisible(true);
            }
        };

        searchAction = new AbstractAction(JUICE.resources.getString("Find...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchDlg.setVisible(true);
            }
        };

        copyModelGUIAction = new AbstractAction(JUICE.resources.getString("Copy_Model_GUI")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                getPasteModelGUIAction().setEnabled(true);
                ModelView view = getCurrentView();
                modelProperties = view.getModelDoc().getElementsByTagName("launcher").item(0).cloneNode(true);
            }
        };

        pasteModelGUIAction = new AbstractAction(JUICE.resources.getString("Paste_Model_GUI")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                ModelView view = getCurrentView();
                view.setModelParameters((Element) modelProperties);
                view.updateLauncherPanel();
            }
        };

        OptimizationWizardGUIAction = new AbstractAction(JUICE.resources.getString("Optimization_Wizard")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                OptimizationWizard wizard = new OptimizationWizard();
                ModelView view = getCurrentView();
                wizard.runWizard(view.getModelDoc(), JUICE.getJamsProperties(), JUICEFrame.this);
            }
        };

        loadModelParamAction = new AbstractAction(JUICE.resources.getString("Load_Model_Parameter...")) {

            @Override
            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getParameterFilter());
                int result = jfc.showOpenDialog(JUICEFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfc.getSelectedFile().getAbsolutePath();
                    File file = new File(path);
                    getCurrentView().loadParams(file);
                }
            }
        };

        saveModelParamAction = new AbstractAction(JUICE.resources.getString("Save_Model_Parameter...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getParameterFilter());
                int result = jfc.showSaveDialog(JUICEFrame.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = jfc.getSelectedFile().getAbsolutePath();
                    File file = new File(path);
                    getCurrentView().saveParams(file);
                }
            }
        };

        runModelAction = new AbstractAction(JAMSConstants.resources.getString("Run_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                ModelView view = getCurrentView();
                view.runModel();
            }
        };

        runModelFromLauncherAction = new AbstractAction(JUICE.resources.getString("Run_model_from_JAMS_Launcher")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                ModelView view = getCurrentView();
                view.runModelFromLauncher();
            }
        };

        infoLogAction = new AbstractAction(JUICE.resources.getString("Info_Log...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                infoDlg.setVisible(true);
            }
        };

        errorLogAction = new AbstractAction(JUICE.resources.getString("Error_Log...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                errorDlg.setVisible(true);
            }
        };

        onlineAction = new AbstractAction(JAMSConstants.resources.getString("JAMS_online...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIHelper.openURL(JAMSConstants.resources.getString("JAMS_URL"));
            }
        };

        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        setTitle(JUICE.APP_TITLE);

        loadModelDlg = new WorkerDlg(this, JUICE.resources.getString("Loading_Model"));
        searchDlg = new SearchDlg(this);

        propertyDlg = new PropertyDlg(this, JUICE.getJamsProperties());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setCurrentDirectory(JUICE.getBaseDir());

        // use outline or live drag mode for performance or look
        if (System.getProperty("os.name").contains("Windows")) {
            modelPanel.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        } else {
            modelPanel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        }

        JButton reloadLibsButton = new JButton(reloadLibsAction);

        libTreePanel = new TreePanel();
        libTreePanel.addCustomButton(reloadLibsButton);

        JPanel rtManagerPanel = new JPanel();
        rtManagerPanel.setLayout(new BorderLayout());
        rtManagerPanel.add(new JLabel(" " + JAMSConstants.resources.getString("Runtime_Manager") + ":"), BorderLayout.NORTH);
        rtManagerPanel.add(new RuntimeManagerPanel(), BorderLayout.CENTER);

        JSplitPane leftSplitPane = new JSplitPane();
        leftSplitPane.setAutoscrolls(true);
        leftSplitPane.setContinuousLayout(true);
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setTopComponent(libTreePanel);
        leftSplitPane.setBottomComponent(rtManagerPanel);

        JSplitPane mainSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(leftSplitPane);
        mainSplitPane.setRightComponent(modelPanel);
        mainSplitPane.setDividerLocation(TREE_PANE_WIDTH);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(DIVIDER_WIDTH);


        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        //toolBar.setPreferredSize(new Dimension(0, JAMSConstants.TOOLBAR_HEIGHT));

        /*
         * toolbar buttons
         */
        JButton modelNewButton = new JButton(newModelAction);
        modelNewButton.setText("");
        modelNewButton.setToolTipText(JUICE.resources.getString("New_Model"));
        modelNewButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelNew.png")));
        toolBar.add(modelNewButton);

        JButton modelOpenButton = new JButton(loadModelAction);
        modelOpenButton.setText("");
        modelOpenButton.setToolTipText(JUICE.resources.getString("Open_Model..."));
        modelOpenButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelOpen.png")));
        toolBar.add(modelOpenButton);

        JButton modelSaveButton = new JButton(saveModelAction);
        modelSaveButton.setText("");
        modelSaveButton.setToolTipText(JUICE.resources.getString("Save_Model"));
        modelSaveButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelSave.png")));
        toolBar.add(modelSaveButton);

        JButton searchButton = new JButton(searchAction);
        searchButton.setText("");
        searchButton.setToolTipText(JUICE.resources.getString("Find..."));
        searchButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Search.png")));
        toolBar.add(searchButton);

        JButton prefsButton = new JButton(editPrefsAction);
        prefsButton.setText("");
        prefsButton.setToolTipText(JUICE.resources.getString("Edit_Preferences..."));
        prefsButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Preferences.png")));
        toolBar.add(prefsButton);

        JButton infoLogButton = new JButton(infoLogAction);
        infoLogButton.setText("");
        infoLogButton.setToolTipText(JAMSConstants.resources.getString("Show_Info_Log..."));
        infoLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/InfoLog.png")));
        toolBar.add(infoLogButton);

        JButton errorLogButton = new JButton(errorLogAction);
        errorLogButton.setText("");
        errorLogButton.setToolTipText(JAMSConstants.resources.getString("Show_Error_Log..."));
        errorLogButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ErrorLog.png")));
        toolBar.add(errorLogButton);

        JButton helpButton = new JButton(onlineAction);
        helpButton.setText("");
        helpButton.setToolTipText(JAMSConstants.resources.getString("JAMS_online..."));
        helpButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Browser.png")));
        toolBar.add(helpButton);

        JButton exitButton = new JButton(exitAction);
        exitButton.setText("");
        exitButton.setToolTipText(JAMSConstants.resources.getString("Exit"));
        exitButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/system-shutdown.png")));
        toolBar.add(exitButton);


        getContentPane().add(toolBar, BorderLayout.NORTH);


        /*
         * status panel
         */
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText(JUICE.resources.getString("JAMS_Status"));
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        /*
         * menu stuff
         */
        JMenuBar mainMenu = new JMenuBar();

        /*
         * file menu
         */
        JMenu fileMenu = new JMenu(JUICE.resources.getString("File"));
        mainMenu.add(fileMenu);

        JMenuItem newModelItem = new JMenuItem(newModelAction);
        newModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileMenu.add(newModelItem);

        JMenuItem loadModelItem = new JMenuItem(loadModelAction);
        loadModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(loadModelItem);

        JMenuItem saveModelItem = new JMenuItem(saveModelAction);
        saveModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveModelItem);

        JMenuItem saveAsModelItem = new JMenuItem(saveAsModelAction);
        saveAsModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        fileMenu.add(saveAsModelItem);

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);

        /*
         * edit menu
         */
        JMenu extrasMenu = new JMenu(JUICE.resources.getString("Edit"));
        mainMenu.add(extrasMenu);

        JMenuItem editPrefsItem = new JMenuItem(editPrefsAction);
        editPrefsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        extrasMenu.add(editPrefsItem);

        JMenuItem loadPrefsItem = new JMenuItem(loadPrefsAction);
        extrasMenu.add(loadPrefsItem);

        JMenuItem savePrefsItem = new JMenuItem(savePrefsAction);
        extrasMenu.add(savePrefsItem);

        extrasMenu.add(new JSeparator());

        JMenuItem searchItem = new JMenuItem(searchAction);
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        extrasMenu.add(searchItem);


        /*
         * model menu
         */
        modelMenu = new JMenu(JUICE.resources.getString("Model"));
        modelMenu.setEnabled(false);
        mainMenu.add(modelMenu);

        JMenuItem runModelItem = new JMenuItem(runModelAction);
        runModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(runModelItem);

        JMenuItem runModelInLauncherItem = new JMenuItem(runModelFromLauncherAction);
        modelMenu.add(runModelInLauncherItem);

        modelMenu.add(new JSeparator());

        JMenuItem loadModelParamItem = new JMenuItem(loadModelParamAction);
        //loadModelParamItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(loadModelParamItem);

        JMenuItem saveModelParamItem = new JMenuItem(saveModelParamAction);
        //loadModelParamItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        modelMenu.add(saveModelParamItem);

        modelMenu.add(new JSeparator());

        copyModelParameterItem = new JMenuItem(copyModelGUIAction);
        modelMenu.add(copyModelParameterItem);

        pasteModelParameterItem = new JMenuItem(pasteModelGUIAction);
        pasteModelGUIAction.setEnabled(false);
        modelMenu.add(pasteModelParameterItem);

        modelMenu.add(new JSeparator());
        OptimizationWizardItem = new JMenuItem(OptimizationWizardGUIAction);
        OptimizationWizardGUIAction.setEnabled(false);
        modelMenu.add(OptimizationWizardItem);

        /*
         * logs menu
         */
        JMenu logsMenu = new JMenu(JUICE.resources.getString("Logs"));
        mainMenu.add(logsMenu);

        JMenuItem infoLogItem = new JMenuItem(infoLogAction);
        logsMenu.add(infoLogItem);

        JMenuItem errorLogItem = new JMenuItem(errorLogAction);
        logsMenu.add(errorLogItem);

        /*
         * windows menu
         */
        windowMenu = new JMenu(JUICE.resources.getString("Windows"));
        windowMenu.setEnabled(false);
        ModelView.viewList.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                JUICEFrame.this.windowMenu.removeAll();
                ArrayList<ModelView> mViews = ModelView.viewList.getViewList();
                for (int i = 0; i < mViews.size(); i++) {
                    JInternalFrame frame = mViews.get(i).getFrame();
                    WindowItem windowItem = new WindowItem(frame.getTitle(), frame);
                    windowItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            WindowItem item = (WindowItem) e.getSource();
                            try {
                                item.frame.setSelected(true);
                            } catch (PropertyVetoException pve) {
                                JAMSTools.handle(pve);
                            }
                        }
                    });
                    JUICEFrame.this.windowMenu.add(windowItem);
                }
                if (mViews.size() == 0) {
                    JUICEFrame.this.windowMenu.setEnabled(false);
                    return;
                } else {
                    JUICEFrame.this.windowMenu.setEnabled(true);
                }
            }
        });
        mainMenu.add(windowMenu);

        /*
         * help menu
         */
        JMenu helpMenu = new JMenu(JUICE.resources.getString("Help"));
        mainMenu.add(helpMenu);

        JMenuItem onlineItem = new JMenuItem(onlineAction);
        helpMenu.add(onlineItem);

        JMenuItem aboutItem = new JMenuItem(aboutAction);
        helpMenu.add(aboutItem);

        /*
         * register observer for ModelView.viewList
         */
        ModelView.viewList.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (ModelView.viewList.getViewList().size() > 0) {
                    JUICEFrame.this.modelMenu.setEnabled(true);
                    JUICEFrame.this.saveModelAction.setEnabled(true);
                    JUICEFrame.this.saveAsModelAction.setEnabled(true);
                    JUICEFrame.this.OptimizationWizardGUIAction.setEnabled(true);
                } else {
                    JUICEFrame.this.modelMenu.setEnabled(false);
                    JUICEFrame.this.saveModelAction.setEnabled(false);
                    JUICEFrame.this.saveAsModelAction.setEnabled(false);
                }
            }
        });

        /*
         * set main menu and initial size
         */
        setJMenuBar(mainMenu);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int height = Math.min((int) (d.height * 0.95), JUICE.SCREEN_HEIGHT);
        int width = Math.min((int) (d.width * 0.95), JUICE.SCREEN_WIDTH);

        setSize(width, height);
        leftSplitPane.setDividerLocation((int) (height*0.6));

        this.libTreePanel.requestFocus();
    }

    public void setLibTree(LibTree tree) {
        this.libTreePanel.setTree(tree);
    }

    public void newModel() {
        ModelView mView = new ModelView(modelPanel);
        mView.setTree(new ModelTree(mView, null));
        mView.setInitialState();
        mView.getFrame().setVisible(true);
        mView.getFrame().requestFocus();
    }

    public void loadModel(String path) {
        this.modelPath = path;
        this.loadModel();
    }

    private void loadModel() {
        loadModelDlg.setTask(new Runnable() {

            public void run() {
                String path = JUICEFrame.this.modelPath;
                ModelView mView = new ModelView(path, modelPanel);
                mView.loadModel(path);
                mView.getFrame().setVisible(true);
                mView.getFrame().requestFocus();
            }
        });
        loadModelDlg.execute();
    }

    private void saveModelAs(ModelView view) {
        jfc.setFileFilter(JAMSFileFilter.getModelFilter());
        int result = jfc.showSaveDialog(JUICEFrame.this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String path = jfc.getSelectedFile().getAbsolutePath();
            File savePath = new File(path);
            view.setSavePath(savePath);
            saveModel(view);
        }
    }

    public void saveModel(ModelView view) {
        if (view.getSavePath() != null) {
            if (!view.save()) {
                GUIHelper.showErrorDlg(this, JUICE.resources.getString("Error_saving_model_to_") + view.getSavePath(), JUICE.resources.getString("Error"));
                view.setSavePath(null);
            } else {
                view.setInitialState();
            }
        } else {
            saveModelAs(view);
        }
    }

    public ModelView getCurrentView() {
        if (modelPanel.getAllFrames().length == 0) {
            return null;
        }
        JInternalFrame frame = modelPanel.getAllFrames()[0];
        ModelView view = ModelView.viewList.getMViews().get(frame);
        return view;
    }

    public TreePanel getLibTreePanel() {
        return libTreePanel;
    }

    public Action getCopyModelGUIAction() {
        return copyModelGUIAction;
    }

    public Action getPasteModelGUIAction() {
        return pasteModelGUIAction;
    }

    public Action getRunModelAction() {
        return runModelAction;
    }

    public Action getRunModelFromLauncherAction() {
        return runModelFromLauncherAction;
    }

    private class WindowItem extends JMenuItem {

        JInternalFrame frame;

        public WindowItem(String title, JInternalFrame frame) {
            super(title);
            this.frame = frame;
        }
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    public LogViewDlg getInfoDlg() {
        return infoDlg;
    }

    public LogViewDlg getErrorDlg() {
        return errorDlg;
    }

    private void exit() {

        ModelView[] views = ModelView.viewList.getViewList().toArray(new ModelView[ModelView.viewList.getViewList().size()]);

        for (ModelView view : views) {
            if (!view.exit()) {
                return;
            }
        }
        if (ModelView.viewList.getViewList().size() == 0) {

            // finally write property file to default location
            try {
                String defaultFile = JUICE.getJamsProperties().getDefaultFilename();
                JUICE.getJamsProperties().save(defaultFile);
            } catch (IOException ioe) {
                JAMSTools.handle(ioe);
            }

            this.setVisible(false);
            this.dispose();
            //System.exit(0);
        }
    }
}
