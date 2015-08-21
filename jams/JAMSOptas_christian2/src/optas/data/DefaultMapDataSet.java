/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import static com.google.common.base.Preconditions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;
import optas.data.view.AbstractListView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class DefaultMapDataSet<K, T> extends DefaultDataSet implements MapDataSet<K, T> {

    final private DataView<K> keys;
    final private DataView<T> values;
    
    private Map<K, T> map;

    final int size;
    
    public DefaultMapDataSet(MapDataSet<K, T> copy) {
        super(copy);
        this.keys = copy.keys();
        this.values = copy.values();
        this.size = copy.getSize();
        buildMapping();
    }
    
    protected DefaultMapDataSet(DefaultMapDataSet<K, T> copy, DataView<Integer> filter) {
        super(copy);
        checkNotNull(filter, "Filter must not be null!");
        this.keys = copy.keys().subset(filter);
        this.values = copy.values().subset(filter);
        this.size = copy.getSize();    
        
        buildMapping();
    }

    public DefaultMapDataSet(String name, DataSetContainer parent, DataView<K> keys, DataView<T> values) {
        super(name);
        checkNotNull(keys, "Argument keys was expected to be not null!");
        checkNotNull(values, "Argument values was expected to be not null!");
        checkArgument(keys.getSize() == values.getSize(), "Argument keys and values were expected to have the same size but "
                + "the size of keys is %s and the size of values is %s!", keys.getSize(), values.getSize());
        
        this.setParent(parent);
        
        this.keys = keys;
        this.values = values;
        
        this.size = keys.getSize();
        
        buildMapping();
    }

    private void buildMapping(){
        map = new HashMap<>();
        
        for (int i=0;i<size;i++){
            map.put(keys.getValue(i), values.getValue(i));
        }
    }
    
    @Override
    public int getSize() {
        return size;
    }
        
    @Override
    public T getValue(int index) {
        return values.getValue(index);
    }
    
    @Override
    public T getValue(K key) {
        return values.getValue(getIndex(key));
    }
    
    @Override
    public T setValue(int index, T value) {
        K key = getKey(index);
        map.remove(key);
        map.put(key, value);
        return values.setValue(index, value);
    }

    @Override
    public Entry<K, T> getEntry(int index) {
        return new Node(index);
    }
    
    @Override
    public DataView<K> keys() {
        return keys;
    }
    
    @Override
    public DataView<T> values() {
        return values;
    }
    
    @Override
    public MapDataSet<K, T> filter(DataView<Integer> filter) {
        checkNotNull(filter, "Filter must not be null!");
        return new DefaultMapDataSet<>(this, filter);
    }

    @Override
    public K getKey(int index) {
        return keys.getValue(index);
    }

    @Override
    public K setKey(int index, K key) {
        return keys.setValue(index, key);
    }

    @Override
    public int getIndex(K arg) {
        for (int i=0;i<keys.getSize();i++){
            if (keys.getValue(i).equals(arg)){
                return i;
            }
        }
        return -1;
    }

    private class Node implements Map.Entry<K, T> {

        int index = 0;

        public Node(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return DefaultMapDataSet.this.getKey(index);
        }

        @Override
        public T getValue() {
            return DefaultMapDataSet.this.getValue(index);
        }

        @Override
        public T setValue(T value) {
            throw new IllegalAccessError("Not allowed to change value of map()");
        }
        
        @Override
        public String toString(){
            return "[" + getKey() + "," + getValue() + "]";
        }

    }

    @Override
    public DataView<Entry<K, T>> map() {
        return new AbstractListView<Entry<K, T>, MapDataSet>(this) {

            @Override
            public int getSize() {
                return input.getSize();
            }

            @Override
            public Map.Entry<K, T> getValue(int i) {
                return new Node(i);
            }

            @Override
            public Entry<K, T> setValue(int i, Entry<K, T> value) {
                throw new UnsupportedOperationException("setValue is not supported by map");
            }
        };
    }
}
