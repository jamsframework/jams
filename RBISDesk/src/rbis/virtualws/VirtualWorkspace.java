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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import rbis.virtualws.stores.GeoDataStore;
import rbis.virtualws.stores.TableDataStore;
import rbis.virtualws.stores.TSDataStore;
import rbis.virtualws.stores.DataStore;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import org.unijena.jams.JAMS;
import org.unijena.jams.io.XMLIO;
import org.unijena.jams.runtime.JAMSClassLoader;
import org.unijena.jams.runtime.JAMSRuntime;
import org.unijena.jams.runtime.StandardRuntime;
import org.w3c.dom.Document;
import rbis.virtualws.stores.ASCIIConverter;

public class VirtualWorkspace {

    private String wsTitle;
    private HashMap<String, DataStore> dataStores = new HashMap<String, DataStore>();
    private JAMSRuntime runtime = new StandardRuntime();
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private File directory = null;

    public VirtualWorkspace(File directory, JAMSRuntime runtime) {
        this.runtime = runtime;
        if (!directory.isDirectory()) {
            this.getRuntime().sendErrorMsg("Error adding datastores: \"" + directory.getAbsolutePath() + "\" is not a directory!");
        } else {
            this.directory = directory;
            this.createDataStores();
        }
    }

    public DataStore addDataStore(Document doc) {

        DataStore store = null;
        String type = doc.getDocumentElement().getTagName();

        if (type.equals("tableda tastore")) {
            store = new TableDataStore(this, doc);
        } else if (type.equals("tsdatastore")) {
            store = new TSDataStore(this, doc);
        } else if (type.equals("geodatastore")) {
            store = new GeoDataStore(this, doc);
        }

        if (store != null) {
            dataStores.put(store.getID(), store);
        }
        return store;
    }
    
    public void reload() {
        
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

    public Set<String> getDataStoreNames() {
        return this.dataStores.keySet();
    }

    public void removeDataStore(DataStore store) {
        dataStores.remove(store);
    }

    public DataStore getDataStore(String dsTitle) {
        return dataStores.get(dsTitle);
    }

    public String getTitle() {
        return wsTitle;
    }

    public void setTitle(String title) {
        this.wsTitle = title;
    }

    private void createDataStores() {

        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.getPath().endsWith(".xml")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] children = directory.listFiles(filter);
        for (File child : children) {
            try {

                Document doc = XMLIO.getDocument(child.getAbsolutePath());
                DataStore store = this.addDataStore(doc);
                if (store != null) {
                    this.getRuntime().println("Added store \"" + store.getID() + "\" from \"" + child.getAbsolutePath() + "\"", JAMS.VERBOSE);
                }

            } catch (FileNotFoundException fnfe) {
                this.getRuntime().sendErrorMsg("Error reading datastore \"" + child.getAbsolutePath() + "\"!");
            }
        }
    }

    public static void main(String[] args) {
        File f = new File("D:/jamsapplication/JAMS-Gehlberg/data/vworkspace");
        JAMSRuntime runtime = new StandardRuntime();
        runtime.setDebugLevel(JAMS.STANDARD);
        runtime.addErrorLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });
        runtime.addInfoLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });
        
        VirtualWorkspace ws = new VirtualWorkspace(f, runtime);

        for (String name : ws.getDataStoreNames()) {
            System.out.println(name);
        }
        
        DataStore store = ws.getDataStore("tmean_timeseries");
        ASCIIConverter asciiConverter = new ASCIIConverter(store);
        System.out.println(asciiConverter.toASCIIString());
        store.close();
    }

    public static void main2(String[] args) throws Exception {

//        Document doc = XMLIO.getDocument("D:/jams/RBISDesk/tabledatastore2.xml");
        Document doc = XMLIO.getDocument("D:/jams/RBISDesk/tsdatastore.xml");
        String[] libs = {"D:/nbprojects/RBISDesk/dist", "D:/nbprojects/RBISDesk/dist/lib"};

        VirtualWorkspace ws = new VirtualWorkspace(null, null);
        ws.getRuntime().setDebugLevel(JAMS.VERBOSE);
        ws.getRuntime().addErrorLogObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println(arg);
            }
        });

        ws.setLibs(libs);

        DataStore store = ws.addDataStore(doc);
        ASCIIConverter asciiConverter = new ASCIIConverter(store);
        System.out.println(asciiConverter.toASCIIString());
        store.close();

    /*
    JAMSCalendar cal = new JAMSCalendar();
    cal.setValue(new GregorianCalendar());
    cal.set(1925, 10, 1, 0, 0, 0);
    System.out.println(cal);
    System.out.println(Math.round((double) cal.getTimeInMillis() / 1000)); //should be "1925-11-01 00:00" / -1393804800
     */
    }
}

