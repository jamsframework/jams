
package optas.hydro.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SpatialDataSet extends DataSet {
    
    private Map<Integer, Area> areas = null;
    private Map<Integer, DataSet> dataSets = null; // ein Pool von Areas für alle Modelruns
    // map area id -> dataset
    // index mapping für IDs
    
    public void addArea(Area area) {
        if (this.areas != null) {
            this.areas = new HashMap<Integer, Area>();
        }
        
        // determine highest id value
        Set<Integer> keys = this.areas.keySet();
        int maxID = -1;
        for (int id : keys) {
            if (maxID < id) {
                maxID = id;
            }
        }
        
        this.areas.put(maxID + 1, area);
    }
    
    // methode für alle ares hinzufügen
    
    public Area getAreaForID(int id) {
        return this.areas.get(id);
    }
    
    public Set<Integer> getIDSet() {
        return this.areas.keySet();
    }
}
