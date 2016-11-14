/*
 * JAMSWorkspace.java
 * Created on 23. Januar 2008, 15:42
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
package jams.workspace;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import jams.workspace.stores.TableDataStore;
import jams.workspace.stores.TSDataStore;
import jams.workspace.stores.InputDataStore;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import jams.JAMS;
import jams.JAMSException;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.model.SmallModelState;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jams.tools.XMLTools;
import org.w3c.dom.Document;
import jams.workspace.stores.DataStore;
import jams.workspace.stores.DefaultOutputDataStore;
import jams.workspace.stores.J2KTSDataStore;
import jams.workspace.stores.OutputDataStore;
import jams.workspace.stores.ShapeFileDataStore;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.TreeSet;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JAMSWorkspace implements Workspace {

    private static final String CONFIG_FILE_NAME = "config.txt", CONFIG_FILE_COMMENT = "JAMS workspace configuration", CONTEXT_ATTRIBUTE_NAME = "context";
    public static final String INPUT_DIR_NAME = "input", OUTPUT_DIR_NAME = "output", TEMP_DIR_NAME = "tmp", DUMP_DIR_NAME = "dump", LOCAL_INDIR_NAME = "local", EXPLORER_DIR_NAME = "explorer";
    public static final String SUFFIX_XML = "xml";
    
    private HashMap<String, Document> inputDataStores = new HashMap<String, Document>();
    private HashMap<String, Document> outputDataStores = new HashMap<String, Document>();
    private HashMap<String, Document> registeredInputDataStores = new HashMap<String, Document>();
    private HashMap<String, Document> registeredOutputDataStores = new HashMap<String, Document>();
    private HashMap<String, ArrayList<String>> contextStores = new HashMap<String, ArrayList<String>>();
    private JAMSRuntime runtime;
    private transient ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private File directory, inputDirectory, outputDirectory = null, outputDataDirectory, localInputDirectory, localDumpDirectory, tmpDirectory, explorerDirectory;
    private Properties properties = new Properties();
    private ArrayList<DataStore> currentStores = new ArrayList<DataStore>();
    private boolean readonly;

    public JAMSWorkspace(File directory, JAMSRuntime runtime) {
        this(directory, runtime, false);
    }

    public JAMSWorkspace(File directory, JAMSRuntime runtime, boolean readonly) {

        this.runtime = runtime;
        if (runtime.getClassLoader() != null) {
            this.classLoader = runtime.getClassLoader();
        }
        this.directory = directory;
        this.currentStores = new ArrayList<DataStore>();
        this.readonly = readonly;
    }
    
    public void init() throws InvalidWorkspaceException {
        this.contextStores.clear();
        this.inputDataStores.clear();
        this.outputDataStores.clear();
        
        this.loadConfig();
        this.checkValidity(readonly);
        //this.loadConfig();
        this.updateDataStores();

        Set<DataStore> rmCandidates = new HashSet<DataStore>();
        for (DataStore d : this.currentStores) {
            try {
                if (d instanceof OutputDataStore) {
                    d.close();
                    rmCandidates.add(d);
                } else {
                    d.setWorkspace(this);
                }
            } catch (IOException ioe) {
                throw new InvalidWorkspaceException(ioe.toString());
            }
        }
        currentStores.removeAll(rmCandidates);
    }

    public void setDirectory(File directory) throws InvalidWorkspaceException {
        this.directory = directory;
        init();
    }

    /**
     * Loads the workspace config from the config file in the root of the
     * workspace directory
     */
    @Override
    public void loadConfig() {
        try {
            properties.setProperty("description", "");
            properties.setProperty("title", "");
            properties.setProperty("persistent", "false");
            properties.setProperty("defaultmodel", "model.jam");
            properties.setProperty("INPUT_DIR_NAME", INPUT_DIR_NAME);
            properties.setProperty("OUTPUT_DIR_NAME", OUTPUT_DIR_NAME);
            properties.setProperty("TEMP_DIR_NAME", TEMP_DIR_NAME);
            properties.setProperty("LOCAL_INDIR_NAME", LOCAL_INDIR_NAME);          
            properties.setProperty("DUMP_DIR_NAME", DUMP_DIR_NAME);          
            properties.setProperty("EXPLORER_DIR_NAME", EXPLORER_DIR_NAME);          
                        
            File file = new File(directory, CONFIG_FILE_NAME);
            if (file.exists()) {
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                properties.load(is);
                is.close();
            } else {
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                properties.store(os, CONFIG_FILE_COMMENT);
                os.close();
            }
            
            if (properties.containsKey("readonly")){
                this.readonly = Boolean.parseBoolean(properties.getProperty("readonly"));
            }
        } catch (IOException ioe) {
            runtime.handle(ioe);
        }
    }

    /**
     * Saves the workspace config in the config file in the root of the
     * workspace directory
     */
    @Override
    public void saveConfig() {
        try {
            File file = new File(directory.getPath() + File.separator + CONFIG_FILE_NAME);
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(os, CONFIG_FILE_COMMENT);
        } catch (IOException ioe) {
            runtime.handle(ioe);
        }
    }

    /*restores all datastores from a saved execution state*/
    @Override
    public void restore(SmallModelState state) {
        Iterator<DataStore> iter = this.getRegisteredDataStores().iterator();
        while (iter.hasNext()) {
            try {
                state.recoverDataStoreState(iter.next());
            } catch (IOException e) {
                this.getRuntime().sendHalt(JAMS.i18n("error_occured_while_restoring_model_state") + ":" + e.toString());
                e.printStackTrace();
            }
        }
    }
    /*restores all datastores from a saved execution state*/

    @Override
    public void saveState(SmallModelState state) {
        Iterator<DataStore> iter = this.getRegisteredDataStores().iterator();
        while (iter.hasNext()) {
            state.saveDataStoreState(iter.next());
        }
    }

    /**
     * Checks if this workspace is valid
     *
     * @param readonly If readonly is false, the workspace can be fixed (e.g.
     * missing directories will be created), otherwise not
     * @throws jams.workspace.JAMSWorkspace.InvalidWorkspaceException
     */
    private void checkValidity(boolean readonly) throws InvalidWorkspaceException {
        this.readonly = readonly;
        if (!directory.isDirectory()) {
            throw new InvalidWorkspaceException(JAMS.i18n("Error_during_model_setup:_")
                    + directory.toString() + JAMS.i18n("_is_not_a_directory"));
        }

//        File configFile = new File(directory, "config.txt");
//        if (!configFile.exists()) {
//            throw new InvalidWorkspaceException(JAMS.i18n("Error_during_model_setup:_")
//                    + directory.toString() + JAMS.i18n("_does_not_contain_config_file"));
//        }

//        this.loadConfig();
        
        File inDir = new File(directory, properties.getProperty("INPUT_DIR_NAME"));
        File outDir = new File(directory, properties.getProperty("OUTPUT_DIR_NAME"));
        File tmpDir = new File(directory, properties.getProperty("TEMP_DIR_NAME"));
        File localInDir = new File(inDir, properties.getProperty("LOCAL_INDIR_NAME"));
        File localDumpDir = new File(localInDir, properties.getProperty("DUMP_DIR_NAME"));
        File explorerDir = new File(directory, properties.getProperty("EXPLORER_DIR_NAME"));

        File[] allDirs = {inDir, outDir, localInDir, localDumpDir, explorerDir};

        if (this.readonly) {
            for (File dir : allDirs) {
                if (!dir.exists()) {
                    throw new InvalidWorkspaceException(JAMS.i18n("Error_during_model_setup:_")
                            + directory.toString() + JAMS.i18n("_does_not_contain_needed_directory_")
                            + dir.toString() + JAMS.i18n("_)"));
                }
            }

        } else {

            try {

                // make sure all dirs are existing
                for (File dir : allDirs) {
                    dir.mkdirs();
                }

            } catch (SecurityException se) {
                throw new InvalidWorkspaceException(JAMS.i18n("Error_during_model_setup:_")
                        + directory.toString() + JAMS.i18n("_is_not_a_valid_workspace!"));
            }
        }
                
        this.inputDirectory = inDir;
        this.outputDirectory = outDir;
        this.localInputDirectory = localInDir;
        this.localDumpDirectory = localDumpDir;
        this.tmpDirectory = tmpDir;
        this.explorerDirectory = explorerDir;       
        
        //setup output directory
        if (this.isPersistent()) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            this.outputDataDirectory = new File(this.outputDirectory.getPath() + File.separator + sdf.format(cal.getTime()));
        } else {
            this.outputDataDirectory = new File(this.outputDirectory.getPath() + File.separator + "current");
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
     *
     * @param libs Array of libs that the new classloader will be based on
     */
//    public void setLibs(String[] libs) {
//        this.classLoader = JAMSClassLoader.createClassLoader(libs, runtime);
//    }
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

    public List<String> getSortedInputDataStoreIDs() {
        List<String> inIDList = new ArrayList<String>(this.inputDataStores.keySet());
        Collections.sort(inIDList);
        return inIDList;
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
     *
     * @param store The datastore to be removed
     */
    public void removeDataStore(InputDataStore store) {
        inputDataStores.remove(store.getID());
    }

    public void registerInputDataStore(String id, Document doc) {

        if (StringTools.isEmptyString(id)) {
            return;
        }

        if (doc == null) {
            registeredInputDataStores.remove(id);
            inputDataStores.remove(id);
        } else {
            registeredInputDataStores.put(id, doc);
            inputDataStores.put(id, doc);
        }
    }

    public void registerOutputDataStore(String id, Document doc) {

        if (StringTools.isEmptyString(id)) {
            return;
        }

        if (doc == null) {
            registeredOutputDataStores.remove(id);
        } else {
            registeredOutputDataStores.put(id, doc);
        }
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
            getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Error_initializing_datastore_"), dsTitle));
            getRuntime().handle(cnfe);
            return null;
        } catch (IOException ioe) {
            getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Error_initializing_datastore_"), dsTitle));
            getRuntime().handle(ioe);
            return null;
        } catch (URISyntaxException use) {
            getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Error_initializing_datastore_"), dsTitle));
            getRuntime().handle(use);
            return null;
        }
        currentStores.add(store);
        return store;
    }

    public ArrayList<DataStore> getRegisteredDataStores() {
        return currentStores;
    }

    /**
     *
     * @return A shape input datastore
     */
    public ShapeFileDataStore getFirstShapeInputDataStore() {

        InputDataStore store = null;
        Set<String> ids = getInputDataStoreIDs();
        for (String id : ids) {
            store = getInputDataStore(id);
            if (store instanceof ShapeFileDataStore) {
                return (ShapeFileDataStore) store;
            }
        }
        return null;
    }

    private DataStore getDataStoreByID(String storeID) {
        for (int j = 0; j < currentStores.size(); j++) {
            if (currentStores.get(j).getID().equals(storeID)) {
                return currentStores.get(j);
            }
        }
        return null;
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

        ArrayList<OutputDataStore> result = new ArrayList<OutputDataStore>();

        for (String storeID : stores) {
            OutputDataStore listedStore = (OutputDataStore) getDataStoreByID(storeID);
            if (listedStore != null) {
                result.add(listedStore);
                continue;
            }
            Document doc = outputDataStores.get(storeID);

            Element elem = (Element) doc.getElementsByTagName("plugin").item(0);
            String className = "";
            if (elem != null) {
                className = elem.getAttribute("type");
            }
            OutputDataStore store = null;
            if (!StringTools.isEmptyString(className)) {
                try {
                    ClassLoader loader = this.runtime.getClassLoader();
                    Class<?> clazz = loader.loadClass(className);
                    OutputDataStore io = (OutputDataStore) clazz.newInstance();

                    Method method = clazz.getMethod("setWorkspace", JAMSWorkspace.class);
                    method.invoke(io, this);
                    method = clazz.getMethod("setDoc", Document.class);
                    method.invoke(io, doc);
                    method = clazz.getMethod("setID", String.class);
                    method.invoke(io, storeID);

                    NodeList parameterNodes = elem.getElementsByTagName("parameter");
                    for (int j = 0; j < parameterNodes.getLength(); j++) {
                        Element parameterNode = (Element) parameterNodes.item(j);

                        String attributeName = parameterNode.getAttribute("id");
                        String attributeValue = "";
                        if (parameterNode.hasAttribute("value")) {
                            attributeValue = parameterNode.getAttribute("value");
                        } else {
                            attributeValue = null;
                        }
                        String methodName = StringTools.getSetterName(attributeName);

                        method = clazz.getMethod(methodName, String.class);
                        method.invoke(io, attributeValue);
                    }
                    store = io;
                } catch (Exception ie) {
                    getRuntime().handle(ie);
                    return null;
                }
            } else {
                store = new DefaultOutputDataStore(this, doc, storeID);
            }
            if (store.isValid()) {
                currentStores.add(store);
                result.add(store);
            }
        }
        return result.toArray(new OutputDataStore[result.size()]);
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
     *
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
     *
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
     *
     * @param persistent If persistent is true, a new data output directory will
     * be created for model output, otherwise output will be directed to
     * standard data output directory ("current")
     */
    public void setPersistent(boolean persistent) {
        properties.setProperty("persistent", Boolean.toString(persistent));
    }

    /**
     *
     * @return the id of this workspace
     */
    public int getID() {
        String id = properties.getProperty("id");
        if (id == null)
            return -1;
        else
            return Integer.parseInt(id);
    }
    
    /**
     *
     * @return sets the id of this workspace
     */
    public void setID(int id){
        properties.setProperty("id", Integer.toString(id));
        saveConfig();
    }
    /**
     *
     * @return The default model path of the workspace
     */
    public String getModelFilename() {
        return properties.getProperty("defaultmodel");
    }

    /**
     * Sets the default model path of the workspace
     *
     * @param path The path
     */
    public void setModelFile(String path) {
        properties.setProperty("defaultmodel", path);
    }

    /**
     * create all non-existing datastores according to input- and output-dir
     */
    public void updateDataStores() {

        //clear old map and add pre-registered input datastores
        inputDataStores.clear();
        for (String storeID : registeredInputDataStores.keySet()) {
            inputDataStores.put(storeID, registeredInputDataStores.get(storeID));
            this.getRuntime().println(MessageFormat.format(JAMS.i18n("Added_input_store_"), storeID, "XML"), JAMS.VERBOSE);
        }

        //add input datastores from file system
        File[] inChildren = FileTools.getFiles(inputDirectory, SUFFIX_XML);
        for (File child : inChildren) {
            try {

                String storeID = getStoreID(child);
                if (!inputDataStores.containsKey(storeID)) {
                    Document doc = XMLTools.getDocument(child.getAbsolutePath());
                    inputDataStores.put(storeID, doc);
                    this.getRuntime().println(MessageFormat.format(JAMS.i18n("Added_input_store_"), storeID, child.getAbsolutePath()), JAMS.VERBOSE);
                }

            } catch (JAMSException jex) {
                this.getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Error_reading_datastore_"), child.getAbsolutePath()));
            }
        }

        //clear old map and add pre-registered output datastores
        outputDataStores.clear();
        for (String storeID : registeredOutputDataStores.keySet()) {

            Document doc = registeredOutputDataStores.get(storeID);
            if (isEnabledOutputDS(doc)) {
                outputDataStores.put(storeID, doc);
                this.getRuntime().println(MessageFormat.format(JAMS.i18n("Added_output_store_"), storeID, "XML"), JAMS.VERBOSE);
            }
        }

        //add output datastores from file system
        File[] outChildren = FileTools.getFiles(outputDirectory, SUFFIX_XML);
        for (File child : outChildren) {
            try {

                String storeID = getStoreID(child);
                if (!outputDataStores.containsKey(storeID)) {

                    Document doc = XMLTools.getDocument(child.getAbsolutePath());
                    if (isEnabledOutputDS(doc)) {
                        outputDataStores.put(storeID, doc);
                        this.getRuntime().println(MessageFormat.format(JAMS.i18n("Added_output_store_"), storeID, child.getAbsolutePath()), JAMS.VERBOSE);
                    }
                }

            } catch (JAMSException jex) {
                this.getRuntime().sendErrorMsg(MessageFormat.format(JAMS.i18n("Error_reading_datastore_"), child.getAbsolutePath()));
            }
        }
        for (String storeID : outputDataStores.keySet()) {
            Document doc = outputDataStores.get(storeID);
            String contextName = getContextName(doc);
            ArrayList<String> stores = contextStores.get(contextName);
            if (stores == null) {
                stores = new ArrayList<String>();
                contextStores.put(contextName, stores);
            }
            stores.add(storeID);
        }
    }

    private boolean isEnabledOutputDS(Document doc) {

        Element root = doc.getDocumentElement();
        if (!root.hasAttribute("enabled") || root.getAttribute("enabled").equals(Boolean.toString(true))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates a string dump of an input datastore
     *
     * @param dsTitle The name of the datastore to be dumped
     * @return The string representation of the datastore
     * @throws java.io.IOException
     */
    public String dataStoreToString(String dsTitle) throws IOException {
        InputDataStore store = this.getInputDataStore(dsTitle);
        String data = dataStoreToString(store);
        store.close();
        return data;
    }

    /**
     * Creates a string dump of an input datastore
     *
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
            return store.getClass().toString() + JAMS.i18n("_not_yet_supported!");
        }
    }

    /**
     * Creates a file dump of an input datastore
     *
     * @param store The datastore to be dumped
     * @throws java.io.IOException
     */
    public void inputDataStoreToFile(InputDataStore store) throws IOException {

        if ((store == null) || (store.getAccessMode() == InputDataStore.CACHE_MODE)) {
            return;
        }

        if (store instanceof TSDataStore) {
            TSDumpProcessor asciiConverter = new TSDumpProcessor();
            File file = new File(this.getLocalDumpDirectory(), store.getID() + ".dump");
            asciiConverter.toASCIIFile((TSDataStore) store, file);
            getRuntime().sendInfoMsg(JAMS.i18n("Dumped_input_datastore_1") + store.getID() + JAMS.i18n("Dumped_input_datastore_2") + file + JAMS.i18n("Dumped_input_datastore_3"));
        }

        store.close();
    }

    /**
     * Creates file dumps of all input datastores
     *
     * @throws java.io.IOException
     */
    public void inputDataStoreToFile() throws IOException {
        for (String dsTitle : this.getInputDataStoreIDs()) {
            inputDataStoreToFile(this.getInputDataStore(dsTitle));
        }
    }

    /**
     * Get the IDs of all datastores of a given type
     *
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
        TreeSet<File> files = new TreeSet<File>();        
        File list[] = outputDirectory.listFiles(filter);
        for (File f : list){
            files.add(f);
        }
        
        /*boolean change = true;
        while (change) {
            change = false;
            for (File f : list) {
                if (f.isDirectory()) {
                    File newlist[] = f.listFiles(filter);
                    for (File newFile : newlist){
                        if (!files.contains(newFile)){
                            files.add(newFile);       
                            change = true;
                        }
                    }
                }
            }
        }*/
        return files.toArray(new File[0]);
    }

    /**
     *
     * @param outputDataDirectory An output data directory
     * @return All existing data files from a given output data directory
     */
    @Override
    public File[] getOutputDataFiles(File outputDataDirectory) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()){
                    return true;
                }if (pathname.isFile() && (
                        pathname.getName().endsWith(OUTPUT_FILE_ENDING) || 
                        pathname.getName().endsWith(SPREADSHEET_FILE_ENDING) || 
                        pathname.getName().endsWith(DATACOLLECTION_FILE_ENDING)) || 
                        pathname.getName().endsWith(".csv")) {
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

//    public static void main(String[] args) throws IOException {
//
//        JAMSRuntime runtime = new StandardRuntime();
//        runtime.setDebugLevel(JAMS.VERBOSE);
//        runtime.addErrorLogObserver(new Observer() {
//
//            @Override
//            public void update(Observable o, Object arg) {
//                System.out.print(arg);
//            }
//        });
//        runtime.addInfoLogObserver(new Observer() {
//
//            @Override
//            public void update(Observable o, Object arg) {
//                System.out.print(arg);
//            }
//        });
//
//        SystemProperties properties = JAMSProperties.createProperties();
//        properties.load("D:/jamsapplication/nsk.jap");
//        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");
//
//
//        JAMSWorkspace ws;
//        try {
//            ws = new JAMSWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg"), runtime, true);
//        } catch (InvalidWorkspaceException iwe) {
//            System.out.println(iwe.getMessage());
//            return;
//        }
//        //JAMSWorkspace ws = new JAMSWorkspace(new File("D:/jamsapplication/ws_test"), runtime);
//        ws.setLibs(libs);
//
//        //System.out.println(ws.dataStoreToString("tmin"));
//        //ws.inputDataStoreToFile("tmin");
//
////        ws.inputDataStoreToFile();
//
//        InputDataStore store = ws.getInputDataStore("sunh_db");
//        TSDumpProcessor asciiConverter = new TSDumpProcessor();
//        System.out.println(asciiConverter.toASCIIString((TSDataStore) store));
//    }
    public static void main(String[] args) throws IOException, InvalidWorkspaceException {

        SystemProperties properties = JAMSProperties.createProperties();
        properties.load("e:/jamsapplication/nsk.jap");
        String[] libs = StringTools.toArray(properties.getProperty("libs", ""), ";");

        JAMSRuntime runtime = new StandardRuntime(properties);
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

        JAMSWorkspace ws;
        ws = new JAMSWorkspace(new File("D:/jamsapplication/JAMS-Gehlberg"), runtime, true);
        //JAMSWorkspace ws = new JAMSWorkspace(new File("D:/jamsapplication/ws_test"), runtime);
//        ws.setLibs(libs);
        ws.init();

        //System.out.println(ws.dataStoreToString("tmin"));
        //ws.inputDataStoreToFile("tmin");

//        ws.inputDataStoreToFile();

        InputDataStore store = ws.getInputDataStore("inData");
//        TSDumpProcessor asciiConverter = new TSDumpProcessor();
//        System.out.println(asciiConverter.toASCIIString((TSDataStore) store));
    }
}
