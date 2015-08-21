/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import optas.data.ensemble.DefaultEnsemble;
import optas.data.ensemble.DefaultSimpleEnsemble;
import optas.data.ensemble.DefaultEfficiencyEnsemble;
import optas.data.ensemble.DefaultTimeSerieEnsemble;
import optas.data.time.MeasuredTimeSerie;
import com.google.common.base.Preconditions;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import optas.data.api.DataCollection;
import optas.data.api.DataSet;
import optas.data.api.DataView;
import optas.data.api.DataSetChangeListener;
import optas.data.api.ModelRun;
import optas.core.OPTASException;
import optas.data.ensemble.api.SimpleEnsemble;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;
import optas.data.ensemble.api.TimeSerieEnsemble;
import optas.data.view.ViewFactory;
import optas.data.time.DefaultTimeSerie;
import optas.data.view.ArrayView;
import optas.optimizer.management.SampleFactory;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class DefaultDataCollection extends DefaultDataSet implements Serializable, DataCollection {
    
    TimeInterval timeDomain;
    TimeFilter timeFilter;
    HashMap<Integer, Boolean> idFilter = new HashMap<Integer, Boolean>();

    HashMap<Integer, ModelRun> set = new HashMap<>();
    HashMap<String, DataSet> globalDatasets = new HashMap<String, DataSet>();

    HashMap<String, Class> datasets;
    String samplerClass;

    boolean isBusy = false;
            
    public DefaultDataCollection(String name) {
        super(name);
        datasets = new HashMap<>();
    }

    @Override
    public DataCollection clone(){
        DataCollection copy = new DefaultDataCollection(getName());
        
        for (ModelRun r : this.getModelRuns()){
            copy.addModelRun(r.clone());
        }
        for (String neam : this.getGlobalDataSetNames()){
            copy.addGlobalDataSet(this.getGlobalDataset(name).clone());
        }
        
        copy.setIDFilter(this.getIDFilter());
        
        for (DataSetChangeListener dcl : listeners)
            copy.addDatasetChangeListener(dcl);
        
        copy.setParent(this.getParent());
        
        for (Entry<String, String> e : propertyMap.entrySet()){
            copy.setProperty(e.getKey(), e.getValue());
        }
        
        copy.setTimeFilter(this.getTimeFilter());        
    }
      
    @Override
    public Collection<ModelRun> getModelRuns(){        
        return Collections.unmodifiableCollection(this.set.values());
    }
    
    @Override
    public ModelRun getModelRun(Integer id){    
        Preconditions.checkNotNull(id, "Id must not be null!");
        return set.get(id);
    }
    
    @Override
    public ArrayView<Integer> getModelRunIds() {
        Set<Integer> filteredSet = new TreeSet<>();
        filteredSet.addAll(set.keySet());
        filteredSet.removeAll(idFilter.keySet());
        return ViewFactory.createView(filteredSet.toArray(new Integer[filteredSet.size()]));
    }
    
    @Override
    public void addDataSet(DataSet s){
        Preconditions.checkNotNull(s, "DataSet must not be null!");
        
        if (getGlobalDataset(s.getName())!=null){
            throw new OPTASException(String.format("Can't add DataSet %s to DataCollection %s. A global DataSet with the same name is already existing!", s.getName(), getName()));
        }
        
        if (getDatasetClass(s.getName())!=null){
            throw new OPTASException(String.format("Can't add DataSet %s to DataCollection %s. A DataSet with the same name is already existing!", s.getName(), getName()));
        }
        
        globalDatasets.put(s.getName(), s);
        this.datasets.put(s.getName(), s.getClass());
        s.setParent(this);

        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
    }
        
    @Override
    public Set<String> getDataSetNames() {
        Set<String> result = new TreeSet<>();
        result.addAll(datasets.keySet());
        result.addAll(globalDatasets.keySet());
        return Collections.unmodifiableSet(result);
    }
    
    @Override
    public Class getDatasetClass(String name) {
        Preconditions.checkNotNull(name, "Name must not be null!");
        return this.datasets.get(name);
    }
    
    @Override
    public Set<String> getDataSetNames(Class clazz) {
        return getDataSetNames(clazz, false);
    }

    public <T extends DataSet> T getDataSet(String datasetName, Class<T> clazz) {
        Preconditions.checkNotNull(datasetName, "Dataset must not be null");
        Preconditions.checkNotNull(clazz, "Class must not be null");
        
        Class datasetClass = this.getDatasetClass(datasetName);
        Preconditions.checkNotNull(datasetClass, "Dataset %s is not part of DataCollection %s", datasetName, clazz);
        Preconditions.checkArgument(clazz.isAssignableFrom(datasetClass), "Dataset %s is not of type %s", datasetName, clazz);
        
        if (DefaultEnsemble.class.isAssignableFrom(clazz)){
            return (T)getEnsembleDataSet(datasetName, clazz);
        }else{       
            return (T)getGlobalDataset(datasetName);
        }
    }
     
    private <T extends DataSet> T getEnsembleDataSet(String datasetName, Class<T> clazz){
        if (this.getDatasetClass(datasetName).equals(TimeSerieEnsemble.class)){
            return (T)getTimeserieEnsemble(datasetName);
        }else if(this.getDatasetClass(datasetName).equals(SimpleEnsemble.class)){
            DefaultSimpleEnsemble e = getSimpleEnsemble(datasetName);
            return (T)e;
        }            
        return null;
    }
    
        
        
    
    @Override
    public Set<String> getDataSetNames(Class clazz, boolean forbidSubTypes) {
        Preconditions.checkNotNull(clazz, "Class must not be null!");
        
        TreeSet<String> result = new TreeSet<>();
        for (String name : datasets.keySet()) {
            boolean isMatch;
            Class classOfDataSet = getDatasetClass(name);
            
            if (!forbidSubTypes){
                isMatch = clazz.isAssignableFrom(classOfDataSet);
            }else{
                isMatch = clazz.equals(classOfDataSet);
            }
            
            if (isMatch){
                result.add(name);
            }
        }
        return result;
    }
        
    @Override
    public void removeDataSet(String name) {
        Preconditions.checkNotNull(name, "Name must not be null!");
        
        this.datasets.remove(name);
        this.globalDatasets.remove(name);
        
        for (ModelRun r : this.set.values()) {
            r.removeDataSet(name);
        }
    }

    @Override
    public void renameDataSet(String oldname, String newname) {
        Preconditions.checkNotNull(oldname, "Old name must not be null!");
        Preconditions.checkNotNull(newname, "New name must not be null!");
        
        DataSet dataset = this.getDataSet(oldname);
        removeDataSet(dataset.getName());
        dataset.setName(newname);
        addDataSet(dataset);
    }

    private void filterID(Integer id) {
        this.idFilter.put(id, Boolean.TRUE);
    }
    
    public void commitFilter() {
        for (Integer id : this.idFilter.keySet()) {
            this.removeModelRun(id);
        }
        idFilter.clear();
    }
    
    public boolean filter(String e, double low, double high, boolean inverse) {
        if (!e.equals("ID")) {
            DataSet ensemble = this.getDataSet(e);
            DefaultSimpleEnsemble effEnsemble = null;
            if (ensemble == null) {
                return false;
            }
            if (ensemble instanceof DefaultSimpleEnsemble) {
                effEnsemble = (DefaultSimpleEnsemble) ensemble;
            } else {
                return false;
            }
            Integer ids[] = effEnsemble.getIds();
            for (Integer id : ids) {
                double value = effEnsemble.getValue(id);
                if (!inverse) {
                    if (value < low || value > high) {
                        filterID(id);
                    }
                } else {
                    if (value >= low && value <= high) {
                        filterID(id);
                    }
                }
            }
        } else {
            Integer ids[] = this.getModelRunIds();
            for (Integer id : ids) {
                if (!inverse) {
                    if (id < low || id > high) {
                        filterID(id);
                    }
                } else {
                    if (id >= low && id <= high) {
                        filterID(id);
                    }
                }
            }
        }
        return true;
    }

    public boolean filterPercentil(String e, double low, double high, boolean inverse) {
        DataSet ensemble = this.getDataSet(e);
        DefaultSimpleEnsemble effEnsemble = null;
        if (ensemble == null) {
            return false;
        }
        if (ensemble instanceof DefaultSimpleEnsemble) {
            effEnsemble = (DefaultSimpleEnsemble) ensemble;
        } else {
            return false;
        }

        Integer ids[] = effEnsemble.sort();

        if (!inverse) {
            for (int i = 0; i < ids.length * low; i++) {
                filterID(ids[i]);
            }

            for (int i = ids.length - 1; i > high * ids.length; i--) {
                filterID(ids[i]);
            }
        } else {
            for (int i = (int) (ids.length * low); i < (int) (high * ids.length); i++) {
                filterID(ids[i]);
            }
        }

        return true;
    }
    
    private void filterTimeDomain(TimeFilter f, String s) {
        throw new IllegalArgumentException();
        /*if (Measurement.class.isAssignableFrom(this.getDatasetClass(s))) {
         Measurement m = (Measurement) this.getDataSet(s);
         if (timeFilter != null) {
         m.removeTimeFilter(timeFilter);
         }
         m.addTimeFilter(f);
         }*/
    }

    public void filterTimeDomain(TimeFilter f) {
        Set<String> set = this.getDataSetNames(MeasuredTimeSerie.class);
        for (String s : set) {
            filterTimeDomain(f, s);
        }
        this.timeFilter = f;
    }

    public void clearTimeDomainFilter() {
        throw new IllegalArgumentException();
        /*Set<String> set = this.getDatasets(Measurement.class);
         for (String s : set){
         if ( Measurement.class.isAssignableFrom(this.getDatasetClass(s)) ){
         Measurement m = (Measurement)this.getDataSet(s);
         if (timeFilter!=null){
         m.removeTimeFilter(timeFilter);
         }
         }
         }
         this.timeFilter = null;*/
    }

    public void clearIDFilter() {
        this.idFilter.clear();
    }

    public boolean unifyDataCollections(DataCollection dc) {
        isBusy = true;
        Integer srcIdList[] = dc.getModelRunIds();
        Integer dstIdList[] = getModelRunIds();

        Set<Integer> srcIdSet = new HashSet<Integer>();
        Set<Integer> dstIdSet = new HashSet<Integer>();

        Set<Integer> commonIds = new HashSet<Integer>();
        Set<Integer> newIds = new HashSet<Integer>();

        dstIdSet.addAll(Arrays.asList(dstIdList));
        srcIdSet.addAll(Arrays.asList(srcIdList));

        commonIds.addAll(srcIdSet);
        commonIds.retainAll(dstIdSet);

        newIds.addAll(srcIdSet);
        newIds.removeAll(commonIds);

        for (Integer id : commonIds) {
            ModelRun srcR = dc.getModelRun(id);
            ModelRun dstR = dc.getModelRun(id);

            DataView<? extends DataSet> list = srcR.getDataSets();
            for (DataSet d : list) {
                if (dstR.getDataSet(d.getName()) == null) {
                    dstR.addDataSet(d);
                    if (!this.datasets.containsKey(d.getName())) {
                        this.datasets.put(d.getName(), d.getClass());
                    }
                }
            }
        }

        for (Integer id : newIds) {
            ModelRun dstR = dc.getModelRun(id);
            //TODO copy dstR
            try {
                this.addModelRun(dstR);
            } catch (OPTASException me) {
                me.printStackTrace();
                return false;
            }
        }
        //add global datasets
        for (String datasetName : dc.getGlobalDataSetNames()) {
            if (!globalDatasets.containsKey(datasetName)) {
                DataSet d = dc.getGlobalDataset(datasetName);
                globalDatasets.put(datasetName, d);
                this.datasets.put(datasetName, d.getClass());
            }
        }

        isBusy = false;
        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
        return true;
    }
               
    public DataSet getGlobalDataset(String name){
        return globalDatasets.get(name);
    }
    
    public void mergeDataCollections(DataCollection dc) {
        isBusy = true;
        Integer ids[] = dc.getModelRunIds();
        int lastID = this.getSimulationCount();
        int offset = 0;
        Arrays.sort(ids);
        for (int i = 0; i < ids.length; i++) {
            ModelRun r = dc.getModelRun(ids[i]);
            Integer newID = new Integer(i + lastID + offset);
            while (this.set.containsKey(newID)) {
                offset++;
                newID++;
            }
            this.addModelRun(new DefaultModelRun(r, newID));
        }
        for (String datasetName : dc.getGlobalDataSetNames()) {
            if (!globalDatasets.containsKey(datasetName)) {
                DataSet d = dc.getGlobalDataset(name);//.getGlobalDatasets().get(datasetName);
                globalDatasets.put(datasetName, d);
                this.datasets.put(datasetName, d.getClass());
            }
        }
        isBusy = false;
        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
    }
   
    private void registerDatasets(ModelRun r) {
        for (DataSet d : r.getDataSets()) {
            if (!this.datasets.containsKey(d.getName())) {
                this.datasets.put(d.getName(), d.getClass());
            }
        }
    }
    
    @Override
    public void addDataSet(SimpleEnsemble ensemble) {
        isBusy = true;
        Integer srcIdList[] = ensemble.getIds();
        Integer dstIdList[] = getModelRunIds();

        Set<Integer> srcIdSet = new HashSet<Integer>();
        Set<Integer> dstIdSet = new HashSet<Integer>();

        Set<Integer> commonIds = new HashSet<Integer>();
        Set<Integer> newIds = new HashSet<Integer>();

        dstIdSet.addAll(Arrays.asList(dstIdList));
        srcIdSet.addAll(Arrays.asList(srcIdList));

        commonIds.addAll(srcIdSet);
        commonIds.retainAll(dstIdSet);

        newIds.addAll(srcIdSet);
        newIds.removeAll(commonIds);

        for (Integer id : commonIds) {
            double srcR = ensemble.getValue(id);

            ModelRun dstR = this.set.get(id);

            try {
                if (ensemble instanceof DefaultEfficiencyEnsemble) {
                    dstR.addDataSet(new Efficiency(new SimpleDataSet(ensemble.getName(), srcR), ((DefaultEfficiencyEnsemble) ensemble).isPostiveBest));
                } else {
                    dstR.addDataSet(new SimpleDataSet(ensemble.getName(), srcR));
                }
            } catch (OPTASException me) {
                me.printStackTrace();
            }
        }

        for (Integer id : newIds) {
            DefaultModelRun dstR = new DefaultModelRun("Run #" + id, id);
            double srcR = ensemble.getValue(id);

            if (ensemble instanceof DefaultEfficiencyEnsemble) {
                dstR.addDataSet(new Efficiency(new SimpleDataSet(ensemble.getName(), srcR), ((DefaultEfficiencyEnsemble) ensemble).isPostiveBest));
            } else {
                dstR.addDataSet(new SimpleDataSet(ensemble.getName(), srcR));
            }
            this.addModelRun(dstR);
        }
        if (ensemble instanceof DefaultEfficiencyEnsemble) {
            if (((DefaultEfficiencyEnsemble) ensemble).isPostiveBest) {
                this.datasets.put(ensemble.getName(), PositiveEfficiency.class);
            } else {
                this.datasets.put(ensemble.getName(), NegativeEfficiency.class);
            }
        } else {
            this.datasets.put(ensemble.getName(), Parameter.class);
        }

        isBusy = false;
        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
    }

    public void addDataSet(TimeSerieEnsemble ensemble) throws OPTASException {
        this.isBusy = true;

        // split existing and new IDs
        Integer[] ensembleIDArray = ensemble.getIds();
        Integer[] collectionIDArray = this.getModelRunIds();
        Set<Integer> ensembleIDSet = new HashSet<>(Arrays.asList(ensembleIDArray));
        Set<Integer> collectionIDSet = new HashSet<>(Arrays.asList(collectionIDArray));
        Set<Integer> existingIDSet = new HashSet<>(collectionIDSet);
        existingIDSet.retainAll(ensembleIDSet);
        Set<Integer> newIDSet = new HashSet<>(ensembleIDSet);
        newIDSet.removeAll(existingIDSet);

        // add datasets for existing IDs
        for (Integer id : existingIDSet) {
            DataView values = ViewFactory.createView(ensemble.getValue(id));
            TimeSerie series = new DefaultTimeSerie(ensemble.getName(), ensemble.getTemporalDomain(), values);
            this.set.get(id).addDataSet(series);
            this.datasets.put(series.getName(), series.getClass());
        }

        // add datasets for non-existing IDs
        for (Integer id : newIDSet) {
            DataView values = ViewFactory.createView(ensemble.getValue(id));
            TimeSerie series = new DefaultTimeSerie(ensemble.getName(), ensemble.getTemporalDomain(), values);
            DefaultModelRun run = new DefaultModelRun("name", id);
            run.addDataSet(series);
            this.addModelRun(run);
            this.datasets.put(series.getName(), series.getClass());
        }

        this.isBusy = false;
        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
    }


    public void setGlobalTimeDomain(TimeInterval interval) {
        if (this.timeDomain == null) {
            this.timeDomain = DefaultDataFactory.getDataFactory().createTimeInterval();
        }
        this.timeDomain.setStart(interval.getStart().clone());
        this.timeDomain.setEnd(interval.getEnd().clone());
        this.timeDomain.setTimeUnit(interval.getTimeUnit());
        this.timeDomain.setTimeUnitCount(interval.getTimeUnitCount());
    }

    //this is highly inefficient!!
    // --> update time domain incrementally when new dataset is added?
    public void updateTimeDomain() {
        if (this.timeDomain == null) {
            this.timeDomain = DefaultDataFactory.getDataFactory().createTimeInterval();
        }
        this.timeDomain.getStart().set(1000, 1, 1, 1, 1, 1);
        this.timeDomain.getEnd().set(10000, 1, 1, 1, 1, 1);

        Set<String> datasets = this.getDataSetNames(TimeSerie.class);
        for (String dataset : datasets) {
            TimeInterval t = ((TimeSerie) this.getDataSet(dataset)).getTemporalDomain();
            if (t == null) {
                continue;
            }

            if (this.timeDomain.getStart().after(t.getStart())) {
                this.timeDomain.setStart(t.getStart().clone());
            }
            if (this.timeDomain.getEnd().before(t.getEnd())) {
                this.timeDomain.setEnd(t.getEnd().clone());
            }
        }
    }

    

    @Override
    public void addModelRun(ModelRun run) throws OPTASException {
        ModelRun r = this.set.get(run.getId());
        Preconditions.checkArgument(r==null, "ModelRun cannot be added to DataCollection! Id %s is already existing!", run.getId());
        if (r != null){
            throw new OPTASException("ModelRun");
        }
        if (r == null) {
            run.setParent(this);
            this.set.put(run.getId(), run);
        } else {
            for (DataSet d : run.getDataSets()) {
                r.addDataSet(d);
            }
        }
        registerDatasets(run);
        fireDatasetChangeEvent(new DefaultDataSetChangeEvent(this));
    }

    @Override
    public void removeModelRun(Integer id) {
        set.remove(id);
    }

    @Override
    public int getSimulationCount() {
        return this.getModelRunIds().length;
        //return this.set.size();
    }
                
    public class StringLexOrder implements Comparator {

        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;

            if (s1.length() < s2.length()) {
                return -1;
            } else if (s1.length() > s2.length()) {
                return 1;
            } else {
                return s1.compareTo(s2);
            }
        }
    }

    public DefaultSimpleEnsemble getSimpleEnsemble(String dataset) {
        double value[] = new double[this.set.size()];
        Integer id[] = new Integer[this.set.size()];

        int c = 0;

        TreeSet<Integer> sortedKeySet = new TreeSet<Integer>();
        sortedKeySet.addAll(Arrays.asList(this.getModelRunIds()));

        for (Integer s : sortedKeySet) {
            ModelRun r = this.set.get(s);
            DataSet d = r.getDataSet(dataset);
            if (d != null && d instanceof SimpleDataSet) {
                SimpleDataSet sd = (SimpleDataSet) d;
                value[c] = sd.getValue();
                id[c] = s;
                c++;
            }
        }
        DefaultSimpleEnsemble se = new DefaultSimpleEnsemble(dataset, c);
        for (int i = 0; i < c; i++) {
            se.add(id[i], value[i]);
        }
        return se;
    }

    public DefaultTimeSerieEnsemble getTimeserieEnsemble(String dataset) {
        TreeSet<Integer> sortedKeySet = new TreeSet<Integer>();
        sortedKeySet.addAll(Arrays.asList(this.getModelRunIds()));

        double value[][] = new double[sortedKeySet.size()][];
        Integer id[] = new Integer[sortedKeySet.size()];

        int c = 0;

        TimeInterval unifiedTimeInterval = DefaultDataFactory.getDataFactory().createTimeInterval();
        Calendar c1 = DefaultDataFactory.getDataFactory().createCalendar();
        Calendar c2 = DefaultDataFactory.getDataFactory().createCalendar();

        long startTime = Long.MIN_VALUE;
        long endTime = Long.MAX_VALUE;

        int timeUnit = 0;
        int timeUnitCount = 0;

        for (Integer s : sortedKeySet) {
            ModelRun r = this.set.get(s);
            DataSet d = r.getDataSet(dataset);
            if (d != null && d instanceof TimeSerie) {
                TimeSerie sd = (TimeSerie) d;
                //hier sollte mal noch gechekct werden, dass die zeitserien konsistent sind.
                //ansonsten mismatch exception. .. 
                if (sd.getTemporalDomain().getTimeUnit() != timeUnit
                        || sd.getTemporalDomain().getTimeUnitCount() != timeUnitCount) {
                    timeUnit = sd.getTemporalDomain().getTimeUnit();
                    timeUnitCount = sd.getTemporalDomain().getTimeUnitCount();
                }
                if (sd.getTemporalDomain().getStart().getTimeInMillis() > startTime) {
                    startTime = sd.getTemporalDomain().getStart().getTimeInMillis();
                }
                if (sd.getTemporalDomain().getStart().getTimeInMillis() < endTime) {
                    endTime = sd.getTemporalDomain().getEnd().getTimeInMillis();
                }
            }
        }

        c1.setTimeInMillis(startTime);
        c2.setTimeInMillis(endTime);
        unifiedTimeInterval.setStart(c1);
        unifiedTimeInterval.setEnd(c2);
        unifiedTimeInterval.setTimeUnit(timeUnit);
        unifiedTimeInterval.setTimeUnitCount(timeUnitCount);

        for (Integer s : sortedKeySet) {
            ModelRun r = this.set.get(s);
            DataSet d = r.getDataSet(dataset);
            if (d != null && d instanceof TimeSerie) {
                TimeSerie<Double> sd = (TimeSerie) d;
                value[c] = new double[(int) unifiedTimeInterval.getNumberOfTimesteps()];
                Calendar time = unifiedTimeInterval.getStart().clone();
                long offset = sd.getTemporalDomain().getStartOffset(unifiedTimeInterval);

                if (offset < 0) {
                    System.out.println("critical error, this should never happen");
                    offset = 0;
                }
                int i = 0;
                while (time.before(unifiedTimeInterval.getEnd())) {
                    value[c][i] = sd.getValue(i);
                    time.add(sd.getTemporalDomain().getTimeUnit(), 1);
                    i++;
                }
                id[c] = s;
                c++;
            }
        }
        DefaultTimeSerieEnsemble se = new DefaultTimeSerieEnsemble(dataset, c, unifiedTimeInterval);
        for (int i = 0; i < c; i++) {
            se.add(id[i], value[i]);
        }
        if (this.timeFilter != null) {
            se.addTimeFilter(timeFilter);
        }
        return se;
    }

    /**
     * Returns a set of all available data set types within the collection
     * denoted by their respective class names. Recommended for use in
     * conjunction with the getDataSets(Class c) method.
     *
     * @return set of data types
     *
     * @see #getDataSetNames(Class)
     */
    public Set<Class> getDataSetTypes() {
        Set<Class> types = new HashSet<Class>();
        for (String dataSetNames : datasets.keySet()) {
            Class c = datasets.get(dataSetNames);
            if (types.isEmpty()) {
                types.add(c);
            } else if (!types.contains(c)) {
                types.add(c);
            }
        }
        return types;
    }

    public TimeInterval getTimeDomain() {
        if (timeDomain == null) {
            updateTimeDomain();
            if (timeDomain == null) {
                return null;
            }
        }
        TimeInterval t = DefaultDataFactory.getDataFactory().createTimeInterval();
        t.setStart(timeDomain.getStart().clone());
        t.setEnd(timeDomain.getEnd().clone());
        return t;
    }

    /*public SpatialDataSet getSpatialDomainForModelRunID(Integer modelRunID) {
     return this.set.get(modelRunID).getSpatialDataSet();
     }*/
    public DefaultDataCollection clone() {
        return createFromByteArray(toByteArrayStream());
    }

    private static DefaultDataCollection createFromByteArray(byte[] array) {
        try {
            ByteArrayInputStream fis = new ByteArrayInputStream(array);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DefaultDataCollection dc = (DefaultDataCollection) ois.readObject();
            ois.close();
            return dc;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        return null;
    }

    private byte[] toByteArrayStream() {
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();

            return fos.toByteArray();
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        }
        return null;
    }

    static public DefaultDataCollection createFromFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DefaultDataCollection dc = (DefaultDataCollection) ois.readObject();
            ois.close();
            fis.close();
            return dc;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
        }
        return null;
    }

    public void save(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private void dumpTSEnsemble(File file, ArrayList<DefaultTimeSerieEnsemble> list, boolean createNewFile) throws IOException {
        if (list.isEmpty()) {
            return;
        }

        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        DefaultTimeSerieEnsemble array[] = list.toArray(new DefaultTimeSerieEnsemble[list.size()]);
        int N = array[0].size;
        int T = array[0].getNumberOfTimesteps();

        BufferedWriter write = new BufferedWriter(new FileWriter(file, !createNewFile));

        if (createNewFile) {
            write.write("@context\n");
            write.write("jams.model.JAMSTemporalContext\tdump\t" + T + "\n");
            write.write("@ancestors" + "\n");
            write.write("optas.optimizer.generic\tsampler\t" + N + "\n");
            write.write("@filters" + "\n");
            write.write("@attributes" + "\n");
            String attrString = "ID";
            String types = "JAMSInteger";
            for (DefaultTimeSerieEnsemble s : list) {
                attrString += "\t" + s.getName();
                types += "\t" + "JAMSDouble";
            }
            write.write(attrString + "\n");
            write.write("@types" + "\n");
            write.write(types + "\n");
            write.write("@data" + "\n");
        }
        Integer ids[] = array[0].getIds();
        Arrays.sort(ids);

        for (int i = 0; i < N; i++) {
            int id = ids[i];

            write.write("sampler\t" + id + "\n");
            write.write("@start" + "\n");
            for (int t = 0; t < T; t++) {
                Date date = array[0].getDate(t);
                String entry = sdf.format(date);
                for (int j = 0; j < array.length; j++) {
                    entry += "\t" + array[j].get(t, id);
                }
                write.write(entry + "\n");
            }
            write.write("@end" + "\n");
        }
        write.close();
    }

    public void constructSample(SampleFactory f) {
        ArrayList<DefaultSimpleEnsemble> simpleEnsembles = new ArrayList<DefaultSimpleEnsemble>();
        ArrayList<DefaultEfficiencyEnsemble> effEnsembles = new ArrayList<DefaultEfficiencyEnsemble>();

        for (String s : this.getDataSetNames()) {
            DataSet d = this.getDataSet(s);
            if (d instanceof DefaultEfficiencyEnsemble) {
                effEnsembles.add((DefaultEfficiencyEnsemble) d);
            } else if (d instanceof DefaultSimpleEnsemble) {
                simpleEnsembles.add((DefaultSimpleEnsemble) d);
            }
        }
        int n = simpleEnsembles.size();
        int m = effEnsembles.size();

        if (n == 0 || m == 0) {
            return;
        }

        int N = this.getSimulationCount();
        for (int i = 0; i < N; i++) {
            int id = simpleEnsembles.get(0).getId(i);
            double x[] = new double[n];
            double y[] = new double[m];

            for (int j = 0; j < n; j++) {
                x[j] = simpleEnsembles.get(j).getValue(id);
            }
            for (int j = 0; j < m; j++) {
                if (effEnsembles.get(j).isPostiveBest) {
                    y[j] = -effEnsembles.get(j).getValue(id);
                } else {
                    y[j] = effEnsembles.get(j).getValue(id);
                }
            }
            f.getSample(x, y);
        }
    }

    private void dumpSimpleEnsemble(File file, ArrayList<DefaultSimpleEnsemble> list, boolean createNewFile, boolean append) throws IOException {
        if (list.isEmpty()) {
            return;
        }

        int N = list.get(0).getSize();

        BufferedWriter write = new BufferedWriter(new FileWriter(file, !createNewFile));
        if (createNewFile) {
            write.write("@context" + "\n");
            write.write("dumping.context\tdump\t" + N + "\n");
            write.write("@ancestors" + "\n");
            write.write("@filters" + "\n");
            write.write("@attributes" + "\n");
            String attrString = "ID";
            String types = "JAMSInteger";
            for (DefaultSimpleEnsemble s : list) {
                attrString += "\t" + s.getName();
                types += "\t" + "JAMSDouble";
            }
            write.write(attrString + "\n");
            write.write("@types" + "\n");
            write.write(types + "\n");
            write.write("@data" + "\n");
            write.write("@start" + "\n");
        }
        DefaultSimpleEnsemble array[] = list.toArray(new DefaultSimpleEnsemble[list.size()]);
        Integer ids[] = array[0].getIds();
        Arrays.sort(ids);
        for (int i = 0; i < N; i++) {
            int id = ids[i];
            String entry = "" + id;
            for (int j = 0; j < array.length; j++) {
                entry += "\t" + array[j].getValue(id);
            }

            write.write(entry + "\n");
        }
        if (createNewFile && !append) {
            write.write("@end");
        }

        write.close();
    }

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd_HHmmss");
    HashMap<File, String> dirFileMap = new HashMap<File, String>();
    HashMap<File, ArrayList<Integer>> idsSavedInDump = new HashMap<File, ArrayList<Integer>>();

    //simple dump to save the datacollection NOW
    public void dump(File directory, boolean append) {
        ArrayList<DefaultSimpleEnsemble> simpleEnsembles = new ArrayList<DefaultSimpleEnsemble>();
        ArrayList<DefaultTimeSerieEnsemble> tsEnsembles = new ArrayList<DefaultTimeSerieEnsemble>();
        ArrayList<MeasuredTimeSerie> msEnsembles = new ArrayList<MeasuredTimeSerie>();

        boolean createNewFile = !append || dirFileMap.get(directory) == null;

        ArrayList<Integer> savedIds = null;
        String dateString = sdf2.format(new Date());

        if (append && !createNewFile) {
            dateString = dirFileMap.get(directory);
            savedIds = idsSavedInDump.get(directory);
            this.clearIDFilter();
            for (Integer id : savedIds) {
                this.filterID(id);
            }
        } else if (append) {
            dirFileMap.put(directory, dateString);
            savedIds = new ArrayList<Integer>();
            idsSavedInDump.put(directory, savedIds);
        }

        for (String s : this.getDataSetNames()) {
            DataSet d = this.getDataSet(s);
            if (d instanceof DefaultSimpleEnsemble) {
                simpleEnsembles.add((DefaultSimpleEnsemble) d);
            } else if (d instanceof DefaultTimeSerieEnsemble) {
                tsEnsembles.add((DefaultTimeSerieEnsemble) d);
            } else if (d instanceof MeasuredTimeSerie) {
                msEnsembles.add((MeasuredTimeSerie) d);
            }
        }

        try {
            dumpSimpleEnsemble(new File(directory.getAbsolutePath() + "/scalar_" + dateString + ".dat"), simpleEnsembles, createNewFile, append);
            dumpTSEnsemble(new File(directory.getAbsolutePath() + "/timeseries_" + dateString + ".dat"), tsEnsembles, createNewFile);
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
        }
        if (savedIds != null) {
            savedIds.addAll(Arrays.asList(this.getModelRunIds()));
            //not really nice, since a previous set filter will be lost
            this.clearIDFilter();
        }
    }
}
