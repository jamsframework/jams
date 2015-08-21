/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import java.util.Iterator;
import optas.data.api.DataView;

/**
 *
 * @author christian
 * @param <T>
 */
abstract class AbstractListViewDecorator<T> implements DataView<T>{
        
    final DataView<T> supplier;
    
    private class BasicIterator implements Iterator<T> {

        int pos = -1;

        @Override
        public boolean hasNext() {
            return pos < AbstractListViewDecorator.this.getSize()-1;
        }

        @Override
        public T next() {
            return AbstractListViewDecorator.this.getValue(++pos);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    public AbstractListViewDecorator(DataView<T> supplier){
        this.supplier = supplier;
    }
    
    @Override
    public int getSize(){
        return supplier.getSize();
    }
    
    @Override
    public T getValue(int i){
        return supplier.getValue(i);
    }
    
    @Override
    public T setValue(int i, T value){
        return supplier.setValue(i, value);
    }
    
    @Override
    public boolean isWritable(){
        return supplier.isWritable();
    }
    
    @Override
    public Iterator<T> iterator() {
        return new AbstractListViewDecorator.BasicIterator();
    }
    
    @Override
    public DataView<T> subset(DataView<Integer> subsetIndices){
        return supplier.subset(subsetIndices);
    }
}
