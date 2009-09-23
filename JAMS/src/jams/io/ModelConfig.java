/*
 * ModelConfig.java
 * Created on 27. August 2006, 23:33
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.io;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import jams.io.XMLIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sven Kralisch
 */
public class ModelConfig {
    
    private HashMap<String, ArrayList<Setting>> settings = new HashMap<String, ArrayList<Setting>>();
    
    public ModelConfig() {
    }
    
    public ModelConfig(Document doc) {
        readConfig(doc);
    }
    
    public ModelConfig(String fileName) {
        readConfig(fileName);
    }
    
    public void readConfig(String fileName) {
        try {
            readConfig(XMLIO.getDocument(fileName));
        } catch (FileNotFoundException fnfe) {}
    }
    
    public void readConfig(Document doc) {
        
        Element root = (Element) doc.getDocumentElement().getElementsByTagName("launcher").item(0);
        NodeList groups = root.getElementsByTagName("group");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            
            NodeList properties = group.getElementsByTagName("property");
            for (int j = 0; j < properties.getLength(); j++) {
                Element property = (Element) properties.item(j);
                this.addSetting(property.getAttribute("component"), property.getAttribute("attribute"), property.getAttribute("value"));
            }
        }
    }
    
    public void addSetting(String componentName, String attributeName, String attributeValue) {
        ArrayList<Setting> list = settings.get(componentName);
        if (list==null) {
            list = new ArrayList<Setting>();
        }
        list.add(new Setting(attributeName, attributeValue));
        
        settings.put(componentName, list);
    }
    
    public ArrayList<Setting> getSettings(String componentName) {
        return settings.get(componentName);
    }
    
    public class Setting {
        private String attribute;
        private String value;
        
        public Setting(String attribute, String value) {
            this.attribute = attribute;
            this.value = value;
        }
        
        public String getAttribute() {
            return attribute;
        }
        
        public String getValue() {
            return value;
        }
    }
}
