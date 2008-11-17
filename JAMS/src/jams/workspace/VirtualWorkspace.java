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
package jams.workspace;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import jams.workspace.stores.GeoDataStore;
import jams.workspace.stores.TableDataStore;
import jams.workspace.stores.TSDataStore;
import jams.workspace.stores.InputDataStore;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.io.XMLIO;
import jams.runtime.JAMSClassLoader;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import org.w3c.dom.Document;
import jams.workspace.stores.DataStore;
import jams.workspace.stores.J2KTSDataStore;
import jams.workspace.stores.OutputDataStore;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

public class VirtualWorkspace {

    public static final String DUMP_MARKER = "#JAMSdatadump";
    private static final String CONFIG_FILE_NAME = "config.txt",  CONFIG_FILE_COMMENT = "JAMS workspace configuration",  CONTEXT_ATTRIBUTE_NAME = "context",  INPUT_DIR_NAME = "input",  OUTPUT_DIR_NAME = "output",  TEMP_DIR_NAME = "tmp",  DUMP_DIR_NAME = "dump",  LOCAL_INDIR_NAME = "local";
    private HashMap<String, Document> inputDataStores = new HashMap<String, Document>();
    private HashMap<String, Document> outputDataStores = new HashMap<String, Document>();
    private HashMap<String, ArrayList<String>> contextStores = new HashMap<String, ArrayList<String>>();
    private JAMSRuntime runtime;
    private transient ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private transient File directory,  inputDirectory,  outputDirectory = null,  outputDataDirectory,  localInputDirectory,  localDumpDirectory,  tmpDirectory;
    private Properties properties = new Properties();
    private ArrayList<DataStore> currentStores = new ArrayList<DataStore>();

    public VirtualWorkspace(File directory, JAMSRuntime runtime) {
        this.runtime = runtime;
        this.classLoader = runtime.getClassLoader();
        this.directory = directory;
        loadConfig();

        if (isValid()) {
            this.createDataStores();
        }
    }

    public void loadConfig() {
        try {

            properties.setProperty("description", "");
            properties.setProperty("title", "");
            properties.setProperty("persistent", "false");

            File file = new File(directory.getPath() + File.separator + CONFIG_FILE_NAME);
            if (file.exists()) {
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                properties.load(is);
            } else {
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                properties.store(os, CONFIG_FILE_COMMENT);
            }
        } catch (IOException ioe) {
            runtime.handle(ioe);
        }
    }

    public void saveConfig() {
        try {
            File file = new File(directory.getPath() + File.separator + CONFIG_FILE_NAME);
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(os, CONFIG_FILE_COMMENT);
        } catch (IOException ioe) {
            runtime.handle(ioe);
        }
    }

    public boolean isValid() {

        if (!directory.isDirectory()) {
            return false;
        }

        try {
            File inDir = new File(directory, INPUT_DIR_NAME);
            File outDir = new File(directory, OUTPUT_DIR_NAME);
            File tmpDir = new File(directory, TEMP_DIR_NAME);
            File localInDir = new File(inDir, LOCAL_INDIR_NAME);
            File localDumpDir = new File(localInDir, DUMP_DIR_NAME);

            inDir.mkdirs();
            outDir.mkdirs();
            tmpDir.mkdirs();
            localInDir.mkdirs();
            localDumpDir.mkdirs();

            this.inputDirectory = inDir;
            this.outputDirectory = outDir;
            this.localInputDirectory = localInDir;
            this.localDumpDirectory = localDumpDir;
            this.tmpDirectory = tmpDir;

            if (this.isPersistent()) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
                this.outputDataDirectory = new File(this.outputDirectory.getPath() + File.separator + sdf.format(cal.getTime()));
            } else {
                this.outputDataDirectory = new File(this.outputDirectory.getPath() + File.separator + "current");
            }

            return true;
        } catch (SecurityException se) {
            return false;
        }
    }

    private String getStoreID(File file) {

        String id = file.getName();
        StringTokenizer tok = new StringTokenizer(id, ".");
        if (tok.countTokens() > 1) {
            id = tok.nextToken();
        }

        return id;
    }

    private String getContextName(Document doc) {
        return doc.getDocumentElement().getAttribute(CONTEXT_ATTRIBUTE_NAME);
    }

    public void reload() {
    }

    /**
     * Creates an individual class loader
     * @param libs Array of libs that the new classloader will be based on
     */
    public void setLibs(String[] libs) {
        this.classLoader = JAMSClassLoader.createClassLoader(libs, runtime);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public JAMSRuntime getRuntime() {
        return runtime;
    }

    public Set<String> getInputDataStoreIDs() {
        return this.inputDataStores.keySet();
    }

    public Set<String> getOutputDataStoreIDs() {
        return this.outputDataStores.keySet();
    }

    public void removeDataStore(InputDataStore store) {
        inputDataStores.remove(store);
    }

    public InputDataStore getInputDataStore(String dsTitle) {

        Document doc = inputDataStores.get(dsTitle);
        if (doc == null) {
            return null;
        }

        InputDataStore store = null;
        String type = doc.getDocumentElement().getTagName();

        try {
            if (type.equals("tabledatastore")) {
                store = new TableDataStore(this, dsTitle, doc);
            } else if (type.equals("tsdatastore")) {
                store = new TSDataStore(this, dsTitle, doc);
            } else if (type.equals("j2ktsdatastore")) {
                store = new J2KTSDataStore(this, dsTitle, doc);
            } else if (type.equals("geodatastore")) {
                store = new GeoDataStore(this, dsTitle, doc);
            }
        } catch (ClassNotFoundException cnfe) {
            getRuntime().sendErrorMsg(JAMS.resources.getString("Error_initializing_datastore_") + dsTitle + JAMS.resources.getString("!"));
            getRuntime().handle(cnfe);
            return null;
        } catch (IOException ioe) {
            getRuntime().sendErrorMsg(JAMS.resources.getString("Error_initializing_datastore_") + dsTitle + JAMS.resources.getString("!"));
            getRuntime().handle(ioe);
            return null;
        }

        return store;
    }

    public OutputDataStore[] getOutputDataStores(String contextName) {

        ArrayList<String> stores = contextStores.get(contextName);

        if (stores == null) {
            return new OutputDataStore[0];
        }

        OutputDataStore[] result = new OutputDataStore[stores.size()];
        int i = 0;
        for (String storeID : stores) {
            Document doc = outputDataStores.get(storeID);
            OutputDataStore store = new OutputDataStore(this, doc, storeID);
            currentStores.add(store);
            result[i] = store;
            i++;
        }

        return result;
    }

    public void close() {
        for (DataStore store : currentStores) {
            try {
                store.close();
            } catch (IOException ioe) {
                runtime.handle(ioe);
            }
        }
    }

    public String getTitle() {
        return properties.getProperty("title");
    }

    public void setTitle(String title) {
        properties.setProperty("title", title);
    }

    public boolean isPersistent() {
        return Boolean.parseBoolean(properties.getProperty("persistent"));
    }

    public void setPersistent(boolean inc) {
        properties.setProperty("persistent", Boolean.toString(inc));
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

        File[] inChildren = inputDirectory.listFiles(filter);
        for (File child : inChildren) {
            try {

                String storeID = getStoreID(child);
                Document doc = XMLIO.getDocument(child.getAbsolutePath());
                inputDataStores.put(storeID, doc);
                this.getRuntime().println(JAMS.resources.getString("Added_input_store_") + storeID + JAMS.resources.getString("_from_") + child.getAbsolutePath() + JAMS.resources.getString("."), JAMS.VERBOSE);

            } catch (FileNotFoundException fnfe) {
                this.getRuntime().sendErrorMsg(JAMS.resources.getString("Error_reading_datastore_") + child.getAbsolutePath() + JAMS.resources.getString("!"));
            }
        }

        File[] outChildren = outputDirectory.listFiles(filter);
        for (File child : outChildren) {
            try {

                Document doc = XMLIO.getDocument(child.getAbsolutePath());

                String storeID = getStoreID(child);
                String contextName = getContextName(doc);

                outputDataStores.put(storeID, doc);

                ArrayList<String> stores = contextStores.get(contextName);
                if (stores == null) {
                    stores = new ArrayList<String>();
                    contextStores.put(contextName, stores);
                }
                stores.add(storeID);

                this.getRuntime().println(JAMS.resources.getString("Added_output_store_") + storeID + JAMS.resources.getString("_from_") + child.getAbsolutePath() + JAMS.resources.getString("."), JAMS.VERBOSE);

            } catch (FileNotFoundException fnfe) {
                this.getRuntime().sendErrorMsg(JAMS.resources.getString("Error_reading_datastore_") + child.getAbsolutePath() + JAMS.resources.getString("!"));
            }
        }
    }

    public String dataStoreToString(String dsTitle) throws IOException {
        InputDataStore store = this.getInputDataStore(dsTitle);
        return dataStoreToString(store);
    }

    public String dataStoreToString(InputDataStore store) throws IOException {
        if (store == null) {
            return null;
        }

        if (store instanceof TSDataStore) {
            TSDumpProcessor asciiConverter = new TSDumpProcessor();
            String result = asciiConverter.toASCIIString((TSDataStore) store);
            return result;
        } else {
            return store.getClass().toString() + JAMS.resources.getString("_not_yet_supported!");
        }
    }

    public void inputDataStoreToFile(String dsTitle) throws IOException {
        InputDataStore store = this.getInputDataStore(dsTitle);

        if (store == null) {
            return;
        }

        if (store instanceof TSDataStore) {
            TSDumpProcessor asciiConverter = new TSDumpProcessor();
            File file = new File(this.getLocalDumpDirectory(), dsTitle + ".dump");
            asciiConverter.toASCIIFile((TSDataStore) store, file);
            getRuntime().sendInfoMsg(JAMS.resources.getString("Dumped_input_datastore_1") + dsTitle + JAMS.resources.getString("Dumped_input_datastore_2") + file + JAMS.resources.getString("Dumped_input_datastore_3"));
        }

        store.close();
    }

    public void inputDataStoreToFile() throws IOException {
        for (String dsTitle : this.getInputDataStoreIDs()) {
            inputDataStoreToFile(dsTitle);
        }
    }

    public File getDirectory() {
        return directory;
    }

    public File getInputDirectory() {
        return inputDirectory;
    }

    public File getOutputDirectory(boolean increment) {
        return outputDirectory;
    }

    public File getOutputDataDirectory() {
        return outputDataDirectory;
    }

    public File getLocalInputDirectory() {
        return localInputDirectory;
    }

    public File getLocalDumpDirectory() {
        return localDumpDirectory;
    }

    public File getTempDirectory() {
        return tmpDirectory;
    }

    public static void main(String[] args) throws IOException {

        JAMSRuntime runtime = new StandardRuntime();
        runtime.setDebugLevel(JAMS.VERBOSE);
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

        JAMSProperties properties = JAMSProperties.createJAMSProperties();
        properties.load("D:/jamsapplication/nsk.jap");
        String[] libs = JAMSTools.toArray(properties.getProperty("libs", ""), ";");

        VirtualWorkspace ws = new VirtualWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg"), runtime);
        //VirtualWorkspace ws = new VirtualWorkspace(new File("D:/jamsapplication/ws_test"), runtime);
        ws.setLibs(libs);

        //System.out.println(ws.dataStoreToString("tmin"));
        //ws.inputDataStoreToFile("tmin");
        ws.inputDataStoreToFile();

    }
}

