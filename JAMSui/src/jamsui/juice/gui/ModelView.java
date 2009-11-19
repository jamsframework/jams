/*
 * ModelView.java
 * Created on 12. Mai 2006, 08:25
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
package jamsui.juice.gui;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.gui.HelpComponent;
import jams.gui.JAMSLauncher;
import jams.gui.tools.GUIHelper;
import jams.gui.WorkerDlg;
import jams.io.ParameterProcessor;
import jams.tools.XMLIO;
import jams.io.XMLProcessor;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jamsui.juice.*;
import jamsui.juice.ComponentDescriptor;
import jamsui.juice.ModelProperties.Group;
import jamsui.juice.ModelProperties.ModelElement;
import jamsui.juice.ModelProperties.ModelProperty;
import jamsui.juice.gui.tree.ModelTree;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author S. Kralisch
 */
public class ModelView {

    private static final int TREE_PANE_WIDTH = 250;
    private JInternalFrame frame;
    private File savePath;
    private Document initialDoc;
    private JButton modelRunButton,  modelGUIRunButton;
    private ModelTree tree;
    private ComponentPanel compEditPanel;
    //private HashMap<ComponentDescriptor, DataRepository> dataRepositories = new HashMap<ComponentDescriptor, DataRepository>();
    private ModelGUIPanel launcherPanel;
    private ModelEditPanel modelEditPanel;
    private String author = "",  date = "",  description = "",  helpBaseUrl = "",  workspace = "";
    private HashMap<String, ComponentDescriptor> componentDescriptors = new HashMap<String, ComponentDescriptor>();
    private TreePanel modelTreePanel;
    private JDesktopPane parentPanel;
    private ModelProperties modelProperties = new ModelProperties();
    private WorkerDlg loadModelDlg;
    private Runnable modelLoading;
    private static int viewCounter = 0;
    public static ViewList viewList = new ViewList();
    private JAMSRuntime runtime;
//    private PanelDlg launcherPanelDlg;

    public ModelView(JDesktopPane parentPanel) {
        this(getNextViewName(), parentPanel);
    }

    public ModelView(String title, JDesktopPane parentPanel) {

        this.parentPanel = parentPanel;
        modelEditPanel = new ModelEditPanel(this);
        compEditPanel = new ComponentPanel(this);
        launcherPanel = new ModelGUIPanel(this);

        modelTreePanel = new TreePanel();

        modelLoading = new Runnable() {

            @Override
            public void run() {
                try {
                    // create the runtime
                    runtime = new StandardRuntime();

                    // add info and error log output
                    runtime.addInfoLogObserver(new Observer() {

                        @Override
                        public void update(Observable obs, Object obj) {
                            JUICE.getJuiceFrame().getInfoDlg().appendText(obj.toString());
                        }
                    });
                    runtime.addErrorLogObserver(new Observer() {

                        @Override
                        public void update(Observable obs, Object obj) {
                            JUICE.getJuiceFrame().getErrorDlg().appendText(obj.toString());
                        }
                    });

                    // load the model
                    Document modelDoc = getModelDoc();
                    if (modelDoc != null) {
                        runtime.loadModel(modelDoc, JUICE.getJamsProperties());
                    } else {
                        runtime = null;
                    }

                } catch (Exception e) {
                    runtime.handle(e);
                }
            }
        };
        // create worker dialog for model setup
        loadModelDlg = new WorkerDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Model_Setup"));

        // create the internal frame
        frame = new JInternalFrame();

        frame.setClosable(true);
        frame.setIconifiable(true);
        frame.setMaximizable(true);
        frame.setResizable(true);
        frame.setTitle(title);
        frame.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/Context_si.png")));
        //frame.setVisible(true);
        frame.setBounds(0, 0, 600, 600);
        frame.addInternalFrameListener(new InternalFrameListener() {

            @Override
            public void internalFrameActivated(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent evt) {
                exit();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent evt) {
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent evt) {
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        /*
         * create the toolbar
         */
        JToolBar toolBar = new JToolBar();
        //toolBar.setPreferredSize(new Dimension(0, JAMS.TOOLBAR_HEIGHT));

        modelRunButton = new JButton(JUICE.getJuiceFrame().getRunModelAction());
        modelRunButton.setText("");
        modelRunButton.setToolTipText(JAMS.resources.getString("Run_Model"));
        modelRunButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRun.png")));
        toolBar.add(modelRunButton);

        modelGUIRunButton = new JButton(JUICE.getJuiceFrame().getRunModelFromLauncherAction());
        modelGUIRunButton.setText("");
        modelGUIRunButton.setToolTipText(JAMS.resources.getString("Run_model_from_JAMS_Launcher"));
        modelGUIRunButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelRunLauncher.png")));
        toolBar.add(modelGUIRunButton);

        JButton copyGUIButton = new JButton(JUICE.getJuiceFrame().getCopyModelGUIAction());
        copyGUIButton.setText("");
        copyGUIButton.setToolTipText(JAMS.resources.getString("Copy_Model_GUI"));
        copyGUIButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Copy.png")));
        toolBar.add(copyGUIButton);

        JButton pasteGUIButton = new JButton(JUICE.getJuiceFrame().getPasteModelGUIAction());
        pasteGUIButton.setText("");
        pasteGUIButton.setToolTipText(JAMS.resources.getString("Paste_Model_GUI"));
        pasteGUIButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Paste.png")));
        toolBar.add(pasteGUIButton);

        /*
         * create the splitpane
         */
        JSplitPane modelSplitPane = new JSplitPane();
        modelSplitPane.setAutoscrolls(true);
        modelSplitPane.setContinuousLayout(true);

        JTabbedPane tabPane = new JTabbedPane();
        //tabPane.addTab("Model configuration", new JScrollPane(modelEditPanel));
        tabPane.addTab(JAMS.resources.getString("Component_configuration"), new JScrollPane(compEditPanel));
        tabPane.addTab(JAMS.resources.getString("GUI_Builder"), new JScrollPane(launcherPanel));

        modelSplitPane.setLeftComponent(modelTreePanel);
        modelSplitPane.setRightComponent(tabPane);
        modelSplitPane.setDividerLocation(TREE_PANE_WIDTH);
        //modelSplitPane.setOneTouchExpandable(true);
        //modelSplitPane.setDividerSize(DIVIDER_WIDTH);


        /*
         * add everything to the frame
         */
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(modelSplitPane, BorderLayout.CENTER);

        ModelView.viewList.addView(frame, this);

        // check if there already was a view opened
        ModelView currentView = JUICE.getJuiceFrame().getCurrentView();

        // add the current frame to the JDesktopPane
        parentPanel.add(frame, JLayeredPane.DEFAULT_LAYER);

        try {
            if (currentView == null) {
                frame.setMaximum(true);
            }
        } catch (PropertyVetoException pve) {
            JAMSTools.handle(pve);
        }
    }

    /**
     * This method will create a JAMSLauncher window with the current model 
     * loaded
     */
    public void runModelFromLauncher() {
        launcherPanel.updateProperties();
        JAMSLauncher launcher = new JAMSLauncher(JUICE.getJuiceFrame(), JUICE.getJamsProperties(), getModelDoc());
        launcher.setVisible(true);
    }

    public void runModel() {

        // first check if provided parameter values are valid
        /*if (!launcherPanel.verifyInputs()) {
        return;
        }*/

        // then load the model via the modelLoading runnable
        loadModelDlg.setTask(modelLoading);
        loadModelDlg.execute();

        // check if runtime has been created successfully
        if (runtime == null) {
            return;
        }

        // then execute it
        Thread t = new Thread() {

            @Override
            public void run() {

                // start the model
                runtime.runModel();

                JUICE.getJuiceFrame().getInfoDlg().appendText("\n\n");
                JUICE.getJuiceFrame().getErrorDlg().appendText("\n\n");

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

    public static String getNextViewName() {
        viewCounter++;
        return JAMS.resources.getString("Model") + viewCounter;
    }

    public boolean save() {

        boolean result = false;

        Document doc = getModelDoc();

        try {
            result = XMLIO.writeXmlFile(doc, savePath);
        } catch (IOException ioe) {
            return false;
        }

        return result;
    }

    public ModelTree getTree() {
        return tree;
    }

    public void setTree(ModelTree tree) {
        this.tree = tree;
        modelTreePanel.setTree(tree);
        updateLauncherPanel();
        modelRunButton.setEnabled(true);
        modelGUIRunButton.setEnabled(true);
    }

    public void updateLauncherPanel() {
        this.launcherPanel.updatePanel();
    }

    public TreePanel getModelTreePanel() {
        return modelTreePanel;
    }

    public void setModelTreePanel(TreePanel modelTreePanel) {
        this.modelTreePanel = modelTreePanel;
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File savePath) {

        if (savePath != null) {
            if (!(savePath.getAbsolutePath().endsWith(".jam") || savePath.getAbsolutePath().endsWith(".xml"))) {
                savePath = new File(savePath.getAbsolutePath() + ".jam");
            }
            frame.setTitle(savePath.getAbsolutePath());
        }
        this.savePath = savePath;
    }

    public void loadParams(File paramsFile) {
        try {
            Document doc = ParameterProcessor.loadParams(getModelDoc(), paramsFile);
            componentDescriptors = new HashMap<String, ComponentDescriptor>();
            this.setTree(new ModelTree(this, doc));
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("File_") + paramsFile.getName() + JAMS.resources.getString("_could_not_be_loaded."), JAMS.resources.getString("File_open_error"));
        }
    }

    public void saveParams(File paramsFile) {
        try {
            String path = null;
            if (getSavePath() != null) {
                path = getSavePath().getAbsolutePath();
            }
            ParameterProcessor.saveParams(getModelDoc(), paramsFile,
                    JUICE.getJamsProperties().getProperty("username"), path);
        } catch (Exception ex) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("File_") + paramsFile.getName() + JAMS.resources.getString("_could_not_be_saved."), JAMS.resources.getString("File_saving_error"));
        }
    }

    /*
     * Create a new name for a component instance.
     * If possible, use the given name, else add a suffix in order to create a unique one.
     */
    public String createComponentInstanceName(String name) {

        Set<String> names = getComponentDescriptors().keySet();

        if (!names.contains(name)) {
            return name;
        }

        String[] sArray = JAMSTools.toArray(name, "_");
        if (sArray.length > 1) {
            String suffix = "_" + sArray[sArray.length - 1];
            name = name.substring(0, name.length() - suffix.length());
        }

        int i = 1;
        String result = name + "_" + i;

        while (names.contains(result)) {
            i++;
            result = name + "_" + i;
        }

        return result;
    }

    public boolean exit() {

        boolean returnValue = false;

        String newXMLString = XMLIO.getStringFromDocument(getModelDoc());
        String oldXMLString = XMLIO.getStringFromDocument(initialDoc);

        if (newXMLString.compareTo(oldXMLString) != 0) {
            int result = GUIHelper.showYesNoCancelDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Save_modifications_in_") + this.getFrame().getTitle() + JAMS.resources.getString("_?"), JAMS.resources.getString("Unsaved_modifications"));
            if (result == JOptionPane.OK_OPTION) {
                JUICE.getJuiceFrame().saveModel(this);
                closeView();
                returnValue = true;
            } else if (result == JOptionPane.NO_OPTION) {
                closeView();
                returnValue = true;
            }
        } else {
            closeView();
            returnValue = true;
        }
        return returnValue;
    }

    private void closeView() {
        boolean maximized = this.getFrame().isMaximum();

        ModelView.viewList.removeView(this.getFrame());
        parentPanel.remove(this.getFrame());
        this.getFrame().dispose();
        parentPanel.updateUI();

        if (maximized) {
            try {
                ModelView currentView = JUICE.getJuiceFrame().getCurrentView();
                if (currentView != null) {
                    currentView.getFrame().setMaximum(true);
                }
            } catch (PropertyVetoException pve) {
                JAMSTools.handle(pve);
            }
        }
    }

    public void setModelParameters(Element launcherNode) {
        Node node;

        if (launcherNode != null) {
            getModelProperties().removeAll();
            NodeList groupNodes = launcherNode.getElementsByTagName("group");
            for (int gindex = 0; gindex < groupNodes.getLength(); gindex++) {
                node = groupNodes.item(gindex);
                Element groupElement = (Element) node;
                String groupName = groupElement.getAttribute("name");
                getModelProperties().addGroup(groupName);
                Group group = getModelProperties().getGroup(groupName);

                // @todo subgroups and properties recursive
                NodeList groupChildNodes = groupElement.getChildNodes();
                for (int pindex = 0; pindex < groupChildNodes.getLength(); pindex++) {
                    node = groupChildNodes.item(pindex);
                    if (node.getNodeName().equalsIgnoreCase("property")) {
                        Element propertyElement = (Element) node;
                        ModelProperty property = getPropertyFromElement(propertyElement);
                        if (property != null) {
                            getModelProperties().addProperty(group, property);
                        }
                    }
                    if (node.getNodeName().equalsIgnoreCase("subgroup")) {
                        Element subgroupElement = (Element) node;
                        String subgroupName = subgroupElement.getAttribute("name");
                        Group subgroup = getModelProperties().createSubgroup(group, subgroupName);
                        setHelpComponent(subgroupElement, subgroup);

                        NodeList propertyNodes = subgroupElement.getElementsByTagName("property");
                        for (int kindex = 0; kindex < propertyNodes.getLength(); kindex++) {
                            Element propertyElement = (Element) propertyNodes.item(kindex);
                            ModelProperty property = getPropertyFromElement(propertyElement);
                            if (property != null) {
                                getModelProperties().addProperty(subgroup, property);
                            }
                        }
                    }
                }
            }
        }
        return;
    }

    private ModelProperty getPropertyFromElement(Element propertyElement) {
        ModelProperties.ModelProperty property = getModelProperties().createProperty();
        property.component = getComponentDescriptor(propertyElement.getAttribute("component"));

        if (property.component == null) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Component_") + propertyElement.getAttribute("component") +
                    JAMS.resources.getString("_does_not_exist,_but_is_referred_in_list_of_model_parameters!") +
                    JAMS.resources.getString("Will_be_removed_when_model_is_saved!"), JAMS.resources.getString("Model_loading_error"));
            return null;
        }

        String attributeName = propertyElement.getAttribute("attribute");
        if (attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {
            property.value = propertyElement.getAttribute("value");
        } else {
            // could refer to a component var or a context attribute
            // only one of them will be != null
            property.var = property.component.getComponentAttributes().get(attributeName);
            property.attribute = property.component.getContextAttributes().get(attributeName);
        }
        /*
        if (attributeName.equals("workspace") && (property.component.getClazz() == JAMSModel.class)) {
        property.var = property.component.createComponentAttribute(attributeName, JAMSDirName.class, ComponentDescriptor.ComponentAttribute.READ_ACCESS);
        }
         */
        //check wether the referred parameter is existing or not
        if ((property.attribute == null) && (property.var == null) &&
                !attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Attribute_") + attributeName +
                    JAMS.resources.getString("_does_not_exist_in_component_") + property.component.getName() +
                    JAMS.resources.getString("._Removing_visual_editor!"), JAMS.resources.getString("Model_loading_error"));
            return null;
        }

        // not used anymore
        //property.defaultValue = propertyElement.getAttribute("default");

        // set description and name
        property.description = propertyElement.getAttribute("description");
        property.name = propertyElement.getAttribute("name");

        // keep compatibility to old launcher behaviour
        // if there is still a value given and it is not an 'enable' attribute, 
        // then copy the value to the regarding component attribute
        if (propertyElement.hasAttribute("value") && !attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {
            String valueString = propertyElement.getAttribute("value");
            if (property.var != null) {
                property.var.setValue(valueString);
            } else {
                property.attribute.setValue(valueString);
            }
        }


        String range = propertyElement.getAttribute("range");
        StringTokenizer tok = new StringTokenizer(range, ";");
        if (tok.countTokens() == 2) {
            property.lowerBound = Double.parseDouble(tok.nextToken());
            property.upperBound = Double.parseDouble(tok.nextToken());
        }
        String lenStr = propertyElement.getAttribute("length");
        if (lenStr != null && lenStr.length() > 0) {
            property.length = Integer.parseInt(lenStr);
        }
        setHelpComponent(propertyElement, property);

        return property;

    }

    /**
     * Return an XML document describing the model.
     * @return The XML document describing the model.
     */
    public Document getModelDoc() {
        if (tree == null) {
            return null;
        }

        launcherPanel.updateProperties();
        return tree.getModelDocument();
    }

    /**
     * Loads a JAMS model from file
     * @param fileName The file containing the models XML document.
     */
    public void loadModel(String fileName) {
        try {
            // first do search&replace on the input xml file
            String newModelFilename = XMLProcessor.modelDocConverter(fileName);
            if (!newModelFilename.equalsIgnoreCase(fileName)) {
                GUIHelper.showInfoDlg(JUICE.getJuiceFrame(),
                        JAMS.resources.getString("The_model_definition_in_") + fileName + JAMS.resources.getString("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.resources.getString("_while_your_original_file_was_left_untouched."), JAMS.resources.getString("Info"));
            }
            fileName = newModelFilename;

            this.setSavePath(new File(fileName));
            this.setTree(new ModelTree(this, XMLIO.getDocument(fileName)));

            this.setInitialState();

        } catch (FileNotFoundException fnfe) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("File_") + fileName + JAMS.resources.getString("_could_not_be_loaded."), JAMS.resources.getString("File_open_error"));
        } catch (Exception e) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Unknown_error_during_Model_loading"), JAMS.resources.getString("Model_loading_error"));
            e.printStackTrace();
        }
    }

    public JInternalFrame getFrame() {
        return frame;
    }

    public ComponentPanel getCompEditPanel() {
        return compEditPanel;
    }

    public ModelEditPanel getModelEditPanel() {
        return modelEditPanel;
    }

    public void setCompEditPanel(ComponentPanel compEditPanel) {
        this.compEditPanel = compEditPanel;
    }

    /*
    public DataRepository getDataRepository(ComponentDescriptor context) {
    DataRepository repo = dataRepositories.get(context);
    if (repo == null) {
    repo = new DataRepository(context);
    dataRepositories.put(context, repo);
    }
    return repo;
    }
    
    public HashMap<ComponentDescriptor, DataRepository> getDataRepositories() {
    return dataRepositories;
    }
     */
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHelpBaseUrl() {
        return helpBaseUrl;
    }

    public void setHelpBaseUrl(String helpBaseUrl) {
        this.helpBaseUrl = helpBaseUrl;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public ComponentDescriptor getComponentDescriptor(String name) {
        return this.getComponentDescriptors().get(name);
    }

    public String registerComponentDescriptor(String oldName, String newName, ComponentDescriptor cd) {

        String newNewName = createComponentInstanceName(newName);
        this.getComponentDescriptors().remove(oldName);
        this.getComponentDescriptors().put(newNewName, cd);

        return newNewName;
    }

    public void unRegisterComponentDescriptor(ComponentDescriptor cd) {
        this.getComponentDescriptors().remove(cd.getName());
    }

    public void setInitialState() {
        this.initialDoc = tree.getModelDocument();
    }

    public ModelProperties getModelProperties() {
        return modelProperties;
    }

    public void setModelProperties(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

    public HashMap<String, ComponentDescriptor> getComponentDescriptors() {
        return componentDescriptors;
    }

    private void setHelpComponent(Element theElement, ModelElement theModelElement) throws DOMException {
        // get help component from help node
        HelpComponent helpComponent = new HelpComponent(theElement);
        theModelElement.setHelpComponent(helpComponent);
    }
}    
    
    