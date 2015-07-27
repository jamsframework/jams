/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author christian
 * @param <T>
 */
public interface Model<T extends Model> extends Serializable{    
    public interface ModelDataChangeListener extends Serializable{
        public void changed(Model model, String key);
    }
    
    public void addModelDataChangeListener(ModelDataChangeListener listener);    
    public void removeModelDataChangeListener(ModelDataChangeListener listener);
    public void removeAllModelDataChangeListener();
    
    Set<String> getProperties();
    String getProperty(String key);
    void setProperty(String key, String value);
    T copy();
    void delete() throws IOException;
    Set<String> getOutputs();    
    int getID();
    void setID(int id);
}
