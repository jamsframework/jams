/*
 * VirtualWorkspace.java
 * Created on 23. Januar 2008, 15:42
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

import rbis.virtualws.stores.GeoDataStore;
import rbis.virtualws.stores.TableDataStore;
import rbis.virtualws.stores.TSDataStore;
import rbis.virtualws.stores.DataStore;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.io.XMLIO;
import org.unijena.jams.runtime.JAMSClassLoader;
import org.unijena.jams.runtime.JAMSRuntime;
import org.unijena.jams.runtime.StandardRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rbis.virtualws.plugins.DataIO;

public class VirtualWorkspace {

    private String wsTitle;
    private HashMap<String, DataStore> stores = new HashMap<String, DataStore>();
    private JAMSRuntime runtime = new StandardRuntime();
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public DataStore addDataStore(Document doc) {

        DataStore store = null;
        Element dataNode = (Element) doc.getElementsByTagName("data").item(0);
        String type = dataNode.getAttribute("type");

        if (type.equals("table")) {
            store = new TableDataStore(this, doc);
        } else if (type.equals("timeseries")) {
            store = new TSDataStore(this, doc);
        } else if (type.equals("geodata")) {
            store = new GeoDataStore(this, doc);
        }

        return store;
    }

    public void setLibs(String[] libs) {
        classLoader = JAMSClassLoader.createClassLoader(libs, runtime);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public JAMSRuntime getRuntime() {
        return runtime;
    }

    public void removeDataStore(DataStore store) {
        stores.remove(store);
    }

    public DataStore getDataStore(String dsTitle) {
        return stores.get(dsTitle);
    }

    public String getTitle() {
        return wsTitle;
    }

    public void setTitle(String title) {
        this.wsTitle = title;
    }

    public static void main(String[] args) throws Exception {

        Document doc = XMLIO.getDocument("D:/jams/RBISDesk/datastore.xml");
        String[] libs = {"D:/nbprojects/RBISDesk/dist", "D:/nbprojects/RBISDesk/dist/lib"};

        //System.out.println(XMLIO.getStringFromDocument(doc));

        VirtualWorkspace ws = new VirtualWorkspace();
        ws.getRuntime().setDebugLevel(JAMS.VERBOSE);
        ws.getRuntime().addErrorLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println(arg);
            }
        });

        ws.setLibs(libs);

        if (ws.getRuntime().getRunState() != JAMS.RUNSTATE_RUN) {
            System.exit(-1);
        }

        DataStore store = ws.addDataStore(doc);
                
        System.out.println(store.getDataSetDefinition().toASCIIString());
        
        System.exit(0);
        
        DataIO reader = store.getDataIO("MetadataReader");

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

