/*
 * StandardDataStore.java
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
package rbis.virtualws;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import rbis.virtualws.plugins.DataIO;

/**
 *
 * @author Sven Kralisch
 */
public abstract class StandardDataStore implements DataStore {

    private Document doc;
    private HashMap<String, DataIO> dataIO;
    private VirtualWorkspace ws;

    public StandardDataStore() {
        this.doc = null;
        this.ws = null;
        this.dataIO = null;
    }

    public StandardDataStore(VirtualWorkspace ws, Document doc) {
        this.doc = doc;
        this.ws = ws;
        this.dataIO = getDataIO(doc, ws);
    }

    public static HashMap<String, DataIO> getDataIO(Document doc, VirtualWorkspace ws) {
//        Element sourceElement = (Element) xmlDoc.getElementsByTagName("dataseries").item(0);

        HashMap<String, DataIO> dataIO = new HashMap<String, DataIO>();

        Element ioElement = (Element) doc.getElementsByTagName("dataio").item(0);
        
        if (ioElement == null) {
            return null;
        }
        
        NodeList nodes = ioElement.getElementsByTagName("plugin");

        for (int n = 0; n < nodes.getLength(); n++) {

            Element ioNode = (Element) nodes.item(n);
            String className = ioNode.getAttribute("type");
            String id = ioNode.getAttribute("id");

            ClassLoader loader = ws.getClassLoader();

            try {

                Class<?> clazz = loader.loadClass(className);
                DataIO io = (DataIO) clazz.newInstance();

                NodeList parameterNodes = ioNode.getElementsByTagName("parameter");
                for (int i = 0; i < parameterNodes.getLength(); i++) {

                    Element parameterNode = (Element) parameterNodes.item(i);

                    String attributeName = parameterNode.getAttribute("id");
                    String attributeValue = parameterNode.getAttribute("value");
                    String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

                    Method method = clazz.getMethod(methodName, String.class);

                    method.invoke(io, attributeValue);

                }

                dataIO.put(id, io);

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
        return dataIO;
    }

    public String getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String descriptiom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataSetDefinition getDataSetDefinition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDataSetDefinition(DataSetDefinition dsDef) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
