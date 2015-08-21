/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import jams.data.Attribute.TimeInterval;
import java.util.Date;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.EntityDataSet;
import optas.data.time.api.TemporalSpatialDataSet;
import optas.data.time.api.TimeSerie;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public class DefaultTemporalSpatialDataSet<T> extends DefaultTimeSerie<EntityDataSet<T>> implements TemporalSpatialDataSet<T> {
    
    public DefaultTemporalSpatialDataSet(String name, DataSetContainer parent, DataView<Date> dates, DataView<EntityDataSet<T>> values) {
        super(name, parent, dates, values);
    }
        
    public DefaultTemporalSpatialDataSet(String name, DataSetContainer parent, TimeSerie<EntityDataSet<T>> dataset) {
        super(name, parent, dataset.dates(), dataset.values());
    }
        
    public DefaultTemporalSpatialDataSet(String name, DataSetContainer parent, TimeInterval temporalDomain, DataView<EntityDataSet<T>> value) {
        super(name, parent, temporalDomain, value);
    } 
    
    public DefaultTemporalSpatialDataSet(TemporalSpatialDataSet<T> dataset, DataView<Integer> filter) {
        super(dataset, filter);
    }
    
    public DefaultTemporalSpatialDataSet(TemporalSpatialDataSet<T> dataset) {
        super(dataset.getName(), dataset.getParent(), dataset);
    }
}
