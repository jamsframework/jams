/*
 * ModelIO.java
 * Created on 16.09.2010, 22:29:03
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMS;
import jams.JAMSException;
import jams.JAMSLogging;
import jams.JAMSVersion;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.io.ParameterProcessor;
import jams.meta.ComponentDescriptor.NullClassException;
import jams.meta.ModelProperties.Group;
import jams.meta.ModelProperties.ModelProperty;
import jams.model.JAMSComponentDescription;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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

    public ModelIO(NodeFactory nodeFactory) {
        JAMSLogging.registerLogger(Logger.getLogger(this.getClass().getName()));
        this.nodeFactory = nodeFactory;
    }

    public static ModelIO getStandardModelIO() {
        return new ModelIO(new NodeFactory() {
            @Override
            public ModelNode createNode(ComponentDescriptor cd) {
                return new ModelNode(cd);
            }
        });
    }

    public ModelDescriptor createModel() {

        ModelDescriptor md = new ModelDescriptor();
        ContextDescriptor cd = new ContextDescriptor(JAMS.i18n("NewModel"), modelClazz, null, md);
        ModelNode rootNode = nodeFactory.createNode(cd);
        rootNode.setType(ModelNode.MODEL_TYPE);
        md.setRootNode(rootNode);

        md.initDatastores(null);

        return md;
    }

    public ModelDescriptor loadModelDescriptor(Document modelDoc, ClassLoader loader, boolean processEditors) {

        this.loader = loader;

        // do some preprocessing on the XML document to be backward compatible
        ParameterProcessor.preProcess(modelDoc);

        ModelDescriptor md = getModelDescriptor(modelDoc, processEditors);
        return md;

    }

    private ModelDescriptor getModelDescriptor(Document modelDoc, boolean processEditors) throws NullClassException {

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

        //create the tree's root node
        ContextDescriptor cd = new ContextDescriptor(modelName, modelClazz, null, md);
        ModelNode rootNode = nodeFactory.createNode(cd);
        rootNode.setType(ModelNode.MODEL_TYPE);

        md.setRootNode(rootNode);

        //handle the workspace node
//        Node workspaceNode = docRoot.getElementsByTagName("var").item(0);
//        if (workspaceNode != null) {
//            try {
//                setVar(cd, (Element) workspaceNode, md, exHandler);
//            } catch (AttributeConfigException ex) {
//                exHandler.handle(ex);
//            }
//        }

        //handle all contextcomponent and component nodes

        NodeList children = docRoot.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            node = children.item(index);
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                element = (Element) node;

                try {
                    rootNode.add(getSubTree(element, md));
                } catch (JAMSException mle) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, mle.getMessage(), mle.getWrappedException());
                }

            } else if (node.getNodeName().equals("attribute")) {

                try {
                    addContextAttribute(cd, (Element) node);
                } catch (JAMSException mle) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, mle.getMessage(), mle.getWrappedException());
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
                md.setModelParameters(launcherNode);
            }
        }

        //handle the datastores node
        Node dataStoreNode = docRoot.getElementsByTagName("datastores").item(0);
        md.initDatastores(dataStoreNode);

        //handle the metaprocessors node
        Node metaProcessorNode = docRoot.getElementsByTagName("preprocessors").item(0);
        md.initPreprocessors(metaProcessorNode);

        return md;
    }

    private ModelNode getSubTree(Element rootElement, ModelDescriptor md) {

        Class<?> clazz = null;
        String componentName = "", className = "", version = "";
        ModelNode rootNode = null;
        boolean enabled = true;


        componentName = rootElement.getAttribute("name");
        className = rootElement.getAttribute("class");

        if (rootElement.hasAttribute("version")) {
            version = rootElement.getAttribute("version");
        } else {
            version = JAMSComponentDescription.DEFAULT_VERSION;
        }

        if (rootElement.hasAttribute("enabled")) {
            enabled = Boolean.parseBoolean(rootElement.getAttribute("enabled"));
        }

        try {

            clazz = loader.loadClass(className);

        } catch (ClassNotFoundException cnfe) {
            throw new JAMSException(JAMS.i18n("Could_not_load_component")
                    + componentName + "\" (" + className + "). "
                    + JAMS.i18n("Please_fix_the_model_definition_file!"), cnfe);
        } catch (NoClassDefFoundError ncdfe) {
            throw new JAMSException(JAMS.i18n("Could_not_load_component")
                    + componentName + "\" (" + className + "). "
                    + JAMS.i18n("Please_fix_the_model_definition_file!"), ncdfe);
        } catch (UnsupportedClassVersionError ucve) {
            throw new JAMSException(MessageFormat.format(JAMS.i18n("ClassVersionErrorWhileLoadingComponentLib"), className), ucve);
        } catch (Throwable t) {
            throw new JAMSException(t.getMessage(), t);
        }

        //ModelNode rootNode = new ModelNode(rootElement.getAttribute("name"));

        String type = rootElement.getNodeName();

        if (type.equals("component")) {

            ComponentDescriptor cd = new ComponentDescriptor(componentName, clazz, version, md);
            cd.setEnabled(enabled);
            rootNode = nodeFactory.createNode(cd);
            rootNode.setType(ModelNode.COMPONENT_TYPE);

            NodeList varChilds = rootElement.getElementsByTagName("var");
            for (int index = 0; index < varChilds.getLength(); index++) {

                try {

                    setVar(cd, (Element) varChilds.item(index), md);

                } catch (JAMSException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex.getWrappedException());
                }
            }

        } else if (type.equals("contextcomponent")) {

            ContextDescriptor cd = new ContextDescriptor(componentName, clazz, version, md);
            cd.setEnabled(enabled);
            rootNode = nodeFactory.createNode(cd);
            rootNode.setType(ModelNode.CONTEXT_TYPE);

            NodeList children = rootElement.getChildNodes();
            for (int index = 0; index < children.getLength(); index++) {
                Node node = children.item(index);

                try {

                    if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {

                        rootNode.add(getSubTree((Element) children.item(index), md));

                    } else if (node.getNodeName().equals("var")) {

                        setVar(cd, (Element) node, md);

                    } else if (node.getNodeName().equals("attribute")) {

                        addContextAttribute(cd, (Element) node);

                    }

                } catch (JAMSException mle) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, mle.getMessage(), mle.getWrappedException());
                }
            }
        }

        //cd.getUnsetAttributes();

        return rootNode;
    }

    private void setVar(ComponentDescriptor cd, Element e, ModelDescriptor md) {

        String fieldName = e.getAttribute("name");
        ComponentField field = cd.getComponentFields().get(fieldName);

        if (field == null) {
            throw new JAMSException(JAMS.i18n("Error_while_loading_component_") + cd.getInstanceName()
                    + JAMS.i18n("_component_attribute_") + fieldName + JAMS.i18n("_does_not_exist!"));
        }

        if (e.hasAttribute("attribute")) {

            String attribute = e.getAttribute("attribute");

            String contextName = e.getAttribute("context");
            if (contextName.equals("")) {
                contextName = modelName;
            }

            ContextDescriptor context = (ContextDescriptor) md.getComponentDescriptor(contextName);
            if (context == null) {
                throw new JAMSException(JAMS.i18n("Error_while_loading_component_") + cd.getInstanceName()
                        + JAMS.i18n("_context_") + contextName + JAMS.i18n("_does_not_exist!"));
            }

            field.linkToAttribute(context, attribute);

            //cd.linkComponentAttribute(name, view.getComponentDescriptor(context), attribute);
                /*            }
             try {
             if (cd.getComponentAttributes().get(name).accessType != ComponentAttribute.READ_ACCESS) {
             Class attributeType = cd.getComponentAttributes().get(name).type;
             context.getDataRepository().addAttribute(new ContextAttribute(attribute, attributeType, context));
             }*/

        } else if (e.hasAttribute("value")) {

            field.setValue(e.getAttribute("value"));

        }
    }

    //add attribute that is defined by a context component
    private void addContextAttribute(ContextDescriptor cd, Element e) {

        String attribute = e.getAttribute("name");
        String typeName = e.getAttribute("class");
        Class type;

        try {
            type = Class.forName(typeName);
        } catch (ClassNotFoundException ex) {
            throw new JAMSException("Given type " + typeName + " for context attribute "
                    + attribute + " in context " + cd.getInstanceName() + " does not exist!", ex);
        }

        // workaround for models that use the "old" API, i.e. JAMSData
        // classes instead of interfaces
        if (!type.isInterface()) {
            type = DefaultDataFactory.getDataFactory().getBelongingInterface(type);
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
                Logger.getLogger(this.getClass().getName()).info("check addContextAttribute for array types!");
                cd.addStaticAttribute(attribute, type, value);
//                    cd.getDataRepository().addAttribute(new ContextAttribute(attribute, type, cd));
            }
        }
    }

    
    public boolean validateModelFile(File f){
        if (f.isDirectory()) {
            return false;
        }
        if (!f.exists()) {
            return false;
        }
        if (!f.getName().endsWith(".xml") && !f.getName().endsWith(".jam")) {
            return false;
        }

        //try to load model file
        ModelIO io = ModelIO.getStandardModelIO();
        Document d = XMLTools.getDocument(f.getAbsolutePath());
        if (d == null) {
            return false;
        }
        //deactive logs
        Filter filterAll = new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                return false;
            }
        };
        Logger.getLogger(ModelIO.class.getName()).setFilter(filterAll);
        Logger.getLogger(jams.meta.ModelDescriptor.class.getName()).setFilter(filterAll);
        
        try {                        
            ModelDescriptor desc = io.loadModelDescriptor(d, null, true);
            if (desc == null) {
                return false;
            }
            if (desc.getModelName() == null || desc.getModelName().isEmpty()) {
                return false;
            }            
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally{
            //reactive logs
            Logger.getLogger(ModelIO.class.getName()).setFilter(null);
            Logger.getLogger(jams.meta.ModelDescriptor.class.getName()).setFilter(null);
        }        
        return true;
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
            rootElement.setAttribute("name", cd.getInstanceName());
            rootElement.setAttribute("author", md.getAuthor());
            rootElement.setAttribute("date", md.getDate());
            rootElement.setAttribute("helpbaseurl", md.getHelpBaseUrl());
            rootElement.setAttribute("version", JAMSVersion.getInstance().getVersionString());

            document.appendChild(rootElement);

            element = (Element) document.createElement("description");
            element.appendChild(document.createCDATASection(md.getDescription()));
            rootElement.appendChild(element);

            element = (Element) document.createElement("var");
            element.setAttribute("name", "workspaceDirectory");
            element.setAttribute("value", md.getWorkspacePath());
            rootElement.appendChild(element);

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
                        }
                        if (modelProperty instanceof Group) {
                            Group subgroup = (Group) modelProperty;
                            Element subgroupElement = (Element) document.createElement("subgroup");
                            subgroupElement.setAttribute("name", subgroup.getCanonicalName());
                            HelpComponent helpComponent = subgroup.getHelpComponent();
                            if (!helpComponent.isEmpty()) {
                                Element helpElement = helpComponent.createDOMElement(document);
                                subgroupElement.appendChild(helpElement);
                            }

                            ArrayList subgroupProperties = subgroup.getProperties();
                            for (int k = 0; k < subgroupProperties.size(); k++) {
                                Object subgroupProperty = subgroupProperties.get(k);

                                if (subgroupProperty instanceof ModelProperty) {
                                    ModelProperty property = (ModelProperty) subgroupProperty;
                                    Element propertyElement = createPropertyElement(document, property);
                                    subgroupElement.appendChild(propertyElement);
                                }
                            }
                            groupElement.appendChild(subgroupElement);
                        }
                    }
                }
                element.appendChild(groupElement);
            }
            rootElement.appendChild(element);

            //create output datastore elements
            element = (Element) document.createElement("datastores");
            for (OutputDSDescriptor ds : md.getDatastores().values()) {
                Document outputDSDoc = ds.createDocument();
                element.appendChild(document.importNode(outputDSDoc.getDocumentElement(), true));
            }
            rootElement.appendChild(element);

            //create metaprocessors
            element = (Element) document.createElement("preprocessors");
            for (MetaProcessorDescriptor mpd : md.getPreprocessors()) {
                Document mpdDoc = mpd.createDocument();
                element.appendChild(document.importNode(mpdDoc.getDocumentElement(), true));
            }
            rootElement.appendChild(element);


//                    //handle the metaprocessors node
//        NodeList metaProcessorNodes = docRoot.getElementsByTagName("metaprocessor");
//        if (metaProcessorNodes != null) {
//            md.setMetaProcessorNodes(metaProcessorNodes);
//        }      

            if (cd instanceof ContextDescriptor) {
                ContextDescriptor context = (ContextDescriptor) cd;

                for (ContextAttribute attribute : context.getStaticAttributes().values()) {
                    element = (Element) document.createElement("attribute");
                    element.setAttribute("name", attribute.getName());
                    element.setAttribute("class", attribute.getType().getName());
                    element.setAttribute("value", attribute.getValue());

                    rootElement.appendChild(element);
                }
            }

            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {

                rootElement.appendChild(getSubDoc((ModelNode) rootNode.getChildAt(i), document));

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
        propertyElement.setAttribute("component", property.component.getInstanceName());
        if (property.var != null) {
            propertyElement.setAttribute("attribute", property.var.getName());
            propertyElement.setAttribute("type", DefaultDataFactory.getDataFactory().getBelongingInterface(property.var.getType()).getSimpleName());
        } else if (property.attribute != null) {
            propertyElement.setAttribute("attribute", property.attribute.getName());
            propertyElement.setAttribute("type", DefaultDataFactory.getDataFactory().getBelongingInterface(property.attribute.getType()).getSimpleName());
        } else {
            propertyElement.setAttribute("attribute", ParameterProcessor.COMPONENT_ENABLE_VALUE);
            propertyElement.setAttribute("type", Attribute.Boolean.class.getSimpleName());
//            propertyElement.setAttribute("value", property.value);
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

        rootElement.setAttribute("name", cd.getInstanceName());
        rootElement.setAttribute("class", cd.getClazz().getName());
        rootElement.setAttribute("enabled", Boolean.toString(cd.isEnabled()));
        rootElement.setAttribute("version", cd.getVersion());

        Element element;

        if (cd instanceof ContextDescriptor) {
            ContextDescriptor context = (ContextDescriptor) cd;

            for (ContextAttribute attribute : context.getStaticAttributes().values()) {

                element = (Element) document.createElement("attribute");
                element.setAttribute("name", attribute.getName());
                element.setAttribute("class", attribute.getType().getName());
                element.setAttribute("value", attribute.getValue());

                rootElement.appendChild(element);
            }
        }

        for (ComponentField var : cd.getComponentFields().values()) {

//            if (!StringTools.isEmptyString(var.getValue()) || ((var.getContext() != null) && !var.getAttribute().equals(""))) {
            if ((var.getValue() != null) || ((var.getContext() != null) && !var.getAttribute().equals(""))) {

                element = document.createElement("var");
                element.setAttribute("name", var.getName());
                if (!var.getAttribute().equals("")) {
                    element.setAttribute("attribute", var.getAttribute());
                    element.setAttribute("context", var.getContext().getInstanceName());
                }
//                if (!StringTools.isEmptyString(var.getValue())) {
                if (var.getValue() != null) {
                    element.setAttribute("value", var.getValue());
                }

                rootElement.appendChild(element);
            }
        }

        if ((rootNode.getType() == ModelNode.CONTEXT_TYPE) || (rootNode.getType() == ModelNode.MODEL_TYPE)) {
            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                rootElement.appendChild(getSubDoc((ModelNode) rootNode.getChildAt(i), document));
            }
        }

        return rootElement;
    }
//    public class AttributeConfigException extends JAMSException {
//
//        public AttributeConfigException(String message, String header) {
//            super(message, header);
//        }
//    }
//    public class ModelLoadException extends JAMSException {
//
//        public ModelLoadException(String message, Throwable wrappedException) {
//            super(message, wrappedException);
//        }
//
//        public ModelLoadException(String message) {
//            super(message);
//        }
//    }
}
