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
package jams.explorer.gui;

import jams.JAMS;
import jams.SystemProperties;
import jams.explorer.DataCollectionViewController;
import jams.explorer.JAMSExplorer;
import jams.explorer.spreadsheet.STPConfigurator;
import jams.gui.PropertyDlg;
import jams.gui.WorkerDlg;
import jams.gui.WorkspaceDlg;
import jams.gui.tools.GUIHelper;
import jams.io.XMLProcessor;
import jams.server.client.gui.BrowseJAMSCloudDlg;
import jams.server.client.gui.SynchronizeDlg;
import jams.tools.JAMSTools;
import jams.tools.StringTools;
import jams.workspace.InvalidWorkspaceException;
import jams.workspace.JAMSWorkspace;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.io.NetCDFFileReader;
import optas.io.NetCDFFileWriter;
import org.w3c.dom.Document;

//import reg.viewer.Viewer;
/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ExplorerFrame extends JFrame {

    protected static final int INOUT_PANE_WIDTH = 250, INOUT_PANE_HEIGHT = 450;
    protected static final int DIVIDER_WIDTH = 6;
    protected JFileChooser jfc = GUIHelper.getJFileChooser();
    protected WorkerDlg openWSDlg;
    protected Action openWSAction, openSTPAction, exitAction, editPrefsAction,
            sensitivityAnalysisAction, reloadWSAction, importDataAction, 
            browseServerAction, closeCurrentPanelAction;
    protected JLabel statusLabel;
    protected JSplitPane mainSplitPane;
    protected JTabbedPane tPane;
    protected JAMSExplorer explorer;
    protected PropertyDlg propertyDlg;
    protected Document modelDoc = null;
    protected BrowseJAMSCloudDlg jamsCloudBrowser = null;
    private JMenuItem saveEnsembleItem;

    public ExplorerFrame(JAMSExplorer explorer) {
        this.explorer = explorer;
        init();
    }

    private void init() {

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        createListener();

        exitAction = new AbstractAction(JAMS.i18n("CLOSE")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        openWSAction = new AbstractAction(JAMS.i18n("OPEN_WORKSPACE...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        };

        editPrefsAction = new AbstractAction(JAMS.i18n("EDIT_PREFERENCES...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(explorer.getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };

        browseServerAction = new AbstractAction(JAMS.i18n("Start_Remote_Control")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                browseJAMSCloud();
            }
        };

        importDataAction = new AbstractAction(JAMS.i18n("IMPORT_DATA")) {

            public void actionPerformed(ActionEvent e) {
                importData();
            }
        };

        openSTPAction = new AbstractAction(JAMS.i18n("STACKED_TIME_PLOT")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                STPConfigurator stp = new STPConfigurator((JAMSExplorer) explorer);
            }
        };

        reloadWSAction = new AbstractAction(JAMS.i18n("RELOAD_WORKSPACE")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        };
        
        closeCurrentPanelAction = new AbstractAction(JAMS.i18n("Close_Current_Panel")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                explorer.getDisplayManager().removeDisplay(tPane.getSelectedComponent());
            }
        };

        update();

        propertyDlg = new PropertyDlg(this, explorer.getProperties());

        openWSDlg = new WorkerDlg(this, JAMS.i18n("OPENING_WORKSPACE"));

        setIconImages(JAMSTools.getJAMSIcons());
        setTitle(JAMSExplorer.APP_TITLE);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //jfc.setSelectedFile(new File(System.getProperty("user.dir")));

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
       
        JButton closeCurrentPanelButton = new JButton(closeCurrentPanelAction);
        closeCurrentPanelButton.setText("");
        closeCurrentPanelButton.setToolTipText((String) closeCurrentPanelAction.getValue(Action.NAME));
        closeCurrentPanelButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/close.png")));
        toolBar.add(closeCurrentPanelButton);        
        

//        JButton wsEditButton = new JButton(editWSAction);
        JButton stpButton = new JButton(openSTPAction);

//        wsEditButton.setText("");
//        wsEditButton.setToolTipText((String) editWSAction.getValue(Action.NAME));
//        wsEditButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Preferences.png")));
//        toolBar.add(wsEditButton);
//        stpButton.setText("");
//        stpButton.setToolTipText((String) openSTPAction.getValue(Action.NAME));
//        stpButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/stp.png")));
//        toolBar.add(stpButton);

        toolBar = processToolBarHook(toolBar);

        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolBarPanel.add(toolBar);

        getContentPane().add(toolBarPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText(JAMS.i18n("DATA_EXPLORER"));
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu(JAMS.i18n("FILE"));
        mainMenu.add(fileMenu);

//        JMenuItem editWSItem = new JMenuItem(editWSAction);
//        prefsMenu.add(editWSItem);
        if (explorer.isStandAlone()) {
            JMenu prefsMenu = new JMenu(JAMS.i18n("PREFERENCES"));
            JMenuItem editPrefsItem = new JMenuItem(editPrefsAction);
            prefsMenu.add(editPrefsItem);
            mainMenu.add(prefsMenu);
        }

        JMenu plotMenu = new JMenu(JAMS.i18n("PLOT"));
        mainMenu.add(plotMenu);

        JMenuItem openWSItem = new JMenuItem(openWSAction);
        openWSItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(openWSItem);

        fileMenu.addSeparator();

//        JMenuItem browserServer = new JMenuItem(browseServerAction);
//        fileMenu.add(browserServer);
        JMenuItem importData = new JMenuItem(importDataAction);
        fileMenu.add(importData);

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);

        JMenuItem stpItem = new JMenuItem(openSTPAction);
//        stpIcon.setAccelerator()
        plotMenu.add(stpItem);

        //<editor-fold defaultstate="collapsed" desc="Ensembles Menu">
        JMenu ensemblesMenu = new JMenu("Ensembles");
        {
            JMenuItem newEnsembleItem = new JMenuItem(new AbstractAction(JAMS.i18n("NEW_ENSEMBLE")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    /* forward to controller */
                    DataCollectionViewController controller = new DataCollectionViewController(new DataCollection());
                    /* display data collection view */
                    tPane.addTab("New Ensemble", controller.getView());
                }
            });
            newEnsembleItem.setEnabled(true);
            ensemblesMenu.add(newEnsembleItem);

            saveEnsembleItem = new JMenuItem(new AbstractAction(JAMS.i18n("SAVE_ENSEMBLE")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog(rootPane, JAMS.i18n("PLEASE_ENTER_A_FILENAME"));
                    Component pane = ExplorerFrame.this.getTPane().getSelectedComponent();
                    if (!(pane instanceof DataCollectionView)) {
                        return;
                    }

                    DataCollectionView view = (DataCollectionView) pane;
                    String path = explorer.getWorkspace().getOutputDataDirectory().getAbsolutePath() + "/" + name;
                    if (!explorer.getWorkspace().getOutputDataDirectory().exists()) {
                        explorer.getWorkspace().getOutputDataDirectory().mkdirs();
                    }
                    if (!path.endsWith(".cdat")) {
                        path = path.concat(".cdat");
                    }
                    view.getDataCollection().save(new File(path));
                }
            });
            saveEnsembleItem.setEnabled(true);
            ensemblesMenu.add(saveEnsembleItem);
        }

        tPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Component pane = ExplorerFrame.this.getTPane().getSelectedComponent();
                if (pane instanceof DataCollectionView) {
                    saveEnsembleItem.setEnabled(true);
                } else {
                    saveEnsembleItem.setEnabled(false);
                }
            }
        });

        mainMenu.add(ensemblesMenu);
        //</editor-fold>

        // NetCDF Menu
        JMenu netCDFMenu = new JMenu("NetCDF");
        mainMenu.add(netCDFMenu);
        final JMenuItem saveToNetCDFItem = new JMenuItem(new AbstractAction("Save as NetCDF...") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Component component = ExplorerFrame.this.getTPane().getSelectedComponent();
                DataCollection collection = ((DataCollectionView) component).getDataCollection();
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".cdf");
                    }

                    @Override
                    public String getDescription() {
                        return "NetCDF 3.0 Format";
                    }
                });
                int result = chooser.showSaveDialog(ExplorerFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getName();
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    NetCDFFileWriter stream = null;
                    try {
                        stream = new NetCDFFileWriter(path);
                        stream.write(collection);
                    } catch (IOException ex) {
                        System.out.println("Unable to write file: " + name + ".");
                    } catch (DataSet.MismatchException ex) {
                        System.out.println(ex.getMessage());
                    } finally {
                        if (stream != null) {
                            try {
                                stream.flush();
                                stream.close();
                            } catch (IOException ex) {
                                System.out.println("Unable to close file: " + name + ".");
                            }
                        }
                    }
                }
            }
        });
        saveToNetCDFItem.setEnabled(false);
        netCDFMenu.add(saveToNetCDFItem);
        JMenuItem readFromNetCDFItem = new JMenuItem(new AbstractAction("Read from NetCDF...") {

            @Override
            public void actionPerformed(ActionEvent ae) {

                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".cdf");
                    }

                    @Override
                    public String getDescription() {
                        return "NetCDF 3.0 Format";
                    }
                });
                int result = chooser.showOpenDialog(ExplorerFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getName();
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    DataCollection collection = null;
                    NetCDFFileReader stream = null;
                    try {
                        stream = new NetCDFFileReader(path);
                        collection = stream.read();
                        DataCollectionViewController controller = new DataCollectionViewController(collection);
                        tPane.addTab("New Ensemble", controller.getView());
                        tPane.setSelectedIndex(tPane.getComponentCount() - 1);
                    } catch (IOException ex) {
                        System.out.println("Unable to write file: " + name + ".");
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (InstantiationException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    } catch (DataSet.MismatchException ex) {
                        System.out.println(ex.getMessage());
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException ex) {
                                System.out.println("Unable to close file: " + name + ".");
                            }
                        }
                    }
                }
            }
        });
        netCDFMenu.add(readFromNetCDFItem);
        tPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Component pane = ExplorerFrame.this.getTPane().getSelectedComponent();
                if (pane instanceof DataCollectionView) {
                    saveToNetCDFItem.setEnabled(true);
                } else {
                    saveToNetCDFItem.setEnabled(false);
                }
            }
        });

        setJMenuBar(mainMenu);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(d.width, JAMSExplorer.SCREEN_WIDTH), Math.min(d.height, JAMSExplorer.SCREEN_HEIGHT));

    }

    /**
     * hook to adapt toolbar in deriving classes
     *
     * @param toolBar
     * @return adapted toolbar
     */
    protected JToolBar processToolBarHook(JToolBar toolBar) {
        return toolBar;
    }

    public void open(File workspaceFile) throws InvalidWorkspaceException {
        String[] libs = StringTools.toArray(explorer.getProperties().getProperty(SystemProperties.LIBS_IDENTIFIER, ""), ";");
        JAMSWorkspace workspace = new JAMSWorkspace(workspaceFile, explorer.getRuntime(), false);
        workspace.init();
        //workspace.setLibs(libs);
        explorer.getDisplayManager().removeAllDisplays();
        explorer.setWorkspace(workspace);
        this.update();
    }

    protected void importData() {
        JAMSWorkspace ws = explorer.getWorkspace();
        try {
            ws.init();
        } catch (InvalidWorkspaceException iwe) {
            iwe.printStackTrace();
            return;
        }
        int id = ws.getID();
        if (id == -1) {
            return;
        }
        try {
            SynchronizeDlg synchronizer = new SynchronizeDlg(this, ws, explorer.getProperties());
            synchronizer.setPreferredSize(new Dimension(640, 480));
            GUIHelper.centerOnParent(synchronizer, true);
            synchronizer.setVisible(true);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    protected void browseJAMSCloud() {
        if (jamsCloudBrowser == null) {
            jamsCloudBrowser = new BrowseJAMSCloudDlg(ExplorerFrame.this, ExplorerFrame.this.explorer.getProperties());
            jamsCloudBrowser.init();
            jamsCloudBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
        GUIHelper.centerOnScreen(jamsCloudBrowser, true);
        jamsCloudBrowser.setVisible(true);
    }

    protected void open() {

        int result = jfc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            openWSDlg.setTask(new Runnable() {

                public void run() {
                    try {
                        open(jfc.getSelectedFile());
                    } catch (InvalidWorkspaceException ex) {
                        explorer.getRuntime().handle(ex);
                    }
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
//            editWSAction.setEnabled(false);
            reloadWSAction.setEnabled(false);
            importDataAction.setEnabled(false);
        } else {
            workspace.updateDataStores();
            jfc.setSelectedFile(workspace.getDirectory());
            setTitle(JAMSExplorer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
//            updateMainPanel(new JPanel());
//            editWSAction.setEnabled(true);
            reloadWSAction.setEnabled(true);
            importDataAction.setEnabled(true);

            explorer.getDisplayManager().getTreePanel().update();
            mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        }
    }

    protected void setWorkSpace2Model() {
        JAMSWorkspace workspace = explorer.getWorkspace();
        try {
            String directoryName = workspace.getDirectory().getCanonicalPath();
            XMLProcessor.setWorkspacePath(modelDoc, directoryName);
        } catch (Exception e) {
            explorer.getRuntime().handle(e);

        }
    }

    protected void createListener() {
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

    protected void exit() {

        //Viewer.destroy();
        for (Window window : ((JAMSExplorer) explorer).getChildWindows()) {
            window.dispose();
        }

        this.setVisible(false);
        this.dispose();

        explorer.exit();
    }
}
