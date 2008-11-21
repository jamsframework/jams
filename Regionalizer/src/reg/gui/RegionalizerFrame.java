/*
 * RegionalizerFrame.java
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

import reg.*;
import reg.gui.InfoPanel;
import jams.JAMSTools;
import jams.gui.LHelper;
import jams.gui.WorkerDlg;
import jams.workspace.VirtualWorkspace;
import java.awt.BorderLayout;
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
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class RegionalizerFrame extends JFrame {

    private static final int INOUT_PANE_WIDTH = 250,  INOUT_PANE_HEIGHT = 500;
    private static final int DIVIDER_WIDTH = 6;
    private JFileChooser jfc = LHelper.getJFileChooser();
    private WorkerDlg openWSDlg;
    private Action openWSAction,  exitAction;
    private JLabel statusLabel;
    private VirtualWorkspace workspace;
    private InfoPanel infoPanel;
    private DataPanel dataPanel;
    private TreePanel treePanel;

    public RegionalizerFrame() {
        init();
    }

    private void init() {

        treePanel = new TreePanel();
        infoPanel = new InfoPanel();
        dataPanel = new DataPanel();


        createListener();

        exitAction = new AbstractAction("Schliessen") {

            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };

        openWSAction = new AbstractAction("Arbeitsverzeichnis öffnen") {

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        };

        openWSDlg = new WorkerDlg(this, "Öffne Arbeitsverzeichnis");

        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        setTitle(Regionalizer.APP_TITLE);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JSplitPane mainSplitPane = new JSplitPane();
        JSplitPane inoutSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(inoutSplitPane);
        mainSplitPane.setRightComponent(dataPanel);
        mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        //mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(DIVIDER_WIDTH);

        inoutSplitPane.setAutoscrolls(true);
        inoutSplitPane.setContinuousLayout(true);
        inoutSplitPane.setLeftComponent(treePanel);
        inoutSplitPane.setRightComponent(infoPanel);
        inoutSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        inoutSplitPane.setDividerLocation(INOUT_PANE_HEIGHT);
        inoutSplitPane.setOneTouchExpandable(false);
        inoutSplitPane.setDividerSize(DIVIDER_WIDTH);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();

        JButton wsOpenButton = new JButton(openWSAction);
        wsOpenButton.setText("");
        wsOpenButton.setToolTipText(openWSAction.toString());
        wsOpenButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelOpen.png")));
        toolBar.add(wsOpenButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText(Regionalizer.APP_TITLE + " v0.1");
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);


        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("Datei");
        mainMenu.add(fileMenu);

        JMenuItem openWSItem = new JMenuItem(openWSAction);
        openWSItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(openWSItem);

        JMenuItem exitItem = new JMenuItem(exitAction);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);


        setJMenuBar(mainMenu);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(d.width, Regionalizer.SCREEN_WIDTH), Math.min(d.height, Regionalizer.SCREEN_HEIGHT));

        open(new File("D:/jamsapplication/JAMS-Gehlberg"));
    }

    private void open(File workspaceFile) {

        try {
            String[] libs = JAMSTools.toArray(Regionalizer.getProperties().getProperty("libs", ""), ";");
            workspace = new VirtualWorkspace(workspaceFile, Regionalizer.getRuntime(), true);
            workspace.setLibs(libs);
            setTitle(Regionalizer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
            treePanel.update(workspace);
        } catch (VirtualWorkspace.InvalidWorkspaceException iwe) {
            Regionalizer.getRuntime().sendHalt(iwe.getMessage());
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

    /**
     * @return the workspace
     */
    public VirtualWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * @return the infoPanel
     */
    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    /**
     * @return the dataPanel
     */
    public DataPanel getDataPanel() {
        return dataPanel;
    }

    /**
     * @return the treePanel
     */
    public TreePanel getTreePanel() {
        return treePanel;
    }

    private void exit() {

        this.dispose();
        System.exit(0);
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
}
