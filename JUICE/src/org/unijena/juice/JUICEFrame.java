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

package org.unijena.juice;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSFileFilter;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.gui.AboutDlg;
import org.unijena.jams.gui.LHelper;
import org.unijena.jams.gui.LogViewDlg;
import org.unijena.jams.gui.PropertyDlg;
import org.unijena.jams.io.XMLIO;
import org.unijena.juice.tree.*;
import org.w3c.dom.Document;

/**
 *
 * @author  S. Kralisch
 */
public class JUICEFrame extends JFrame {
    
    private static final int TREE_PANE_WIDTH = 250;
    private static final int DIVIDER_WIDTH = 9;
    
    private PropertyDlg propertyDlg;
    private JFileChooser jfc = new JFileChooser();
    private TreePanel libTreePanel = new TreePanel();
    private JDesktopPane modelPanel = new JDesktopPane();
    private JMenu windowMenu = new JMenu();
    private JLabel statusLabel;
    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, "Info Log");
    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, "Error Log");
    
    
    public JUICEFrame() {
        init();
    }
    
    private void init() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        this.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent e) {
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                exit();
            }
            public void windowDeactivated(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowOpened(WindowEvent e) {
            }
        });
        
        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        setTitle(JUICE.APP_TITLE);
        modelPanel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        
        propertyDlg = new PropertyDlg(this, JUICE.getJamsProperties());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setCurrentDirectory(JUICE.getBaseDir());
        
        JSplitPane mainSplitPane = new JSplitPane();
        mainSplitPane.setAutoscrolls(true);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setLeftComponent(libTreePanel);
        mainSplitPane.setRightComponent(modelPanel);
        mainSplitPane.setDividerLocation(TREE_PANE_WIDTH);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(DIVIDER_WIDTH);
        
        
        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(0, 40));
        
        JButton modelNewButton = new JButton();
        modelNewButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelNew.png")));
        modelNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JUICEFrame.this.newModel();
            }
        });
        toolBar.add(modelNewButton);
        
        getContentPane().add(toolBar, BorderLayout.NORTH);
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setPreferredSize(new java.awt.Dimension(14, 20));
        statusLabel = new JLabel();
        statusLabel.setText("JAMS Status");
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);
        
        //Menu stuff
        JMenuBar mainMenu = new JMenuBar();
        
        JMenuItem aboutItem = new JMenuItem();
        JMenu fileMenu = new JMenu();
        JMenu helpMenu = new JMenu();
        JMenu extrasMenu = new JMenu();
        JMenu logsMenu = new JMenu();
        
        JMenuItem editOptionsItem = new JMenuItem();
        JMenuItem loadOptionsItem = new JMenuItem();
        JMenuItem saveOptionsItem = new JMenuItem();
        JMenuItem loadModelItem = new JMenuItem();
        JMenuItem newModelItem = new JMenuItem();
        JMenuItem exitItem = new JMenuItem();
        JMenuItem saveModelItem = new JMenuItem();
        JMenuItem saveAsModelItem = new JMenuItem();
        
        fileMenu.setText("File");
        newModelItem.setText("New Model");
        newModelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newModel();
            }
        });
        newModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileMenu.add(newModelItem);
        
        loadModelItem.setText("Load Model");
        loadModelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                int result = jfc.showOpenDialog(JUICEFrame.this);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    loadModel(stringValue);
                }
            }
        });
        loadModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(loadModelItem);
        
        saveModelItem.setText("Save Model");
        saveModelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveModel(getCurrentView());
            }
        });
        saveModelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveModelItem);
        
        saveAsModelItem.setText("Save Model As");
        saveAsModelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveModelAs(getCurrentView());
            }
        });
        fileMenu.add(saveAsModelItem);
        
        exitItem.setText("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);
        mainMenu.add(fileMenu);
        
        extrasMenu.setText("Extras");
        editOptionsItem.setText("Edit Options");
        editOptionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(JUICE.getJamsProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }
                
            }
        });
        editOptionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        extrasMenu.add(editOptionsItem);
        
        loadOptionsItem.setText("Load Options");
        loadOptionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                int result = jfc.showOpenDialog(JUICEFrame.this);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        JAMSProperties prop = JUICE.getJamsProperties();
                        prop.load(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
            }
        });
        extrasMenu.add(loadOptionsItem);
        
        saveOptionsItem.setText("Save Options");
        saveOptionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                int result = jfc.showSaveDialog(JUICEFrame.this);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        JAMSProperties prop = JUICE.getJamsProperties();
                        prop.save(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
                
            }
        });
        extrasMenu.add(saveOptionsItem);
        mainMenu.add(extrasMenu);
        
        logsMenu.setText("Logs");
        JMenuItem infoLogItem = new JMenuItem("Info log");
        infoLogItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoDlg.setVisible(true);
            }
        });
        logsMenu.add(infoLogItem);
        JMenuItem errorLogItem = new JMenuItem("Error log");
        errorLogItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                errorDlg.setVisible(true);
            }
        });
        logsMenu.add(errorLogItem);
        mainMenu.add(logsMenu);
        
        windowMenu.setText("Windows");
        windowMenu.setEnabled(false);
        ModelView.viewList.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                JUICEFrame.this.windowMenu.removeAll();
                ArrayList<ModelView> mViews = ModelView.viewList.getViewList();
                for (int i = 0; i < mViews.size(); i++) {
                    JInternalFrame frame = mViews.get(i).getFrame();
                    WindowItem windowItem = new WindowItem(frame.getTitle(), frame);
                    windowItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            WindowItem item = (WindowItem) e.getSource();
                            try {
                                item.frame.setSelected(true);
                            } catch (PropertyVetoException pve) {
                                JAMS.handle(pve);
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
        
        
        helpMenu.setText("Help");
        aboutItem.setText("About");
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new AboutDlg(JUICEFrame.this).setVisible(true);
            }
        });
        helpMenu.add(aboutItem);
        mainMenu.add(helpMenu);
        
        setJMenuBar(mainMenu);
        
        //pack();
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        
        setSize((int) (d.width*JUICE.SCREEN_WIDTH_FRACTION), (int) (d.height*JUICE.SCREEN_HEIGHT_FRACTION));
        super.setExtendedState(Frame.MAXIMIZED_BOTH);
        
    }
    
    public void setLibTree(LibTree tree) {
        this.libTreePanel.setTree(tree);
    }
    
    private void newModel() {
        ModelView mView = new ModelView(modelPanel);
        mView.setTree(new ModelTree(mView));
        mView.setInitialState();
        mView.getFrame().setVisible(true);
    }
    
    public void loadModel(String path) {
        try {
            Document modelDoc = XMLIO.getDocument(path);
            ModelView mView = new ModelView(path, modelPanel);
            mView.setModelDoc(modelDoc);
            mView.setSavePath(new File(path));
            mView.setTree(new ModelTree(mView));
            mView.setInitialState();
            mView.getFrame().setVisible(true);
        } catch (FileNotFoundException fnfe) {
            LHelper.showErrorDlg(this, "File " + path + " could not be loaded.", "File open error");
        }
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
                LHelper.showErrorDlg(this, "Unable to save model at " + view.getSavePath(), "Error");
                view.setSavePath(null);
            }
        } else {
            saveModelAs(view);
        }
    }
    
    public ModelView getCurrentView() {
        JInternalFrame frame = modelPanel.getAllFrames()[0];
        ModelView view = ModelView.viewList.getMViews().get(frame);
        return view;
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
                JAMS.handle(ioe);
            }
            
            this.dispose();
            System.exit(0);
        }
    }
    
}
