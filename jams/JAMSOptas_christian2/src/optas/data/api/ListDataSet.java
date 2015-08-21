/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

import optas.backup.FilteredListDataSet;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 */
public interface ListDataSet<T> extends DataSet{
    T getValue(int index);
    T setValue(int index, T value);
    
    int getSize();
    
    DataView<T> values();
    
    ListDataSet<T> filter(DataView<Integer> filter);
}
