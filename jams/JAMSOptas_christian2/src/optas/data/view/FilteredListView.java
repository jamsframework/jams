/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import optas.data.api.DataView;
import optas.core.OPTASException;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public class FilteredListView<T> extends AbstractListViewDecorator<T> {
                
    DataView<Integer> subset = null;
            
    public FilteredListView(DataView<T> supplier, DataView<Integer> subset){
        super(supplier);
        
        //check if subset is compatible
        for (int index : subset){
            if (index < 0 || index > supplier.getSize()){
                throw new OPTASException("Failed to filter data supplier. The filtered value " + index + " is out of bounds!");
            }
        }
        this.subset = subset;
    }
    
    @Override
    public int getSize(){
        return subset.getSize();
    }
    @Override
    public T getValue(int i){
        return supplier.getValue(subset.getValue(i));
    }

    @Override
    public T setValue(int i, T value) {
        T oldValue = getValue(i);
        supplier.setValue(subset.getValue(i), value);
        return oldValue;
    }
    
    @Override
    public boolean isWritable(){
        return supplier.isWritable();
    }
}
