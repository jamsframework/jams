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
import jams.gui.LHelper;
import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import reg.viewer.Viewer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class RegionalizerFrame extends JFrame {

    private static final int INOUT_PANE_WIDTH = 250, INOUT_PANE_HEIGHT = 450;

    private static final int DIVIDER_WIDTH = 6;

    private JFileChooser jfc = LHelper.getJFileChooser();

    private WorkerDlg openWSDlg;

    private Action openWSAction, exitAction;

    private JLabel statusLabel;

    private JSplitPane mainSplitPane;

    private JScrollPane mainScroll;

    private JTabbedPane spreadSheetTabs;

    private JAMSExplorer regionalizer;

    public RegionalizerFrame(JAMSExplorer regionalizer) {
        this.regionalizer = regionalizer;
        init();
    }

    private void init() {

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
        setTitle(JAMSExplorer.APP_TITLE);

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        mainScroll = new JScrollPane();

        spreadSheetTabs = new JTabbedPane();

        mainSplitPane = new JSplitPane();
        JSplitPane inoutSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(inoutSplitPane);
//        mainSplitPane.setRightComponent(mainScroll);
        mainSplitPane.setDividerLocation(INOUT_PANE_WIDTH);
        //mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(DIVIDER_WIDTH);

        inoutSplitPane.setAutoscrolls(true);
        inoutSplitPane.setContinuousLayout(true);
        inoutSplitPane.setLeftComponent(regionalizer.getDisplayManager().getTreePanel());
        inoutSplitPane.setRightComponent(regionalizer.getDisplayManager().getInputDSInfoPanel());
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
        statusLabel.setText(JAMSExplorer.APP_TITLE + " v0.1");
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
        spreadSheetTabs.remove(regionalizer.getDisplayManager().getSpreadSheets().get(name));
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
                    //JAMSExplorer.open(jfc.getSelectedFile());
                }
            });
            openWSDlg.execute();
        }
    }

    private void exit() {

        this.setVisible(false);
        this.dispose();
        //System.exit(0);
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
                    Viewer.destroy();
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
