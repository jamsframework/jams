/*
 * JAMSLauncher.java
 * Created on 27. August 2006, 21:55
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.jams.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.unijena.jams.*;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.HelpComponent;
import org.unijena.jams.io.XMLIO;
import org.unijena.jams.io.XMLProcessor;
import org.unijena.jams.runtime.StandardRuntime;
import org.unijena.jams.runtime.JAMSRuntime;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSLauncher extends JFrame {

    public static final int APPROVE_OPTION = 1;
    public static final int EXIT_OPTION = 0;
    private static final String baseTitle = "JAMS Launcher";
    private int result = EXIT_OPTION;
    private Map<InputComponent, Element> inputMap;
    private Map<InputComponent, JScrollPane> groupMap;
    private String modelFilename;
    private Document modelDocument = null;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JAMSProperties properties;
    private JButton resetButton,  runButton;
    private JMenuItem saveItem,  saveAsItem;
    private PropertyDlg propertyDlg;
    private JFileChooser jfc;
    private LogViewDlg infoDlg = new LogViewDlg(this, 400, 400, "Info Log");
    private LogViewDlg errorDlg = new LogViewDlg(this, 400, 400, "Error Log");
    private HelpDlg helpDlg;
    private JMenuBar mainMenu;
    private JMenu logsMenu;
    private String initialModelDocString = "";
    private JAMSRuntime runtime;
    private Runnable modelLoading;
    private WorkerDlg setupModelDlg;

    public JAMSLauncher(JAMSProperties properties) {
        this.properties = properties;
        init();
    }

    public JAMSLauncher(String modelFilename, JAMSProperties properties, String cmdLineArgs) {
        this(properties);
        loadModelDefinition(modelFilename, JAMSTools.toArray(cmdLineArgs, ";"));
    }

    protected void loadModelDefinition(String modelFilename, String[] args) {

        if (!closeModel()) {
            return;
        }

        try {

            //check if file exists
            File file = new File(modelFilename);
            if (!file.exists()) {
                LHelper.showErrorDlg(this, "Model file " + modelFilename + " could not be found!", "File open error");
                return;
            }

            // first do search&replace on the input xml file
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                LHelper.showInfoDlg(JAMSLauncher.this,
                        "The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.", "Info");
                this.modelFilename = newModelFilename;
            } else {
                this.modelFilename = modelFilename;
            }

            // create string from input model definition file and replace "%x" occurences by cmd line data

            String xmlString = JAMSTools.fileToString(modelFilename);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%" + i, args[i]);
                }
            }

            // finally, create the model document from the string

            this.modelDocument = XMLIO.getDocumentFromString(xmlString);
            this.initialModelDocString = XMLIO.getStringFromDocument(this.modelDocument);
            this.modelFilename = modelFilename;

            fillAttributes(this.getModelDocument());
            fillTabbedPane(this.getModelDocument());

        //LHelper.showInfoDlg(JAMSLauncher.this, "Model has been successfully loaded!", "Info");

        } catch (IOException ioe) {
            LHelper.showErrorDlg(JAMSLauncher.this, "The specified model configuration file \"" + modelFilename + "\" could not be found!", "Error");
        } catch (SAXException se) {
            LHelper.showErrorDlg(JAMSLauncher.this, "The specified model configuration file \"" + modelFilename + "\" contains errors!", "Error");
        }
    }

    private void init() throws HeadlessException, DOMException, NumberFormatException {

        modelLoading = new Runnable() {

            public void run() {

                // create a copy of the model document
                Document modelDocCopy = (Document) getModelDocument().cloneNode(true);

                // create the runtime
                runtime = new StandardRuntime();

                // add info and error log output
                runtime.addInfoLogObserver(new Observer() {

                    public void update(Observable obs, Object obj) {
                        JAMSLauncher.this.getInfoDlg().appendText(obj.toString());
                    }
                });
                runtime.addErrorLogObserver(new Observer() {

                    public void update(Observable obs, Object obj) {
//                        LHelper.showErrorDlg(JAMSLauncher.this, "An error has occurred! Please check the error log for further information!", "JAMS Error");
                        JAMSLauncher.this.getErrorDlg().appendText(obj.toString());
                    }
                });

                // load the model
                runtime.loadModel(modelDocCopy, getProperties());
            }
        };

        jfc = LHelper.getJFileChooser();

        setupModelDlg = new WorkerDlg(this, "Setting up the model");

        this.propertyDlg = new PropertyDlg(this, getProperties());
        this.helpDlg = new HelpDlg(this);

        this.setLocationByPlatform(true);
        this.setLayout(new BorderLayout());
        int width = Integer.parseInt(getProperties().getProperty("guiconfigwidth", "600"));
        int height = Integer.parseInt(getProperties().getProperty("guiconfigheight", "400"));
        this.setPreferredSize(new Dimension(width, height));

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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

        this.setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        this.setTitle(getBaseTitle());

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

        runButton = new JButton();
        runButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                Thread t = new Thread() {

                    public void run() {
                        runModel();

                        // collect some garbage ;)
                        Runtime.getRuntime().gc();
                    }
                };
                t.start();
            }
        });
        runButton.setEnabled(false);

        resetButton = new JButton("Default");
        resetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                for (InputComponent ic : getInputMap().keySet()) {
                    Element element = getInputMap().get(ic);
                    ic.setValue(element.getAttribute("default"));
                }
            }
        });
        resetButton.setEnabled(false);
        //buttonPanel.add(resetButton);

        mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load Model");
        loadItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                File file = null;
                if (JAMSLauncher.this.modelFilename != null) {
                    file = new File(JAMSLauncher.this.modelFilename);
                } else {
                    file = new File(System.getProperty("user.dir"));
                }
                jfc.setCurrentDirectory(file);
                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (jfc.showOpenDialog(JAMSLauncher.this) == JFileChooser.APPROVE_OPTION) {

                    String modelFilename = jfc.getSelectedFile().getAbsolutePath();
                    loadModelDefinition(modelFilename, null);

                }
            }
        });
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        fileMenu.add(loadItem);

        saveItem = new JMenuItem("Save Model");
        saveItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                saveModel();
            }
        });
        saveItem.setEnabled(false);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add(saveItem);

        saveAsItem = new JMenuItem("Save Model As...");
        saveAsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                File file = null;
                if (JAMSLauncher.this.modelFilename != null) {
                    file = new File(JAMSLauncher.this.modelFilename);
                } else {
                    file = new File(System.getProperty("user.dir"));
                }

                jfc.setCurrentDirectory(file);

                jfc.setFileFilter(JAMSFileFilter.getModelFilter());
                if (jfc.showSaveDialog(JAMSLauncher.this) == JFileChooser.APPROVE_OPTION) {
                    JAMSLauncher.this.modelFilename = jfc.getSelectedFile().getAbsolutePath();
                    saveModel();
                }
            }
        });
        saveAsItem.setEnabled(false);
        fileMenu.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                exit();
            }
        });
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        fileMenu.add(exitItem);
        getMainMenu().add(fileMenu);

        JMenu editMenu = new JMenu("Extras");
        JMenuItem editOptionsItem = new JMenuItem("Edit Options");
        editOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                propertyDlg.setProperties(getProperties());
                propertyDlg.setVisible(true);
                if (propertyDlg.getResult() == PropertyDlg.APPROVE_OPTION) {
                    propertyDlg.validateProperties();
                }

            }
        });
        editMenu.add(editOptionsItem);

        JMenuItem loadOptionsItem = new JMenuItem("Load Options");
        loadOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showOpenDialog(JAMSLauncher.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().load(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
            }
        });
        editMenu.add(loadOptionsItem);

        JMenuItem saveOptionsItem = new JMenuItem("Save Options as...");
        saveOptionsItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                jfc.setFileFilter(JAMSFileFilter.getPropertyFilter());
                jfc.setSelectedFile(new File(getProperties().getDefaultFilename()));
                int result = jfc.showSaveDialog(JAMSLauncher.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String stringValue = jfc.getSelectedFile().getAbsolutePath();
                    try {
                        getProperties().save(stringValue);
                    } catch (IOException ioe) {
                        JAMS.handle(ioe);
                    }
                }
            }
        });
        editMenu.add(saveOptionsItem);
        getMainMenu().add(editMenu);

        logsMenu = new JMenu("Logs");
        JMenuItem infoLogItem = new JMenuItem("Model info log");
        infoLogItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getInfoDlg().setVisible(true);
            }
        });
        getLogsMenu().add(infoLogItem);
        JMenuItem errorLogItem = new JMenuItem("Model error log");
        errorLogItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getErrorDlg().setVisible(true);
            }
        });
        getLogsMenu().add(errorLogItem);
        getMainMenu().add(getLogsMenu());

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                new AboutDlg(null).setVisible(true);
            }
        });
        helpMenu.add(aboutItem);
        getMainMenu().add(helpMenu);

        setJMenuBar(getMainMenu());

        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(0, 40));
        toolBar.add(runButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        pack();
    //setVisible(true);
    }

    protected boolean verifyInputs() {
        // verify all provided values
        for (InputComponent ic : getInputMap().keySet()) {
            if (!ic.verify()) {

                tabbedPane.setSelectedComponent(getGroupMap().get(ic));

                Color oldColor = ic.getComponent().getBackground();
                ic.getComponent().setBackground(new Color(255, 0, 0));

                if (ic.getErrorCode() == InputComponent.INPUT_OUT_OF_RANGE) {
                    LHelper.showErrorDlg(this, "Selected value out of range!", "Range error");
                } else {
                    LHelper.showErrorDlg(this, "Invalid value!", "Format error");
                }

                ic.getComponent().setBackground(oldColor);
                return false;
            }
        }
        return true;
    }

    private void fillTabbedPane(final Document doc) {

        tabbedPane.removeAll();

        inputMap = new HashMap<InputComponent, Element>();
        groupMap = new HashMap<InputComponent, JScrollPane>();

        JPanel contentPanel, scrollPanel;
        JScrollPane scrollPane;
        GridBagLayout gbl;
        Node node;

        Element root = doc.getDocumentElement();
        Element config = (Element) root.getElementsByTagName("launcher").item(0);
        NodeList groups = config.getElementsByTagName("group");

        for (int i = 0; i < groups.getLength(); i++) {

            contentPanel = new JPanel();
            gbl = new GridBagLayout();
            contentPanel.setLayout(gbl);
            scrollPanel = new JPanel();
            scrollPanel.add(contentPanel);
            scrollPane = new JScrollPane(scrollPanel);

            Element groupElement = (Element) groups.item(i);

            int row = 1;
            NodeList groupChildNodes = groupElement.getChildNodes();
            for (int pindex = 0; pindex < groupChildNodes.getLength(); pindex++) {
                node = groupChildNodes.item(pindex);
                if (node.getNodeName().equalsIgnoreCase("property")) {
                    Element propertyElement = (Element) node;
                    drawProperty(contentPanel, scrollPane, gbl, propertyElement, row);
                    row++;
                }
                if (node.getNodeName().equalsIgnoreCase("subgroup")) {
                    Element subgroupElement = (Element) node;
                    String subgroupName = subgroupElement.getAttribute("name");

                    // create the subgroup panel
                    JPanel subgroupPanel = new JPanel(gbl);

                    // create some nice font for the border title
                    Font titledBorderFont = (Font) UIManager.getDefaults().get("TitledBorder.font");
                    int fontSize = titledBorderFont.getSize();
                    if (titledBorderFont.getStyle() == Font.BOLD) {
                        fontSize += 2;
                    }
                    Font newTitledBorderFont = new Font(titledBorderFont.getName(), Font.BOLD, fontSize);

                    // create and set the border
                    subgroupPanel.setBorder(BorderFactory.createTitledBorder(null, subgroupName,
                            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, newTitledBorderFont));


                    // add the subgroup panel
                    row++;
                    LHelper.addGBComponent(contentPanel, gbl, subgroupPanel,
                            0, row, 3, 1,
                            6, 2, 6, 2,
                            1, 1);
                    // help button?
                    HelpComponent helpComponent = new HelpComponent(subgroupElement);
                    if (!helpComponent.isEmpty()) {
                        JPanel helpPanel = new JPanel();
                        HelpButton helpButton = createHelpButton(helpComponent);
                        helpPanel.add(helpButton);
                        LHelper.addGBComponent(contentPanel, gbl, helpPanel,
                                4, row, 1, 1,
                                1, 1, 1, 1,
                                1, 1);
                    }



                    row++;
                    NodeList propertyNodes = subgroupElement.getElementsByTagName("property");
                    for (int kindex = 0; kindex < propertyNodes.getLength(); kindex++) {
                        Element propertyElement = (Element) propertyNodes.item(kindex);
                        drawProperty(subgroupPanel, scrollPane, gbl, propertyElement, row);
                        row++;
                    }
                    row = row + 2;

                    row++;
                }
            }

            tabbedPane.addTab(groupElement.getAttribute("name"), scrollPane);
        }

        runButton.setEnabled(true);
        resetButton.setEnabled(true);
        saveItem.setEnabled(true);
        saveAsItem.setEnabled(true);
    }

    private void drawProperty(JPanel contentPanel, JScrollPane scrollPane, GridBagLayout gbl, Element property, int row) {

        // create a label with the property's name and some space in front of it
        JLabel nameLabel = new JLabel(property.getAttribute("name"));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        LHelper.addGBComponent(contentPanel, gbl, nameLabel, 0, row, 1, 1, 0, 0);

        InputComponent ic = LHelper.createInputComponent(property.getAttribute("type"));

        StringTokenizer tok = new StringTokenizer(property.getAttribute("range"), ";");
        if (tok.countTokens() == 2) {
            String lower = tok.nextToken();
            String upper = tok.nextToken();
            ic.setRange(Double.parseDouble(lower), Double.parseDouble(upper));
        }
        String lenStr = property.getAttribute("length");
        if (lenStr != null && lenStr.length() > 0) {
            ic.setLength(Integer.parseInt(lenStr));
        }

        ic.getComponent().setToolTipText(property.getAttribute("description"));
        ic.setValue(property.getAttribute("value"));

        getInputMap().put(ic, property);
        getGroupMap().put(ic, scrollPane);

        LHelper.addGBComponent(contentPanel, gbl, (Component) ic, 1, row, 2, 1, 1, 1);

        // help button?
        HelpComponent helpComponent = new HelpComponent(property);
        if (!helpComponent.isEmpty()) {
            JPanel helpPanel = new JPanel();
            HelpButton helpButton = createHelpButton(helpComponent);
            helpPanel.add(helpButton);
            LHelper.addGBComponent(contentPanel, gbl, helpPanel,
                    3, row, 1, 1,
                    1, 1, 1, 1,
                    1, 1);
        }


        return;
    }

    private void fillAttributes(final Document doc) {

        Element root = doc.getDocumentElement();
        setTitle(getBaseTitle() + ": " + root.getAttribute("name") + " [" + this.modelFilename + "]");
        setHelpBaseUrl(root.getAttribute("helpbaseurl"));

    }

    private boolean closeModel() {

        if (this.modelDocument == null) {
            return true;
        }

        //check if model definition has been modified
        for (InputComponent ic : getInputMap().keySet()) {
            Element element = getInputMap().get(ic);
            element.setAttribute("value", ic.getValue());
        }

        if (this.modelDocument != null) {
            String modelDocString = XMLIO.getStringFromDocument(this.modelDocument);
            if (!initialModelDocString.equals(modelDocString)) {
                int result = LHelper.showYesNoCancelDlg(this, "Save modifications in " + this.modelFilename + "?", "JAMS Launcher: unsaved modifications");
                if (result == JOptionPane.CANCEL_OPTION) {
                    return false;
                } else if (result == JOptionPane.OK_OPTION) {
                    saveModel();
                }
            }
        }
        return true;
    }

    private void exit() {

        //close the current model
        if (!closeModel()) {
            return;
        }

        // finally write property file to default location
        try {
            String defaultFile = getProperties().getDefaultFilename();
            getProperties().save(defaultFile);
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }

        dispose();
        System.exit(0);
    }

    protected void runModel() {

        // check if provided values are valid
        if (!verifyInputs()) {
            return;
        }

        // set values of document elements to provided
        for (InputComponent ic : getInputMap().keySet()) {
            Element element = getInputMap().get(ic);
            element.setAttribute("value", ic.getValue());
        }

        // first load the model via the modelLoading runnable
        setupModelDlg.setTask(modelLoading);
        setupModelDlg.execute();

        // start the model
        Thread t = new Thread() {

            public void run() {

                runtime.runModel();

                getInfoDlg().appendText("\n\n");
                getErrorDlg().appendText("\n\n");

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

    private void saveModel() {

        for (InputComponent ic : getInputMap().keySet()) {
            Element element = getInputMap().get(ic);
            element.setAttribute("value", ic.getValue());
        }
        try {
            XMLIO.writeXmlFile(getModelDocument(), modelFilename);
        } catch (IOException ioe) {
            LHelper.showErrorDlg(JAMSLauncher.this, "Error saving configuration to " + JAMSLauncher.this.modelFilename, "Error");
            return;
        }
        LHelper.showInfoDlg(JAMSLauncher.this, "Configuration has been saved to " + JAMSLauncher.this.modelFilename, "Info");
    }

    public int getResult() {
        return result;
    }

    protected String getBaseTitle() {
        return baseTitle;
    }

    public JMenuBar getMainMenu() {
        return mainMenu;
    }

    protected JAMSProperties getProperties() {
        return properties;
    }

    public Map<InputComponent, Element> getInputMap() {
        return inputMap;
    }

    public Map<InputComponent, JScrollPane> getGroupMap() {
        return groupMap;
    }

    protected JMenu getLogsMenu() {
        return logsMenu;
    }

    protected JButton getRunButton() {
        return runButton;
    }

    protected Document getModelDocument() {
        return modelDocument;
    }

    protected LogViewDlg getInfoDlg() {
        return infoDlg;
    }

    protected LogViewDlg getErrorDlg() {
        return errorDlg;
    }

    protected String getHelpBaseUrl() {
        return this.helpDlg.getBaseUrl();
    }

    protected void setHelpBaseUrl(String helpBaseUrl) {
        this.helpDlg.setBaseUrl(helpBaseUrl);
    }

    protected HelpButton createHelpButton(HelpComponent helpComponent) {
        HelpButton helpButton = new HelpButton(helpComponent);
        helpButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                HelpButton button = (HelpButton) e.getSource();
                button.showHelp();
            }
        });
        helpButton.setEnabled(true);
        return helpButton;

    }

    public void help(HelpComponent helpComponent) {
        helpDlg.load(helpComponent);
        helpDlg.setVisible(true);

    }

    class HelpButton extends JButton {

        HelpComponent helpComponent;

        public HelpButton(HelpComponent helpComponent) {
            super();
            this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            this.setPreferredSize(new Dimension(20, 20));
            this.setIcon(HelpComponent.HELP_ICON);
            this.setToolTipText("Help");
            this.helpComponent = helpComponent;

        }

        public void showHelp() {
            help(helpComponent);
        }
    }
}
