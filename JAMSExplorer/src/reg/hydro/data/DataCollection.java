/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro.data;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class DataCollection extends DataSet implements Serializable{
    TimeInterval timeDomain;    

    HashMap<Integer, Modelrun> set = new HashMap<Integer, Modelrun>();
    HashMap<String, DataSet> globalDatasets = new HashMap<String, DataSet>();

    HashMap<String, Class> datasets;
    Class samplerClass;

    public DataCollection() {
        datasets = new HashMap<String, Class>();
    }

    public void setSamplerClass(String typeId) {
        try {
            this.samplerClass = ClassLoader.getSystemClassLoader().loadClass(typeId);
        } catch (ClassNotFoundException cnfe) {
//            this.samplerClass = jams.components.optimizer.Optimizer.class;
        }
    }

    public Class getSamplerClass() {
        if (samplerClass == null) {
//            return jams.components.optimizer.Optimizer.class;
        }
        return samplerClass;
    }


    public void mergeDataCollections(DataCollection dc){
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
    }

    public Integer[] getModelrunIds(){
        return set.keySet().toArray(new Integer[set.keySet().size()]);
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

    public void addTimeSerie(TimeSerie s){
        globalDatasets.put(s.name,s);
        this.datasets.put(s.name, s.getClass());
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
    }
    public void removeModelRun(Integer id) {
        set.put(id, null);
    }

    public int getSimulationCount() {
        return this.set.size();
    }
    public Set<String> getDatasets(){
        Set<String> result = new HashSet<String>();
        result.addAll(datasets.keySet());        
        return result;
    }
    public Set<String> getDatasets(Class clazz){
        TreeSet<String> sets = new TreeSet<String>();
        for (String setname : datasets.keySet()){
            if (getDatasetClass(setname).equals(clazz))
                sets.add(setname);
        }
        return sets;
    }
    public Class getDatasetClass(String name){
        return this.datasets.get(name);
    }

    public DataSet getDataSet(String dataset){
        if (this.getDatasetClass(dataset).equals(TimeSerie.class))
            return getTimeserieEnsemble(dataset);
        else if (this.getDatasetClass(dataset).equals(Measurement.class))
            return this.globalDatasets.get(dataset);
        else{
            SimpleEnsemble e = getSimpleEnsemble(dataset);
            if (this.getDatasetClass(dataset).equals(Efficiency.class)){
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
        sortedKeySet.addAll(this.set.keySet());

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
            //se.set(i, id[i], value[i]);
            se.add(id[i],value[i]);
        }
        return se;
    }

    public TimeSerieEnsemble getTimeserieEnsemble(String dataset){
        double value[][] = new double[this.set.keySet().size()][];
        Integer id[] = new Integer[this.set.keySet().size()];

        int c=0;

        TimeInterval unifiedTimeInterval = JAMSDataFactory.createTimeInterval();
        Calendar c1 = JAMSDataFactory.createCalendar();
        Calendar c2 = JAMSDataFactory.createCalendar();

        long startTime = 0;
        long endTime   = Long.MAX_VALUE;

        int timeUnit = 0;
        int timeUnitCount = 0;

        for (Integer s : this.set.keySet()){
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

        for (Integer s : this.set.keySet()){
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
        TimeSerieEnsemble se = new TimeSerieEnsemble(dataset,c);
        for (int i =0;i<c;i++){
            se.set(id[i], value[i]);
        }

        return se;
    }

    public TimeInterval getTimeDomain() {
        if (timeDomain == null)
            return null;
        TimeInterval t = JAMSDataFactory.createTimeInterval();
        t.setStart(timeDomain.getStart().clone());
        t.setEnd(timeDomain.getEnd().clone());
        return t;
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


    public static void main(String args[]){
        if (args[0].equals("merge")){
            if (args.length!=4){
                System.out.println("usage: merge src-file1 src-file2 dst-file");
            }
            File file1 = new File(args[1]);
            System.out.println("Source file1 ... " + file1.getAbsolutePath());
            File file2 = new File(args[2]);
            System.out.println("Source file2 ... " + file1.getAbsolutePath());
            File file_dst = new File(args[3]);
            System.out.println("Dest file ... " + file1.getAbsolutePath());
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
