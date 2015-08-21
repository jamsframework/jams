/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import optas.data.api.ListDataSet;
import optas.data.api.DataView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * @param <T>
 * @param <U>
 */
public class AbstractListDataSetDecorator<T, U extends ListDataSet<T>> extends AbstractDataSetDecorator<U> implements ListDataSet<T> {
            
    public AbstractListDataSetDecorator(U dataset) {
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
    public DataView<T> values() {
        return dataset.values();
    }   

    @Override
    public ListDataSet<T> filter(DataView<Integer> filter) {
        return dataset.filter(filter);
    }
}
