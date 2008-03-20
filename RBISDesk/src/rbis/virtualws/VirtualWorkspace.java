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
import java.io.IOException;
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
import org.w3c.dom.Element;
import rbis.virtualws.stores.ASCIIConverter;

public class VirtualWorkspace {

    private String wsTitle;
    private HashMap<String, Document> dataStores = new HashMap<String, Document>();
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

    public String addDataStore(Document doc) {
        Element root = doc.getDocumentElement();
        String id = root.getAttribute("id");
        dataStores.put(id, doc);
        return id;
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

        Document doc = dataStores.get(dsTitle);

        DataStore store = null;
        String type = doc.getDocumentElement().getTagName();

        if (type.equals("tableda tastore")) {
            store = new TableDataStore(this, doc);
        } else if (type.equals("tsdatastore")) {
            store = new TSDataStore(this, doc);
        } else if (type.equals("geodatastore")) {
            store = new GeoDataStore(this, doc);
        }

        return store;
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
                String storeID = this.addDataStore(doc);
                this.getRuntime().println("Added store \"" + storeID + "\" from \"" + child.getAbsolutePath() + "\"", JAMS.VERBOSE);

            } catch (FileNotFoundException fnfe) {
                this.getRuntime().sendErrorMsg("Error reading datastore \"" + child.getAbsolutePath() + "\"!");
            }
        }
    }

    public String dataStoreToString(String dsTitle) {
        DataStore store = this.getDataStore(dsTitle);
        ASCIIConverter asciiConverter = new ASCIIConverter(store);
        String result = asciiConverter.toASCIIString();
        store.close();
        return result;
    }

    public void dataStoreToFile(String dsTitle, File file) throws IOException {
        DataStore store = this.getDataStore(dsTitle);
        ASCIIConverter asciiConverter = new ASCIIConverter(store);
        asciiConverter.toASCIIFile(file);
        store.close();
    }

    public void wsToFile() throws IOException {
        for (String dsTitle : this.getDataStoreNames()) {

            DataStore store = this.getDataStore(dsTitle);
            File file = new File(this.directory.getAbsolutePath() + File.separator + "_" + dsTitle + ".txt");
            ASCIIConverter asciiConverter = new ASCIIConverter(store);
            asciiConverter.toASCIIFile(file);
            store.close();

        }
    }

    public static void main(String[] args) throws IOException {

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

        VirtualWorkspace ws = new VirtualWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg/data/vworkspace"), runtime);

        //System.out.println(ws.dataStoreToString("tmean_timeseries"));
        //ws.dataStoreToFile("tmean_timeseries", new File("D:/jamsapplication/JAMS-Gehlberg/data/vworkspace/_tmean_dump.txt"));
        ws.wsToFile();
    }
}

