/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import optas.data.api.DataSet;
import optas.data.api.DataSetChangeEvent;
import optas.data.api.DataSetContainer;
import optas.data.api.DataSetChangeListener;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class DefaultDataSet implements DataSet{    
    public String name = null;
    protected DataSetContainer parent = null;

    List<DataSetChangeListener> listeners = new ArrayList<>();
    Map<String, String> propertyMap = new HashMap<>();
    
    public DefaultDataSet(String name){
        checkNotNull(name);        
        this.name = name;
        this.parent = null;
    }
        
    public DefaultDataSet(DataSet d){
        this.name = d.getName();
        this.parent = d.getParent();
    }

    @Override
    public String getName(){
        return name;
    }
    
    @Override
    public void setName(String name){
        checkNotNull(name);  
        this.name = name;
    }
    
    @Override
    public DataSetContainer getParent(){
        return parent;
    }
    
    @Override
    public void setParent(DataSetContainer parent){
        this.parent = parent;
    }
 
    @Override
    public void addDatasetChangeListener(DataSetChangeListener dcl){
        checkNotNull(dcl, "DatasetChangeListener must not be null!");
        this.listeners.add(dcl);
    }
    
    @Override
    public void removeDatasetChangeListener(DataSetChangeListener dcl){
        checkNotNull(dcl, "DatasetChangeListener must not be null!");
        this.listeners.remove(dcl);
    }
    
    @Override
    public void fireDatasetChangeEvent(DataSetChangeEvent dce){
        checkNotNull(dce, "DatasetChangeEvent must not be null!");
        for (DataSetChangeListener dcl : listeners){
            dcl.datasetChanged(dce);
        }
        
        if (this.parent!=null){
            this.parent.fireDatasetChangeEvent(dce);
        }
    }

    @Override
    public void removeAllDatasetChangeListener(){
        this.listeners.clear();
    }
    
    @Override
    public String toString(){
        return name;
    }

    @Override
    public void setProperty(String key, String value) {
        checkNotNull(key, "Property must not be null!");
        propertyMap.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        checkNotNull(key, "Property must not be null!");
        return propertyMap.get(key);
    }
    
    @Override
    public Set<String> getProperties() {
        return Collections.unmodifiableSet(propertyMap.keySet());
    }
}
