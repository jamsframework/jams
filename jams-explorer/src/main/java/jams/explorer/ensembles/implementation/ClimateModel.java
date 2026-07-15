/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.implementation;


import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.meta.ModelDescriptor;
import jams.meta.ModelIO;
import jams.tools.XMLTools;
import jams.workspace.dsproc.AbstractDataStoreProcessor;
import jams.workspace.dsproc.DataStoreProcessor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

/**
 *
 * @author christian
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ClimateModel extends AbstractModel<ClimateModel>{
        
    transient static public final String GCM = "GCM";
    transient static public final String GCM_MEMBER = "GCM Member";
    transient static public final String RCM = "RCM";
    transient static public final String SCENARIO = "Scenario";
    transient static public final String TIME = "Time";
    transient static public final String FILE_PREFIX = "File-Prefix";
    transient static public final String LOCATION = "Location";
    transient static public final String MODEL_FILE = "Model-File";
    
    @XmlTransient
    public File basePath = new File("");
    
    @XmlElement
    HashMap<String, TreeSet<File>> outputFiles = new HashMap<String, TreeSet<File>>();
    
    @XmlElement
    TreeSet<String> selectedDirectories = new TreeSet<String>();
    
    @XmlTransient
    static TreeSet<String> propertySet = new TreeSet<String>(){
        {
            add(GCM);
            add(GCM_MEMBER);
            add(RCM);
            add(SCENARIO);
            add(TIME);
            add(FILE_PREFIX);
            add(LOCATION);
            add(MODEL_FILE);
        }
    };
    
    @XmlTransient
    static Properties defaults = new Properties(){
        {
            put(GCM, "unknown");
            put(GCM_MEMBER, "unknown");
            put(RCM, "unknown");
            put(LOCATION, "");
            put(SCENARIO, "unknown");
            put(TIME, "1970-01-01 1970-01-01 6 1");
            put(FILE_PREFIX, "unknown");
            put(MODEL_FILE, "unknown");
        }
    };
    
    public ClimateModel(){
        this(0);
    }
    public ClimateModel(int id){
        super(id);
        
    }
    public ClimateModel(int id, File modelFile){
        super(id);
        
        init(modelFile);
    }
    
    private void init(File modelFile){
        Document doc = XMLTools.getDocument(modelFile.getAbsolutePath());
        ModelDescriptor md = ModelIO.getStandardModelIO().loadModelDescriptor(doc, ClassLoader.getSystemClassLoader(), true);
        String workspace = (md.getWorkspacePath());
        if (workspace.isEmpty()){
            workspace = modelFile.getParentFile().getAbsolutePath();
        }
        //do it first!!
        loadConfigFile(new File(workspace, "config.txt"));                    
        
        setBasePath(new File(workspace));
        setLocation(new File(workspace));
        setModelFile(modelFile);        
    }
    
    public void setBasePath(File newbasePath){
        basePath = newbasePath;
        collectOutputs();
    }
    
    public void relocate(File newbasePath){
        File path = this.getLocation();
        File modelFile = this.getModelfile();
        basePath = newbasePath;
        this.setLocation(path);
        this.setModelFile(modelFile);
    }
    
    private void collectOutputs() {        
        this.outputFiles.clear();
        
        if (getLocation()==null)
            return;
        
        File outputDirectory = new File(getLocation(), "/output");
        if ( !outputDirectory.exists() )
            return;
        
        for (File subDirectory : outputDirectory.listFiles()){
            if (!subDirectory.isDirectory())
                continue;
            TreeSet<File> outputSet = new TreeSet<File>();

            for (File f : subDirectory.listFiles()) {
                if (!f.isFile())
                    continue;
                switch (AbstractDataStoreProcessor.getDataStoreType(f)) {                    
                    case SpatioTemporal: break;                    
                    case Timeserie:break;                        
                    default:
                        continue;
                }
                outputSet.add(f);
            }
            outputFiles.put(subDirectory.getName(), outputSet);
            selectedDirectories.add(subDirectory.getName());
        }
    }
    
    public ClimateModel(ClimateModel m){
        super(m);                
    }
    
    @Override
    public ClimateModel copy(){
        return new ClimateModel(this);
    }
    
    @Override
    public void delete() throws IOException{
        Files.delete((getLocation()).toPath());
    }
    
    public ClimateModel[] createDuplicates(int n) throws IOException{
        int counter = 0;
        ClimateModel models[] = new ClimateModel[n];
        
        for (int i=0;i<n;i++){
            File workspace = this.getLocation();
            if (workspace.isFile() || !workspace.exists())
                return new ClimateModel[0];
            File newWorkspace = new File(workspace.getAbsoluteFile() + "(" + (counter+1) + ")");
            while(newWorkspace.exists()){
                counter++;
            }
            FileUtils.copyDirectory(workspace, newWorkspace);

            models[i] = new ClimateModel(this);
            models[i].setLocation(newWorkspace);
            //TODO setPath in modelfile .. 
        }
        return models;
    }

    public DataStoreProcessor[] getDataStoreProcessors(String name){
        ArrayList<DataStoreProcessor> dsProcs = new ArrayList<DataStoreProcessor>();
        for (String outputDirectory : this.outputFiles.keySet()){
            if (!isOutputSelected(outputDirectory)){
                continue;
            }                   
            File outputDatastore = new File(new File(new File(this.getLocation(),"output"),outputDirectory), name);           
            if (!outputDatastore.exists())
                continue;
            
            DataStoreProcessor dsProc = new DataStoreProcessor(outputDatastore);
            dsProcs.add(dsProc);
        }
            
        return dsProcs.toArray(new DataStoreProcessor[0]);
    }
    
    public boolean isOutputSelected(String directory){
        return this.selectedDirectories.contains(directory);
    }
    
    @Override
    public Set<String> getOutputs(){
        return this.outputFiles.keySet();
    }
    
    public Set<File> getOutputFiles(String directory){
        return this.outputFiles.get(directory);
    }
    
    public void setOutputSelection(String directory, boolean selected){
        if (selected)
            this.selectedDirectories.add(directory);
        else
            this.selectedDirectories.remove(directory);   
        
        this.setProperty("output", Arrays.toString(selectedDirectories.toArray()));
    }
    
    public void toggleOutputSelection(String directory){
        if (isOutputSelected(directory))
            setOutputSelection(directory, false);
        else
            setOutputSelection(directory, true);
    }
    
    protected boolean loadConfigFile(File configFile){        
        Properties p = new Properties(defaults);
        
        try{
            p.load(new FileReader(configFile));
        }catch(IOException ioe){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, ioe.toString(), ioe);
        }
        
        p.forEach(new BiConsumer<Object, Object>(){
            @Override
            public void accept(Object key, Object value) {
                setProperty(key.toString(), value.toString());
            }
            
        });
        return true;
    }
    
    public void save(){
        saveConfigFile(new File(getLocation(), "config.txt"));
    }
    
    protected boolean saveConfigFile(File configFile){        
        Properties p = new Properties(defaults);
        
        for (String s : getProperties()){
            p.put(s, getProperty(s));
        }
                        
        try{
            p.store(new FileWriter(configFile),"");
        }catch(IOException ioe){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, ioe.toString(), ioe);
        }
               
        return true;
    }
    
    public void setGCM(String gcm){
        setProperty(GCM, gcm);
    }

    public String getGCM(){
        return getProperty(GCM);
    }

    public void setModelFile(File f){        
        Path p_shape = f.toPath();
        Path p_base  = basePath.toPath();
       
        String relativePath = p_base.relativize(p_shape).toString();
        
        setProperty(MODEL_FILE, relativePath);
    }

    public File getModelfile(){
        return new File(basePath,getProperty(MODEL_FILE));
    }

    public void setGCMMember(String GCMMember){
        setProperty(GCM_MEMBER, GCMMember);
    }

    public String getGCMMember(){
        return getProperty(GCM_MEMBER);
    }

    public void setRCM(String rcm){
        setProperty(RCM, rcm);
    }

    public String getRCM(){
        return getProperty(RCM);
    }

    public void setScenario(String scenario){
        setProperty(SCENARIO, scenario);
    }

    public String getScenario(){
        return getProperty(SCENARIO);
    }

    public void setFilePrefix(String prefix){
        setProperty(FILE_PREFIX, prefix);
    }

    public String getFilePrefix(){
        return getProperty(FILE_PREFIX);
    }
    
    public void setTimePeriod(Attribute.TimeInterval t){
        setProperty(TIME, t.toString());
    }
    

    public Attribute.TimeInterval getTimePeriod(){
        Attribute.TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
        try{
            if (getProperty(TIME) != null)
                ti.setValue(getProperty(TIME));
        }finally{
            return ti;
        }
    }
    
    public void setLocation(File f){
        Path p_shape = f.toPath();
        Path p_base  = basePath.toPath();
        
        String relativePath = p_base.relativize(p_shape).toString();
        setRelativeLocation(relativePath);
        collectOutputs();
    }
    
    public File getLocation(){
        if (basePath == null | getRelativeLocation() == null){
            return null;
        }
        return new File(basePath, getRelativeLocation());
    }
    
    public void setRelativeLocation(String relativePath){
        setProperty(LOCATION, relativePath);
        collectOutputs();        
    }
    
    public String getRelativeLocation(){
        return getProperty(LOCATION);
    }
            
    @Override
    public String toString(){
        String s = String.format("[%d] ", id) + " ";
        if (getRCM()!=null) s+= getRCM() + "-";
        if (getGCM()!=null) s+= getGCM();
        if (getScenario()!=null) s+= " (" + getScenario() + ")";
                                
        if (s.isEmpty()){
            s = "unnamed";
        }
        return s;
    }
}
