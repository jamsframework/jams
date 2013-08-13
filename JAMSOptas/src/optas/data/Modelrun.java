
package optas.data;

import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author chris
 */
public class Modelrun extends DataSet{
    
    private ArrayList<DataSet> datasets;
    private HashMap<Class, ArrayList<DataSet>> map;
    private SpatialDataSet spatialDataSet = null;
    private Integer id;

    TimeInterval timeDomain;

    static final long serialVersionUID = -9046263815810640999L;

    public Modelrun(Integer id, TimeInterval timeDomain){
        this.timeDomain = timeDomain;       
        datasets        = new ArrayList<DataSet>();
        map = new HashMap<Class, ArrayList<DataSet>>();
        this.id         = id;
    }

    public Integer getId(){
        return id;
    }

    public TimeInterval getTimeDomain(){
        return timeDomain;
    }

    public Iterator<DataSet> getDatasets(){
        return datasets.iterator();
    }
    
    public DataSet getDataset(String name){
        for (DataSet d : datasets){
            if (d.name.equals(name))
                return d;
        }
        return null;
    }
    
    public boolean hasSpatialDataSet() {
        return this.spatialDataSet != null;
    }
    
    public SpatialDataSet getSpatialDataSet() {
        return this.spatialDataSet;
    }

    public void addDataSet(DataSet set) throws MismatchException {
        this.addDataSet(null, set);
//        if (set instanceof SimpleDataSet){
//        } else if (set instanceof TimeSerie) {
//            addTimeSerie((TimeSerie)set);
//        }
//        registerDataSet(set);
    }
    
    public void addDataSet(Integer areaID, DataSet set) throws MismatchException {
        
        if (set instanceof TimeSerie) {
            addTimeSerie((TimeSerie)set);
        } else {            
        }
        
        if (areaID != null) {
            if (!hasSpatialDataSet()) {
                this.spatialDataSet = new SpatialDataSet();
            }
            this.spatialDataSet.addDatasetForAreaID(areaID, set);
        }
        registerDataSet(set);
    }

    private void registerDataSet(DataSet set){
        set.parent = this;
        this.datasets.add(set);

        if (!this.map.containsKey(set.getClass()))
            this.map.put(set.getClass(), new ArrayList<DataSet>());
        this.map.get(set.getClass()).add(set);
    }

    public void removeDataset(String name) {
        for (DataSet d : this.datasets) {
            if (d.name.equals(name)) {
                this.datasets.remove(d);
                for (ArrayList<DataSet> list : this.map.values()) {
                    if (list.contains(d)){
                        list.remove(d);
                    }
                }
                break;
            }
        }
    }
    private void addTimeSerie(TimeSerie timeserie) throws MismatchException{
        if (this.timeDomain==null){
            this.timeDomain = timeserie.getTimeDomain();
        }else
            if (!this.timeDomain.equals(timeserie.getTimeDomain()))
                throw new MismatchException("time domains of different series do not match:" + timeDomain.toString() + " vs. " + timeserie.getTimeDomain());
        timeserie.parent = this;        
    }
       
    public DataSet getDatasets(Class clazz, int index){
        return this.map.get(clazz).get(index);
    }
        
    public int getDatasetCount(Class clazz){
        return this.map.get(clazz).size();
    }
//this should only used with care!!!
    public void changeId(Integer id){
        this.id = id;
    }
}
