/*
 * JAMSLauncher.java
 * Created on 14. August 2008, 13:37
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.unijena.jams.*;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.HelpComponent;
import org.unijena.jams.io.ParameterProcessor;
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
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSLauncher extends JFrame {

    protected static final String BASE_TITLE = "JAMS Launcher";
    private Map<InputComponent, Element> inputMap;
    private Map<InputComponent, JScrollPane> groupMap;
    private Document modelDocument = null;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JAMSProperties properties;
    private JButton runButton;
    private HelpDlg helpDlg;
    private String initialModelDocString = "";
    private JAMSRuntime runtime;
    private Runnable modelLoading;
    private WorkerDlg setupModelDlg;
    private Font titledBorderFont;
    private Action runModelAction;

    public JAMSLauncher(JAMSProperties properties) {
        this.properties = properties;
        init();
    }

    public JAMSLauncher(JAMSProperties properties, Document modelDocument) {
        this(properties);
        loadModelDefinition(modelDocument);
    }

    public JAMSLauncher(JAMSProperties properties, String modelFilename, String cmdLineArgs) {
        this(properties);
        loadModelDefinition(modelFilename, JAMSTools.toArray(cmdLineArgs, ";"));
    }

    protected void loadModelDefinition(Document modelDocument) {
        this.modelDocument = modelDocument;
        fillAttributes(this.getModelDocument());
        fillTabbedPane(modelDocument);
    }

    protected void loadModelDefinition(String modelFilename, String[] args) {

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

            fillAttributes(this.getModelDocument());
            fillTabbedPane(this.getModelDocument());

        //LHelper.showInfoDlg(JAMSLauncher.this, "Model has been successfully loaded!", "Info");

        } catch (IOException ioe) {
            LHelper.showErrorDlg(JAMSLauncher.this, "The specified model configuration file \"" + modelFilename + "\" could not be found!", "Error");
        } catch (SAXException se) {
            LHelper.showErrorDlg(JAMSLauncher.this, "The specified model configuration file \"" + modelFilename + "\" contains errors!", "Error");
        }
    }

    protected void init() throws HeadlessException, DOMException, NumberFormatException {

        modelLoading = new Runnable() {

            public void run() {

                // check if provided values are valid
                if (!verifyInputs()) {
                    runtime = null;
                    return;
                }
                updateProperties();

                // create a copy of the model document                
                Document modelDocCopy = (Document) getModelDocument().cloneNode(true);

                // create the runtime
                runtime = new StandardRuntime();

                // add info and error log output
                runtime.addInfoLogObserver(new Observer() {

                    public void update(Observable obs, Object obj) {
                        processInfoLog(obj.toString());
                    }
                });
                runtime.addErrorLogObserver(
                        new Observer() {

                            public void update(Observable obs, Object obj) {
//                        LHelper.showErrorDlg(JAMSLauncher.this, "An error has occurred! Please check the error log for further information!", "JAMS Error");
                                processErrorLog(obj.toString());
                            }
                        });

                // load the model
                runtime.loadModel(modelDocCopy, getProperties());
            }
        };

        runModelAction = new AbstractAction("Run Model") {

            @Override
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
        };

        setupModelDlg = new WorkerDlg(this, "Model Setup");

        // create some nice font for the border title
        titledBorderFont = (Font) UIManager.getDefaults().get("TitledBorder.font");
        int fontSize = titledBorderFont.getSize();
        if (titledBorderFont.getStyle() == Font.BOLD) {
            fontSize += 2;
        }
        titledBorderFont = new Font(titledBorderFont.getName(), Font.BOLD, fontSize);

        this.helpDlg = new HelpDlg(this);

        this.setLocationByPlatform(true);
        this.setLayout(new BorderLayout());
        int width = Integer.parseInt(getProperties().getProperty("guiconfigwidth", "600"));
        int height = Integer.parseInt(getProperties().getProperty("guiconfigheight", "400"));
        this.setPreferredSize(new Dimension(width, height));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        this.setTitle(BASE_TITLE);

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

        runButton = new JButton(runModelAction);
        runButton.setText("");
        runButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
        runButton.setEnabled(false);

        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(0, 40));
        toolBar.add(runButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        pack();
    }

    protected void processInfoLog(String logText) {
        // do nothing here
    }

    protected void processErrorLog(String logText) {
        // do nothing here
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

        // create the component hash
        HashMap<String, HashMap<String, Element>> componentHash =
                ParameterProcessor.getAttributeHash(getModelDocument());

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
                    drawProperty(contentPanel, scrollPane, gbl, propertyElement, componentHash, row);
                    row++;
                }
                if (node.getNodeName().equalsIgnoreCase("subgroup")) {
                    Element subgroupElement = (Element) node;
                    String subgroupName = subgroupElement.getAttribute("name");

                    // create the subgroup panel
                    JPanel subgroupPanel = new JPanel(gbl);

                    // create and set the border
                    subgroupPanel.setBorder(BorderFactory.createTitledBorder(null, subgroupName,
                            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, titledBorderFont));

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
                        drawProperty(subgroupPanel, scrollPane, gbl, propertyElement, componentHash, row);
                        row++;
                    }
                    row = row + 2;

                    row++;
                }
            }

            tabbedPane.addTab(groupElement.getAttribute("name"), scrollPane);
        }

        runButton.setEnabled(true);
    }

    private void drawProperty(JPanel contentPanel, JScrollPane scrollPane, GridBagLayout gbl,
            Element property, HashMap<String, HashMap<String, Element>> componentHash, int row) {

        String componentName = property.getAttribute("component");
        String attributeName = property.getAttribute("attribute");
        Element attribute = componentHash.get(componentName).get(attributeName);

        Element targetElement;

        // check type of property
        if (attribute != null) {

            // case 1: attribute is referred

            targetElement = attribute;

            // keep compatibility to old launcher behaviour
            if (property.hasAttribute("value")) {
                attribute.setAttribute("value", property.getAttribute("value"));

                // remove property's  value and default attributes
                property.removeAttribute("value");
                property.removeAttribute("default");
            }

        } else if (attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {

            // case 2: "enable" property of a component is referred

            targetElement = property;

            // remove property's default attributes
            property.removeAttribute("default");

        } else {

            // case 3: attribute does not exist, property removed

            property.getParentNode().removeChild(property);
            LHelper.showInfoDlg(this, "Attribute " + attributeName + " does not " +
                    "exist in component " + componentName + "! Removing visual editor!", "Info");
            return;
        }

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
        ic.setValue(targetElement.getAttribute("value"));

        getInputMap().put(ic, targetElement);
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

    protected void fillAttributes(final Document doc) {

        // extract some model information        
        Element root = doc.getDocumentElement();
        setTitle(BASE_TITLE + ": " + root.getAttribute("name"));
        setHelpBaseUrl(root.getAttribute("helpbaseurl"));

    }

    protected void updateProperties() {
        //check if model definition has been modified
        for (InputComponent ic : getInputMap().keySet()) {
            Element element = getInputMap().get(ic);
            if (ic.verify()) {
                element.setAttribute("value", ic.getValue());
            }
        }
    }

    protected void exit() {
        setVisible(false);
    }

    protected void runModel() {

        // first load the model via the modelLoading runnable
        setupModelDlg.setTask(modelLoading);
        setupModelDlg.execute();

        // check if runtime has been created successfully
        if (runtime == null) {
            return;
        }

        // start the model
        Thread t = new Thread() {

            public void run() {
                try {
                    runtime.runModel();
                } catch (Exception e) {
                    runtime.handle(e);
                }

                //dump the runtime and clean up
                runtime = null;
                Runtime.getRuntime().gc();
            }
        };
        t.start();
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

    protected Document getModelDocument() {
        return modelDocument;
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
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public String getInitialModelDocString() {
        return initialModelDocString;
    }

    protected Action getRunModelAction() {
        return runModelAction;
    }

    class HelpButton extends JButton {

        HelpComponent helpComponent;

        public HelpButton(HelpComponent helpComponent) {
            super();
            this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            this.setPreferredSize(new Dimension(20, 20));
            this.setText("?");
            this.setFont(titledBorderFont);
            //this.setIcon(HelpComponent.HELP_ICON);
            this.setToolTipText("Help");
            this.helpComponent = helpComponent;

        }

        public void showHelp() {
            help(helpComponent);
        }
    }

    public static void main(String[] args) throws Exception {
        JAMSProperties props = new JAMSProperties();
        props.load("D:/jamsapplication/nsk.jap");
        JAMSLauncher launcher = new JAMSLauncher(props, "D:/jamsapplication/JAMS-Gehlberg/j2k_gehlberg.jam", "");
//        JAMSLauncher launcher = new JAMSLauncher(props, "D:/jamsapplication/test.jam", "");
        launcher.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        launcher.setVisible(true);
    }
}
