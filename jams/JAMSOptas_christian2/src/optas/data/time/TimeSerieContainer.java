/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import jams.data.Attribute;
import java.util.Date;
import optas.data.api.DataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;
import optas.data.time.api.TimeSerie;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public abstract class TimeSerieContainer<T extends DataSet> extends DefaultTimeSerie<T> implements DataSetContainer<Date, T> {
        
    public TimeSerieContainer(String name, DataSetContainer parent, DataView<Date> dates, DataView<T> values) {
        super(name, parent, dates, values);
    }
    
    public TimeSerieContainer(String name, DataSetContainer parent, MapDataSet<Date, T> dataset) {
        super(name, parent, dataset.keys(), dataset.values());
    }
    
    public TimeSerieContainer(String name, DataSetContainer parent, TimeSerie<T> dataset) {
        super(name, parent, dataset);
    }
        
    public TimeSerieContainer(String name, DataSetContainer parent, Attribute.TimeInterval temporalDomain, DataView<T> value) {
        super(name, parent, temporalDomain, value);
    } 
    
    public TimeSerieContainer(DefaultTimeSerie dataset, DataView<Integer> filter) {
        super(dataset, filter);
    }
        
    @Override
    public DataView<T> getDataSets() {
        return this.values();
    }

    @Override
    public T getDataSet(Date time) {
        return this.getValue(time);
    }    

    /*@Override
    public DataSupplier<T> getDataSets(Class clazz) {
        
    }*/
}
