/*
 * DataProvider.java
 * Created on 28. Januar 2008, 13:12
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
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.io.XMLIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import rbis.virtualws.plugins.DataIO;

/**
 *
 * @author Sven Kralisch
 */
public class TableDataProvider {

    private Document xmlDoc;
    private DataIO io;
    private VirtualWorkspace ws;

    public TableDataProvider(VirtualWorkspace ws, Document xmlDoc) {
        this.xmlDoc = xmlDoc;
        this.ws = ws;
        io = getDataIO(xmlDoc, ws);
    }   

    private static DataIO getDataIO(Document doc, VirtualWorkspace ws) {
//        Element sourceElement = (Element) xmlDoc.getElementsByTagName("dataseries").item(0);
        
        DataIO dataIO = null;
        
        Element ioNode = (Element) doc.getElementsByTagName("dataio").item(0);
        String className = ioNode.getAttribute("type");

        ClassLoader loader = ws.getClassLoader();

        try {

            Class<?> clazz = loader.loadClass(className);
            dataIO = (DataIO) clazz.newInstance();

            NodeList parameterNodes = ioNode.getElementsByTagName("parameter");
            for (int i = 0; i < parameterNodes.getLength(); i++) {

                Element parameterNode = (Element) parameterNodes.item(i);

                String attributeName = parameterNode.getAttribute("id");
                String attributeValue = parameterNode.getAttribute("value");
                String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);

                Method method = clazz.getMethod(methodName, String.class);

                method.invoke(dataIO, attributeValue);

            }

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
        
        return dataIO;
    }

    public boolean hasNext() {
        return false;
    }

    public DataSet getNext() {
        return null;
    }

    public static void main(String[] args) throws Exception {

        Document doc = XMLIO.getDocument("D:/jams/RBISDesk/datastore.xml");

        //System.out.println(XMLIO.getStringFromDocument(doc));

        VirtualWorkspace ws = new VirtualWorkspace();
        ws.getRuntime().setDebugLevel(JAMS.VERBOSE);
        ws.getRuntime().addErrorLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println(arg);
            }
        });

        String[] libs = {"D:/nbprojects/RBISDesk/dist", "D:/nbprojects/RBISDesk/dist/lib"};
        ws.setLibs(libs);

        TableDataProvider provider = new TableDataProvider(ws, doc);

        if (ws.getRuntime().getRunState() != JAMS.RUNSTATE_RUN) {
            System.exit(-1);
        }

        DataIO reader = provider.io;

        long start = System.currentTimeMillis();

        reader.init();

        if (ws.getRuntime().getRunState() != JAMS.RUNSTATE_RUN) {
            System.exit(-1);
        }

        DataSet[] data = reader.getValues(1);
        int rows = data.length;
        int columns = data[0].getData().length;
        while (data.length > 0) {
            data = reader.getValues(1000);
            rows += data.length;
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start));
        System.out.println("Rows: " + rows);
        System.out.println("Cols: " + columns);

        reader.cleanup();

        JAMSCalendar cal = new JAMSCalendar();
        cal.setValue(new GregorianCalendar());
        cal.set(1925, 10, 1, 0, 0, 0);
        System.out.println(cal);
        System.out.println(Math.round((double) cal.getTimeInMillis() / 1000)); //should be "1925-11-01 00:00" / -1393804800

    }
}
