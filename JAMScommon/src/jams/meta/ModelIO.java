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
import jams.data.JAMSDataFactory;
import jams.meta.ComponentDescriptor.ComponentField;
import jams.tools.StringTools;
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

    public ModelIO(ClassLoader loader) {
        this.loader = loader;
    }

//    public ModelIO(ModelDescriptor md, JAMSClassLoader loader) {
//        this.md = md;
//        this.loader = loader;
//    }
    public ModelDescriptor loadModel(Document modelDoc) throws JAMSException {

        return getModelDescriptor(modelDoc);

    }

    private ModelDescriptor getModelDescriptor(Document modelDoc) throws JAMSException {

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
        Element dataStoreNode = (Element) docRoot.getElementsByTagName("datastores").item(0);
        if (dataStoreNode != null) {
            md.setDatastores(dataStoreNode);
        }

        //create the tree's root node
        ContextDescriptor cd = new ContextDescriptor(modelName, modelClazz, md);
        JAMSNode rootNode = new JAMSNode(cd);
        rootNode.setType(JAMSNode.MODEL_ROOT);

        md.setRootNode(rootNode);


        //handle all contextcomponent and component nodes

        NodeList childs = docRoot.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                element = (Element) node;

                try {
                    rootNode.add(getSubTree(element, md));
                } catch (ModelLoadException mle) {
                    throw new JAMSException(JAMS.resources.getString("Could_not_load_component_")
                            + mle.getComponentName() + "\" (" + mle.getClassName() + "). "
                            + JAMS.resources.getString("Please_fix_the_model_definition_file!"), JAMS.resources.getString("Error_loading_model"));
//                    this.view.getFrame().dispose();
                }

            } else if (node.getNodeName().equals("attribute")) {
                addContextAttribute(cd, (Element) node);
            } else if (node.getNodeName().equals("var")) {
                element = (Element) node;
                if (element.getAttribute("name").equals("workspaceDirectory")) {
                    md.setWorkspacePath(element.getAttribute("value"));
                }
            }
        }

//        GUI
//        view.getModelEditPanel().updatePanel();

        //handle the launcher node
        Element launcherNode = (Element) docRoot.getElementsByTagName("launcher").item(0);
        if (launcherNode != null) {
            md.setModelParameters(launcherNode);
        }

        return md;
    }

    private JAMSNode getSubTree(Element rootElement, ModelDescriptor md) throws ModelLoadException, JAMSException {

        Class<?> clazz;
        JAMSNode rootNode = null;
        String componentName = "", className = "";

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
            throw new ModelLoadException(className, componentName);
        } catch (NoClassDefFoundError ncdfe) {
            throw new ModelLoadException(className, componentName);
        }

        //JAMSNode rootNode = new JAMSNode(rootElement.getAttribute("name"));

        String type = rootElement.getNodeName();

        if (type.equals("component")) {

            ComponentDescriptor cd = new ComponentDescriptor(componentName, clazz, md);
            rootNode = new JAMSNode(cd);

            rootNode.setType(JAMSNode.COMPONENT_NODE);

            NodeList varChilds = rootElement.getElementsByTagName("var");
            for (int index = 0; index < varChilds.getLength(); index++) {
                setVar(cd, (Element) varChilds.item(index), md);
            }

        } else if (type.equals("contextcomponent")) {

            ContextDescriptor cd = new ContextDescriptor(componentName, clazz, md);
            rootNode = new JAMSNode(cd);

            rootNode.setType(JAMSNode.CONTEXT_NODE);

            NodeList childs = rootElement.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                Node node = childs.item(index);
                if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {

                    JAMSNode childNode = getSubTree((Element) childs.item(index), md);
                    if (childNode != null) {
                        rootNode.add(childNode);
                    }

                } else if (node.getNodeName().equals("var")) {

                    setVar(cd, (Element) node, md);

                } else if (node.getNodeName().equals("attribute")) {

                    addContextAttribute(cd, (Element) node);

                }
            }
        }

        //cd.getUnsetAttributes();

        return rootNode;
    }

    private void setVar(ComponentDescriptor cd, Element e, ModelDescriptor md) throws JAMSException {

        String fieldName = e.getAttribute("name");
        ComponentField field = cd.getComponentFields().get(fieldName);

        if (field == null) {
            throw new JAMSException(JAMS.resources.getString("Error_while_loading_component_") + cd.getName()
                    + JAMS.resources.getString("_component_attribute_") + fieldName + JAMS.resources.getString("_does_not_exist!"), JAMS.resources.getString("Model_loading_error"));
        }

        if (e.hasAttribute("attribute")) {

            String contextName = e.getAttribute("context");
            if (contextName.equals("")) {
                contextName = modelName;
            }

            ContextDescriptor context = (ContextDescriptor) md.getComponentDescriptor(contextName);
            if (context == null) {
                throw new JAMSException(JAMS.resources.getString("Error_while_loading_component_") + cd.getName()
                        + JAMS.resources.getString("_context_") + contextName + JAMS.resources.getString("_does_not_exist!"), JAMS.resources.getString("Model_loading_error"));
            }

            String attribute = e.getAttribute("attribute");

            field.linkToAttribute(context, attribute);
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
            throw new JAMSException("Given type " + typeName + " for context attribute " +
                    attribute + " in context " + cd.getName() + " does not exist!", JAMS.resources.getString("Model_loading_error"));
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
