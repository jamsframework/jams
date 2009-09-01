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

import jams.JAMSTools;
import jams.workspace.JAMSWorkspace.InvalidWorkspaceException;
import java.io.FileNotFoundException;
import reg.*;
import jams.gui.GUIHelper;
import jams.gui.JAMSLauncher;
import jams.gui.PropertyDlg;
import jams.gui.WorkerDlg;
import jams.gui.WorkspaceDlg;
import jams.io.XMLIO;
import jams.workspace.JAMSWorkspace;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
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
import org.w3c.dom.Document;
//<<<<<<< .mine
import reg.spreadsheet.STPConfigurator;
import reg.viewer.Viewer;
//=======
//import reg.viewer.Viewer;
//>>>>>>> .r1384

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ExplorerFrame extends JFrame {

    private static final int INOUT_PANE_WIDTH = 250, INOUT_PANE_HEIGHT = 450;

    private static final int DIVIDER_WIDTH = 6;

    private JFileChooser jfc = GUIHelper.getJFileChooser();

    private WorkerDlg openWSDlg;

//<<<<<<< .mine
    //private Action openWSAction, exitAction, editWSAction, launchModelAction, editPrefsAction, openSTPAction;
//=======
    private Action openWSAction, exitAction, editWSAction, launchModelAction, editPrefsAction, reloadWSAction, openSTPAction;
//>>>>>>> .r1384

    private JLabel statusLabel;

    private JSplitPane mainSplitPane;

    private JTabbedPane spreadSheetTabs;

    private JAMSExplorer explorer;

    private PropertyDlg propertyDlg;

    private WorkspaceDlg wsDlg = new WorkspaceDlg();

    public ExplorerFrame(JAMSExplorer regionalizer) {
        this.explorer = regionalizer;
        init();
    }

    private void init() {

        createListener();

        exitAction = new AbstractAction("Close") {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        openWSAction = new AbstractAction("Open Workspace...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        };

        editWSAction = new AbstractAction("Edit Workspace...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                wsDlg.setVisible(explorer.getWorkspace());
            }
        };

        launchModelAction = new AbstractAction("Start Model") {

            @Override
            public void actionPerformed(ActionEvent e) {
                launchModel();
            }
        };

        editPrefsAction = new AbstractAction("Edit Preferences...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(explorer.getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
            }
        };

//<<<<<<< .mine
        openSTPAction = new AbstractAction("Stacked Time Plot") {

            @Override
            public void actionPerformed(ActionEvent e) {
                STPConfigurator stp = new STPConfigurator(explorer);
            }
        };

//=======
        reloadWSAction = new AbstractAction("Reload Workspace") {

            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        };

//>>>>>>> .r1384
        update();

        propertyDlg = new PropertyDlg(this, explorer.getProperties());

        openWSDlg = new WorkerDlg(this, "Opening Workspace");

        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        setTitle(JAMSExplorer.APP_TITLE);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setSelectedFile(new File(System.getProperty("user.dir")));

        spreadSheetTabs = new JTabbedPane();

        mainSplitPane = new JSplitPane();
        JSplitPane inoutSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(inoutSplitPane);
        mainSplitPane.setRightComponent(new JPanel());
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
        wsEditButton.setText("");
        wsEditButton.setToolTipText((String) editWSAction.getValue(Action.NAME));
        wsEditButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Preferences.png")));
        toolBar.add(wsEditButton);

        JButton launchModelButton = new JButton(launchModelAction);
        launchModelButton.setText("");
        launchModelButton.setToolTipText((String) launchModelAction.getValue(Action.NAME));
        launchModelButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
        toolBar.add(launchModelButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText(JAMSExplorer.APP_TITLE + " " + JAMSExplorer.APP_VERSION);
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);


        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        mainMenu.add(fileMenu);
        JMenu prefsMenu = new JMenu("Preferences");
        mainMenu.add(prefsMenu);
        JMenu plotMenu = new JMenu("Plot");
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
    }

    public void addToTabbedPane(String title, Component comp) {
        spreadSheetTabs.addTab(title, comp);
        spreadSheetTabs.setSelectedComponent(comp);
//        spreadSheetTabs.
        updateMainPanel(spreadSheetTabs);
    }

    public void showTab(Component comp) {
        try {
            spreadSheetTabs.setSelectedComponent(comp);
        } catch (NullPointerException npe) {
        }
    }

    public void removeFromTabbedPane(Component comp) {
        spreadSheetTabs.remove(comp);
        updateMainPanel(spreadSheetTabs);
    }

    public void removeFromTabbedPane(String name) {
//        spreadSheetTabs.remove(comp);
        spreadSheetTabs.remove(explorer.getDisplayManager().getSpreadSheets().get(name));
        updateMainPanel(spreadSheetTabs);
    }

    public void updateMainPanel(Component comp) {
//        mainScroll.setViewportView(comp);
//        mainScroll.updateUI();
        mainSplitPane.setRightComponent(comp);
    }

    private void open() {

        int result = jfc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            openWSDlg.setTask(new Runnable() {

                public void run() {

                    String[] libs = JAMSTools.toArray(explorer.getProperties().getProperty("libs", ""), ";");
                    try {
                        JAMSWorkspace workspace = new JAMSWorkspace(jfc.getSelectedFile(), explorer.getRuntime(), true);
                        workspace.setLibs(libs);
                        explorer.open(workspace);
                    } catch (InvalidWorkspaceException ex) {
                        explorer.getRuntime().handle(ex);
                    }
                }
            });
            openWSDlg.execute();
        }
    }

    public void update() {
        JAMSWorkspace workspace = explorer.getWorkspace();

        if (workspace == null) {
            editWSAction.setEnabled(false);
            launchModelAction.setEnabled(false);
            reloadWSAction.setEnabled(false);
        } else {
            jfc.setSelectedFile(workspace.getDirectory());
            setTitle(JAMSExplorer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
            updateMainPanel(new JPanel());
            editWSAction.setEnabled(true);
            reloadWSAction.setEnabled(true);

            // check if the default model is existing
            File modelFile = new File(workspace.getDirectory(), workspace.getModelFilename());
            if (modelFile.exists()) {
                launchModelAction.setEnabled(true);
            }
            explorer.getDisplayManager().getTreePanel().update();
            mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        }
    }

    private void exit() {

        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    private void launchModel() {

        JAMSWorkspace ws = explorer.getWorkspace();
        try {
            Document modelDoc = XMLIO.getDocument(new File(ws.getDirectory(), ws.getModelFilename()).getPath());
            JAMSLauncher launcher = new JAMSLauncher(explorer.getProperties(), modelDoc);
            launcher.setVisible(true);
        } catch (FileNotFoundException ex) {
            explorer.getRuntime().handle(ex);
        }
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
                if (JAMSExplorer.GEOWIND_ENABLE) {
                    //Viewer.destroy();
                }
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
}
