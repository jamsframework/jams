/*
 * StandardInputDataStore.java
 * Created on 4. Februar 2008, 23:21
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.workspace.stores;

import jams.workspace.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jams.workspace.DataReader;
import jams.JAMS;
import jams.tools.StringTools;
import java.io.File;
import java.io.Serializable;

/**
 *
 * @author Sven Kralisch
 */
public abstract class StandardInputDataStore implements InputDataStore, Serializable {

    protected HashMap<String, DataReader> dataIO = new HashMap<String, DataReader>();

    transient protected Workspace ws;

    protected DataSetDefinition dsd;

    protected int bufferSize = 0;

    protected String id, description = "", missingDataValue = "", displayName="";

    protected int accessMode = InputDataStore.LIVE_MODE;
    protected boolean writeCache = false;


    public StandardInputDataStore(JAMSWorkspace ws) {
        this.ws = ws;
    }

    public StandardInputDataStore(JAMSWorkspace ws, String id, Document doc) throws ClassNotFoundException {
        this.ws = ws;
        this.id = id;//doc.getDocumentElement().getAttribute("id");

        Node descriptionNode = doc.getDocumentElement().getElementsByTagName("description").item(0);
        if (descriptionNode != null) {
            this.description = descriptionNode.getTextContent();
        }
        Node displaynameNode = doc.getDocumentElement().getElementsByTagName("displayname").item(0);
        if (displaynameNode != null) {
            this.displayName = displaynameNode.getTextContent();
        } else {
            this.displayName = id;
        }

        Element parameterElement = (Element) doc.getDocumentElement().getElementsByTagName("parameter").item(0);

        Element bufferSizeElement = (Element) parameterElement.getElementsByTagName("buffersize").item(0);
        if (bufferSizeElement != null) {
            this.bufferSize = Integer.parseInt(bufferSizeElement.getAttribute("value"));
        }

        Element accessmodeElement = (Element) parameterElement.getElementsByTagName("accessmode").item(0);
        if (accessmodeElement != null) {
            this.accessMode = Integer.parseInt(accessmodeElement.getAttribute("value"));
        }

        Element missingdataElement = (Element) parameterElement.getElementsByTagName("missingdatavalue").item(0);
        if (missingdataElement != null) {
            this.missingDataValue = missingdataElement.getAttribute("value");
        }

        if (this.accessMode == InputDataStore.CACHE_MODE) {
            File file = null;
            file = new File(ws.getLocalDumpDirectory(), id + ".dump");
            if (!file.exists()) {
                writeCache = true;
                ws.getRuntime().sendInfoMsg("Writing cache file .. " + file.getPath());
            }
        }
        if (!readCache()) {
            this.dataIO = createDataIO(doc);
            this.dsd = createDataSetDefinitionFromDocument(doc);
        }
    }

    protected boolean readCache(){
        return !writeCache && this.accessMode == InputDataStore.CACHE_MODE;
    }

    private DefaultDataSetDefinition createDataSetDefinitionFromDocument(Document doc) {

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

        DefaultDataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

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

            if (metadataIO != null) {
                int result = metadataIO.init();
                if (result < 0) {
                    ws.getRuntime().sendErrorMsg(JAMS.i18n("Initialization_of_data_I/O_component_") +
                            this.getID() + JAMS.i18n("_(") + this.getClass().getName() + JAMS.i18n(")_failed!"));
                    return null;
                }
            } else {
                ws.getRuntime().sendErrorMsg(JAMS.i18n("Initialization_of_data_I/O_component_") +
                        this.getID() + JAMS.i18n("_(") + this.getClass().getName() + JAMS.i18n(")_failed!"));
                return null;
            }
        }

        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            DataReader metadataIO = dataIO.get(columnElement.getAttribute("metadataio"));
            int source = Integer.parseInt(columnElement.getAttribute("source"));
                        
            DataSet metadataSet = metadataIO.getMetadata(source-1);

            ArrayList<Object> values = new ArrayList<Object>();
            for (DataValue value : metadataSet.getData()) {
                values.add(value.getObject());
            }
            def.setAttributeValues(i, values);

        }

        for (int i = 0; i < columnList.getLength(); i++) {
            Element columnElement = (Element) columnList.item(i);
            DataReader metadataIO = dataIO.get(columnElement.getAttribute("metadataio"));

            //metadataIO.cleanup();
        }

        return def;
    }

    private HashMap<String, DataReader> createDataIO(Document doc) throws ClassNotFoundException {

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
                    String methodName = StringTools.getSetterName(attributeName);

                    Method method = clazz.getMethod(methodName, String.class);

                    method.invoke(io, attributeValue);

                }

                _dataIO.put(nodeID, io);

            } catch (InstantiationException ie) {
                ws.getRuntime().handle(ie);
                return null;
            } catch (IllegalAccessException iae) {
                ws.getRuntime().handle(iae);
                return null;
            } catch (NoSuchMethodException nsme) {
                ws.getRuntime().handle(nsme);
                return null;
            } catch (InvocationTargetException ite) {
                ws.getRuntime().handle(ite);
                return null;
            }
        }
        return _dataIO;
    }

    /**
     * helper method to get value of a sub-node
     * @param theElement
     * @param theNodeName
     * @return value or null
     */
    protected String getNodeValue(Element theElement, String theNodeName) {
        String theNodeValue = null;
        NodeList uriNodes = theElement.getElementsByTagName(theNodeName);
        if (uriNodes != null) {
            Element uriNode = (Element) uriNodes.item(0);
            if (uriNode != null) {

                theNodeValue = uriNode.getTextContent();
            }
        }
        return theNodeValue;
    }

    public String getID() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
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

    public String getMissingDataValue() {
        return missingDataValue;
    }
        
    /**
     * @return the accessMode
     */
    public int getAccessMode() {
        return accessMode;
    }
}
