/*
 * ParameterProcessor.java
 * Created on 10. Juli 2008, 15:48
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
package jams.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.JAMS;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ParameterProcessor {

    public static final String COMPONENT_ENABLE_VALUE = "%enable%";

    /**
     * This method loads model parameters, i.e. all component attributes that
     * are provided with a value at model start, from a file.
     * @param model The model document whose parameters are to be loaded.
     * @param paramFile The file that contains the attribute values.
     * @return The resulting model document.
     * @throws java.io.IOException
     */
    static public Document loadParams(Document model, File paramFile) throws IOException {

        Properties props = new Properties();
        props.load(new FileReader(paramFile));
        return loadParams(model, props);

    }

    /**
     * This method loads model parameters, i.e. all component attributes that
     * are provided with a value at model start, from a Properties object.
     * @param model The model document whose parameters are to be loaded.
     * @param params A Properties object that contains parameter identifyers 
     * (componentname.attributename) and their values.
     * @return The resulting model document.
     */
    static public Document loadParams(Document model, Properties params) {

        Element attribute;
        String key, value;
        HashMap<String, Element> attributeHash;

        HashMap<String, HashMap<String, Element>> componentHash = getAttributeHash(model);
        for (String componentName : componentHash.keySet()) {
            attributeHash = componentHash.get(componentName);
            for (String attributeName : attributeHash.keySet()) {
                attribute = attributeHash.get(attributeName);
                key = componentName + "." + attribute.getAttribute("name");
                //System.out.println("loadParams. attKey:" + key);
                value = params.getProperty(key);

                if (value != null) {
                    attribute.setAttribute("value", value);
                     //System.out.println("loadParams. attKey:" + key + " -> new value:" + value);
                }
            }
        }
        return model;
    }

    /**
     * get value of certain attribute
     * @param model
     * @param theAttributeName (<context>.<attribute>)
     * @return attribute "value" or null
     */
    public static String getAttributeValue(Document model, String theAttributeName) {

        Element attribute;
        String key;
        HashMap<String, Element> attributeHash;

        HashMap<String, HashMap<String, Element>> componentHash = getAttributeHash(model);
        for (String componentName : componentHash.keySet()) {
            attributeHash = componentHash.get(componentName);
            for (String attributeName : attributeHash.keySet()) {
                attribute = attributeHash.get(attributeName);
                key = componentName + "." + attribute.getAttribute("name");
                if (key.equals(theAttributeName)) {
                    return attribute.getAttribute("value");
                }
            }
        }
        return null;
    }

    /**
     * This method saves all model parameters, i.e. all component attributes that
     * are provided with a value at model start, to a file.
     * @param model The model document whose parameters are to be saved.
     * @param paramsFile The file that will contain the saved parameter values.
     * @param userName User name - will be stored in comments
     * @param modelFileName Model file name - will be stored in comments
     * @throws java.io.IOException
     */
    static public void saveParams(Document model, File paramsFile, String userName, String modelFileName) throws IOException {

        HashMap<String, HashMap<String, Element>> componentHash = getAttributeHash(model);
        Properties params = new Properties();

        for (String componentName : componentHash.keySet()) {
            HashMap<String, Element> attributeHash = componentHash.get(componentName);

            for (String attributeName : attributeHash.keySet()) {
                Element attribute = attributeHash.get(attributeName);
                params.setProperty(componentName + "." + attribute.getAttribute("name"), attribute.getAttribute("value"));
            }
        }
        
        String userNameString = System.getProperty("user.name");
        if ((userName != null) && !userName.equals("")) {
            userNameString +=  " <" + userName + ">";
        }
        
        String modelNameString = model.getDocumentElement().getAttribute("name");
        if ((modelFileName != null) && !modelFileName.equals("")) {
            modelNameString +=  " <" + modelFileName + ">";
        }
        
        params.store(new FileOutputStream(paramsFile), JAMS.resources.getString("JAMS_model_parameter_file") +
                "\nUser: " + userNameString + "\nModel: " + modelNameString);
    }

    /**
     * This method returns a HashMap that contains component names as keys and
     * the belonging XML Element as value.
     * @param model The model document that the HashMap is created from.
     * @return A HashMap object.
     */
    public static HashMap<String, Element> getComponentHash(Document model) {
        HashMap<String, Element> componentHash = new HashMap<String, Element>();

        ArrayList<Element> elementList = getElementList(model);
        // process the elements
        for (Element element : elementList) {
            componentHash.put(element.getAttribute("name"), element);
        }

        return componentHash;
    }

    /**
     * This method returns a HashMap that contains component names as keys
     * and a HashMap as value. This second HashMap contains component attribute
     * names as keys and the belonging XML Element as value.
     * @param model The model document that the HashMap is created from.
     * @return A HashMap object.
     */
    public static HashMap<String, HashMap<String, Element>> getAttributeHash(Document model) {

        HashMap<String, HashMap<String, Element>> componentHash = new HashMap<String, HashMap<String, Element>>();

        ArrayList<Element> elementList = getElementList(model);

        // process the elements
        for (Element element : elementList) {

            HashMap<String, Element> attributeHash = new HashMap<String, Element>();

            NodeList childs = element.getChildNodes();
            for (int j = 0; j < childs.getLength(); j++) {
                Node child = childs.item(j);
                if (child.getNodeName().equals("var")) {
                    Element var = (Element) child;
                    if (var.hasAttribute("value")) {
                        attributeHash.put(var.getAttribute("name"), var);
                    }
                } else if (child.getNodeName().equals("attribute")) {
                    Element attribute = (Element) child;
                    attributeHash.put(attribute.getAttribute("name"), attribute);
                }
            }

            componentHash.put(element.getAttribute("name"), attributeHash);
        }
        
        /*
        NodeList componentList = root.getElementsByTagName("component");
        for (int i = 0; i < componentList.getLength(); i++) {
        Element element = (Element) componentList.item(i);
        
        HashMap<String, Element> attributeHash = new HashMap<String, Element>();
        
        NodeList childs = element.getChildNodes();
        for (int j = 0; j < childs.getLength(); j++) {
        Node child = childs.item(j);
        if (child.getNodeName().equals("var")) {
        Element var = (Element) child;
        if (var.hasAttribute("value")) {
        attributeHash.put(var.getAttribute("name"), var);
        }
        }
        }
        
        componentHash.put(element.getAttribute("name"), attributeHash);
        }
         */
        return componentHash;
    }

    private static ArrayList<Element> getElementList(Document model) {
        Element root = model.getDocumentElement();

        // create an ArrayList that holds all interesting elements
        ArrayList<Element> elementList = new ArrayList<Element>();

        // first add the model itself
        elementList.add(root);

        // then add all context elements
        NodeList contextList = root.getElementsByTagName("contextcomponent");
        for (int i = 0; i < contextList.getLength(); i++) {
            elementList.add((Element) contextList.item(i));
        }

        // finally add all component elements
        NodeList componentList = root.getElementsByTagName("component");
        for (int i = 0; i < componentList.getLength(); i++) {
            elementList.add((Element) componentList.item(i));
        }

        return elementList;
    }

    /**
     * This method cleans all property elements of a given model document from 
     * formerly used attributes that are not needed anymore. This 
     * includes removal of value and default attributes.
     * @param modelDoc The model document to be processed.
     */
    public static void stripPropertyElements(Document modelDoc) {
        NodeList propertyList = modelDoc.getDocumentElement().getElementsByTagName("property");
        for (int i = 0; i < propertyList.getLength(); i++) {
            Element propertyElement = (Element) propertyList.item(i);
            propertyElement.removeAttribute("value");
            propertyElement.removeAttribute("default");
        }
    }

    /**
     * This method searches for property nodes that refer to attribute 
     * COMPONENT_ENABLE_VALUE. If it finds one, the referred component is removed
     * from its parent if the property node's value is "0"
     * @param modelDoc The model document to be processed
     */
    public static void preProcess(Document modelDoc) {

        NodeList propertyList = modelDoc.getDocumentElement().getElementsByTagName("property");
        HashMap<String, Element> componentHash = getComponentHash(modelDoc);

        for (int i = 0; i < propertyList.getLength(); i++) {
            Element propertyElement = (Element) propertyList.item(i);

            // check if this is an "%enable%" property and if so, if its value is "0"
            if (propertyElement.getAttribute("attribute").equals(ParameterProcessor.COMPONENT_ENABLE_VALUE) &&
                    propertyElement.getAttribute("value").equals("0")) {

                // get the belonging element object and remove it from its parent
                Element componentElement = componentHash.get(propertyElement.getAttribute("component"));
                if (componentElement != null)
                    componentElement.getParentNode().removeChild(componentElement);
            }
        }
    }
}
