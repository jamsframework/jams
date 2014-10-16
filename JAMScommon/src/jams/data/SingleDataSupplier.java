/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.data;

/**
 *
 * @author christian
 */
public class SingleDataSupplier<T> extends AbstractDataSupplier<T, T> {
        
    public SingleDataSupplier(T input) {
        super(input);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public T get(int i) {        
        return input;
    }
}
