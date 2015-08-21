/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import optas.data.api.DataView;

/**
 *
 * @author christian
 * @param <T>
 */
public class DeepCopyListView<T> extends AbstractListView<T, Void> {
                                
    final T data[];
    
    public DeepCopyListView(DataView<T> supplier){
        super(null);
        
        Preconditions.checkNotNull(supplier,"Supplier must not be null!");
        
        data = (T[])(new Object[supplier.getSize()]);
        
        for (int i=0;i<supplier.getSize();i++){
            data[i] = supplier.getValue(i);
        }
    }
    
    @Override
    public int getSize(){
        return data.length;
    }
    
    @Override
    public T getValue(int i){
        return data[i];
    }

    @Override
    public T setValue(int i, T value) {
        return data[i] = value;
    }
    
    @Override
    public boolean isWritable(){
        return true;
    }
}
