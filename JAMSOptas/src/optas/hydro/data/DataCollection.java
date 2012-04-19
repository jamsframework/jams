/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.data;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import optas.optimizer.management.SampleFactory;

/**
 *
 * @author chris
 */
public class DataCollection extends DataSet implements Serializable{
    TimeInterval timeDomain;    
    TimeFilter timeFilter;
    HashMap<Integer, Boolean> idFilter = new HashMap<Integer, Boolean>();

    HashMap<Integer, Modelrun> set = new HashMap<Integer, Modelrun>();
    HashMap<String, DataSet> globalDatasets = new HashMap<String, DataSet>();

    HashMap<String, Class> datasets;
    String samplerClass;

    boolean isBusy = false;
    transient Set<DatasetChangeListener> listener = new HashSet<DatasetChangeListener>();
    
    public class DatasetChangeEvent implements Serializable{
        Serializable source;
        DataSet target;

        public DatasetChangeEvent(Serializable src, DataSet target){
            this.source = src;
            this.target = target;
        }
    }

    public interface DatasetChangeListener extends Serializable{
        public void datasetChanged(DatasetChangeEvent dc);
    }
    

    public DataCollection() {
        datasets = new HashMap<String, Class>();
    }

    public void addChangeListener(DatasetChangeListener dcl){
        if (listener==null) //can happen after deserialization .. 
            listener = new HashSet<DatasetChangeListener>();
        listener.add(dcl);
    }

    public void fireChangeEvent(DatasetChangeEvent dce){
        if (isBusy)
            return;
        if (listener == null)
            return;
        for (DatasetChangeListener dcl : listener){
            dcl.datasetChanged(dce);
        }
    }

    public void setSamplerClass(String typeId) {
        this.samplerClass = typeId;
    }

    public String getSamplerClass() {
        if (samplerClass == null) {
            return null;//jams.components.optimizer.Optimizer.class;
        }
        return samplerClass;
    }

    private void filterID(Integer id){
        this.idFilter.put(id, Boolean.TRUE);
    }

    public void removeDataset(String name){
        this.datasets.remove(name);
        this.globalDatasets.remove(name);
        for (Integer i : this.set.keySet()){
            set.get(i).removeDataset(name);
        }
    }

    public boolean filter(String e, double low, double high, boolean inverse){
        DataSet ensemble = this.getDataSet(e);
        SimpleEnsemble effEnsemble = null;
        if (ensemble == null)
            return false;
        if (ensemble instanceof SimpleEnsemble)
            effEnsemble = (SimpleEnsemble)ensemble;
        else
            return false;

        Integer ids[] = effEnsemble.getIds();        
        for (Integer id : ids){
            double value = effEnsemble.getValue(id);
            if (!inverse){
                if (value < low || value > high)
                    filterID(id);
            }else{
                if (value >= low && value <= high)
                    filterID(id);
            }
        }
        
        return true;
    }

    public boolean filterPercentil(String e, double low, double high, boolean inverse){
        DataSet ensemble = this.getDataSet(e);
        SimpleEnsemble effEnsemble = null;
        if (ensemble == null)
            return false;
        if (ensemble instanceof SimpleEnsemble)
            effEnsemble = (SimpleEnsemble)ensemble;
        else
            return false;

        Integer ids[] = effEnsemble.sort();

        if (!inverse){
            for (int i=0;i<ids.length*low;i++){
                filterID(ids[i]);
            }

            for (int i=ids.length-1;i>high*ids.length;i--){
                filterID(ids[i]);
            }
        }else{
            for (int i=(int)(ids.length*low);i<(int)(high*ids.length);i++){
                filterID(ids[i]);
            }
        }

        return true;
    }

    public void commitFilter(){
        for (Integer id : this.idFilter.keySet())
            this.removeModelRun(id);
        idFilter.clear();
    }

    public void filterTimeDomain(TimeFilter f){
        Set<String> set = this.getDatasets(Measurement.class);
        for (String s : set){
            if ( Measurement.class.isAssignableFrom(this.getDatasetClass(s)) ){
                Measurement m = (Measurement)this.getDataSet(s);
                if (timeFilter!=null){
                    m.removeTimeFilter(timeFilter);                    
                }
                m.addTimeFilter(f);
            }
        }
        this.timeFilter = f;
    }

    public void clearTimeDomainFilter(){
        Set<String> set = this.getDatasets(Measurement.class);
        for (String s : set){
            if ( Measurement.class.isAssignableFrom(this.getDatasetClass(s)) ){
                Measurement m = (Measurement)this.getDataSet(s);
                if (timeFilter!=null){
                    m.removeTimeFilter(timeFilter);
                }
            }
        }
        this.timeFilter = null;
    }
    public void clearIDFilter(){
        this.idFilter.clear();
    }

    public boolean unifyDataCollections(DataCollection dc){
        isBusy = true;
        Integer srcIdList[] = dc.getModelrunIds();
        Integer dstIdList[] = getModelrunIds();

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

        for (Integer id : commonIds){
            Modelrun srcR = dc.set.get(id);
            Modelrun dstR = this.set.get(id);

            Iterator<DataSet> iter = srcR.getDatasets();
            while (iter.hasNext()){
                DataSet d = iter.next();
                if (dstR.getDataset(d.name)!=null){
                    continue;
                }else{
                    try{
                        //TODO duplicate d
                        dstR.addDataSet(d);
                        if (!this.datasets.containsKey(d.name)) {
                            this.datasets.put(d.name, d.getClass());
                        }
                    }catch(MismatchException me){
                        me.printStackTrace();
                        return false;
                    }
                }
            }
        }

        for (Integer id : newIds){
            Modelrun dstR = dc.set.get(id);
            //TODO copy dstR
            try{
                this.addModelRun(dstR);
            }catch(MismatchException me){
                me.printStackTrace();
                return false;
            }
        }
        //add global datasets
        for (String datasetName : dc.globalDatasets.keySet()) {
            if (!globalDatasets.containsKey(datasetName)) {
                DataSet d = dc.globalDatasets.get(datasetName);
                globalDatasets.put(datasetName, d);
                this.datasets.put(datasetName, d.getClass());
            }
        }

        isBusy = false;
        fireChangeEvent(new DatasetChangeEvent(this, this));
        return true;
    }
    
    public void mergeDataCollections(DataCollection dc){
        isBusy = true;
        Integer ids[] = dc.getModelrunIds();
        int lastID = this.getSimulationCount();
        int offset = 0;
        Arrays.sort(ids);
        for (int i=0;i<ids.length;i++){
            Modelrun r = dc.set.get(ids[i]);
            Integer newID = new Integer(i+lastID+offset);
            while(this.set.containsKey(newID)){                
                offset++;
                newID++;
            }
            r.changeId(newID);
            try{
                this.addModelRun(r);
            }catch(MismatchException me){
                me.printStackTrace();
            }
        }
        for (String datasetName : dc.globalDatasets.keySet()){
            if (!globalDatasets.containsKey(datasetName)){
                DataSet d = dc.globalDatasets.get(datasetName);
                globalDatasets.put(datasetName, d);
                this.datasets.put(datasetName, d.getClass());
            }
        }
        isBusy = false;
        fireChangeEvent(new DatasetChangeEvent(this, this));
    }

    public Integer[] getModelrunIds(){
        Set<Integer> filteredSet = new TreeSet<Integer>();
        filteredSet.addAll(set.keySet());
        filteredSet.removeAll(idFilter.keySet());
        return filteredSet.toArray(new Integer[filteredSet.size()]);
    }

    private void registerDatasets(Modelrun r) {
        Iterator<DataSet> iter = r.getDatasets();
        while (iter.hasNext()) {
            DataSet d = iter.next();
            if (!this.datasets.containsKey(d.name)) {
                this.datasets.put(d.name, d.getClass());
            }
        }
    }

    public void addEnsemble(SimpleEnsemble s){
        isBusy = true;
        Integer srcIdList[] = s.getIds();
        Integer dstIdList[] = getModelrunIds();

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

        for (Integer id : commonIds){
            double srcR = s.getValue(id);

            Modelrun dstR = this.set.get(id);

            try {
                if (s instanceof EfficiencyEnsemble) {
                    dstR.addDataSet(new Efficiency(new SimpleDataSet(srcR, s.name, this), ((EfficiencyEnsemble) s).isPostiveBest));
                } else {
                    dstR.addDataSet(new SimpleDataSet(srcR, s.name, this));
                }
            } catch (MismatchException me) {
                me.printStackTrace();
            }
        }

        for (Integer id : newIds){
            Modelrun dstR = new Modelrun(id,null);
            double srcR = s.getValue(id);

            try {
                if (s instanceof EfficiencyEnsemble) {
                    dstR.addDataSet(new Efficiency(new SimpleDataSet(srcR, s.name, this), ((EfficiencyEnsemble) s).isPostiveBest));
                } else {
                    dstR.addDataSet(new SimpleDataSet(srcR, s.name, this));
                }
                this.addModelRun(dstR);
            } catch (MismatchException me) {
                me.printStackTrace();
            }
        }
        if (s instanceof EfficiencyEnsemble){
            if (((EfficiencyEnsemble)s).isPostiveBest)
                this.datasets.put(s.name, PositiveEfficiency.class);
            else
                this.datasets.put(s.name, NegativeEfficiency.class);
        }else
            this.datasets.put(s.name, Parameter.class);
        
        isBusy = false;
        fireChangeEvent(new DatasetChangeEvent(this, this));
    }
//this is highly inefficient!!
    private void updateTimeDomain(){
        this.timeDomain.getStart().set(1000, 1, 1, 1, 1, 1);
        this.timeDomain.getEnd().set(10000, 1, 1, 1, 1, 1);

        Set<String> set = this.getDatasets(TimeSerie.class);
        for (String s : set){
            TimeInterval t = ((TimeSerie)this.getDataSet(s)).getTimeDomain();

            if (this.timeDomain.getStart().after(t.getStart()))
                this.timeDomain.setStart(t.getStart().clone());
            if (this.timeDomain.getEnd().before(t.getEnd()))
                this.timeDomain.setEnd(t.getEnd().clone());
        }
    }

    public void addTimeSerie(TimeSerie s){
        globalDatasets.put(s.name,s);
        this.datasets.put(s.name, s.getClass());

        fireChangeEvent(new DatasetChangeEvent(this, this));
    }

    public void addModelRun(Modelrun set) throws MismatchException {
        if (set.getTimeDomain()!=null){
            if (this.timeDomain == null)
                this.timeDomain = set.getTimeDomain();
        }
        Modelrun r = this.set.get(set.getId());
        if (r == null) {
            set.parent = this;
            this.set.put(set.getId(), set);
        }else{
            Iterator<DataSet> iter = set.getDatasets();
            while(iter.hasNext()){
                r.addDataSet(iter.next());
            }
        }
        registerDatasets(set);
        fireChangeEvent(new DatasetChangeEvent(this, this));
    }
    public void removeModelRun(Integer id) {
        set.remove(id);
    }

    public int getSimulationCount() {
        return this.getModelrunIds().length;
        //return this.set.size();
    }
    public Set<String> getDatasets(){
        Set<String> result = new TreeSet<String>();
        result.addAll(datasets.keySet());        
        return result;
    }
    public Set<String> getDatasets(Class clazz){
        TreeSet<String> sets = new TreeSet<String>();
        for (String setname : datasets.keySet()){
            if (clazz.getName().contains("TimeSerie")){  // workaround because we want neg and pos efficiencies to be efficiencies but we wont get a measurement when we want a timeseries ensemble arg
                                                            // solution would be to ask for timeserieensembles and simpleensemnles ... 
                if (clazz.equals(getDatasetClass(setname))){
                    sets.add(setname);
                }
            }else if (clazz.isAssignableFrom(getDatasetClass(setname)))
                sets.add(setname);
        }
        return sets;
    }
    public Class getDatasetClass(String name){
        return this.datasets.get(name);
    }

    public DataSet getDataSet(String dataset){
        if (this.getDatasetClass(dataset)==null)
            return null;
        if (this.getDatasetClass(dataset).equals(TimeSerie.class))
            return getTimeserieEnsemble(dataset);
        else if (this.getDatasetClass(dataset).equals(Measurement.class)){            
            return this.globalDatasets.get(dataset);
        } else {
            SimpleEnsemble e = getSimpleEnsemble(dataset);
            if (NegativeEfficiency.class.isAssignableFrom(this.getDatasetClass(dataset))){
                return new EfficiencyEnsemble(e, false);
            }else if(PositiveEfficiency.class.isAssignableFrom(this.getDatasetClass(dataset))){
                return new EfficiencyEnsemble(e, true);
            }else
                return e;
        }
    }

    public class StringLexOrder implements Comparator{
        public int compare(Object o1,Object o2){
            String s1 = (String)o1;
            String s2 = (String)o2;

            if (s1.length() < s2.length())
                return -1;
            else if (s1.length() > s2.length())
                return 1;
            else{
                return s1.compareTo(s2);
            }
        }
    }

    public SimpleEnsemble getSimpleEnsemble(String dataset){
        double value[] = new double[this.set.keySet().size()];
        Integer id[] = new Integer[this.set.keySet().size()];

        int c=0;

        TreeSet<Integer> sortedKeySet = new TreeSet<Integer>();
        sortedKeySet.addAll(Arrays.asList(this.getModelrunIds()));

        for (Integer s : sortedKeySet){
            Modelrun r = this.set.get(s);
            DataSet d = r.getDataset(dataset);
            if (d!=null && d instanceof SimpleDataSet){
                SimpleDataSet sd = (SimpleDataSet)d;
                value[c] = sd.getValue();
                id[c] = s;
                c++;
            }
        }
        SimpleEnsemble se = new SimpleEnsemble(dataset,c);
        for (int i =0;i<c;i++){
            se.add(id[i],value[i]);
        }
        return se;
    }

    public TimeSerieEnsemble getTimeserieEnsemble(String dataset){
        TreeSet<Integer> sortedKeySet = new TreeSet<Integer>();
        sortedKeySet.addAll(Arrays.asList(this.getModelrunIds()));

        double value[][] = new double[sortedKeySet.size()][];
        Integer id[] = new Integer[sortedKeySet.size()];

        int c=0;

        TimeInterval unifiedTimeInterval = JAMSDataFactory.createTimeInterval();
        Calendar c1 = JAMSDataFactory.createCalendar();
        Calendar c2 = JAMSDataFactory.createCalendar();

        long startTime = 0;
        long endTime   = Long.MAX_VALUE;

        int timeUnit = 0;
        int timeUnitCount = 0;

        for (Integer s : sortedKeySet){
            Modelrun r = this.set.get(s);
            DataSet d = r.getDataset(dataset);
            if (d!=null && d instanceof TimeSerie){
                TimeSerie sd = (TimeSerie)d;                
                //hier sollte mal noch gechekct werden, dass die zeitserien konsistent sind.
                //ansonsten mismatch exception. .. 
                if (sd.getTimeDomain().getTimeUnit() != timeUnit ||
                    sd.getTimeDomain().getTimeUnitCount() != timeUnitCount){
                    timeUnit = sd.getTimeDomain().getTimeUnit();
                    timeUnitCount = sd.getTimeDomain().getTimeUnitCount();
                }
                if (sd.getTimeDomain().getStart().getTimeInMillis()>startTime)
                    startTime = sd.getTimeDomain().getStart().getTimeInMillis();
                if (sd.getTimeDomain().getStart().getTimeInMillis()<endTime)
                    endTime = sd.getTimeDomain().getEnd().getTimeInMillis();
            }
        }

        c1.setTimeInMillis(startTime);
        c2.setTimeInMillis(endTime);
        unifiedTimeInterval.setStart(c1);
        unifiedTimeInterval.setEnd(c2);
        unifiedTimeInterval.setTimeUnit(timeUnit);
        unifiedTimeInterval.setTimeUnitCount(timeUnitCount);

        for (Integer s : sortedKeySet){
            Modelrun r = this.set.get(s);
            DataSet d = r.getDataset(dataset);
            if (d!=null && d instanceof TimeSerie){
                TimeSerie sd = (TimeSerie)d;                
                value[c] = new double[(int)unifiedTimeInterval.getNumberOfTimesteps()];
                Calendar time = unifiedTimeInterval.getStart().clone();
                long offset = sd.getTimeDomain().getStartOffset(unifiedTimeInterval);
                
                if (offset<0){
                    System.out.println("critical error, this should never happen");
                    offset = 0;
                }
                int i=0;
                while(time.before(unifiedTimeInterval.getEnd())){
                    value[c][i] = sd.getValue(i);
                    time.add(sd.getTimeDomain().getTimeUnit(), 1);
                    i++;
                }
                id[c] = s;
                c++;
            }
        }
        TimeSerieEnsemble se = new TimeSerieEnsemble(dataset,c, unifiedTimeInterval);
        for (int i =0;i<c;i++){
            se.add(id[i], value[i]);
        }
        if (this.timeFilter!=null)
            se.addTimeFilter(timeFilter);
        return se;
    }

    /**
     * Returns a set of all available data set types within the collection
     * denotedby their respective class names. Recommended for use in
     * conjunction with the getDataSets(Class c) method.
     *
     * @return set of data types
     *
     * @see getDatasets(Class c)
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
        if (timeDomain == null)
            return null;
        TimeInterval t = JAMSDataFactory.createTimeInterval();
        t.setStart(timeDomain.getStart().clone());
        t.setEnd(timeDomain.getEnd().clone());
        return t;
    }

    public DataCollection clone(){
        return createFromByteArray(toByteArrayStream());
    }

    private static DataCollection createFromByteArray(byte[] array){
        try{
            ByteArrayInputStream fis = new ByteArrayInputStream(array);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DataCollection dc = (DataCollection)ois.readObject();
            ois.close();
            return dc;
        }catch(IOException ioe){
            System.out.println(ioe.toString());
        }catch(ClassNotFoundException cnfe){
            System.out.println(cnfe.toString());
        }
        return null;
    }

    private byte[] toByteArrayStream(){
        try{
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();

            return fos.toByteArray();
        }catch(IOException ioe){
            System.out.println(ioe.toString());
        }
        return null;
    }

    public static DataCollection createFromFile(File file){
        try{
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DataCollection dc = (DataCollection)ois.readObject();
            ois.close();
            fis.close();
            return dc;
        }catch(IOException ioe){
            System.out.println(ioe.toString());
        }catch(ClassNotFoundException cnfe){
            System.out.println(cnfe.toString());
        }
        return null;
    }

    public void save(File file){
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private void dumpTSEnsemble(File file, ArrayList<TimeSerieEnsemble> list) throws IOException{
        if (list.isEmpty())
            return;

        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        TimeSerieEnsemble array[] = list.toArray(new TimeSerieEnsemble[list.size()]);
        int N = array[0].size;
        int T = array[0].getTimesteps();
        
        BufferedWriter write = new BufferedWriter(new FileWriter(file));
        write.write("@context\n");
        write.write("jams.model.JAMSTemporalContext\tdump\t"+T+"\n");
        write.write("@ancestors"+"\n");
        write.write("optas.optimizer.generic\tsampler\t"+N+"\n");
        write.write("@filters"+"\n");
        write.write("@attributes"+"\n");
        String attrString = "ID";
        String types = "JAMSInteger";
        for (TimeSerieEnsemble s : list){
            attrString += "\t" + s.getName();
            types += "\t" + "JAMSDouble";
        }
        write.write(attrString+"\n");
        write.write("@types"+"\n");
        write.write(types+"\n");
        write.write("@data"+"\n");
        Integer ids[] = array[0].getIds();
        Arrays.sort(ids);

        for (int i=0;i<N;i++){
            write.write("sampler\t"+i+"\n");
            write.write("@start"+"\n");
            int id = ids[i];
            for (int t=0;t<T;t++){
                Date date = array[0].getDate(t);
                String entry = sdf.format(date);
                for (int j=0;j<array.length;j++){
                    entry += "\t" + array[j].get(t, id);
                }
                write.write(entry+"\n");
            }
            write.write("@end"+"\n");
        }
        write.close();
    }

    public void constructSample(SampleFactory f){
        ArrayList<SimpleEnsemble> simpleEnsembles = new ArrayList<SimpleEnsemble>();
        ArrayList<EfficiencyEnsemble> effEnsembles = new ArrayList<EfficiencyEnsemble>();

        for (String s : this.getDatasets()){
            DataSet d = this.getDataSet(s);
            if (d instanceof EfficiencyEnsemble)
                effEnsembles.add((EfficiencyEnsemble)d);
            else if (d instanceof SimpleEnsemble){
                simpleEnsembles.add((SimpleEnsemble)d);
            }
        }
        int n = simpleEnsembles.size();
        int m = effEnsembles.size();

        if (n == 0 || m == 0)
            return;

        int N = this.getSimulationCount();
        for (int i=0;i<N;i++){
            int id = simpleEnsembles.get(0).getId(0);
            double x[] = new double[n];
            double y[] = new double[m];

            for (int j=0;j<n;j++){
                x[j] = simpleEnsembles.get(j).getValue(id);
            }
            for (int j=0;j<m;j++){
                y[j] = simpleEnsembles.get(j).getValue(id);
            }
            f.getSample(x, y);
        }
    }

    private void dumpSimpleEnsemble(File file, ArrayList<SimpleEnsemble> list) throws IOException{
        if (list.isEmpty())
            return;

        int N = list.get(0).getSize();

        BufferedWriter write = new BufferedWriter(new FileWriter(file));
        write.write("@context"+"\n");
        write.write("dumping.context\tdump\t"+N+"\n");
        write.write("@ancestors"+"\n");
        write.write("@filters"+"\n");
        write.write("@attributes"+"\n");
        String attrString = "ID";
        String types = "JAMSInteger";
        for (SimpleEnsemble s : list){
            attrString += "\t" + s.getName();
            types += "\t" + "JAMSDouble";
        }
        write.write(attrString+"\n");
        write.write("@types"+"\n");
        write.write(types+"\n");
        write.write("@data"+"\n");
        write.write("@start"+"\n");

        SimpleEnsemble array[] = list.toArray(new SimpleEnsemble[list.size()]);
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
        write.write("@end");
        write.close();
    }

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public void dump(File directory){
        ArrayList<SimpleEnsemble> simpleEnsembles = new ArrayList<SimpleEnsemble>();
        ArrayList<TimeSerieEnsemble> tsEnsembles = new ArrayList<TimeSerieEnsemble>();
        ArrayList<Measurement> msEnsembles = new ArrayList<Measurement>();

        for (String s : this.getDatasets()){
            DataSet d = this.getDataSet(s);
            if (d instanceof SimpleEnsemble)
                simpleEnsembles.add((SimpleEnsemble)d);
            else if (d instanceof TimeSerieEnsemble)
                tsEnsembles.add((TimeSerieEnsemble)d);
            else if (d instanceof Measurement){
                msEnsembles.add((Measurement)d);
            }
        }
        try{
            dumpSimpleEnsemble(new File(directory.getAbsolutePath()+"/scalar_"+sdf2.format(new Date())+".dat"),simpleEnsembles);
            dumpTSEnsemble(new File(directory.getAbsolutePath()+"/timeseries_"+sdf2.format(new Date())+".dat"),tsEnsembles);
        }catch(IOException ioe){
            System.err.println(ioe);
            ioe.printStackTrace();
        }

    }

    static final long serialVersionUID = 3078644694169015704L;

    public static void main(String args[]){
        if (args[0].equals("merge")){
            if (args.length!=4){
                System.out.println("usage: merge src-file1 src-file2 dst-file");
            }
            File file1 = new File(args[1]);
            System.out.println("Source file1 ... " + file1.getAbsolutePath());
            File file2 = new File(args[2]);
            System.out.println("Source file2 ... " + file2.getAbsolutePath());
            File file_dst = new File(args[3]);
            System.out.println("Dest file ... " + file_dst.getAbsolutePath());
            System.out.println("Loading file1 ... ");
            DataCollection collection1 = DataCollection.createFromFile(file1);
            System.out.println("Loading file2 ... ");
            DataCollection collection2 = DataCollection.createFromFile(file2);
            System.out.println("Mergeing ... ");
            collection1.mergeDataCollections(collection2);
            System.out.println("Saving ... ");
            collection1.save(file_dst);
        }
    }
}
