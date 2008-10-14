/*
 * OutputDataStore.java
 * Created on 9. September 2008, 22:23
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
package jams.workspace.stores;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.VirtualWorkspace;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import jams.model.JAMSContext;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDataStore implements DataStore {

    private static final String TRACE_STRING = "attribute";
    private static final String FILTER_STRING = "filter";
    private static final String CONTEXT_STRING = "context";
    private static final String EXPRESSION_STRING = "expression";
    private static final String ATTRIBUTE_STRING = "id";
    private String id;
    private String[] attributes;
    private Filter[] filters;
    private BufferedWriter writer;
    private VirtualWorkspace ws;

    public OutputDataStore(VirtualWorkspace ws, Document doc, String id) {

        this.id = id;
        this.ws = ws;

        Element root = doc.getDocumentElement();

        NodeList traceNodes = root.getElementsByTagName(TRACE_STRING);
        int length = traceNodes.getLength();
        attributes = new String[length];
        for (int i = 0; i < length; i++) {
            Element traceElement = (Element) traceNodes.item(i);
            attributes[i] = traceElement.getAttribute(ATTRIBUTE_STRING);
        }

        NodeList filterNodes = root.getElementsByTagName(FILTER_STRING);
        length = filterNodes.getLength();
        filters = new Filter[length];
        for (int i = 0; i < length; i++) {
            Element filterElement = (Element) filterNodes.item(i);
            filters[i] = new Filter(filterElement.getAttribute(CONTEXT_STRING),
                    filterElement.getAttribute(EXPRESSION_STRING));
        }        
    }

    public String getID() {
        return id;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void open() throws IOException {
        File outputDirectory = ws.getOutputDataDirectory();
        outputDirectory.mkdirs();

        File outputFile = new File(outputDirectory.getPath() + File.separator + id + ".dat");
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    public void write(Object o) throws IOException {
        writer.write(o.toString());
        writer.flush();
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    public Filter[] getFilters() {
        return filters;
    }

    public class Filter {

        private String contextName,  expression;
        private Pattern pattern = null;
        private JAMSContext context = null;

        public Filter(String contextName, String expression) {
            this.contextName = contextName;
            this.expression = expression;
        }

        public String getContextName() {
            return contextName;
        }

        public String getExpression() {
            return expression;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public JAMSContext getContext() {
            return context;
        }

        public void setContext(JAMSContext context) {
            this.context = context;
        }
    }
}
