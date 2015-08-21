/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.backup;

import optas.data.AbstractListDataSetDecorator;
import static com.google.common.base.Preconditions.checkNotNull;
import optas.data.api.ListDataSet;
import optas.data.api.DataView;
import optas.core.OPTASException;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class FilteredListDataSet<T> extends AbstractListDataSetDecorator<T,ListDataSet<T>> implements ListDataSet<T> {

    DataView<Integer> filter;
    public FilteredListDataSet(ListDataSet<T> dataset, DataView<Integer> filter) {
        super(dataset);                
        this.filter = filter;
        
        precheckFilter();
    }

    private void precheckFilter(){
        checkNotNull(filter);
        for (int index : filter){
            if (index < 0 || index > dataset.getSize())
                throw new OPTASException("Failed to filter dataset " + dataset.getName() + ". The filtered value " + index + " is out of bounds ");
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
    public FilteredListDataSet<T> filter(DataView<Integer> filter){        
        return new FilteredListDataSet<>((this), filter);
    }
    /*
    @Override
    public ListDataSet<T> unfilter(){
        return this.dataset;
    }
    
    @Override
    public DataSupplier<Integer> getFilter(){
        return filter;
    }*/
}
