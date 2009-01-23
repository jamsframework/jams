/*
 * ModelTree.java
 * Created on 20. April 2006, 11:53
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
package jams.juice.gui.tree;

import jams.JAMS;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jams.JAMSTools;
import jams.JAMSVersion;
import jams.gui.HelpComponent;
import jams.gui.LHelper;
import jams.io.ParameterProcessor;
import jams.model.JAMSContext;
import javax.swing.JFrame;
import jams.juice.ComponentDescriptor;
import jams.juice.ComponentDescriptor.ComponentAttribute;
import jams.juice.ContextAttribute;
import jams.juice.JUICE;
import jams.juice.ModelProperties.ModelProperty;
import jams.juice.ModelProperties.Group;
import jams.juice.gui.ModelView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author S. Kralisch
 */
public class ModelTree extends JAMSTree {

    private static final String NEW_MODEL_NAME = JUICE.resources.getString("New_Model");
    private static final String MODEL_CLASS_NAME = "jams.model.JAMSModel";
    private ModelView view;
    private String modelName = NEW_MODEL_NAME;
    private JPopupMenu popup;
    private boolean smartExpand = true;

    public ModelTree(ModelView view, Document modelDoc) {
        super();
        setEditable(true);

        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.view = view;
        updateModelTree(modelDoc);

        addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                displayComponentInfo();
            }
        });

        JMenuItem showMetadataItem = new JMenuItem(JUICE.resources.getString("Show_Metadata..."));
        showMetadataItem.setAccelerator(KeyStroke.getKeyStroke('M'));
        showMetadataItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showMetaData();
            }
        });

        JMenuItem deleteItem = new JMenuItem(JUICE.resources.getString("Delete"));
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteNode();
            }
        });

        JMenuItem moveUpItem = new JMenuItem(JUICE.resources.getString("Move_up"));
        moveUpItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        moveUpItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                moveUpNode();
            }
        });
        JMenuItem moveDownItem = new JMenuItem(JUICE.resources.getString("Move_down"));
        moveDownItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        moveDownItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                moveDownNode();
            }
        });

        popup = new JPopupMenu();
        popup.add(showMetadataItem);
        popup.add(deleteItem);
        popup.add(moveUpItem);
        popup.add(moveDownItem);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    showPopup(evt);
                }
            }
        });

        addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_DELETE:
                        deleteNode();
                        break;
                    case '-':
                        moveUpNode();
                        break;
                    case '+':
                        moveDownNode();
                        break;
                }
            }
        });
    }

    private void deleteNode() {

        if (this.getSelectionPaths() == null) {
            return;
        }

        for (TreePath path : this.getSelectionPaths()) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();

            int result = LHelper.showYesNoDlg(JUICE.getJuiceFrame(),
                    JUICE.resources.getString("Really_delete_component_") +
                    node.getUserObject().toString() +
                    JUICE.resources.getString("Really_delete_component_2"), JUICE.resources.getString("Deleting_component"));
            if (result == JOptionPane.YES_OPTION) {
                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
                view.unRegisterComponentDescriptor(cd);
                node.removeFromParent();
                this.updateUI();
            }
        }

        this.setSelectionPath(null);
    }

    private void showMetaData() {
        if (this.getSelectionPaths() == null) {
            return;
        }

        for (TreePath path : this.getSelectionPaths()) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
            cd.displayMetadataDlg((JFrame) this.getTopLevelAncestor());
        }
    }

    private void moveUpNode() {

        int i, j;
        TreePath[] paths = this.getSelectionPaths();
        if (paths == null) {
            return;
        }

        int[] index = new int[paths.length];

        i = 0;
        for (TreePath path : paths) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();
            index[i] = parent.getIndex(node);
            i++;
        }

        i = index.length - 1;
        for (int k = paths.length - 1; k >= 0; k--) {

            JAMSNode node = (JAMSNode) paths[k].getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();

            index[i]--;
            j = i - 1;
            while ((j >= 0) && (index[j] == index[i])) {
                index[i]--;
                j--;
            }

            if (index[i] >= 0) {
                parent.insert(node, index[i]);
            }
            i--;
        }

        this.updateUI();
    }

    private void moveDownNode() {

        int i, j;
        TreePath[] paths = this.getSelectionPaths();
        int[] index = new int[paths.length];

        i = 0;
        for (TreePath path : paths) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();
            index[i] = parent.getIndex(node);
            i++;
        }

        i = 0;
        for (TreePath path : paths) {

            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();

            index[i]++;
            j = i + 1;
            while ((j < index.length) && (index[j] == index[i])) {
                index[i]++;
                j++;
            }

            if (index[i] < parent.getChildCount()) {
                parent.insert(node, index[i]);
            }
            i++;
        }

        this.updateUI();
    }

    private void showPopup(MouseEvent evt) {

        TreePath p = this.getClosestPathForLocation(evt.getX(), evt.getY());
        this.addSelectionPath(p);

        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();

        if (node.getType() == JAMSNode.MODEL_ROOT) {
            //return;
        }

        if (node != null) {
            try {
                Class<?> clazz = ((ComponentDescriptor) node.getUserObject()).getClazz();
                if (clazz != null) {
                    popup.show(this, evt.getX(), evt.getY());
                }
            } catch (ClassCastException cce) {
            }
        }
    }

    private void displayComponentInfo() {

        JAMSNode node = (JAMSNode) this.getLastSelectedPathComponent();
        if (node != null) {
            view.getCompEditPanel().setComponentDescriptor(node);
        }
    }

    // Create a XML document from the model tree
    public Document getModelDocument() {

        Document document = null;
        Element element;

        JAMSNode rootNode = (JAMSNode) this.getModel().getRoot();

        // in case no model had been loaded or created, the rootNode is null
        if (rootNode == null) {
            return null;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            ComponentDescriptor cd = (ComponentDescriptor) rootNode.getUserObject();

            Element rootElement = (Element) document.createElement("model");
            rootElement.setAttribute("name", cd.getName());
            rootElement.setAttribute("author", view.getAuthor());
            rootElement.setAttribute("date", view.getDate());
            rootElement.setAttribute("helpbaseurl", view.getHelpBaseUrl());
            rootElement.setAttribute("version", JAMSVersion.getInstance().getVersionString());

            rootElement.appendChild(document.createTextNode("\n"));

            document.appendChild(rootElement);

            element = (Element) document.createElement("description");
            element.appendChild(document.createCDATASection(view.getDescription()));
            rootElement.appendChild(element);

            element = (Element) document.createElement("var");
            element.setAttribute("name", "workspaceDirectory");
            element.setAttribute("value", view.getWorkspace());
            rootElement.appendChild(element);

            rootElement.appendChild(document.createTextNode("\n"));

            element = (Element) document.createElement("launcher");
            for (String group : view.getModelProperties().getGroupNames()) {
                Element groupElement = (Element) document.createElement("group");
                groupElement.setAttribute("name", group);
                Vector properties = view.getModelProperties().getGroup(group).getProperties();
                if (properties != null) {
                    for (Object modelProperty : properties) {

                        // <@todo> groups consist of subgroups and properties,
                        //          subgroups consist of properties
                        //          this could be recursive too
                        if (modelProperty instanceof ModelProperty) {
                            ModelProperty property = (ModelProperty) modelProperty;
                            Element propertyElement = createPropertyElement(document, property);
                            groupElement.appendChild(propertyElement);
                            groupElement.appendChild(document.createTextNode("\n"));
                        }
                        if (modelProperty instanceof Group) {
                            Group subgroup = (Group) modelProperty;
                            Element subgroupElement = (Element) document.createElement("subgroup");
                            subgroupElement.setAttribute("name", subgroup.getCanonicalName());
                            HelpComponent helpComponent = subgroup.getHelpComponent();
                            if (!helpComponent.isEmpty()) {
                                Element helpElement = helpComponent.createDOMElement(document);
                                subgroupElement.appendChild(helpElement);
                                subgroupElement.appendChild(document.createTextNode("\n"));
                            }

                            Vector subgroupProperties = subgroup.getProperties();
                            for (int k = 0; k < subgroupProperties.size(); k++) {
                                Object subgroupProperty = subgroupProperties.get(k);

                                if (subgroupProperty instanceof ModelProperty) {
                                    ModelProperty property = (ModelProperty) subgroupProperty;
                                    Element propertyElement = createPropertyElement(document, property);
                                    subgroupElement.appendChild(propertyElement);
                                    subgroupElement.appendChild(document.createTextNode("\n"));
                                }
                            }
                            groupElement.appendChild(subgroupElement);
                            groupElement.appendChild(document.createTextNode("\n"));
                        }
                    }
                }
                element.appendChild(groupElement);
                element.appendChild(document.createTextNode("\n"));
            }
            rootElement.appendChild(element);
            rootElement.appendChild(document.createTextNode("\n"));

            for (ContextAttribute attribute : cd.getContextAttributes().values()) {
                element = (Element) document.createElement("attribute");
                element.setAttribute("name", attribute.getName());
                element.setAttribute("class", attribute.getType().getName());
                element.setAttribute("value", attribute.getValue());

                rootElement.appendChild(element);
                rootElement.appendChild(document.createTextNode("\n"));
            }

            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {

                rootElement.appendChild(getSubDoc((JAMSNode) rootNode.getChildAt(i), document));
                rootElement.appendChild(document.createTextNode("\n"));

            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return document;
    }

    // return XML element representing a JAMS model property based on a 
    // ModelProperty object
    private Element createPropertyElement(Document document, ModelProperty property) {
        Element propertyElement = (Element) document.createElement("property");
        propertyElement.setAttribute("component", property.component.getName());
        if (property.var != null) {
            propertyElement.setAttribute("attribute", property.var.name);
            propertyElement.setAttribute("type", property.var.type.getSimpleName());
        } else if (property.attribute != null) {
            propertyElement.setAttribute("attribute", property.attribute.getName());
            propertyElement.setAttribute("type", property.attribute.getType().getSimpleName());
        } else {
            propertyElement.setAttribute("attribute", ParameterProcessor.COMPONENT_ENABLE_VALUE);
            propertyElement.setAttribute("type", JUICE.JAMS_DATA_TYPES[0].getSimpleName());
            propertyElement.setAttribute("value", property.value);
        }
        //propertyElement.setAttribute("default", property.defaultValue);
        propertyElement.setAttribute("description", property.description);
        propertyElement.setAttribute("name", property.name);
        //propertyElement.setAttribute("value", property.value);
        propertyElement.setAttribute("range", "" + property.lowerBound + ";" + property.upperBound);
        if (property.length > 0) {
            propertyElement.setAttribute("length", "" + property.length);
        }

        HelpComponent helpComponent = property.getHelpComponent();
        if (!helpComponent.isEmpty()) {
            Element helpElement = helpComponent.createDOMElement(document);
            propertyElement.appendChild(helpElement);
            propertyElement.appendChild(document.createTextNode("\n"));
        }

        return propertyElement;
    }

    // return XML document element representing subtree of a JAMSTree (JTree)
    // whose root node is a given JAMSNode
    private Element getSubDoc(JAMSNode rootNode, Document document) {

        Element rootElement = null;
        ComponentDescriptor cd = (ComponentDescriptor) rootNode.getUserObject();

        switch (rootNode.getType()) {
            case JAMSNode.COMPONENT_NODE:
                rootElement = (Element) document.createElement("component");
                break;
            case JAMSNode.CONTEXT_NODE:
                rootElement = (Element) document.createElement("contextcomponent");
                break;
            case JAMSNode.MODEL_ROOT:
                rootElement = (Element) document.createElement("contextcomponent");
                cd.setClazz(jams.model.JAMSContext.class);
        }

        rootElement.setAttribute("name", cd.getName());
        rootElement.setAttribute("class", cd.getClazz().getName());
        rootElement.appendChild(document.createTextNode("\n"));

        Element element;
        for (ContextAttribute attribute : cd.getContextAttributes().values()) {
            rootElement.appendChild(document.createTextNode("\n"));

            element = (Element) document.createElement("attribute");
            element.setAttribute("name", attribute.getName());
            element.setAttribute("class", attribute.getType().getName());
            element.setAttribute("value", attribute.getValue());

            rootElement.appendChild(element);
        }

        for (ComponentAttribute var : cd.getComponentAttributes().values()) {
            if (!var.getValue().equals("") || ((var.getContext() != null) && !var.getAttribute().equals(""))) {

                element = document.createElement("var");
                element.setAttribute("name", var.name);
                if (!var.getAttribute().equals("")) {
                    element.setAttribute("attribute", var.getAttribute());
                    element.setAttribute("context", var.getContext().getName());
                }
                if (!var.getValue().equals("")) {
                    element.setAttribute("value", var.getValue());
                }

                rootElement.appendChild(element);
                rootElement.appendChild(document.createTextNode("\n"));

            }
        }

        if ((rootNode.getType() == JAMSNode.CONTEXT_NODE) || (rootNode.getType() == JAMSNode.MODEL_ROOT)) {
            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                rootElement.appendChild(getSubDoc((JAMSNode) rootNode.getChildAt(i), document));
                rootElement.appendChild(document.createTextNode("\n"));
            }
        }

        return rootElement;
    }

    public void updateModelTree(Document modelDoc) {

        TreeModel model = new DefaultTreeModel(getModelTree(modelDoc));
        setModel(model);
        this.setSelectionRow(0);
        this.displayComponentInfo();
        this.expandAll();
        smartExpand = false;
    }

    private JAMSNode getModelTree(Document modelDoc) {

        Node node;
        Element element, docRoot;
        Class<?> modelClazz;

        try {
            modelClazz = JUICE.getLoader().loadClass(MODEL_CLASS_NAME);
        } catch (ClassNotFoundException cnfe) {
            modelClazz = null;
        }

        if (modelDoc == null) {
            ComponentDescriptor cd = new ComponentDescriptor(NEW_MODEL_NAME, modelClazz, this);
            JAMSNode rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.MODEL_ROOT);
            return rootNode;
        }

        //get model name, description, author and date

        docRoot = modelDoc.getDocumentElement();
        modelName = docRoot.getAttribute("name");
        view.setAuthor(docRoot.getAttribute("author"));
        view.setDate(docRoot.getAttribute("date"));
        view.setHelpBaseUrl(docRoot.getAttribute("helpbaseurl"));

        Node descriptionNode = docRoot.getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            view.setDescription(descriptionNode.getTextContent().trim());
        }

        //create the tree's root node

        ComponentDescriptor cd = new ComponentDescriptor(modelName, modelClazz, this);
        JAMSNode rootNode = new JAMSNode(cd);
        rootNode.setType(JAMSNode.MODEL_ROOT);


        //handle all contextcomponent and component nodes

        NodeList childs = docRoot.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                element = (Element) node;

                try {
                    rootNode.add(getSubTree(element));
                } catch (ModelLoadException mle) {
                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Could_not_load_component_") +
                            mle.getComponentName() + "\" (" + mle.getClassName() + "). " +
                            JUICE.resources.getString("Please_fix_the_model_definition_file!"), JUICE.resources.getString("Error_loading_model"));
                    this.view.getFrame().dispose();
                    return null;
                }

            } else if (node.getNodeName().equals("attribute")) {
                addContextAttribute(cd, (Element) node);
            } else if (node.getNodeName().equals("var")) {
                element = (Element) node;
                if (element.getAttribute("name").equals("workspaceDirectory")) {
                    view.setWorkspace(element.getAttribute("value"));
                }
            }
        }
        view.getModelEditPanel().updatePanel();


        //handle the launcher node

        Element launcherNode = (Element) docRoot.getElementsByTagName("launcher").item(0);
        view.setModelParameters(launcherNode);

        return rootNode;
    }

    private JAMSNode getSubTree(Element rootElement) throws ModelLoadException {

        Class<?> clazz;
        ComponentDescriptor cd = null;
        String componentName = "", className = "";

        try {

            componentName = rootElement.getAttribute("name");
            className = rootElement.getAttribute("class");

            clazz = JUICE.getLoader().loadClass(className);

            cd = new ComponentDescriptor(componentName, clazz, this);

        } catch (ClassNotFoundException cnfe) {
            throw new ModelLoadException(className, componentName);
        } catch (NoClassDefFoundError ncdfe) {
            throw new ModelLoadException(className, componentName);
        }

        //JAMSNode rootNode = new JAMSNode(rootElement.getAttribute("name"));
        JAMSNode rootNode = null;

        String type = rootElement.getNodeName();

        if (type.equals("component")) {

            rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.COMPONENT_NODE);

            NodeList varChilds = rootElement.getElementsByTagName("var");
            for (int index = 0; index < varChilds.getLength(); index++) {
                setVar(cd, (Element) varChilds.item(index));
            }

        } else if (type.equals("contextcomponent")) {

            rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.CONTEXT_NODE);

            NodeList childs = rootElement.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                Node node = childs.item(index);
                if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {

                    JAMSNode childNode = getSubTree((Element) childs.item(index));
                    if (childNode != null) {
                        rootNode.add(childNode);
                    }

                } else if (node.getNodeName().equals("var")) {

                    setVar(cd, (Element) node);

                } else if (node.getNodeName().equals("attribute")) {

                    addContextAttribute(cd, (Element) node);

                }
            }
        }

        //cd.getUnsetAttributes();

        return rootNode;
    }

    //add attribute that is defined by a context component
    private void addContextAttribute(ComponentDescriptor cd, Element e) {
        try {
            String attribute = e.getAttribute("name");
            String typeName = e.getAttribute("class");
            Class type = Class.forName(typeName);

            if (!type.isArray()) {
                // if the type is not an array, simply create a context attribute
                // and add it to the repository
                String value = e.getAttribute("value");
                cd.addContextAttribute(attribute, type, value);
//                cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
            } else {
                // if it is an array, tokenize the attribute string (semicolon-separated)
                // and do the above for every token
                String[] values = JAMSTools.toArray(attribute);
                for (String value : values) {
                    cd.addContextAttribute(attribute, type, value);
//                    cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
                }
            }

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void setVar(ComponentDescriptor cd, Element e) {

        if (e.hasAttribute("attribute")) {

            String contextName = e.getAttribute("context");
            if (contextName.equals("")) {
                contextName = modelName;
            }

            ComponentDescriptor context = view.getComponentDescriptor(contextName);
            if (context == null) {
                LHelper.showErrorDlg(this.view.getFrame(), JUICE.resources.getString("Error_while_loading_component_") + cd.getName() +
                        JUICE.resources.getString("_context_") + contextName + JUICE.resources.getString("_does_not_exist!"), JUICE.resources.getString("Model_loading_error"));
                return;
            }
            String name = e.getAttribute("name");
            String attribute = e.getAttribute("attribute");

            try {
                ComponentDescriptor.ComponentAttribute ca = cd.getComponentAttributes().get(name);
                ca.linkToAttribute(context, attribute);
            //cd.linkComponentAttribute(name, view.getComponentDescriptor(context), attribute);
/*            } 
            try {
            if (cd.getComponentAttributes().get(name).accessType != ComponentAttribute.READ_ACCESS) {
            Class attributeType = cd.getComponentAttributes().get(name).type;
            context.getDataRepository().addAttribute(new ContextAttribute(attribute, attributeType, context));
            }*/
            } catch (NullPointerException ex) {
                LHelper.showErrorDlg(this.view.getFrame(), JUICE.resources.getString("Error_while_loading_component_") + cd.getName() +
                        JUICE.resources.getString("_component_attribute_") + name + JUICE.resources.getString("_does_not_exist!"), JUICE.resources.getString("Model_loading_error"));
                return;
            }

        }
        if (e.hasAttribute("value")) {
            try {
                cd.getComponentAttributes().get(e.getAttribute("name")).setValue(e.getAttribute("value"));
            } catch (NullPointerException ex) {
                LHelper.showErrorDlg(this.view.getFrame(), JUICE.resources.getString("Error_while_loading_component_") + cd.getName() +
                        JUICE.resources.getString("_component_attribute_") + e.getAttribute("name") + JUICE.resources.getString("_does_not_exist!"), JUICE.resources.getString("Model_loading_error"));
                return;
            }

        }
    }

    public ModelView getView() {
        return view;
    }

    @Override
    protected void setExpandedState(TreePath path, boolean state) {

        // If smartExpand is true, expand only nodes that do not represent 
        // simple JAMSContext objects. Nodes representing subclasses of 
        // JAMSContext will be expanded
        if (smartExpand) {
            JAMSNode node = (JAMSNode) path.getLastPathComponent();
            if (node.getType() == JAMSNode.CONTEXT_NODE) {
                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
                if (cd.getClazz() == JAMSContext.class) {
                    return;
                }
            }
        }
        super.setExpandedState(path, state);
    }

    class ModelLoadException extends Exception {

        private String className,  componentName;

        public ModelLoadException(String className, String componentName) {
            super();
            this.className = className;
            this.componentName = componentName;
        }

        public String getClassName() {
            return className;
        }

        public String getComponentName() {
            return componentName;
        }
    }
}
