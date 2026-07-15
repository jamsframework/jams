/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.implementation;

import jams.explorer.ensembles.api.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author christian
 */
public abstract class AbstractModel<T extends Model> implements Model<T>{
    private HashMap<String, String> propertyMap = new HashMap<String, String>();                            
    transient ArrayList<ModelDataChangeListener> listeners = new ArrayList<ModelDataChangeListener>();
    int id;
    
    public AbstractModel(int id){
        setID(id);
    }
    
    @Override
    public void addModelDataChangeListener(ModelDataChangeListener listener){
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    
    @Override
    public void removeModelDataChangeListener(ModelDataChangeListener listener){
        listeners.remove(listener);
    }
    
    @Override
    public void removeAllModelDataChangeListener(){
        listeners.clear();
    }
        
    public AbstractModel(AbstractModel m){
        this.propertyMap.putAll(m.propertyMap);        
    }
    
    @XmlTransient
    @Override
    public Set<String> getProperties() {
        return getPropertyMap().keySet();
    }

    @Override
    public String getProperty(String key) {
        return getPropertyMap().get(key);
    }
    
    @Override
    public void setProperty(String key, String value) {
        getPropertyMap().put(key, value);
        for (ModelDataChangeListener listener : listeners){
            listener.changed(this, key);
        }
    }
            
    @Override
    public int getID(){
        return id;        
    }
    
    @Override
    public void setID(int id){
        this.id = id;
    }
    
    /**
     * @return the propertyMap
     */
    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    /**
     * @param propertyMap the propertyMap to set
     */
    public void setPropertyMap(HashMap<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }          
}
