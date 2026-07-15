
package optas.data;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SpatialDataSet extends DataSet {
    
    private int[] index = null;
    private Map<Integer, DataSet> dataSets = null;
    
    public void addDatasetForAreaID(Integer id, DataSet dataSet) {
        if (this.dataSets == null) {
            this.dataSets = new TreeMap<Integer, DataSet>();
        }
        this.dataSets.put(id, dataSet);
        this.rebuildIndex();
    }
    
    public DataSet getDataSetForAreaID(int id) {
        return this.dataSets.get(id);
    }
    
    public Integer getIDAtIndex(int i) {
        return index[i];
    }
    
    public Set<Integer> getIDSet() {
        return this.dataSets.keySet();
    }
    
    public int getSize() {
        return index.length;
    }
    
    private void rebuildIndex() {
        index = new int[dataSets.size()];
        int i = 0;
        for (Object key : (TreeSet) dataSets.keySet()) {
            index[i] = (Integer) key;
            i++;
        }
    }
}
