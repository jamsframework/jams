/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import jams.data.Attribute.TimeInterval;
import java.util.Date;
import java.util.Map;
import optas.data.AbstractDataSetDecorator;
import optas.data.api.DataView;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public abstract class AbstractTimeSerieDecorator<T> extends AbstractDataSetDecorator<TimeSerie<T>> implements TimeSerie<T> {

    protected AbstractTimeSerieDecorator(TimeSerie t) {
        super(t);
    }

    @Override
    public TimeInterval getTemporalDomain() {
        return this.dataset.getTemporalDomain();
    }

    @Override
    public Date getTime(int index) {
        return this.dataset.getTime(index);
    }
    
    @Override
    public T getValue(Date time) {
        return this.dataset.getValue(time);
    }
    
    @Override
    public TimeSerie<T> filter(TimeFilter filter) {
        return dataset.filter(filter);
    }

    @Override
    public TimeSerie<T> filter(DataView<Integer> filter){
        return dataset.filter(filter);
    }
            
    @Override
    public DataView<Date> dates() {
        return dataset.dates();
    }
    
    @Override
    public boolean isRegularTimeSerie() {
        return dataset.isRegularTimeSerie();
    }

    @Override
    public int getIndex(Date time) {
        return dataset.getIndex(time);
    }

    @Override
    public T getValue(int index) {
        return dataset.getValue(index);
    }

    @Override
    public T setValue(int index, T value) {
        return dataset.setValue(index, value);
    }

    @Override
    public int getNumberOfTimesteps() {
        return dataset.getNumberOfTimesteps();
    }

    @Override
    public DataView<T> values() {
        return dataset.values();
    }

    @Override
    public DataView<Map.Entry<Date, T>> map() {
        return dataset.map();
    }
}
