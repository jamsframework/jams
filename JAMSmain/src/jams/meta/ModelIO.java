/*
 * Context.java
 * Created on 16.09.2010, 22:29:03
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMS;
import jams.JAMSException;
import jams.ExceptionHandler;
import jams.JAMSVersion;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.io.ParameterProcessor;
import jams.meta.ComponentDescriptor.NullClassException;
import jams.meta.ComponentField.AttributeLinkException;
import jams.meta.ModelProperties.Group;
import jams.meta.ModelProperties.ModelProperty;
import jams.tools.StringTools;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ModelIO {

    private static final Class modelClazz = jams.model.JAMSModel.class;
    private ClassLoader loader;
    private String modelName;
    private NodeFactory nodeFactory;

    public ModelIO(ClassLoader loader, NodeFactory nodeFactory) {
        this.loader = loader;
        this.nodeFactory = nodeFactory;
    }

//    public ModelIO(ModelDescriptor md, JAMSClassLoader loader) {
//        this.md = md;
//        this.loader = loader;
//    }
    public ModelDescriptor createModel(ExceptionHandler exHandler) throws JAMSException {

        ModelDescriptor md = new ModelDescriptor();
        ContextDescriptor cd = new ContextDescriptor(JAMS.i18n("New_Model"), modelClazz, md, exHandler);
        ModelNode rootNode = nodeFactory.createNode(cd);
        rootNode.setType(ModelNode.MODEL_TYPE);
        md.setRootNode(rootNode);

        md.initDatastores(exHandler);

        return md;
    }

    public ModelDescriptor loadModel(Document modelDoc, boolean processEditors, ExceptionHandler exHandler) throws JAMSException {

        return getModelDescriptor(modelDoc, processEditors, exHandler);

    }

    private ModelDescriptor getModelDescriptor(Document modelDoc, boolean processEditors, ExceptionHandler exHandler) throws NullClassException {

        Node node;
        Element element, docRoot;

        ModelDescriptor md = new ModelDescriptor();

        //get model name, description, author and date

        docRoot = modelDoc.getDocumentElement();
        modelName = docRoot.getAttribute("name");
        md.setAuthor(docRoot.getAttribute("author"));
        md.setDate(docRoot.getAttribute("date"));
        md.setHelpBaseUrl(docRoot.getAttribute("helpbaseurl"));

        //handle the description node
        Node descriptionNode = docRoot.getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            md.setDescription(descriptionNode.getTextContent().trim());
        }

        //handle the datastores node
        Node dataStoreNode = docRoot.getElementsByTagName("datastores").item(0);
        if (dataStoreNode != null) {
            md.setDataStoresNode(dataStoreNode);
        }

        //create the tree's root node
        ContextDescriptor cd = new ContextDescriptor(modelName, modelClazz, md, exHandler);
        ModelNode rootNode = nodeFactory.createNode(cd);
        rootNode.setType(ModelNode.MODEL_TYPE);

        md.setRootNode(rootNode);

        //handle the workspace node
        Node workspaceNode = docRoot.getElementsByTagName("var").item(0);
        if (workspaceNode != null) {
            try {
                setVar(cd, (Element) workspaceNode, md, exHandler);
            } catch (NullFieldException ex) {
                exHandler.handle(ex);
            }
        }

        //handle all contextcomponent and component nodes

        NodeList childs = docRoot.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                element = (Element) node;

                try {
                    rootNode.add(getSubTree(element, md, exHandler));
                } catch (ModelLoadException mle) {
                    exHandler.handle(mle);
                } catch (NullClassException nfe) {
                    exHandler.handle(nfe);
                }

            } else if (node.getNodeName().equals("attribute")) {

                try {
                    addContextAttribute(cd, (Element) node);
                } catch (JAMSException jex) {
                    exHandler.handle(jex);
                }

            } else if (node.getNodeName().equals("var")) {
                element = (Element) node;
                if (element.getAttribute("name").equals("workspaceDirectory")) {
                    md.setWorkspacePath(element.getAttribute("value"));
                }
            }
        }

//        GUI
//        view.getModelEditPanel().updatePanel();

        if (processEditors) {

            //handle the launcher node
            Element launcherNode = (Element) docRoot.getElementsByTagName("launcher").item(0);
            if (launcherNode != null) {
                md.setModelParameters(launcherNode, exHandler);
            }
        }
        
        md.initDatastores(exHandler);

        return md;
    }

    private ModelNode getSubTree(Element rootElement, ModelDescriptor md, ExceptionHandler exHandler) throws ModelLoadException, NullClassException {

        Class<?> clazz;
        String componentName = "", className = "";
        ModelNode rootNode = null;

        try {

            componentName = rootElement.getAttribute("name");

            className = rootElement.getAttribute("class");
            clazz = loader.loadClass(className);

//            GUI
//            cd.addObserver(new Observer() {
//
//                public void update(Observable o, Object arg) {
//                    ModelTree.this.updateUI();
//                }
//            });

        } catch (ClassNotFoundException cnfe) {
            throw new ModelLoadException(JAMS.i18n("Could_not_load_component_")
                    + componentName + "\" (" + className + "). "
                    + JAMS.i18n("Please_fix_the_model_definition_file!"), JAMS.i18n("Error_loading_model"));
        } catch (NoClassDefFoundError ncdfe) {
            throw new ModelLoadException(JAMS.i18n("Could_not_load_component_")
                    + componentName + "\" (" + className + "). "
                    + JAMS.i18n("Please_fix_the_model_definition_file!"), JAMS.i18n("Error_loading_model"));
        }

        //ModelNode rootNode = new ModelNode(rootElement.getAttribute("name"));

        String type = rootElement.getNodeName();

        if (type.equals("component")) {

            ComponentDescriptor cd = new ComponentDescriptor(componentName, clazz, md, exHandler);
            rootNode = nodeFactory.createNode(cd);
            rootNode.setType(ModelNode.COMPONENT_TYPE);

            NodeList varChilds = rootElement.getElementsByTagName("var");
            for (int index = 0; index < varChilds.getLength(); index++) {

                try {
                    setVar(cd, (Element) varChilds.item(index), md, exHandler);
                } catch (NullFieldException ex) {
                    exHandler.handle(ex);
                }

            }

        } else if (type.equals("contextcomponent")) {

            ContextDescriptor cd = new ContextDescriptor(componentName, clazz, md, exHandler);
            rootNode = nodeFactory.createNode(cd);
            rootNode.setType(ModelNode.CONTEXT_TYPE);

            NodeList children = rootElement.getChildNodes();
            for (int index = 0; index < children.getLength(); index++) {
                Node node = children.item(index);
                if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {

                    try {
                        ModelNode childNode = getSubTree((Element) children.item(index), md, exHandler);
                        if (childNode != null) {
                            rootNode.add(childNode);
                        }
                    } catch (ModelLoadException mle) {
                        exHandler.handle(mle);
                    } catch (NullClassException nfe) {
                        exHandler.handle(nfe);
                    }

                } else if (node.getNodeName().equals("var")) {

                    try {
                        setVar(cd, (Element) node, md, exHandler);
                    } catch (NullFieldException ex) {
                        exHandler.handle(ex);
                    }

                } else if (node.getNodeName().equals("attribute")) {

                    try {
                        addContextAttribute(cd, (Element) node);
                    } catch (JAMSException ex) {
                        exHandler.handle(ex);
                    }

                }
            }
        }

        //cd.getUnsetAttributes();

        return rootNode;
    }

    private void setVar(ComponentDescriptor cd, Element e, ModelDescriptor md, ExceptionHandler exHandler) throws NullFieldException {

        String fieldName = e.getAttribute("name");
        ComponentField field = cd.getComponentFields().get(fieldName);

        if (field == null) {
            throw new NullFieldException(JAMS.i18n("Error_while_loading_component_") + cd.getName()
                    + JAMS.i18n("_component_attribute_") + fieldName + JAMS.i18n("_does_not_exist!"), JAMS.i18n("Model_loading_error"));
        }

        if (e.hasAttribute("attribute")) {

            String contextName = e.getAttribute("context");
            if (contextName.equals("")) {
                contextName = modelName;
            }

            ContextDescriptor context = (ContextDescriptor) md.getComponentDescriptor(contextName);
            if (context == null) {
                throw new NullFieldException(JAMS.i18n("Error_while_loading_component_") + cd.getName()
                        + JAMS.i18n("_context_") + contextName + JAMS.i18n("_does_not_exist!"), JAMS.i18n("Model_loading_error"));
            }

            String attribute = e.getAttribute("attribute");
            try {
                field.linkToAttribute(context, attribute);
            } catch (AttributeLinkException ex) {
                exHandler.handle(ex);
            }
            //cd.linkComponentAttribute(name, view.getComponentDescriptor(context), attribute);
                /*            }
            try {
            if (cd.getComponentAttributes().get(name).accessType != ComponentAttribute.READ_ACCESS) {
            Class attributeType = cd.getComponentAttributes().get(name).type;
            context.getDataRepository().addAttribute(new ContextAttribute(attribute, attributeType, context));
            }*/

        }
        if (e.hasAttribute("value")) {

            field.setValue(e.getAttribute("value"));

        }
    }

    //add attribute that is defined by a context component
    private void addContextAttribute(ContextDescriptor cd, Element e) throws JAMSException {

        String attribute = e.getAttribute("name");
        String typeName = e.getAttribute("class");
        Class type;

        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException ex) {
            throw new JAMSException("Given type " + typeName + " for context attribute "
                    + attribute + " in context " + cd.getName() + " does not exist!", JAMS.i18n("Model_loading_error"));
        }

        // workaround for models that use the "old" API, i.e. JAMSData
        // classes instead of interfaces
        if (!type.isInterface()) {
            type = JAMSDataFactory.getBelongingInterface(type);
        }

        if (!type.isArray()) {
            // if the type is not an array, simply create a context attribute
            // and add it to the repository
            String value = e.getAttribute("value");
            cd.addStaticAttribute(attribute, type, value);
//                cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
        } else {
            // if it is an array, tokenize the attribute string (semicolon-separated)
            // and do the above for every token
            String[] values = StringTools.toArray(attribute, ";");
            for (String value : values) {
                System.out.println("check addContextAttribute for array types!");
                cd.addStaticAttribute(attribute, type, value);
//                    cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
            }
        }
    }

    // Create a XML document from the model tree
    public Document getModelDocument(ModelDescriptor md) {

        Document document = null;
        Element element;

        ModelNode rootNode = md.getRootNode();

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
            rootElement.setAttribute("author", md.getAuthor());
            rootElement.setAttribute("date", md.getDate());
            rootElement.setAttribute("helpbaseurl", md.getHelpBaseUrl());
            rootElement.setAttribute("version", JAMSVersion.getInstance().getVersionString());

            rootElement.appendChild(document.createTextNode("\n"));

            document.appendChild(rootElement);

            element = (Element) document.createElement("description");
            element.appendChild(document.createCDATASection(md.getDescription()));
            rootElement.appendChild(element);

            element = (Element) document.createElement("var");
            element.setAttribute("name", "workspaceDirectory");
            element.setAttribute("value", md.getWorkspacePath());
            rootElement.appendChild(element);

            rootElement.appendChild(document.createTextNode("\n"));

            element = (Element) document.createElement("launcher");
            for (String group : md.getModelProperties().getGroupNames()) {
                Element groupElement = (Element) document.createElement("group");
                groupElement.setAttribute("name", group);
                ArrayList properties = md.getModelProperties().getGroup(group).getProperties();
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

                            ArrayList subgroupProperties = subgroup.getProperties();
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

            //create output datastore elements
            element = (Element) document.createElement("datastores");
            for (OutputDSDescriptor ds : md.getDatastores().values()) {
                Document outputDSDoc = ds.createDocument();
                element.appendChild(document.importNode(outputDSDoc.getDocumentElement(), true));
                element.appendChild(document.createTextNode("\n"));
            }
            rootElement.appendChild(element);
            rootElement.appendChild(document.createTextNode("\n"));

            if (cd instanceof ContextDescriptor) {
                ContextDescriptor context = (ContextDescriptor) cd;

                for (ContextAttribute attribute : context.getStaticAttributes().values()) {
                    element = (Element) document.createElement("attribute");
                    element.setAttribute("name", attribute.getName());
                    element.setAttribute("class", attribute.getType().getName());
                    element.setAttribute("value", attribute.getValue());

                    rootElement.appendChild(element);
                    rootElement.appendChild(document.createTextNode("\n"));
                }
            }

            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {

                rootElement.appendChild(getSubDoc((ModelNode) rootNode.getChildAt(i), document));
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
            propertyElement.setAttribute("attribute", property.var.getName());
            propertyElement.setAttribute("type", property.var.getType().getSimpleName());
        } else if (property.attribute != null) {
            propertyElement.setAttribute("attribute", property.attribute.getName());
            propertyElement.setAttribute("type", property.attribute.getType().getSimpleName());
        } else {
            propertyElement.setAttribute("attribute", ParameterProcessor.COMPONENT_ENABLE_VALUE);
            propertyElement.setAttribute("type", Attribute.Boolean.class.getSimpleName());
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
    // whose root node is a given ModelNode
    private Element getSubDoc(ModelNode rootNode, Document document) {

        Element rootElement = null;
        ComponentDescriptor cd = (ComponentDescriptor) rootNode.getUserObject();

        switch (rootNode.getType()) {
            case ModelNode.COMPONENT_TYPE:
                rootElement = (Element) document.createElement("component");
                break;
            case ModelNode.CONTEXT_TYPE:
                rootElement = (Element) document.createElement("contextcomponent");
                break;
            case ModelNode.MODEL_TYPE:
                rootElement = (Element) document.createElement("contextcomponent");
                cd.setClazz(jams.model.JAMSContext.class);
        }

        rootElement.setAttribute("name", cd.getName());
        rootElement.setAttribute("class", cd.getClazz().getName());
        rootElement.appendChild(document.createTextNode("\n"));

        Element element;

        if (cd instanceof ContextDescriptor) {
            ContextDescriptor context = (ContextDescriptor) cd;

            for (ContextAttribute attribute : context.getStaticAttributes().values()) {

                rootElement.appendChild(document.createTextNode("\n"));

                element = (Element) document.createElement("attribute");
                element.setAttribute("name", attribute.getName());
                element.setAttribute("class", attribute.getType().getName());
                element.setAttribute("value", attribute.getValue());

                rootElement.appendChild(element);
            }
        }

        for (ComponentField var : cd.getComponentFields().values()) {

            if (!StringTools.isEmptyString(var.getValue()) || ((var.getContext() != null) && !var.getAttribute().equals(""))) {

                element = document.createElement("var");
                element.setAttribute("name", var.getName());
                if (!var.getAttribute().equals("")) {
                    element.setAttribute("attribute", var.getAttribute());
                    element.setAttribute("context", var.getContext().getName());
                }
                if (!StringTools.isEmptyString(var.getValue())) {
                    element.setAttribute("value", var.getValue());
                }

                rootElement.appendChild(element);
                rootElement.appendChild(document.createTextNode("\n"));

            }
        }

        if ((rootNode.getType() == ModelNode.CONTEXT_TYPE) || (rootNode.getType() == ModelNode.MODEL_TYPE)) {
            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                rootElement.appendChild(getSubDoc((ModelNode) rootNode.getChildAt(i), document));
                rootElement.appendChild(document.createTextNode("\n"));
            }
        }

        return rootElement;
    }

    public class NullFieldException extends JAMSException {

        public NullFieldException(String message, String header) {
            super(message, header);
        }
    }

    public class ModelLoadException extends JAMSException {

        public ModelLoadException(String message, String header) {
            super(message, header);
        }
    }
}
