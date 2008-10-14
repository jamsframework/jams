/*
 * StandardInputDataStore.java
 * Created on 4. Februar 2008, 23:21
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

import jams.workspace.*;
import jams.workspace.datatypes.DataValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.workspace.DataReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author Sven Kralisch
 */
public abstract class StandardInputDataStore implements InputDataStore {

    protected HashMap<String, DataReader> dataIO;
    protected VirtualWorkspace ws;
    protected DataSetDefinition dsd;
    protected int bufferSize = 0;
    private String id,  description = "";
    protected BufferedReader dumpReader;

    public StandardInputDataStore(File file) throws IOException {

        this.dumpReader = new BufferedReader(new FileReader(file));

        String firstLine = dumpReader.readLine();

        if (firstLine.startsWith(VirtualWorkspace.DUMP_MARKER)) {
            readStandardDump();
        } else {
            readJ2KDump(file.getAbsolutePath(), firstLine);
        }

    }

    public StandardInputDataStore(VirtualWorkspace ws, Document doc) {
        this.ws = ws;

        this.id = doc.getDocumentElement().getAttribute("id");

        Node descriptionNode = doc.getDocumentElement().getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            this.description = descriptionNode.getTextContent();
        }

        Element parameterElement = (Element) doc.getDocumentElement().getElementsByTagName("parameter").item(0);

        Element bufferSizeElement = (Element) parameterElement.getElementsByTagName("buffersize").item(0);
        if (bufferSizeElement != null) {
            this.bufferSize = Integer.parseInt(bufferSizeElement.getAttribute("value"));
        }

        this.dataIO = createDataIO(doc);
        this.dsd = createDataSetDefinitionFromDocument(doc);
    }

    private DataSetDefinition createDataSetDefinitionFromDocument(Document doc) {

        ArrayList<Class> dataTypes = new ArrayList<Class>();

        Element metadataElement = (Element) doc.getElementsByTagName("metadata").item(0);

        NodeList columnList = metadataElement.getElementsByTagName("column");
        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            try {
                Class type = Class.forName(columnElement.getAttribute("type"));
                dataTypes.add(type);
            } catch (ClassNotFoundException cnfe) {
                ws.getRuntime().handle(cnfe);
            }
        }

        DataSetDefinition def = new DataSetDefinition(dataTypes);

        NodeList rowList = metadataElement.getElementsByTagName("row");
        for (int i = 0; i < rowList.getLength(); i++) {
            Element rowElement = (Element) rowList.item(i);
            try {
                Class type = Class.forName(rowElement.getAttribute("type"));
                def.addAttribute(rowElement.getAttribute("id"), type);
            } catch (ClassNotFoundException cnfe) {
                ws.getRuntime().handle(cnfe);
            }
        }

        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            DataReader metadataIO = dataIO.get(columnElement.getAttribute("metadataio"));

            int result = metadataIO.init();
            if (result < 0) {
                ws.getRuntime().sendErrorMsg("Initialization of data I/O component (" + this.getClass().getName() + ") failed..");
                return null;
            }
        }

        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            DataReader metadataIO = dataIO.get(columnElement.getAttribute("metadataio"));

            metadataIO.fetchValues(1);
            DataSet metadataSet = metadataIO.getData()[0];

            ArrayList<Object> values = new ArrayList<Object>();
            for (DataValue value : metadataSet.getData()) {
                values.add(value.getObject());
            }
            def.setAttributeValues(i, values);

        }

        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            DataReader metadataIO = dataIO.get(columnElement.getAttribute("metadataio"));

            metadataIO.cleanup();
        }

        return def;
    }

    private HashMap<String, DataReader> createDataIO(Document doc) {

        HashMap<String, DataReader> _dataIO = new HashMap<String, DataReader>();

        Element ioElement = (Element) doc.getElementsByTagName("dataio").item(0);

        if (ioElement == null) {
            return null;
        }

        HashMap<String, String> varMap = new HashMap<String, String>();

        Element variableElement = (Element) ioElement.getElementsByTagName("variables").item(0);
        if (variableElement != null) {
            NodeList varNodes = variableElement.getElementsByTagName("var");
            for (int n = 0; n < varNodes.getLength(); n++) {
                Element varNode = (Element) varNodes.item(n);
                varMap.put(varNode.getAttribute("id"), varNode.getAttribute("value"));
            }
        }

        NodeList ioNodes = ioElement.getElementsByTagName("plugin");
        for (int n = 0; n < ioNodes.getLength(); n++) {

            Element ioNode = (Element) ioNodes.item(n);
            String className = ioNode.getAttribute("type");
            String nodeID = ioNode.getAttribute("id");

            ClassLoader loader = ws.getClassLoader();

            try {

                Class<?> clazz = loader.loadClass(className);
                DataReader io = (DataReader) clazz.newInstance();

                NodeList parameterNodes = ioNode.getElementsByTagName("parameter");
                for (int i = 0; i < parameterNodes.getLength(); i++) {

                    Element parameterNode = (Element) parameterNodes.item(i);

                    String attributeName = parameterNode.getAttribute("id");
                    String attributeValue = "";
                    if (parameterNode.hasAttribute("value")) {
                        attributeValue = parameterNode.getAttribute("value");
                    } else {
                        String varID = parameterNode.getAttribute("varid");
                        attributeValue = varMap.get(varID);
                    }
                    String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

                    Method method = clazz.getMethod(methodName, String.class);

                    method.invoke(io, attributeValue);

                }

                _dataIO.put(nodeID, io);

            } catch (ClassNotFoundException cnfe) {
                ws.getRuntime().handle(cnfe);
            } catch (InstantiationException ie) {
                ws.getRuntime().handle(ie);
            } catch (IllegalAccessException iae) {
                ws.getRuntime().handle(iae);
            } catch (NoSuchMethodException nsme) {
                ws.getRuntime().handle(nsme);
            } catch (InvocationTargetException ite) {
                ws.getRuntime().handle(ite);
            }
        }
        return _dataIO;
    }

    public String getID() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public DataSetDefinition getDataSetDefinition() {
        return this.dsd;
    }

    public DataReader getDataIO(String id) {
        return dataIO.get(id);
    }

    private void readStandardDump() throws IOException {
    }

    private void readJ2KDump(String id, String firstLine) throws IOException {

        this.id = id;

        // read header information from the J2K time series file

        String line = firstLine;
        //skip comment lines
        while (line.charAt(0) == '#') {
            this.description += line.substring(1) + "\n";
            line = dumpReader.readLine();
        }

        StringBuffer dataValueAttribs = new StringBuffer();
        while (!line.startsWith("@dataSetAttribs")) {
            dataValueAttribs.append(line + "\n");
            line = dumpReader.readLine();
        }

        StringBuffer dataSetAttribs = new StringBuffer();
        while (!line.startsWith("@statAttribVal")) {
            dataSetAttribs.append(line + "\n");
            line = dumpReader.readLine();
        }

        StringBuffer statAttribVal = new StringBuffer();
        while (!line.startsWith("@dataVal")) {
            statAttribVal.append(line + "\n");
            line = dumpReader.readLine();
        }

        // create a DataSetDefinition object

        StringTokenizer tok1 = new StringTokenizer(statAttribVal.toString(), "\n");
        tok1.nextToken();
        StringTokenizer tok2 = new StringTokenizer(tok1.nextToken());

        int attributeCount = tok2.countTokens() - 1;
        ArrayList<Class> dataTypes = new ArrayList<Class>();
        for (int i = 0; i < attributeCount; i++) {
            dataTypes.add(Double.class);
        }
        DataSetDefinition def = new DataSetDefinition(dataTypes);

        while (tok1.hasMoreTokens()) {

            String attributeName = tok2.nextToken();
            def.addAttribute(attributeName, String.class);
            ArrayList<Object> values = new ArrayList<Object>();
            while (tok2.hasMoreTokens()) {
                values.add(tok2.nextToken());
            }
            def.setAttributeValues(attributeName, values);
            tok2 = new StringTokenizer(tok1.nextToken());
        }

        tok1 = new StringTokenizer(dataValueAttribs.toString());
        tok1.nextToken(); // skip the "@"-tag
        String parameterName = tok1.nextToken();
        String parameterString = "PARAMETER";
        def.addAttribute(parameterString, String.class);
        def.setAttributeValues(parameterString, parameterName);

        this.dsd = def;

    }
}
