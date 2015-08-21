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
public class UnmodifiableListView<T> extends AbstractListViewDecorator<T> {
                            
    public UnmodifiableListView(DataView<T> supplier){
        super(supplier);
    }
    

    @Override
    public T setValue(int i, T value) {
        throw new UnsupportedOperationException("List View is not writable");
    }
    
    @Override
    public boolean isWritable(){
        return supplier.isWritable();
    }
}
