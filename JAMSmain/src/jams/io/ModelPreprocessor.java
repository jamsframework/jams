/*
 * ModelPreprocessor.java
 * Created on 28. August 2006, 08:25
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.io;

import jams.*;
import java.util.ArrayList;
import jams.io.ModelConfig.Setting;
import jams.runtime.JAMSRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch
 */
public class ModelPreprocessor {
    
    Document modelDoc;
    ModelConfig config;
    JAMSRuntime rt;
    
    public ModelPreprocessor(Document modelDoc, ModelConfig config, JAMSRuntime rt) {
        this.modelDoc = modelDoc;
        this.config = config;
        this.rt = rt;

    }
    
    
    public void process() {
        processNode(modelDoc.getDocumentElement());
    }
    
    private void processNode(Element rootElement) {
        Node node;
        
        String type = rootElement.getNodeName();
        
        if (type.equals("contextcomponent") || type.equals("component") || type.equals("model")) {
            String compName = rootElement.getAttribute("name");
            
            ArrayList<Setting> list = config.getSettings(compName);
            if (list != null) {

                NodeList varChilds, attributeChilds;
                
                attributeChilds = rootElement.getElementsByTagName("attribute");
                varChilds = rootElement.getElementsByTagName("var");
                
                for (Setting s : list) {
                    if (s.getAttribute().equals("%enable%") && (s.getValue().equals("0"))) {
                        rootElement.getParentNode().removeChild(rootElement);
                        rt.println(JAMS.i18n("Disabling_component_") + compName, JAMS.VERBOSE);
                        return;
                    }
                }
                
                for (int index = 0; index < attributeChilds.getLength(); index++) {
                    Element attribute = (Element) attributeChilds.item(index);
                    for (Setting s : list) {
                        if (s.getAttribute().equals(attribute.getAttribute("name"))) {
                            attribute.setAttribute("value", s.getValue());
                            rt.println(JAMS.i18n("Setting_") + attribute.getAttribute("name") + JAMS.i18n("_to_") + s.getValue(), JAMS.VERBOSE);
                        }
                    }
                }
                
                for (int index = 0; index < varChilds.getLength(); index++) {
                    Element var = (Element) varChilds.item(index);
                    for (Setting s : list) {
                        if (s.getAttribute().equals(var.getAttribute("name"))) {
                            var.setAttribute("value", s.getValue());
                            rt.println(JAMS.i18n("Setting_") + var.getAttribute("name") + JAMS.i18n("_to_") + s.getValue(), JAMS.VERBOSE);
                        }
                    }
                }
            }
        }
        
        if (type.equals("contextcomponent") || type.equals("model")) {
            NodeList childs = rootElement.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                node = childs.item(index);
                if (node.getNodeName().equals("contextcomponent") || node.getNodeName().equals("component")) {
                    processNode((Element) node);
                }
            }
        }
        
        
    }
/*
    public Element findElement(String componentName, String varName) {
 
        if (componentName != "") {
            Element comp = findElement(modelDoc.getDocumentElement(), componentName);
            if (comp!=null) {
                NodeList varChilds = comp.getElementsByTagName("var");
                for (int j = 0; j < varChilds.getLength(); j++) {
                    Element var = (Element) varChilds.item(j);
 
                    if (var.getAttribute("name").equals(varName)) {
                        return var;
                    }
                }
            }
        } else {
            NodeList varChilds = modelDoc.getDocumentElement().getElementsByTagName("globvar");
            for (int j = 0; j < varChilds.getLength(); j++) {
                Element var = (Element) varChilds.item(j);
 
                if (var.getAttribute("name").equals(varName)) {
                    return var;
                }
            }
        }
        return null;
    }
 
    private Element findElement(Element rootElement, String componentName) {
 
        Element element;
        Node node;
 
        NodeList childs = rootElement.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            node = childs.item(index);
            if (node.getNodeName() == "contextcomponent" || node.getNodeName() == "component") {
                element = (Element) node;
 
                if (element.getAttribute("name").equals(componentName)) {
                    return element;
                } else if (element.getNodeName() == "contextcomponent") {
                    Element result = findElement(element, componentName);
                    if (result != null)
                        return result;
                }
 
            }
        }
 
        return null;
    }
 */
}
