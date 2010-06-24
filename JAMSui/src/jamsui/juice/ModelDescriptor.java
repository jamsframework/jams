/*
 * Context.java
 * Created on 23.06.2010, 16:39:22
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
package jamsui.juice;

import jams.JAMS;
import jams.gui.HelpComponent;
import jams.gui.tools.GUIHelper;
import jams.io.ParameterProcessor;
import jams.tools.StringTools;
import jamsui.juice.ModelProperties.Group;
import jamsui.juice.ModelProperties.ModelElement;
import jamsui.juice.ModelProperties.ModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Set;
import java.util.StringTokenizer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ModelDescriptor {

    private HashMap<String, ComponentDescriptor> componentDescriptors;
    private HashMap<String, OutputDSDescriptor> outputDataStores;
    private ModelProperties modelProperties;
    private String author = "", date = "", description = "", helpBaseUrl = "", workspacePath = "";

    public ModelDescriptor() {
        componentDescriptors = new HashMap<String, ComponentDescriptor>();
        outputDataStores = new HashMap<String, OutputDSDescriptor>();
        modelProperties = new ModelProperties();
    }

    public String registerComponentDescriptor(String oldName, String newName, ComponentDescriptor cd) {

        String newNewName = createComponentInstanceName(newName);
        getComponentDescriptors().remove(oldName);
        getComponentDescriptors().put(newNewName, cd);

        return newNewName;
    }

    public void unRegisterComponentDescriptor(ComponentDescriptor cd) {
        getComponentDescriptors().remove(cd.getName());
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

    public void setDatastores(Element dataStoresNode) {

        //hier outputdsdescriptor objekte erzeugen!
        String name = dataStoresNode.getAttribute("name");
        ComponentDescriptor context = getComponentDescriptor(dataStoresNode.getAttribute("context"));
        OutputDSDescriptor od = new OutputDSDescriptor(context);
        od.setName(name);

        // fill the contextAttributes
        ArrayList<ContextAttribute> contextAttributes = od.getContextAttributes();
        NodeList attributeNodes = dataStoresNode.getElementsByTagName("attribute");
        for (int i = 0; i < attributeNodes.getLength(); i++) {

            Element attributeElement = (Element) attributeNodes.item(i);
            String attributeName = attributeElement.getAttribute("id");
            //context.getDataRepository().getAttributeByTypeName(null, name)
        }

        // fill the filters
        ArrayList<String> filters = od.getFilters();
    }

    public void setModelParameters(Element launcherNode) {
        Node node;

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
                    ModelProperty property = getPropertyFromElement(propertyElement, mProp);
                    if (property != null) {
                        mProp.addProperty(group, property);
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
                        ModelProperty property = getPropertyFromElement(propertyElement, mProp);
                        if (property != null) {
                            mProp.addProperty(subgroup, property);
                        }
                    }
                }
            }
        }
        return;
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
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Component_") + propertyElement.getAttribute("component")
                    + JAMS.resources.getString("_does_not_exist,_but_is_referred_in_list_of_model_parameters!")
                    + JAMS.resources.getString("Will_be_removed_when_model_is_saved!"), JAMS.resources.getString("Model_loading_error"));
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
        if ((property.attribute == null) && (property.var == null)
                && !attributeName.equals(ParameterProcessor.COMPONENT_ENABLE_VALUE)) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("Attribute_") + attributeName
                    + JAMS.resources.getString("_does_not_exist_in_component_") + property.component.getName()
                    + JAMS.resources.getString("._Removing_visual_editor!"), JAMS.resources.getString("Model_loading_error"));
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

    public ComponentDescriptor getComponentDescriptor(String name) {
        return getComponentDescriptors().get(name);
    }

    public HashMap<String, ComponentDescriptor> getComponentDescriptors() {
        return componentDescriptors;
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

}
