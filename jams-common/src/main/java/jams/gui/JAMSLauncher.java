/*
 * JAMSLauncher.java
 * Created on 14. August 2008, 13:37
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
package jams.gui;

import jams.meta.HelpComponent;
import jams.gui.tools.GUIHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
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
import jams.*;
import jams.gui.input.InputComponent;
import jams.io.ParameterProcessor;
import jams.runtime.StandardRuntime;
import jams.runtime.JAMSRuntime;
import jams.gui.input.InputComponentFactory;
import jams.model.JAMSFullModelState;
import jams.model.Model;
import jams.tools.JAMSTools;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.io.File;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSLauncher extends JFrame {

    protected static final String BASE_TITLE = JAMS.i18n("JAMS_Launcher");
    private static final int BUTTON_SIZE = 20;
    private Map<InputComponent, Element> inputMap;
    private Map<InputComponent, String> attributeNameMap;
    private Map<InputComponent, JScrollPane> groupMap;
    protected Document modelDocument = null;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private SystemProperties properties;
    private JButton runButton, updateButton;
    private HelpDlg helpDlg;
    protected String initialModelDocString = "";
    protected File loadPath;
    protected JAMSRuntime runtime;
    private Runnable modelLoading;
    private WorkerDlg loadModelDlg;
    private Font titledBorderFont;
    private Action runModelAction, updateModelAction;
    private JToolBar toolBar;
    protected JAMSFullModelState state = null;
    protected boolean isEnsembleManagerEnabled;
    private Observer observer;

    public JAMSLauncher(Window parent, SystemProperties properties) {
        this.properties = properties;

        init();
    }

    public JAMSLauncher(Window parent, SystemProperties properties, Document modelDocument) {
        this(parent, properties);
        loadModelDefinition(modelDocument);
    }

    public JAMSLauncher(Window parent, SystemProperties properties, Document modelDocument, File loadPath) {
        this(parent, properties, modelDocument);
        this.loadPath = loadPath;
    }

    protected void loadModelDefinition(Document modelDocument) {
        this.modelDocument = modelDocument;
        fillAttributes(this.getModelDocument());
        fillTabbedPane(modelDocument);
    }

    protected void init() throws HeadlessException, DOMException, NumberFormatException {

        modelLoading = new ErrorCatchingRunnable() {

            @Override
            public void safeRun() {

                if (state != null) {
                    Model model = state.getModel();
                    runtime = model.getRuntime();
                    return;
                }
                // check if provided values are valid
                if (!verifyInputs()) {
                    runtime = null;
                    return;
                }
                updateProperties();

                // create a copy of the model document                
                Document modelDocCopy = (Document) getModelDocument().cloneNode(true);

                // try to determine the default workspace directory
                String defaultWorkspacePath = null;
                if (Boolean.parseBoolean(properties.getProperty(SystemProperties.USE_DEFAULT_WS_PATH)) && (loadPath != null)) {
                    defaultWorkspacePath = loadPath.getParent();
                }

                // create the runtime
                runtime = new StandardRuntime(getProperties());

                // add info and error log output
                runtime.addInfoLogObserver(new Observer() {

                    @Override
                    public void update(Observable obs, Object obj) {
                        processInfoLog(obj.toString());
                    }
                });
                runtime.addErrorLogObserver(new Observer() {

                    @Override
                    public void update(Observable obs, Object obj) {
//                        GUIHelper.showErrorDlg(JAMSLauncher.this, "An error has occurred! Please check the error log for further information!", "JAMS Error");
                        processErrorLog(obj.toString());
                    }
                });

                // load the model
                runtime.loadModel(modelDocCopy, defaultWorkspacePath, loadPath.getAbsolutePath());

                // if workspace has not been provided, check if the document has been
                // read from file and try to use parent directory instead
//                if (StringTools.isEmptyString(runtime.getModel().getWorkspacePath())
//                        && (loadPath != null)) {
//                    String dir = loadPath.getParent();
//                    runtime.getModel().setWorkspacePath(dir);
//                    runtime.sendInfoMsg(JAMS.i18n("no_workspace_defined_use_loadpath") + dir);
//                }
            }
        };

        runModelAction = new AbstractAction(JAMS.i18n("Run_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread() {

                    public void run() {
                        runModel();

                        // collect some garbage ;)
                        java.lang.Runtime.getRuntime().gc();
                    }
                };
                t.start();
            }
        };

        updateModelAction = new AbstractAction(JAMS.i18n("Update_Model")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (verifyInputs(false)) {
                    updateProperties();
                    observer.update(null, getModelDocument());
                }
            }
        };

        loadModelDlg = new WorkerDlg(this, JAMS.i18n("Model_Setup"));

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
        int width = Integer.parseInt(getProperties().getProperty(SystemProperties.GUICONFIGWIDTH_IDENTIFIER, "600"));
        int height = Integer.parseInt(getProperties().getProperty(SystemProperties.GUICONFIGHEIGHT_IDENTIFIER, "400"));
        this.setPreferredSize(new Dimension(width, height));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setIconImages(JAMSTools.getJAMSIcons());
        this.setTitle(BASE_TITLE);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        runButton = new JButton(runModelAction);
        runButton.setText("");
        runButton.setToolTipText(JAMS.i18n("Run_Model"));
        runButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
        runButton.setEnabled(false);

        updateButton = new JButton(updateModelAction);
        updateButton.setText("");
        updateButton.setToolTipText(JAMS.i18n("Update_Model"));
        updateButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelSave.png")));
        updateButton.setEnabled(false);

        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(0, 40));
        toolBar.add(runButton);
        toolBar.add(updateButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        String value = getProperties().getProperty("EnsembleManager");
        if (value != null) {
            isEnsembleManagerEnabled = (Boolean.parseBoolean(value) == true);
        } else {
            isEnsembleManagerEnabled = false;
        }

        pack();
    }

    protected void processInfoLog(String logText) {
        // do nothing here
    }

    protected void processErrorLog(String logText) {
        // do nothing here
    }

    protected boolean verifyInputs() {
        return verifyInputs(true);
    }

    protected boolean verifyInputs(boolean runModel) {
        // verify all provided values
        for (InputComponent ic : getInputMap().keySet()) {
            if (!ic.verify()) {

                tabbedPane.setSelectedComponent(getGroupMap().get(ic));

                ic.setMarked(true);

                String info = "";
                if (runModel) {
                    info = JAMS.i18n("_Stopping_model_execution.");
                }

                if (ic.getErrorCode() == InputComponent.INPUT_OUT_OF_RANGE) {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("Selected_value_out_of_range!") + info, JAMS.i18n("Range_error"));
                } else {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("Invalid_value_found!") + info, JAMS.i18n("Format_error"));
                }

                ic.setMarked(false);

                return false;
            }
        }
        return true;
    }

    protected void fillTabbedPane(final Document doc) {

        // create the component hash
        HashMap<String, HashMap<String, Element>> componentHash
                = ParameterProcessor.getAttributeHash(getModelDocument());

        tabbedPane.removeAll();

        inputMap = new HashMap<InputComponent, Element>();
        attributeNameMap = new HashMap<InputComponent, String>();
        groupMap = new HashMap<InputComponent, JScrollPane>();

        JPanel contentPanel, scrollPanel;
        JScrollPane scrollPane;
        GridBagLayout gbl;
        Node node;

        Element root = doc.getDocumentElement();
        Element config = (Element) root.getElementsByTagName("launcher").item(0);
        if (config != null) {
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
                        GUIHelper.addGBComponent(contentPanel, gbl, subgroupPanel,
                                0, row, 3, 1,
                                6, 2, 6, 2,
                                1, 1);
                        // help button?
                        HelpComponent helpComponent = new HelpComponent(subgroupElement);
                        if (!helpComponent.isEmpty()) {
                            JPanel helpPanel = new JPanel();
                            HelpButton helpButton = createHelpButton(helpComponent);
                            helpPanel.add(helpButton);
                            GUIHelper.addGBComponent(contentPanel, gbl, helpPanel,
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
        }
        runButton.setEnabled(true);
        updateButton.setEnabled(true);
    }

    private void drawProperty(JPanel contentPanel, JScrollPane scrollPane, GridBagLayout gbl,
            Element property, HashMap<String, HashMap<String, Element>> componentHash, int row) {

        String componentName = property.getAttribute("component");
        String componentAttributeName = property.getAttribute("attribute");
        String elementAttributeName;
        Element targetElement;
        HashMap<String, Element> attributeMap;

        attributeMap = componentHash.get(componentName);
        if (attributeMap == null) {
            property.getParentNode().removeChild(property);
            GUIHelper.showInfoDlg(this, JAMS.i18n("Component_with_name_") + componentName + JAMS.i18n("_does_not_exist!") + JAMS.i18n("!_Removing_visual_editor!"), JAMS.i18n("Info"));
            return;
        }

        // check type of property
        if (componentAttributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {

            // case 1: "enable" property of a component is referred
            elementAttributeName = "enabled";
            targetElement = attributeMap.get(componentName);

        } else {

            // case 2: attribute is referred
            elementAttributeName = "value";
            targetElement = attributeMap.get(componentAttributeName);

        }

        // check if attribute is existing
        if (targetElement == null) {
            // attribute does not exist, property removed
            property.getParentNode().removeChild(property);
            GUIHelper.showInfoDlg(this, JAMS.i18n("Attribute_") + componentAttributeName + JAMS.i18n("_does_not_exist_in_component_") + componentName + JAMS.i18n("!_Removing_visual_editor!"), JAMS.i18n("Info"));
            return;
        }

//        // keep compatibility to old launcher behaviour
//        if (property.hasAttribute("value")) {
//            System.out.println(componentName + "." + elementAttributeName + " - " + property.getAttribute("value"));
//
//            targetElement.setAttribute(elementAttributeName, property.getAttribute("value"));
//            // remove property's  value and default attributes
//            property.removeAttribute("value");
//            property.removeAttribute("default");
//        }
        // create a label with the property's name and some space in front of it
        JLabel nameLabel = new JLabel(property.getAttribute("name"));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        GUIHelper.addGBComponent(contentPanel, gbl, nameLabel, 0, row, 1, 1, 0, 0);

        InputComponent ic;
        try {

            // try to get the proper input component
            String typeName = property.getAttribute("type");

            // check if old API version is used in model definition, i.e. classes instead of interfaces
            String prefix;
            if (typeName.startsWith("JAMS")) {
                prefix = "jams.data.";
            } else {
                prefix = "jams.data.Attribute$";
            }

            if (!typeName.startsWith(prefix)) {
                typeName = prefix + typeName;
            }
            ic = InputComponentFactory.createInputComponent(Class.forName(typeName));

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

            ic.setHelpText(property.getAttribute("description"));
            ic.setValue(targetElement.getAttribute(elementAttributeName));

            getInputMap().put(ic, targetElement);
            getAttributeNameMap().put(ic, elementAttributeName);
            getGroupMap().put(ic, scrollPane);

            GUIHelper.addGBComponent(contentPanel, gbl, (Component) ic, 1, row, 2, 1, 1, 1);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        // help button?
        HelpComponent helpComponent = new HelpComponent(property);
        if (!helpComponent.isEmpty()) {
            HelpButton helpButton = createHelpButton(helpComponent);
            GUIHelper.addGBComponent(contentPanel, gbl, helpButton,
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
            String attributeName = getAttributeNameMap().get(ic);
            if (ic.verify()) {
                element.setAttribute(attributeName, ic.getValue());
            }
        }
    }

    protected void exit() {
        setVisible(false);
        dispose();
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    protected void runModel() {

        // first load the model via the modelLoading runnable
        loadModelDlg.setTask(modelLoading);
        loadModelDlg.execute();

        // check if runtime has been created successfully
        if (runtime == null) {
            return;
        }

        // start the model
        Thread t = new Thread() {

            public void run() {
                try {
                    if (state == null) {
                        runtime.runModel();
                    } else {
                        runtime.resume(state.getSmallModelState());
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    runtime.handle(t);
                }

                //dump the runtime and clean up
                runtime = null;
                java.lang.Runtime.getRuntime().gc();
            }
        };
        t.start();
    }

    public SystemProperties getProperties() {
        return properties;
    }

    public Map<InputComponent, Element> getInputMap() {
        return inputMap;
    }

    public Map<InputComponent, String> getAttributeNameMap() {
        return attributeNameMap;
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

    /**
     * @return the toolBar
     */
    public JToolBar getToolBar() {
        return toolBar;
    }

    protected class HelpButton extends JButton {

        HelpComponent helpComponent;

        public HelpButton(HelpComponent helpComponent) {
            super();
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            this.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            this.getInsets().set(0, 0, 0, 0);
            this.setText("?");
            this.setFont(titledBorderFont);
            this.setToolTipText(JAMS.i18n("Help"));
            this.helpComponent = helpComponent;

        }

        public void showHelp() {
            help(helpComponent);
        }
    }
}
