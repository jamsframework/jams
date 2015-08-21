/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Set;
import optas.data.api.DataSet;
import optas.data.api.DataSetChangeEvent;
import optas.data.api.DataSetContainer;
import optas.data.api.DataSetChangeListener;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T> extension of DataSet
 */
public class AbstractDataSetDecorator<T extends DataSet> implements DataSet{    
    protected T dataset = null;

    public AbstractDataSetDecorator(T dataset){
        checkNotNull(dataset);
        this.dataset = dataset;
    }

    @Override
    public String getName(){
        return dataset.getName();
    }
    
    @Override
    public void setName(String name){
        dataset.setName(name);
    }
    
    @Override
    public DataSetContainer getParent(){
        return dataset.getParent();
    }
    
    @Override
    public void setParent(DataSetContainer parent){
        dataset.setParent(parent);
    }
    
    @Override
    public String toString(){
        return dataset.toString();
    }

    @Override
    public void addDatasetChangeListener(DataSetChangeListener dcl) {
        dataset.addDatasetChangeListener(dcl);
    }

    @Override
    public void removeDatasetChangeListener(DataSetChangeListener dcl) {
        dataset.removeDatasetChangeListener(dcl);
    }

    @Override
    public void removeAllDatasetChangeListener() {
        dataset.removeAllDatasetChangeListener();
    }
    
    @Override
    public void fireDatasetChangeEvent(DataSetChangeEvent dce) {
        dataset.fireDatasetChangeEvent(dce);
    }

    @Override
    public void setProperty(String key, String value) {
        dataset.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return dataset.getProperty(key);
    }

    @Override
    public Set<String> getProperties() {
        return dataset.getProperties();
    }
}
