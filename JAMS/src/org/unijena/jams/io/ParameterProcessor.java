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
package org.unijena.jams.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import org.unijena.jams.JAMS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ParameterProcessor {

    static public Document loadParams(Document model, File paramFile) throws IOException {

        Properties props = new Properties();
        props.load(new FileReader(paramFile));
        return loadParams(model, props);

    }

    static public Document loadParams(Document model, Properties params) {

        HashMap<String, Element> componentHash = getComponentHash(model);
        for (String componentName : componentHash.keySet()) {
            Element component = componentHash.get(componentName);
            NodeList childs = component.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeName().equals("var")) {
                    Element var = (Element) child;
                    String value = params.getProperty(componentName + "." + var.getAttribute("name"));
                    if (value != null) {
                        var.setAttribute("value", value);
                    }
                }
            }
        }

        return model;
    }

    static public void saveParams(Document model, File paramsFile) throws IOException {

        HashMap<String, Element> componentHash = getComponentHash(model);
        Properties params = new Properties();

        for (String componentName : componentHash.keySet()) {
            Element component = componentHash.get(componentName);
            NodeList childs = component.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeName().equals("var")) {
                    Element var = (Element) child;
                    if (var.hasAttribute("value")) {
                        params.setProperty(componentName + "." + var.getAttribute("name"), var.getAttribute("value"));
                    }
                }
            }
        }

        params.store(new FileOutputStream(paramsFile), "JAMS model parameter file");
    }

    private static HashMap<String, Element> getComponentHash(Document model) {

        HashMap<String, Element> componentHash = new HashMap<String, Element>();

        Element root = model.getDocumentElement();
        NodeList contextList = root.getElementsByTagName("contextcomponent");
        NodeList componentList = root.getElementsByTagName("component");
        for (int i = 0; i < contextList.getLength(); i++) {
            Element element = (Element) contextList.item(i);
            componentHash.put(element.getAttribute("name"), element);
        }
        for (int i = 0; i < componentList.getLength(); i++) {
            Element element = (Element) componentList.item(i);
            componentHash.put(element.getAttribute("name"), element);
        }

        return componentHash;
    }
}
