/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time.api;

import jams.data.Attribute;
import java.util.Date;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.EntityDataSet;
import optas.data.api.MapDataSet;
import optas.data.time.DefaultTemporalSpatialDataSet;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 */
public interface TemporalSpatialDataSet<T> extends TimeSerie<EntityDataSet<T>> {

    static public <T> TemporalSpatialDataSet<T> getInstance(String name, DataSetContainer parent, DataView<Date> dates, DataView<EntityDataSet<T>> values) {
        return new DefaultTemporalSpatialDataSet<>(name, parent, dates, values);
    }
    
    static public <T> TemporalSpatialDataSet<T> getInstance(String name, DataSetContainer parent,MapDataSet<Date, EntityDataSet<T>> dataset) {
        return new DefaultTemporalSpatialDataSet<>(name, parent, dataset.keys(), dataset.values());
    }
    
    static public <T> TemporalSpatialDataSet<T> getInstance(String name, DataSetContainer parent,TemporalSpatialDataSet<T> dataset) {
        return new DefaultTemporalSpatialDataSet<>(name, parent, dataset);
    }

    static public <T> TemporalSpatialDataSet<T> getInstance(String name, DataSetContainer parent, Attribute.TimeInterval temporalDomain, DataView<EntityDataSet<T>> value) {
        return new DefaultTemporalSpatialDataSet<>(name, parent, temporalDomain, value);
    }
    
    static public <T> TemporalSpatialDataSet<T> getInstance(TemporalSpatialDataSet<T> dataset, DataView<Integer> filter) {
        return new DefaultTemporalSpatialDataSet<>(dataset, filter);
    }
}
