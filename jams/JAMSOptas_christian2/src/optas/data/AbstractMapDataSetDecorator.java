/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import java.util.Map.Entry;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <K> typ of key
 * @param <T> typ of values
 * @param <U> typ of the input
 */
public class AbstractMapDataSetDecorator<K, T, U extends MapDataSet<K, T>> extends AbstractDataSetDecorator<U> implements MapDataSet<K, T> {
            
    public AbstractMapDataSetDecorator(U dataset) {
        super(dataset);
    }

    @Override
    public int getSize() {
        return dataset.getSize();
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
    public K getKey(int index) {
        return dataset.getKey(index);
    }

    @Override
    public K setKey(int index, K key) {
        return dataset.setKey(index, key);
    }
    
    @Override
    public Entry<K,T> getEntry(int index) {
        return dataset.getEntry(index);
    }

    @Override
    public int getIndex(K key) {
        return dataset.getIndex(key);
    }

    @Override
    public DataView<T> values() {
        return dataset.values();
    }
    
    @Override
    public DataView<K> keys() {
        return dataset.keys();
    }
    
    @Override
    public DataView<Entry<K, T>> map() {
        return dataset.map();
    }

    @Override
    public MapDataSet<K, T> filter(DataView<Integer> filter) {
        return dataset.filter(filter);
    }

    @Override
    public T getValue(K key) {
        return dataset.getValue(key);
    }
}
