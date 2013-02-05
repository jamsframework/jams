/*
 * MetaProcessorDescriptor.java
 * Created on 01.02.2013, 12:28:29
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

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class MetaProcessorDescriptor {
    
    private String className;
    private boolean enabled;
    private HashMap<String, String> properties = new HashMap();
    private ContextDescriptor context;
    
    public MetaProcessorDescriptor(String className, ContextDescriptor context, boolean enabled) {
        this.className = className;
        this.context = context;
        this.enabled = enabled;
    }
    
        public Document createDocument() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element mpElement = (Element) document.createElement("metaprocessor");
        mpElement.setAttribute("context", this.context.getInstanceName());
        mpElement.setAttribute("class", this.getClassName());
        mpElement.setAttribute("enabled", Boolean.toString(this.enabled));

        for (Map.Entry<String, String> e : getProperties().entrySet()) {
            Element propertyElement = (Element) document.createElement("property");
            propertyElement.setAttribute("name", e.getKey());
            propertyElement.setAttribute("value", e.getValue());
            mpElement.appendChild(propertyElement);
        }

        document.appendChild(mpElement);

        return document;
    }

    /**
     * @return the properties
     */
    public HashMap<String, String> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @return the cd
     */
    public ContextDescriptor getContext() {
        return context;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}