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

package org.unijena.juice.gui.tree;

import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;
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
import org.unijena.jams.gui.LHelper;
import org.unijena.juice.ComponentDescriptor;
import org.unijena.juice.ComponentDescriptor.ComponentAttribute;
import org.unijena.juice.ContextAttribute;
import org.unijena.juice.JUICE;
import org.unijena.juice.ModelProperties;
import org.unijena.juice.ModelProperties.ModelProperty;
import org.unijena.juice.gui.ModelView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author S. Kralisch
 */
public class ModelTree extends JAMSTree {
    
    private static final String NEW_MODEL_NAME = "New Model";
    private static final String MODEL_CLASS_NAME = "org.unijena.jams.model.JAMSModel";
    
    private ModelView view;
    private String modelName = NEW_MODEL_NAME;
    private JPopupMenu popup;
    
    public ModelTree(ModelView view) {
        super();
        setEditable(true);
        
        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.view = view;
        
        updateModelTree();
        
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                displayComponentInfo();
            }
        });
        
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteNode();
            }
        });
        
        JMenuItem moveUpItem = new JMenuItem("Move Up");
        moveUpItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        moveUpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveUpNode();
            }
        });
        JMenuItem moveDownItem = new JMenuItem("Move Down");
        moveDownItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        moveDownItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveDownNode();
            }
        });
        
        popup = new JPopupMenu();
        popup.add(deleteItem);
        popup.add(moveUpItem);
        popup.add(moveDownItem);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    showPopup(evt);
                }
            }
        });
        
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case KeyEvent.VK_DELETE :
                        deleteNode();
                        break;
                    case '-' :
                        moveUpNode();
                        break;
                    case '+' :
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
                        
            int result = LHelper.showYesNoDlg(JUICE.getJuiceFrame(), "Really delete component " + node.getUserObject().toString() + "?", "Deleting component");
            if (result == JOptionPane.YES_OPTION) {
                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();
                view.unRegisterComponentDescriptor(cd);
                node.removeFromParent();
                this.updateUI();
            }
        }
        
        this.setSelectionPath(null);
    }
    
    private void moveUpNode() {
        
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
        
        i = index.length-1;
        for (int k = paths.length-1; k >= 0; k--) {
            
            JAMSNode node = (JAMSNode) paths[k].getLastPathComponent();
            JAMSNode parent = (JAMSNode) node.getParent();
            
            index[i]--;
            j = i-1;
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
            j = i+1;
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
            return;
        }
        
        if (node != null) {
            try {
                Class<?> clazz = ((ComponentDescriptor) node.getUserObject()).getClazz();
                if (clazz != null) {
                    popup.show(this, evt.getX(), evt.getY());
                }
            } catch (ClassCastException cce) {}
        }
    }
    
    
    
    private void displayComponentInfo() {
        
        JAMSNode node =  (JAMSNode) this.getLastSelectedPathComponent();
        if (node != null) {
            view.getCompEditPanel().setComponentDescriptor(node);
        }
    }
    
    //Create a document from the model tree
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
            rootElement.appendChild(document.createTextNode("\n"));
            
            document.appendChild(rootElement);
            
            element = (Element) document.createElement("description");
            element.setTextContent(view.getDescription());
            rootElement.appendChild(element);
            
            rootElement.appendChild(document.createTextNode("\n"));
            
            element = (Element) document.createElement("launcher");
            for (String group : view.getModelProperties().getGroupNames()) {
                Element groupElement = (Element) document.createElement("group");
                groupElement.setAttribute("name", group);
                ArrayList<ModelProperty> properties = view.getModelProperties().getGroup(group).getProperties();
                if (properties != null) {
                    for (ModelProperties.ModelProperty property : properties) {
                        Element propertyElement = (Element) document.createElement("property");
                        propertyElement.setAttribute("component", property.component.getName());
                        if (property.var != null) {
                            propertyElement.setAttribute("attribute", property.var.name);
                            propertyElement.setAttribute("type", property.var.type.getSimpleName());
                        } else if (property.attribute != null) {
                            propertyElement.setAttribute("attribute", property.attribute.getName());
                            propertyElement.setAttribute("type", property.attribute.getType().getSimpleName());
                        } else {
                            propertyElement.setAttribute("attribute", "%enable%");
                            propertyElement.setAttribute("type", JUICE.JAMS_DATA_TYPES[0].getSimpleName());
                        }
                        propertyElement.setAttribute("default", property.defaultValue);
                        propertyElement.setAttribute("description", property.description);
                        propertyElement.setAttribute("name", property.name);
                        propertyElement.setAttribute("value", property.value);
                        propertyElement.setAttribute("range", "" + property.lowerBound + ";" + property.upperBound);
                        if (property.length > 0 )
                            propertyElement.setAttribute("length", "" + property.length);
                        
                        groupElement.appendChild(propertyElement);
                        groupElement.appendChild(document.createTextNode("\n"));
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
    
    private Element getSubDoc(JAMSNode rootNode, Document document) {
        
        Element rootElement = null;
        ComponentDescriptor cd = (ComponentDescriptor) rootNode.getUserObject();
        
        if (rootNode.getType() == JAMSNode.COMPONENT_NODE) {
            rootElement = (Element) document.createElement("component");
        } else if (rootNode.getType() == JAMSNode.CONTEXT_NODE) {
            rootElement = (Element) document.createElement("contextcomponent");
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
        
        if (rootNode.getType() == JAMSNode.CONTEXT_NODE) {
            
            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                
                rootElement.appendChild(getSubDoc((JAMSNode) rootNode.getChildAt(i), document));
                rootElement.appendChild(document.createTextNode("\n"));
                
            }
        }
        
        return rootElement;
    }
    
    public void updateModelTree() {
        
        TreeModel model = new DefaultTreeModel(getModelTree());
/*
        model.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {
            }
            public void treeNodesInserted(TreeModelEvent e) {
                JAMSNode rootNode = (JAMSNode) e.getTreePath().getLastPathComponent();
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    System.out.println(rootNode.getChildAt(i));
                }
            }
            public void treeNodesRemoved(TreeModelEvent e) {
            }
            public void treeStructureChanged(TreeModelEvent e) {
            }
        });
 */
        setModel(model);
        this.setSelectionRow(0);
        this.displayComponentInfo();
        this.expandAll();
        
    }
    
    private JAMSNode getModelTree() {
        
        Node node;
        Element element, docRoot;
        Document modelDoc = view.getModelDoc();
        Class<?> clazz;
        
        try {
            clazz  = JUICE.getLoader().loadClass(MODEL_CLASS_NAME);
        } catch (ClassNotFoundException cnfe) {
            clazz = null;
        }
        
        if (modelDoc == null) {
            ComponentDescriptor cd = new ComponentDescriptor(NEW_MODEL_NAME, clazz, this);
            JAMSNode rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.MODEL_ROOT);
            return rootNode;
        }
        
        //get model name, description, author and date
        
        docRoot = modelDoc.getDocumentElement();
        modelName = docRoot.getAttribute("name");
        
        view.setAuthor(docRoot.getAttribute("author"));
        view.setDate(docRoot.getAttribute("date"));
        
        Node descriptionNode = docRoot.getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            view.setDescription(descriptionNode.getTextContent());
            view.getModelEditPanel().update();
        }
        
        //create the tree's root node
        
        ComponentDescriptor cd = new ComponentDescriptor(modelName, clazz, this);
        JAMSNode rootNode = new JAMSNode(cd);
        rootNode.setType(JAMSNode.MODEL_ROOT);
        
        
        //handle all contextcomponent and component nodes
        
        NodeList childs = docRoot.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName() == "contextcomponent" || node.getNodeName() == "component") {
                element = (Element) node;
                
                try {
                    rootNode.add(getSubTree(element));
                } catch (ModelLoadException mle) {
                    LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Could not load component \"" +
                            mle.getComponentName() + "\" (" + mle.getClassName() + "). " +
                            "Please fix the model definition file!", "Error loading model");
                    this.view.getFrame().dispose();
                    return null;
                }
                
            } else if (node.getNodeName() == "attribute") {
                addContextAttribute(cd, (Element) node);
            }
        }
        
        //handle the launcher node
        
        Element launcherNode = (Element) docRoot.getElementsByTagName("launcher").item(0);
        if (launcherNode != null) {
            NodeList groupNodes = launcherNode.getElementsByTagName("group");
            for (int gindex = 0; gindex < groupNodes.getLength(); gindex++) {
                node = groupNodes.item(gindex);
                Element groupElement = (Element) node;
                String group = groupElement.getAttribute("name");
                view.getModelProperties().addGroup(group);
                
                NodeList propertyNodes = groupElement.getElementsByTagName("property");
                for (int pindex = 0; pindex < propertyNodes.getLength(); pindex++) {
                    node = propertyNodes.item(pindex);
                    Element propertyElement = (Element) node;
                    
                    ModelProperties.ModelProperty property = view.getModelProperties().createProperty();
                    property.component = view.getComponentDescriptor(propertyElement.getAttribute("component"));
                    
                    if (property.component == null) {
                        LHelper.showErrorDlg(JUICE.getJuiceFrame(), "Component \"" + propertyElement.getAttribute("component") +
                                "\" does not exist, but is referred in list of model parameters!\n" +
                                "Will be removed when model is saved!", "Model loading error");
                    } else {
                        
                        String attributeName = propertyElement.getAttribute("attribute");
                        
                        property.var = property.component.getComponentAttributes().get(attributeName);
                        
                        //in case this is a context component, check whether this refers to a context attribute
                        if (property.attribute == null) {
                            property.attribute = property.component.getContextAttributes().get(attributeName);
                        }
                        
                        //check wether the referred var is existing or not
                        if ((property.attribute == null) && (property.var == null) && !attributeName.equals("%enable%")) {
                            LHelper.showErrorDlg(JUICE.getJuiceFrame(),  "Attribute " + attributeName +
                                    " does not exist in component " + property.component.getName() +
                                    ". Removing associated property!", "Model loading error");
                            continue;
                        }
                        
                        property.defaultValue = propertyElement.getAttribute("default");
                        property.description = propertyElement.getAttribute("description");
                        property.name = propertyElement.getAttribute("name");
                        property.value = propertyElement.getAttribute("value");
                        String range = propertyElement.getAttribute("range");
                        StringTokenizer tok = new StringTokenizer(range, ";");
                        if (tok.countTokens() == 2) {
                            property.lowerBound = Double.parseDouble(tok.nextToken());
                            property.upperBound = Double.parseDouble(tok.nextToken());
                        }
                        String lenStr = propertyElement.getAttribute("length");
                        if (lenStr != null && lenStr.length()>0)
                            property.length = Integer.parseInt(lenStr);

                        view.getModelProperties().addProperty(view.getModelProperties().getGroup(group), property);
                    }
                }
            }
        }
        
        return rootNode;
    }
    
    private JAMSNode getSubTree(Element rootElement) throws ModelLoadException {
        
        Class<?> clazz;
        ComponentDescriptor cd = null;
        String componentName = "", className = "";
        
        try {
            
            componentName = rootElement.getAttribute("name");
            className = rootElement.getAttribute("class");
            
            clazz  = JUICE.getLoader().loadClass(className);
            
            cd = new ComponentDescriptor(componentName, clazz, this);
            
        } catch (ClassNotFoundException cnfe) {
            throw new ModelLoadException(className, componentName);
        } catch (NoClassDefFoundError ncdfe) {
            throw new ModelLoadException(className, componentName);
        }
        
        //JAMSNode rootNode = new JAMSNode(rootElement.getAttribute("name"));
        JAMSNode rootNode = null;
        
        String type = rootElement.getNodeName();
        
        if (type == "component") {
            
            rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.COMPONENT_NODE);
            
            NodeList varChilds = rootElement.getElementsByTagName("var");
            for (int index = 0; index < varChilds.getLength(); index++) {
                setVar(cd, (Element) varChilds.item(index));
            }
            
        } else if (type == "contextcomponent") {
            
            rootNode = new JAMSNode(cd);
            rootNode.setType(JAMSNode.CONTEXT_NODE);
            
            NodeList childs = rootElement.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                Node node = childs.item(index);
                if (node.getNodeName() == "contextcomponent" || node.getNodeName() == "component") {
                    
                    JAMSNode childNode = getSubTree((Element) childs.item(index));
                    if (childNode != null) {
                        rootNode.add(childNode);
                    }
                    
                } else if (node.getNodeName() == "var") {
                    
                    setVar(cd, (Element) node);
                    
                } else if (node.getNodeName() == "attribute") {
                    
                    addContextAttribute(cd, (Element) node);
                    
                }
            }
        }
        
        //cd.getUnsetAttributes();
        
        return rootNode;
    }
    
    private void addContextAttribute(ComponentDescriptor cd, Element e) {
        try {
            String attribute = e.getAttribute("name");
            String typeName = e.getAttribute("class");
            Class type = Class.forName(typeName);
            String value = e.getAttribute("value");
            cd.addContextAttribute(attribute, type, value);
            cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    private void setVar(ComponentDescriptor cd, Element e) {
        
        if (e.hasAttribute("attribute")) {
            
            String context = e.getAttribute("context");
            if (context.equals("")) {
                context = modelName;
            }
            
            String name = e.getAttribute("name");
            String attribute = e.getAttribute("attribute");
            
            try {
                ComponentDescriptor.ComponentAttribute ca = cd.getComponentAttributes().get(name);
                ca.linkToAttribute(view.getComponentDescriptor(context), attribute);
                //cd.linkComponentAttribute(name, view.getComponentDescriptor(context), attribute);
            } catch (NullPointerException ex) {
                LHelper.showErrorDlg(this.view.getFrame(), "Error while loading component \"" + cd.getName() +
                        "\": context \"" + context + "\" does not exist!", "Model loading error");
                return;
            }
            try {
                if (cd.getComponentAttributes().get(name).accessType != ComponentAttribute.READ_ACCESS) {
                    Class attributeType = cd.getComponentAttributes().get(name).type;
                    view.getComponentDescriptor(context).getDataRepository().addAttribute(new ContextAttribute(attribute, attributeType, view.getComponentDescriptor(context)));
                }
            } catch (NullPointerException ex) {
                LHelper.showErrorDlg(this.view.getFrame(), "Error while loading component \"" + cd.getName() +
                        "\": component attribute \"" + e.getAttribute("name") + "\" does not exist!", "Model loading error");
                return;
            }
            
        } 
        if (e.hasAttribute("value")) {
            
            //cd.setComponentAttribute(e.getAttribute("name"), e.getAttribute("value"));
            cd.getComponentAttributes().get(e.getAttribute("name")).setValue(e.getAttribute("value"));
            
        }
    }
    
    public ModelView getView() {
        return view;
    }
    
    class ModelLoadException extends Exception {
        
        private String className, componentName;
        
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
