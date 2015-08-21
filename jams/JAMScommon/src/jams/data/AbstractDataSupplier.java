/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.data;

import java.util.Iterator;

/**
 *
 * @author christian
 * @param <T>
 * @param <U>
 */
public abstract class AbstractDataSupplier<T, U> implements DataSupplier<T> {
    protected U input;
    
    private class BasicIterator implements Iterator<T> {

        int pos = -1;

        @Override
        public boolean hasNext() {
            return pos < AbstractDataSupplier.this.size()-1;
        }

        @Override
        public T next() {
            return AbstractDataSupplier.this.get(++pos);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public AbstractDataSupplier(U input){
        this.input = input;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new BasicIterator();
    }
}
