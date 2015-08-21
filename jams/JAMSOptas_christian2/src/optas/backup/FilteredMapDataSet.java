/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.backup;

import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Map.Entry;
import optas.data.AbstractMapDataSetDecorator;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;
import optas.core.OPTASException;
import optas.data.view.AbstractListView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <K>
 * @param <T>
 */
public class FilteredMapDataSet<K, T> extends AbstractMapDataSetDecorator<K, T,MapDataSet<K, T>> implements MapDataSet<K, T> {

    DataView<Integer> filter;
    public FilteredMapDataSet(MapDataSet<K, T> dataset, DataView<Integer> filter) {
        super(dataset);                
        this.filter = filter;
        
        precheckFilter();
    }

    private void precheckFilter(){
        checkNotNull(filter, "Filter must not be null!");
        for (int index : filter){
            Preconditions.checkArgument(index < 0 || index > dataset.getSize(), 
                    "Failed to filter dataset %s. The filtered value %s is out of bounds!", dataset.getName(), index );
        }
    }
    
    @Override
    public int getSize() {
        return filter.getSize();
    }
        
    @Override
    public T getValue(int index) {
        return dataset.getValue(filter.getValue(index));
    }
    
    @Override
    public T setValue(int index, T value) {
        return dataset.setValue(filter.getValue(index), value);
    }
    
    @Override
    public K getKey(int index) {
        return dataset.getKey(filter.getValue(index));
    }
    
    @Override
    public K setKey(int index, K key) {
        return dataset.setKey(filter.getValue(index), key);
    }
    
    @Override
    public Entry<K,T> getEntry(int index) {
        return dataset.getEntry(filter.getValue(index));
    }
    
    @Override
    public DataView<T> values() {
        return new AbstractListView<T, Void>(null){

            @Override
            public int getSize() {
                return FilteredMapDataSet.this.getSize();
            }

            @Override
            public T getValue(int i) {
                return FilteredMapDataSet.this.getValue(i);
            }

            @Override
            public T setValue(int i, T value) {
                return FilteredMapDataSet.this.setValue(i, value);
            }
        };
    }
    
    @Override
    public DataView<K> keys() {
        return new AbstractListView<K, Void>(null){

            @Override
            public int getSize() {
                return FilteredMapDataSet.this.getSize();
            }

            @Override
            public K getValue(int i) {
                return FilteredMapDataSet.this.getKey(i);
            }

            @Override
            public K setValue(int i, K key) {
                return FilteredMapDataSet.this.setKey(i, key);
            }
        };
    }
    
    @Override
    public DataView<Entry<K, T>> map() {
        return new AbstractListView<Entry<K, T>, Void>(null){

            @Override
            public int getSize() {
                return FilteredMapDataSet.this.getSize();
            }

            @Override
            public Entry<K, T> getValue(int i) {
                return FilteredMapDataSet.this.getEntry(i);
            }

            @Override
            public Entry<K, T> setValue(int i, Entry<K, T> entry) {
                FilteredMapDataSet.this.setKey(i, entry.getKey());
                FilteredMapDataSet.this.setValue(i, entry.getValue());
                
                return null;
            }
        };
    }
    
    @Override
    public FilteredMapDataSet<K, T> filter(DataView<Integer> filter){        
        return new FilteredMapDataSet<>((this), filter);
    }
    /*
    @Override
    public MapDataSet<K, T> unfilter(){
        return this.dataset;
    }
    
    @Override
    public DataSupplier<Integer> getFilter(){
        return filter;
    }*/
}
