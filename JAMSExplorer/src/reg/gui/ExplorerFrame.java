/*
 * ExplorerFrame.java
 * Created on 18. November 2008, 21:40
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package reg.gui;

import jams.JAMS;
import jams.SystemProperties;
import jams.gui.tools.GUIHelper;
import jams.gui.PropertyDlg;
import jams.gui.WorkerDlg;
import jams.gui.WorkspaceDlg;
import jams.io.XMLProcessor;
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
import org.w3c.dom.Document;
import reg.DataCollectionViewController;
import reg.JAMSExplorer;
import optas.hydro.data.DataCollection;
import reg.OutputPanelFactory;
import reg.spreadsheet.STPConfigurator;
import reg.viewer.Viewer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ExplorerFrame extends JFrame {

    protected static final int INOUT_PANE_WIDTH = 250, INOUT_PANE_HEIGHT = 450;
    protected static final int DIVIDER_WIDTH = 6;
    protected JFileChooser jfc = GUIHelper.getJFileChooser();
    protected WorkerDlg openWSDlg;
    protected Action openWSAction, openSTPAction, exitAction, editWSAction, editPrefsAction,
            sensitivityAnalysisAction, reloadWSAction, importDataAction;
    protected JLabel statusLabel;
    protected JSplitPane mainSplitPane;
    protected JTabbedPane tPane;
    protected JAMSExplorer explorer;
    protected PropertyDlg propertyDlg;
    protected WorkspaceDlg wsDlg;
    protected Document modelDoc = null;    
    protected RemoteDataRetrival remoteClient = null;

    private JMenuItem saveEnsembleItem;

    public ExplorerFrame(JAMSExplorer explorer) {
        this.explorer = explorer;        
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
        
        importDataAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("IMPORT_DATA")) {

            public void actionPerformed(ActionEvent e) {
                importData();
            }
        };


        openSTPAction = new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("STACKED_TIME_PLOT")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                STPConfigurator stp = new STPConfigurator((JAMSExplorer) explorer);
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

        JMenu fileMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("FILE"));
        mainMenu.add(fileMenu);

        JMenu prefsMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PREFERENCES"));
        mainMenu.add(prefsMenu);

        JMenuItem editWSItem = new JMenuItem(editWSAction);
        prefsMenu.add(editWSItem);

        if (explorer.isStandAlone()) {
            JMenuItem editPrefsItem = new JMenuItem(editPrefsAction);
            prefsMenu.add(editPrefsItem);
        }

        JMenu plotMenu = new JMenu(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PLOT"));
        mainMenu.add(plotMenu);

        JMenuItem openWSItem = new JMenuItem(openWSAction);
        openWSItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(openWSItem);

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
            JMenuItem newEnsembleItem = new JMenuItem(new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NEW_ENSEMBLE")) {

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
           
            saveEnsembleItem = new JMenuItem(new AbstractAction(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SAVE_ENSEMBLE")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog(rootPane, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PLEASE_ENTER_A_FILENAME"));
                    Component pane = ExplorerFrame.this.getTPane().getSelectedComponent();
                    if (!(pane instanceof DataCollectionView))
                        return;

                    DataCollectionView view = (DataCollectionView)pane;
                    String path = explorer.getWorkspace().getOutputDataDirectory().getAbsolutePath() + "/" + name;
                    if (!explorer.getWorkspace().getOutputDataDirectory().exists()){
                        explorer.getWorkspace().getOutputDataDirectory().mkdirs();
                    }
                    if (!path.endsWith(".cdat")){
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
        
        
        setJMenuBar(mainMenu);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(d.width, JAMSExplorer.SCREEN_WIDTH), Math.min(d.height, JAMSExplorer.SCREEN_HEIGHT));

    }

    /**
     * hook to adapt toolbar in deriving classes
     * @param toolBar
     * @return adapted toolbar
     */
    protected JToolBar processToolBarHook(JToolBar toolBar) {
        return toolBar;
    }

        
    public void open(File workspaceFile) throws InvalidWorkspaceException {
        String[] libs = StringTools.toArray(explorer.getProperties().getProperty(SystemProperties.LIBS_IDENTIFIER, ""), ";");
        JAMSWorkspace workspace = new JAMSWorkspace(workspaceFile, explorer.getRuntime(), true);
        workspace.init();
//        workspace.setLibs(libs);
        explorer.getDisplayManager().removeAllDisplays();
        explorer.setWorkspace(workspace);
        this.update();
    }

    protected void importData(){
        if (remoteClient==null){
            if (this.explorer.getWorkspace()==null){
                JOptionPane.showMessageDialog(remoteClient, java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PLEASE_OPEN_A_WORKSPACE_FIRST"));
            }
            remoteClient = new RemoteDataRetrival(new File(this.explorer.getWorkspace().getOutputDataDirectory().getParentFile() + "/download/"));
        }
        JDialog dlg = remoteClient.getDialog(this);
        dlg.setVisible(true);
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
            editWSAction.setEnabled(false);
            reloadWSAction.setEnabled(false);
        } else {
            workspace.updateDataStores();
            jfc.setSelectedFile(workspace.getDirectory());
            setTitle(JAMSExplorer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
//            updateMainPanel(new JPanel());
            editWSAction.setEnabled(true);
            reloadWSAction.setEnabled(true);

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

        Viewer.destroy();

        for (Window window : ((JAMSExplorer) explorer).getChildWindows()) {
            window.dispose();
        }

        this.setVisible(false);
        this.dispose();

        explorer.exit();
    }
}
