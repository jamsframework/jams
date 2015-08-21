/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import static com.google.common.base.Preconditions.checkNotNull;
import optas.data.api.ListDataSet;
import optas.data.api.DataSet;
import optas.data.api.DataView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class DefaultListDataSet<T> extends DefaultDataSet implements ListDataSet<T> {

    final private DataView<T> dataset;

    public DefaultListDataSet(ListDataSet<T> copy) {
        super(copy);
        this.dataset = copy.values();
    }
    
    public DefaultListDataSet(DefaultListDataSet<T> copy, DataView<Integer> filter) {
        super(copy);
        this.dataset = copy.values().subset(filter);
    }

    public DefaultListDataSet(String name, DataView<T> set) {
        super(name);
        checkNotNull(set);
        this.dataset = set;
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
        return dataset;
    }
    
    @Override
    public DefaultListDataSet<T> filter(DataView<Integer> filter) {
        return new DefaultListDataSet(this, filter);
    }
}
