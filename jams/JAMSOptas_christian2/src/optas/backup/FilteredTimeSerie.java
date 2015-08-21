/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.backup;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import optas.data.api.DataView;
import optas.data.time.api.TimeSerie;
import optas.data.view.ViewFactory;
import optas.data.time.AbstractTimeSerieDecorator;
import optas.data.time.DefaultTimeFilter;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public class FilteredTimeSerie<T> extends AbstractTimeSerieDecorator<T> {
    final DefaultTimeFilter filter;
    final FilteredMapDataSet<Date, T> filteredSupplier=null;
    
    public FilteredTimeSerie(TimeSerie<T> set, DefaultTimeFilter filter) {
        super(set);
        checkNotNull(filter, "Filter must not be null!");
        
        this.filter = filter;
        
        //filteredSupplier = set.filter(mapFilteredTimeSteps(filter));
    }

    private DataView<Integer> mapFilteredTimeSteps(DefaultTimeFilter filter) {
        ArrayList<Integer> indexMap = new ArrayList<>();
        indexMap.clear();
        for (int i = 0; i < dataset.getNumberOfTimesteps(); i++) {
            Date d = dataset.getTime(i);
            if (!filter.isFiltered(d)) {
                indexMap.add(i);
            }
        }
        return ViewFactory.createView(indexMap);
    }
        
    @Override
    public Date getTime(int index) {
        return filteredSupplier.getKey(index);
    }    
        
    @Override
    public T getValue(int index) {
        return filteredSupplier.getValue(index);
    }
    
    @Override
    public T setValue(int index, T value) {
        return filteredSupplier.setValue(index, value);
    }
    
    @Override
    public T getValue(Date time) {
        int index = getIndex(time);
        if (index != -1){
            return getValue(index);            
        }
        return null;
    }
        
    @Override
    public int getIndex(Date key) {
        return filteredSupplier.getIndex(key);
    }  

    @Override
    public DataView<T> values() {
        return filteredSupplier.values();
    }

    @Override
    public DataView<Date> dates() {
        return filteredSupplier.keys();
    }
    
    @Override
    public DataView<Entry<Date,T>> map() {
        return filteredSupplier.map();
    }    
}
