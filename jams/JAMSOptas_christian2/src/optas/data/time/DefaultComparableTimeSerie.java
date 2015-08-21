/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import jams.JAMS;
import jams.data.Attribute;
import java.util.Date;
import optas.data.api.DataSetContainer;
import optas.data.time.api.ComparableTimeSerie;
import optas.data.api.DataView;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;
import optas.typetools.main.java.net.jodah.typetools.TypeResolver;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public class DefaultComparableTimeSerie<T extends Comparable> extends AbstractTimeSerieDecorator<T> implements ComparableTimeSerie<T> {
    
    private Class<T> parameterType = (Class<T>)TypeResolver.resolveRawArguments(DefaultComparableTimeSerie.class, getClass())[0];
    
    public DefaultComparableTimeSerie(TimeSerie<T> set) {
        super(set);
    }
    
    public DefaultComparableTimeSerie(String name, DataSetContainer parent, Attribute.TimeInterval timeDomain, DataView<T> value) {
        super(new DefaultTimeSerie<>(name, parent, timeDomain, value));
    }

    public DefaultComparableTimeSerie(String name, DataSetContainer parent, DataView<Date> dates, DataView<T> value) {
        super(new DefaultTimeSerie<>(name, parent, dates, value));
    }
        
    private boolean isMissingData(T value){
        return value.compareTo(JAMS.getMissingDataValue(parameterType))!=0;
    }
    
    @Override
    public int getArgMin() {
        T min = null;
        int index = -1;
        for (int i = 0; i < this.getNumberOfTimesteps(); i++) {
            T value = dataset.getValue(i);
            if ( !isMissingData(value) && (index == -1 || value.compareTo(min)<0)) {
                min = value;
                index = i;
            }
        }

        return index;
    }

    @Override
    public int getArgMax() {
        T max = null;
        int index = -1;
        for (int i = 0; i < this.getNumberOfTimesteps(); i++) {
            T value = dataset.getValue(i);
            if ( !isMissingData(value) && (index == -1 || value.compareTo(max)>0)) {
                max = value;
                index = i;
            }
        }

        return index;
    }

    @Override
    public T getMin() {
        int index = getArgMin();
        if (index >= 0) {
            return dataset.getValue(index);
        } else {
            return JAMS.getMissingDataValue(parameterType);
    
        }
    }

    @Override
    public T getMax() {
        int index = getArgMax();
        if (index >= 0) {
            return dataset.getValue(index);
        } else {
            return JAMS.getMissingDataValue(parameterType);
    
        }
    }    

    @Override
    public DefaultComparableTimeSerie<T> filter(DataView<Integer> filter) {
        return new DefaultComparableTimeSerie<>(this.dataset.filter(filter));
    }
    
    @Override
    public DefaultComparableTimeSerie<T> filter(TimeFilter filter) {
        return new DefaultComparableTimeSerie<>(this.dataset.filter(filter));
    }
}
