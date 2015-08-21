/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time.api;

import jams.data.Attribute;
import java.util.Date;
import java.util.Map;
import optas.data.api.DataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;
import optas.data.time.DefaultTimeSerie;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 */
public interface TimeSerie<T> extends DataSet {

    static public <T> TimeSerie<T> getInstance(String name, DataSetContainer parent, DataView<Date> dates, DataView<T> values) {
        return new DefaultTimeSerie<>(name, parent, dates, values);
    }
    
    static public <T> TimeSerie<T> getInstance(String name, DataSetContainer parent,MapDataSet<Date, T> dataset) {
        return new DefaultTimeSerie<>(name, parent, dataset.keys(), dataset.values());
    }
    
    static public <T> TimeSerie<T> getInstance(String name, DataSetContainer parent,TimeSerie<T> dataset) {
        return new DefaultTimeSerie<>(name, parent, dataset);
    }

    static public <T> TimeSerie<T> getInstance(String name, DataSetContainer parent,Attribute.TimeInterval temporalDomain, DataView<T> value) {
        return new DefaultTimeSerie<>(name, parent, temporalDomain, value);
    }
    
    static public <T> TimeSerie<T> getInstance(DefaultTimeSerie dataset, DataView<Integer> filter) {
        return new DefaultTimeSerie<>(dataset, filter);
    }    
    
    Date getTime(int index);
    int getIndex(Date time);
    
    T getValue(int index);
    T getValue(Date time);
    T setValue(int index, T value);
    
    Attribute.TimeInterval getTemporalDomain();
    int getNumberOfTimesteps();
    boolean isRegularTimeSerie();
    
    DataView<Date> dates();
    DataView<T> values();
    DataView<Map.Entry<Date,T>> map();
    
    TimeSerie<T> filter(TimeFilter filter);       
    TimeSerie<T> filter(DataView<Integer> filter);        
}
