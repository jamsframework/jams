/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface DataSet extends Serializable{
    String getName();
    void setName(String name);
    
    DataSetContainer getParent();
    void setParent(DataSetContainer parent);
    
    void addDatasetChangeListener(DataSetChangeListener dcl);
    void removeDatasetChangeListener(DataSetChangeListener dcl);
    void removeAllDatasetChangeListener();
    
    void fireDatasetChangeEvent(DataSetChangeEvent dce);
    
    void setProperty(String key, String value);
    String getProperty(String key);
    Set<String> getProperties();
}
