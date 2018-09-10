/*
 * ModelDescriptor.java
 * Created on 23.06.2010, 16:39:22
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
import jams.io.ParameterProcessor;
import jams.meta.ModelProperties.Group;
import jams.meta.ModelProperties.ModelElement;
import jams.meta.ModelProperties.ModelProperty;
import jams.runtime.JAMSRuntime;
import jams.tools.StringTools;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ModelDescriptor extends ComponentCollection {

    private HashMap<String, OutputDSDescriptor> outputDataStores;
    private ArrayList<MetaProcessorDescriptor> preprocessors = new ArrayList();
    private HashMap<String, AttributeList> attributeLists = new HashMap();
    private ModelProperties modelProperties;
    private String author = "", date = "", description = "", helpBaseUrl = "", workspacePath = "", modelFilePath = "", modelName = "";
    private ModelNode rootNode;

    public ModelDescriptor() {
        JAMSLogging.registerLogger(JAMSLogging.LogOption.CollectAndShow,
                Logger.getLogger(this.getClass().getName()));
        modelProperties = new ModelProperties();
    }

    public ModelDescriptor(ModelDescriptor md) {
        JAMSLogging.registerLogger(JAMSLogging.LogOption.CollectAndShow,
                Logger.getLogger(this.getClass().getName()));
        this.author = md.author; //is deep copy
        this.date = md.date;
        this.description = md.description;
        this.helpBaseUrl = md.helpBaseUrl;
        this.modelName = md.modelName;
        this.workspacePath = md.workspacePath;
        this.modelFilePath = md.modelFilePath;

        //are those safe??
        this.modelProperties = md.getModelProperties();
        this.outputDataStores = (HashMap<String, OutputDSDescriptor>) md.getDatastores().clone();
        this.preprocessors = (ArrayList<MetaProcessorDescriptor>) md.getPreprocessors().clone();
        this.rootNode = md.getRootNode().clone(this, true, new HashMap<ContextDescriptor, ContextDescriptor>());
    }   

    /*
     * Create a new name for a component instance. If possible, use the given
     * name, else add a suffix in order to create a unique one.
     */
    @Override
    public String createComponentInstanceName(String name) {

        Set<String> names = getComponentDescriptors().keySet();

        if (!names.contains(name)) {
            return name;
        }

        String[] sArray = StringTools.toArray(name, "_");
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

    public void addOutputDataStore(OutputDSDescriptor dataStore) {
        outputDataStores.put(dataStore.getName(), dataStore);
    }

    public void removeOutputDataStore(OutputDSDescriptor dataStore) {
        outputDataStores.remove(dataStore.getName());
    }

    public void initPreprocessors(Node preprocessorNode) {

        preprocessors = new ArrayList();

        if (preprocessorNode == null) {
            return;
        }

        NodeList nodes = ((Element) preprocessorNode).getElementsByTagName("metaprocessor");

        for (int i = 0; i < nodes.getLength(); i++) {

            Element processorElement = (Element) nodes.item(i);

            ComponentDescriptor context = getComponentDescriptor(processorElement.getAttribute("context"));

            if ((context == null) || !(context instanceof ContextDescriptor)) {
                continue;
            }

            HashMap<String, String> properties = new HashMap();

            NodeList propertyNodes = processorElement.getElementsByTagName("property");
            for (int j = 0; j < propertyNodes.getLength(); j++) {
                Element attributeElement = (Element) propertyNodes.item(j);
                properties.put(attributeElement.getAttribute("name"), attributeElement.getAttribute("value"));
            }

            String className = processorElement.getAttribute("class");
            boolean enabled = Boolean.parseBoolean(processorElement.getAttribute("enabled"));

            MetaProcessorDescriptor mpd = new MetaProcessorDescriptor(className, (ContextDescriptor) context, enabled);
            mpd.setProperties(properties);
            preprocessors.add(mpd);

        }
    }

    public void initDatastores(Node dataStoresNode) {

        outputDataStores = new HashMap<String, OutputDSDescriptor>();

        if (dataStoresNode == null) {
            return;
        }

        NodeList nodes = ((Element) dataStoresNode).getElementsByTagName("outputdatastore");

        for (int i = 0; i < nodes.getLength(); i++) {

            Element e = (Element) nodes.item(i);

            ContextDescriptor context = (ContextDescriptor) getComponentDescriptor(e.getAttribute("context"));

            if (context == null) {
                Logger.getLogger(this.getClass().getName()).warning(MessageFormat.format(JAMS.i18n("Context_does_not_exist"), e.getAttribute("context")));
                continue;
            }

            String name = e.getAttribute("name");
            boolean enabled = Boolean.parseBoolean(e.getAttribute("enabled"));
            OutputDSDescriptor od = new OutputDSDescriptor(context);
            od.setName(name);
            od.setEnabled(enabled);

            // fill the contextAttributes
            ArrayList<ContextAttribute> contextAttributes = od.getContextAttributes();
            NodeList attributeNodes = e.getElementsByTagName("attribute");
            for (int j = 0; j < attributeNodes.getLength(); j++) {

                Element attributeElement = (Element) attributeNodes.item(j);
                String attributeName = attributeElement.getAttribute("id");
                ContextAttribute ca = context.getDynamicAttributes().get(attributeName);
                if (ca == null) {
                    ca = context.getStaticAttributes().get(attributeName);
                }
                if (ca == null) {
                    Logger.getLogger(this.getClass().getName()).warning(MessageFormat.format(JAMS.i18n("Attribute_does_not_exist_and_is_removed"),
                            attributeName, od.getName()));
                } else {
                    contextAttributes.add(ca);
                }
            }

            NodeList filterNodes = e.getElementsByTagName("filter");
            for (int j = 0; j < filterNodes.getLength(); j++) {
                Element filterElement = (Element) filterNodes.item(j);
                String expression = filterElement.getAttribute("expression");
                ContextDescriptor filterContext = (ContextDescriptor) getComponentDescriptor(filterElement.getAttribute("context"));
                od.addFilter(filterContext, expression);
            }
            this.outputDataStores.put(od.getName(), od);
        }
    }

    public HashMap<String, OutputDSDescriptor> getDatastores() {
        return this.outputDataStores;
    }

    public void setModelParameters(Element launcherNode) {
        Node node;

        ArrayList<JAMSException> exceptions = new ArrayList<JAMSException>();
        ModelProperties mProp = getModelProperties();

        mProp.removeAll();
        NodeList groupNodes = launcherNode.getElementsByTagName("group");
        for (int gindex = 0; gindex < groupNodes.getLength(); gindex++) {
            node = groupNodes.item(gindex);
            Element groupElement = (Element) node;
            String groupName = groupElement.getAttribute("name");
            mProp.addGroup(groupName);
            Group group = mProp.getGroup(groupName);

            // @todo subgroups and properties recursive
            NodeList groupChildNodes = groupElement.getChildNodes();
            for (int pindex = 0; pindex < groupChildNodes.getLength(); pindex++) {
                node = groupChildNodes.item(pindex);
                if (node.getNodeName().equalsIgnoreCase("property")) {
                    Element propertyElement = (Element) node;
                    try {
                        ModelProperty property = getPropertyFromElement(propertyElement, mProp);
                        mProp.addProperty(group, property);
                    } catch (JAMSException je) {
                        exceptions.add(je);
                        Logger.getLogger(this.getClass().getName()).warning(je.getMessage());
                    }
                }
                if (node.getNodeName().equalsIgnoreCase("subgroup")) {
                    Element subgroupElement = (Element) node;
                    String subgroupName = subgroupElement.getAttribute("name");
                    Group subgroup = mProp.createSubgroup(group, subgroupName);
                    setHelpComponent(subgroupElement, subgroup);

                    NodeList propertyNodes = subgroupElement.getElementsByTagName("property");
                    for (int kindex = 0; kindex < propertyNodes.getLength(); kindex++) {
                        Element propertyElement = (Element) propertyNodes.item(kindex);
                        try {
                            ModelProperty property = getPropertyFromElement(propertyElement, mProp);
                            mProp.addProperty(subgroup, property);
                        } catch (JAMSException je) {
                            exceptions.add(je);
                            Logger.getLogger(this.getClass().getName()).warning(je.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void setHelpComponent(Element theElement, ModelElement theModelElement) throws DOMException {
        // get help component from help node
        HelpComponent helpComponent = new HelpComponent(theElement);
        theModelElement.setHelpComponent(helpComponent);
    }

    private ModelProperty getPropertyFromElement(Element propertyElement, ModelProperties mProp) {
        ModelProperties.ModelProperty property = mProp.createProperty();
        property.component = getComponentDescriptor(propertyElement.getAttribute("component"));

        if (property.component == null) {

            throw new JAMSException(JAMS.i18n("Component_") + propertyElement.getAttribute("component")
                    + JAMS.i18n("_does_not_exist,_but_is_referred_in_list_of_model_parameters!")
                    + JAMS.i18n("Will_be_removed_when_model_is_saved!"), JAMS.i18n("Error_loading_model"));
        }

        String attributeName = propertyElement.getAttribute("attribute");
        if (!attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {
//            property.value = propertyElement.getAttribute("value");
//        } else {
            // could refer to a component var or a context attribute
            // only one of them will be != null
            property.var = property.component.getComponentFields().get(attributeName);

            if (property.component instanceof ContextDescriptor) {
                property.attribute = ((ContextDescriptor) property.component).getStaticAttributes().get(attributeName);
            }

        }
        /*
         * if (attributeName.equals("workspace") &&
         * (property.component.getClazz() == JAMSModel.class)) { property.var =
         * property.component.createComponentAttribute(attributeName,
         * JAMSDirName.class,
         * ComponentDescriptor.ComponentAttribute.READ_ACCESS); }
         */
        //check wether the referred parameter is existing or not
        if ((property.attribute == null) && (property.var == null)
                && !attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {

            throw new JAMSException(JAMS.i18n("Attribute_") + attributeName
                    + JAMS.i18n("_does_not_exist_in_component_") + property.component.getInstanceName()
                    + JAMS.i18n("._Removing_visual_editor!"), JAMS.i18n("Error_loading_model"));
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

    public ModelProperties getModelProperties() {
        return modelProperties;
    }

    public void setModelProperties(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

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

    public String getWorkspacePath() {
        return workspacePath;
    }

    public void setWorkspacePath(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }
    
    /**
     * @return the rootNode
     */
    public ModelNode getRootNode() {
        return rootNode;
    }

    /**
     * @param rootNode the rootNode to set
     */
    public void setRootNode(ModelNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * @return the modelName
     */
    public String getModelName() {
        return ((ComponentDescriptor) rootNode.getUserObject()).getInstanceName();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ComponentField> getParameterFields() {

        ArrayList<ComponentField> fields = new ArrayList<ComponentField>();

        Enumeration nodes = rootNode.breadthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            ComponentDescriptor cd = (ComponentDescriptor) ((ModelNode) nodes.nextElement()).getUserObject();
            fields.addAll(cd.getParameterFields());
        }

        return fields;
    }

    /**
     * @return the preprocessors
     */
    public ArrayList<MetaProcessorDescriptor> getPreprocessors() {
        return preprocessors;
    }

    public void metaProcess(JAMSRuntime rt) {

        boolean metaProcessorActive = false;

        for (MetaProcessorDescriptor mpd : preprocessors) {

            if (mpd.isEnabled()) {

                try {

                    Class clazz = rt.getClassLoader().loadClass(mpd.getClassName());
                    MetaProcessor mp = (MetaProcessor) clazz.newInstance();

                    for (Entry<String, String> e : mpd.getProperties().entrySet()) {
                        mp.setValue(e.getKey(), e.getValue());
                    }

                    if (!metaProcessorActive) {
                        rt.println("");
                        metaProcessorActive = true;
                    }

                    rt.println("Running MetaProcessor " + clazz.getName() + " on " + mpd.getContext().getInstanceName());
                    mp.process(mpd.getContext(), this, rt);

//                    mpd.setEnabled(false);
                } catch (Exception ex) {
                    throw (new JAMSException("Error while preprocessing model: " + ex.getMessage() + "\n" + StringTools.getStackTraceString(ex.getStackTrace()), ex));
                }
            }
        }

        if (metaProcessorActive) {
            rt.println("");
        }

    }
    
    public AttributeList addToAttributeList(String name, Class type, String value) {

        AttributeList list = attributeLists.get(name);

        // get the list if existing or create a new one
        if (list == null) {
            list = new AttributeList(this, name);
            list.setType(type);
            attributeLists.put(name, list);
        }
            
        list.add(value);
        
        return list;
    }
    
    /**
     * @return the attributeLists
     */
    public HashMap<String, AttributeList> getAttributeLists() {
        return attributeLists;
    }    
//    public static void main(String[] args) throws IOException {
//
//        SystemProperties properties = JAMSProperties.createProperties();
//        properties.load("e:/jamsapplication/nsk.jap");
//        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");
//
//        JAMSRuntime runtime = new StandardRuntime(properties);
//
//        ClassLoader classLoader = JAMSClassLoader.createClassLoader(libs, new JAMSLogger());
//
//        ModelIO io = new ModelIO(new NodeFactory() {
//            public ModelNode createNode(ComponentDescriptor cd) {
//                return new ModelNode(cd);
//            }
//        }, Logger.getLogger("test"));
//
//        Document doc = XMLTools.getDocument("e:/jamsapplication/JAMS-Gehlberg/j2k_gehlberg.jam");
//
//        // get the model and access some meta data
//
//        ModelDescriptor md = null;
//
//        ExceptionHandler exHandler = new ExceptionHandler() {
//            public void handle(JAMSException ex) {
//                ex.printStackTrace();
//            }
//
//            public void handle(List<JAMSException> exList) {
//            }
//        };
//
//        md = io.loadModel(doc, classLoader, false);
//
//        md.metaProcess(runtime.getClassLoader(), runtime.getLogger());
//
//        XMLTools.writeXmlFile(io.getModelDocument(md), "e:/jamsapplication/JAMS-Gehlberg/j2k_concurrent.jam");
//
//    }
}
