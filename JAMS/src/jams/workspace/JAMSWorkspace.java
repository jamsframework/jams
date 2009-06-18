/*
 * JAMSWorkspace.java
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
import jams.workspace.stores.TableDataStore;
import jams.workspace.stores.TSDataStore;
import jams.workspace.stores.InputDataStore;
import java.util.HashMap;
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
import jams.workspace.stores.ShapeFileDataStore;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

public class JAMSWorkspace implements Serializable {

    /**
     * Comment string used to mark dump files of input datastores
     */
    public static final String DUMP_MARKER = "#JAMSdatadump", OUTPUT_FILE_ENDING = ".dat" ;

    private static final String CONFIG_FILE_NAME = "config.txt",  CONFIG_FILE_COMMENT = "JAMS workspace configuration",  CONTEXT_ATTRIBUTE_NAME = "context";

    private static final String INPUT_DIR_NAME = "input",  OUTPUT_DIR_NAME = "output",  TEMP_DIR_NAME = "tmp",  DUMP_DIR_NAME = "dump",  LOCAL_INDIR_NAME = "local";

    private HashMap<String, Document> inputDataStores = new HashMap<String, Document>();

    private HashMap<String, Document> outputDataStores = new HashMap<String, Document>();

    private HashMap<String, ArrayList<String>> contextStores = new HashMap<String, ArrayList<String>>();

    private JAMSRuntime runtime;

    private transient ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    private transient File directory,  inputDirectory,  outputDirectory = null,  outputDataDirectory,  localInputDirectory,  localDumpDirectory,  tmpDirectory;

    private Properties properties = new Properties();

    private ArrayList<DataStore> currentStores = new ArrayList<DataStore>();

    public JAMSWorkspace(File directory, JAMSRuntime runtime) throws InvalidWorkspaceException {
        this(directory, runtime, false);
    }

    public JAMSWorkspace(File directory, JAMSRuntime runtime, boolean readonly) throws InvalidWorkspaceException {

        this.runtime = runtime;
        if (runtime.getClassLoader() != null) {
            this.classLoader = runtime.getClassLoader();
        }
        this.directory = directory;

        this.loadConfig();
        this.checkValidity(readonly);
        this.createDataStores();
    }

    /**
     * Loads the workspace config from the config file in the root of the
     * workspace directory
     */
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

    /**
     * Saves the workspace config in the config file in the root of the
     * workspace directory
     */
    public void saveConfig() {
        try {
            File file = new File(directory.getPath() + File.separator + CONFIG_FILE_NAME);
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(os, CONFIG_FILE_COMMENT);
        } catch (IOException ioe) {
            runtime.handle(ioe);
        }
    }

    /**
     * Checks if this workspace is valid
     * @param readonly If readonly is false, the workspace can be fixed (e.g.
     * missing directories will be created), otherwise not
     * @throws jams.workspace.JAMSWorkspace.InvalidWorkspaceException
     */
    public void checkValidity(boolean readonly) throws InvalidWorkspaceException {

        if (!directory.isDirectory()) {
            throw new InvalidWorkspaceException(JAMS.resources.getString("Error_during_model_setup:_") +
                    directory.toString() + JAMS.resources.getString("_is_not_a_directory"));
        }

        File configFile = new File(directory, "config.txt");
        if (!configFile.exists()) {
            throw new InvalidWorkspaceException(JAMS.resources.getString("Error_during_model_setup:_") +
                    directory.toString() + JAMS.resources.getString("_does_not_contain_config_file"));
        }

        File inDir = new File(directory, INPUT_DIR_NAME);
        File outDir = new File(directory, OUTPUT_DIR_NAME);
        File tmpDir = new File(directory, TEMP_DIR_NAME);
        File localInDir = new File(inDir, LOCAL_INDIR_NAME);
        File localDumpDir = new File(localInDir, DUMP_DIR_NAME);

        if (readonly) {
            File[] allDirs = {inDir, outDir, localInDir, localDumpDir};
            for (File dir : allDirs) {
                if (!dir.exists()) {
                    throw new InvalidWorkspaceException(JAMS.resources.getString("Error_during_model_setup:_") +
                            directory.toString() + JAMS.resources.getString("_does_not_contain_needed_directory_") +
                            dir.toString() + JAMS.resources.getString("_)"));
                }
            }
            this.inputDirectory = inDir;
            this.outputDirectory = outDir;
            this.localInputDirectory = localInDir;
            this.localDumpDirectory = localDumpDir;
            this.tmpDirectory = tmpDir;

        } else {

            try {

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

            } catch (SecurityException se) {
                throw new InvalidWorkspaceException(JAMS.resources.getString("Error_during_model_setup:_") +
                        directory.toString() + JAMS.resources.getString("_is_not_a_valid_workspace!"));
            }
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

    /**
     * Creates an individual class loader
     * @param libs Array of libs that the new classloader will be based on
     */
    public void setLibs(String[] libs) {
        this.classLoader = JAMSClassLoader.createClassLoader(libs, runtime);
    }

    /**
     *
     * @return The classloader that this workspace uses
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     *
     * @return The JAMSRuntime object that this workspace uses
     */
    public JAMSRuntime getRuntime() {
        return runtime;
    }

    /**
     *
     * @return A set of the names of all input datastores
     */
    public Set<String> getInputDataStoreIDs() {
        return this.inputDataStores.keySet();
    }

    /**
     *
     * @return A set of the names of all output datastores
     */
    public Set<String> getOutputDataStoreIDs() {
        return this.outputDataStores.keySet();
    }

    /**
     * Removes a datastore from the list of datastores
     * @param store The datastore to be removed
     */
    public void removeDataStore(InputDataStore store) {
        inputDataStores.remove(store.getID());
    }

    /**
     *
     * @param dsTitle The name of the datastore to be returned
     * @return An input datastore named by dsTitle
     */
    public InputDataStore getInputDataStore(String dsTitle) {

        Document doc = inputDataStores.get(dsTitle);
        if (doc == null) {
            return null;
        }

        InputDataStore store = null;
        String type = doc.getDocumentElement().getTagName();

        try {
            if (type.equals(InputDataStore.TYPE_TABLEDATASTORE)) {
                store = new TableDataStore(this, dsTitle, doc);
            } else if (type.equals(InputDataStore.TYPE_TSDATASTORE)) {
                store = new TSDataStore(this, dsTitle, doc);
            } else if (type.equals(InputDataStore.TYPE_J2KTSDATASTORE)) {
                store = new J2KTSDataStore(this, dsTitle, doc);
            } else if (type.equals(InputDataStore.TYPE_SHAPEFILEDATASTORE)) {
                store = new ShapeFileDataStore(this, dsTitle, doc);
            }
        } catch (ClassNotFoundException cnfe) {
            getRuntime().sendErrorMsg(JAMS.resources.getString("Error_initializing_datastore_") + dsTitle + JAMS.resources.getString("!"));
            getRuntime().handle(cnfe);
            return null;
        } catch (IOException ioe) {
            getRuntime().sendErrorMsg(JAMS.resources.getString("Error_initializing_datastore_") + dsTitle + JAMS.resources.getString("!"));
            getRuntime().handle(ioe);
            return null;
        } catch (URISyntaxException use) {
            getRuntime().sendErrorMsg(JAMS.resources.getString("Error_initializing_datastore_") + dsTitle + JAMS.resources.getString("!"));
            getRuntime().handle(use);
            return null;
        }

        return store;
    }

    /**
     *
     * @param contextName The instance name of a context component
     * @return All output datastores defined for the given context
     */
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

    /**
     * Closes the workspace, i.e. closes all datastores
     */
    public void close() {
        for (DataStore store : currentStores) {
            try {
                store.close();
            } catch (IOException ioe) {
                runtime.handle(ioe);
            }
        }
    }

    /**
     *
     * @return The workspace title
     */
    public String getTitle() {
        return properties.getProperty("title");
    }

    /**
     * Sets the workspace title
     * @param title The title
     */
    public void setTitle(String title) {
        properties.setProperty("title", title);
    }

    /**
     *
     * @return The workspace description
     */
    public String getDescription() {
        return properties.getProperty("description");
    }

    /**
     * Sets the workspace description
     * @param description The description
     */
    public void setDescription(String description) {
        properties.setProperty("description", description);
    }

    /**
     *
     * @return If the data output directory will be overwritten or not
     */
    public boolean isPersistent() {
        return Boolean.parseBoolean(properties.getProperty("persistent"));
    }

    /**
     * Defines if the data output directory is overwritten or not
     * @param persistent If persistent is true, a new data output directory will be created
     * for model output, otherwise output will be directed to standard data
     * output directory ("current")
     */
    public void setPersistent(boolean persistent) {
        properties.setProperty("persistent", Boolean.toString(persistent));
    }

    private void createDataStores() {

        FileFilter filter = new FileFilter() {

            @Override
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

    /**
     * Creates a string dump of an input datastore
     * @param dsTitle The name of the datastore to be dumped
     * @return The string representation of the datastore
     * @throws java.io.IOException
     */
    public String dataStoreToString(String dsTitle) throws IOException {
        InputDataStore store = this.getInputDataStore(dsTitle);
        return dataStoreToString(store);
    }

    /**
     * Creates a string dump of an input datastore
     * @param store The datastore to be dumped
     * @return The string representation of the datastore
     * @throws java.io.IOException
     */
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

    /**
     * Creates a file dump of an input datastore
     * @param dsTitle The name of the datastore to be dumped
     * @throws java.io.IOException
     */
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

    /**
     * Creates file dumps of all input datastores
     * @throws java.io.IOException
     */
    public void inputDataStoreToFile() throws IOException {
        for (String dsTitle : this.getInputDataStoreIDs()) {
            inputDataStoreToFile(dsTitle);
        }
    }

    /**
     * Get the IDs of all datastores of a given type
     * @param type The type to look for
     * @return A String array containg the datastores IDs
     */
    public String[] getDataStoreIDs(String type) {
        
        ArrayList<String> list = new ArrayList<String>();
        
        for (String dsTitle : this.getInputDataStoreIDs()) {
            Document doc = inputDataStores.get(dsTitle);
            String thisType = doc.getDocumentElement().getTagName();
            if (type.equals(thisType)) {
                list.add(dsTitle);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     *
     * @return The directory of the workspace
     */
    public File getDirectory() {
        return directory;
    }

    /**
     *
     * @return The input directory, i.e. the directory where the input
     * datastores are defined
     */
    public File getInputDirectory() {
        return inputDirectory;
    }

    /**
     *
     * @return The current data output directory
     */
    public File getOutputDataDirectory() {
        return outputDataDirectory;
    }

    /**
     * 
     * @return All existing data output directories
     */
    public File[] getOutputDataDirectories() {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && !pathname.getName().endsWith(".svn")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return outputDirectory.listFiles(filter);
    }

    /**
     *
     * @param outputDataDirectory An output data directory
     * @return All existing data files from a given output data directory
     */
    public File[] getOutputDataFiles(File outputDataDirectory) {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(OUTPUT_FILE_ENDING)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return outputDataDirectory.listFiles(filter);
    }

    /**
     *
     * @return The directory where local input files are stored
     */
    public File getLocalInputDirectory() {
        return localInputDirectory;
    }

    /**
     *
     * @return The directory where input datastore dump files are stored
     */
    public File getLocalDumpDirectory() {
        return localDumpDirectory;
    }

    /**
     *
     * @return The temp directory
     */
    public File getTempDirectory() {
        return tmpDirectory;
    }

    public class InvalidWorkspaceException extends Exception {

        public InvalidWorkspaceException(String msg) {
            super(msg);
        }
    }

    public static void main(String[] args) throws IOException {

        JAMSRuntime runtime = new StandardRuntime();
        runtime.setDebugLevel(JAMS.VERBOSE);
        runtime.addErrorLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });
        runtime.addInfoLogObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                System.out.print(arg);
            }
        });

        JAMSProperties properties = JAMSProperties.createJAMSProperties();
        properties.load("D:/jamsapplication/nsk.jap");
        String[] libs = JAMSTools.toArray(properties.getProperty("libs", ""), ";");


        JAMSWorkspace ws;
        try {
            ws = new JAMSWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg"), runtime, true);
        } catch (InvalidWorkspaceException iwe) {
            System.out.println(iwe.getMessage());
            return;
        }
        //JAMSWorkspace ws = new JAMSWorkspace(new File("D:/jamsapplication/ws_test"), runtime);
        ws.setLibs(libs);

        //System.out.println(ws.dataStoreToString("tmin"));
        //ws.inputDataStoreToFile("tmin");
        ws.inputDataStoreToFile();

    }
}

