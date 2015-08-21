/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

import java.util.Map.Entry;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 */
public interface MapDataSet<K,T> extends DataSet{
    K getKey(int index);
    K setKey(int index, K key);
    T getValue(int index);
    T getValue(K key);
    T setValue(int index, T value);
    Entry<K,T> getEntry(int index);
    int getIndex(K key);
    
    int getSize();
    
    DataView<T> values();
    DataView<K> keys();
    DataView<Entry<K,T>> map();
    
    MapDataSet<K,T> filter(DataView<Integer> filter);    
}
