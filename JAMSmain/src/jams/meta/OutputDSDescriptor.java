/*
 * OutputDSDescriptor.java
 * Created on 22.03.2010, 15:21:51
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

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSDescriptor {

    private ContextDescriptor context;
    private String name;
    private boolean enabled = true;
    private ArrayList<ContextAttribute> contextAttributes = new ArrayList<ContextAttribute>();
    private ArrayList<FilterDescriptor> filters = new ArrayList<FilterDescriptor>();

    public OutputDSDescriptor(ContextDescriptor context) {
        this.context = context;
    }

    public Document createDocument() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element dsElement = (Element) document.createElement("outputdatastore");
        dsElement.setAttribute("context", this.context.getInstanceName());
        dsElement.setAttribute("name", this.getName());
        dsElement.setAttribute("enabled", Boolean.toString(this.enabled));

        for (FilterDescriptor f : filters) {
            Element filterElement = (Element) document.createElement("filter");
            filterElement.setAttribute("context", f.context.getInstanceName());
            filterElement.setAttribute("expression", f.expression);
            dsElement.appendChild(filterElement);
        }

        Element traceElement = (Element) document.createElement("trace");
        dsElement.appendChild(traceElement);

        for (ContextAttribute ca : contextAttributes) {
            Element caElement = (Element) document.createElement("attribute");
            caElement.setAttribute("id", ca.getName());
            traceElement.appendChild(caElement);
        }

        document.appendChild(dsElement);

        return document;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the contextAttributes
     */
    public ArrayList<ContextAttribute> getContextAttributes() {
        return contextAttributes;
    }

    /**
     * @return the filters
     */
    public ArrayList<FilterDescriptor> getFilters() {
        return filters;
    }

    /**
     * @return the context
     */
    public ContextDescriptor getContext() {
        return context;
    }
    
    /**
     * @param context the context to set
     */
    public void setContext(ContextDescriptor context) {
        this.context = context;
    }    

    @Override
    public String toString() {
        char enabledChar;
        if (this.isEnabled()) {
            enabledChar = Character.toChars(9746)[0];//9745
        } else {
            enabledChar = Character.toChars(9744)[0];
        }
        return /*enabledChar + " " + */name + " [" + context.getInstanceName() + "]";
    }

    public FilterDescriptor addFilter(ContextDescriptor context, String expression) {
        FilterDescriptor f = new FilterDescriptor();
        f.context = context;
        f.expression = expression;
        filters.add(f);
        return f;
    }

    public void removeFilter(FilterDescriptor f) {
        filters.remove(f);
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

    public class FilterDescriptor {
        public String expression;
        public ContextDescriptor context;

        public String toString() {
            return expression + " [" + context.getInstanceName() + "]";
        }
    }
}
